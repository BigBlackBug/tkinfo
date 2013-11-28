package com.qbix.tkinfo.activities;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.qbix.tkinfo.App;
import com.qbix.tkinfo.App.Duration;
import com.qbix.tkinfo.R;
import com.qbix.tkinfo.activities.misc.BackKeyPressListener;
import com.qbix.tkinfo.activities.misc.IntentConstants;
import com.qbix.tkinfo.activities.misc.NFCException;
import com.qbix.tkinfo.activities.misc.TranskartEditText;
import com.qbix.tkinfo.model.CardDescriptor;
import com.qbix.tkinfo.model.DataProvider;
import com.qbix.tkinfo.model.CardDescriptor.LastUsageInfo;
import com.qbix.tkinfo.model.CardDescriptor.RechargeInfo;
import com.qbix.tkinfo.model.DataProvider.CardSavingException;

public class ShowAllActivity extends Activity {

	private static final String TAG = "show_all_activity";

	static final int UPDATE_ACTIVITY_REQUEST_CODE = 1001;

	static final int FETCH_CARD_REQUEST_CODE = 2002;
	
	private String cardNumber;
	private CardDescriptor selectedCard;
	private DataProvider dp = App.getDataProvider();
	private Resources resources;
	
	private TranskartEditText nameValueEditText;
	private View mainLayout;
	private TextView valueTextView ;

	private View showInfoButton;

	private boolean newlyCreated;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.resources = getResources();
		
