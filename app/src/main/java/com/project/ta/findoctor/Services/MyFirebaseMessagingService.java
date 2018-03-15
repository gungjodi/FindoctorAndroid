package com.project.ta.findoctor.Services;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.project.ta.findoctor.Activity.ChatUserActivity;
import com.project.ta.findoctor.Activity.MenuActivity;
import com.project.ta.findoctor.R;
import com.project.ta.findoctor.Utils.Constants;

import java.util.UUID;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FCM Service";
    private FirebaseUser user;
    Intent chatIntent;
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        user = FirebaseAuth.getInstance().getCurrentUser();

        String senderName = remoteMessage.getData().get("senderName");
        String title = "Message from "+senderName;
        String notif_tipe = remoteMessage.getData().get("notif_tipe");
        Uri sound= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        if(notif_tipe.equals(Constants.NOTIFIKASI_ANTRIAN))
        {
            chatIntent = new Intent(this, MenuActivity.class);
        }
        else
        {
            long recipient_id = Long.valueOf(remoteMessage.getData().get("recipient_id"));
            long sender_id = Long.valueOf(remoteMessage.getData().get("sender_id"));
            String receiverName = remoteMessage.getData().get("receiver_name");
            chatIntent = new Intent(this, ChatUserActivity.class);
            chatIntent.putExtra("receiverUid",sender_id);
            chatIntent.putExtra("receiverName",senderName);
            chatIntent.putExtra("userId",recipient_id);
            chatIntent.putExtra("userName",receiverName);
        }

        chatIntent.setAction(Intent.ACTION_MAIN);
        chatIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, UUID.randomUUID().hashCode(),chatIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle(title)
                .setContentText(remoteMessage.getData().get("body"))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setSound(sound)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();
        if(user!=null)
        {
            NotificationManagerCompat manager = NotificationManagerCompat.from(getApplicationContext());
            manager.notify(123, notification);
        }

    }
}
