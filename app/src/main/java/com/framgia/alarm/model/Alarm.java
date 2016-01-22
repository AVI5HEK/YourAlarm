package com.framgia.alarm.model;

/**
 * Created by avishek on 1/15/16.
 */
public class Alarm {
    private int mId;
    private long mTime;
    private int mStatus;
    private String mLabel;
    private String mAlarmToneUri;
    private String mDaySchedule;
    public Alarm(int mId, long time, int status, String label, String alarmToneUri, String
            daySchedule) {
        this.mId = mId;
        this.mTime = time;
        this.mStatus = status;
        this.mLabel = label;
        this.mAlarmToneUri = alarmToneUri;
        this.mDaySchedule = daySchedule;
    }

    public Alarm(long time, int status, String label, String alarmToneUri, String daySchedule) {
        this.mTime = time;
        this.mStatus = status;
        this.mLabel = label;
        this.mAlarmToneUri = alarmToneUri;
        this.mDaySchedule = daySchedule;
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
}
