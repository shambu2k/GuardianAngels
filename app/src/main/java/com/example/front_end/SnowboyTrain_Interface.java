package com.example.front_end;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Streaming;

public interface SnowboyTrain_Interface {
    public String Snowboy_BASE_URL = "https://snowboy.kitt.ai";

    @POST("/api/v1/train/")
    Call<ResponseBody> downloadOfflineData(@Body hwTrainPojo trainData);
}
