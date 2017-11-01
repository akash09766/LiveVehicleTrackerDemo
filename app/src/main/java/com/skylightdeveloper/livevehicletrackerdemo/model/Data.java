package com.skylightdeveloper.livevehicletrackerdemo.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by akash.wangalwar on 30/10/17.
 */

public class Data {
    @SerializedName("geocoded_waypoints")
    public List<Geocoded_waypoints> geocoded_waypoints;
    @SerializedName("routes")
    public List<Routes> routes;
    @SerializedName("status")
    public String status;

    public static class Geocoded_waypoints {
        @SerializedName("geocoder_status")
        public String geocoder_status;
        @SerializedName("place_id")
        public String place_id;
        @SerializedName("types")
        public List<String> types;
    }

    public static class Northeast {
        @SerializedName("lat")
        public double lat;
        @SerializedName("lng")
        public double lng;
    }

    public static class Southwest {
        @SerializedName("lat")
        public double lat;
        @SerializedName("lng")
        public double lng;
    }

    public static class Bounds {
        @SerializedName("northeast")
        public Northeast northeast;
        @SerializedName("southwest")
        public Southwest southwest;
    }

    public static class Distance {
        @SerializedName("text")
        public String text;
        @SerializedName("value")
        public int value;
    }

    public static class Duration {
        @SerializedName("text")
        public String text;
        @SerializedName("value")
        public int value;
    }

    public static class End_location {
        @SerializedName("lat")
        public double lat;
        @SerializedName("lng")
        public double lng;
    }

    public static class Polyline {
        @SerializedName("points")
        public String points;
    }

    public static class Start_location {
        @SerializedName("lat")
        public double lat;
        @SerializedName("lng")
        public double lng;
    }

    public static class Steps {
        @SerializedName("distance")
        public Distance distance;
        @SerializedName("duration")
        public Duration duration;
        @SerializedName("end_location")
        public End_location end_location;
        @SerializedName("html_instructions")
        public String html_instructions;
        @SerializedName("polyline")
        public Polyline polyline;
        @SerializedName("start_location")
        public Start_location start_location;
        @SerializedName("travel_mode")
        public String travel_mode;
    }

    public static class Traffic_speed_entry {
    }

    public static class Via_waypoint {
    }

    public static class Legs {
        @SerializedName("distance")
        public Distance distance;
        @SerializedName("duration")
        public Duration duration;
        @SerializedName("end_address")
        public String end_address;
        @SerializedName("end_location")
        public End_location end_location;
        @SerializedName("start_address")
        public String start_address;
        @SerializedName("start_location")
        public Start_location start_location;
        @SerializedName("steps")
        public List<Steps> steps;
        @SerializedName("traffic_speed_entry")
        public List<Traffic_speed_entry> traffic_speed_entry;
        @SerializedName("via_waypoint")
        public List<Via_waypoint> via_waypoint;
    }

    public static class Overview_polyline {
        @SerializedName("points")
        public String points;
    }

    public static class Warnings {
    }

    public static class Waypoint_order {
    }

    public static class Routes {
        @SerializedName("bounds")
        public Bounds bounds;
        @SerializedName("copyrights")
        public String copyrights;
        @SerializedName("legs")
        public List<Legs> legs;
        @SerializedName("overview_polyline")
        public Overview_polyline overview_polyline;
        @SerializedName("summary")
        public String summary;
        @SerializedName("warnings")
        public List<Warnings> warnings;
        @SerializedName("waypoint_order")
        public List<Waypoint_order> waypoint_order;
    }
}
