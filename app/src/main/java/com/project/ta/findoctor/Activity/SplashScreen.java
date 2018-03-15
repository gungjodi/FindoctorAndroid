package com.project.ta.findoctor.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.project.ta.findoctor.Activity.MenuActivity;

public class SplashScreen extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(this, MenuActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

}