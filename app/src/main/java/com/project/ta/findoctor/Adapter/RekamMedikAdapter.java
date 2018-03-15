package com.project.ta.findoctor.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.project.ta.findoctor.Models.RekamMedikModel;
import com.project.ta.findoctor.R;

import java.util.ArrayList;

/**
 * Created by GungJodi on 6/18/2017.
 */

public class RekamMedikAdapter extends ArrayAdapter<RekamMedikModel> {

    RekamMedikModel rekamMedikClass;

    public RekamMedikAdapter(Context context, ArrayList<RekamMedikModel> rekamMedikClassArrayList) {
        super(context, 0, rekamMedikClassArrayList);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        rekamMedikClass = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.rekam_medik_lv, parent, false);
        }


        TextView klinik_dokter = (TextView) convertView.findViewById(R.id.klinik_dokter_text);
        TextView nama = (TextView) convertView.findViewById(R.id.nama_text);
        TextView diagnosa = (TextView) convertView.findViewById(R.id.diagnosa_text);
        TextView tgl_rekam = (TextView) convertView.findViewById(R.id.tgl_rekam_text);

        // Populate the data into the template view using the data object
        klinik_dokter.setText(rekamMedikClass.nama_klinik);
        nama.setText(rekamMedikClass.nama_user);
        diagnosa.setText(rekamMedikClass.diagnosa);
        tgl_rekam.setText(rekamMedikClass.tanggal_periksa);

        return convertView;

    }

}