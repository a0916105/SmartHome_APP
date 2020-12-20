package com.wst.smarthome.ui.home;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.wst.smarthome.HttpCRUD;
import com.wst.smarthome.R;

import java.util.Calendar;

public class EditScheduleActivity extends AppCompatActivity {

    private final static int AC = 0, DH = 1, AP = 2, UPDATE = -1;
    private int add;
    private Intent intent;
    private static final int RESULT_CODE = 200;
    private String id, schedule, devices, date = "", time = "", repeat = "", onOff;
    private Context context;
    private Spinner spinnerDevices;
    private TextView textViewDate, textViewTime, textViewRepeat;
    private Switch switchSchedule, switchDevice;
    private final CompoundButton.OnCheckedChangeListener ccListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()) {
                case R.id.switch_schedule:
//                    switchSchedule.setText(isChecked ? "啟用" : "關閉");
                    schedule = isChecked ? "enable" : "disable";
                    break;
                case R.id.switch_device:
                    switchDevice.setText(isChecked ? "開" : "關");
                    onOff = isChecked ? "On" : "Off";
                    break;
            }
        }
    };
    private Button buttonDatePicker, buttonTimePicker, buttonRepeat, buttonOk, buttonCancel, buttonClear;
    private final View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.button_datePicker:
                    Calendar calendar = Calendar.getInstance();
                    new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                            date = year + "-" + ((month + 1) < 10 ? "0" : "") + (month + 1) + "-" + (dayOfMonth < 10 ? "0" : "") + dayOfMonth;
                            textViewDate.setText(date);
                            repeat = "";
                            textViewRepeat.setText(repeat);
                        }
                    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
                    break;
                case R.id.button_timePicker:
                    Calendar c = Calendar.getInstance();
                    new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            time = (hourOfDay < 10 ? "0" : "") + hourOfDay + ":" + (minute < 10 ? "0" : "") + minute;
                            textViewTime.setText(time);
                        }
                    }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false).show();
                    break;
                case R.id.button_repeat:
                    showDialog_checkbox();
                    break;
                case R.id.button_ok:
                    String date = textViewDate.getText().toString();
                    String time = textViewTime.getText().toString();

                    if (time.length() != 0 && (date.length() != 0 ^ repeat.length() != 0)) {
                        StringBuilder tempParam = new StringBuilder();
                        if (switchSchedule.isChecked())
                            tempParam.append("schedule=enable");
                        else
                            tempParam.append("schedule=disable");

                        tempParam.append("&devices=").append(devices);

                        if (date.length() != 0)
                            tempParam.append("&Date=").append(date);

                        if (repeat.length() != 0)
                            tempParam.append("&weekday=").append(repeat);

                        tempParam.append("&Time=").append(time);

                        if (switchDevice.isChecked())
                            tempParam.append("&switch=On");
                        else
                            tempParam.append("&switch=Off");

                        if (add != UPDATE) {
                            new HttpCRUD(HttpCRUD.INSERT_SCHEDULE, tempParam.toString()) {
                                @Override
                                public void run() {
                                    super.run();
                                    buttonOk.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            intent.putExtra("devices", devices);
                                            setResult(RESULT_CODE, intent);
                                            finish();
                                        }
                                    });
                                }
                            }.start();
                        } else {
                            tempParam.append("&id=" + id);

                            new HttpCRUD(HttpCRUD.UPDATE_SCHEDULE, tempParam.toString()) {
                                @Override
                                public void run() {
                                    super.run();
                                    buttonOk.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            intent.putExtra("devices", devices);
                                            setResult(RESULT_CODE, intent);
                                            finish();
                                        }
                                    });
                                }
                            }.start();
                        }
                    } else if (time.isEmpty()) {
                        Toast.makeText(context, "請指定時間", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "請指定日期或重複情形", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.button_clear:
                    textViewDate.setText("");
                    textViewTime.setText("");
                    textViewRepeat.setText("");
                    date = "";
                    time = "";
                    repeat = "";
                    break;
                case R.id.button_cancel:
                    finish();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_schedule);

        context = this;
        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);

        findViews();
        //使用在res資料夾的內容來設定Spinner顯示的內容和layout
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(context, R.array.device, R.layout.simple_spinner_item);
        //設定Spinner的下拉形式
        spinnerAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        spinnerDevices.setAdapter(spinnerAdapter);
        setListeners();

        intent = getIntent();
        add = intent.getIntExtra("add", -2);
        Log.d("Intent", "add=" + add);
        if (add != UPDATE) {
            scheduleSetTitle("新增", add);
            spinnerDevices.setSelection(add);
        } else {
            id = intent.getStringExtra("id");
            schedule = intent.getStringExtra("schedule");
            devices = intent.getStringExtra("devices");
            date = intent.getStringExtra("date");
            repeat = intent.getStringExtra("weekday");
            time = intent.getStringExtra("time");
            onOff = intent.getStringExtra("onOff");
            switch (devices) {
                case "0":
                    setTitle("修改 冷氣機 排程");
                    break;
                case "1":
                    setTitle("修改 除濕機 排程");
                    break;
                case "2":
                    setTitle("修改 空氣清淨機 排程");
                    break;
            }
            spinnerDevices.setSelection(Integer.parseInt(devices));
            switchSchedule.setChecked(schedule.equals("enable"));
            textViewDate.setText(date);
            textViewTime.setText(time);
            String repeatString;
            if (repeat.equals("Only")) {
                repeatString = "僅只一次";
            }else if (repeat.equals("Sun,Mon,Tue,Wed,Thu,Fri,Sat")){
                repeatString = "每天";
            }else if (repeat.equals("Mon,Tue,Wed,Thu,Fri")){
                repeatString = "工作日";
            }else if (repeat.equals("Sun,Sat")){
                repeatString = "週末";
            }else {
                repeatString = repeat.replace("Sun", "週日").replace("Mon", "週一")
                        .replace("Tue", "週二").replace("Wed", "週三")
                        .replace("Thu", "週四").replace("Fri", "週五")
                        .replace("Sat", "週六");
            }
            textViewRepeat.setText(repeatString);
            switchDevice.setChecked(onOff.equals("On"));
        }
    }

    private void findViews() {
        spinnerDevices = (Spinner) findViewById(R.id.spinner_devices);
        textViewDate = (TextView) findViewById(R.id.textView_date);
        textViewTime = (TextView) findViewById(R.id.textView_time);
        textViewRepeat = (TextView) findViewById(R.id.textView_repeat);

        switchSchedule = (Switch) findViewById(R.id.switch_schedule);
        switchDevice = (Switch) findViewById(R.id.switch_device);

        buttonDatePicker = (Button) findViewById(R.id.button_datePicker);
        buttonTimePicker = (Button) findViewById(R.id.button_timePicker);
        buttonRepeat = (Button) findViewById(R.id.button_repeat);
        buttonOk = (Button) findViewById(R.id.button_ok);
        buttonCancel = (Button) findViewById(R.id.button_cancel);
        buttonClear = (Button) findViewById(R.id.button_clear);
    }

    private void setListeners() {
        spinnerDevices.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d("spinnerDevices", "position=" + position);
                scheduleSetTitle(add != UPDATE ? "新增" : "修改", position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        switchSchedule.setOnCheckedChangeListener(ccListener);
        switchDevice.setOnCheckedChangeListener(ccListener);

        buttonDatePicker.setOnClickListener(clickListener);
        buttonTimePicker.setOnClickListener(clickListener);
        buttonRepeat.setOnClickListener(clickListener);
        buttonOk.setOnClickListener(clickListener);
        buttonCancel.setOnClickListener(clickListener);
        buttonClear.setOnClickListener(clickListener);
    }

    private void scheduleSetTitle(String edit, int device) {
        switch (device) {
            case AC:
                setTitle(edit + " 冷氣機 排程");
                devices = "0";
                break;
            case DH:
                setTitle(edit + " 除濕機 排程");
                devices = "1";
                break;
            case AP:
                setTitle(edit + " 空氣清淨機 排程");
                devices = "2";
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDialog_checkbox() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        //載入自訂對話框
        View custom_dialog = getLayoutInflater().inflate(R.layout.dialogview_repeat, null);
        //設定顯示的對話框樣式
        builder.setView(custom_dialog);
        builder.setTitle("重複");
        String[] repeatList = getResources().getStringArray(R.array.repeat);
        int repeatLength = repeatList.length;
        boolean[] checkList = new boolean[repeatLength];
        //取得自訂對話框的元件
        RadioGroup radioGroupRepeat = custom_dialog.findViewById(R.id.radioGroup_repeat);
        RadioButton radioButtonOnly = custom_dialog.findViewById(R.id.radioButton_Only);
        RadioButton radioButtonEveryday = custom_dialog.findViewById(R.id.radioButton_everyday);
        RadioButton radioButtonWorkday = custom_dialog.findViewById(R.id.radioButton_workday);
        RadioButton radioButtonWeekend = custom_dialog.findViewById(R.id.radioButton_weekend);
        RadioButton radioButtonWeek = custom_dialog.findViewById(R.id.radioButton_week);
        CheckBox checkBoxSun = custom_dialog.findViewById(R.id.checkBox_Sun);
        CheckBox checkBoxMon = custom_dialog.findViewById(R.id.checkBox_Mon);
        CheckBox checkBoxTue = custom_dialog.findViewById(R.id.checkBox_Tue);
        CheckBox checkBoxWed = custom_dialog.findViewById(R.id.checkBox_Wed);
        CheckBox checkBoxThu = custom_dialog.findViewById(R.id.checkBox_Thu);
        CheckBox checkBoxFri = custom_dialog.findViewById(R.id.checkBox_Fri);
        CheckBox checkBoxSat = custom_dialog.findViewById(R.id.checkBox_Sat);
        CheckBox[] checkBoxWeekday = {checkBoxSun, checkBoxMon, checkBoxTue, checkBoxWed, checkBoxThu, checkBoxFri, checkBoxSat};
        //載入原本的設定
        for (int i = 0; i < 7; i++) {
            if (repeat.contains(repeatList[i])) {
                checkBoxWeekday[i].setChecked(true);
                checkList[i] = true;
            }
        }
        if (repeat.contains(repeatList[7])) {
            radioButtonOnly.setChecked(true);
            checkList[7] = true;
        }else if (repeat.equals("Sun,Mon,Tue,Wed,Thu,Fri,Sat")){
            radioButtonEveryday.setChecked(true);
        }else if (repeat.equals("Mon,Tue,Wed,Thu,Fri")){
            radioButtonWorkday.setChecked(true);
        }else if (repeat.equals("Sun,Sat")){
            radioButtonWeekend.setChecked(true);
        }else {
            radioButtonWeek.setChecked(true);
            for (int i = 0; i < 7; i++)
                checkBoxWeekday[i].setEnabled(true);
        }
        //監聽自訂對話框的元件
        radioGroupRepeat.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radioButton_Only:
//                        if (radioButtonOnly.isChecked()) {//避免清除時觸發
                            for (int i = 0; i < 7; i++)
                                checkBoxWeekday[i].setEnabled(false);
                            for (int i = 0; i < 7; i++) {
                                checkBoxWeekday[i].setChecked(false);
                                checkList[i] = false;
                            }
                            checkList[7] = true;
//                        }
                        break;
                    case R.id.radioButton_everyday:
                        if (radioButtonEveryday.isChecked()) {//避免清除時觸發
                            for (int i = 0; i < 7; i++)
                                checkBoxWeekday[i].setEnabled(false);
                            for (int i = 0; i < 7; i++) {
                                checkBoxWeekday[i].setChecked(true);
                                checkList[i] = true;
                            }
                            checkList[7] = false;
                        }
                        break;
                    case R.id.radioButton_workday:
                        if (radioButtonWorkday.isChecked()) {//避免清除時觸發
                            for (int i = 0; i < 7; i++)
                                checkBoxWeekday[i].setEnabled(false);
                            for (int i = 1; i <= 5; i++) {
                                checkBoxWeekday[i].setChecked(true);
                                checkList[i] = true;
                            }
                            checkBoxWeekday[6].setChecked(false);
                            checkList[6] = false;
                            checkBoxWeekday[0].setChecked(false);
                            checkList[0] = false;
                            checkList[7] = false;
                        }
                        break;
                    case R.id.radioButton_weekend:
                        if (radioButtonWeekend.isChecked()) {//避免清除時觸發
                            for (int i = 0; i < 7; i++)
                                checkBoxWeekday[i].setEnabled(false);
                            for (int i = 1; i <= 5; i++) {
                                checkBoxWeekday[i].setChecked(false);
                                checkList[i] = false;
                            }
                            checkBoxWeekday[6].setChecked(true);
                            checkList[6] = true;
                            checkBoxWeekday[0].setChecked(true);
                            checkList[0] = true;
                            checkList[7] = false;
                        }
                        break;
                    case R.id.radioButton_week:
                        if (radioButtonWeek.isChecked()) {//避免清除時觸發
                            for (int i = 0; i < 7; i++)
                                checkBoxWeekday[i].setEnabled(true);
                            checkList[7] = false;
                        }
                        break;
                }
            }
        });
        CompoundButton.OnCheckedChangeListener listener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                switch (buttonView.getId()) {
                    case R.id.checkBox_Sun:
                        checkList[0]=isChecked;
                        break;
                    case R.id.checkBox_Mon:
                        checkList[1]=isChecked;
                        break;
                    case R.id.checkBox_Tue:
                        checkList[2]=isChecked;
                        break;
                    case R.id.checkBox_Wed:
                        checkList[3]=isChecked;
                        break;
                    case R.id.checkBox_Thu:
                        checkList[4]=isChecked;
                        break;
                    case R.id.checkBox_Fri:
                        checkList[5]=isChecked;
                        break;
                    case R.id.checkBox_Sat:
                        checkList[6]=isChecked;
                        break;
                }
            }
        };
        checkBoxSun.setOnCheckedChangeListener(listener);
        checkBoxMon.setOnCheckedChangeListener(listener);
        checkBoxTue.setOnCheckedChangeListener(listener);
        checkBoxWed.setOnCheckedChangeListener(listener);
        checkBoxThu.setOnCheckedChangeListener(listener);
        checkBoxFri.setOnCheckedChangeListener(listener);
        checkBoxSat.setOnCheckedChangeListener(listener);

        builder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                StringBuilder sbData = new StringBuilder();
                for (int i = 0; i < repeatLength; i++) {
                    if (checkList[i]) {
                        sbData.append(sbData.length() > 0 ? "," : "").append(repeatList[i]);
                    }
                }
                repeat = sbData.toString();
                String repeatString = "";
                if (repeat.equals("Only")) {
                    repeatString = "僅只一次";
                }else if (repeat.equals("Sun,Mon,Tue,Wed,Thu,Fri,Sat")){
                    repeatString = "每天";
                }else if (repeat.equals("Mon,Tue,Wed,Thu,Fri")){
                    repeatString = "工作日";
                }else if (repeat.equals("Sun,Sat")){
                    repeatString = "週末";
                }else {
                    repeatString = repeat.replace("Sun", "週日").replace("Mon", "週一")
                            .replace("Tue", "週二").replace("Wed", "週三")
                            .replace("Thu", "週四").replace("Fri", "週五")
                            .replace("Sat", "週六");
                }
                textViewRepeat.setText(repeatString);
                if (repeat.length() != 0) {//解決什麼都沒選，也會清除"日期"的內容
                    date = "";
                    textViewDate.setText(date);
                }

                dialog.dismiss();
            }
        });

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setNeutralButton("清除", null);//避免按了之後，對話框消失

        final AlertDialog dialog = builder.create();  //創建對話框
        dialog.show();
        //另外寫對話框的NeutralButton監聽
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i=0;i<7;i++) {
//                    checkList[i] = false;//onCheckedChanged()會觸發，所以省略
                    checkBoxWeekday[i].setEnabled(false);
                    checkBoxWeekday[i].setChecked(false);
                }
                radioGroupRepeat.clearCheck();
            }
        });
    }
}