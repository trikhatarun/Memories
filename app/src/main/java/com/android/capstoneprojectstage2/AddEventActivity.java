package com.android.capstoneprojectstage2;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract.Events;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by trikh on 14-04-2017.
 */

public class AddEventActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private final int MY_PERMISSIONS_REQUEST_WRITE_CALENDAR = 1011;
    @BindView(R.id.event_title)
    TextInputLayout eventTitle;
    @BindView(R.id.spinner)
    Spinner spinner;
    @BindView(R.id.done_button)
    Button doneButton;
    @BindView(R.id.check_in_location)
    TextView locationTextField;
    private GoogleApiClient googleApiClientInstance;
    private StringBuilder result;
    private ContentResolver cr;
    private ContentValues values;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_event_layout);

        ButterKnife.bind(this);

        cr = getContentResolver();

        googleApiClientInstance = new GoogleApiClient.Builder(AddEventActivity.this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(AddEventActivity.this)
                .addOnConnectionFailedListener(AddEventActivity.this)
                .build();

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText eventTitleEditText = eventTitle.getEditText();
                if (eventTitleEditText != null) {
                    String title = eventTitleEditText.getText().toString();
                    if (title.equals("")) {
                        eventTitle.setError(getString(R.string.empty_title_error));
                        return;
                    }

                    String occasion = spinner.getSelectedItem().toString();
                    long calendarId = 3;
                    long startMillis;
                    long endMillis;
                    Calendar beginTime = Calendar.getInstance();
                    beginTime.set(beginTime.get(Calendar.YEAR), beginTime.get(Calendar.MONTH), beginTime.get(Calendar.DATE));
                    startMillis = beginTime.getTimeInMillis();
                    Calendar endTime = Calendar.getInstance();
                    endTime.set(beginTime.get(Calendar.YEAR), beginTime.get(Calendar.MONTH), beginTime.get(Calendar.DATE));
                    endMillis = endTime.getTimeInMillis();

                    values = new ContentValues();
                    values.put(Events.DTSTART, startMillis);
                    values.put(Events.DTEND, endMillis);
                    values.put(Events.TITLE, title);
                    values.put(Events.DESCRIPTION, occasion);
                    values.put(Events.CALENDAR_ID, calendarId);
                    values.put(Events.EVENT_TIMEZONE, String.valueOf(TimeZone.getDefault().getID()));
                    values.put(Events.EVENT_LOCATION, String.valueOf(result));
                    addEvent();
                }
            }
        });

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LocationRequest locationRequestInstance = LocationRequest.create();
        locationRequestInstance.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Snackbar.make(locationTextField, getString(R.string.ask_location_string), Snackbar.LENGTH_LONG);
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClientInstance, locationRequestInstance, AddEventActivity.this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Snackbar.make(locationTextField, getString(R.string.failed_location), Snackbar.LENGTH_LONG);
    }

    @Override
    public void onLocationChanged(Location location) {
        result = new StringBuilder();
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                result.append(address.getAddressLine(0));
                result.append(", ");
                result.append(address.getAddressLine(1));
                result.append(", ");
                result.append(address.getLocality()).append("\n");
                result.append(address.getCountryName());
            }
        } catch (IOException e) {
            Log.e("tag", e.getMessage());
        }
        locationTextField.setText(result);
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClientInstance.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        googleApiClientInstance.disconnect();
    }

    void addEvent() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(AddEventActivity.this,
                    new String[]{Manifest.permission.WRITE_CALENDAR},
                    MY_PERMISSIONS_REQUEST_WRITE_CALENDAR);
            return;
        }
        Uri resultantUri = cr.insert(Events.CONTENT_URI, values);
        if (resultantUri != null) {
            Log.i(AddEventActivity.class.getSimpleName(), resultantUri.toString());
            Toast.makeText(this, getString(R.string.event_add_success), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_CALENDAR: {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.length > 0)
                        && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    addEvent();
                }
            }

        }
    }

}