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
    private static final String TABLE_EVENT = "event";
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
    // column names of event table
    /**
     * event_id
     * alarm_id
     */
    private static final String EVENT_COLUMN_EVENT_ID = "event_id";
    private static final String EVENT_COLUMN_ALARM_ID = "alarm_id";
    private static final String EVENT_COLUMN_ACCOUNT = "account";
    //create statement of alarm table
    private static final String CREATE_TABLE_ALARM = "CREATE TABLE " + TABLE_ALARM
            + "(" + COLUMN_ID + " INTEGER PRIMARY KEY, "
            + COLUMN_TIME + " LONG, "
            + COLUMN_STATUS + " INTEGER, "
            + COLUMN_LABEL + " TEXT, "
            + COLUMN_ALARM_TONE_URI + " TEXT, "
            + COLUMN_DAY_SCHEDULE + " TEXT)";
    //create statement of event table
    private static final String CREATE_TABLE_EVENT = "CREATE TABLE " + TABLE_EVENT
            + "(" + EVENT_COLUMN_EVENT_ID + " TEXT, "
            + EVENT_COLUMN_ALARM_ID + " INTEGER, "
            + EVENT_COLUMN_ACCOUNT + " TEXT)";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_ALARM);
        db.execSQL(CREATE_TABLE_EVENT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ALARM);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENT);
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
        values.put(COLUMN_TIME, alarm.getTime());
        values.put(COLUMN_STATUS, alarm.getStatus());
        values.put(COLUMN_LABEL, alarm.getLabel());
        values.put(COLUMN_ALARM_TONE_URI, alarm.getAlarmToneUri());
        values.put(COLUMN_DAY_SCHEDULE, alarm.getDaySchedule());
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

    //get single row
    public Alarm getAlarmById(int id) {
        String query = "SELECT * FROM " + TABLE_ALARM + " WHERE " + COLUMN_ID + " = " + id;
        Cursor c = this.getReadableDatabase().rawQuery(query, null);
        c.moveToFirst();
        Alarm alarm = new Alarm(c.getInt(c.getColumnIndex(COLUMN_ID)),
                c.getLong(c.getColumnIndex(COLUMN_TIME)),
                c.getInt(c.getColumnIndex(COLUMN_STATUS)),
                c.getString(c.getColumnIndex(COLUMN_LABEL)),
                c.getString(c.getColumnIndex(COLUMN_ALARM_TONE_URI)),
                c.getString(c.getColumnIndex(COLUMN_DAY_SCHEDULE)));
        c.close();
        return alarm;
    }

    //get last alarm id
    public int getLastAlarmId() {
        String query = "SELECT * FROM " + TABLE_ALARM;
        Cursor c = mDb.rawQuery(query, null);
        if (c.getCount() > 0) {
            c.moveToLast();
            int lastAlarmId = c.getInt(c.getColumnIndex(COLUMN_ID));
            c.close();
            return lastAlarmId;
        } else return Constants.INT_ZERO;
    }

    //update alarm
    public long updateAlarm(Alarm alarm, int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TIME, alarm.getTime());
        values.put(COLUMN_STATUS, alarm.getStatus());
        values.put(COLUMN_LABEL, alarm.getLabel());
        values.put(COLUMN_ALARM_TONE_URI, alarm.getAlarmToneUri());
        values.put(COLUMN_DAY_SCHEDULE, alarm.getDaySchedule());
        return db.update(TABLE_ALARM, values, COLUMN_ID + " = " + id, null);
    }

    //delete alarm
    public int deleteAlarm(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_ALARM, COLUMN_ID + " = " + id, null);
    }

    //create event
    public long createEvent(String eventId, int alarmId, String account) {
        ContentValues values = new ContentValues();
        values.put(EVENT_COLUMN_EVENT_ID, eventId);
        values.put(EVENT_COLUMN_ALARM_ID, alarmId);
        values.put(EVENT_COLUMN_ACCOUNT, account);
        return this.getWritableDatabase().insert(TABLE_EVENT, null, values);
    }

    public boolean eventExists(int alarmId) {
        String query = "SELECT * FROM " + TABLE_EVENT + " WHERE " + EVENT_COLUMN_ALARM_ID + " = "
                + alarmId;
        Cursor c = this.getReadableDatabase().rawQuery(query, null);
        boolean exists = c.getCount() > Constants.INT_ZERO;
        c.close();
        return exists;
    }

    public ArrayList<com.framgia.alarm.model.Event> getEvent(int alarmId) {
        ArrayList<com.framgia.alarm.model.Event> events = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_EVENT + " WHERE " + EVENT_COLUMN_ALARM_ID + " = "
                + alarmId;
        Cursor c = mDb.rawQuery(query, null);
        if (c != null && c.getCount() > 0) {
            c.moveToFirst();
            for (int i = 0; i < c.getCount(); i++) {
                events.add(new com.framgia.alarm.model.Event(c.getString(c.getColumnIndex
                        (EVENT_COLUMN_EVENT_ID)),
                        c.getInt(c.getColumnIndex(EVENT_COLUMN_ALARM_ID)),
                        c.getString(c.getColumnIndex(EVENT_COLUMN_ACCOUNT))));
                c.moveToNext();
            }
        }
        if (c != null) {
            c.close();
        }
        return events;
    }

    //update event
    public long updateEvent(int eventId, int alarmId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(EVENT_COLUMN_EVENT_ID, eventId);
        values.put(EVENT_COLUMN_ALARM_ID, alarmId);
        return db.update(TABLE_EVENT, values, EVENT_COLUMN_ALARM_ID + " = " + eventId, null);
    }

    //delete event
    public int deleteEvent(int alarmId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_EVENT, EVENT_COLUMN_ALARM_ID + " = " + alarmId, null);
    }

    public void closeDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null && db.isOpen())
            db.close();
    }
}
