package com.testwidget.activities;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.testwidget.App;
import com.testwidget.App.Duration;
import com.testwidget.CardDescriptor;
import com.testwidget.R;
import com.testwidget.TranskartManager;
import com.testwidget.TranskartManager.DocumentValidationException;
import com.testwidget.TranskartManager.TranskartSession;

public class UpdateActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(new ProgressBar(this));
		setFinishOnTouchOutside(true);
		try {
			new CaptchaGrabber().execute();
		} catch (Exception e) {
			Log.e("activity", "blabla error", e);
		}
	}
	
	private class CaptchaGrabber extends AsyncTask<Void, Void, Drawable>{
		
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
			}else{
				App.showToast(activity, "SOME ERROR>I DONT'T KNOW WHAT HAPPENED", Duration.LONG);
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
						App.showToast(activity, "io exception", Duration.LONG);
					} catch (final DocumentValidationException e) {
						App.showToast(activity, e.getMessage(), Duration.LONG);
					} catch (Exception ex){
						App.showToast(activity,"unknown error", Duration.LONG);
					}
					return null;
				}

				@Override
				protected void onPostExecute(final CardDescriptor result) {
					if (result != null) {
						CardDescriptor old = App.getDataProvider().getByNumber(
								result.getCardNumber());
						result.setCardName(old.getCardName());
						App.getDataProvider().saveOrUpdateCard(result);
						TranskartWidget.updateAllWidgets(activity.getApplicationContext());
						activity.setResult(RESULT_OK);
						App.showToast(activity,"Информация о карте '"
								+ result.getCardName()
								+ "' была успешно обновлена", Duration.LONG);
					} else {
						activity.setResult(RESULT_CANCELED);
					}
					
					activity.finish();
				}
			}.execute();
		}
	}
	
}
