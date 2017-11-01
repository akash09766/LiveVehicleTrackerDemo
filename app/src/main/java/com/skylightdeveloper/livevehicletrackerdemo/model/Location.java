package com.skylightdeveloper.livevehicletrackerdemo.model;

/**
 * Created by Akash Wangalwar on 23-09-2016.
 */
public class Location {

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    private double lat;
    private double lng;
}
