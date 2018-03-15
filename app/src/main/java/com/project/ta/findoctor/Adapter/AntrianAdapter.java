package com.project.ta.findoctor.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.project.ta.findoctor.Models.ListAntrianModel;
import com.project.ta.findoctor.Models.RekamMedikModel;
import com.project.ta.findoctor.R;

import java.util.ArrayList;

/**
 * Created by GungJodi on 6/22/2017.
 */

public class AntrianAdapter extends ArrayAdapter<ListAntrianModel> {

    ListAntrianModel listAntrianModel;

    public AntrianAdapter(Context context, ArrayList<ListAntrianModel> listAntrianModelArrayList) {
        super(context, 0, listAntrianModelArrayList);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        listAntrianModel = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.antrian_text_list, parent, false);
        }

//
        TextView noAntrian = (TextView) convertView.findViewById(R.id.no_antrian_lv_text);
        TextView nama = (TextView) convertView.findViewById(R.id.nama_pasien_lv_text);
        TextView statusAntrian = (TextView) convertView.findViewById(R.id.status_antrian_lv_text);
        TextView waktuRegistrasi = (TextView) convertView.findViewById(R.id.waktu_registrasi_lv_text);
        TextView waktuProses = (TextView) convertView.findViewById(R.id.waktu_proses_lv_text);
        TextView waktuSelesai = (TextView) convertView.findViewById(R.id.waktu_selesai_lv_text);

        noAntrian.setText(""+listAntrianModel.no_antrian);
        nama.setText(""+listAntrianModel.nama_pasien);
        statusAntrian.setText(""+listAntrianModel.nama_status);
        waktuRegistrasi.setText(""+listAntrianModel.waktu_registrasi);
        waktuProses.setText(""+listAntrianModel.waktu_proses);
        waktuSelesai.setText(""+listAntrianModel.waktu_selesai);

        return convertView;

    }

}