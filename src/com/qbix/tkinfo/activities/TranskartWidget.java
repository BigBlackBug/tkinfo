package com.qbix.tkinfo.activities;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.util.Log;
import android.widget.RemoteViews;

import com.qbix.tkinfo.App;
import com.qbix.tkinfo.R;
import com.qbix.tkinfo.activities.misc.IntentConstants;
import com.qbix.tkinfo.model.CardDescriptor;
import com.qbix.tkinfo.model.DataProvider;
import com.qbix.tkinfo.model.DataProvider.NoDataException;

public class TranskartWidget extends AppWidgetProvider {
	private static final String ACTION_APPWIDGET_UPDATE = "android.appwidget.action.APPWIDGET_UPDATE";
	public static final String ACTION_UPDATE_CARD = "com.qbix.tkinfo.ACTION_UPDATE_CARD";
	public static final String ACTION_NEXT_CARD = "com.qbix.tkinfo.ACTION_NEXT_CARD";
	public static final String ACTION_PREVIOUS_CARD = "com.qbix.tkinfo.ACTION_PREVIOUS_CARD";

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		Log.i("widget", "onUpdate called");
		Log.i("widget", "amount of widgets " + appWidgetIds.length);

		for (int widgetId : appWidgetIds) {
			
			DataProvider dataProvider = App.getDataProvider();
			if (!dataProvider.isEmpty()) {
				RemoteViews views = new RemoteViews(context.getPackageName(),
						R.layout.widget_layout);
				
				CardDescriptor current = dataProvider.getCurrent();
				fill(context,views, current);
				
				registerClickEvent(context, views, ACTION_UPDATE_CARD,R.id.update_button);
				registerClickEvent(context, views, ACTION_NEXT_CARD, R.id.next_card_button);
				registerClickEvent(context, views, ACTION_PREVIOUS_CARD, R.id.prev_card_button);
				appWidgetManager.updateAppWidget(widgetId, views);
			} else {
				RemoteViews views = new RemoteViews(context.getPackageName(),
						R.layout.widget_empty_layout);
				setAddCardIntent(context, views);
				appWidgetManager.updateAppWidget(widgetId, views);
			}

			
//			registerClickEvent(context, views, ACTION_OPEN_DETAILS, R.id.clickable_layout);
			
			// registerClickEvent(context, views, "start_activity",
			// R.id.card_balance_tf);
			// registerClickEvent(context, views, "start_activity",
			// R.id.card_name_tf);
			// registerClickEvent(context, views, "start_activity",
			// R.id.card_number_tf);

			// Register an onClickListener
			// Intent intent2 = new Intent(context, TestWidget.class);
			//
			// intent2.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
			// intent2.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
			// appWidgetIds);
			//
			// PendingIntent pendingIntent2 =
			// PendingIntent.getBroadcast(context,
			// 0, intent2, PendingIntent.FLAG_UPDATE_CURRENT);
			// views.setOnClickPendingIntent(R.id.button2, pendingIntent2);

			
		}
	}

	private void registerClickEvent(Context context, RemoteViews views,
			String action, int viewId) {
		Intent intent = new Intent(action);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
				intent, 0);
		views.setOnClickPendingIntent(viewId, pendingIntent);
	}

	public static void repaintAllWidgets(Context context,
			RemoteViews remoteViews) {
		ComponentName myWidget = new ComponentName(context, TranskartWidget.class);
		AppWidgetManager manager = AppWidgetManager.getInstance(context);
		manager.updateAppWidget(myWidget, remoteViews);
	}

	public static void updateAllWidgets(Context context) {
		Intent intent = new Intent(context, TranskartWidget.class);
		intent.setAction(ACTION_APPWIDGET_UPDATE);
		int[] appWidgetIds = AppWidgetManager.getInstance(context)
				.getAppWidgetIds(new ComponentName(context, TranskartWidget.class));
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
		context.sendBroadcast(intent);
	}

	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);
		Log.i("widget", "onEnabled called");
	}

	@Override
	public void onDisabled(Context context) {
		super.onDisabled(context);
		Log.i("widget", "onDisabled called");
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		DataProvider dataProvider = App.getDataProvider();
		String action = intent.getAction();
		Log.i("handler", "received action '" + action + "'");
		RemoteViews remoteViews;
		try {
			remoteViews = new RemoteViews(context.getPackageName(),
					R.layout.widget_layout);
			if (action.equals(ACTION_NEXT_CARD)) {
				CardDescriptor next = dataProvider.next();
				fill(context,remoteViews, next);
			} else if (action.equals(ACTION_PREVIOUS_CARD)) {
				CardDescriptor next = dataProvider.previous();
				fill(context,remoteViews, next);
			} else if (action.equals(ACTION_UPDATE_CARD)) {
				Rect rect = intent.getSourceBounds();
				Log.i("handler", "bottom:" + rect.bottom + " top:" + rect.top
						+ " left:" + rect.left + " right:" + rect.right);
				Intent intent2 = new Intent(context, UpdateActivity.class);
				intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
				intent2.putExtra(IntentConstants.LEFT_BORDER_INDEX, rect.left);
				intent2.putExtra(IntentConstants.BOTTOM_BORDER_INDEX, rect.bottom);
				intent2.putExtra(IntentConstants.WIDTH, rect.right - rect.left);
				intent2.putExtra(IntentConstants.CARD_NUMBER,
						dataProvider.getCurrent().getCardNumber());
				context.startActivity(intent2);
			}
			setShowAllIntent(context, remoteViews, dataProvider.getCurrent());
			
		} catch (NoDataException ex) {
			Log.d("widget", "no data");
			remoteViews = new RemoteViews(context.getPackageName(),
					R.layout.widget_empty_layout);
			setAddCardIntent(context, remoteViews);
		}
		
		repaintAllWidgets(context.getApplicationContext(), remoteViews);
		super.onReceive(context, intent);
	}

	private void setShowAllIntent(Context context, RemoteViews views,
			CardDescriptor currentDescriptor) {
		Intent startIntent = new Intent(context, ShowAllActivity.class);
		startIntent.putExtra(IntentConstants.CARD_NUMBER, currentDescriptor.getCardNumber());
		Log.i("widget", "setting intent "+currentDescriptor.getCardNumber());
		PendingIntent activity = PendingIntent.getActivity(context, 0,
				startIntent, Intent.FLAG_ACTIVITY_NEW_TASK);
		views.setOnClickPendingIntent(R.id.clickable_layout, activity);
	}
	
	private void setAddCardIntent(Context context, RemoteViews views) {
		Intent startIntent = new Intent(context, AddNewCardActivity.class);
//		startIntent.putExtra(IntentConstants.CARD_NUMBER, currentDescriptor.getCardNumber());
//		Log.i("widget", "setting intent "+currentDescriptor.getCardNumber());
		PendingIntent activity = PendingIntent.getActivity(context, 0,
				startIntent, Intent.FLAG_ACTIVITY_NO_HISTORY);
		views.setOnClickPendingIntent(R.id.new_card_text_view, activity);
	}

	private void fill(Context context,RemoteViews remoteViews, CardDescriptor descriptor) {
		fillRaw(context,remoteViews, descriptor.getCardName(), descriptor.getCardNumber(),
				String.valueOf(descriptor.getBalance()), descriptor.getLastUpdated().getFormattedString());
	}

	private void fillRaw(Context context,RemoteViews remoteViews, String name, String number,
			String balance, String lastUpdated) {
		Resources res = context.getResources();
		remoteViews.setTextViewText(R.id.card_name_tf,
				String.format(res.getString(R.string.card_name), name));
		remoteViews.setTextViewText(R.id.card_number_tf,
				String.format(res.getString(R.string.card_number), number));
		remoteViews.setTextViewText(R.id.card_balance_tf,
				String.format(res.getString(R.string.card_balance), balance));
		remoteViews.setTextViewText(R.id.last_modified_tf, String.format(
				res.getString(R.string.last_updated), lastUpdated));
	}


}
