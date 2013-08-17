package com.testwidget.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.testwidget.App;
import com.testwidget.CardDescriptor;
import com.testwidget.CardDescriptor.LastUsageInfo;
import com.testwidget.CardDescriptor.RechargeInfo;
import com.testwidget.R;
import com.testwidget.dataprovider.DataProvider;

public class ShowAllActivity extends Activity {

	private static final int REQ_CODE = 1001;
	
	private String cardNumber;
	private DataProvider dp = App.getDataProvider();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_all_layout);
		
		Intent intent = getIntent();
		cardNumber = intent.getStringExtra(IntentConstants.CARD_NUMBER);
		Log.i("SHOWALL", "received "+cardNumber);
		
		CardDescriptor cd = dp.getByNumber(cardNumber);
		prepareTextViews(cd);
	}

	private void prepareTextViews(CardDescriptor cd) {
		Resources resources = getResources();
		setDescription(R.id.card_name_block,
				resources.getString(R.string.card_name_string), cd.getCardName());
		setDescription(R.id.card_balance_block,
				resources.getString(R.string.card_balance_string), cd.getBalanceString());
		setDescription(R.id.card_number_block,
				resources.getString(R.string.card_number_string), cd.getCardNumber());
		setDescription(R.id.card_type_block,
				resources.getString(R.string.card_type_string), cd.getCardType());
		setDescription(R.id.last_activated_block,
				resources.getString(R.string.last_activated_string),
				cd.getActivationDate().getFormattedString());
		setDescription(R.id.valid_until_block,
				resources.getString(R.string.valid_until_string), cd.getValidUntil().getFormattedString());
		LastUsageInfo lastUsageInfo = cd.getLastUsageInfo();
		setDescription(R.id.last_used_date_block,
				resources.getString(R.string.date_string),
				lastUsageInfo.getDate().getFormattedString());
		setDescription(R.id.last_used_transport_type_block,
				resources.getString(R.string.last_used_transport_type_string),
				lastUsageInfo.getTransportType());
		setDescription(
				R.id.last_used_transport_number_block,
				resources.getString(R.string.last_used_transport_number_string),
				lastUsageInfo.getTransportNumber());
		setDescription(R.id.last_used_charge_type_block,
				resources.getString(R.string.last_used_charge_type_string),
				lastUsageInfo.getOperationType());
		RechargeInfo rechargeInfo = cd.getRechargeInfo();
		setDescription(R.id.recharged_on_block,
				resources.getString(R.string.date_string),
				rechargeInfo.getRechargeDate().getFormattedString());
		setDescription(R.id.recharged_at_block,
				resources.getString(R.string.recharge_location_string),
				rechargeInfo.getRechargeLocation());
		setDescription(R.id.recharged_by_block,
				resources.getString(R.string.recharge_amount_string),
				rechargeInfo.getRechargeAmountString());
		setDescription(R.id.last_updated_block,
				resources.getString(R.string.last_updated_string),
				cd.getLastUpdated().getFormattedString());
	}

	private void setDescription(int layoutId, String columnDescription,
			String columnValue) {
		LinearLayout block = (LinearLayout) findViewById(layoutId);
		TextView columnDescriptionView = (TextView) block
				.findViewById(R.id.description_tf);
		columnDescriptionView.setText(columnDescription);
		TextView columnValueView = (TextView) block.findViewById(R.id.value_tf);
		columnValueView.setText(columnValue);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		MenuItem findItem = menu.findItem(R.id.add_new_card_item);
		NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		if (nfcAdapter != null && nfcAdapter.isEnabled()) {
			findItem.setIcon(getResources().getDrawable(R.drawable.add_card_icon_nfc));
		}
//		findItem.setI
		return true;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.i("showall", "on resume");
		invalidateOptionsMenu();
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.i("showall", "on pause");
		invalidateOptionsMenu();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.i("menu click", item.getItemId() + "");
		switch (item.getItemId()) {
		case R.id.add_new_card_item: {
			Intent intent = new Intent(this, AddNewCardActivity.class);
			startActivity(intent);
			return true;
		}
		case R.id.update_item: {
//			Rect rect = getIntent().getSourceBounds();
			Intent intent = new Intent(this, UpdateActivity.class);
//			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//			intent.putExtra(IntentConstants.LEFT_BORDER_INDEX, rect.left);
//			intent.putExtra(IntentConstants.BOTTOM_BORDER_INDEX, rect.bottom);
//			intent.putExtra(IntentConstants.WIDTH, rect.right - rect.left);
			intent.putExtra(IntentConstants.CARD_NUMBER,cardNumber);
			startActivityForResult(intent, REQ_CODE);
			return true;
		}
		default: {
			return super.onOptionsItemSelected(item);
		}
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i("show all", "returned");
		if(requestCode == REQ_CODE){
			Log.i("show all", "reqcode OK "+resultCode);
			if(resultCode == RESULT_OK){
				Log.i("show all", "res code OK");
				CardDescriptor cd = dp.getByNumber(cardNumber);
				prepareTextViews(cd);
			}
		}
	}

}
