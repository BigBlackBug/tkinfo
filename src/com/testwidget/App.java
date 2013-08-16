package com.testwidget;

import java.util.Date;

import android.app.Application;
import android.util.Log;

import com.testwidget.CardDescriptor.DateWrapper;
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

}
