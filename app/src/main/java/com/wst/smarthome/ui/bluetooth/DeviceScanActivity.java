package com.wst.smarthome.ui.bluetooth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.wst.smarthome.R;

import java.util.Set;

public class DeviceScanActivity extends AppCompatActivity {

    private Button buttonPairedDevices,buttonDiscover;
    View.OnClickListener myCListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.button_pairedDevices:
                    listPairedDevices(v);
                    break;
                case R.id.button_discover:
                    discover(v);
                    break;
            }
        }
    };
    private Set<BluetoothDevice> pairedDevices;
    private ArrayAdapter<String> btArrayAdapter;
    private ListView listViewDevices;
    private BluetoothAdapter btAdapter;
    private final int RETURN_DATA=40;
    private Context context;
    private boolean isRegisterReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_scan);

        setTitle("Bluetooth Devices");

        context = this;
        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);

        buttonPairedDevices=(Button)findViewById(R.id.button_pairedDevices);
        buttonDiscover=(Button)findViewById(R.id.button_discover);
        buttonPairedDevices.setOnClickListener(myCListener);
        buttonDiscover.setOnClickListener(myCListener);

        btArrayAdapter = new ArrayAdapter<String>
                (context,R.layout.simple_list_item_1);
        // get a handle on the bluetooth radio
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        listViewDevices=(ListView)findViewById(R.id.listView_devices);
        listViewDevices.setAdapter(btArrayAdapter);
        listViewDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(!btAdapter.isEnabled()) {
                    Toast.makeText(getBaseContext(), "Bluetooth not on",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                // Get the device MAC address, which is the last 17 chars in the View
                String info = ((TextView) view).getText().toString();
                String address = info.substring(info.length() - 17);
                String name = info.substring(0,info.length() - 17);
                Intent returnIntent = new Intent();
                returnIntent.putExtra("address",address);
                returnIntent.putExtra("name",name);
                setResult(RETURN_DATA,returnIntent);
                finish();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void discover(View view){
        // Check if the device is already discovering
        if(btAdapter.isDiscovering()){ //如果已經找到裝置
            btAdapter.cancelDiscovery(); //取消尋找
            Toast.makeText(getApplicationContext(),"Discovery stopped",Toast.LENGTH_SHORT).show();
        } else{
            if(btAdapter.isEnabled()) { //如果沒找到裝置且已按下尋找
                btArrayAdapter.clear(); // clear items
                // 設定廣播資訊過濾
                IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                // 註冊廣播接收器，接收並處理搜尋結果
                this.registerReceiver(blReceiver, intentFilter);
                // Register for broadcasts when discovery has finished
                intentFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                this.registerReceiver(blReceiver, intentFilter);
                isRegisterReceiver = true;
                btAdapter.startDiscovery(); //開始尋找
                Toast.makeText(getApplicationContext(), "Discovery started",
                        Toast.LENGTH_SHORT).show();
            } else{
                Toast.makeText(getApplicationContext(), "Bluetooth not on",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver blReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // add the name to the list
                btArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                btArrayAdapter.notifyDataSetChanged();
                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Toast.makeText(getApplicationContext(), "Discovery finished",
                        Toast.LENGTH_SHORT).show();
                if (btArrayAdapter.getCount() == 0) {
                    String noDevices = "No devices found";
                    btArrayAdapter.add(noDevices);
                    btArrayAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    private void listPairedDevices(View view){
        if(btAdapter.isDiscovering())
            btAdapter.cancelDiscovery(); //取消尋找

        pairedDevices = btAdapter.getBondedDevices();
        if(btAdapter.isEnabled()) {
            btArrayAdapter.clear(); // clear items
            // put it's one to the adapter
            for (BluetoothDevice device : pairedDevices)
                btArrayAdapter.add(device.getName() + "\n" + device.getAddress());

            Toast.makeText(getApplicationContext(), "Show Paired Devices",
                    Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(getApplicationContext(), "Bluetooth not on",
                    Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Make sure we're not doing discovery anymore
        if (btAdapter != null)
            btAdapter.cancelDiscovery();

        // Unregister broadcast listeners
        if (isRegisterReceiver)
            this.unregisterReceiver(blReceiver);
    }

}