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

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by trikh on 20-04-2017.
 */

class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventViewHolder> {
    private Cursor cursor;
    private Context context;

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
        holder.lastYearLocation.setText(cursor.getString(cursor.getColumnIndex(EventContract.EventEntry.LOCATION)));
        switch (descriptionText) {
            case "Birthday":
                holder.logo.setImageResource(R.drawable.ic_cake);
                break;
            case "Anniversary":
                holder.logo.setImageResource(R.drawable.anniversary);
                break;
            default:
                holder.logo.setImageResource(R.drawable.ic_event);
                break;
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

    class EventViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.event_logo)
        ImageView logo;
        @BindView(R.id.occasion_name)
        TextView title;
        @BindView(R.id.occasion_type)
        TextView description;
        @BindView(R.id.lastYearLocation)
        TextView lastYearLocation;


        EventViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
