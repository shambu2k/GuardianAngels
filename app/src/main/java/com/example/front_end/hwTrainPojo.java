package com.example.front_end;

import com.google.gson.annotations.SerializedName;

public class hwTrainPojo {

    @SerializedName("gender")
    private String gender;

    @SerializedName("en")
    private String age_group;

    @SerializedName("name")
    private String name;

    @SerializedName("language")
    private String language;

    @SerializedName("voice_samples")
    private VoiceSamples[] voice_samples;

    @SerializedName("microphone")
    private String microphone;

    @SerializedName("token")
    private String token;

    public hwTrainPojo(String gender, String age_group, String name, String language, VoiceSamples[] voice_samples, String microphone, String token) {
        this.gender = gender;
        this.age_group = age_group;
        this.name = name;
        this.language = language;
        this.voice_samples = voice_samples;
        this.microphone = microphone;
        this.token = token;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAge_group() {
        return age_group;
    }

    public void setAge_group(String age_group) {
        this.age_group = age_group;
    }

    public String gethName() {
        return name;
    }

    public void sethName(String name) {
        this.name = name;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public VoiceSamples[] getVoice_samples() {
        return voice_samples;
    }

    public void setVoice_samples(VoiceSamples[] voice_samples) {
        this.voice_samples = voice_samples;
    }

    public String getMicrophone() {
        return microphone;
    }

    public void setMicrophone(String microphone) {
        this.microphone = microphone;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

}
