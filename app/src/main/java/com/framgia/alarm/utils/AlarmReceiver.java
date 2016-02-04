package com.framgia.alarm.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.framgia.alarm.RingAlarmActivity;

import java.util.Calendar;

/**
 * Created by avishek on 1/15/16.
 */
public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        DatabaseHelper db = new DatabaseHelper(context);
        int today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;
        if (db.getAlarmById(intent.getExtras().getInt(Constants.ID)).getDaySchedule().charAt
                (today) == Integer.toString(Constants.ON).charAt(Constants.INT_ZERO)) {
            Intent scheduledIntent = new Intent(context, RingAlarmActivity.class);
            scheduledIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            scheduledIntent.putExtra(Constants.ID, intent.getExtras().getInt(Constants.ID));
            scheduledIntent.putExtra(Constants.LABEL, intent.getExtras().getString(Constants.LABEL));
            scheduledIntent.putExtra(Constants.URI, intent.getExtras().getString(Constants.URI));
            scheduledIntent.putExtra(Constants.CONTACT, intent.getExtras().getString(Constants
                    .CONTACT));
            context.startActivity(scheduledIntent);
            db.closeDB();
        }
    }
}
