package com.framgia.alarm.model;

/**
 * Created by avishek on 1/15/16.
 */
public class Alarm {
    private int mId;
    private long mTime;
    private int mStatus;
    private String mLabel;
    private String mAlarm_tone_uri;
    private String mDay_schedule;

    public Alarm(int mId, long time, int status, String label, String alarm_tone_uri, String
            day_schedule) {
        this.mId = mId;
        this.mTime = time;
        this.mStatus = status;
        this.mLabel = label;
        this.mAlarm_tone_uri = alarm_tone_uri;
        this.mDay_schedule = day_schedule;
    }

    public Alarm(long time, int status, String label, String alarm_tone_uri, String day_schedule) {
        this.mTime = time;
        this.mStatus = status;
        this.mLabel = label;
        this.mAlarm_tone_uri = alarm_tone_uri;
        this.mDay_schedule = day_schedule;
    }

    public int getmId() {
        return mId;
    }

    public long getmTime() {
        return mTime;
    }

    public int getmStatus() {
        return mStatus;
    }

    public String getmLabel() {
        return mLabel;
    }

    public String getmAlarm_tone_uri() {
        return mAlarm_tone_uri;
    }

    public String getmDay_schedule() {
        return mDay_schedule;
    }
}
