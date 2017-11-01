package com.skylightdeveloper.livevehicletrackerdemo.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.model.LatLng;
import com.skylightdeveloper.livevehicletrackerdemo.R;
import com.skylightdeveloper.livevehicletrackerdemo.config.LContants;
import com.skylightdeveloper.livevehicletrackerdemo.model.StartEndLocationData;
import com.skylightdeveloper.livevehicletrackerdemo.model.UserChoiceLocation;

/**
 * Created by akash.wangalwar on 31/10/17.
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText mStartingAddrEt, mEndingAddrEt;
    private Button mStartNavBtn;
    private String TAG = MainActivity.class.getSimpleName();
    private static final int START_LOCATION_REQUEST_CODE = 111;
    private static final int END_LOCATION_REQUEST_CODE = 112;

    public static final int START_LOCATION_REQUEST_CODE_USER_CHOICE = 114;
    public static final int END_LOCATION_REQUEST_CODE_USER_CHOICE = 115;

    private LatLng mStartingLatLng, mEndingLatLng;
    public static final String LOCATION_INTENT_FILTER = "location_filter";
    private Button mStartNavGoogleMapBtn;
    private ImageView mPickStartingLocationIv, mPickEndingLocationIv;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        setIdsToViews();
        setIdsListenersViews();

    }

    private void setIdsListenersViews() {

        mStartNavBtn.setOnClickListener(this);
        mStartNavGoogleMapBtn.setOnClickListener(this);

        mStartingAddrEt.setOnClickListener(this);
        mEndingAddrEt.setOnClickListener(this);

        mPickStartingLocationIv.setOnClickListener(this);
        mPickEndingLocationIv.setOnClickListener(this);

        mStartingAddrEt.setFocusable(false);
        mStartingAddrEt.setClickable(true);
        mEndingAddrEt.setFocusable(false);
        mEndingAddrEt.setClickable(true);
    }

    private void setIdsToViews() {
        mStartingAddrEt = (EditText) findViewById(R.id.start_location_et_id);
        mEndingAddrEt = (EditText) findViewById(R.id.end_location_et_id);

        mStartNavBtn = (Button) findViewById(R.id.start_nav_but_id);
        mStartNavGoogleMapBtn = (Button) findViewById(R.id.start_nav_but_google_map_id);

        mPickStartingLocationIv = (ImageView) findViewById(R.id.pick_location_starting_iv_id);
        mPickEndingLocationIv = (ImageView) findViewById(R.id.pick_location_ending_iv_id);
    }

    @Override
    public void onClick(View view) {

        if (mPickStartingLocationIv == view) {
            Intent intent = new Intent(this, UserChoiceLocationActivity.class);
            intent.putExtra(LContants.USER_CHOICE_LOCATION_REQUEST_TYPE_INTENT_FILTER, LContants.START_LOCATION);
            startActivityForResult(intent, START_LOCATION_REQUEST_CODE_USER_CHOICE);
            return;
        }
        if (mPickEndingLocationIv == view) {
            Intent intent = new Intent(this, UserChoiceLocationActivity.class);
            intent.putExtra(LContants.USER_CHOICE_LOCATION_REQUEST_TYPE_INTENT_FILTER, LContants.END_LOCATION);
            startActivityForResult(intent, END_LOCATION_REQUEST_CODE_USER_CHOICE);
            return;
        }

        if (mStartNavGoogleMapBtn == view) {
            if (validateStartingAndEndingLocation()) {
                String uri = "http://maps.google.com/maps?saddr=" + mStartingLatLng.latitude + "," + mStartingLatLng.longitude + "&daddr=" + mEndingLatLng.latitude + "," + mEndingLatLng.longitude;
                Log.d(TAG, "onClick: Uri : " + uri);
                startNavigationUsingGoogleMap(uri);
            }
            return;
        }
        if (mStartNavBtn == view) {
            Log.d(TAG, "onClick:mStartNavBtn ");
            if (validateStartingAndEndingLocation())
                gotoLocationActivity();
            return;
        }
        if (mStartingAddrEt == view) {
            Log.d(TAG, "onClick: mStartingAddrEt");
            getLocationAddress(START_LOCATION_REQUEST_CODE);
            return;
        }
        if (mEndingAddrEt == view) {
            Log.d(TAG, "onClick: mEndingAddrEt");
            getLocationAddress(END_LOCATION_REQUEST_CODE);
            return;
        }
    }

    private boolean validateStartingAndEndingLocation() {

        if (mStartingAddrEt.getText().toString().trim().isEmpty()) {
            showToast(getString(R.string.start_location_et_error));
            return false;
        }
        if (mEndingAddrEt.getText().toString().trim().isEmpty()) {
            showToast(getString(R.string.end_location_et_error));
            return false;
        }
        return true;
    }

    private void showToast(String errorMsg) {
        Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
    }

    private void gotoLocationActivity() {
        Intent intent = new Intent(this, LocationActivity.class);
        StartEndLocationData data = new StartEndLocationData(mStartingLatLng, mEndingLatLng);
        intent.putExtra(LOCATION_INTENT_FILTER, data);
        startActivity(intent);
    }

    private void startNavigationUsingGoogleMap(String uri) {
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                Uri.parse(uri));
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == MainActivity.START_LOCATION_REQUEST_CODE_USER_CHOICE) {
            UserChoiceLocation    start = (UserChoiceLocation) data.getParcelableExtra(MainActivity.LOCATION_INTENT_FILTER);
            Log.d(TAG, "onActivityResult: start add : "+start.getmAddress());
            Log.d(TAG, "onActivityResult: start lat: "+start.getmLocation().latitude+ "  lng : "+start.getmLocation().longitude );
            mStartingAddrEt.setText(start.getmAddress());
            mStartingLatLng = start.getmLocation();

        } else if (resultCode == MainActivity.END_LOCATION_REQUEST_CODE_USER_CHOICE) {
            UserChoiceLocation    end = (UserChoiceLocation) data.getParcelableExtra(MainActivity.LOCATION_INTENT_FILTER);
            Log.d(TAG, "onActivityResult: start add : "+end.getmAddress());
            Log.d(TAG, "onActivityResult: start lat: "+end.getmLocation().latitude+ "  lng : "+end.getmLocation().longitude );
            mEndingAddrEt.setText(end.getmAddress());
            mEndingLatLng = end.getmLocation();
        } else if (requestCode == START_LOCATION_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Get the user's selected place from the Intent.
                Place place = PlaceAutocomplete.getPlace(this, data);
                Log.i(TAG, "Place Selected: " + place.getName());
                Log.i(TAG, "Place Selected lat and long: " + place.getLatLng());
                // Format the place's details and display them in the TextView.
                if (!place.getName().toString().isEmpty()) {
                    mStartingAddrEt.setText(place.getName());
                    mStartingLatLng = place.getLatLng();
                }

            }
        } else if (requestCode == END_LOCATION_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Get the user's selected place from the Intent.
                Place place = PlaceAutocomplete.getPlace(this, data);
                Log.i(TAG, "Place Selected: " + place.getName());
                Log.i(TAG, "Place Selected lat and long: " + place.getLatLng());
                // Format the place's details and display them in the TextView.
                if (!place.getName().toString().isEmpty()) {
                    mEndingAddrEt.setText(place.getName());
                    mEndingLatLng = place.getLatLng();
                }

            }
        } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
            Status status = PlaceAutocomplete.getStatus(this, data);
            Log.e(TAG, "Error: RESULT_ERROR Status = " + status.toString());
        } else if (resultCode == RESULT_CANCELED) {
            Log.d(TAG, "onActivityResult: RESULT_CANCELED ");
            // Indicates that the activity closed before a selection was made. For example if
            // the user pressed the back button.
        }
    }

    private void getLocationAddress(int requestCode) {
        try {
            // The autocomplete activity requires Google Play Services to be available. The intent
            // builder checks this and throws an exception if it is not the case.
            AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                    .setTypeFilter(Place.TYPE_COUNTRY)
                    .setCountry("IN")
                    .build();

            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                    .setFilter(typeFilter)
                    /*.setBoundsBias(new LatLngBounds(
                            new LatLng(-33.880490, 151.184363),
                            new LatLng(-33.858754, 151.229596)))*/
                    .build(this);
            startActivityForResult(intent, requestCode);
        } catch (GooglePlayServicesRepairableException e) {
            // Indicates that Google Play Services is either not installed or not up to date. Prompt
            // the user to correct the issue.
            GoogleApiAvailability.getInstance().getErrorDialog(this, e.getConnectionStatusCode(),
                    0 /* requestCode */).show();
        } catch (GooglePlayServicesNotAvailableException e) {
            // Indicates that Google Play Services is not available and the problem is not easily
            // resolvable.
            String message = "Google Play Services is not available: " +
                    GoogleApiAvailability.getInstance().getErrorString(e.errorCode);

            Log.e(TAG, message);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }
}
