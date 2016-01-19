package com.framgia.alarm.utils;

import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.framgia.alarm.R;
import com.framgia.alarm.SetAlarmActivity;
import com.framgia.alarm.model.Alarm;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by avishek on 1/18/16.
 */
public class AlarmListAdapter extends BaseAdapter {
    private ArrayList<Alarm> mListItems;
    private LayoutInflater mLayoutInflater;
    private Context mContext;

    public AlarmListAdapter(Context context, ArrayList<Alarm> arrayList) {
        mListItems = arrayList;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context
                .LAYOUT_INFLATER_SERVICE);
        mContext = context;
    }

    @Override
    public int getCount() {
        return mListItems.size();
    }

    @Override
    public Alarm getItem(int position) {
        return mListItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getmId();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mLayoutInflater.inflate(R.layout.list_alarm, null);
            holder.time = (TextView) convertView.findViewById(R.id.text_time);
            holder.status = (ToggleButton) convertView.findViewById(R.id.toggle_on_off);
            holder.label = (TextView) convertView.findViewById(R.id.text_label);
            holder.day[Constants.INT_ZERO] = (ToggleButton) convertView.findViewById(R.id.toggle_sun);
            holder.day[Constants.INT_ONE] = (ToggleButton) convertView.findViewById(R.id.toggle_mon);
            holder.day[Constants.INT_TWO] = (ToggleButton) convertView.findViewById(R.id
                    .toggle_tue);
            holder.day[Constants.INT_THREE] = (ToggleButton) convertView.findViewById(R.id.toggle_wed);
            holder.day[Constants.INT_FOUR] = (ToggleButton) convertView.findViewById(R.id.toggle_thu);
            holder.day[Constants.INT_FIVE] = (ToggleButton) convertView.findViewById(R.id.toggle_fri);
            holder.day[Constants.INT_SIX] = (ToggleButton) convertView.findViewById(R.id.toggle_sat);
            holder.ringtone = (TextView) convertView.findViewById(R.id.text_ringtone);
            Date date = new Date(getItem(position).getmTime());
            SimpleDateFormat formatter = new SimpleDateFormat(Constants.HOUR_MINUTE, Locale.US);
            String dateFormatted = formatter.format(date);
            holder.time.setText(dateFormatted);
            if (getItem(position).getmStatus() == Constants.ON) holder.status.setChecked(true);
            holder.label.setText(getItem(position).getmLabel());
            for (int i = 0; i < 7; i++) {
                if ((getItem(position).getmDay_schedule().charAt(i)) == Integer.toString
                        (Constants.ON).charAt(Constants.INT_ZERO))
                    holder.day[i].setChecked(true);
            }
            holder.ringtone.setText(RingtoneManager.getRingtone(mContext, Uri.parse(getItem(position)
                    .getmAlarm_tone_uri()))
                    .getTitle(mContext));
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, SetAlarmActivity.class);
                    intent.putExtra(Constants.ID, getItem(position).getmId());
                    mContext.startActivity(intent);
                }
            });
        }
        return convertView;
    }

    private static class ViewHolder {
        TextView time, label, ringtone;
        ToggleButton status;
        ToggleButton[] day = new ToggleButton[7];
    }
}
