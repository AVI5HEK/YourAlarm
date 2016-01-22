package com.framgia.alarm.model;

/**
 * Created by avishek on 1/21/16.
 */
public class Event {
    private String mEventId;
    private int mAlarmId;
    private String mAccount;

    public String getEventId() {
        return mEventId;
    }

    public String getAccount() {
        return mAccount;
    }

    public Event(String mEventId, int mAlarmId, String mAccount) {
        this.mEventId = mEventId;
        this.mAlarmId = mAlarmId;
        this.mAccount = mAccount;
    }
}
