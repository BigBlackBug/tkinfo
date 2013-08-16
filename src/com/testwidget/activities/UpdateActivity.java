package com.testwidget.activities;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
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
import android.widget.ImageView.ScaleType;
import android.widget.Toast;

import com.testwidget.App;
import com.testwidget.CardDescriptor;
import com.testwidget.R;
import com.testwidget.TranskartManager;
import com.testwidget.TranskartManager.DocumentValidationException;
import com.testwidget.TranskartManager.TranskartSession;

public class UpdateActivity extends Activity {

	private static class Actor extends AsyncTask<TranskartManager, Void, Drawable>{
		private Throwable throwable;
		private TranskartSession session;
		private String cardNumber;
		private Activity activity;
		
		public Actor(String cardNumber, Activity activity) {
			this.cardNumber = cardNumber;
			this.activity = activity;
		}
		@Override
		protected Drawable doInBackground(
				TranskartManager... params) {
			try {
				TranskartManager manager = params[0];
				this.session = manager
						.startSession(cardNumber);
				return session.getCaptcha();
				
			} catch (Throwable t) {
				this.throwable = t;
				return null;
			}
			
		}
		@Override
		protected void onPostExecute(Drawable captcha) {
			if(throwable == null){
				ImageView c = (ImageView) activity.findViewById(R.id.captcha_image_view);
				Log.i("update","width "+c.getWidth()+" height "+c.getHeight());;
				c.setScaleType(ScaleType.FIT_CENTER);
				c.setImageDrawable(captcha);
				//TODO drawable must fill view
				Button ok = (Button) activity.findViewById(R.id.button1);
				ok.setOnClickListener(new CardDescriptorUpdater(session,
						activity));
			}else{
				activity.runOnUiThread(new Runnable() {
				    public void run() {
				        Toast.makeText(activity, "SOME ERROR>I DONT'T KNOW WHAT HAPPENED", Toast.LENGTH_LONG).show();
				    }
				});
			}
		}
		
	}
	
	private String cardNumber;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.update_layout);
		setFinishOnTouchOutside(true);
		// overridePendingTransition
		cardNumber = getIntent().getStringExtra(IntentConstants.CARD_NUMBER);
		//TODO display rotating something
		try {
			TranskartManager m = new TranskartManager(this);
			new Actor(cardNumber, this).execute(m);
		} catch (Exception e) {
			Log.e("activity", "blabla error", e);
		}

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
					EditText et = (EditText) activity.findViewById(R.id.editText1);
					Editable captchaValue = et.getText();
					try {
						CardDescriptor cardDescriptor = s
								.getCardDescriptor(String.valueOf(captchaValue
										.toString()));
						return cardDescriptor;
					} catch (IOException e) {
						Log.e("activity", "blabla click error", e);
						activity.runOnUiThread(new Runnable() {
						    public void run() {
						        Toast.makeText(activity, "IO EXCPETION", Toast.LENGTH_LONG).show();
						    }
						});
						return null;
					} catch (final DocumentValidationException e) {
						activity.runOnUiThread(new Runnable() {
						    public void run() {
						        Toast.makeText(activity, e.getMessage(), Toast.LENGTH_LONG).show();
						    }
						});
						return null;
					}
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
						activity.runOnUiThread(new Runnable() {
						    public void run() {
								Toast.makeText(activity,
										"Информация о карте '"
												+ result.getCardName()
												+ "' была успешно обновлена",
										Toast.LENGTH_LONG).show();
						    }
						});
					} else {
						activity.setResult(RESULT_CANCELED);
					}
					
					activity.finish();
				}
			}.execute();
		}
	}
	//FIXME position
	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		Log.i("activity", "onAtttachedToWindow");
		View view = getWindow().getDecorView();
		WindowManager.LayoutParams lp = (WindowManager.LayoutParams) view
				.getLayoutParams();
		lp.gravity = Gravity.LEFT | Gravity.TOP;
		Intent intent = getIntent();
		int left = intent.getIntExtra(IntentConstants.LEFT_BORDER_INDEX, 0);
		int bot = intent.getIntExtra(IntentConstants.BOTTOM_BORDER_INDEX, 0);
		int width = intent.getIntExtra(IntentConstants.WIDTH, 0);
		Log.i("activity", "received left " + left + "bot " + bot + "width "
				+ width);
		lp.x = left;
		lp.y = bot;
		getWindowManager().updateViewLayout(view, lp);
	}

}
