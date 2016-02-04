package com.framgia.alarm;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.framgia.alarm.utils.AlarmListAdapter;
import com.framgia.alarm.utils.Constants;
import com.framgia.alarm.utils.DatabaseHelper;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private ListView mListView;
    private DatabaseHelper mDb;
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSetUpToolbar();
        mInitializeViews();
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);
    }

    private void mInitializeViews() {
        mListView = (ListView) findViewById(R.id.list_alarm);
        mTextView = (TextView) findViewById(R.id.text_first_time);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDb = new DatabaseHelper(getApplicationContext());
        AlarmListAdapter mAdapter = new AlarmListAdapter(this, mDb.getAlarms());
        mListView.setAdapter(mAdapter);
        if (mListView.getAdapter().getCount() != Constants.INT_ZERO)
            mTextView.setVisibility(View.GONE);
        else mTextView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDb.closeDB();
    }

    private void mSetUpToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                startActivity(new Intent(getApplicationContext(), SetAlarmActivity.class)
                        .putExtra(Constants.ID, Constants.NEW_ALARM));
                break;
        }
    }
}
