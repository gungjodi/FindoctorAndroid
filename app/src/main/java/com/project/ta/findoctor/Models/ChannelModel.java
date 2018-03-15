package com.project.ta.findoctor.Models;

import android.app.Activity;

/**
 * Created by GungJodi on 6/13/2017.
 */

public class ChannelModel
{
    public long receiverUid;
    public String receiverName;
    public String receiverEmail;
    public long senderID;
    public String readStatus;
    public Activity activity;
    public ChannelModel(long receiverUid, String receiverName, String receiverEmail,long senderID,String readStatus, Activity activity) {
        this.receiverUid = receiverUid;
        this.receiverName = receiverName;
        this.receiverEmail = receiverEmail;
        this.senderID = senderID;
        this.readStatus = readStatus;
        this.activity=activity;
    }
}