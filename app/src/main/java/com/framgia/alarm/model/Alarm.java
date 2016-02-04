package com.framgia.alarm.model;

/**
 * Created by avishek on 1/15/16.
 */
public class Alarm {
    private final int mId;
    private final long mTime;
    private final int mStatus;
    private final String mLabel;
    private final String mAlarmToneUri;
    private final String mDaySchedule;
    private final String mContact;

    public Alarm(int mId, long time, int status, String label, String alarmToneUri, String
            daySchedule, String contact) {
        this.mId = mId;
        this.mTime = time;
        this.mStatus = status;
        this.mLabel = label;
        this.mAlarmToneUri = alarmToneUri;
        this.mDaySchedule = daySchedule;
        this.mContact = contact;
    }

    public Alarm(long time, int status, String label, String alarmToneUri, String daySchedule,
                 String contact) {
        this(-1, time, status, label, alarmToneUri, daySchedule, contact);
    }

    public int getId() {
        return mId;
    }

    public long getTime() {
        return mTime;
    }

    public int getStatus() {
        return mStatus;
    }

    public String getLabel() {
        return mLabel;
    }

    public String getAlarmToneUri() {
        return mAlarmToneUri;
    }

    public String getDaySchedule() {
        return mDaySchedule;
    }

    public String getContact() {
        return mContact;
    }
}
