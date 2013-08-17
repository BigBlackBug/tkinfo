package com.testwidget.activities;

import java.io.IOException;
import java.nio.ByteBuffer;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import com.testwidget.App;
import com.testwidget.R;
import com.testwidget.App.Duration;

public class NFCActivity extends Activity {

	public static final String TAG = "NfcDemo";
	private TextView mTextView;
	private NfcAdapter mNfcAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test_nfc_activity);
		mTextView = (TextView) findViewById(R.id.card_number_tf);
		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
		if (mNfcAdapter == null) {
			App.showToast(this, "This device doesn't support NFC.", Duration.LONG);
			finish();
			return;
		}
		if (!mNfcAdapter.isEnabled()) {
			mTextView.setText("NFC is disabled.");
		} else {
			mTextView.setText("NFC is enabled");
		}
		handleIntent(getIntent());
	}

	@Override
	protected void onResume() {
		super.onResume();
		setupForegroundDispatch(this, mNfcAdapter);
	}

	@Override
	protected void onPause() {
		stopForegroundDispatch(this, mNfcAdapter);
		super.onPause();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		handleIntent(intent);
	}

	private void handleIntent(Intent intent) {
		String action = intent.getAction();
		if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
			Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			NfcA nfcA = NfcA.get(tagFromIntent);
			MifareClassic mfc = MifareClassic.get(tagFromIntent);
			byte[] data;

			try {
				mfc.connect();
				boolean auth = false;
				int blockIndex = 0;
				int sectorIndex = 0;
				auth = mfc.authenticateSectorWithKeyA(sectorIndex,
						MifareClassic.KEY_DEFAULT);
				if (auth) {
					data = mfc.readBlock(blockIndex);

					ByteBuffer wrapped = ByteBuffer.wrap(new byte[] { 00, 00,
							00, 00, data[3], data[2], data[1], data[0] });
					long num = wrapped.getLong(); // 1

					mTextView.setText(String.valueOf(num));
				} else {
					// Authentication failed - Handle it
				}
			} catch (IOException e) {
				Log.e(TAG, "connection dropped", e);
			}
		}
	}

	public static void setupForegroundDispatch(final Activity activity,
			NfcAdapter adapter) {
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
			throw new RuntimeException("Check your mime type.");
		}
		adapter.enableForegroundDispatch(activity, pendingIntent, filters,
				techList);
	}

	public static void stopForegroundDispatch(final Activity activity,
			NfcAdapter adapter) {
		adapter.disableForegroundDispatch(activity);
	}

}
