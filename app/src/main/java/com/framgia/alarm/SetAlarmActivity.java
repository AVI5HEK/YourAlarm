package com.framgia.alarm;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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
    private static final int RESULT_PICK_CONTACT = 2;
    private static final int REQUEST_PERMISSIONS_CREATE_EVENT = 3;
    private static final int REQUEST_PERMISSIONS_DELETE_EVENT = 4;
    private static final int REQUEST_PERMISSIONS_UPDATE_EVENT = 5;
    private static final int REQUEST_PERMISSIONS_CALL_PHONE = 6;
    private TimePicker mTimePicker;
    private EditText mLabel;
    private ToggleButton mToggleOnOff;
    private Button mButtonTonePicker, mButtonContactPicker;
    private CheckBox mCheckContact;
    private ImageButton mButtonDeleteAlarm;
    private String mToneUri;
    private String mContact;
    private final ToggleButton[] mDayOfWeek = new ToggleButton[7];
    private boolean mNewAlarm;
    private int mId;
    private DatabaseHelper mDb;
    private CoordinatorLayout mCoordinatorLayout;
    private LinearLayout mLinear4;
    private boolean mFirstTime = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_alarm);
        mSetToolbar();
        mInitializeView();
        mSetListeners();
        mFirstTime = true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mFirstTime) {
            mId = getIntent().getExtras().getInt(Constants.ID);
            mNewAlarm = mId == Constants.NEW_ALARM;
            mDb = new DatabaseHelper(getApplicationContext());
            if (!mNewAlarm) mPopulateViews(mDb.getAlarmById(mId));
            else {
                mToggleOnOff.setChecked(true);
                mButtonDeleteAlarm.setVisibility(View.GONE);
                mButtonTonePicker.setText(RingtoneManager.getRingtone(this, RingtoneManager
                        .getDefaultUri(RingtoneManager.TYPE_ALARM)).getTitle(this));
                mDayOfWeek[Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1].setChecked(true);
                mToneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString();
                mContact = Constants.EMPTY_STRING;
                mCheckContact.setChecked(false);
                mLinear4.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mDb.closeDB();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Calendar alarmInstance = Calendar.getInstance();
        alarmInstance.set(Calendar.HOUR_OF_DAY, mTimePicker.getCurrentMinute());
        alarmInstance.set(Calendar.MINUTE, mTimePicker.getCurrentMinute());
        Calendar currentInstance = Calendar.getInstance();
        mShowToast(String.format(getResources().getString(R.string.time_difference),
                alarmInstance.get(Calendar.HOUR_OF_DAY) - currentInstance.get(Calendar.HOUR_OF_DAY),
                alarmInstance.get(Calendar.MINUTE) - currentInstance.get(Calendar.MINUTE)));
        /*Log.e("current hr and min", Integer.toString(currentInstance.get(Calendar.HOUR_OF_DAY)) + ", " + Integer.toString(currentInstance.get(Calendar.MINUTE)));
        Log.e("alarm hr and min", Integer.toString(alarmInstance.get(Calendar.HOUR_OF_DAY)) + ", " +
                "" + Integer.toString(alarmInstance.get(Calendar.MINUTE)));*/
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
        mContact = alarm.getContact();
        if (!(TextUtils.isEmpty(mContact))) {
            mCheckContact.setChecked(true);
            mLinear4.setVisibility(View.VISIBLE);
            mButtonContactPicker.setText(mContact);
        } else {
            mCheckContact.setChecked(false);
            mLinear4.setVisibility(View.INVISIBLE);
        }
    }

    private void mSetListeners() {
        mButtonTonePicker.setOnClickListener(this);
        mButtonDeleteAlarm.setOnClickListener(this);
        mButtonContactPicker.setOnClickListener(this);
        mCheckContact.setOnClickListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            switch (requestCode) {
                case RESULT_PICK_TONE:
                    if (resultCode == RESULT_OK && null != data) {
                        Uri uri = data.getParcelableExtra(RingtoneManager
                                .EXTRA_RINGTONE_PICKED_URI);
                        if (uri != null) {
                            mToneUri = uri.toString();
                            mButtonTonePicker.setText(RingtoneManager.getRingtone(this, uri)
                                    .getTitle(this));
                        }
                    } else {
                        Toast.makeText(this, getString(R.string.picker_msg), Toast
                                .LENGTH_LONG).show();
                    }
                    return;
                case RESULT_PICK_CONTACT:
                    if (resultCode == RESULT_OK && null != data) {
                        Cursor cursor = getContentResolver().query(data.getData(), null, null,
                                null, null);
                        cursor.moveToFirst();
                        mContact = cursor.getString(cursor.getColumnIndex(ContactsContract
                                .CommonDataKinds.Phone.NUMBER));
                        mButtonContactPicker.setText(mContact);
                        cursor.close();
                    }
            }
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.picker_error_msg), Toast
                    .LENGTH_LONG).show();
        }
    }

    private void mInitializeView() {
        mTimePicker = (TimePicker) findViewById(R.id.text_time);
        mLabel = (EditText) findViewById(R.id.edit_label);
        mToggleOnOff = (ToggleButton) findViewById(R.id.toggle_on_off);
        mButtonTonePicker = (Button) findViewById(R.id.button_tone_picker);
        mButtonContactPicker = (Button) findViewById(R.id.button_contact_picker);
        mDayOfWeek[Constants.INT_ZERO] = (ToggleButton) findViewById(R.id.toggle_sun);
        mDayOfWeek[Constants.INT_ONE] = (ToggleButton) findViewById(R.id.toggle_mon);
        mDayOfWeek[Constants.INT_TWO] = (ToggleButton) findViewById(R.id.toggle_tue);
        mDayOfWeek[Constants.INT_THREE] = (ToggleButton) findViewById(R.id.toggle_wed);
        mDayOfWeek[Constants.INT_FOUR] = (ToggleButton) findViewById(R.id.toggle_thu);
        mDayOfWeek[Constants.INT_FIVE] = (ToggleButton) findViewById(R.id.toggle_fri);
        mDayOfWeek[Constants.INT_SIX] = (ToggleButton) findViewById(R.id.toggle_sat);
        mButtonDeleteAlarm = (ImageButton) findViewById(R.id.button_delete_alarm);
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.main_layout);
        mCheckContact = (CheckBox) findViewById(R.id.check_contact);
        mLinear4 = (LinearLayout) findViewById(R.id.linear_4);
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
            if (!TextUtils.isEmpty(mContact)) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermission(REQUEST_PERMISSIONS_CALL_PHONE);
                    return false;
                }
            }
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
            if (!TextUtils.isEmpty(mContact)) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermission(REQUEST_PERMISSIONS_CALL_PHONE);
                    return false;
                }
            }
            for (int i = 0; i < 7; i++) {
                if (mDayOfWeek[i].isChecked() && mToggleOnOff.isChecked())
                    break;
                else if (i == 6) {
                    mShowToast(getString(R.string.toast_calendar_select_day));
                    return false;
                }
            }
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

        return super.

                onOptionsItemSelected(item);

    }

    private void requestPermission(final int requestCode) {
        if (requestCode == REQUEST_PERMISSIONS_CREATE_EVENT ||
                requestCode == REQUEST_PERMISSIONS_UPDATE_EVENT ||
                requestCode == REQUEST_PERMISSIONS_DELETE_EVENT) {
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
        } else if (requestCode == REQUEST_PERMISSIONS_CALL_PHONE) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(SetAlarmActivity.this,
                    Manifest.permission.CALL_PHONE)) {
                Snackbar.make(mCoordinatorLayout, R.string.toast_permission_call_phone_rationale,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.action_ok, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ActivityCompat.requestPermissions(SetAlarmActivity.this,
                                        new String[]{Manifest.permission.CALL_PHONE}, requestCode);
                            }
                        })
                        .show();
            } else {
                ActivityCompat.requestPermissions(SetAlarmActivity.this,
                        new String[]{Manifest.permission.CALL_PHONE}, requestCode);
            }
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
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
                return;
            }
            case REQUEST_PERMISSIONS_CALL_PHONE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mShowToast(getString(R.string.toast_permission_granted));
                } else {
                    mShowToast(getString(R.string.toast_permission_call_phone_message));
                }
            }
        }
    }

    private boolean saveEvent() {
        Alarm alarm = createAlarm();
        saveAlarm(alarm);
        boolean success = false;
        for (int i = 0; i < 7; i++) {
            if (alarm.getStatus() == 1 && alarm.getDaySchedule().charAt(i) == Integer.toString
                    (Constants.ON).charAt(0)) {
                long eventID = CalenderUtils.addEventToCalender(getContentResolver(), alarm,
                        i + Constants.INT_ONE);
                success = eventID > Constants.INT_ZERO;
                if (success) mDb.createEvent((int) eventID, mId);
                else return success;
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
        String contact = mContact;
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
                daySchedule, contact);
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
        intent.putExtra(Constants.CONTACT, alarm.getContact());
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
            case R.id.button_contact_picker:
                mFirstTime = false;
                startActivityForResult(new Intent(Intent.ACTION_PICK, ContactsContract
                        .CommonDataKinds.Phone.CONTENT_URI), RESULT_PICK_CONTACT);
                break;
            case R.id.check_contact:
                if (mCheckContact.isChecked())
                    mLinear4.setVisibility(View.VISIBLE);
                else {
                    mLinear4.setVisibility(View.INVISIBLE);
                    mContact = Constants.EMPTY_STRING;
                }
        }
    }
}
