package com.example.front_end;

import com.google.gson.annotations.SerializedName;

public class HelperUpdatePojo {
    @SerializedName("message")
    String status;

    public String getStatus() {
        return status;
    }
}
