package com.skylightdeveloper.livevehicletrackerdemo;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by akash.wangalwar on 30/10/17.
 */

public interface NetworkManager {

    @GET("json?origin=19.161248,73.000011&destination=21.142361,79.097034&sensor=false&key=AIzaSyARF2IM-bQYkKlquxTiHCtHxraVw2GYybU")
    Call<String> getRoutes();
}
