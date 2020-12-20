package com.wst.smarthome.ui.auto;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.wst.smarthome.HttpCRUD;
import com.wst.smarthome.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AutoFragment extends Fragment {

    private EditText editTextCelsiusOn, editTextCelsiusOff, editTextHumidityOn, editTextHumidityOff, editTextPM2_5_On, editTextPM2_5_Off;
    private Button buttonOk, buttonClear, buttonRead;
    private Switch switchAuto;
    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.button_clear:
                    editTextCelsiusOn.setText("");
                    editTextCelsiusOff.setText("");
                    editTextHumidityOn.setText("");
                    editTextHumidityOff.setText("");
                    editTextPM2_5_On.setText("");
                    editTextPM2_5_Off.setText("");
                    break;
                case R.id.button_ok:
                    updateTempParam = new StringBuilder();
                    if (switchAuto.isChecked())
                        updateTempParam.append("Auto=Y");
                    else
                        updateTempParam.append("Auto=N");
                    String celsius_On = editTextCelsiusOn.getText().toString();
                    updateTempParam.append("&Celsius_On=").append(celsius_On.isEmpty() ? "null" : celsius_On);
                    String celsius_Off = editTextCelsiusOff.getText().toString();
                    updateTempParam.append("&Celsius_Off=").append(celsius_Off.isEmpty() ? "null" : celsius_Off);
                    String humidity_On = editTextHumidityOn.getText().toString();
                    updateTempParam.append("&Humidity_On=").append(humidity_On.isEmpty() ? "null" : humidity_On);
                    String humidity_Off = editTextHumidityOff.getText().toString();
                    updateTempParam.append("&Humidity_Off=").append(humidity_Off.isEmpty() ? "null" : humidity_Off);
                    String pm2_5_On = editTextPM2_5_On.getText().toString();
                    updateTempParam.append("&PM2_5_On=").append(pm2_5_On.isEmpty() ? "null" : pm2_5_On);
                    String pm2_5_Off = editTextPM2_5_Off.getText().toString();
                    updateTempParam.append("&PM2_5_Off=").append(pm2_5_Off.isEmpty() ? "null" : pm2_5_Off);

                    new HttpCRUD(HttpCRUD.UPDATE_SETTINGS, updateTempParam.toString()){
                        @Override
                        public void run() {
                            super.run();
                            if (dataString.contains("UPDATE_settings_OK")) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast toast = Toast.makeText(getContext(), "設定自動控制成功", Toast.LENGTH_LONG);
                                        toast.setGravity(Gravity.CENTER, 0, 0);
                                        toast.show();
                                    }
                                });
                            }else {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast toast = Toast.makeText(getContext(), "設定自動控制失敗", Toast.LENGTH_LONG);
                                        toast.setGravity(Gravity.CENTER, 0, 0);
                                        toast.show();
                                    }
                                });
                            }
                        }
                    }.start();
                    break;
                case R.id.button_read:
                    new ShowSettings(HttpCRUD.SHOW_SETTINGS, "").start();
                    break;
                case R.id.switch_auto:
