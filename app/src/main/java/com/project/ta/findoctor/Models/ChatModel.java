package com.project.ta.findoctor.Models;

import android.text.format.DateFormat;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by GungJodi on 6/10/2017.
 */

public class ChatModel {
    public String sender;
    public String senderUid;
    public String receiver;
    public String receiverUid;
    public String message;
    public long timestamp;
    public String formattedTime;
    public String readStatus;
    @Exclude
    public String ignoreThisField;

    public ChatModel() {
        sender = "";
        message = "";
        senderUid = "12";
        timestamp = 0;
        readStatus= "false";
    }

    public ChatModel(String sender,String receiver, String senderUid, String receiverUid, String message, long timestamp, String formattedTime,String readStatus) {
        this.sender = sender;
        this.senderUid = senderUid;
        this.receiver = receiver;
        this.receiverUid = receiverUid;
        this.message = message;
        this.timestamp = timestamp;
        this.formattedTime = formattedTime;
        this.readStatus = readStatus;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setTime(long time) {
        this.timestamp = time;

        long oneDayInMillis = 24 * 60 * 60 * 1000;
        long timeDifference = System.currentTimeMillis() - time;

        if(timeDifference < oneDayInMillis){
            formattedTime = DateFormat.format("hh:mm a", time).toString();
        }else{
            formattedTime = DateFormat.format("dd MMM - hh:mm a", time).toString();
        }
    }

    public void setFormattedTime(String formattedTime) {
        this.formattedTime = formattedTime;
    }

    public String getFormattedTime(){
        long oneDayInMillis = 24 * 60 * 60 * 1000;
        long timeDifference = System.currentTimeMillis() - timestamp;

        if(timeDifference < oneDayInMillis){
            return DateFormat.format("hh:mm a", timestamp).toString();
        }else{
            return DateFormat.format("dd MMM - hh:mm a", timestamp).toString();
        }
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("sender", sender);
        result.put("senderUid", senderUid);
        result.put("receiver", receiver);
        result.put("receiverUid", receiverUid);
        result.put("message", message);
        result.put("timestamp", timestamp);
        result.put("formattedTime", formattedTime);
        result.put("readStatus", readStatus);

        return result;
    }

}
