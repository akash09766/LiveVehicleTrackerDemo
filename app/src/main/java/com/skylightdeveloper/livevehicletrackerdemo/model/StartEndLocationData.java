package com.skylightdeveloper.livevehicletrackerdemo.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by akash.wangalwar on 31/10/17.
 */

public class StartEndLocationData implements Parcelable {
    public LatLng getmStartingLatLng() {
        return mStartingLatLng;
    }

    public LatLng getmEndingLatLng() {
        return mEndingLatLng;
    }

    private LatLng mStartingLatLng, mEndingLatLng;

    public StartEndLocationData(LatLng mStartingLatLng, LatLng mEndingLatLng) {
        this.mStartingLatLng = mStartingLatLng;
        this.mEndingLatLng = mEndingLatLng;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.mStartingLatLng, flags);
        dest.writeParcelable(this.mEndingLatLng, flags);
    }

    protected StartEndLocationData(Parcel in) {
        this.mStartingLatLng = in.readParcelable(LatLng.class.getClassLoader());
        this.mEndingLatLng = in.readParcelable(LatLng.class.getClassLoader());
    }

    public static final Parcelable.Creator<StartEndLocationData> CREATOR = new Parcelable.Creator<StartEndLocationData>() {
        @Override
        public StartEndLocationData createFromParcel(Parcel source) {
            return new StartEndLocationData(source);
        }

        @Override
        public StartEndLocationData[] newArray(int size) {
            return new StartEndLocationData[size];
        }
    };
}
