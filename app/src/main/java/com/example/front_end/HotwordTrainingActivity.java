package com.example.front_end;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;


import java.io.File;
import java.io.IOException;

public class HotwordTrainingActivity extends AppCompatActivity {

    private static final String TAG = HotwordTrainingActivity.class.getSimpleName();

    ProgressBar spinner;
    Button record_butt;
    Boolean recording = false;

    TextView ttv;

    WavRecorder recorder;

    int counter = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotword_training);

        spinner = findViewById(R.id.spinner);
        ttv = findViewById(R.id.text_hwins);
        record_butt = findViewById(R.id.record_button);


        record_butt.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                if (!recording && counter <= 3) {
                    spinner.setVisibility(View.VISIBLE);
                    record_butt.setText("Stop");
                    recording = true;
                    startRecording();
                } else {
                    if (counter >= 3) {
                        record_butt.setVisibility(View.GONE);
                        startTheEncode();
                    }
                    spinner.setVisibility(View.GONE);
                    record_butt.setText("Start");
                    recording = false;
                    recorder.stopRecording();
                    Log.d(TAG, "This is wav no " + counter);
                    counter++;

                }
            }
        });
    }

    private void startRecording() {
        String PATH = "/sdcard/";
        String directoryName = PATH.concat("SOS");

        File directory = new File(directoryName);
        if (! directory.exists()){
            directory.mkdir();
        }
        recorder = new WavRecorder("/sdcard/HELPER" + "/train" + counter + ".wav");
        recorder.startRecording();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startTheEncode() {
        spinner.setVisibility(View.VISIBLE);
        ttv.setText("Audio needs to be encoded and sent to server. Please wait..");

        String[] arfs = {"/sdcard/HELPER" + "/train1.wav", "/sdcard/HELPER" + "/train2.wav", "/sdcard/HELPER" + "/train3.wav"};


        try {
            SnowboyApi.JsonPreparer(arfs);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}

/*  File file1 = new File("/sdcard/HELPER" + "/train1.wav");
        File file2 = new File("/sdcard/HELPER" + "/train2.wav");
        File file3 = new File("/sdcard/HELPER" + "/train3.wav");

        byte[] bytes1 = FileUtils.readFileToByteArray(file1);
        byte[] bytes2 = FileUtils.readFileToByteArray(file2);
        byte[] bytes3 = FileUtils.readFileToByteArray(file3);

        String encoded1 = Base64.encodeToString(bytes1, 0); Log.d(TAG, encoded1);
        String encoded2 = Base64.encodeToString(bytes2, 0); Log.d(TAG, encoded2);
        String encoded3 = Base64.encodeToString(bytes3, 0); Log.d(TAG, encoded3);

        String[] samples = {encoded1, encoded2, encoded3};

        hwTrainPojo pojo = new hwTrainPojo("M", "20_29", "DistressHotword", "en", samples, "phone_mic", getString(R.string.SnowBoy_key));

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(SnowboyTrain_Interface.Snowboy_BASE_URL)
                .client(new OkHttpClient.Builder().build())
                .build();
        SnowboyTrain_Interface snowboyInterface = retrofit.create(SnowboyTrain_Interface.class);

        Call<ResponseBody> call = snowboyInterface.downloadOfflineData(pojo);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Got the body for the file");

                    Toast.makeText(getApplicationContext(), "Downloading...", Toast.LENGTH_SHORT).show();
                    downloadFileTask = new DownloadFileTask();
                    downloadFileTask.execute(response.body());

                } else {
                    Log.d(TAG, "Connection failed " + response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "FAILED!!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class DownloadFileTask extends AsyncTask<ResponseBody, Pair<Integer, Long>, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(ResponseBody... urls) {
            //Copy you logic to calculate progress and call
            saveToDisk(urls[0], "hotword.pmdl");
            return null;
        }

        protected void onProgressUpdate(Pair<Integer, Long>... progress) {

            Log.d("API123", progress[0].second + " ");

            if (progress[0].first == 100)
                Toast.makeText(getApplicationContext(), "File downloaded successfully", Toast.LENGTH_SHORT).show();


            if (progress[0].second > 0) {
                int currentProgress = (int) ((double) progress[0].first / (double) progress[0].second * 100);
                ttv.setText("Progress " + currentProgress + "%");

            }

            if (progress[0].first == -1) {
                Toast.makeText(getApplicationContext(), "Download failed", Toast.LENGTH_SHORT).show();
            }

        }

        public void doProgress(Pair<Integer, Long> progressDetails) {
            publishProgress(progressDetails);
        }

        @Override
        protected void onPostExecute(String result) {

        }
    }

    private void saveToDisk(ResponseBody body, String filename) {
        try {

            File destinationFile = new File("/sdcard/Snowboy", filename);

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(destinationFile);
                byte data[] = new byte[4096];
                int count;
                int progress = 0;
                long fileSize = body.contentLength();
                Log.d(TAG, "File Size=" + fileSize);
                while ((count = inputStream.read(data)) != -1) {
                    outputStream.write(data, 0, count);
                    progress += count;
                    Pair<Integer, Long> pairs = new Pair<>(progress, fileSize);
                    downloadFileTask.doProgress(pairs);
                    Log.d(TAG, "Progress: " + progress + "/" + fileSize + " >>>> " + (float) progress / fileSize);
                }

                outputStream.flush();

                Log.d(TAG, destinationFile.getParent());
                Pair<Integer, Long> pairs = new Pair<>(100, 100L);
                downloadFileTask.doProgress(pairs);
                return;
            } catch (IOException e) {
                e.printStackTrace();
                Pair<Integer, Long> pairs = new Pair<>(-1, Long.valueOf(-1));
                downloadFileTask.doProgress(pairs);
                Log.d(TAG, "Failed to save the file!");
                return;
            } finally {
                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "Failed to save the file!");
            return;
        }
    }  */