//                    if (switchAuto.isChecked())
//                        switchAuto.setText("開");
//                    else
//                        switchAuto.setText("關");
                    break;
            }
        }
    };
    private StringBuilder updateTempParam;
    private ImageButton buttonConAdd, buttonConMinus, buttonCoffAdd, buttonCoffMinus, buttonHonAdd, buttonHonMinus, buttonHoffAdd, buttonHoffMinus, buttonPonAdd, buttonPonMinus, buttonPoffAdd, buttonPoffMinus;
    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        private ScheduledExecutorService scheduledExecutor;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                updateAddOrSubtract(v.getId());    //手指按下時觸發不停的發送消息
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                stopAddOrSubtract();    //手指抬起時停止發送
            }
            return true;

        }

        private void updateAddOrSubtract(int viewId) {
            final int vid = viewId;
            scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
            scheduledExecutor.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    Message msg = new Message();
                    msg.what = vid;
                    handler.sendMessage(msg);
                }
            }, 0, 200, TimeUnit.MILLISECONDS);    //每間隔200ms發送Message
        }

        private Handler handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                int viewId = msg.what;
                switch (viewId){
                    case R.id.button_c_On_add:
                        buttonAddMinus(editTextCelsiusOn,"1",1);
                        break;
                    case R.id.button_c_On_minus:
                        buttonAddMinus(editTextCelsiusOn,"0",-1);
                        break;
                    case R.id.button_c_Off_add:
                        buttonAddMinus(editTextCelsiusOff,"1",1);
                        break;
                    case R.id.button_c_Off_minus:
                        buttonAddMinus(editTextCelsiusOff,"0",-1);
                        break;
                    case R.id.button_h_On_add:
                        buttonAddMinus(editTextHumidityOn,"1",1);
                        break;
                    case R.id.button_h_On_minus:
                        buttonAddMinus(editTextHumidityOn,"0",-1);
                        break;
                    case R.id.button_h_Off_add:
                        buttonAddMinus(editTextHumidityOff,"1",1);
                        break;
                    case R.id.button_h_Off_minus:
                        buttonAddMinus(editTextHumidityOff,"0",-1);
                        break;
                    case R.id.button_p_On_add:
                        buttonAddMinus(editTextPM2_5_On,"1",1);
                        break;
                    case R.id.button_p_On_minus:
                        buttonAddMinus(editTextPM2_5_On,"0",-1);
                        break;
                    case R.id.button_p_Off_add:
                        buttonAddMinus(editTextPM2_5_Off,"1",1);
                        break;
                    case R.id.button_p_Off_minus:
                        buttonAddMinus(editTextPM2_5_Off,"0",-1);
                        break;
                }
            }
        };

        private void stopAddOrSubtract() {
            if (scheduledExecutor != null) {
                scheduledExecutor.shutdownNow();
                scheduledExecutor = null;
            }
        }

        private void buttonAddMinus(EditText editText,String initText,int addMinus) {
            if (editText.length() == 0)
                editText.setText(initText);
            else {
                float humidity = Float.parseFloat(editText.getText().toString());
                editText.setText((humidity+addMinus) + "");
                editText.setSelection(editText.getText().length());
            }
        }
    };

    private static class DecimalTextWatcher implements TextWatcher {
        private boolean deleteLastChar;//是否需要刪除末尾
        private EditText editText;
        private Integer max = null;
        private Integer min = null;

        public DecimalTextWatcher(EditText editText) {
            this.editText = editText;
        }

        public DecimalTextWatcher(EditText editText, Integer max, Integer min) {
            this.editText = editText;
            this.max = max;
            this.min = min;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!s.toString().isEmpty()) {
                if (s.toString().equals(".")) {
                    //將.轉換成0.
                    editText.setText("0.");
                    //游標強制到末尾
                    editText.setSelection(editText.getText().length());
                }

                Pattern p = Pattern.compile("^(0\\d)$");
                Matcher m = p.matcher(s.toString());
                if (m.find()) {
                    //將00~09去掉前面的0
                    editText.setText(s.toString().substring(1, 2));
                    //游標強制到末尾
                    editText.setSelection(editText.getText().length());
                }

                try {
                    double value = Double.parseDouble(s.toString());
                    if (max != null) {
                        if (value > max) {
                            editText.setText(max + "");
                            //游標強制到末尾
                            editText.setSelection(editText.getText().length());
                        }
                    }
                    if (min != null) {
                        if (value < min) {//設定最小值
                            editText.setText(min+"");
                            //游標強制到末尾
                            editText.setSelection(editText.getText().length());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (s.toString().contains(".")) {
                //如果點後面有超過2位數值,則刪掉最後一位
                int length = s.length() - s.toString().lastIndexOf(".");
                if (length >= 3) {//說明後面有2位數值
                    deleteLastChar = true;
                } else {
                    deleteLastChar = false;
                }
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (deleteLastChar) {
                //設定新的擷取的字串
                editText.setText(s.toString().substring(0, s.toString().length() - 1));
                //游標強制到末尾
                editText.setSelection(editText.getText().length());
            }
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_auto, container, false);

        findViews(root);
        setListeners();

        new ShowSettings(HttpCRUD.SHOW_SETTINGS, "").start();

        return root;
    }

    private void findViews(View root) {
        editTextCelsiusOn = (EditText) root.findViewById(R.id.editText_celsiusOn);
        editTextCelsiusOff = (EditText) root.findViewById(R.id.editText_celsiusOff);
        editTextHumidityOn = (EditText) root.findViewById(R.id.editText_humidityOn);
        editTextHumidityOff = (EditText) root.findViewById(R.id.editText_humidityOff);
        editTextPM2_5_On = (EditText) root.findViewById(R.id.editText_PM2_5_On);
        editTextPM2_5_Off = (EditText) root.findViewById(R.id.editText_PM2_5_Off);

        buttonOk = (Button) root.findViewById(R.id.button_ok);
        buttonClear = (Button) root.findViewById(R.id.button_clear);
        buttonRead = (Button) root.findViewById(R.id.button_read);

        switchAuto = (Switch) root.findViewById(R.id.switch_auto);

        buttonConAdd = (ImageButton) root.findViewById(R.id.button_c_On_add);
        buttonConMinus = (ImageButton) root.findViewById(R.id.button_c_On_minus);
        buttonCoffAdd = (ImageButton) root.findViewById(R.id.button_c_Off_add);
        buttonCoffMinus = (ImageButton) root.findViewById(R.id.button_c_Off_minus);
        buttonHonAdd = (ImageButton) root.findViewById(R.id.button_h_On_add);
        buttonHonMinus = (ImageButton) root.findViewById(R.id.button_h_On_minus);
        buttonHoffAdd = (ImageButton) root.findViewById(R.id.button_h_Off_add);
        buttonHoffMinus = (ImageButton) root.findViewById(R.id.button_h_Off_minus);
        buttonPonAdd = (ImageButton) root.findViewById(R.id.button_p_On_add);
        buttonPonMinus = (ImageButton) root.findViewById(R.id.button_p_On_minus);
        buttonPoffAdd = (ImageButton) root.findViewById(R.id.button_p_Off_add);
        buttonPoffMinus = (ImageButton) root.findViewById(R.id.button_p_Off_minus);
    }

    private void setListeners() {
        //限制只能輸入2位小數
        editTextCelsiusOn.addTextChangedListener(new DecimalTextWatcher(editTextCelsiusOn));
        editTextCelsiusOff.addTextChangedListener(new DecimalTextWatcher(editTextCelsiusOff));
        editTextPM2_5_On.addTextChangedListener(new DecimalTextWatcher(editTextPM2_5_On, null, 0));
        editTextPM2_5_Off.addTextChangedListener(new DecimalTextWatcher(editTextPM2_5_Off, null, 0));
        //限制只能輸入2位小數+最大值為100
        editTextHumidityOn.addTextChangedListener(new DecimalTextWatcher(editTextHumidityOn, 100, 0));
        editTextHumidityOff.addTextChangedListener(new DecimalTextWatcher(editTextHumidityOff, 100, 0));

        buttonClear.setOnClickListener(listener);
        buttonOk.setOnClickListener(listener);
        buttonRead.setOnClickListener(listener);

        switchAuto.setOnClickListener(listener);

        buttonConAdd.setOnTouchListener(touchListener);
        buttonConMinus.setOnTouchListener(touchListener);
        buttonCoffAdd.setOnTouchListener(touchListener);
        buttonCoffMinus.setOnTouchListener(touchListener);
        buttonHonAdd.setOnTouchListener(touchListener);
        buttonHonMinus.setOnTouchListener(touchListener);
        buttonHoffAdd.setOnTouchListener(touchListener);
        buttonHoffMinus.setOnTouchListener(touchListener);
        buttonPonAdd.setOnTouchListener(touchListener);
        buttonPonMinus.setOnTouchListener(touchListener);
        buttonPoffAdd.setOnTouchListener(touchListener);
        buttonPoffMinus.setOnTouchListener(touchListener);
    }

    private class ShowSettings extends HttpCRUD {
        public ShowSettings(int subUrl_index, String param) {
            super(subUrl_index, param);
        }

        @Override
        public void run() {
            super.run();

            int length = 0;
            JSONArray jsonArray = null;

            if (dataString.length() == 0)
                return;

            try {
                //將網頁回傳資料轉成JSON陣列
                jsonArray = new JSONArray(dataString);
                length = jsonArray.length();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            for (int i = 0; i < length; i++) {
                JSONObject jsonObj = null;
                try {
                    //從JSON陣列的第i個元素來取得JSON物件
                    jsonObj = jsonArray.getJSONObject(i);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    //從JSON物件中取得key對應的值
                    String item = jsonObj.getString("item");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    String auto = jsonObj.getString("auto");
                    if (auto.equals("Y")) {
                        switchAuto.post(new Runnable() {
                            @Override
                            public void run() {
                                switchAuto.setChecked(true);
//                                switchAuto.setText("開");
                            }
                        });
                    } else {
                        switchAuto.post(new Runnable() {
                            @Override
                            public void run() {
                                switchAuto.setChecked(false);
//                                switchAuto.setText("關");
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    String celsius_On = jsonObj.getString("celsius_On");
                    editTextCelsiusOn.post(new Runnable() {
                        @Override
                        public void run() {
                            editTextCelsiusOn.setText(celsius_On);
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                    editTextCelsiusOn.post(new Runnable() {
                        @Override
                        public void run() {
                            editTextCelsiusOn.setText("");
                        }
                    });
                }
                try {
                    String celsius_Off = jsonObj.getString("celsius_Off");
                    editTextCelsiusOff.post(new Runnable() {
                        @Override
                        public void run() {
                            editTextCelsiusOff.setText(celsius_Off);
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                    editTextCelsiusOff.post(new Runnable() {
                        @Override
                        public void run() {
                            editTextCelsiusOff.setText("");
                        }
                    });
                }
                try {
                    String humidity_On = jsonObj.getString("humidity_On");
                    editTextHumidityOn.post(new Runnable() {
                        @Override
                        public void run() {
                            editTextHumidityOn.setText(humidity_On);
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                    editTextHumidityOn.post(new Runnable() {
                        @Override
                        public void run() {
                            editTextHumidityOn.setText("");
                        }
                    });
                }
                try {
                    String humidity_Off = jsonObj.getString("humidity_Off");
                    editTextHumidityOff.post(new Runnable() {
                        @Override
                        public void run() {
                            editTextHumidityOff.setText(humidity_Off);
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                    editTextHumidityOff.post(new Runnable() {
                        @Override
                        public void run() {
                            editTextHumidityOff.setText("");
                        }
                    });
                }
                try {
                    String pm2_5_On = jsonObj.getString("pm2_5_On");
                    editTextPM2_5_On.post(new Runnable() {
                        @Override
                        public void run() {
                            editTextPM2_5_On.setText(pm2_5_On);
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                    editTextPM2_5_On.post(new Runnable() {
                        @Override
                        public void run() {
                            editTextPM2_5_On.setText("");
                        }
                    });
                }
                try {
                    String pm2_5_Off = jsonObj.getString("pm2_5_Off");
                    editTextPM2_5_Off.post(new Runnable() {
                        @Override
                        public void run() {
                            editTextPM2_5_Off.setText(pm2_5_Off);
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                    editTextPM2_5_Off.post(new Runnable() {
                        @Override
                        public void run() {
                            editTextPM2_5_Off.setText("");
                        }
                    });
                }
            }
        }
    }
}