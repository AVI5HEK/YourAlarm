package com.framgia.alarm;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.framgia.alarm.utils.Constants;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class RingAlarmActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {
    private TextView mTime, mLabel;
    private Button mButtonCancel;
    private MediaPlayer mMediaPlayer;
    private Vibrator mVibrator;
    private AlarmCountDownTimer mCountDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ring_alarm);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mInitializeViews();
        try {
            mPlayRingtone();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mTime.setText(new SimpleDateFormat(Constants.HOUR_MINUTE, Locale.US).format(Calendar
                .getInstance().getTime()));
        mLabel.setText(getIntent().getExtras().getString(Constants.LABEL));
        mCountDownTimer = new AlarmCountDownTimer(Constants.COUNTDOWN_START_TIME, Constants
                .COUNTDOWN_INTERVAL);
        mCountDownTimer.start();
        mButtonCancel.setOnClickListener(this);
        mButtonCancel.setOnLongClickListener(this);
    }

    private void mCall() {
        startActivity(new Intent(Intent.ACTION_CALL)
                .setData(Uri.parse("tel:" + getIntent().getExtras().getString(Constants.CONTACT))));
    }

    private void mPlayRingtone() throws IOException {
        mVibrator = (Vibrator) getApplicationContext().getSystemService(Context
                .VIBRATOR_SERVICE);
        mVibrator.vibrate(Constants.PATTERN, 0);
        Uri uri = Uri.parse(getIntent().getExtras().getString(Constants.URI));
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setDataSource(this, uri);
        final AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (audioManager.getStreamVolume(AudioManager.STREAM_RING) != Constants.INT_ZERO) {
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
            mMediaPlayer.setLooping(true);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        }
    }

    private void mInitializeViews() {
        mTime = (TextView) findViewById(R.id.text_time);
        mLabel = (TextView) findViewById(R.id.text_label);
        mButtonCancel = (Button) findViewById(R.id.button_dismiss);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_dismiss)
            Toast.makeText(getApplicationContext(), getString(R.string
                    .toast_message_ring_alarm_activity), Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onLongClick(View v) {
        if (v.getId() == R.id.button_dismiss) {
            stopAlarm();
            Intent homeIntent = new Intent(Intent.ACTION_MAIN);
            homeIntent.addCategory(Intent.CATEGORY_HOME);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(homeIntent);
            finish();
        }
        return false;
    }

    private void stopAlarm() {
        mMediaPlayer.stop();
        mVibrator.cancel();
    }

    public class AlarmCountDownTimer extends CountDownTimer {

        public AlarmCountDownTimer(long startTime, long interval) {
            super(startTime, interval);
        }

        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            if (!(TextUtils.isEmpty(getIntent().getExtras().getString
                    (Constants.CONTACT)))) {
                stopAlarm();
                mCall();
                finish();
            }
        }
    }
}
