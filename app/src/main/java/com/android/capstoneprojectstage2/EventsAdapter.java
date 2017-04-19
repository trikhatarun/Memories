package com.android.capstoneprojectstage2;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.MapView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by trikh on 19-04-2017.
 */

public class EventsAdapter extends CursorAdapter {
    public EventsAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.events_list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final ViewHolder listItemView = new ViewHolder(view);


    }

    static class ViewHolder {
        @BindView(R.id.mapView)
        MapView mapView;
        @BindView(R.id.occasion_type)
        TextView description;
        @BindView(R.id.occasion_name)
        TextView title;
        @BindView(R.id.event_logo)
        ImageView logo;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
