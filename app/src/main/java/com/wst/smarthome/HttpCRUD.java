package com.wst.smarthome;

import android.util.Log;

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

public class HttpCRUD extends Thread{
    private URL url;
    private String webAddress="http://192.168.43.223:8080/SmartHome/";//筆電
//    private String webAddress="http://192.168.59.123:8080/SmartHome/";//教室
    private String subUrl;
    private String[] subUrlArray={"API_Insert", "API_Show", "API_Show_Sensors", "API_Show_ESP32", "API_Update", "API_Update_settings", "API_Update_ESP32", "API_Delete"};
    public final static int INSERT_SCHEDULE=0;
    public final static int SHOW_SCHEDULE=1;
    public final static int READ_SENSORS=2;
    public final static int SHOW_SETTINGS=3;
    public final static int UPDATE_SCHEDULE=4;
    public final static int UPDATE_SETTINGS=5;
    public final static int DISABLE_SCHEDULE=6;
    public final static int DELETE_SCHEDULE=7;
    private HttpURLConnection conn;
    private int code;
    private InputStream is;
    protected String dataString="";
    private String param;
    private OutputStream out;

    public HttpCRUD(final int subUrl_index, String param) {
        this.subUrl = subUrlArray[subUrl_index];
        this.param = param;
    }

    @Override
    public void run() {
        super.run();
        try {
            url = new URL(webAddress+subUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            conn = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            //設定引數
            conn.setRequestMethod("POST");   //設定POST方式連線
            conn.setDoOutput(true);   //需要輸出
            conn.setUseCaches(false);  //不允許快取
            //conn.getOutputStream()會自動connect
            out = conn.getOutputStream();
            //建立輸出流，向指向的URL傳入引數
            OutputStreamWriter writer = new OutputStreamWriter(out);

            writer.write(param);
            writer.flush();
            writer.close();
            out.close();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            code = conn.getResponseCode();
            Log.d("HttpCRUD", "code=" + code);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (code == HttpURLConnection.HTTP_OK) {
            try {
                is = conn.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            InputStreamReader reader = new InputStreamReader(is);
            BufferedReader stringReader = new BufferedReader(reader);
            String line = "";
            try {
                while ((line = stringReader.readLine()) != null)
                    dataString += (line+"\n");
                Log.d("HttpCRUD", "dataString=" + dataString);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
