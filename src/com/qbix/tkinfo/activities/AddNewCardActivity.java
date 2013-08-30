package com.qbix.tkinfo.activities;

import java.io.IOException;
import java.nio.ByteBuffer;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
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

import com.qbix.tkinfo.App;
import com.qbix.tkinfo.App.Duration;
import com.qbix.tkinfo.R;
import com.qbix.tkinfo.model.CardDescriptor;
import com.qbix.tkinfo.model.TranskartManager;
import com.qbix.tkinfo.model.DataProvider.CardSavingException;
import com.qbix.tkinfo.model.DataProvider.DuplicateCardException;
import com.qbix.tkinfo.model.TranskartManager.DocumentValidationException;
import com.qbix.tkinfo.model.TranskartManager.TranskartSession;

public class AddNewCardActivity extends Activity {
	private static final String TAG = "add_new_card_activity";
	
	private ProgressBar pb;
	private LinearLayout captchaLayout;
	private TextView captchaTextView;
	private Resources resources;
	private TextView cardNumberTf;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("add", "on create");
		setContentView(R.layout.activity_add_new_card);
		this.resources = getResources();
		
		try {
			new CaptchaFetcher().execute();
		} catch (Exception e) {
			App.showToast(this, resources.getString(R.string.internal_app_error),
					Duration.LONG);
			Log.e(TAG, "error fetching captcha", e);
			finish();
		}

		pb = (ProgressBar) findViewById(R.id.progressBar1);
		captchaLayout = (LinearLayout) findViewById(R.id.captcha_layout);
		captchaTextView = (TextView) findViewById(R.id.captcha_text_view_);
		cardNumberTf = (TextView) findViewById(R.id.card_number_tf);
		
		
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
					new CaptchaFetcher().execute();
				} catch (Exception e) {
					App.showToast(AddNewCardActivity.this,
							resources.getString(R.string.internal_app_error),
							Duration.LONG);
					Log.e(TAG, "error fetching captcha", e);
					finish();
				}
			}
		});
		
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.i("add", "on resume");
		NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		if (nfcAdapter != null) {
			App.setupForegroundDispatch(this, nfcAdapter);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.i("add", "on pause");
		NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		if (nfcAdapter != null) {
			App.stopForegroundDispatch(this, nfcAdapter);
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		String action = intent.getAction();
		if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
		Tag intentTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		MifareClassic mfc = MifareClassic.get(intentTag);
		byte[] data;

		try {
			mfc.connect();
			
			int blockIndex = 0;
			int sectorIndex = 0;
			boolean auth = mfc.authenticateSectorWithKeyA(sectorIndex,
					MifareClassic.KEY_DEFAULT);
			if (auth) {
				data = mfc.readBlock(blockIndex);

				ByteBuffer wrapped = ByteBuffer.wrap(new byte[] { 00, 00,
						00, 00, data[3], data[2], data[1], data[0] });
				long cardNumber = wrapped.getLong(); 

				cardNumberTf.setText(String.valueOf(cardNumber));
			} else {
				//TODO Authentication failed - Handle it
			}
		} catch (IOException e) {
			Log.e("tag", "connection dropped", e);
			}
		}
	}

	private class CaptchaFetcher extends AsyncTask<Void, Void, Drawable> {
		
		private static final int ACTIVITY_CLOSE_DELAY = 1500;
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
				ok.setOnClickListener(new CardDescriptorSaver(session));
			}else{
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
				App.closeActivityAfterDelay(activity, ACTIVITY_CLOSE_DELAY);
				Log.i(TAG, "error fetching captcha", throwable);
			}
		};
	}
	
	private class CardDescriptorSaver implements View.OnClickListener {

		private TranskartSession s;
		private Activity activity = AddNewCardActivity.this;

		public CardDescriptorSaver(TranskartSession s) {
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
							App.showToast(activity, resources
									.getString(R.string.empty_card_number_field),
									Duration.LONG);
						}
						CardDescriptor cardDescriptor = s
								.getCardDescriptor(captchaValue
										.toString(), cardNumber);
						TextView cardNameTF = (TextView) activity.findViewById(R.id.card_name_tf);
						String cardName = cardNameTF.getText().toString();
						if(cardName.trim().isEmpty()){
							App.showToast(activity, resources
									.getString(R.string.empty_card_name_field),
									Duration.LONG);
						}
						cardDescriptor.setCardName(cardName);
						return cardDescriptor;
					} catch (IOException e) {
						App.showToast(
								activity, 
								resources.getString(R.string.network_connection_error),
								Duration.LONG);
					} catch (DocumentValidationException e) {
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
						try {
							App.getDataProvider().saveCard(result);
							App.showToast(activity, resources.getString(
									R.string.card_adding_success,
									result.getCardNumber()), Duration.LONG);
						} catch (DuplicateCardException dex) {
							App.showToast(activity,
									resources.getString(
											R.string.card_already_saved,
											result.getCardNumber()), Duration.LONG);
							return;
						} catch (CardSavingException ex) {
							App.showToast(activity, resources.getString(
									R.string.error_writing_card_to_disk,
									result.getCardNumber()), Duration.LONG);
						}
						setResult(RESULT_OK);
						TranskartWidget.updateAllWidgets(getApplicationContext());
						finish();
					}else{
						new CaptchaFetcher().execute();
						captchaTextView.setText("");
					}
				}
			}.execute();
		}
	}
	
}
