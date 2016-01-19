package com.framgia.alarm;

import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.framgia.alarm.model.Alarm;
import com.framgia.alarm.utils.Constants;
import com.framgia.alarm.utils.DatabaseHelper;

import java.util.Calendar;

public class SetAlarmActivity extends AppCompatActivity {
    private static final int RESULT_PICK_TONE = 1;
    private TimePicker mTimePicker;
    private EditText mLabel;
    private ToggleButton mToggleOnOff;
    private Button mTonePicker;
    private String mToneUri;
    private ToggleButton[] mDayOfWeek = new ToggleButton[7];
    private boolean mNewAlarm;
    private int mId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_alarm);
        mSetToolbar();
        mInitializeView();
        mSetListeners();
        mId = getIntent().getExtras().getInt(Constants.ID);
        mNewAlarm = (mId == Constants.OFF) ? true : false;
        //do later
    }

    private void mSetListeners() {
        mTonePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(RingtoneManager.ACTION_RINGTONE_PICKER),
                        RESULT_PICK_TONE);
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
                    mTonePicker.setText(RingtoneManager.getRingtone(this, uri).getTitle(this));
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
        mTonePicker = (Button) findViewById(R.id.button_tone_picker);
        mDayOfWeek[0] = (ToggleButton) findViewById(R.id.toggle_sun);
        mDayOfWeek[1] = (ToggleButton) findViewById(R.id.toggle_mon);
        mDayOfWeek[2] = (ToggleButton) findViewById(R.id.toggle_tue);
        mDayOfWeek[3] = (ToggleButton) findViewById(R.id.toggle_wed);
        mDayOfWeek[4] = (ToggleButton) findViewById(R.id.toggle_thu);
        mDayOfWeek[5] = (ToggleButton) findViewById(R.id.toggle_fri);
        mDayOfWeek[6] = (ToggleButton) findViewById(R.id.toggle_sat);
    }

    private void mSetToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
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
        String daySchedule = "";
        String[] day = new String[7];
        for (int i = 0; i < 7; i++) {
            day[i] = mDayOfWeek[i].isChecked() ? Integer.toString(Constants.ON) : Integer.toString
                    (Constants.OFF);
            daySchedule += day[i];
        }
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        DatabaseHelper db = new DatabaseHelper(getApplicationContext());
        db.createAlarm(new Alarm(calendar.getTimeInMillis(), status, label, uri, daySchedule));
        db.closeDB();
        finish();
    }
}
