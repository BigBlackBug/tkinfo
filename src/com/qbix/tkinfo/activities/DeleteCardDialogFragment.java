package com.qbix.tkinfo.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import com.qbix.tkinfo.App;
import com.qbix.tkinfo.R;

public class DeleteCardDialogFragment extends DialogFragment {
	
	public static final String TAG = "delete_card_dialog_fragment";
	
	private String cardNumber;
	
	public DeleteCardDialogFragment() {
	}
	
	public void setCardNumber(String cardNumber) {
		this.cardNumber = cardNumber;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		String message = getResources().getString(R.string.delete_card_prompt, cardNumber);
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(message)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								Log.i(TAG, "clicked ok to delete");
//								App.getDataProvider().deleteCard(cardNumber);
								App.getDataProvider().removeCurrentCard();
								TranskartWidget.updateAllWidgets(getActivity());
								App.closeActivityAfterDelay(getActivity(), 1000);
							}
						})
				.setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								Log.i(TAG, "refused to delete");
								return;
							}
						});
		return builder.create();
	}
}