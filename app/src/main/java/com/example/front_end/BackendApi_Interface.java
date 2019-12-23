package com.example.front_end;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface BackendApi_Interface {

    public String BASE_URL = "http://05271d04.ngrok.io";
    @FormUrlEncoded
    @POST("/login")
    Call<LoginAuth> LogIn( @Field("user") String username, @Field("pass") String password);

    @FormUrlEncoded
    @POST("/location_update")
    Call<HelperUpdatePojo> sendHloc(@Field("latitude") String lat, @Field("longitude") String lng, @Field("user_id") String id,
                          @Field("timestamp") String timeStamp);

    @FormUrlEncoded
    @POST("/distress")
    Call<List<HelperPojo>> sendDistress(@Field("latitude") String lat, @Field("longitude") String lng, @Field("user_id") String id,
                                 @Field("timestamp") String timeStamp);


}
