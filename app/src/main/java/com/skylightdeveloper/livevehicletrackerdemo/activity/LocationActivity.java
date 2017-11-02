package com.skylightdeveloper.livevehicletrackerdemo.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.skylightdeveloper.livevehicletrackerdemo.config.LContants;
import com.skylightdeveloper.livevehicletrackerdemo.config.LiveLocationConfig;
import com.skylightdeveloper.livevehicletrackerdemo.model.Data;
import com.skylightdeveloper.livevehicletrackerdemo.network.NetworkManager;
import com.skylightdeveloper.livevehicletrackerdemo.R;
import com.skylightdeveloper.livevehicletrackerdemo.model.StartEndLocationData;
import com.skylightdeveloper.livevehicletrackerdemo.preferences.AppPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.skylightdeveloper.livevehicletrackerdemo.model.Share.lat;

public class LocationActivity extends FragmentActivity implements OnMapReadyCallback, OnLocationUpdatedListener {

    private static final String TAG = LocationActivity.class.getSimpleName();
    private GoogleMap mMap;
    private boolean mLocationPermissionGranted;
    private final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 113;
    private Location mLocation;
    //    private Location mPrevLocation;
    private StartEndLocationData data;
    private TextView mDetailsTv;
    PolylineOptions lineOptions = null;
    ArrayList<LatLng> points;
    private static final int REQUEST_LOCATION_PERMISSION = 113;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.location_activity_layout);

        setIdsToViews();
        initMap();

        data = (StartEndLocationData) getIntent().getParcelableExtra(MainActivity.LOCATION_INTENT_FILTER);

        Log.d(TAG, "onCreate: start " + data.getmStartingLatLng().latitude + " " + data.getmStartingLatLng().longitude + "   end " + data.getmEndingLatLng().latitude + "  " + data.getmEndingLatLng().longitude);
    }

    private void setIdsToViews() {

        mDetailsTv = (TextView) findViewById(R.id.details_tv_id);
    }

    private void initMap() {

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Log.d(TAG, "onMapReady: ");
        if (android.os.Build.VERSION.SDK_INT > 23) {
            getLocationPermission();
        } else {
            startLocationUpdates();
        }

        drawPolyline();

        googleMap.getUiSettings().setZoomControlsEnabled(true);
    }

    private void drawPolyline() {

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
// set your desired log level
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
// add your other interceptors …
// add logging as last interceptor
        httpClient.addInterceptor(logging);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(LiveLocationConfig.BASE_URL_DIRECTION_API)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();
        NetworkManager networkManager = retrofit.create(NetworkManager.class);

        String startingLatLng = data.getmStartingLatLng().latitude + "," + data.getmStartingLatLng().longitude;
        String endingLatLng = data.getmEndingLatLng().latitude + "," + data.getmEndingLatLng().longitude;

        Call<Data> call = networkManager.getRoutes(LContants.TRANSIT_MODE, LContants.TRANSIT_ROUTING_PREFERENCE, startingLatLng, endingLatLng, false, getString(R.string.google_maps_key));


        call.enqueue(new Callback<Data>() {
            @Override
            public void onResponse(Call<Data> call, Response<Data> response) {

                Log.d(TAG, "onResponse: " + response.body().toString());
                Data data = response.body();
                setDataToViews(data);
                parseData(data);

                /*ParserTask parserTask = new ParserTask();

                // Invokes the thread for parsing the JSON data
                parserTask.execute(response.body().toString());*/
            }

            @Override
            public void onFailure(Call<Data> call, Throwable t) {

                Log.d(TAG, "onFailure: retrofit " + t.getMessage());
            }
        });
    }

    private void parseData(Data data) {


        List<List<HashMap<String, String>>> routes = new ArrayList<>();

        try {

            /** Traversing all routes */
            for (int i = 0; i < data.routes.size(); i++) {

                List path = new ArrayList<>();

                /** Traversing all legs */
                for (int j = 0; j < data.routes.get(i).legs.size(); j++) {

                    /** Traversing all steps */
                    for (int k = 0; k < data.routes.get(i).legs.get(j).steps.size(); k++) {
                        String polyline = "";
//                        polyline = (String)((JSONObject)((JSONObject)jSteps.get(k)).get("polyline")).get("points");
                        polyline = data.routes.get(i).legs.get(j).steps.get(k).polyline.points;
                        List<LatLng> list = decodePoly(polyline);
//                        polyLineList.addAll(list);
                        /** Traversing all points */
                        for (int l = 0; l < list.size(); l++) {
                            HashMap<String, String> hm = new HashMap<>();
                            hm.put("lat", Double.toString((list.get(l)).latitude));
                            hm.put("lng", Double.toString((list.get(l)).longitude));
                            path.add(hm);
                        }
                    }
                    routes.add(path);
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "parse: Exception : " + e.getMessage());
        }


        // Traversing through all the routes
        for (int i = 0; i < routes.size(); i++) {
            points = new ArrayList<>();
            lineOptions = new PolylineOptions();

            // Fetching i-th route
            List<HashMap<String, String>> path = routes.get(i);

            // Fetching all the points in i-th route
            for (int j = 0; j < path.size(); j++) {
                HashMap<String, String> point = path.get(j);

                double lat = Double.parseDouble(point.get("lat"));
                double lng = Double.parseDouble(point.get("lng"));
                LatLng position = new LatLng(lat, lng);

                points.add(position);
            }

            // Adding all the points in the route to LineOptions
            lineOptions.addAll(points);
            lineOptions.width(10);
            lineOptions.color(Color.RED);

            Log.d("onPostExecute", "onPostExecute lineoptions decoded");

        }

        // Drawing polyline in the Google Map for the i-th route
        if (lineOptions != null) {
            mMap.addPolyline(lineOptions);
        } else {
            Log.d("onPostExecute", "without Polylines drawn");
        }

       /* marker = mMap.addMarker(new MarkerOptions().position(this.data.getmStartingLatLng())
                .flat(true)
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.car_map)));
        handler = new Handler();
        index = -1;
        next = 1;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (index < polyLineList.size() - 1) {
                    index++;
                    next = index + 1;
                }
                if (index < polyLineList.size() - 1) {
                    startPosition = polyLineList.get(index);
                    endPosition = polyLineList.get(next);
                }
                ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
                valueAnimator.setDuration(3000);
                valueAnimator.setInterpolator(new LinearInterpolator());
                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        v = valueAnimator.getAnimatedFraction();
                        lng = v * endPosition.longitude + (1 - v)
                                * startPosition.longitude;
                        lat = v * endPosition.latitude + (1 - v)
                                * startPosition.latitude;
                        LatLng newPos = new LatLng(lat, lng);
                        marker.setPosition(newPos);
                        marker.setAnchor(0.5f, 0.5f);
                        marker.setRotation((float)getBearing(startPosition, newPos));
                        mMap.moveCamera(CameraUpdateFactory
                                .newCameraPosition
                                        (new CameraPosition.Builder()
                                                .target(newPos)
                                                .zoom(15.5f)
                                                .build()));
                    }
                });
                valueAnimator.start();
                handler.postDelayed(this, 3000);
            }
        }, 3000);*/
    }

    /*private float v;
    private double lat, lng;
    private LatLng startPosition, endPosition;

    private double getBearing(LatLng latLng1, LatLng latLng2) {

        double PI = 3.14159;
        double lat1 = latLng1.latitude * PI / 180;
        double long1 = latLng1.longitude * PI / 180;
        double lat2 = latLng2.latitude * PI / 180;
        double long2 = latLng2.longitude * PI / 180;

        double dLon = (long2 - long1);

        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1)
                * Math.cos(lat2) * Math.cos(dLon);

        double brng = Math.atan2(y, x);

        brng = Math.toDegrees(brng);
        brng = (brng + 360) % 360;

        return brng;
    }*/

    /* private float getBearing(LatLng begin, LatLng end) {
        double lat = Math.abs(begin.latitude - end.latitude);
        double lng = Math.abs(begin.longitude - end.longitude);

        if (begin.latitude < end.latitude && begin.longitude < end.longitude)
            return (float) (Math.toDegrees(Math.atan(lng / lat)));
        else if (begin.latitude >= end.latitude && begin.longitude < end.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 90);
        else if (begin.latitude >= end.latitude && begin.longitude >= end.longitude)
            return (float) (Math.toDegrees(Math.atan(lng / lat)) + 180);
        else if (begin.latitude < end.latitude && begin.longitude >= end.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 270);
        return -1;
    }*/

    private void setDataToViews(Data data) {
        mDetailsTv.setVisibility(View.VISIBLE);
        mDetailsTv.setText(data.routes.get(0).legs.get(0).duration.text + " (" + data.routes.get(0).legs.get(0).distance.text + ")");
    }

    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    private String getUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;


        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = output + "?" + parameters;


        return url;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                    startLocationUpdates();
                }else {
                    AppPreferences appPreferences = new AppPreferences(this);
                    showLongSnackBar(getString(R.string.app_permission_denied_user_clarification_msg));

                    appPreferences.setLocationPermissionDeniedStatus(true);
                }
            }
        }
    }

    private void startLocationUpdates() {
        SmartLocation.with(this).location()
                .start(this);
        if(requestLocationPermission()){
            mMap.setMyLocationEnabled(true);
        }
    }

    private void getLocationPermission() {
    /*
     * Request location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.
     */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
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


                                        ActivityCompat.requestPermissions(LocationActivity.this,
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
    public void onLocationUpdated(Location location) {

        Log.d(TAG, "onLocationUpdated: " + location.getLatitude() + " / " + location.getLongitude());

        if (mLocation == null) {

            mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).position(data.getmStartingLatLng()));
            mMap.addMarker(new MarkerOptions().position(data.getmEndingLatLng()));
            CameraPosition position = new CameraPosition.Builder()
                    .target(data.getmStartingLatLng())
                    .zoom(17.0f).build();

            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(position));
        } else {
            updateCameraBearing(location.getBearing(), location);
        }
        Log.d(TAG, "onLocationUpdated: Speed : " + getSpeed(location, mLocation) * 1.609344);
        mLocation = location;

    }

    public static double getSpeed(Location currentLocation, Location oldLocation) {

        if (oldLocation == null) {
            return 0;
        }
        double newLat = currentLocation.getLatitude();
        double newLon = currentLocation.getLongitude();

        double oldLat = oldLocation.getLatitude();
        double oldLon = oldLocation.getLongitude();

        if (currentLocation.hasSpeed()) {
            return currentLocation.getSpeed();
        } else {
            double radius = 6371000;
            double dLat = Math.toRadians(newLat - oldLat);
            double dLon = Math.toRadians(newLon - oldLon);
            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                    Math.cos(Math.toRadians(newLat)) * Math.cos(Math.toRadians(oldLat)) *
                            Math.sin(dLon / 2) * Math.sin(dLon / 2);
            double c = 2 * Math.asin(Math.sqrt(a));
            double distance = Math.round(radius * c);

            double timeDifferent = currentLocation.getTime() - oldLocation.getTime();
            return distance / timeDifferent;
        }
    }


    private void updateCameraBearing(float bearing, Location location) {
        if (mMap == null) return;
        CameraPosition camPos = CameraPosition
                .builder(
                        mMap.getCameraPosition() // current Camera
                )
                .target(new LatLng(location.getLatitude(), location.getLongitude()))
                .bearing(bearing)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos));
    }


    public class DataParser {

        /**
         * Receives a JSONObject and returns a list of lists containing latitude and longitude
         */
        public List<List<HashMap<String, String>>> parse(JSONObject jObject) {

            List<List<HashMap<String, String>>> routes = new ArrayList<>();
            JSONArray jRoutes;
            JSONArray jLegs;
            JSONArray jSteps;

            try {

                jRoutes = jObject.getJSONArray("routes");

                /** Traversing all routes */
                for (int i = 0; i < jRoutes.length(); i++) {
                    jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
                    List path = new ArrayList<>();

                    /** Traversing all legs */
                    for (int j = 0; j < jLegs.length(); j++) {
                        jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");

                        /** Traversing all steps */
                        for (int k = 0; k < jSteps.length(); k++) {
                            String polyline = "";
                            polyline = (String) ((JSONObject) ((JSONObject) jSteps.get(k)).get("polyline")).get("points");
                            List<LatLng> list = decodePoly(polyline);

                            /** Traversing all points */
                            for (int l = 0; l < list.size(); l++) {
                                HashMap<String, String> hm = new HashMap<>();
                                hm.put("lat", Double.toString((list.get(l)).latitude));
                                hm.put("lng", Double.toString((list.get(l)).longitude));
                                path.add(hm);
                            }
                        }
                        routes.add(path);
                    }
                }

            } catch (JSONException e) {
                Log.e(TAG, "parse: Exception : " + e.getMessage());
            } catch (Exception e) {
            }


            return routes;
        }


        /**
         * Method to decode polyline points
         * Courtesy : https://jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
         */
        private List<LatLng> decodePoly(String encoded) {

            List<LatLng> poly = new ArrayList<>();
            int index = 0, len = encoded.length();
            int lat = 0, lng = 0;

            while (index < len) {
                int b, shift = 0, result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lat += dlat;

                shift = 0;
                result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lng += dlng;

                LatLng p = new LatLng((((double) lat / 1E5)),
                        (((double) lng / 1E5)));
                poly.add(p);
            }

            return poly;
        }
    }


   /* private void startNavigation01() {
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                Uri.parse("http://maps.google.com/maps?daddr=19.228290,72.977003"));
        startActivity(intent);
    }

    private void startNavigation() {
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                Uri.parse("http://maps.google.com/maps?saddr=19.161494,73.001781&daddr=19.228290,72.977003"));
        startActivity(intent);
    }*/



/*
    public void animateMarker(final LatLng startPosition, final LatLng toPosition,
                              final boolean hideMarker) {
        Log.d(TAG, "animateMarker: prev : "+startPosition.latitude+"/"+startPosition.longitude);
        Log.d(TAG, "animateMarker: curr : "+toPosition.latitude+"/"+toPosition.longitude);


        final Marker marker = mMap.addMarker(new MarkerOptions()
                .position(startPosition)
//                .title(mCarParcelableListCurrentLation.get(position).mCarName)
//                .snippet(mCarParcelableListCurrentLation.get(position).mAddress)
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.car_map)));


        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();

        final long duration = 1000;
        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                double lng = t * toPosition.longitude + (1 - t)
                        * startPosition.longitude;
                double lat = t * toPosition.latitude + (1 - t)
                        * startPosition.latitude;

                marker.setPosition(new LatLng(lat, lng));

                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                    if (hideMarker) {
                        marker.setVisible(false);
                    } else {
                        marker.setVisible(true);
                    }
                }
            }
        });
    }*/

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

    public void showLongSnackBar(String message) {
        Snackbar snackbar = Snackbar
                .make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG);
        snackbar.show();
    }
}
