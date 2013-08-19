package com.testwidget;

import android.app.Activity;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.nfc.NfcAdapter;
import android.nfc.tech.MifareClassic;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.testwidget.dataprovider.DataProvider;
//3894706279 2938811773
public class App extends Application {
	
	private static DataProvider dataProvider;
	
	@Override
	public void onCreate() {
		super.onCreate();
		dataProvider = new DataProvider(getApplicationContext());
		Log.i("app", "app started "+ String.valueOf(dataProvider.size()));
//		DataProvider dp = App.getDataProvider();
//		CardDescriptor cd = new CardDescriptor();
//		cd.setCardName("a2");
//		cd.setCardNumber(		"3894706279");
//		cd.setBalance(514);
//		cd.setLastUpdated(new Date());
//		dp.saveOrUpdateCard(cd);
//		
//		CardDescriptor cd2 = new CardDescriptor();
//		cd2.setCardName("a");
//		cd2.setCardNumber(		"2938811773");
//		cd2.setBalance(544);
//		cd2.setLastUpdated(new Date());
//		dp.saveOrUpdateCard(cd2);
	}

	public static DataProvider getDataProvider() {
		return dataProvider;
	}
	
	public enum Duration{
		LONG(Toast.LENGTH_LONG),
		SHORT(Toast.LENGTH_SHORT);
		private int value;
		private Duration(int value){
			this.value=value;
		}
	}
	
	public static void showToast(final Activity activity, final String text,
			final Duration duration) {
		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(activity, text, duration.value).show();
			}
		});
	}
	
	public static Drawable scaleDrawable(Resources resources, Drawable source,
			double scaleX, double scaleY) {
		Bitmap bitmap = ((BitmapDrawable) source).getBitmap();
		int bw = bitmap.getWidth();
		int bh = bitmap.getHeight();
		return new BitmapDrawable(resources,
				Bitmap.createScaledBitmap(bitmap, 
						(int)(bw*scaleX),
						(int)(bh*scaleY), true));
	}
	
	public static void setupForegroundDispatch(final Activity activity,
			NfcAdapter adapter){
		final Intent intent = new Intent(activity.getApplicationContext(),
				activity.getClass());
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		final PendingIntent pendingIntent = PendingIntent.getActivity(
				activity.getApplicationContext(), 0, intent, 0);
		IntentFilter[] filters = new IntentFilter[1];
		String[][] techList = new String[][] { new String[] { MifareClassic.class
				.getName() } };
		
		filters[0] = new IntentFilter();
		filters[0].addAction(NfcAdapter.ACTION_TECH_DISCOVERED);
		try {
			filters[0].addDataType("*/*");
		} catch (MalformedMimeTypeException e) {
			//never happens
		}
		adapter.enableForegroundDispatch(activity, pendingIntent, filters,
				techList);
	}

	public static void stopForegroundDispatch(final Activity activity,
			NfcAdapter adapter) {
		adapter.disableForegroundDispatch(activity);
	}

	public static void closeAfterDelay(final Activity activity, int delay) {
		new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {

			@Override
			public void run() {
				activity.finish();

			}
		}, delay);
	}

}
