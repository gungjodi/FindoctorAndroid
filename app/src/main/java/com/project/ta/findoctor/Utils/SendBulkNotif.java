package com.project.ta.findoctor.Utils;

import com.project.ta.findoctor.Interfaces.AsyncResponse;
import com.project.ta.findoctor.Services.BackgroundWebService;
import com.project.ta.findoctor.Services.SendChatNotif;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Mitrais on 7/08/2017.
 */

public class SendBulkNotif {
    private static JSONObject message;
    public static void send(long recipient_id,long sender_id,String senderName,String receiver_name, String message_body) {
        try
        {
            message = new JSONObject();
            message.put("senderName",senderName);
            message.put("sender_id",sender_id);
            message.put("recipient_id",recipient_id);
            message.put("receiver_name",receiver_name);
            message.put("notif_tipe",Constants.NOTIFIKASI_CHAT);
            message.put("body",message_body);
            BackgroundWebService request = new BackgroundWebService("/getFirebaseToken/" + recipient_id, new AsyncResponse() {
                @Override
                public void processFinish(String output) {
                    try
                    {
                        JSONArray jsonArray = new JSONArray(output);
                        for(int i=0;i<jsonArray.length();i++)
                        {
                            JSONObject data = jsonArray.getJSONObject(i);
                            String firebase_token = data.getString("firebase_id");
                            SendChatNotif sendChatNotif = new SendChatNotif(message, firebase_token, new AsyncResponse() {
                                @Override
                                public void processFinish(String output) {

                                }
                            });
                            sendChatNotif.execute();
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            });
            request.execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
