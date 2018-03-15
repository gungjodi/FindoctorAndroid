package com.project.ta.findoctor.Models;

import android.app.Activity;
import android.content.Context;

/**
 * Created by GungJodi on 7/24/2017.
 */

public class DetailDokterModel {
    public long id;
    public long id_klinik;
    public String nama_klinik;
    public String jenis_klinik;
    public double latitude;
    public double longitude;
    public String jarak;
    public String id_hari_list;
    public String jam_buka_list;
    public String jam_tutup_list;
    public int is_buka;
    public Activity activity;

    public DetailDokterModel(Activity activity, long id, long id_klinik, String nama_klinik,String jenis_klinik, double latitude, double longitude,String jarak, String id_hari_list, String jam_buka_list, String jam_tutup_list,int is_buka) {
        this.activity=activity;
        this.id = id;
        this.id_klinik = id_klinik;
        this.nama_klinik = nama_klinik;
        this.jenis_klinik = jenis_klinik;
        this.latitude = latitude;
        this.longitude = longitude;
        this.jarak = jarak;
        this.id_hari_list = id_hari_list;
        this.jam_buka_list = jam_buka_list;
        this.jam_tutup_list = jam_tutup_list;
        this.is_buka = is_buka;
    }
}
