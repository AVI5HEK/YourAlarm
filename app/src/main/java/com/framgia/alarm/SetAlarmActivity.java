package com.framgia.alarm;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import com.framgia.alarm.utils.CalenderUtils;
import com.framgia.alarm.utils.Constants;
import com.framgia.alarm.utils.DatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class SetAlarmActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int RESULT_PICK_TONE = 1;
    private static final int REQUEST_PERMISSIONS_CREATE_EVENT = 3;
    private static final int REQUEST_PERMISSIONS_DELETE_EVENT = 4;
    private static final int REQUEST_PERMISSIONS_UPDATE_EVENT = 5;
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
    private CoordinatorLayout mCoordinatorLayout;

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
        mNewAlarm = mId == Constants.NEW_ALARM;
        mDb = new DatabaseHelper(getApplicationContext());
        if (!mNewAlarm) mPopulateViews(mDb.getAlarmById(mId));
        else {
            mToggleOnOff.setChecked(true);
            mButtonDeleteAlarm.setVisibility(View.GONE);
            mButtonTonePicker.setText(RingtoneManager.getRingtone(this, RingtoneManager
                    .getDefaultUri(RingtoneManager.TYPE_ALARM)).getTitle(this));
            mToneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mDb.closeDB();
    }

    private void mDeleteAlarmDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(SetAlarmActivity.this).create();
        alertDialog.setTitle(getString(R.string.delete_alarm_dialog_title));
        alertDialog.setMessage(getString(R.string.delete_alarm_dialog_message));
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string
                .delete_alarm_dialog_button_yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteAlarm();
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

    private void deleteAlarm() {
        if (mDb.eventExists(mId)) {
            if ((ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR)
                    != PackageManager.PERMISSION_GRANTED) || (ContextCompat.checkSelfPermission
                    (this, Manifest.permission.WRITE_CALENDAR)
                    != PackageManager.PERMISSION_GRANTED)) {
                requestPermission(REQUEST_PERMISSIONS_DELETE_EVENT);
                return;
            } else {
                deleteEventsFromCalendar();
                mDb.deleteAlarm(mId);
                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                if (alarmManager != null) {
                    alarmManager.cancel(PendingIntent.getBroadcast(getApplicationContext(),
                            mId, new Intent(getApplicationContext(), AlarmReceiver.class),
                            PendingIntent.FLAG_CANCEL_CURRENT));
                }
                finish();
            }
        } else {
            mDb.deleteAlarm(mId);
            finish();
        }
    }

    private void mShowToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    private void mPopulateViews(Alarm alarm) {
        mTimePicker.setCurrentHour(Integer.parseInt(new SimpleDateFormat("HH", Locale.US).format(new
                Date(alarm.getTime()))));
        mTimePicker.setCurrentMinute(Integer.parseInt(new SimpleDateFormat(Constants.HOUR_MINUTE,
                Locale.US).format(new Date(alarm.getTime())).substring(Constants.INT_THREE,
                Constants.INT_FIVE)));
        if (alarm.getStatus() == Constants.ON) mToggleOnOff.setChecked(true);
        else mToggleOnOff.setChecked(false);
        mLabel.setText(alarm.getLabel());
        for (int i = 0; i < 7; i++) {
            if (alarm.getDaySchedule().charAt(i) == Integer.toString(Constants.ON).charAt
                    (Constants.INT_ZERO)) {
                mDayOfWeek[i].setChecked(true);
            }
        }
        mToneUri = alarm.getAlarmToneUri();
        mButtonTonePicker.setText(RingtoneManager.getRingtone(this, Uri.parse(mToneUri)).getTitle
                (this));
    }

    private void mSetListeners() {
        mButtonTonePicker.setOnClickListener(this);
        mButtonDeleteAlarm.setOnClickListener(this);
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
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.main_layout);
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
        if (id == R.id.action_done) {
            if (mDb.eventExists(mId)) {
                if ((ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR)
                        != PackageManager.PERMISSION_GRANTED) ||
                        (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR)
                                != PackageManager.PERMISSION_GRANTED)) {
                    requestPermission(REQUEST_PERMISSIONS_UPDATE_EVENT);
                    return false;
                } else {
                    updateEventInCalendar();
                    finish();
                }
            } else {
                saveAlarm(createAlarm());
                finish();
            }
        } else if (id == R.id.action_add_to_google_calendar) {
            if (mDb.eventExists(mId)) {
                if ((ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR)
                        != PackageManager.PERMISSION_GRANTED) ||
                        (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR)
                                != PackageManager.PERMISSION_GRANTED)) {
                    requestPermission(REQUEST_PERMISSIONS_UPDATE_EVENT);
                    return false;
                } else {
                    updateEventInCalendar();
                    finish();
                }
            } else {
                if ((ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR)
                        != PackageManager.PERMISSION_GRANTED) ||
                        (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR)
                                != PackageManager.PERMISSION_GRANTED)) {
                    requestPermission(REQUEST_PERMISSIONS_CREATE_EVENT);
                } else {
                    addAlarmToCalendar();
                    finish();
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void requestPermission(final int requestCode) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(SetAlarmActivity.this,
                Manifest.permission.READ_CALENDAR) ||
                ActivityCompat.shouldShowRequestPermissionRationale(SetAlarmActivity.this,
                        Manifest.permission.WRITE_CALENDAR)) {
            Snackbar.make(mCoordinatorLayout, R.string.toast_permission_calendar_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.action_ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(SetAlarmActivity.this,
                                    new String[]{Manifest.permission.READ_CALENDAR, Manifest.permission
                                            .WRITE_CALENDAR}, requestCode);
                        }
                    })
                    .show();
        } else {
            ActivityCompat.requestPermissions(SetAlarmActivity.this,
                    new String[]{Manifest.permission.READ_CALENDAR, Manifest.permission
                            .WRITE_CALENDAR}, requestCode);
        }
    }

    private void addAlarmToCalendar() {
        if (saveEvent()) {
            mShowToast(getString(R.string.toast_create_success_message));
        } else mShowToast(getString(R.string.toast_create_failure_message));
    }

    private void deleteEventsFromCalendar() {
        if (deleteEvents()) {
            mDb.deleteEvents(mId);
            mShowToast(getString(R.string.toast_delete_success_message));
        } else mShowToast(getString(R.string.toast_delete_failure_message));
    }

    private void updateEventInCalendar() {
        if (saveUpdatedEvent()) {
            mShowToast(getString(R.string.toast_update_success_message));
        } else mShowToast(getString(R.string.toast_update_failure_message));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[]
            grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS_CREATE_EVENT: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    mShowToast(getString(R.string.toast_permission_granted));
                } else {
                    mShowToast(getString(R.string.toast_permission_create_message));
                }
                return;
            }
            case REQUEST_PERMISSIONS_UPDATE_EVENT: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    mShowToast(getString(R.string.toast_permission_granted));
                } else {
                    saveAlarm(createAlarm());
                    finish();
                    mShowToast(getString(R.string.toast_permission_update_message));
                }
                return;
            }
            case REQUEST_PERMISSIONS_DELETE_EVENT: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    mShowToast(getString(R.string.toast_permission_granted));
                } else {
                    mShowToast(getString(R.string.toast_permission_delete_message));
                }
            }
        }
    }

    private boolean saveEvent() {
        Alarm alarm = createAlarm();
        saveAlarm(alarm);
        boolean success = false;
        for (int i = 0; i < 7; i++) {
            if (alarm.getDaySchedule().charAt(i) == Integer.toString(Constants.ON).charAt(0)) {
                long eventID = CalenderUtils.addEventToCalender(getContentResolver(), alarm,
                        i + Constants.INT_ONE);
                success = eventID > Constants.INT_ZERO;
                if (success) mDb.createEvent((int) eventID, mId);
            }
        }
        return success;
    }

    private boolean saveUpdatedEvent() {
        boolean success = false;
        if (deleteEvents()) {
            mDb.deleteEvents(mId);
            if (saveEvent())
                success = true;
        }
        return success;
    }

    private boolean deleteEvents() {
        long rowsDeleted;
        int[] eventIds = mDb.getEventIds(mId);
        boolean success = false;
        if (eventIds.length > Constants.INT_ZERO)
            for (int eventId : eventIds) {
                if (eventId > Constants.INT_ZERO) {
                    rowsDeleted = CalenderUtils.removeCalendarEvent(eventId,
                            getContentResolver());
                    success = rowsDeleted > Constants.INT_ZERO;
                }
            }
        return success;
    }

    private Alarm createAlarm() {
        int hour = mTimePicker.getCurrentHour();
        int minute = mTimePicker.getCurrentMinute();
        int status = mToggleOnOff.isChecked() ? Constants.ON : Constants.OFF;
        String label = mLabel.getText().toString();
        String uri = mToneUri;
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
        calendar.set(Calendar.SECOND, Constants.INT_ZERO);
        calendar.set(Calendar.MILLISECOND, Constants.INT_ZERO);
        return new Alarm(calendar.getTimeInMillis(), status, label, uri,
                daySchedule);
    }

    private void insertOrUpdateAlarm(Alarm alarm) {
        if (mNewAlarm) {
            mId = (int) mDb.createAlarm(alarm);
        } else mDb.updateAlarm(alarm, mId);
    }

    private void saveAlarm(Alarm alarm) {
        insertOrUpdateAlarm(alarm);
        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
        intent.putExtra(Constants.ID, mId);
        intent.putExtra(Constants.LABEL, alarm.getLabel());
        intent.putExtra(Constants.URI, alarm.getAlarmToneUri());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), mId,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        long interval = Constants.ALARM_INTERVAL;
        if (mToggleOnOff.isChecked()) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarm.getTime(),
                    interval, pendingIntent);
        } else {
            if (alarmManager != null) {
                alarmManager.cancel(pendingIntent);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_delete_alarm:
                mDeleteAlarmDialog();
                break;
            case R.id.button_tone_picker:
                startActivityForResult(new Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
                        .putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager
                                .TYPE_ALARM), RESULT_PICK_TONE);
                break;
        }
    }
}
