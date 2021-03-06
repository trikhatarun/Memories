package com.android.capstoneprojectstage2.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by trikh on 16-04-2017.
 */

public class EventContract {
    private static final String CONTENT_AUTHORITY = "com.android.capstoneprojectstage2";
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    private static final String PATH_EVENTS = "events";

    public static final class EventEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_EVENTS).build();
        public static final String TABLE_NAME = "events";
        public static final String EVENT_TITLE = "title";
        public static final String EVENT_DESCRIPTION = "description";
        public static final String EVENT_DATE = "date";
        public static final String LOCATION = "location";

    }
}
