package com.testwidget.activities;

import java.io.IOException;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.testwidget.App;
import com.testwidget.App.Duration;
import com.testwidget.CardDescriptor;
import com.testwidget.R;
import com.testwidget.TranskartManager;
import com.testwidget.TranskartManager.DocumentValidationException;
import com.testwidget.TranskartManager.TranskartSession;
import com.testwidget.dataprovider.DataProvider.DuplicateCardException;

public class AddNewCardActivity extends Activity {
	private ProgressBar pb;
	private LinearLayout captchaLayout;
	private TextView captchaTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_new_card);
		
		try {
			new CaptchaGrabber().execute();
		} catch (Exception e) {
			//TODO HANDLER
			Log.e("activity", "blabla error", e);
		}

		pb = (ProgressBar) findViewById(R.id.progressBar1);
		captchaLayout = (LinearLayout) findViewById(R.id.captcha_layout);
		captchaTextView = (TextView) findViewById(R.id.captcha_text_view_);
		
		Button backButton = (Button) findViewById(R.id.back_button);

		backButton.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
		    	setResult(RESULT_CANCELED);
		    	finish();
		    }
		});
		
		ImageView captchaImageView = (ImageView) findViewById(R.id.captcha_image_view_);
		captchaImageView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				try {
					new CaptchaGrabber().execute();
				} catch (Exception e) {
					//TODO HANDLER
					Log.e("activity", "blabla error", e);
				}
			}
		});

	}
	
	private class CaptchaGrabber extends AsyncTask<Void, Void, Drawable> {
		
		private static final double SCALE_COEFFICIENT = 1.5;
		
		private TranskartSession session;
		private Throwable throwable;
		private Activity activity = AddNewCardActivity.this;

		@Override
		protected Drawable doInBackground(Void... params) {
			try {
				activity.runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						pb.setVisibility(View.VISIBLE);	
						captchaLayout.setVisibility(View.GONE);
					}
				});
				TranskartManager m = new TranskartManager(activity);
				this.session = m.startSession();
				return session.getCaptcha();
				
			} catch (Throwable t) {
				this.throwable = t;
				return null;
			}
		}
		
		@Override
		protected void onPostExecute(Drawable captcha) {
			if(throwable == null){
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						pb.setVisibility(View.GONE);
						captchaLayout.setVisibility(View.VISIBLE);
					}
				});
				ImageView c = (ImageView) activity.findViewById(R.id.captcha_image_view_);
				Drawable scaledCaptcha = App.scaleDrawable(
						activity.getResources(), captcha, SCALE_COEFFICIENT,
						SCALE_COEFFICIENT);
				c.setImageDrawable(scaledCaptcha);
				Button ok = (Button) activity.findViewById(R.id.confirm_button);
				ok.setOnClickListener(new CardDescriptorUpdater(session));
			}else{
				App.showToast(activity, "SOME ERROR>I DONT'T KNOW WHAT HAPPENED", Duration.LONG);
				Log.i("error","msg",throwable);
			}
		};
	}
	
	private class CardDescriptorUpdater implements View.OnClickListener {

		private TranskartSession s;
		private Activity activity = AddNewCardActivity.this;

		public CardDescriptorUpdater(TranskartSession s) {
			this.s = s;
		}

		@Override
		public void onClick(View v) {
			new AsyncTask<Void, Void, CardDescriptor>() {

				@Override
				protected CardDescriptor doInBackground(Void... params) {
					EditText et = (EditText) activity.findViewById(R.id.captcha_text_view_);
					Editable captchaValue = et.getText();
					try {
						TextView cardNumberTF = (TextView) activity.findViewById(R.id.card_number_tf);
						String cardNumber = cardNumberTF.getText().toString();
						if(cardNumber.trim().isEmpty()){
							App.showToast(activity, "empty card number", Duration.LONG);
						}
						CardDescriptor cardDescriptor = s
								.getCardDescriptor(captchaValue
										.toString(), cardNumber);
						TextView cardNameTF = (TextView) activity.findViewById(R.id.card_name_tf);
						String cardName = cardNameTF.getText().toString();
						if(cardName.trim().isEmpty()){
							App.showToast(activity, "empty name", Duration.LONG);
						}
						cardDescriptor.setCardName(cardName);
						return cardDescriptor;
					} catch (IOException e) {
						App.showToast(activity, "io error", Duration.LONG);
					} catch (DocumentValidationException e) {
						App.showToast(activity,e.getMessage(), Duration.LONG);
					} catch (Exception ex){
						App.showToast(activity,"unknown error", Duration.LONG);
					}
					return null;
				}

				@Override
				protected void onPostExecute(final CardDescriptor result) {
					if (result != null) {
						try{
							App.getDataProvider().saveCard(result);
						}catch(DuplicateCardException dex){
							App.showToast(activity, "dup card number", Duration.LONG);
							return;
						}
						TranskartWidget.updateAllWidgets(activity.getApplicationContext());
						App.showToast(activity,
								"Карта '" + result.getCardName()
										+ "' была успешно добавлена",
								Duration.LONG);
						setResult(RESULT_OK);
						TranskartWidget.updateAllWidgets(getApplicationContext());
						finish();
					}else{
						new CaptchaGrabber().execute();
						captchaTextView.setText("");
					}
				}
			}.execute();
		}
	}
	
}
