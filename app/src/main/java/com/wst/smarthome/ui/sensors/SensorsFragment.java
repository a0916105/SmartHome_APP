package com.wst.smarthome.ui.sensors;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wst.smarthome.HttpCRUD;
import com.wst.smarthome.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class SensorsFragment extends Fragment {

    private TextView textViewCelsius,textViewHumidity,textViewPM2_5,textViewCelsiusMax,textViewCelsiusMin,textViewCelsiusAvg,textViewHumidityMax,textViewHumidityMin,textViewHumidityAvg,textViewPM2_5Max,textViewPM2_5Min,textViewPM2_5Avg;
    private Timer timer;
    private TimerTask task;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_sensors, container, false);

        textViewCelsius=(TextView)root.findViewById(R.id.textView_celsius);
        textViewHumidity=(TextView)root.findViewById(R.id.textView_humidity);
        textViewPM2_5=(TextView)root.findViewById(R.id.textView_PM2_5);
        textViewCelsiusMax=(TextView)root.findViewById(R.id.textView_celsius_max);
        textViewCelsiusMin=(TextView)root.findViewById(R.id.textView_celsius_min);
        textViewCelsiusAvg=(TextView)root.findViewById(R.id.textView_celsius_avg);
        textViewHumidityMax=(TextView)root.findViewById(R.id.textView_humidity_max);
        textViewHumidityMin=(TextView)root.findViewById(R.id.textView_humidity_min);
        textViewHumidityAvg=(TextView)root.findViewById(R.id.textView_humidity_avg);
        textViewPM2_5Max=(TextView)root.findViewById(R.id.textView_PM2_5_max);
        textViewPM2_5Min=(TextView)root.findViewById(R.id.textView_PM2_5_min);
        textViewPM2_5Avg=(TextView)root.findViewById(R.id.textView_PM2_5_avg);

        return root;
    }

    @Override
    public void onStop() {
        super.onStop();
        timer.cancel();
    }

    @Override
    public void onResume() {
        super.onResume();
        timer = new Timer();
        task = new TimerTask() {
            @Override
            public void run() {
                new HttpCRUD(HttpCRUD.READ_SENSORS,"sensors=today"){
                    private JSONArray jsonArray;
                    private String celsius,humidity,pm2_5,celsiusMax,humidityMax,pm2_5Max,celsiusMin,humidityMin,pm2_5Min,celsiusAvg,humidityAvg,pm2_5Avg;

                    @Override
                    public void run() {
                        super.run();

                        if (dataString.length()==0)
                            return;

                        try {
                            //將網頁回傳資料轉成JSON陣列
                            jsonArray = new JSONArray(dataString);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        JSONObject jsonObj = null;
                        try {
                            //從JSON陣列的第0個元素(現在)來取得JSON物件
                            jsonObj = jsonArray.getJSONObject(0);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        try {
                            //從JSON物件中取得key對應的值
                            celsius = String.format("%.1f",jsonObj.getDouble("celsius"));
                        } catch (JSONException e) {
                            celsius="";
                            e.printStackTrace();
                        }
                        try {
                            humidity = String.format("%.1f",jsonObj.getDouble("humidity"));
                        } catch (JSONException e) {
                            humidity="";
                            e.printStackTrace();
                        }
                        try {
                            pm2_5 = String.format("%.1f",jsonObj.getDouble("pm2_5"));
                        } catch (JSONException e) {
                            pm2_5="";
                            e.printStackTrace();
                        }
                        try {
                            //從JSON陣列的第1個元素(Max)來取得JSON物件
                            jsonObj = jsonArray.getJSONObject(1);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        try {
                            celsiusMax = String.format("%.1f",jsonObj.getDouble("celsius"));
                        } catch (JSONException e) {
                            celsiusMax="";
                            e.printStackTrace();
                        }
                        try {
                            humidityMax = String.format("%.1f",jsonObj.getDouble("humidity"));
                        } catch (JSONException e) {
                            humidityMax="";
                            e.printStackTrace();
                        }
                        try {
                            pm2_5Max = String.format("%.1f",jsonObj.getDouble("pm2_5"));
                        } catch (JSONException e) {
                            pm2_5Max="";
                            e.printStackTrace();
                        }
                        try {
                            //從JSON陣列的第2個元素(Min)來取得JSON物件
                            jsonObj = jsonArray.getJSONObject(2);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        try {
                            celsiusMin = String.format("%.1f",jsonObj.getDouble("celsius"));
                        } catch (JSONException e) {
                            celsiusMin="";
                            e.printStackTrace();
                        }
                        try {
                            humidityMin = String.format("%.1f",jsonObj.getDouble("humidity"));
                        } catch (JSONException e) {
                            humidityMin="";
                            e.printStackTrace();
                        }
                        try {
                            pm2_5Min = String.format("%.1f",jsonObj.getDouble("pm2_5"));
                        } catch (JSONException e) {
                            pm2_5Min="";
                            e.printStackTrace();
                        }
                        try {
                            //從JSON陣列的第3個元素(Avg)來取得JSON物件
                            jsonObj = jsonArray.getJSONObject(3);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        try {
                            celsiusAvg = String.format("%.1f",jsonObj.getDouble("celsius"));
                        } catch (JSONException e) {
                            celsiusAvg="";
                            e.printStackTrace();
                        }
                        try {
                            humidityAvg = String.format("%.1f",jsonObj.getDouble("humidity"));
                        } catch (JSONException e) {
                            humidityAvg="";
                            e.printStackTrace();
                        }
                        try {
                            pm2_5Avg = String.format("%.1f",jsonObj.getDouble("pm2_5"));
                        } catch (JSONException e) {
                            pm2_5Avg="";
                            e.printStackTrace();
                        }

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                textViewCelsius.setText(celsius);
                                textViewHumidity.setText(humidity);
                                textViewPM2_5.setText(pm2_5);
                                textViewCelsiusMax.setText(celsiusMax);
                                textViewCelsiusMin.setText(celsiusMin);
                                textViewCelsiusAvg.setText(celsiusAvg);
                                textViewHumidityMax.setText(humidityMax);
                                textViewHumidityMin.setText(humidityMin);
                                textViewHumidityAvg.setText(humidityAvg);
                                textViewPM2_5Max.setText(pm2_5Max);
                                textViewPM2_5Min.setText(pm2_5Min);
                                textViewPM2_5Avg.setText(pm2_5Avg);
                            }
                        });
                    }
                }.start();
            }
        };
        timer.schedule(task,0,5000);
    }
}