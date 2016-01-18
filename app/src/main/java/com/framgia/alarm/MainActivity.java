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

import com.framgia.alarm.utils.AlarmListAdapter;
import com.framgia.alarm.utils.Constants;
import com.framgia.alarm.utils.DatabaseHelper;

public class MainActivity extends AppCompatActivity {
    private ListView mListView;
    private DatabaseHelper mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSetUpToolbar();
        mListView = (ListView) findViewById(R.id.list_alarm);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), SetAlarmActivity.class)
                        .putExtra(Constants.ID, Constants.NEW_ALARM));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDb = new DatabaseHelper(getApplicationContext());
        mDb.closeDB();
        AlarmListAdapter mAdapter = new AlarmListAdapter(this, mDb.getAlarms());
        mListView.setAdapter(mAdapter);
    }

    private void mSetUpToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }
}
