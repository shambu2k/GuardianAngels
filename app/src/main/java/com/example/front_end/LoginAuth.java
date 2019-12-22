package com.example.front_end;

import com.google.gson.annotations.SerializedName;

public class LoginAuth {
    @SerializedName("message")
    String AuthMessage;

    @SerializedName("user_id")
    String userId;



    public LoginAuth(String authMessage) {
        AuthMessage = authMessage;
    }

    public String getAuthMessage() {
        return AuthMessage;
    }

    public String getUserId() {
        return userId;
    }
}
