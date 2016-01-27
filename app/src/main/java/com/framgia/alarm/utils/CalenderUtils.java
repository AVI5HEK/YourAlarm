package com.framgia.alarm.utils;

/**
 * Created by FRAMGIA\nguyen.viet.manh on 22/01/2016.
 */

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.net.Uri;

import com.framgia.alarm.model.Alarm;

import java.util.Calendar;
import java.util.TimeZone;

public class CalenderUtils {
    private static final String EVENT_URI_STR = "content://com.android.calendar/events";
    private static final String REMINDER_URI_STRING = "content://com.android.calendar/reminders";
    //default get first calendar in account android system
    private static final int CALENDAR_ID_VALUE = 1;
    private static final String CALENDAR_ID = "calendar_id";
    private static final String TITLE = "title";
    private static final String EVENT_TIME_ZONE = "eventTimezone";
    private static final String DT_START = "dtstart";
    private static final String DT_END = "dtend";
    private static final String EVENT_STATUS = "eventStatus";
    private static final String HAS_ALARM = "hasAlarm";
    private static final String EVENT_ID = "event_id";
    private static final String MINUTES = "minutes";
    private static final String METHOD = "method";

    public static long addEventToCalender(ContentResolver cr, Alarm alarm, int dayOfWek) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(alarm.getTime());
        c.set(Calendar.DAY_OF_WEEK, dayOfWek);
        c.add(Calendar.DATE, Constants.INT_SEVEN);
        ContentValues event = new ContentValues();
        event.put(CALENDAR_ID, CALENDAR_ID_VALUE);
        event.put(TITLE, alarm.getLabel());
        event.put(EVENT_TIME_ZONE, TimeZone.getDefault().getID());
        long startDate = c.getTimeInMillis();
        // For next 1hr
        long endDate = startDate + Constants.EVENT_LENGTH;
        event.put(DT_START, startDate);
        event.put(DT_END, endDate);
        event.put(EVENT_STATUS, Constants.INT_ONE);
        event.put(HAS_ALARM, Constants.INT_ONE);
        Uri eventUri = cr.insert(Uri.parse(EVENT_URI_STR), event);
        long eventID = Long.parseLong(eventUri != null ? eventUri.getLastPathSegment() : Integer
                .toString(Constants.INT_ZERO));
        ContentValues reminderValues = new ContentValues();
        reminderValues.put(EVENT_ID, eventID);
        // Default value of the system. Minutes is a integer
        reminderValues.put(MINUTES, Constants.INT_FIVE);
        // Alert Methods: Default(0), Alert(1), Email(2), SMS(3)
        reminderValues.put(METHOD, Constants.INT_ONE);
        cr.insert(Uri.parse(REMINDER_URI_STRING), reminderValues);
        return eventID;
    }

    public static long removeCalendarEvent(int eventId, ContentResolver resolver) {
        return resolver.delete(ContentUris.withAppendedId(Uri.parse(EVENT_URI_STR),
                eventId), null, null);
    }
}
