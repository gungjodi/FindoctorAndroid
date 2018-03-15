package com.project.ta.findoctor.Services;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class MyFirebaseApp extends Application {

    String email="-";
    String user_name="Guest";
    long user_id=0;
    long id_dokter=0;
    long user_tipe=0;

    String nama_dokter="";
    String notlp="-";
    String tempatlahir="-";
    String tanggallahir="-";
    String nama_kategori="-";
    String date_now = "";
    String document = "";
    String firebase_token = "";
    String device_model = "";
    int valid = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

    }

    public String getUser_name() {
        return user_name;
    }
    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public long getUser_id() {
        return user_id;
    }
    public void setUser_id(long user_id) {
        this.user_id = user_id;
    }

    public String getNotlp() {
        return notlp;
    }
    public void setNotlp(String notlp) {
        this.notlp = notlp;
    }

    public String getTempatlahir() {
        return tempatlahir;
    }
    public void setTempatlahir(String tempatlahir) {
        this.tempatlahir = tempatlahir;
    }

    public String getTanggallahir() {
        return tanggallahir;
    }
    public void setTanggallahir(String tanggallahir) {
        this.tanggallahir = tanggallahir;
    }

    public long getUser_tipe() {
        return user_tipe;
    }
    public void setUser_tipe(long user_tipe) {
        this.user_tipe = user_tipe;
    }

    public int getValid() {
        return valid;
    }
    public void setValid(int valid) {
        this.valid = valid;
    }

    public String getNama_kategori() {
        return nama_kategori;
    }
    public void setNama_kategori(String nama_kategori) {
        this.nama_kategori = nama_kategori;
    }

    public String getDate_now() {
        return date_now;
    }
    public void setDate_now(String date_now) {
        this.date_now = date_now;
    }

    public void setDocument(String document) {
        this.document = document;
    }
    public String getDocument() {
        return document;
    }

    public void setId_dokter(long id_dokter) {
        this.id_dokter= id_dokter;
    }
    public long getId_dokter()
    {
        return id_dokter;
    }

    public String getNama_dokter() {
        return nama_dokter;
    }
    public void setNama_dokter(String nama_dokter) {
        this.nama_dokter = nama_dokter;
    }

    public String getFirebase_token() {
        return firebase_token;
    }
    public void setFirebase_token(String firebase_token) {
        this.firebase_token = firebase_token;
    }

    public String getDevice_model() {
        return device_model;
    }

    public void setDevice_model(String device_model) {
        this.device_model = device_model;
    }
}
