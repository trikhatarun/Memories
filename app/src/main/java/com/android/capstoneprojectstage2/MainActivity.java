package com.android.capstoneprojectstage2;

import android.Manifest;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.capstoneprojectstage2.background.EventFetchingJobDispatcher;
import com.android.capstoneprojectstage2.background.GeofenceTransitionsIntentService;
import com.android.capstoneprojectstage2.data.EventContract;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, GoogleApiClient.ConnectionCallbacks, ResultCallback<Status>, GoogleApiClient.OnConnectionFailedListener {

    public static final int RETURNING_FROM_ADD_ACTIVITY = 122;
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    private final int RC_SIGN_IN = 110;
    @BindView(android.R.id.content)
    View parentLayout;
    @Nullable
    @BindView(R.id.googleSignInButton)
    SignInButton signInButton;
    @Nullable
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @Nullable
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @Nullable
    @BindView(R.id.events_list)
    RecyclerView eventsList;
    @Nullable
    @BindView(R.id.empty_view)
    TextView emptyView;

    int LOADER_ID;
    int dataCount = 0;
    private GoogleApiClient googleApiClient;
    private PendingIntent mGeofencePendingIntent;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private EventsAdapter adapter;
    private ArrayList<Geofence> mGeofenceList;
    private GoogleApiClient mGoogleApiClient;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        adapter = new EventsAdapter(this);

        mGeofencePendingIntent = null;
        mGeofenceList = new ArrayList<>();

        loadData();

        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));

        final int periodicity = (int) TimeUnit.HOURS.toSeconds(24);
        final int toleranceInterval = (int) TimeUnit.HOURS.toSeconds(1);

        Job refreshDatabase = dispatcher.newJobBuilder()
                .setService(EventFetchingJobDispatcher.class)
                .setRecurring(true)
                .setTag("Refresh Database")
                .setLifetime(Lifetime.FOREVER)
                .setTrigger(Trigger.executionWindow(periodicity, periodicity + toleranceInterval))
                .setReplaceCurrent(true)
                .build();

        dispatcher.mustSchedule(refreshDatabase);

        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    setContentView(R.layout.activity_main);
                    ButterKnife.bind(MainActivity.this);
                    assert toolbar != null;
                    setSupportActionBar(toolbar);
                    toolbar.setTitle(getString(R.string.app_name));

                    if (eventsList != null) {
                        eventsList.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                        eventsList.setAdapter(adapter);
                    }

                    assert fab != null;
                    fab.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent addEventIntent = new Intent(MainActivity.this, AddEventActivity.class);
                            startActivityForResult(addEventIntent, RETURNING_FROM_ADD_ACTIVITY);
                        }
                    });
                    Snackbar.make(parentLayout, "Welcome, " + user.getDisplayName(), Snackbar.LENGTH_LONG).show();
                } else {
                    setContentView(R.layout.login_screen);
                    ButterKnife.bind(MainActivity.this);

                    assert signInButton != null;
                    signInButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent signIn = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                            startActivityForResult(signIn, RC_SIGN_IN);
                        }
                    });
                }
            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            }
        } else if (requestCode == RETURNING_FROM_ADD_ACTIVITY) {
            if (resultCode == RESULT_OK) {
                mGeofenceList.add((Geofence) data.getParcelableExtra(getString(R.string.geoObjectKey)));

                try {
                    LocationServices.GeofencingApi.addGeofences(
                            mGoogleApiClient,
                            getGeofencingRequest(),
                            getGeofencePendingIntent()
                    ).setResultCallback(this); // Result processed in onResult().
                } catch (SecurityException securityException) {
                    Log.e("Main Activity: ", securityException.toString());
                }
            }
            getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS: {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.length > 0)
                        && (grantResults[0] == PackageManager.PERMISSION_GRANTED) && (grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                    loadData();
                }
            }

        }
    }

    private void loadData() {
        Boolean permissionsResult = checkAndRequestPermissions();
        if (permissionsResult) {


            Log.v("Permissions Result: ", permissionsResult.toString());
            getSupportLoaderManager().initLoader(LOADER_ID, null, this);
        }

    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()) {
                    Snackbar.make(parentLayout, getString(R.string.sign_in_failed_error), Snackbar.LENGTH_LONG);
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (authStateListener != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
        mGoogleApiClient.disconnect();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        ContentResolver contentResolver = getContentResolver();
        contentResolver.delete(EventContract.EventEntry.CONTENT_URI, null, null);
        Calendar cc = Calendar.getInstance();
        int iYear = cc.get(Calendar.YEAR);
        int iMonth = cc.get(Calendar.MONTH);
        int iDay = cc.get(Calendar.DATE);

        long startMillis, endMillis;
        Calendar beginTime = Calendar.getInstance();
        beginTime.set(iYear, iMonth, iDay, 0, 0, 1);
        startMillis = beginTime.getTimeInMillis();
        Calendar endTime = Calendar.getInstance();
        endTime.set(iYear, iMonth, iDay, 23, 59, 59);
        endMillis = endTime.getTimeInMillis();

        Cursor cursor = CalendarContract.Instances.query(contentResolver, null, startMillis, endMillis);
        if (cursor != null) {
            cursor.moveToFirst();
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                ContentValues values = new ContentValues();
                values.put(EventContract.EventEntry.EVENT_TITLE, cursor.getString(cursor.getColumnIndex(CalendarContract.Events.TITLE)));
                values.put(EventContract.EventEntry.EVENT_DESCRIPTION, cursor.getString(cursor.getColumnIndex(CalendarContract.Events.DESCRIPTION)));
                values.put(EventContract.EventEntry.EVENT_DATE, cursor.getLong(cursor.getColumnIndex(CalendarContract.Events.DTSTART)));
                values.put(EventContract.EventEntry.LOCATION, cursor.getString(cursor.getColumnIndex(CalendarContract.Events.EVENT_LOCATION)));

                Uri insertUri = contentResolver.insert(EventContract.EventEntry.CONTENT_URI, values);
                if (insertUri != null) {
                    Log.v("Refresh Database: ", insertUri.toString() + "\n\n");
                }
            }
            cursor.close();
        } else {
            Log.v("Cursor check Calendar: ", "Empty cursor from calendar");
        }
        String[] projection = new String[]{EventContract.EventEntry.EVENT_TITLE, EventContract.EventEntry.EVENT_DESCRIPTION, EventContract.EventEntry.LOCATION};
        return new CursorLoader(this, EventContract.EventEntry.CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.v("Cursor on Load: ", "" + data.getCount());
        dataCount = data.getCount();
        if (dataCount > 0) {
            if (emptyView != null) {
                emptyView.setVisibility(View.GONE);
            }
        }
        adapter.setCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.setCursor(null);
    }

    private boolean checkAndRequestPermissions() {

        int readCalendar = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR);
        int writeCalendar = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR);
        int loc = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        Log.v("Position", "" + readCalendar + writeCalendar + loc);
        List<String> listPermissionsNeeded = new ArrayList<>();

        if (readCalendar != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_CALENDAR);
        }
        if (writeCalendar != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_CALENDAR);
        }
        if (loc != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray
                    (new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i("Main Activity: ", getString(R.string.connection_completed));
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onResult(@NonNull Status status) {
        Log.v("Result: ", status.toString());
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

        // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
        // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
        // is already inside that geofence.
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

        // Add the geofences to be monitored by geofencing service.

        builder.addGeofences(mGeofenceList);

        // Return a GeofencingRequest.
        return builder.build();
    }
}
