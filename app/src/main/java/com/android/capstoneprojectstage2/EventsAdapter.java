package com.android.capstoneprojectstage2;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.capstoneprojectstage2.data.EventContract;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.android.capstoneprojectstage2.R.id.mapView;

/**
 * Created by trikh on 20-04-2017.
 */

class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventViewHolder> implements OnMapReadyCallback {
    private Cursor cursor;
    private Context context;
    private GoogleMap map;

    EventsAdapter(Context context) {
        this.context = context;
    }

    @Override
    public EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(context).inflate(R.layout.events_list_item, parent, false);

        return new EventViewHolder(item);
    }

    @Override
    public void onBindViewHolder(EventViewHolder holder, int position) {
        cursor.moveToPosition(position);
        holder.title.setText(cursor.getString(cursor.getColumnIndex(EventContract.EventEntry.EVENT_TITLE)));
        String descriptionText = cursor.getString(cursor.getColumnIndex(EventContract.EventEntry.EVENT_DESCRIPTION));
        holder.description.setText(descriptionText);
        if (descriptionText.equals("Birthday")) {
            holder.logo.setImageResource(R.drawable.ic_cake);
        } else if (descriptionText.equals("Anniversary")) {
            holder.logo.setImageResource(R.drawable.anniversary);
        } else {
            holder.logo.setImageResource(R.drawable.ic_event);
        }


    }

    void setCursor(Cursor cursor) {
        this.cursor = cursor;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        int count = 0;
        if (cursor != null) {
            count = cursor.getCount();
        }
        return count;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

    }

    class EventViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.event_logo)
        ImageView logo;
        @BindView(R.id.occasion_name)
        TextView title;
        @BindView(R.id.occasion_type)
        TextView description;
        @BindView(mapView)
        MapView mapview;


        EventViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
