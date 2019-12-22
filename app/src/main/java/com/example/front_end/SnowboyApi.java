package com.example.front_end;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

public class SnowboyApi {

    private static final String TAG = "SnowboyApi";

    //All these fields are required which Snowboy call api doc doesn't menthoned! dah.
    private static String name = "DistressHotword";
    private static String language= "en";
    private static String age_group= "20_29";
    private static String gender = "M";
    private static String microphone = "LA 20";
    private static String token = "68bd7ea6caa46606f3ce666829ed2daf9911910d";


    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void JsonPreparer(String[] args) throws IOException {
        if(args.length == 4){
            String wav164 = Base64.getEncoder().encodeToString(Files.readAllBytes(Paths.get(args[0])));
            String wav264 = Base64.getEncoder().encodeToString(Files.readAllBytes(Paths.get(args[1])));
            String wav364 = Base64.getEncoder().encodeToString(Files.readAllBytes(Paths.get(args[2])));

         /*   StringBuilder voiceSamples = new StringBuilder();
            voiceSamples.append("[");
            voiceSamples.append("{\"wave\":\""+wav164+"\"},");
            voiceSamples.append("{\"wave\":\""+wav264+"\"},");
            voiceSamples.append("{\"wave\":\""+wav364+"\"}");
            voiceSamples.append("]");

            StringBuilder json = new StringBuilder();
            json.append("{");
            json.append("\"name\":\""+name+"\",");
            json.append("\"language\":\""+language+"\",");
            json.append("\"age_group\":\""+age_group+"\",");
            json.append("\"gender\":\""+gender+"\",");
            json.append("\"microphone\":\""+microphone+"\",");
            json.append("\"token\":\""+token+"\",");
            json.append("\"voice_samples\":"+voiceSamples+"");
            json.append("}");   */

            VoiceSamples v1 = new VoiceSamples(wav164);
            VoiceSamples v2 = new VoiceSamples(wav264);
            VoiceSamples v3 = new VoiceSamples(wav364);

            VoiceSamples[] vs = {v1, v2, v3};


            hwTrainPojo pojo = new hwTrainPojo(gender, age_group, name, language, vs, microphone, token);

            Gson gson = new Gson();
            String json = gson.toJson(pojo);


            Log.d(TAG, "JSON prepared.  "+json);
            callAPI(json, args[3]);

        }else{
           Log.d(TAG, "Usage: java TestSnowboyAPI 1.wav 2.wav 3.wav mypmdl.pmdl");
        }
    }

    private static void callAPI(String json, String outputFile) throws IOException {
        String url="https://snowboy.kitt.ai/api/v1/train/";
        URL urlObj =new URL(url);
        HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();
        con.setDoOutput(true);
        con.setDoInput(true);
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestMethod("POST");
        OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
        wr.write(json);
        wr.close();
        Log.d(TAG, "Request sent");

        int HttpResult = con.getResponseCode();
        if (HttpResult == HttpURLConnection.HTTP_CREATED) {
            Log.d(TAG, "Message Created, Reading response...");
            FileOutputStream outputStream =
                    new FileOutputStream(new File(outputFile));
            InputStream inputStream = con.getInputStream();
            int read = 0;
            byte[] bytes = new byte[inputStream.available()];

            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
            Log.d(TAG, "Writing pmdl file end, closing streams....");
            outputStream.close();
            inputStream.close();
            con.disconnect();
            Log.d(TAG, "Done!");
        } else {
            Log.d(TAG, con.getResponseMessage());
            Log.d(TAG, String.valueOf(con.getResponseCode()));
            BufferedReader br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            Log.d(TAG, sb.toString());
        }
    }
}
