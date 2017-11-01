package com.skylightdeveloper.livevehicletrackerdemo.model;

import java.util.ArrayList;

/**
 * Created by Akash Wangalwar on 23-09-2016.
 */
public class Address {

    public ArrayList<AddressData> getResults() {
        return results;
    }

    public void setResults(ArrayList<AddressData> results) {
        this.results = results;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getError_message() {
        return error_message;
    }

    public void setError_message(String error_message) {
        this.error_message = error_message;
    }

    private String error_message;
    private ArrayList<AddressData> results;
    private String status;
}

