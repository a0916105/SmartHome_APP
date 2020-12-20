package com.wst.smarthome.ui.bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.wst.smarthome.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.UUID;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class BluetoothFragment extends Fragment {

    private Context context;
    private Button buttonConnect,buttonDisconnect,buttonOffBT,buttonSend,buttonClear,buttonRead,buttonErase,buttonDisWiFi,buttonReboot;
    private EditText editTextSSID,editTextPassword;
    private ImageView imageViewLock;
    private Switch switchAC,switchDH,switchAP;
    View.OnClickListener myCListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.imageView_lock:
                    pwdShow(editTextPassword,imageViewLock);
                    break;
                case R.id.button_connect:
                    btDeviceConnect();
                    break;
                case R.id.button_disconnect:
                    if (connectedThread != null) {
                        connectedThread.cancel();
                        Toast.makeText(context, "Disconnected to Device: " + name,
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.button_offBT:
                    bluetoothOff();
                    break;
                case R.id.button_send:
                    //First check to make sure thread created
                    if(connectedThread != null) {
                        String ssid = editTextSSID.getText().toString();
                        String pass = editTextPassword.getText().toString();
                        //傳送將輸入的資料出去
                        connectedThread.write("{\"ssid\":\"" +ssid+ "\",\"pass\":\""+pass+"\"}");
                    }
                    break;
                case R.id.button_read:
                    //First check to make sure thread created
                    if(connectedThread != null) {
                        btRead="read";
                        connectedThread.write("{\"read\":true}");
                    }
                    break;
                case R.id.button_clear:
                    editTextSSID.setText("");
                    editTextPassword.setText("");
                    editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    imageViewLock.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_lock_idle_lock));
                    break;
                case R.id.button_erase:
                    //First check to make sure thread created
                    if(connectedThread != null) {
                        connectedThread.write("{\"erase\":true}");
                        connectedThread.cancel();
                        Toast.makeText(context, "Disconnected to Device: " + name,
                                Toast.LENGTH_SHORT).show();
                        buttonConnect.setEnabled(true);
                        buttonDisconnect.setEnabled(false);
                        editTextSSID.setText("");
                        editTextPassword.setText("");
                        editTextSSID.setEnabled(false);
                        editTextPassword.setEnabled(false);
                        imageViewLock.setEnabled(false);
                        buttonSend.setEnabled(false);
                        buttonClear.setEnabled(false);
                        buttonErase.setEnabled(false);
                        buttonRead.setEnabled(false);
                        buttonDisWiFi.setEnabled(false);
                        buttonReboot.setEnabled(false);
                        switchAC.setEnabled(false);
                        switchDH.setEnabled(false);
                        switchAP.setEnabled(false);
                    }
                    break;
                case R.id.button_disWiFi:
                    //First check to make sure thread created
                    if(connectedThread != null)
                        connectedThread.write("{\"connect\":false}");
                    break;
                case R.id.button_reboot:
                    //First check to make sure thread created
                    if(connectedThread != null) {
                        connectedThread.write("{\"reboot\":true}");
                        connectedThread.cancel();
                        Toast.makeText(context, "Disconnected to Device: " + name,
                                Toast.LENGTH_SHORT).show();
                        buttonConnect.setEnabled(true);
                        buttonDisconnect.setEnabled(false);
                        editTextSSID.setText("");
                        editTextPassword.setText("");
                        editTextSSID.setEnabled(false);
                        editTextPassword.setEnabled(false);
                        imageViewLock.setEnabled(false);
                        buttonSend.setEnabled(false);
                        buttonClear.setEnabled(false);
                        buttonErase.setEnabled(false);
                        buttonRead.setEnabled(false);
                        buttonDisWiFi.setEnabled(false);
                        buttonReboot.setEnabled(false);
                        switchAC.setEnabled(false);
                        switchDH.setEnabled(false);
                        switchAP.setEnabled(false);
                    }
                    break;
                case R.id.switch_AC:
                    if (switchAC.isChecked()) {
                        switchAC.setChecked(!switchAC.isChecked());
                        //First check to make sure thread created
                        if (connectedThread != null)
                            connectedThread.write("{\"AC\":true}");
                    }else {
                        switchAC.setChecked(!switchAC.isChecked());
                        //First check to make sure thread created
                        if (connectedThread != null)
                            connectedThread.write("{\"AC\":false}");
                    }
                    Toast.makeText(context, "Disable Auto Control", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.switch_DH:
                    if (switchDH.isChecked()) {
                        switchDH.setChecked(!switchDH.isChecked());
                        //First check to make sure thread created
                        if (connectedThread != null)
                            connectedThread.write("{\"DH\":true}");
                    }else {
                        switchDH.setChecked(!switchDH.isChecked());
                        //First check to make sure thread created
                        if (connectedThread != null)
                            connectedThread.write("{\"DH\":false}");
                    }
                    Toast.makeText(context, "Disable Auto Control", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.switch_AP:
                    if (switchAP.isChecked()) {
                        switchAP.setChecked(!switchAP.isChecked());
                        //First check to make sure thread created
                        if (connectedThread != null)
                            connectedThread.write("{\"AP\":true}");
                    }else {
                        switchAP.setChecked(!switchAP.isChecked());
                        //First check to make sure thread created
                        if (connectedThread != null)
                            connectedThread.write("{\"AP\":false}");
                    }
                    Toast.makeText(context, "Disable Auto Control", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private BluetoothAdapter btAdapter;

    // Our main handler that will receive callback notifications
    private Handler handler;
    // bluetooth background worker thread to send and receive data
    private ConnectedThread connectedThread;
    // bi-directional client-to-client data path
    private BluetoothSocket btSocket = null;

    private static final UUID BTMODULEUUID = UUID.fromString
            ("00001101-0000-1000-8000-00805F9B34FB"); // 藍牙串列埠服務

    // #defines for identifying shared types between calling functions
    // used to identify adding bluetooth names
    private final static int REQUEST_ENABLE_BT = 1;
    private final static int REQUEST_DeviceScan = 4;
    // used in bluetooth handler to identify message update
    private final static int MESSAGE_READ = 2;
    private final static int Message_NG = 3;
    private final static int Message_OK = 5;
    private final int RETURN_DATA=40;
    private String receiveData = "";
    private String name;
    private String btRead="";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_bluetooth, container, false);

        context = getContext();

        findViews(root);

        buttonDisconnect.setEnabled(false);

        editTextSSID.setEnabled(false);
        editTextPassword.setEnabled(false);
        imageViewLock.setEnabled(false);

        buttonSend.setEnabled(false);
        buttonClear.setEnabled(false);
        buttonErase.setEnabled(false);
        buttonRead.setEnabled(false);
        buttonDisWiFi.setEnabled(false);
        buttonReboot.setEnabled(false);

        switchAC.setEnabled(false);
        switchDH.setEnabled(false);
        switchAP.setEnabled(false);

        setListeners();

        // get a handle on the bluetooth radio
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        // 詢問藍芽裝置權限
        if(ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(getActivity(), new String[]
                    {Manifest.permission.ACCESS_COARSE_LOCATION}, 1);

        //定義執行緒 當收到不同的指令做對應的內容
        handler = new Handler(Looper.getMainLooper(), msg -> {
            switch (msg.what){
                case MESSAGE_READ: //收到MESSAGE_READ 開始接收資料
                    receiveData = (String) msg.obj;
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(receiveData);
                        if (btRead.equals("read")) {
                            String json_ssid = jsonObject.getString("ssid");
                            String json_pass = jsonObject.getString("pass");
                            json_ssid = json_ssid.isEmpty()?"無資料":json_ssid;
                            json_pass = json_pass.isEmpty()?"無資料":json_pass;
                            editTextSSID.setText(json_ssid);
                            editTextPassword.setText(json_pass);
                            editTextPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                            imageViewLock.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_partial_secure));
                            btRead="";
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        boolean json_ac = jsonObject.getBoolean("AC");
                        switchAC.setChecked(json_ac);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        boolean json_dh = jsonObject.getBoolean("DH");
                        switchDH.setChecked(json_dh);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        boolean json_ap = jsonObject.getBoolean("AP");
                        switchAP.setChecked(json_ap);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    return true;
                case Message_NG:
                    Toast.makeText(context, "Socket creation failed",
                            Toast.LENGTH_SHORT).show();
                    return true;
                case Message_OK:
                    if(msg.arg1 == 1) {
                        buttonConnect.setEnabled(false);
                        buttonDisconnect.setEnabled(true);
                        editTextSSID.setEnabled(true);
                        editTextPassword.setEnabled(true);
                        imageViewLock.setEnabled(true);
                        buttonSend.setEnabled(true);
                        buttonClear.setEnabled(true);
                        buttonErase.setEnabled(true);
                        buttonRead.setEnabled(true);
                        buttonDisWiFi.setEnabled(true);
                        buttonReboot.setEnabled(true);
                        switchAC.setEnabled(true);
                        switchDH.setEnabled(true);
                        switchAP.setEnabled(true);
                        if(connectedThread != null)
                            connectedThread.write("{\"devices\":true}");
                        Toast.makeText(context, "Connected to Device：" + (String) (msg.obj),
                                Toast.LENGTH_SHORT).show();
                    } else
                        Toast.makeText(context, "Connection Failed",
                                Toast.LENGTH_SHORT).show();
                    return true;
            }
            return false;
        });
        return root;
    }

    private void findViews(View root) {
        buttonConnect=(Button)root.findViewById(R.id.button_connect);
        buttonDisconnect=(Button)root.findViewById(R.id.button_disconnect);
        buttonOffBT=(Button)root.findViewById(R.id.button_offBT);

        editTextSSID=(EditText)root.findViewById(R.id.editText_ssid);
        editTextPassword=(EditText)root.findViewById(R.id.editText_password);
        imageViewLock=(ImageView)root.findViewById(R.id.imageView_lock);

        buttonSend=(Button)root.findViewById(R.id.button_send);
        buttonClear=(Button)root.findViewById(R.id.button_clear);
        buttonErase=(Button)root.findViewById(R.id.button_erase);
        buttonRead=(Button)root.findViewById(R.id.button_read);
        buttonDisWiFi=(Button)root.findViewById(R.id.button_disWiFi);
        buttonReboot=(Button)root.findViewById(R.id.button_reboot);

        switchAC=(Switch)root.findViewById(R.id.switch_AC);
        switchDH=(Switch)root.findViewById(R.id.switch_DH);
        switchAP=(Switch)root.findViewById(R.id.switch_AP);
    }

    private void setListeners() {
        buttonConnect.setOnClickListener(myCListener);
        buttonDisconnect.setOnClickListener(myCListener);
        buttonOffBT.setOnClickListener(myCListener);

        imageViewLock.setOnClickListener(myCListener);

        buttonSend.setOnClickListener(myCListener);
        buttonClear.setOnClickListener(myCListener);
        buttonErase.setOnClickListener(myCListener);
        buttonRead.setOnClickListener(myCListener);
        buttonDisWiFi.setOnClickListener(myCListener);
        buttonReboot.setOnClickListener(myCListener);

        switchAC.setOnClickListener(myCListener);
        switchDH.setOnClickListener(myCListener);
        switchAP.setOnClickListener(myCListener);
    }

    /**
     * @param editText
     * @param imageView
     * 設置隱藏/顯示密碼
     */
    public void pwdShow(EditText editText,ImageView imageView){
        int type = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD;
        if(editText.getInputType() == type){//密碼可見
            editText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            imageView.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_partial_secure));

        }else{
            editText.setInputType(type);
            imageView.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_lock_idle_lock));
        }
        editText.setSelection(editText.getText().length());     //把光標設置到當前文本末尾
    }

    private void btDeviceConnect(){
        if (!btAdapter.isEnabled()) {//如果藍牙沒開啟
            Intent enableBtIntent = new
                    Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);//跳出視窗
            //開啟設定藍牙畫面
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        else{
            Toast.makeText(context,"Bluetooth is already on",
                    Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(context, DeviceScanActivity.class);
            startActivityForResult(intent,REQUEST_DeviceScan);
        }
    }

    private void bluetoothOff(){
        btAdapter.disable(); // turn off bluetooth
        Toast.makeText(context,"Bluetooth turned Off",
                Toast.LENGTH_SHORT).show();
        buttonConnect.setEnabled(true);
        buttonDisconnect.setEnabled(false);
        editTextSSID.setText("");
        editTextPassword.setText("");
        editTextSSID.setEnabled(false);
        editTextPassword.setEnabled(false);
        imageViewLock.setEnabled(false);
        buttonSend.setEnabled(false);
        buttonClear.setEnabled(false);
        buttonErase.setEnabled(false);
        buttonRead.setEnabled(false);
        buttonDisWiFi.setEnabled(false);
        buttonReboot.setEnabled(false);
        switchAC.setEnabled(false);
        switchDH.setEnabled(false);
        switchAP.setEnabled(false);
    }

    // Enter here after user selects "yes" or "no" to enabling radio
    //定義當按下跳出是否開啟藍牙視窗後要做的內容
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent Data) {
        super.onActivityResult(requestCode, resultCode, Data);
        // Check which request we're responding to
        if (requestCode == REQUEST_ENABLE_BT) {
            switch (resultCode) {
                case RESULT_OK:
                    Toast.makeText(context, "Bluetooth turned on", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(context, DeviceScanActivity.class);
                    startActivityForResult(intent, REQUEST_DeviceScan);
                    break;
                case RESULT_CANCELED:
                    Toast.makeText(context, "Bluetooth is not turned on", Toast.LENGTH_SHORT).show();
                    break;
            }
        }

        if (requestCode == REQUEST_DeviceScan) {
            if (resultCode == RETURN_DATA) {
                String address = Data.getStringExtra("address");
                name = Data.getStringExtra("name");
                // Spawn a new thread to avoid blocking the GUI one
                new Thread()
                {
                    public void run() {
                        boolean fail = false;
                        //取得裝置MAC找到連接的藍芽裝置
                        BluetoothDevice device = btAdapter.getRemoteDevice(address);
                        Message msg = new Message();

                        try {
                            btSocket = createBluetoothSocket(device);
                            //建立藍芽socket
                        } catch (IOException e) {
                            fail = true;
                            msg.what = Message_NG;
                            handler.sendMessage(msg);
                        }
                        // Establish the Bluetooth socket connection.
                        try {
                            btSocket.connect(); //建立藍芽連線
                        } catch (IOException e) {
                            try {
                                fail = true;
                                btSocket.close(); //關閉socket
                            } catch (IOException e2) {
                                msg.what = Message_NG;
                                handler.sendMessage(msg);
                            }
                        }
                        if(!fail) {
                            //開啟執行緒用於傳輸及接收資料
                            connectedThread = new ConnectedThread(btSocket);
                            connectedThread.start();
                            //開啟新執行緒顯示連接裝置名稱
                            handler.obtainMessage(Message_OK, 1, -1, name)
                                    .sendToTarget();
                        }
                    }
                }.start();
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (connectedThread != null)
            connectedThread.cancel();
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws
            IOException {
        //creates secure outgoing connection with BT device using UUID
        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { e.printStackTrace(); }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            BufferedReader br = new BufferedReader(new InputStreamReader(mmInStream));
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    String response = br.readLine();
                    //Transfer these data to the UI thread
                    Message msg = new Message();
                    msg.what = MESSAGE_READ;
                    msg.obj = response;
                    handler.sendMessage(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String input) {
            byte[] bytes = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { e.printStackTrace(); }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { e.printStackTrace(); }

            buttonConnect.setEnabled(true);
            buttonDisconnect.setEnabled(false);
            editTextSSID.setText("");
            editTextPassword.setText("");
            editTextSSID.setEnabled(false);
            editTextPassword.setEnabled(false);
            imageViewLock.setEnabled(false);
            buttonSend.setEnabled(false);
            buttonClear.setEnabled(false);
            buttonErase.setEnabled(false);
            buttonRead.setEnabled(false);
            buttonDisWiFi.setEnabled(false);
            buttonReboot.setEnabled(false);
            switchAC.setEnabled(false);
            switchDH.setEnabled(false);
            switchAP.setEnabled(false);
        }
    }
}