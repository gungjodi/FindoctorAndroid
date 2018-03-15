package com.project.ta.findoctor.Services;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.project.ta.findoctor.Activity.MenuActivity;

public class FirebaseIDService extends FirebaseInstanceIdService {

    private static final String TAG = "FirebaseIDService";

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        System.out.println("TOKEN : "+refreshedToken);
        ((MyFirebaseApp)this.getApplication()).setFirebase_token(refreshedToken);
        sendRegistrationToServer(refreshedToken);
    }

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        // Add custom implementation, as needed.
    }
}