		setContentView(R.layout.show_all_layout);
		
//		Intent intent = getIntent();
//		cardNumber = intent.getStringExtra(IntentConstants.CARD_NUMBER);
		try{
			cardNumber = readCardNumber(getIntent());
		}catch(NFCException ex){
			App.showToast(this,
					"Ошибка считывания с карты. Причина:'" + ex.getMessage()
							+ "'", Duration.SHORT);
			finish();
			return;
		}
		Log.i(TAG, "received "+cardNumber);
		initListeners();
		CardDescriptor card = dp.getByNumber(cardNumber);
		if(card == null){
			this.newlyCreated = true;
			//TODO show dialog, and fillViews
			showInfoButton.setVisibility(View.GONE);
			findViewById(R.id.main_scroll_view).setVisibility(View.GONE);
			Intent updateIntent = new Intent(this, UpdateActivity.class);
			updateIntent.putExtra(IntentConstants.CARD_NUMBER,cardNumber);
			updateIntent.putExtra(IntentConstants.REQUEST_CODE, FETCH_CARD_REQUEST_CODE);
			Log.i(TAG,"before start");
			startActivityForResult(updateIntent, FETCH_CARD_REQUEST_CODE);
			Log.i(TAG,"AFTER start");
		}else{
			this.newlyCreated = false;
			fillTextViews(card);
			this.selectedCard = card;
		}
		
	}
	
	private void initListeners() {
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
					fillTextViews(selectedCard);
					if(!newlyCreated){
						try {
							dp.saveOrUpdateCard(selectedCard);
							TranskartWidget.updateAllWidgets(getApplicationContext());
						} catch (CardSavingException ex) {
							App.showToast(ShowAllActivity.this, resources.getString(
									R.string.error_writing_card_to_disk,
									selectedCard.getCardNumber()), Duration.LONG);
						}
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
		showInfoButton = (Button)findViewById(R.id.show_info_button);
		showInfoButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ShowAllActivity.this, InformationActivity.class);
				startActivity(intent);
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
		Log.i(TAG, "newintent+"+intent);
		try{
			cardNumber = readCardNumber(intent);
		}catch(NFCException ex){
			App.showToast(this,
					"Ошибка считывания с карты. Причина:'" + ex.getMessage()
							+ "'", Duration.SHORT);
			finish();
			return;
		}
		Log.i(TAG, "onnewintent received "+cardNumber);
		
		selectedCard = dp.getByNumber(cardNumber);
		fillTextViews(selectedCard);
		
	}
	private void fillTextViews(CardDescriptor card) {
		Resources resources = getResources();
		TextView columnDescriptionView = (TextView) 
				findViewById(R.id.__name_description_tf);
		columnDescriptionView.setText(resources.getString(R.string.card_name_string));
		TextView columnValueView = (TextView) findViewById(R.id.__name_value_tf);
		columnValueView.setText(card.getCardName());
		int balance = card.getBalance();
		String balanceString;
		if(balance == -1){
			balanceString = "Безлимит";
		}else{
			 balanceString = card.getBalanceString();
		}
		setDescription(R.id.card_balance_block,
				resources.getString(R.string.card_balance_string), balanceString);
		setDescription(R.id.card_number_block,
				resources.getString(R.string.card_number_string), card.getCardNumber());
		setDescription(R.id.card_type_block,
				resources.getString(R.string.card_type_string), card.getCardType());
		setDescription(R.id.last_activated_block,
				resources.getString(R.string.last_activated_string),
				card.getActivationDate().getFormattedString());
		setDescription(R.id.valid_until_block,
				resources.getString(R.string.valid_until_string), card.getValidUntil().getFormattedString());
		LastUsageInfo lastUsageInfo = card.getLastUsageInfo();
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
		RechargeInfo rechargeInfo = card.getRechargeInfo();
		setDescription(R.id.recharged_on_block,
				resources.getString(R.string.date_string),
				rechargeInfo.getRechargeDate().getFormattedString());
		setDescription(R.id.recharged_at_block,
				resources.getString(R.string.recharge_location_string),
				rechargeInfo.getRechargeLocation());
		String rechargeAmountString;
		int rechargeAmount = rechargeInfo.getRechargeAmount();
		if(rechargeAmount == -1){
			rechargeAmountString = "Месячный безлимит";
		}else{
			rechargeAmountString = rechargeInfo.getRechargeAmountString();
		}
		setDescription(R.id.recharged_by_block,
				resources.getString(R.string.recharge_amount_string),
				rechargeAmountString);
		setDescription(R.id.last_updated_block,
				resources.getString(R.string.last_updated_string),
				card.getLastUpdated().getFormattedString());
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
		Log.i(TAG, "oncreatemenu");
		
//		String cardNumber = intent.getStringExtra(IntentConstants.CARD_NUMBER);
//		CardDescriptor card= App.getDataProvider().getByNumber(cardNumber);
//		if(card)
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		if(newlyCreated){
			menu.findItem(R.id.update_item).setVisible(false);
			menu.findItem(R.id.delete_card_item).setVisible(false);
			menu.findItem(R.id.add_new_card_item).setIcon(
					getResources().getDrawable(R.drawable.ic_action_save));
			//TODO SHOW SAVE ICON
		}else{
			MenuItem findItem = menu.findItem(R.id.add_new_card_item);
			NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
			if (nfcAdapter != null && nfcAdapter.isEnabled()) {
				findItem.setIcon(getResources().getDrawable(
						R.drawable.add_card_icon_nfc));
			}
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
			if(newlyCreated){
				if(selectedCard.getCardName()==null || selectedCard.getCardName().isEmpty()){
					App.showToast(this, "Имя карты не может быть пустым", Duration.SHORT);
					return true;
				}
				dp.saveCard(selectedCard);
				App.showToast(this, "Карта '" + selectedCard.getCardName()
						+ "' успешно сохранена", Duration.SHORT);
				App.closeActivityAfterDelay(this, 500);
				TranskartWidget.updateAllWidgets(getApplicationContext());
			}else{
				Intent intent = new Intent(this, AddNewCardActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}
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
			intent.putExtra(IntentConstants.REQUEST_CODE, UPDATE_ACTIVITY_REQUEST_CODE);
			startActivityForResult(intent, UPDATE_ACTIVITY_REQUEST_CODE);
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
		Log.i(TAG,"activity returned. req code "+requestCode+"  res code "+resultCode);
		if(resultCode == RESULT_OK){
			Log.i(TAG, "res code OK");
			if(requestCode == UPDATE_ACTIVITY_REQUEST_CODE){
				Log.i(TAG, "reqcode upd "+resultCode);
				selectedCard = dp.getByNumber(cardNumber);
				fillTextViews(selectedCard);
			}else if(requestCode == FETCH_CARD_REQUEST_CODE){
				Log.i(TAG, "reqcode fetch"+resultCode);
				findViewById(R.id.main_scroll_view).setVisibility(View.VISIBLE);
				CardDescriptor newCard = (CardDescriptor) data
						.getSerializableExtra(IntentConstants.PROCESSED_CARD_DESCRIPTOR);
				fillTextViews(newCard);
				this.selectedCard = newCard;
			}
		}else{
			finish();
		}
	}
	
	private String readCardNumber(Intent intent) throws NFCException {
		String action = intent.getAction();
		Log.i(TAG, "reading stuff. "+ action);
		
		if(action == null){
			return intent.getStringExtra(IntentConstants.CARD_NUMBER);
		}else if (action.equals(NfcAdapter.ACTION_TECH_DISCOVERED)) {
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
					Log.i(TAG, "handling intent. got number " + cardNumber);
//					mfc.close();
					return String.valueOf(cardNumber);
				} else {
					throw new NFCException("Карта не прошла аутентификацию");
					// TODO Authentication failed - Handle it
				}
			} catch (IOException e) {
				Log.e("tag", "connection dropped", e);
				throw new NFCException("Потеряно соединение с картой", e);
			}
			
		} else{
			return null;
		}
		
	}

}
