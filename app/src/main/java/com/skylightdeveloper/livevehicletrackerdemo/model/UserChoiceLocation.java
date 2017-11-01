package com.skylightdeveloper.livevehicletrackerdemo.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by akash.wangalwar on 31/10/17.
 */

public class UserChoiceLocation implements Parcelable {

    private String mAddress;
    private LatLng mLocation;

    public UserChoiceLocation(String mAddress, LatLng mLocation) {
        this.mAddress = mAddress;
        this.mLocation = mLocation;
    }

    public String getmAddress() {
        return mAddress;
    }

    public LatLng getmLocation() {
        return mLocation;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mAddress);
        dest.writeParcelable(this.mLocation, flags);
    }

    protected UserChoiceLocation(Parcel in) {
        this.mAddress = in.readString();
        this.mLocation = in.readParcelable(LatLng.class.getClassLoader());
    }

    public static final Parcelable.Creator<UserChoiceLocation> CREATOR = new Parcelable.Creator<UserChoiceLocation>() {
        @Override
        public UserChoiceLocation createFromParcel(Parcel source) {
            return new UserChoiceLocation(source);
        }

        @Override
        public UserChoiceLocation[] newArray(int size) {
            return new UserChoiceLocation[size];
        }
    };
}
