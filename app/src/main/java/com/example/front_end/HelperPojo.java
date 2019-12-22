package com.example.front_end;

import com.google.gson.annotations.SerializedName;

public class HelperPojo {
    @SerializedName("helper_id")
    private String hId;

    @SerializedName("name")
    private String hName;

    @SerializedName("latitude")
    private String lat;

    @SerializedName("longitude")
    private String lng;

    public String gethId() {
        return hId;
    }

    public String gethName() {
        return hName;
    }

    public String getLat() {
        return lat;
    }

    public String getLng() {
        return lng;
    }
}
