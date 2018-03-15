package com.project.ta.findoctor.Models;

import android.app.Activity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import static io.fabric.sdk.android.services.network.HttpRequest.trace;

/**
 * Created by mac on 2/18/17.
 */

public class WaktuPraktikModel implements Serializable, Comparable<WaktuPraktikModel>{
    public long id_klinik_dokter;
    public long user_id;
    public long id_hari;
    public String hari;
    public String jam_buka;
    public String jam_tutup;
    public String date_now;
    public String no_antrian;
    public String antrian_terakhir;
    public String my_antrian;
    public long user_tipe;
    public transient Activity activity;

    public WaktuPraktikModel(Activity activity, long user_id, long id_klinik_dokter, long id_hari, long user_tipe, String hari, String jam_buka, String jam_tutup, String date_now, String no_antrian, String antrian_terakhir, String my_antrian) {
        this.id_klinik_dokter = id_klinik_dokter;
        this.id_hari = id_hari;
        this.hari = hari;
        this.jam_buka = jam_buka;
        this.jam_tutup = jam_tutup;
        this.date_now=date_now;
        this.user_tipe=user_tipe;
        this.user_id=user_id;
        this.no_antrian=no_antrian;
        this.activity = activity;
        this.antrian_terakhir=antrian_terakhir;
        this.my_antrian=my_antrian;
    }

    @Override
    public int compareTo(WaktuPraktikModel o) {
        return 0;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        WaktuPraktikModel other = (WaktuPraktikModel) obj;
        if (id_hari != other.id_hari)
            return false;
        return true;
    }

    public JSONObject getJSONObject() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("id_klinik_dokter", this.id_klinik_dokter);
            obj.put("id_hari", this.id_hari);
            obj.put("hari", this.hari);
            obj.put("jam_buka", this.jam_buka);
            obj.put("jam_tutup", this.jam_tutup);
            obj.put("date_now", this.date_now);
        } catch (JSONException e) {
            trace("DefaultListItem.toString JSONException: "+e.getMessage());
        }
        return obj;
    }

}
