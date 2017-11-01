package com.skylightdeveloper.livevehicletrackerdemo.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.skylightdeveloper.livevehicletrackerdemo.R;
import com.skylightdeveloper.livevehicletrackerdemo.config.LContants;
import com.skylightdeveloper.livevehicletrackerdemo.config.LiveLocationConfig;
import com.skylightdeveloper.livevehicletrackerdemo.model.Address;
import com.skylightdeveloper.livevehicletrackerdemo.model.Share;
import com.skylightdeveloper.livevehicletrackerdemo.model.UserChoiceLocation;
import com.skylightdeveloper.livevehicletrackerdemo.network.NetworkManager;

import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by akash.wangalwar on 31/10/17.
 */

public class UserChoiceLocationActivity extends BaseActivity implements
        OnMapReadyCallback, GoogleMap.OnCameraChangeListener, View.OnClickListener {

    private static final String TAG = UserChoiceLocationActivity.class.getSimpleName();
    private GoogleMap googleMap;
    private MarkerOptions marker;
    private LatLng centerOfMap;
    private TextView mPickLocation;
    private static final int PICK_ADDRES = 153;
    private boolean retried;
    private ProgressDialog mProgressDialog;
    private String mRequestType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.user_choice_location_activity);


        setIdsToViews();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.track_map);

        mapFragment.getMapAsync(this);

        mRequestType = getIntent().getStringExtra(LContants.USER_CHOICE_LOCATION_REQUEST_TYPE_INTENT_FILTER);
        Log.d(TAG, "onCreate: mRequestType : " + mRequestType);
    }

    private void setIdsToViews() {

        mPickLocation = (TextView) findViewById(R.id.pick_location_tv_id);
        mPickLocation.setOnClickListener(this);

        mProgressDialog = getProgressDialog(getString(R.string.fetching_adress));
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        if (this.googleMap != null) {

            GetCurrentLocation();
            this.googleMap.setMyLocationEnabled(true);

            this.googleMap.setOnCameraChangeListener(this);
        }
        googleMap.getUiSettings().setZoomControlsEnabled(true);

    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

        centerOfMap = googleMap.getCameraPosition().target;

        Log.d(TAG, "onCameraChange: lat : " +
                centerOfMap.latitude + " long : " + centerOfMap.longitude);

        Log.d(TAG, "onCameraChange: Truncated : lat : " +
                roundDoubleValueToSixPrec(centerOfMap.latitude, 6)
                + " long : " + roundDoubleValueToSixPrec(centerOfMap.longitude, 6));

    }

    private void getAddressFromLatLng(final String latLong) {

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(LiveLocationConfig.BASE_URL_GEO_CODING_API)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();

        NetworkManager networkManager = retrofit.create(NetworkManager.class);

        Call<Address> call = networkManager.getAddressFromlatLang(latLong, "ROOFTOP", "street_address", getString(R.string.google_maps_key));

        call.enqueue(new Callback<Address>() {
            @Override
            public void onResponse(Call<Address> call, Response<Address> response) {

                Address address = response.body();

                if (address != null && address.getResults() != null && address.getResults().size() > 0) {

                    mProgressDialog.hide();

                    Log.d(TAG, "onSuccess: add : " + address.getResults().get(0).getFormatted_address());
                    Log.d(TAG, "onSuccess: lat : " + roundDoubleValueToSixPrec(address.getResults().get(0).
                            getGeometry().getLocation().getLat(), 6));
                    Log.d(TAG, "onSuccess: long: " + roundDoubleValueToSixPrec(address.getResults().get(0).
                            getGeometry().getLocation().getLng(), 6));

                    UserChoiceLocation userChoiceLocation = new UserChoiceLocation(address.getResults().get(0).getFormatted_address(), new LatLng(roundDoubleValueToSixPrec(address.getResults().get(0).
                            getGeometry().getLocation().getLat(), 6), roundDoubleValueToSixPrec(address.getResults().get(0).
                            getGeometry().getLocation().getLng(), 6)));

                    Intent intent = new Intent();
                    intent.putExtra(MainActivity.LOCATION_INTENT_FILTER, userChoiceLocation);

                    if (mRequestType.equalsIgnoreCase(LContants.START_LOCATION)) {
                        setResult(MainActivity.START_LOCATION_REQUEST_CODE_USER_CHOICE, intent);
                    } else {
                        setResult(MainActivity.END_LOCATION_REQUEST_CODE_USER_CHOICE, intent);
                    }
                    finish();

                } else {
                    Log.d(TAG, "onResponse: null or size zero ");

                    retryGetAddressFromLatLng(latLong);
                }
            }

            @Override
            public void onFailure(Call<Address> call, Throwable t) {
                mProgressDialog.hide();
                Log.e(TAG, "onFailure:a " + t.getMessage());
                showShortSnackBar(getString(R.string.error_message));
            }
        });
    }

    private void retryGetAddressFromLatLng(String latLong) {

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(LiveLocationConfig.BASE_URL_GEO_CODING_API)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();

        NetworkManager networkManager = retrofit.create(NetworkManager.class);

        Call<Address> call = networkManager.retryAddressFromlatLang(latLong);

        call.enqueue(new Callback<Address>() {
            @Override
            public void onResponse(Call<Address> call, Response<Address> response) {
                mProgressDialog.hide();

                Address address = response.body();

                if (address != null && address.getResults() != null && address.getResults().size() > 0) {

                    Log.d(TAG, "onSuccess: add : " + address.getResults().get(0).getFormatted_address());
                    Log.d(TAG, "onSuccess: lat : " + roundDoubleValueToSixPrec(address.getResults().get(0).
                            getGeometry().getLocation().getLat(), 6));
                    Log.d(TAG, "onSuccess: long: " + roundDoubleValueToSixPrec(address.getResults().get(0).
                            getGeometry().getLocation().getLng(), 6));

                    UserChoiceLocation userChoiceLocation = new UserChoiceLocation(address.getResults().get(0).getFormatted_address(), new LatLng(roundDoubleValueToSixPrec(address.getResults().get(0).
                            getGeometry().getLocation().getLat(), 6), roundDoubleValueToSixPrec(address.getResults().get(0).
                            getGeometry().getLocation().getLng(), 6)));

                    Intent intent = new Intent();
                    intent.putExtra(MainActivity.LOCATION_INTENT_FILTER, userChoiceLocation);

                    if (mRequestType.equalsIgnoreCase(LContants.START_LOCATION)) {
                        setResult(MainActivity.START_LOCATION_REQUEST_CODE_USER_CHOICE, intent);
                    } else {
                        setResult(MainActivity.END_LOCATION_REQUEST_CODE_USER_CHOICE, intent);
                    }
                    finish();

                } else {
                    Log.d(TAG, "onResponse: null or size zero ");
                    showShortSnackBar(getString(R.string.error_retreiving_address_from_latLng));
                }
            }

            @Override
            public void onFailure(Call<Address> call, Throwable t) {
                mProgressDialog.hide();
                Log.e(TAG, "onFailure:a " + t.getMessage());
                showShortSnackBar(getString(R.string.error_message));
            }
        });
    }

    private void GetCurrentLocation() {

        double[] d = getlocation();
        Share.lat = d[0];
        Share.lng = d[1];
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(Share.lat, Share.lng), 15));
    }

    public double[] getlocation() {

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = lm.getProviders(true);

        Location l = null;
        for (int i = 0; i < providers.size(); i++) {
            l = lm.getLastKnownLocation(providers.get(i));
            if (l != null)
                break;
        }
        double[] gps = new double[2];

        if (l != null) {
            gps[0] = l.getLatitude();
            gps[1] = l.getLongitude();
        }
        return gps;
    }

    public static double roundDoubleValueToSixPrec(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

 /*   @Override
    public void onSuccess(Address address) {

        if (address != null && address.getResults() != null && address.getResults().size() > 0) {

            Log.d(TAG, "onSuccess: add : " + address.getResults().get(0).getFormatted_address());
            Log.d(TAG, "onSuccess: lat : " + roundDoubleValueToSixPrec(address.getResults().get(0).
                    getGeometry().getLocation().getLat(), 6));
            Log.d(TAG, "onSuccess: long: " + roundDoubleValueToSixPrec(address.getResults().get(0).
                    getGeometry().getLocation().getLng(), 6));

            Intent intent = new Intent(this, SaveNewAddressActivity.class);
            intent.putExtra(SConstant.ADDRESS, address.getResults().get(0)
                    .getFormatted_address());

            intent.putExtra(SConstant.LATITUDE, address.getResults().get(0)
                    .getGeometry().getLocation().getLat());

            intent.putExtra(SConstant.LONGITUDE, address.getResults().get(0)
                    .getGeometry().getLocation().getLng());

            intent.putExtra(SConstant.NEWADDRESS, true);
            startActivity(intent);
            finish();

        } else if (address != null && address.getStatus() != null &&
                address.getStatus().equalsIgnoreCase(SConstant.ZERO_RESULTS)) {

            if (!retried) {
                retried = true;

                if (centerOfMap != null *//*&& centerOfMap.latitude != 0 && centerOfMap.longitude !=0*//*) {

                    UiUtils.showProgressDialog(this, "Re-fetching Address...");

                    NetworkManager networkManager = new NetworkManager(this);
                    networkManager.retryGetAddressFromlatLang(this,
                            roundDoubleValueToSixPrec(centerOfMap.latitude, 6) + "," +
                                    roundDoubleValueToSixPrec(centerOfMap.longitude, 6));
                } else {
                    Log.d(TAG, "onClick: Location UnAvailable ?");
                }

            } else {
                showLongSnackBar("We couldn't detect your location. Please try again and point to correct location.");
            }
        } else {
            showLongSnackBar("We couldn't detect your location. Please try again and point to correct location.");
        }
    }

    @Override
    public void onfailure(String errorMessage) {

        showShortToast(errorMessage);
        Log.e(TAG, "onfailure: errorMessage : " + errorMessage);
    }*/

    @Override
    public void onClick(View view) {
        if (view == mPickLocation) {

            if (centerOfMap != null /*&& centerOfMap.latitude != 0 && centerOfMap.longitude !=0*/) {
                mProgressDialog.show();
                getAddressFromLatLng(roundDoubleValueToSixPrec(centerOfMap.latitude, 6) + ","
                        + roundDoubleValueToSixPrec(centerOfMap.longitude, 6));
            } else {
                Log.d(TAG, "onClick: Location UnAvailable ?");
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        setResult(Activity.RESULT_CANCELED);
        finish();
    }
}


