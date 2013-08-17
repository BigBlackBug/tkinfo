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
import android.widget.ImageView.ScaleType;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_new_card);

		pb = (ProgressBar) findViewById(R.id.progressBar1);
		captchaLayout = (LinearLayout) findViewById(R.id.captcha_layout);
		
		Button btn = (Button) findViewById(R.id.back_button);

		btn.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
		    	setResult(RESULT_CANCELED);
		    	finish();
		    }
		});

		new AsyncTask<Void, Void, Drawable>() {
			private TranskartSession session;
			private Throwable throwable;
			private Activity activity =  AddNewCardActivity.this;
			@Override
			protected Drawable doInBackground(Void... params) {
				try {
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
					runOnUiThread(new Runnable() {
						public void run() {
							pb.setVisibility(View.GONE);
							captchaLayout.setVisibility(View.VISIBLE);
						}
					});
					ImageView c = (ImageView) activity.findViewById(R.id.captcha_image_view_);
					Log.i("update","width "+c.getWidth()+" height "+c.getHeight());;
					c.setScaleType(ScaleType.FIT_CENTER);
					c.setImageDrawable(captcha);
					Button ok = (Button) activity.findViewById(R.id.confirm_button);
					ok.setOnClickListener(new CardDescriptorUpdater(session,
							activity));
				}else{
					App.showToast(activity, "SOME ERROR>I DONT'T KNOW WHAT HAPPENED", Duration.LONG);
				}
			};
		}.execute();
	}
	
	private static class CardDescriptorUpdater implements View.OnClickListener {

		private TranskartSession s;
		private Activity activity;

		public CardDescriptorUpdater(TranskartSession s, Activity a) {
			this.s = s;
			this.activity = a;
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
					}
				}
			}.execute();
		}
	}
	
}
