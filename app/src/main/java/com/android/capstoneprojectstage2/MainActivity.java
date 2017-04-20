package com.android.capstoneprojectstage2;

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
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.android.capstoneprojectstage2.background.EventFetchingJobDispatcher;
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
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private final int RC_SIGN_IN = 110;
    private final int MY_PERMISSIONS_REQUEST_READ_CALENDAR = 117;
    private final int RETURNING_FROM_ADD_ACTIVITY = 122;
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
    int LOADER_ID;
    private GoogleApiClient googleApiClient;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private EventsAdapter adapter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
                .build();

        adapter = new EventsAdapter(this);

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
                        Log.v("RecyclerView: ", "Adapter  set h");
                        eventsList.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                        eventsList.setAdapter(adapter);
                    }

                    assert fab != null;
                    fab.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent addEventIntent = new Intent(MainActivity.this, AddEventActivity.class);
                            addEventIntent.putExtra("userEmail", user.getEmail());
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
        } else {
            getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CALENDAR: {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.length > 0)
                        && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    loadData();
                }
            }

        }
    }

    private void loadData() {
        getSupportLoaderManager().initLoader(LOADER_ID, null, this);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        Log.v("Main activity", "firebaseAuthWithGoogle:" + account.getId());
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
    public void onStop() {
        super.onStop();
        if (authStateListener != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.v("Loader: ", "Loader started");
        ContentResolver contentResolver = getContentResolver();
        contentResolver.delete(EventContract.EventEntry.CONTENT_URI, null, null);
        Calendar cc = Calendar.getInstance();
        int iYear = cc.get(Calendar.YEAR);
        int iMonth = cc.get(Calendar.MONTH);
        int iDay = cc.get(Calendar.DATE);

        long startMillis, endMillis;
        Calendar beginTime = Calendar.getInstance();
        beginTime.set(iYear, iMonth, iDay);
        startMillis = beginTime.getTimeInMillis();
        Calendar endTime = Calendar.getInstance();
        endTime.set(iYear, iMonth, iDay);
        endMillis = endTime.getTimeInMillis();
        Cursor cursor = CalendarContract.Instances.query(contentResolver, null, startMillis, endMillis);
        if (cursor != null) {
            Log.v("Cursor check Calendar: ", "Valued cursor from calendar with count = " + cursor.getCount());
            cursor.moveToFirst();
            Log.v("Cursor first element: ", cursor.toString());
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
        Log.v("Position: ", "Running directly");
        String[] projection = new String[]{EventContract.EventEntry.EVENT_TITLE, EventContract.EventEntry.EVENT_DESCRIPTION, EventContract.EventEntry.LOCATION};
        return new CursorLoader(this, EventContract.EventEntry.CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.v("Cursor on Load: ", "" + data.getCount());
        adapter.setCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.setCursor(null);
    }
}
