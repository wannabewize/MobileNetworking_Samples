package com.wannabewize.example.jsonrequest_okhttp;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "JsonRequest";
    private OkHttpClient client;
    private String serverAddress = "http://192.168.0.129:3000/upload";
    private TextView resultTextView;
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        client = new OkHttpClient();

        gson = new Gson();

        final EditText mNameField = (EditText) findViewById(R.id.nameEditText);
        resultTextView = (TextView) findViewById(R.id.resultTextView);

        findViewById(R.id.sendButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = mNameField.getText().toString();
                new NetworkTask().execute(name);
            }
        });
    }

    // GSON
    public class Data {
        NameObject data;

        class NameObject {

            String name;

            public NameObject(String name) {
                this.name = name;
            }
        }
    }

    class NetworkTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                String name = params[0];

                // JSON 준비 - 문자열
                String jsonStr = "{ \"data\" : { \"name\":\"" + name + "\" } }";

                // GSON
                Data rootObj = new Data();
                rootObj.data = rootObj.new NameObject(name);
                String jsonStr2 = gson.toJson(rootObj);

                Log.d(TAG, "GSON : " + jsonStr2);

                RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonStr2);

                // 요청
                Request request = new Request.Builder().url(serverAddress).post(body).build();
                Call call = client.newCall(request);
                Response response = call.execute();
                if ( response.isSuccessful() ) {
                    String result = response.body().string();
                    return result;
                }
                else {
                    Log.e(TAG, "Error : " + response.message());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if ( s != null ) {
                resultTextView.setText(s);
            }
            else {
                resultTextView.setText("에러 발생");
            }
        }
    }


}
