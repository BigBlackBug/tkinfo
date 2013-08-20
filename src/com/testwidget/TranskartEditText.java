package com.testwidget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

public class TranskartEditText extends EditText {
	
	private static final String TAG = "tk_edit_text";
	
	private List<BackKeyPressListener> listeners;

	public TranskartEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		String editTextLabel = getResources().getString(R.string.edit_text_label);
		setImeActionLabel(editTextLabel, EditorInfo.IME_ACTION_DONE);
		listeners = new ArrayList<BackKeyPressListener>();
	}

	private void notifyListeners() {
		for (BackKeyPressListener listener : listeners) {
			listener.backKeyPressed(this);
		}
	}

	public void addListener(BackKeyPressListener listener) {
		listeners.add(listener);
	}

	public void removeListener(BackKeyPressListener listener) {
		listeners.remove(listener);
	}

	@Override
	public boolean onKeyPreIme(int keyCode, KeyEvent event) {
		Log.i(TAG, "onKeyPreIme " + event);
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_UP) {
			notifyListeners();
		}
		return super.onKeyPreIme(keyCode, event);
	}

}
