package com.android.capstoneprojectstage2.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;

/**
 * Created by trikh on 21-04-2017.
 */

public class WidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new WidgetRemoteFactory(this.getApplicationContext(), intent);
    }
}
