package com.framgia.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.framgia.alarm.model.Alarm;
import com.framgia.alarm.utils.AlarmReceiver;
import com.framgia.alarm.utils.Constants;
import com.framgia.alarm.utils.DatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class SetAlarmActivity extends AppCompatActivity {
    private static final int RESULT_PICK_TONE = 1;
    private TimePicker mTimePicker;
    private EditText mLabel;
    private ToggleButton mToggleOnOff;
    private Button mButtonTonePicker;
    private ImageButton mButtonDeleteAlarm;
    private String mToneUri;
    private ToggleButton[] mDayOfWeek = new ToggleButton[7];
    private boolean mNewAlarm;
    private int mId;
    private DatabaseHelper mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_alarm);
        mSetToolbar();
        mInitializeView();
        mSetListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mId = getIntent().getExtras().getInt(Constants.ID);
        mNewAlarm = (mId == Constants.NEW_ALARM) ? true : false;
        mDb = new DatabaseHelper(getApplicationContext());
        if (mId > Constants.OFF) mPopulateViews(mDb.getAlarmById(mId));
    }

    @Override
    protected void onStop() {
        super.onStop();
        mDb.closeDB();
    }

    private void mDeleteAlarm() {
        AlertDialog alertDialog = new AlertDialog.Builder(SetAlarmActivity.this).create();
        alertDialog.setTitle(getString(R.string.delete_alarm_dialog_title));
        alertDialog.setMessage(getString(R.string.delete_alarm_dialog_message));
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string
                .delete_alarm_dialog_button_yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mDb.deleteAlarm(mId);
                finish();
            }
        });
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string
                .delete_alarm_dialog_button_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alertDialog.show();
    }

    private void mPopulateViews(Alarm alarm) {
        mTimePicker.setCurrentHour(Integer.parseInt(new SimpleDateFormat("HH", Locale.US).format(new
                Date(alarm.getmTime()))));
        mTimePicker.setCurrentMinute(Integer.parseInt(new SimpleDateFormat(Constants.HOUR_MINUTE,
                Locale.US).format(new Date(alarm.getmTime())).substring(3, 5)));
        if (alarm.getmStatus() == Constants.ON) mToggleOnOff.setChecked(true);
        else mToggleOnOff.setChecked(false);
        mLabel.setText(alarm.getmLabel());
        for (int i = 0; i < 7; i++) {
            if (alarm.getmDay_schedule().charAt(i) == Integer.toString(Constants.ON).charAt
                    (Constants.INT_ZERO)) {
                mDayOfWeek[i].setChecked(true);
            }
        }
        mToneUri = alarm.getmAlarm_tone_uri();
        mButtonTonePicker.setText(RingtoneManager.getRingtone(this, Uri.parse(mToneUri)).getTitle
                (this));
    }

    private void mSetListeners() {
        mButtonTonePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(RingtoneManager.ACTION_RINGTONE_PICKER),
                        RESULT_PICK_TONE);
            }
        });
        mButtonDeleteAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDeleteAlarm();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == RESULT_PICK_TONE && resultCode == RESULT_OK
                    && null != data) {
                Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                if (uri != null) {
                    mToneUri = uri.toString();
                    mButtonTonePicker.setText(RingtoneManager.getRingtone(this, uri).getTitle
                            (this));
                }
            } else {
                Toast.makeText(this, getString(R.string.alarm_tone_picker_msg), Toast
                        .LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.alarm_tone_picker_error_msg), Toast
                    .LENGTH_LONG).show();
        }
    }

    private void mInitializeView() {
        mTimePicker = (TimePicker) findViewById(R.id.text_time);
        mLabel = (EditText) findViewById(R.id.edit_label);
        mToggleOnOff = (ToggleButton) findViewById(R.id.toggle_on_off);
        mButtonTonePicker = (Button) findViewById(R.id.button_tone_picker);
        mDayOfWeek[Constants.INT_ZERO] = (ToggleButton) findViewById(R.id.toggle_sun);
        mDayOfWeek[Constants.INT_ONE] = (ToggleButton) findViewById(R.id.toggle_mon);
        mDayOfWeek[Constants.INT_TWO] = (ToggleButton) findViewById(R.id.toggle_tue);
        mDayOfWeek[Constants.INT_THREE] = (ToggleButton) findViewById(R.id.toggle_wed);
        mDayOfWeek[Constants.INT_FOUR] = (ToggleButton) findViewById(R.id.toggle_thu);
        mDayOfWeek[Constants.INT_FIVE] = (ToggleButton) findViewById(R.id.toggle_fri);
        mDayOfWeek[Constants.INT_SIX] = (ToggleButton) findViewById(R.id.toggle_sat);
        mButtonDeleteAlarm = (ImageButton) findViewById(R.id.button_delete_alarm);
    }

    private void mSetToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_set_alarm, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_done)
            mSetAlarm();
        return super.onOptionsItemSelected(item);
    }

    private void mSetAlarm() {
        int hour = mTimePicker.getCurrentHour();
        int minute = mTimePicker.getCurrentMinute();
        int status = mToggleOnOff.isChecked() ? Constants.ON : Constants.OFF;
        String label = mLabel.getText().toString();
        String uri = mToneUri == null ? RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                .toString() : mToneUri;
        String daySchedule = Constants.EMPTY_STRING;
        String[] day = new String[7];
        for (int i = 0; i < 7; i++) {
            day[i] = mDayOfWeek[i].isChecked() ? Integer.toString(Constants.ON) : Integer.toString
                    (Constants.OFF);
            daySchedule += day[i];
        }
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        if (mNewAlarm) {
            mId = (int) mDb.createAlarm(new Alarm(calendar.getTimeInMillis(), status, label, uri,
                    daySchedule));
        } else {
            mDb.updateAlarm(new Alarm(calendar.getTimeInMillis(), status, label, uri, daySchedule)
                    , mId);
        }
        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
        intent.putExtra(Constants.ID, mId);
        intent.putExtra(Constants.LABEL, label);
        intent.putExtra(Constants.URI, uri);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), mId,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        long interval = Constants.ALARM_INTERVAL;
        if (mToggleOnOff.isChecked()) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    interval, pendingIntent);
        } else {
            if (alarmManager != null) {
                alarmManager.cancel(pendingIntent);
            }
        }
        finish();
    }
}
