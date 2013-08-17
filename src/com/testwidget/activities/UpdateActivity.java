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
import android.widget.ProgressBar;
import android.widget.Toast;

import com.testwidget.App;
import com.testwidget.CardDescriptor;
import com.testwidget.R;
import com.testwidget.TranskartManager;
import com.testwidget.App.Duration;
import com.testwidget.TranskartManager.DocumentValidationException;
import com.testwidget.TranskartManager.TranskartSession;

public class UpdateActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(new ProgressBar(this));
		setFinishOnTouchOutside(true);
		try {
			new CaptchaHandler(this).execute();
		} catch (Exception e) {
			Log.e("activity", "blabla error", e);
		}
	}
	
	private static class CaptchaHandler extends AsyncTask<Void, Void, Drawable>{
		private Throwable throwable;
		private TranskartSession session;
		private Activity activity;
		
		public CaptchaHandler(Activity activity) {
			this.activity = activity;
		}
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
				Log.i("update","width "+c.getWidth()+" height "+c.getHeight());;
				c.setScaleType(ScaleType.FIT_CENTER);
				c.setImageDrawable(captcha);
				//TODO drawable must fill view
				Button ok = (Button) activity.findViewById(R.id.button1);
				ok.setOnClickListener(new CardDescriptorUpdater(session,
						activity));
			}else{
				App.showToast(activity, "SOME ERROR>I DONT'T KNOW WHAT HAPPENED", Duration.LONG);
			}
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
					EditText et = (EditText) activity.findViewById(R.id.captcha_text_view);
					Editable captchaValue = et.getText();
					try {
						String cardNumber = activity.getIntent()
								.getStringExtra(IntentConstants.CARD_NUMBER);
						CardDescriptor cardDescriptor = s
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
