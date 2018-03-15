package com.project.ta.findoctor.Models;

/**
 * Created by GungJodi on 6/22/2017.
 */

public class ListAntrianModel {
    public long id;
    public long id_klinik_dokter;
    public long id_pasien;
    public long no_antrian;
    public long status_antrian;
    public String waktu_registrasi;
    public String waktu_proses;
    public String waktu_selesai;
    public String nama_pasien;
    public String nama_status;
    public String tanggal_registrasi;

    public ListAntrianModel(long id, long id_klinik_dokter, long id_pasien, long no_antrian,
                            long status_antrian, String waktu_registrasi, String waktu_proses,
                            String waktu_selesai, String nama_pasien, String nama_status,String tanggal_registrasi)
    {
        this.id = id;
        this.id_klinik_dokter = id_klinik_dokter;
        this.id_pasien = id_pasien;
        this.no_antrian = no_antrian;
        this.status_antrian = status_antrian;
        this.waktu_registrasi = waktu_registrasi;
        this.waktu_proses = waktu_proses;
        this.waktu_selesai = waktu_selesai;
        this.nama_pasien = nama_pasien;
        this.nama_status = nama_status;
        this.tanggal_registrasi=tanggal_registrasi;
    }
}
