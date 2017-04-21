package com.android.capstoneprojectstage2.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import com.android.capstoneprojectstage2.R;

/**
 * Created by trikh on 21-04-2017.
 */

public class WidgetProvider extends AppWidgetProvider {
/*    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName thisWidget = new ComponentName(context, WidgetProvider.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.list_view);
    }*/

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        Log.v("Position: ", " On update called");
        for (int appWidgetId : appWidgetIds) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            Intent adapter = new Intent(context, WidgetService.class);
            remoteViews.setEmptyView(R.id.list_view, R.id.empty_view);
            remoteViews.setRemoteAdapter(R.id.list_view, adapter);

            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
    }
}
