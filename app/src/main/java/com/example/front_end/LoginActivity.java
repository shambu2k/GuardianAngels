package com.example.front_end;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {

    String[] allPermissions = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
    Manifest.permission.RECORD_AUDIO};
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;

    private Boolean allGranted = false;

    EditText user_edt, pass_edt;
    Button login_butt;
    TextView newRegister_tv, loginStats_tv;

    ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        user_edt = findViewById(R.id.username);
        pass_edt = findViewById(R.id.password);
        login_butt = findViewById(R.id.login_button);
        newRegister_tv = findViewById(R.id.register_new);
        loginStats_tv = findViewById(R.id.login_status);
        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Loading...");
        pDialog.setCancelable(false);

        getPermissions();

        login_butt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 performLogin();
            }
        });
    }

    private void performLogin(){
        if(checkfieldsEmpty()){
            loginStats_tv.setText("Enter all credentials");
            loginStats_tv.setVisibility(View.VISIBLE);
            return;
        }
        else {
            pDialog.show();
            loginStats_tv.setVisibility(View.GONE);
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();
            Retrofit retrofit=new Retrofit.Builder()
                    .baseUrl(BackendApi_Interface.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
            BackendApi_Interface backendApi_interface = retrofit.create(BackendApi_Interface.class);
            Call<LoginAuth> call= backendApi_interface.LogIn(user_edt.getText().toString(),pass_edt.getText().toString());

            call.enqueue(new Callback<LoginAuth>() {
                @Override
                public void onResponse(Call<LoginAuth> call, Response<LoginAuth> response) {
                    if(response.body().getAuthMessage().equals("success"))
                    {
                        pDialog.dismiss();
                        Intent intent = new Intent(new Intent(LoginActivity.this, MainActivity.class));
                        intent.putExtra("userID", response.body().getUserId());
                        startActivity(intent);
                    } else if(response.body().getAuthMessage().equals("failure")) {

                        pDialog.dismiss();
                        loginStats_tv.setVisibility(View.VISIBLE);
                        loginStats_tv.setText("Invalid Credentials");

                    }
                }

                @Override
                public void onFailure(Call<LoginAuth> call, Throwable t) {
                    pDialog.dismiss();
                   /* Intent intent = new Intent(new Intent(LoginActivity.this, MainActivity.class));
                    intent.putExtra("userID", "123");
                    startActivity(intent);  */
                   loginStats_tv.setVisibility(View.VISIBLE);
                    loginStats_tv.setText("Login Failed, Try again later!");
                }
            });
        }
    }

    private boolean checkfieldsEmpty(){
        if(TextUtils.isEmpty(user_edt.getText().toString()) ||
        TextUtils.isEmpty(pass_edt.getText().toString())){
            return true;
        }
        else{
            return false;
        }
    }

    private void getPermissions() {
        if(ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_NETWORK_STATE)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, allPermissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        allGranted = true;
        switch (requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE : {
                if(grantResults.length > 0){
                    for(int i = 0; i < grantResults.length; i++){
                        if(grantResults[i]!=PackageManager.PERMISSION_GRANTED){
                            allGranted = false;
                            return;
                        }
                    }
                }
            }
        }
    }
}
