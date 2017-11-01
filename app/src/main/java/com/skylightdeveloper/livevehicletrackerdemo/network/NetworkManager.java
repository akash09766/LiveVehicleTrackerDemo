package com.skylightdeveloper.livevehicletrackerdemo.network;

import android.content.Context;

import com.skylightdeveloper.livevehicletrackerdemo.R;
import com.skylightdeveloper.livevehicletrackerdemo.model.Address;
import com.skylightdeveloper.livevehicletrackerdemo.model.Data;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by akash.wangalwar on 30/10/17.
 */

public interface NetworkManager {

    @GET("json")
    Call<Data> getRoutes(@Query("mode") String mode, @Query("transit_routing_preference") String transit_routing_preference,
                         @Query("origin") String origin, @Query("destination") String destination, @Query("sensor") boolean sensor, @Query("key") String key);

    @GET("json")
    Call<Address> getAddressFromlatLang(@Query("latlng") String latlng, @Query("location_type") String location_type, @Query("result_type") String result_type, @Query("key") String key);

    @GET("json")
    Call<Address> retryAddressFromlatLang(@Query("latlng") String latlng);
}
