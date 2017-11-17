package com.skylightdeveloper.livevehicletrackerdemo.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
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
import com.skylightdeveloper.livevehicletrackerdemo.preferences.AppPreferences;

/**
 * Created by akash.wangalwar on 31/10/17.
 */

public class MainActivity extends BaseActivity implements View.OnClickListener {

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
    private static final int REQUEST_LOCATION_PERMISSION = 116;

    private static final int USER_CHOICE_STARTING_LOCATION_CLICKED = 311;
    private static final int USER_CHOICE_ENDING_LOCATION_CLICKED = 321;
    private static final int START_NAVIGATION_CLICKED = 331;
    private int WHICH_BUTTON_CLICKED = 0;
    private Button mCustomStartNavBut;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        setIdsToViews();
        setIdsListenersViews();
        requestLocationPermission();
    }

    private void setIdsListenersViews() {

        mStartNavBtn.setOnClickListener(this);
        mStartNavGoogleMapBtn.setOnClickListener(this);

        mStartingAddrEt.setOnClickListener(this);
        mEndingAddrEt.setOnClickListener(this);

        mPickStartingLocationIv.setOnClickListener(this);
        mPickEndingLocationIv.setOnClickListener(this);

        mCustomStartNavBut.setOnClickListener(this);

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

        mCustomStartNavBut = (Button) findViewById(R.id.custom_start_nav_but_id);

        mPickStartingLocationIv = (ImageView) findViewById(R.id.pick_location_starting_iv_id);
        mPickEndingLocationIv = (ImageView) findViewById(R.id.pick_location_ending_iv_id);
    }

    @Override
    public void onClick(View view) {

        if (mPickStartingLocationIv == view) {

            WHICH_BUTTON_CLICKED = USER_CHOICE_STARTING_LOCATION_CLICKED;

            if (requestLocationPermission()) {
                Intent intent = new Intent(this, UserChoiceLocationActivity.class);
                intent.putExtra(LContants.USER_CHOICE_LOCATION_REQUEST_TYPE_INTENT_FILTER, LContants.START_LOCATION);
                startActivityForResult(intent, START_LOCATION_REQUEST_CODE_USER_CHOICE);
            }

            return;
        }
        if (mPickEndingLocationIv == view) {

            WHICH_BUTTON_CLICKED = USER_CHOICE_ENDING_LOCATION_CLICKED;

            if (requestLocationPermission()) {
                Intent intent = new Intent(this, UserChoiceLocationActivity.class);
                intent.putExtra(LContants.USER_CHOICE_LOCATION_REQUEST_TYPE_INTENT_FILTER, LContants.END_LOCATION);
                startActivityForResult(intent, END_LOCATION_REQUEST_CODE_USER_CHOICE);
                return;
            }
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
            if (validateStartingAndEndingLocation()) {

                WHICH_BUTTON_CLICKED = START_NAVIGATION_CLICKED;

                if (requestLocationPermission()) {
                    gotoLocationActivity();
                }
            }
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

        if(mStartingLatLng == null){
            showToast(getString(R.string.reselect_start_location_et_error));
            return false;
        }
        if (mEndingLatLng == null) {
            showToast(getString(R.string.reselect_end_location_et_error));
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
            UserChoiceLocation start = (UserChoiceLocation) data.getParcelableExtra(MainActivity.LOCATION_INTENT_FILTER);
            Log.d(TAG, "onActivityResult: start add : " + start.getmAddress());
            Log.d(TAG, "onActivityResult: start lat: " + start.getmLocation().latitude + "  lng : " + start.getmLocation().longitude);
            mStartingAddrEt.setText(start.getmAddress());
            mStartingLatLng = start.getmLocation();

        } else if (resultCode == MainActivity.END_LOCATION_REQUEST_CODE_USER_CHOICE) {
            UserChoiceLocation end = (UserChoiceLocation) data.getParcelableExtra(MainActivity.LOCATION_INTENT_FILTER);
            Log.d(TAG, "onActivityResult: start add : " + end.getmAddress());
            Log.d(TAG, "onActivityResult: start lat: " + end.getmLocation().latitude + "  lng : " + end.getmLocation().longitude);
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

    private boolean requestLocationPermission() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }

        if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            return true;
        } else {

            AppPreferences appPreferences = new AppPreferences(this);

            if (!appPreferences.getLocationaPermissionDeniedStatus()) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_LOCATION_PERMISSION);

            } else if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {


                showLocationDialogOK(getString(R.string.android_location_permissions_denied_chat),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case DialogInterface.BUTTON_POSITIVE:


                                        ActivityCompat.requestPermissions(MainActivity.this,
                                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                                REQUEST_LOCATION_PERMISSION);

                                        break;
                                    case DialogInterface.BUTTON_NEGATIVE:
                                        // proceed with logic by disabling the related features or quit the app.
                                        break;
                                }
                            }
                        });
            } else {
                showSettingSnackBar("You need to enable Location permission from Settings ");
            }
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {

            case REQUEST_LOCATION_PERMISSION:

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (WHICH_BUTTON_CLICKED == USER_CHOICE_STARTING_LOCATION_CLICKED) {
                        mPickStartingLocationIv.performClick();

                    } else if (WHICH_BUTTON_CLICKED == USER_CHOICE_ENDING_LOCATION_CLICKED) {
                        mPickEndingLocationIv.performClick();

                    } else if (WHICH_BUTTON_CLICKED == START_NAVIGATION_CLICKED) {
                        mStartNavBtn.performClick();
                    } else {
                        Log.e(TAG, "onRequestPermissionsResult: INVALID CONDITION WILL NEVER OCCUR ");
                    }

                } else {
                    AppPreferences appPreferences = new AppPreferences(this);
                    showLongSnackBar(getString(R.string.app_permission_denied_user_clarification_msg));

                    appPreferences.setLocationPermissionDeniedStatus(true);
                }
                break;
        }
    }

    private void showLocationDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }

    private void showSettingSnackBar(String message) {

        Snackbar snackbar = Snackbar
                .make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.action_settings), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        launchAppSettingScreen();
                    }
                });

        snackbar.setActionTextColor(ContextCompat.getColor(this, R.color.dark_sky));

        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        textView.setMaxLines(4);
        snackbar.show();
    }

    private void launchAppSettingScreen() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }
}
