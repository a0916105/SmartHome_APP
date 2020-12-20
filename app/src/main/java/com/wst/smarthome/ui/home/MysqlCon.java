package com.wst.smarthome.ui.home;

import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MysqlCon {

    // 資料庫定義
    String mysql_ip = "192.168.43.223";//筆電
//    String mysql_ip = "192.168.59.123";//教室
    int mysql_port = 3306; // Port 預設為 3306
    String db_name = "smart_home";
    String url = "jdbc:mysql://"+mysql_ip+":"+mysql_port+"/"+db_name;
    String db_user = "wst";
    String db_password = "1qaz@wsx";
    private Connection con;

    public void run() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Log.v("DB","加載驅動成功");
        }catch( ClassNotFoundException e) {
            Log.e("DB","加載驅動失敗");
            return;
        }

        // 連接資料庫
        try {
            con = DriverManager.getConnection(url,db_user,db_password);
            Log.v("DB","遠端連接成功");
        }catch(SQLException e) {
            Log.e("DB","遠端連接失敗");
            Log.e("DB", e.toString());
        }
    }

    public String getData() {
        String data = "";
        try {
            con = DriverManager.getConnection(url, db_user, db_password);
            String sql = "SELECT * FROM `home000_sensors`";
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next())
            {
                String id = rs.getString("id");
                String celsius = rs.getString("celsius");
                String humidity = rs.getString("humidity");
                String pm2_5 = rs.getString("PM2_5");
                String datetime = rs.getString("datetime");
                data += id + "：" + celsius + ", " + humidity + ", " + pm2_5 + ", " + datetime + "\n";
            }
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    public boolean[] getDeviceState() {
        boolean[] data = new boolean[3];
        try {
            con = DriverManager.getConnection(url, db_user, db_password);
            String sql = "SELECT AC,DH,AP FROM `home000_settings` WHERE item='set_values'";
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);

            rs.next();
            int ac = rs.getInt("AC");
            int dh = rs.getInt("DH");
            int ap = rs.getInt("AP");

            st.close();

            data[0] = ac == 1;
            data[1] = dh == 1;
            data[2] = ap == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    public void close() {
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
