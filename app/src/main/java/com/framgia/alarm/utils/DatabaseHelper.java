package com.framgia.alarm.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.framgia.alarm.model.Alarm;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by avishek on 1/15/16.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "ALARM.db";
    private static final int DATABASE_VERSION = 1;
    private DatabaseHelper mDbHelper;
    private Context mContext;
    private SQLiteDatabase mDb;
    // Table name
    private static final String TABLE_ALARM = "alarm";
    // column names of alarm table
    /**
     * time
     * status
     * label
     * alarm_tone_uri
     * day_schedule
     */
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TIME = "time";
    private static final String COLUMN_STATUS = "status";
    private static final String COLUMN_LABEL = "label";
    private static final String COLUMN_ALARM_TONE_URI = "alarm_tone_uri";
    private static final String COLUMN_DAY_SCHEDULE = "day_schedule";
    //create statement
    private static final String CREATE_TABLE_ALARM = "CREATE TABLE " + TABLE_ALARM
            + "(" + COLUMN_ID + " INTEGER PRIMARY KEY, "
            + COLUMN_TIME + " LONG, "
            + COLUMN_STATUS + " INTEGER, "
            + COLUMN_LABEL + " TEXT, "
            + COLUMN_ALARM_TONE_URI + " TEXT, "
            + COLUMN_DAY_SCHEDULE + " TEXT)";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_ALARM);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ALARM);
    }

    public DatabaseHelper open() throws SQLException {
        mDbHelper = new DatabaseHelper(mContext);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        if (mDbHelper != null) {
            mDbHelper.close();
        }
    }

    //CRUD methods for alarm table
    public long createAlarm(Alarm alarm) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_TIME, alarm.getmTime());
        values.put(COLUMN_STATUS, alarm.getmStatus());
        values.put(COLUMN_LABEL, alarm.getmLabel());
        values.put(COLUMN_ALARM_TONE_URI, alarm.getmAlarm_tone_uri());
        values.put(COLUMN_DAY_SCHEDULE, alarm.getmDay_schedule());
        return this.getWritableDatabase().insert(TABLE_ALARM, null, values);
    }

    //get every alarm in the database
    public ArrayList<Alarm> getAlarms() {
        ArrayList<Alarm> alarms = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_ALARM;
        Cursor c = this.getReadableDatabase().rawQuery(query, null);
        if (c.moveToFirst()) {
            do {
                alarms.add(new Alarm(c.getInt(c.getColumnIndex(COLUMN_ID)),
                        c.getLong(c.getColumnIndex(COLUMN_TIME)),
                        c.getInt(c.getColumnIndex(COLUMN_STATUS)),
                        c.getString(c.getColumnIndex(COLUMN_LABEL)),
                        c.getString(c.getColumnIndex(COLUMN_ALARM_TONE_URI)),
                        c.getString(c.getColumnIndex(COLUMN_DAY_SCHEDULE))));
            } while (c.moveToNext());
        }
        c.close();
        return alarms;
    }

    public void closeDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null && db.isOpen())
            db.close();
    }
}
