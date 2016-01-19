package com.framgia.alarm;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.framgia.alarm.utils.Constants;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class RingAlarmActivity extends AppCompatActivity {
    private TextView mTime, mLabel;
    private Button mButtonCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ring_alarm);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Toast.makeText(getApplicationContext(), "working", Toast.LENGTH_LONG).show();
        mTime = (TextView) findViewById(R.id.text_time);
        mLabel = (TextView) findViewById(R.id.text_label);
        mButtonCancel = (Button) findViewById(R.id.button_dismiss);
        Uri uri = Uri.parse(getIntent().getExtras().getString(Constants.URI));
        Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), uri);
        ringtone.play();
        mTime.setText(new SimpleDateFormat(Constants.HOUR_MINUTE, Locale.US).format(Calendar
                .getInstance().getTime()));
        mLabel.setText(getIntent().getExtras().getString(Constants.LABEL));
        mButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
