package com.wst.smarthome.ui.home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.wst.smarthome.HttpCRUD;
import com.wst.smarthome.R;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class HomeFragment extends Fragment {

    private Context context;
    private MysqlCon con;
    private Switch switchAC,switchDH,switchAP;
    private final static int AC=0,DH=1,AP=2;
    private final View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.button_AC:
                    intent.putExtra("device",AC);
                    startActivity(intent);
                    break;
                case R.id.button_DH:
                    intent.putExtra("device",DH);
                    startActivity(intent);
                    break;
                case R.id.button_AP:
                    intent.putExtra("device",AP);
                    startActivity(intent);
                    break;
                case R.id.switch_AC:
                    if (switchAC.isChecked())
                        new DeviceStatesUpdate(AC,"On").start();
                    else
                        new DeviceStatesUpdate(AC,"Off").start();
                    break;
                case R.id.switch_DH:
                    if (switchDH.isChecked())
                        new DeviceStatesUpdate(DH,"On").start();
                    else
                        new DeviceStatesUpdate(DH,"Off").start();
                    break;
                case R.id.switch_AP:
                    if (switchAP.isChecked())
                        new DeviceStatesUpdate(AP,"On").start();
                    else
                        new DeviceStatesUpdate(AP,"Off").start();
                    break;
            }
        }
    };
    private Timer timer;
    private Button buttonAC,buttonDH,buttonAP;
    private Intent intent;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        context = getContext();

        findViews(root);
        intent = new Intent(context, ScheduleActivity.class);
        setListeners();

        return root;
    }

    private void findViews(View root) {
        switchAC=(Switch)root.findViewById(R.id.switch_AC);
        switchDH=(Switch)root.findViewById(R.id.switch_DH);
        switchAP=(Switch)root.findViewById(R.id.switch_AP);

        buttonAC=(Button)root.findViewById(R.id.button_AC);
        buttonDH=(Button)root.findViewById(R.id.button_DH);
        buttonAP=(Button)root.findViewById(R.id.button_AP);
    }

    private void setListeners() {
        switchAC.setOnClickListener(listener);
        switchDH.setOnClickListener(listener);
        switchAP.setOnClickListener(listener);

        buttonAC.setOnClickListener(listener);
        buttonDH.setOnClickListener(listener);
        buttonAP.setOnClickListener(listener);
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
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                con = new MysqlCon();
                con.run();
                final boolean[] data = con.getDeviceState();
                Log.v("HomeFragment", "DeviceState：" + Arrays.toString(data));

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        switchAC.setChecked(data[AC]);
                        switchDH.setChecked(data[DH]);
                        switchAP.setChecked(data[AP]);
                    }
                });
            }
        };
        timer.schedule(task,0,5000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        con.close();
    }

    private class DeviceStatesUpdate extends HttpCRUD{
        public DeviceStatesUpdate(int device, String onOff) {
            super(HttpCRUD.UPDATE_SCHEDULE,"schedule=enable&weekday=Now&devices="+device+"&switch="+onOff+"&id="+device);
        }
    }
}