package com.android.capstoneprojectstage2.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.android.capstoneprojectstage2.data.EventContract.EventEntry;


/**
 * Created by trikh on 16-04-2017.
 */

public class EventDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "storage.db";
    private static final int DATABASE_VERSION = 1;


    public EventDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_ITEMS_TABLE = "CREATE TABLE " + EventEntry.TABLE_NAME + "( "
                + EventEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + EventEntry.EVENT_TITLE + " TEXT NOT NULL, "
                + EventEntry.EVENT_DATE + " LONG NOT NULL, "
                + EventEntry.EVENT_DESCRIPTION + " TEXT DEFAULT NULL, "
                + EventEntry.LOCATION + " TEXT DEFAULT NULL);";

        db.execSQL(SQL_CREATE_ITEMS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
