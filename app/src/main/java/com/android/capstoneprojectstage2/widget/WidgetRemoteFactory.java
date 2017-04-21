package com.android.capstoneprojectstage2.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Binder;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.android.capstoneprojectstage2.R;
import com.android.capstoneprojectstage2.data.EventContract;
import com.android.capstoneprojectstage2.data.EventDbHelper;

/**
 * Created by trikh on 21-04-2017.
 */

class WidgetRemoteFactory implements RemoteViewsService.RemoteViewsFactory {

    private Context contextInstance;
    private Cursor cursorInstance;
    private EventDbHelper DbHelperInstance;

    WidgetRemoteFactory(Context applicationContext, Intent intent) {
        contextInstance = applicationContext;
    }

    @Override
    public void onCreate() {
        DbHelperInstance = new EventDbHelper(contextInstance);
    }

    @Override
    public void onDataSetChanged() {
        final long identityToken = Binder.clearCallingIdentity();
        if (cursorInstance != null) {
            cursorInstance.close();
        }
        SQLiteDatabase db = DbHelperInstance.getReadableDatabase();
        cursorInstance = db.query(EventContract.EventEntry.TABLE_NAME, new String[]{EventContract.EventEntry.EVENT_TITLE, EventContract.EventEntry.EVENT_DESCRIPTION}, null, null, null, null, null, null);

        Binder.restoreCallingIdentity(identityToken);

    }

    @Override
    public void onDestroy() {
        if (cursorInstance != null) {
            cursorInstance.close();
            cursorInstance = null;
        }
    }

    @Override
    public int getCount() {
        return cursorInstance.getCount();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews listItemView = new RemoteViews(contextInstance.getPackageName(), R.layout.widget_list_item);
        if (cursorInstance.moveToPosition(position)) {
            listItemView.setTextViewText(R.id.widget_event_title, cursorInstance.getString(cursorInstance.getColumnIndex(EventContract.EventEntry.EVENT_TITLE)));
            String descriptionText = cursorInstance.getString(cursorInstance.getColumnIndex(EventContract.EventEntry.EVENT_DESCRIPTION));
            listItemView.setTextViewText(R.id.widget_event_description, descriptionText);
            if (descriptionText.equals("Birthday")) {
                listItemView.setImageViewResource(R.id.widget_event_logo, R.drawable.ic_cake);
            } else if (descriptionText.equals("Anniversary")) {
                listItemView.setImageViewResource(R.id.widget_event_logo, R.drawable.anniversary);
            } else {
                listItemView.setImageViewResource(R.id.widget_event_logo, R.drawable.ic_event);
            }
        }
        return listItemView;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}