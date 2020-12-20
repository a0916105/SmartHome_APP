package com.wst.smarthome.ui.home;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.wst.smarthome.HttpCRUD;
import com.wst.smarthome.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScheduleActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_CODE = 100;
    private static final int RESULT_CODE = 200;
    private Context context;
    private ListView listViewSchedule;
    private LinearLayout mLlEditBar;//控制右方那一行的顯示與隱藏
    private ScheduleAdapter adapter;
    private List<Map<String, Object>> itemList;//所有數據
    private List<String> mCheckedID = new ArrayList<>();//將選中數據的ID放入裏面
    private SparseBooleanArray stateCheckedMap = new SparseBooleanArray();//用來存放CheckBox的選中狀態，true爲選中,false爲沒有選中
    private boolean isSelectedAll = true;//用來控制點擊全選，全選和全不選相互切換
    private int device=-2;
    private final static int AC=0,DH=1,AP=2,UPDATE=-1;
    private String deviceString;
    private Intent edit;
    private FloatingActionButton fabAdd;
    private ImageView imageViewSelectAll;
    private TextView textViewSelectAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        context = this;
        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        device = intent.getIntExtra("device",-2);
        scheduleSetTitle(device);

        findViews();
        mLlEditBar.setVisibility(View.GONE);//隱藏右方佈局
        listViewSchedule.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        itemList=new ArrayList<Map<String,Object>>();
        itemList.clear();
        new ShowSchedule(HttpCRUD.SHOW_SCHEDULE,"device="+device).start();

        edit = new Intent(context,EditScheduleActivity.class);

        setListeners();
    }

    private void scheduleSetTitle(int device) {
        switch (device){
            case AC:
                deviceString="冷氣機";
                break;
            case DH:
                deviceString="除濕機";
                break;
            case AP:
                deviceString="空氣清淨機";
                break;
        }
        setTitle(deviceString);
    }

    private void findViews() {
        mLlEditBar = (LinearLayout) findViewById(R.id.linearLayout_editBar);
        imageViewSelectAll=(ImageView) findViewById(R.id.imageView_selectAll);
        textViewSelectAll=(TextView)findViewById(R.id.textView_selectAll);
        listViewSchedule = (ListView) findViewById(R.id.listView_schedule);
        fabAdd=(FloatingActionButton)findViewById(R.id.fab_add);
    }

    private void setListeners() {
        listViewSchedule.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mLlEditBar.getVisibility() == View.VISIBLE)
                    cancel();

                Map<String,Object> item= (Map<String, Object>) parent.getItemAtPosition(position);
                String _id = (String) item.get("id");
                String schedule = (String) item.get("schedule");
                String devices = (String) item.get("devices");
                String date = (String) item.get("date");
                String weekday = (String) item.get("weekday");
                String time = (String) item.get("time");
                String onOff = (String) item.get("onOff");
                edit.putExtra("id",_id);
                edit.putExtra("schedule",schedule);
                edit.putExtra("devices",devices);
                edit.putExtra("date",date);
                edit.putExtra("weekday",weekday);
                edit.putExtra("time",time);
                edit.putExtra("onOff",onOff);
                edit.putExtra("add",UPDATE);
                startActivityForResult(edit,REQUEST_CODE);
            }
        });

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLlEditBar.getVisibility() == View.VISIBLE)
                    cancel();
                edit.putExtra("add",device);
                startActivityForResult(edit,REQUEST_CODE);
            }
        });

        findViewById(R.id.imageView_cancel).setOnClickListener(this);
        imageViewSelectAll.setOnClickListener(this);
        findViewById(R.id.imageView_selectInvert).setOnClickListener(this);
        findViewById(R.id.imageView_delete).setOnClickListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==REQUEST_CODE){
            switch (resultCode){
                case RESULT_CODE:
                    device = Integer.parseInt(data.getStringExtra("devices"));
                    scheduleSetTitle(device);
                    break;
            }
        }
        itemList.clear();
        new ShowSchedule(HttpCRUD.SHOW_SCHEDULE,"device="+device).start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageView_cancel:
                cancel();
                break;
            case R.id.imageView_selectAll:
                selectAll();
                break;
            case R.id.imageView_selectInvert:
                inverse();
                break;
            case R.id.imageView_delete:
                delete();
                break;
        }
    }

    private void cancel() {
        setStateCheckedMap(false);//將CheckBox的所有選中狀態變成未選中
        mLlEditBar.setVisibility(View.GONE);//隱藏右方佈局
        adapter.setShowCheckBox(false);//讓CheckBox那個方框隱藏
        adapter.notifyDataSetChanged();//更新ListView
    }

    private void delete() {
        if (mCheckedID.size() == 0) {
            Toast.makeText(context, "您還沒有選中任何數據！", Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("刪除 "+deviceString+" 排程")
                .setMessage("\n是否要刪除?")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("刪除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        beSureDelete();
                        dialog.dismiss();
                    }
                }).create().show();
    }

    private void beSureDelete() {
        for (int i=0;i<mCheckedID.size();i++) {
            String id = mCheckedID.get(i);
            String param = "id=" + id;
            new HttpCRUD(HttpCRUD.DELETE_SCHEDULE, param).start();
        }
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        setStateCheckedMap(false);//將CheckBox的所有選中狀態變成未選中
        mCheckedID.clear();//清空選中數據
        itemList.clear();
        new ShowSchedule(HttpCRUD.SHOW_SCHEDULE,"device="+device){
            @Override
            public void run() {
                super.run();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.setShowCheckBox(true);//CheckBox的那個方框顯示
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }.start();
        Toast.makeText(context, "刪除成功", Toast.LENGTH_SHORT).show();
    }
    /**
     * 反選就是stateCheckedMap的值爲true時變爲false,false時變成true
     * */
    private void inverse() {
        mCheckedID.clear();
        int countTrue=0;
        for (int i = 0; i < itemList.size(); i++) {
            if (stateCheckedMap.get(i)) {
                stateCheckedMap.put(i, false);
            } else {
                stateCheckedMap.put(i, true);
                mCheckedID.add((String) itemList.get(i).get("id"));
                countTrue++;
            }
            listViewSchedule.setItemChecked(i, stateCheckedMap.get(i));//這個好行可以控制ListView複用的問題，不設置這個會出現點擊一個會選中多個
        }
        if (countTrue==itemList.size()) {
            imageViewSelectAll.setImageResource(R.drawable.uncheck_all);
            textViewSelectAll.setText("全不選");
            isSelectedAll = false;
        } else if (countTrue==0){
            imageViewSelectAll.setImageResource(R.drawable.check_all);
            textViewSelectAll.setText("全選");
            isSelectedAll = true;
        }
        adapter.notifyDataSetChanged();
    }

    private void selectAll() {
        mCheckedID.clear();//清空之前選中數據
        if (isSelectedAll) {
            setStateCheckedMap(true);//將CheckBox的所有選中狀態變成選中
            imageViewSelectAll.setImageResource(R.drawable.uncheck_all);
            textViewSelectAll.setText("全不選");
            isSelectedAll = false;
            for (int i=0;i<itemList.size();i++) {//把所有的數據添加到選中列表中
                mCheckedID.add((String) itemList.get(i).get("id"));
            }
        } else {
            setStateCheckedMap(false);//將CheckBox的所有選中狀態變成未選中
            imageViewSelectAll.setImageResource(R.drawable.check_all);
            textViewSelectAll.setText("全選");
            isSelectedAll = true;
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_schedule, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (mLlEditBar.getVisibility() == View.VISIBLE)
                    cancel();
                finish();
                break;
            case R.id.item_delete:
                mLlEditBar.setVisibility(View.VISIBLE);//顯示右方佈局
                adapter.setShowCheckBox(true);//CheckBox的那個方框顯示
                adapter.notifyDataSetChanged();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 設置所有CheckBox的選中狀態
     */
    private void setStateCheckedMap(boolean isSelectedAll) {
        for (int i = 0; i < itemList.size(); i++) {
            stateCheckedMap.put(i, isSelectedAll);
            listViewSchedule.setItemChecked(i, isSelectedAll);
        }
    }

    @Override
    public void onBackPressed() {
        if (mLlEditBar.getVisibility() == View.VISIBLE) {
            cancel();
            return;
        }
        super.onBackPressed();
    }

    private class ShowSchedule extends HttpCRUD {
        public ShowSchedule(int subUrl_index, String param) {
            super(subUrl_index, param);
        }

        @Override
        public void run() {
            super.run();

            //在Thread控制UI
            runOnUiThread(new Runnable() {
                private int length;
                private JSONArray jsonArray;

                @Override
                public void run() {
                    if (dataString.length()==0)
                        return;

                    try {
                        //將網頁回傳資料轉成JSON陣列
                        jsonArray = new JSONArray(dataString);
                        length =jsonArray.length();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    for (int i=0;i<length;i++){
                        Map<String,Object> data=new HashMap<>();
                        JSONObject jsonObj = null;
                        try {
                            //從JSON陣列的第i個元素來取得JSON物件
                            jsonObj=jsonArray.getJSONObject(i);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        try {
                            //從JSON物件中取得key對應的值
                            String id=jsonObj.getString("id");
                            data.put("id",id);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        try {
                            String schedule=jsonObj.getString("schedule");
                            data.put("schedule",schedule);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        try {
                            String devices=jsonObj.getString("devices");
                            data.put("devices",devices);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        try {
                            String date=jsonObj.getString("date");
                            data.put("date",date);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            data.put("date","");
                        }
                        try {
                            String weekday=jsonObj.getString("weekday");
                            data.put("weekday",weekday);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            data.put("weekday","");
                        }
                        try {
                            String time=jsonObj.getString("time");
                            data.put("time",time.substring(0,5));
                        } catch (JSONException e) {
                            e.printStackTrace();
                            data.put("time","");
                        }
                        try {
                            String onOff=jsonObj.getString("onOff");
                            data.put("onOff",onOff);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        itemList.add(data);
                    }
                    adapter = new ScheduleAdapter(context,itemList,stateCheckedMap,mCheckedID);
                    listViewSchedule.setAdapter(adapter);
                }
            });
        }
    }
}