package com.example.front_end;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vikramezhil.droidspeech.DroidSpeech;
import com.vikramezhil.droidspeech.OnDSListener;
import com.vikramezhil.droidspeech.OnDSPermissionsListener;


import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
        DialogInterface.OnClickListener, OnDSListener, OnDSPermissionsListener {

    DroidSpeech droidSpeech;
    private String TAG = "MainActivity";
    private String uid;
    Button sos, lendHelp, trainhw;

    private WavRecorder recorder;

    private Boolean sosToggle =false, lendHtoggle = false;

    GoogleMap mMap;
    SupportMapFragment mapFragment;
    LocationRequest mLocationRequest;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    FusedLocationProviderClient mFusedLocationClient;
    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                //The last location in the list is the newest
                Location location = locationList.get(locationList.size() - 1);
                Log.i(TAG, "Location: " + location.getLatitude() + " " + location.getLongitude());
                mLastLocation = location;
                if (mCurrLocationMarker != null) {
                    mCurrLocationMarker.remove();
                }

                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                //move map camera
               // mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 11));
            }
        }
    };

    private final static int INTERVAL = 5000; //5 seconds
    Handler mHandlerV = new Handler();

    Runnable mHandlerTaskV = new Runnable()
    {
        @Override
        public void run() {
            victimDistress();
            mHandlerV.postDelayed(mHandlerTaskV, INTERVAL);
        }
    };

    Handler mHandlerH = new Handler();

    Runnable mHandlerTaskH = new Runnable()
    {
        @Override
        public void run() {
            helperLocationUpdate();
            mHandlerH.postDelayed(mHandlerTaskH, INTERVAL);
        }
    };

    private List<Marker> helperMarkers;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        droidSpeech = new DroidSpeech(this, null);
        droidSpeech.setOnDroidSpeechListener(this);


        Intent intent = getIntent();
        uid = intent.getStringExtra("userID");

        helperMarkers = new ArrayList<>();

        sos = findViewById(R.id.sos_button);
        lendHelp = findViewById(R.id.helpOn_button);
        trainhw = findViewById(R.id.train_hw);

        sos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!sosToggle && !lendHtoggle){
                    startSOSrecording();
                    sosToggle = true;
                    mHandlerTaskV.run();
                    sos.setText("Stop SOS");
                }else {
                    stopSOSRecording();
                    sosToggle = false;
                    mHandlerV.removeCallbacks(mHandlerTaskV);
                    sos.setText("SOS");
                }

            }
        });

        lendHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!lendHtoggle && !sosToggle){
                    lendHtoggle = true;
                    mHandlerV.removeCallbacks(mHandlerTaskV);
                    mHandlerTaskH.run();
                    lendHelp.setText("Stop Helping");
                }
                else {
                    lendHtoggle = false;
                    lendHelp.setText("Lend help");
                    mHandlerV.removeCallbacks(mHandlerTaskV);
                    mHandlerH.removeCallbacks(mHandlerTaskH);
                }

            }
        });

        trainhw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, HotwordTrainingActivity.class));
            }
        });

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        initMap();
    }

    private void helperLocationUpdate() {
        LocalDateTime now = LocalDateTime.now();
        char[] timeStamp= now.toString().toCharArray();
        timeStamp[10] = ' ';
        char[] timeStamp2 = Arrays.copyOfRange(timeStamp, 0, 19);

        Log.d(TAG, "Time is: " + String.valueOf(timeStamp));
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BackendApi_Interface.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        BackendApi_Interface backendApi_interface = retrofit.create(BackendApi_Interface.class);
        Call<HelperUpdatePojo> call = backendApi_interface.sendHloc(String.valueOf(mLastLocation.getLatitude()), String.valueOf(mLastLocation.getLongitude()), uid, String.valueOf(timeStamp2));
        call.enqueue(new Callback<HelperUpdatePojo>() {
            @Override
            public void onResponse(Call<HelperUpdatePojo> call, Response<HelperUpdatePojo> response) {
                if(response.body().getStatus()!=null){
                    if(response.body().getStatus().equals("success")){
                        Toast.makeText(getApplicationContext(), "Helper location updated", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<HelperUpdatePojo> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Helper location not updated", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void victimDistress(){
        LocalDateTime now = LocalDateTime.now();
        char[] timeStamp= now.toString().toCharArray();
        timeStamp[10] = ' ';
        char[] timeStamp2 = Arrays.copyOfRange(timeStamp, 0, 19);

        Log.d(TAG, "Time is: " + String.valueOf(timeStamp));
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BackendApi_Interface.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        BackendApi_Interface backendApi_interface = retrofit.create(BackendApi_Interface.class);
        Call<List<HelperPojo>> call = backendApi_interface.sendDistress(String.valueOf(mLastLocation.getLatitude()), String.valueOf(mLastLocation.getLongitude()), uid, String.valueOf(timeStamp2));

        call.enqueue(new Callback<List<HelperPojo>>() {
            @Override
            public void onResponse(Call<List<HelperPojo>> call, Response<List<HelperPojo>> response) {

                if(response.body()!=null){
                    Log.d(TAG, response.body().toString());
                    updateMarkers(response.body());
                }

            }

            @Override
            public void onFailure(Call<List<HelperPojo>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "No helpers around", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateMarkers(List<HelperPojo> hList){

        if(helperMarkers!=null){
            for (int i = 0; i < helperMarkers.size(); i++ ) {
                helperMarkers.get(i).remove();
            }
            helperMarkers = new ArrayList<>();
        }

        for(int i =0; i<hList.size(); i++){
           helperMarkers.add(mMap.addMarker(new MarkerOptions()
            .position(new LatLng(Float.valueOf(hList.get(i).getLat()),Float.valueOf(hList.get(i).getLng())))
            .title(hList.get(i).gethName())
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.helper_location_marker))));
        }

    }

    private void initMap(){
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MainActivity.this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        droidSpeech.startDroidSpeechRecognition();
        Log.d(TAG, "Map is ready");
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);


        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
      //  mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        mMap.setMyLocationEnabled(true);
    }

    private void startSOSrecording(){
        LocalDateTime now = LocalDateTime.now();
        String PATH = "/sdcard/";
        String directoryName = PATH.concat("SOS/");

        File directory = new File(directoryName);
        if (! directory.exists()){
            directory.mkdir();
        }

        String direc2 = directoryName.concat("Blackbox");
        File dir2 = new File(direc2);
        if (! dir2.exists()){
            dir2.mkdir();
        }

        recorder = new WavRecorder("/sdcard/SOS/Blackbox/"+now.toString()+".wav");
        recorder.startRecording();
    }

    private void stopSOSRecording() {
        if(recorder!=null){
            recorder.stopRecording();
        }
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this).setMessage("Start navigation to helper?")
                .setTitle("Choose action").setCancelable(true).setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Uri gmmIntentUri = Uri.parse("google.navigation:q="+marker.getPosition().latitude+","+marker.getPosition().longitude+"&mode=w");
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                        mapIntent.setPackage("com.google.android.apps.maps");
                        startActivity(mapIntent);
                    }
                }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        return false;
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onClick(DialogInterface dialog, int which) {

    }

    @Override
    public void onDroidSpeechSupportedLanguages(String currentSpeechLanguage, List<String> supportedSpeechLanguages) {

    }

    @Override
    public void onDroidSpeechRmsChanged(float rmsChangedValue) {

    }

    @Override
    public void onDroidSpeechLiveResult(String liveSpeechResult) {

        Log.d(TAG, liveSpeechResult);
        if(liveSpeechResult.contains("Domino")){
            sosToggle = true;
            mHandlerTaskV.run();
            sos.setText("Stop SOS");
            Toast.makeText(getApplicationContext(), "Hotword Triggered", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDroidSpeechFinalResult(String finalSpeechResult) {
        if(finalSpeechResult.contains("Domino")){
            sosToggle = true;
            mHandlerTaskV.run();
            sos.setText("Stop SOS");
            Toast.makeText(getApplicationContext(), "Triggered", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDroidSpeechClosedByUser() {

    }

    @Override
    public void onDroidSpeechError(String errorMsg) {

    }

    @Override
    public void onDroidSpeechAudioPermissionStatus(boolean audioPermissionGiven, String errorMsgIfAny) {

    }
}
