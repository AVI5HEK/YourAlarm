package com.framgia.alarm;

import android.accounts.AccountManager;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class SetAlarmActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int REQUEST_TONE_PICKER = 1;
    private static final int REQUEST_ACCOUNT_PICKER = 1000;
    private static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    private static final int REQUEST_CREATE = 100;
    private static final int REQUEST_UPDATE = 200;
    private static final int REQUEST_DELETE = 300;
    ProgressDialog mProgress;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {CalendarScopes.CALENDAR};
    private String mAccountName;
    private TimePicker mTimePicker;
    private EditText mLabel;
    private ToggleButton mToggleOnOff;
    private Button mButtonTonePicker;
    private ImageButton mButtonDeleteAlarm;
    private String mToneUri;
    private ToggleButton[] mDayOfWeek = new ToggleButton[7];
    private boolean mNewAlarm;
    private int mId;
    private String mEventId;
    private DatabaseHelper mDb;
    private Calendar mCalendar;
    private GoogleAccountCredential mCredential;
    private com.google.api.services.calendar.Calendar mService = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_alarm);
        mSetToolbar();
        mInitializeView();
        mSetListeners();
        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));
    }

    @Override
    protected void onStart() {
        super.onStart();
        mId = getIntent().getExtras().getInt(Constants.ID);
        mCalendar = Calendar.getInstance();
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

    private void mDeleteAlarm() {
        AlertDialog alertDialog = new AlertDialog.Builder(SetAlarmActivity.this).create();
        alertDialog.setTitle(getString(R.string.delete_alarm_dialog_title));
        alertDialog.setMessage(getString(R.string.delete_alarm_dialog_message));
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string
                .delete_alarm_dialog_button_yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mDb.deleteAlarm(mId);
                if (mDb.eventExists(mId)) {
                    ArrayList<com.framgia.alarm.model.Event> events = mDb.getEvent(mId);
                    String calendarId = "primary";
                    for (com.framgia.alarm.model.Event event : events) {
                        mMakeRequestTask(mGetCredential(event.getAccount()));
                        try {
                            mService.events().delete(calendarId, event.getEventId()).execute();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
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
                Date(alarm.getTime()))));
        mTimePicker.setCurrentMinute(Integer.parseInt(new SimpleDateFormat(Constants.HOUR_MINUTE,
                Locale.US).format(new Date(alarm.getTime())).substring(3, 5)));
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
        switch (requestCode) {
            case REQUEST_TONE_PICKER:
                if (resultCode == RESULT_OK && null != data) {
                    try {
                        Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                        if (uri != null) {
                            mToneUri = uri.toString();
                            mButtonTonePicker.setText(RingtoneManager.getRingtone(this, uri).getTitle
                                    (this));
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, getString(R.string.alarm_tone_picker_error_msg), Toast
                                .LENGTH_LONG).show();
                    }
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    mAccountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (mAccountName != null) {
                        mCredential.setSelectedAccountName(mAccountName);
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, mAccountName);
                        editor.apply();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode != RESULT_OK) {
                    mChooseAccount();
                }
                break;
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
        if (id == R.id.action_add_to_google_calendar) {
            if (mIsGooglePlayServicesAvailable()) {
                mAddToGoogleCalendar();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean mIsGooglePlayServicesAvailable() {
        final int connectionStatusCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
            mShowGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
            return false;
        } else if (connectionStatusCode != ConnectionResult.SUCCESS) {
            return false;
        }
        return true;
    }

    private void mShowGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
        Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                connectionStatusCode,
                SetAlarmActivity.this,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    private void mMakeRequestTask(GoogleAccountCredential credential) {
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        mService = new com.google.api.services.calendar.Calendar.Builder(transport, jsonFactory,
                credential).setApplicationName(getApplicationContext().getString
                (getApplicationContext().getApplicationInfo().labelRes)).build();
    }

    private void mAddToGoogleCalendar() {
        if (mCredential.getSelectedAccountName() == null) {
            mChooseAccount();
        } else {
            if (mIsDeviceOnline()) {
                if (mNewAlarm) {
                    mId = mDb.getLastAlarmId() + Constants.INT_ONE;
                    new MakeRequestTask(mCredential, REQUEST_CREATE).execute();
                } else {
                    if (mDb.eventExists(mId))
                        new MakeRequestTask(mCredential, REQUEST_UPDATE).execute();
                    else new MakeRequestTask(mCredential, REQUEST_DELETE).execute();
                }
            } else {
                Toast.makeText(getApplicationContext(), "No network connection available.", Toast
                        .LENGTH_LONG).show();
            }
        }
    }

    private boolean mIsDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    private void mChooseAccount() {
        startActivityForResult(mCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
    }

    private GoogleAccountCredential mGetCredential(String accountName) {
        return GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(accountName);
    }

    private void mSetAlarm() {
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
        mCalendar.set(Calendar.HOUR_OF_DAY, hour);
        mCalendar.set(Calendar.MINUTE, minute);
        mCalendar.set(Calendar.SECOND, Constants.INT_ZERO);
        mCalendar.set(Calendar.MILLISECOND, Constants.INT_ZERO);
        if (mNewAlarm) {
            mId = (int) mDb.createAlarm(new Alarm(mCalendar.getTimeInMillis(), status, label, uri,
                    daySchedule));
        } else {
            mDb.updateAlarm(new Alarm(mCalendar.getTimeInMillis(), status, label, uri, daySchedule)
                    , mId);
            if (mDb.eventExists(mId)) {
                new MakeRequestTask(mCredential, REQUEST_UPDATE).execute();
            }
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
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, mCalendar.getTimeInMillis(),
                    interval, pendingIntent);
        } else {
            if (alarmManager != null) {
                alarmManager.cancel(pendingIntent);
            }
        }
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_delete_alarm:
                mDeleteAlarm();
                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                if (alarmManager != null) {
                    alarmManager.cancel(PendingIntent.getBroadcast(getApplicationContext(),
                            mId, new Intent(getApplicationContext(), AlarmReceiver.class),
                            PendingIntent.FLAG_CANCEL_CURRENT));
                }
                break;
            case R.id.button_tone_picker:
                startActivityForResult(new Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
                        .putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager
                                .TYPE_ALARM), REQUEST_TONE_PICKER);
                break;
        }
    }

    private class MakeRequestTask extends AsyncTask<Void, Void, String> {
        private com.google.api.services.calendar.Calendar mService = null;
        private Exception mLastError = null;
        private int mRequestCode;

        public MakeRequestTask(GoogleAccountCredential credential, int requestCode) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName(getApplicationContext().getString
                            (getApplicationContext().getApplicationInfo().labelRes))
                    .build();
            mRequestCode = requestCode;
        }

        /**
         * Background task to call Google Calendar API.
         *
         * @param params no parameters needed for this task.
         */
        @Override
        protected String doInBackground(Void... params) {
            try {
                return getDataFromApi(mRequestCode);
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        /**
         * Fetch a list of the next 10 events from the primary calendar.
         *
         * @return List of Strings describing returned events.
         * @throws IOException
         */
        private String getDataFromApi(int requestCode) throws IOException {
            String response = "";
            switch (requestCode) {
                case REQUEST_CREATE:
                    for (int i = 0; i < 7; i++) {
                        if (mDayOfWeek[i].isChecked()) {
                            Event event = new Event()
                                    .setSummary(mLabel.getText().toString());
                            mEventId = UUID.randomUUID().toString();
                            event.setId(mEventId);
                            mCalendar.set(Calendar.DAY_OF_WEEK, i + Constants.INT_ONE);
                            mCalendar.add(Calendar.DATE, Constants.INT_SEVEN);
                            DateTime startDateTime = new DateTime(mCalendar.getTimeInMillis());
                            EventDateTime start = new EventDateTime()
                                    .setDateTime(startDateTime);
                            event.setStart(start);
                            DateTime endDateTime = new DateTime(mCalendar.getTimeInMillis() +
                                    Constants.EVENT_ALARM_LENGTH);
                            EventDateTime end = new EventDateTime()
                                    .setDateTime(endDateTime);
                            event.setEnd(end);
                            EventReminder[] reminderOverrides = new EventReminder[]{
                                    new EventReminder().setMethod("email").setMinutes(24 * 60),
                                    new EventReminder().setMethod("popup").setMinutes(Constants.INT_ZERO),
                            };
                            Event.Reminders reminders = new Event.Reminders()
                                    .setUseDefault(false)
                                    .setOverrides(Arrays.asList(reminderOverrides));
                            event.setReminders(reminders);
                            String calendarId = "primary";
                            event = mService.events().insert(calendarId, event).execute();
                            mDb.createEvent(mEventId, mId, mAccountName);
                            response = event != null ? "success" : "failed";
                        }
                    }
                    break;
                case REQUEST_UPDATE:
                    //else update event
                    //will work on this later
                    response = "successful";
                    break;
                case REQUEST_DELETE:
                    if (mDb.eventExists(mId)) {
                        ArrayList<com.framgia.alarm.model.Event> events = mDb.getEvent(mId);
                        String calendarId = "primary";
                        for (com.framgia.alarm.model.Event event : events) {
                            mService.events().delete(calendarId, event.getEventId()).execute();
                        }
                        response = "deleted";
                    }
                    break;
            }
            return response;
        }

        @Override
        protected void onPreExecute() {
            mProgress.show();
        }

        @Override
        protected void onPostExecute(String output) {
            mProgress.hide();
            if (output == null || output.length() == 0) {
                Toast.makeText(getApplicationContext(), "No results returned.", Toast
                        .LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), output, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    mShowGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            SetAlarmActivity.REQUEST_AUTHORIZATION);
                } else {
                    Toast.makeText(getApplicationContext(), "The following error occurred:\n"
                            + mLastError.getMessage(), Toast.LENGTH_LONG);
                }
            } else {
                Toast.makeText(getApplicationContext(), "Request cancelled.", Toast.LENGTH_LONG)
                        .show();
            }
        }
    }
}
