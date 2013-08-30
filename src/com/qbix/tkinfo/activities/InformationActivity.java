package com.qbix.tkinfo.activities;

import com.qbix.tkinfo.R;
import com.qbix.tkinfo.R.layout;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class InformationActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_information);
		setTitle(getResources().getString(R.string.title_activity_information));
	}

}
