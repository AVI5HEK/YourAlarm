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
    private final ArrayList<Alarm> mListItems;
    private final LayoutInflater mLayoutInflater;
    private final Context mContext;

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
        return getItem(position).getId();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
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
            Date date = new Date(getItem(position).getTime());
            SimpleDateFormat formatter = new SimpleDateFormat(Constants.HOUR_MINUTE, Locale.US);
            String dateFormatted = formatter.format(date);
            holder.time.setText(dateFormatted);
            if (getItem(position).getStatus() == Constants.ON) holder.status.setChecked(true);
            holder.label.setText(getItem(position).getLabel());
            for (int i = 0; i < 7; i++) {
                if ((getItem(position).getDaySchedule().charAt(i)) == Integer.toString
                        (Constants.ON).charAt(Constants.INT_ZERO))
                    holder.day[i].setChecked(true);
            }
            holder.ringtone.setText(RingtoneManager.getRingtone(mContext, Uri.parse(getItem(position)
                    .getAlarmToneUri()))
                    .getTitle(mContext));
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, SetAlarmActivity.class);
                    intent.putExtra(Constants.ID, getItem(position).getId());
                    mContext.startActivity(intent);
                }
            });
            holder.status.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int status = holder.status.isChecked() ? Constants.ON : Constants.OFF;
                    DatabaseHelper db = new DatabaseHelper(mContext);
                    String daySchedule = Constants.EMPTY_STRING;
                    for (int i = 0; i < 7; i++) {
                        daySchedule += holder.day[i].isChecked() ? Integer.toString(Constants.ON) :
                                Integer.toString(Constants.OFF);
                    }
                    db.updateAlarm(new Alarm(
                            getItem(position).getTime(),
                            status,
                            getItem(position).getLabel(),
                            getItem(position).getAlarmToneUri(),
                            daySchedule), (int) getItemId(position));
                    db.closeDB();
                }
            });
        }
        return convertView;
    }

    private static class ViewHolder {
        TextView time, label, ringtone;
        ToggleButton status;
        final ToggleButton[] day = new ToggleButton[7];
    }
}
