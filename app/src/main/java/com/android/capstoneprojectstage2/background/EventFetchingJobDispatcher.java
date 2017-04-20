package com.android.capstoneprojectstage2.background;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.android.capstoneprojectstage2.data.EventContract;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import java.util.Calendar;

/**
 * Created by trikh on 19-04-2017.
 */

public class EventFetchingJobDispatcher extends JobService {
    @Override
    public boolean onStartJob(JobParameters job) {

        Log.v("Refresh Database: ", "JOB started" + "\n\n");

        ContentResolver contentResolver = getContentResolver();
        contentResolver.delete(EventContract.EventEntry.CONTENT_URI, null, null);

        Calendar cc = Calendar.getInstance();
        int iYear = cc.get(Calendar.YEAR);
        int iMonth = cc.get(Calendar.MONTH);
        int iDay = cc.get(Calendar.DATE);

        long startMillis;
        Calendar beginTime = Calendar.getInstance();
        beginTime.set(iYear, iMonth, iDay);
        startMillis = beginTime.getTimeInMillis();

        String selection = "(( " + CalendarContract.Events.DTSTART + " ==? )" + " AND ( " + CalendarContract.Events.ALL_DAY + "=?" + "))";
        String[] selectionArgs = new String[]{String.valueOf(startMillis), String.valueOf(1)};
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        Cursor cursor = getApplicationContext().getContentResolver().query(CalendarContract.Events.CONTENT_URI,
                new String[]{CalendarContract.Events.TITLE, CalendarContract.Events.DESCRIPTION, CalendarContract.Events.DTSTART, CalendarContract.Events.EVENT_LOCATION}, selection, selectionArgs, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                ContentValues values = new ContentValues();
                values.put(EventContract.EventEntry.EVENT_TITLE, cursor.getString(0));
                values.put(EventContract.EventEntry.EVENT_DESCRIPTION, cursor.getString(1));
                values.put(EventContract.EventEntry.EVENT_DATE, cursor.getLong(2));
                values.put(EventContract.EventEntry.LOCATION, cursor.getString(3));

                Uri insertUri = contentResolver.insert(EventContract.EventEntry.CONTENT_URI, values);
                if (insertUri != null) {
                    Log.v("Refresh Database: ", insertUri.toString() + "\n\n");
                }
            }
            cursor.close();
        }

        return false;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return false;
    }
}
