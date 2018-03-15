package com.project.ta.findoctor.Models;

import android.app.Activity;

/**
 * Created by mac on 2/28/17.
 */

public class RekamMedikModel {
    public long id_rekam;
    public long id_dokter;
    public long id_klinik;
    public long id_pasien;
    public String diagnosa;
    public String tanggal_periksa;
    public String penanganan;
    public String keterangan;
    public String nama_klinik;
    public String nama_user;
    public Activity activity;

    public RekamMedikModel(Activity activity, long id_dokter, long id_klinik, long id_pasien, String diagnosa, String tanggal_periksa, String penanganan,
                           String keterangan, String nama_klinik, String nama_user, long id_rekam) {
        this.id_dokter = id_dokter;
        this.id_klinik = id_klinik;
        this.id_pasien = id_pasien;
        this.diagnosa = diagnosa;
        this.tanggal_periksa = tanggal_periksa;
        this.penanganan = penanganan;
        this.keterangan = keterangan;
        this.nama_klinik = nama_klinik;
        this.nama_user = nama_user;
        this.id_rekam = id_rekam;
        this.activity=activity;
    }
}
