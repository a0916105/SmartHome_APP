package com.wst.smarthome.ui.home;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.wst.smarthome.HttpCRUD;
import com.wst.smarthome.R;

import java.util.List;
import java.util.Map;

public class ScheduleAdapter extends BaseAdapter {
    private Context mContext;
    ViewHolder holder;
    List<Map<String, Object>> itemList;
    private boolean isShowCheckBox = false;//表示當前是否是多選狀態。
    private SparseBooleanArray stateCheckedMap;//用來存放CheckBox的選中狀態，true爲選中,false爲沒有選中
    private List<String> mCheckedID;//將選中數據的ID放入裏面

    public ScheduleAdapter(Context context, List<Map<String, Object>> itemList, SparseBooleanArray stateCheckedMap, List<String> mCheckedID){
        this.mContext=context;
        this.itemList=itemList;
        this.stateCheckedMap=stateCheckedMap;
        this.mCheckedID=mCheckedID;
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public Object getItem(int position) {
        return itemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return Integer.parseInt((String) itemList.get(position).get("id"));
    }

    public class ViewHolder {
        public CheckBox checkBox;
        public TextView tvID;
        public TextView tvDevices;
        public TextView tvOnOff;
        public TextView tvDate;
        public TextView tvTime;
        public TextView tvRepeat;
        public Switch switchSchedule;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = View.inflate(mContext, R.layout.item_schedule, null);

            holder.checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);
            holder.tvID = (TextView) convertView.findViewById(R.id.textView_id);
            holder.tvDevices = (TextView) convertView.findViewById(R.id.textView_devices);
            holder.tvOnOff = (TextView) convertView.findViewById(R.id.textView_switch);
            holder.tvDate = (TextView) convertView.findViewById(R.id.textView_date);
            holder.tvTime = (TextView) convertView.findViewById(R.id.textView_time);
            holder.tvRepeat = (TextView) convertView.findViewById(R.id.textView_weekday);
            holder.switchSchedule = (Switch)convertView.findViewById(R.id.switch_schedule);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        showAndHideCheckBox();//控制CheckBox的那個的框顯示與隱藏
        holder.checkBox.setChecked(stateCheckedMap.get(position));//設置CheckBox是否選中
        String id = (String) itemList.get(position).get("id");
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                stateCheckedMap.put(position,isChecked);
                if (isChecked)
                    mCheckedID.add(id);
                else
                    mCheckedID.remove(id);
            }
        });
        holder.tvID.setText(id);
        String deviceString = (String) itemList.get(position).get("devices");
        switch (deviceString){
            case "0":
                deviceString="冷氣機";
                break;
            case "1":
                deviceString="除濕機";
                break;
            case "2":
                deviceString="空氣清淨機";
                break;
        }
        holder.tvDevices.setText(deviceString);
        holder.tvOnOff.setText(((String) itemList.get(position).get("onOff")).equals("On")?"開":"關");
        holder.tvDate.setText((String) itemList.get(position).get("date"));
        holder.tvTime.setText((String) itemList.get(position).get("time"));
        String repeat = (String) itemList.get(position).get("weekday");
        if (repeat.equals("Only")) {
            repeat = "僅只一次";
        }else if (repeat.equals("Sun,Mon,Tue,Wed,Thu,Fri,Sat")){
            repeat = "每天";
        }else if (repeat.equals("Mon,Tue,Wed,Thu,Fri")){
            repeat = "工作日";
        }else if (repeat.equals("Sun,Sat")){
            repeat = "週末";
        }else {
            repeat = repeat.replace("Sun", "週日").replace("Mon", "週一").replace("Tue", "週二")
                    .replace("Wed", "週三").replace("Thu", "週四").replace("Fri", "週五")
                    .replace("Sat", "週六");
        }
        holder.tvRepeat.setText(repeat);
        holder.switchSchedule.setChecked(((String) itemList.get(position).get("schedule")).equals("enable"));
        holder.switchSchedule.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String param="schedule="+(isChecked?"enable":"disable")+"&id="+id;
                new HttpCRUD(HttpCRUD.DISABLE_SCHEDULE,param).start();
                itemList.get(position).put("schedule",(isChecked?"enable":"disable"));
            }
        });

        return convertView;
    }

    private void showAndHideCheckBox() {
        if (isShowCheckBox) {
            holder.checkBox.setVisibility(View.VISIBLE);
        } else {
            holder.checkBox.setVisibility(View.INVISIBLE);
        }
    }

    public boolean isShowCheckBox() {
        return isShowCheckBox;
    }

    public void setShowCheckBox(boolean showCheckBox) {
        isShowCheckBox = showCheckBox;
    }
}
