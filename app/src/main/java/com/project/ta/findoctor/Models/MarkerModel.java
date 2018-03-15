package com.project.ta.findoctor.Models;

/**
 * Created by GungJodi on 6/18/2017.
 */

public class MarkerModel {
    public String nama;
    public String jenis_klinik;
    public long id_jenis_klinik;
    public long id_pengelola;
    public double latitude;
    public double longitude;
    public String nama_pengelola;
    public long id;
    public String dokter_klinik_id;
    public String dokters_str;
    public String dokters_id;
    public String kategoris_str;
    public String kategoris_id;

    public MarkerModel(String nama_in, String jenis,
                       int id_jenis, long id_p, String nama_p, long id_marker, String dokters,
                       String kategoris, String id_dokters, String id_kats,double latitude,double longitude, String klinik_id)
    {
        nama = nama_in;
        jenis_klinik = jenis;
        id_jenis_klinik = id_jenis;
        id_pengelola = id_p;
        nama_pengelola = nama_p;
        id = id_marker;
        dokters_str=dokters;
        dokters_id=id_dokters;
        kategoris_str=kategoris;
        kategoris_id=id_kats;
        latitude=latitude;
        longitude=longitude;
        dokter_klinik_id=klinik_id;
    }
}