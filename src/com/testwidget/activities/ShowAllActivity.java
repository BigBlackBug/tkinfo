package com.testwidget.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.qbix.transkartwidget.App;
import com.qbix.transkartwidget.App.Duration;
import com.qbix.transkartwidget.BackKeyPressListener;
import com.qbix.transkartwidget.CardDescriptor;
import com.qbix.transkartwidget.CardDescriptor.LastUsageInfo;
import com.qbix.transkartwidget.CardDescriptor.RechargeInfo;
import com.qbix.transkartwidget.R;
import com.qbix.transkartwidget.TranskartEditText;
import com.testwidget.dataprovider.DataProvider;
import com.testwidget.dataprovider.DataProvider.CardSavingException;

public class ShowAllActivity extends Activity {

	private static final String TAG = "show_all_activity";

	private static final int REQ_CODE = 1001;
	
	private String cardNumber;
	private CardDescriptor selectedCard;
	private DataProvider dp = App.getDataProvider();
	private Resources resources;
	
	private TranskartEditText nameValueEditText;
	private View mainLayout;
	private TextView valueTextView ;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.resources = getResources();
		
		setContentView(R.layout.show_all_layout);
		
		Intent intent = getIntent();
		cardNumber = intent.getStringExtra(IntentConstants.CARD_NUMBER);
		Log.i(TAG, "received "+cardNumber);
		
		selectedCard = dp.getByNumber(cardNumber);
		fillTextViews();
		
		valueTextView = (TextView) findViewById(R.id.__name_value_tf);
		
		LinearLayout nameLayout = (LinearLayout) findViewById(R.id.name_layout);
		nameLayout.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View view) {
				Log.i(TAG, "clicked on layout view.");
				valueTextView.setVisibility(View.GONE);
				
				EditText valueEditText = (EditText) findViewById(R.id.___name_et);
				valueEditText.setVisibility(View.VISIBLE);
				valueEditText.requestFocus();
				
				InputMethodManager imm = (InputMethodManager) 
						getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.toggleSoftInputFromWindow(nameValueEditText.getWindowToken(),
						InputMethodManager.SHOW_IMPLICIT, 0);
			}
		});

		nameValueEditText = (TranskartEditText) findViewById(R.id.___name_et);
		nameValueEditText.addListener(new BackKeyPressListener() {
			
			@Override
			public void backKeyPressed(TranskartEditText editText) {
				Log.i(TAG, "received back keypress event");
				revertUiState();
			}
		});
		
		nameValueEditText.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE ||
		                event.getAction() == KeyEvent.ACTION_DOWN &&
		                event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
					Log.i(TAG, "clicked OK");
					selectedCard.setCardName(nameValueEditText.getText().toString());
					try {
						dp.saveOrUpdateCard(selectedCard);
						fillTextViews();
						TranskartWidget.updateAllWidgets(getApplicationContext());
					} catch (CardSavingException ex) {
						App.showToast(ShowAllActivity.this, resources.getString(
								R.string.error_writing_card_to_disk,
								selectedCard.getCardNumber()), Duration.LONG);
					}
					
					revertUiState();
		            return true;
		        }
		        return false;
			}
		});
		
		mainLayout = findViewById(R.id.main_show_all_layout);
		mainLayout.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				Log.i(TAG, "clicked on layout");
				Log.i(TAG, "hiding keyboard");
				if (nameValueEditText.isShown()) {
					revertUiState();
				}
				return true;
			}
		});
	
	}
	
	private void revertUiState(){
		InputMethodManager imm = (InputMethodManager) 
				getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(nameValueEditText.getWindowToken(), 0);
		nameValueEditText.setVisibility(View.GONE);
		nameValueEditText.setText("");
		valueTextView.setVisibility(View.VISIBLE);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		cardNumber = intent.getStringExtra(IntentConstants.CARD_NUMBER);
		Log.i(TAG, "onnewintent received "+cardNumber);
		
		selectedCard = dp.getByNumber(cardNumber);
		fillTextViews();
		
	}
	private void fillTextViews() {
		Resources resources = getResources();
		TextView columnDescriptionView = (TextView) 
				findViewById(R.id.__name_description_tf);
		columnDescriptionView.setText(resources.getString(R.string.card_name_string));
		TextView columnValueView = (TextView) findViewById(R.id.__name_value_tf);
		columnValueView.setText(selectedCard.getCardName());
		
		setDescription(R.id.card_balance_block,
				resources.getString(R.string.card_balance_string), selectedCard.getBalanceString());
		setDescription(R.id.card_number_block,
				resources.getString(R.string.card_number_string), selectedCard.getCardNumber());
		setDescription(R.id.card_type_block,
				resources.getString(R.string.card_type_string), selectedCard.getCardType());
		setDescription(R.id.last_activated_block,
				resources.getString(R.string.last_activated_string),
				selectedCard.getActivationDate().getFormattedString());
		setDescription(R.id.valid_until_block,
				resources.getString(R.string.valid_until_string), selectedCard.getValidUntil().getFormattedString());
		LastUsageInfo lastUsageInfo = selectedCard.getLastUsageInfo();
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
		RechargeInfo rechargeInfo = selectedCard.getRechargeInfo();
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
				selectedCard.getLastUpdated().getFormattedString());
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
		switch (item.getItemId()) {
		case R.id.add_new_card_item: {
			Intent intent = new Intent(this, AddNewCardActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
		case R.id.delete_card_item:{
			DeleteCardDialogFragment newFragment = new DeleteCardDialogFragment();
			newFragment.setCardNumber(cardNumber);
		    newFragment.show(getFragmentManager(), DeleteCardDialogFragment.TAG);
			return true;
		}
		default: {
			return super.onOptionsItemSelected(item);
		}
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i(TAG, "returned");
		if(requestCode == REQ_CODE){
			Log.i(TAG, "reqcode OK "+resultCode);
			if(resultCode == RESULT_OK){
				Log.i(TAG, "res code OK");
				selectedCard = dp.getByNumber(cardNumber);
				fillTextViews();
			}
		}
	}

}
