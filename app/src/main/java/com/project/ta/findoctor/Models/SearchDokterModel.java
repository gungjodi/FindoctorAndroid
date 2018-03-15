package com.project.ta.findoctor.Models;

import android.app.Activity;

/**
 * Created by Anisa on 3/13/2017.
 */
    public class SearchDokterModel {
        public long id_dokter;
        public long id_kategori;
        public long tipe;
        public String name;
        public String kategori;
        public Activity activity;
        public SearchDokterModel() {
            this.id_dokter = id_dokter;
            this.id_kategori = id_kategori;
            this.tipe = tipe;
            this.name = name;
            this.kategori = kategori;
        }
    }