package com.project.ta.findoctor.Models;

/**
 * Created by GungJodi on 6/18/2017.
 */


public class DokterInKlinikModel
{
    public long id_klinik_dokter;
    public long id_dokter;
    public String nama_dokter;
    public long id_kategori;
    public String kategori;

    public DokterInKlinikModel() {
        super();
    }

    public DokterInKlinikModel(long id_klinik_dokter, long id_dokter, String nama_dokter, long id_kategori, String kategori) {
        this.id_klinik_dokter = id_klinik_dokter;
        this.id_dokter = id_dokter;
        this.nama_dokter = nama_dokter;
        this.id_kategori = id_kategori;
        this.kategori = kategori;
    }

    @Override
    public String toString() {
        return nama_dokter + " - "+ kategori;
    }
}