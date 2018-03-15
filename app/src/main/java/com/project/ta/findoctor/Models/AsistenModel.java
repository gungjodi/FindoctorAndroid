package com.project.ta.findoctor.Models;

import android.app.Activity;

/**
 * Created by Mitrais on 10/08/2017.
 */

public class AsistenModel {
    public long id_asisten;
    public String name;
    public String email;
    public int is_aktif;
    public Activity activity;

    public AsistenModel(long id_asisten, String name,String email,int is_aktif, Activity activity) {
        this.id_asisten = id_asisten;
        this.name = name;
        this.email = email;
        this.is_aktif=is_aktif;
        this.activity = activity;
    }
}
