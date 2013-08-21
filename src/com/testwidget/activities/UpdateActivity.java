package com.testwidget.activities;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.qbix.transkartwidget.App;
import com.qbix.transkartwidget.App.Duration;
import com.qbix.transkartwidget.CardDescriptor;
import com.qbix.transkartwidget.R;
import com.qbix.transkartwidget.TranskartManager;
import com.qbix.transkartwidget.TranskartManager.DocumentValidationException;
import com.qbix.transkartwidget.TranskartManager.TranskartSession;
import com.testwidget.dataprovider.DataProvider.CardSavingException;

public class UpdateActivity extends Activity {

	private static final String TAG = "update_activity";
	private Resources resources;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(new ProgressBar(this));
		setFinishOnTouchOutside(true);
		this.resources = getResources();
		try {
			new CaptchaFetcher().execute();
		} catch (Exception e) {
			App.showToast(this, 
					getString(R.string.internal_app_error),
					Duration.LONG);
			Log.e(TAG, "internal app error", e);
			setResult(RESULT_CANCELED);
			finish();
		}
	}
	
	private class CaptchaFetcher extends AsyncTask<Void, Void, Drawable>{
		
		private static final double SCALE_COEFFICIENT = 1.5;
		
		private Throwable throwable;
		private TranskartSession session;
		private Activity activity = UpdateActivity.this;
		
		@Override
		protected Drawable doInBackground(
				Void... params) {
			try {
				TranskartManager manager = new TranskartManager(activity);
				this.session = manager.startSession();
				return session.getCaptcha();
			} catch (Throwable t) {
				this.throwable = t;
				return null;
			}
			
		}
		
		@Override
		protected void onPostExecute(Drawable captcha) {
			if(throwable == null){
				activity.setContentView(R.layout.update_layout);
				ImageView c = (ImageView) activity.findViewById(R.id.captcha_image_view);
				Drawable scaledCaptcha = App.scaleDrawable(
						activity.getResources(), captcha, SCALE_COEFFICIENT,
						SCALE_COEFFICIENT);
				c.setImageDrawable(scaledCaptcha);
				Button ok = (Button) activity.findViewById(R.id.button1);
				ok.setOnClickListener(new CardDescriptorUpdater(session));
				
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT , 0);
			} else {
				if(throwable instanceof IOException){
					App.showToast(
							activity,
							resources.getString(R.string.network_connection_error),
							Duration.LONG);
				}else{
					App.showToast(activity,
							resources.getString(R.string.internal_app_error),
							Duration.LONG);	
					Log.e(TAG, "internal app error", throwable);
				}
				setResult(RESULT_CANCELED);
				finish();
			}
		}
		
	}

	private class CardDescriptorUpdater implements View.OnClickListener {

		private TranskartSession session;
		private Activity activity = UpdateActivity.this;

		public CardDescriptorUpdater(TranskartSession session) {
			this.session = session;
		}

		@Override
		public void onClick(View v) {
			new AsyncTask<Void, Void, CardDescriptor>() {

				@Override
				protected CardDescriptor doInBackground(Void... params) {
					EditText et = (EditText) activity.findViewById(R.id.captcha_text_view);
					Editable captchaValue = et.getText();
					try {
						String cardNumber = activity.getIntent()
								.getStringExtra(IntentConstants.CARD_NUMBER);
						CardDescriptor cardDescriptor = session
								.getCardDescriptor(captchaValue
										.toString(), cardNumber);
						return cardDescriptor;
					} catch (IOException e) {
						App.showToast(
								activity, 
								resources.getString(R.string.network_connection_error),
								Duration.LONG);
					} catch (final DocumentValidationException e) {
						App.showToast(activity, e.getMessage(), Duration.LONG);
					} catch (Exception ex){
						Log.e(TAG, "",ex);
						App.showToast(activity,
								resources.getString(R.string.internal_app_error),
								Duration.LONG);
					}
					return null;
				}

				@Override
				protected void onPostExecute(final CardDescriptor result) {
					if (result != null) {
						CardDescriptor old = App.getDataProvider().getByNumber(
								result.getCardNumber());
						result.setCardName(old.getCardName());
						try {
							App.getDataProvider().saveOrUpdateCard(result);
							App.showToast(activity, resources.getString(
									R.string.card_updating_success,
									result.getCardNumber()), Duration.LONG);
						} catch (CardSavingException ex) {
							App.showToast(activity, resources.getString(
									R.string.error_writing_card_to_disk,
									result.getCardNumber()), Duration.LONG);
						}
						TranskartWidget.updateAllWidgets(activity.getApplicationContext());
						activity.setResult(RESULT_OK);
					} else {
						activity.setResult(RESULT_CANCELED);
					}
					
					activity.finish();
				}
			}.execute();
		}
	}
	
}
