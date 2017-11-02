package com.skylightdeveloper.livevehicletrackerdemo.preferences;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by akash.wangalwar on 02/11/17.
 */

public class AppPreferences {

    private static final String APP_SHARED_PREFS = "livevehicletrackerdemo";
    private SharedPreferences _sharedPrefs;
    private SharedPreferences.Editor _prefsEditor;
    private static final String LOCATION_PERMISSION = "location_permission";

    public AppPreferences(Context context) {
        try {
            this._sharedPrefs = context.getSharedPreferences(APP_SHARED_PREFS,
                    Activity.MODE_PRIVATE);
            this._prefsEditor = _sharedPrefs.edit();
        } catch (Exception e) {
        }
    }

    public boolean getLocationaPermissionDeniedStatus() {
        return _sharedPrefs.getBoolean(LOCATION_PERMISSION, false);
    }

    public void setLocationPermissionDeniedStatus(boolean isLoggedIn) {

        _prefsEditor.putBoolean(LOCATION_PERMISSION, isLoggedIn);
        _prefsEditor.commit();
    }
}