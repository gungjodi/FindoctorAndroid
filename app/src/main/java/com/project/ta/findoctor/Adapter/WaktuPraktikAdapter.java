package com.project.ta.findoctor.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.project.ta.findoctor.Interfaces.AsyncResponse;
import com.project.ta.findoctor.Utils.MethodLib;
import com.project.ta.findoctor.Models.WaktuPraktikModel;
import com.project.ta.findoctor.R;
import com.project.ta.findoctor.Services.WebService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by GungJodi on 6/18/2017.
 */

public class WaktuPraktikAdapter extends ArrayAdapter<WaktuPraktikModel> {

    WaktuPraktikModel waktu;

    public WaktuPraktikAdapter(Context context, ArrayList<WaktuPraktikModel> users) {
        super(context, 0, users);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        waktu = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.waktu_praktik_list, parent, false);
        }
        try {
            final Date datenow = new SimpleDateFormat("yyyy-MM-dd").parse(waktu.date_now);
            Calendar c = Calendar.getInstance();
            c.setTime(datenow);
            int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
            long id_hari = waktu.id_hari;
            convertView.setBackgroundResource(R.color.tw__composer_red);
            TextView noAntrianText = (TextView) convertView.findViewById(R.id.text_antrian_inlist);
            TextView antrianTerakhirText = (TextView) convertView.findViewById(R.id.antrian_terakhir_inlist);
            TextView myAntrianText = (TextView) convertView.findViewById(R.id.my_antrian_text);
            if(id_hari==dayOfWeek)
            {
                convertView.setBackgroundResource(R.color.tw__composer_blue);
                if(waktu.user_tipe==2)
                {
                    noAntrianText.setText(waktu.no_antrian);
                    antrianTerakhirText.setText(waktu.antrian_terakhir);
                    myAntrianText.setText(waktu.my_antrian);
                    convertView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            WebService webService = new WebService(waktu.activity, "/getStatusBuka/"+waktu.id_klinik_dokter+"/"+waktu.id_hari,
                                    new AsyncResponse() {
                                        @Override
                                        public void processFinish(String output) {
                                            String json = output;
                                            if(json.equals("0"))
                                            {
                                                MethodLib.showToast(waktu.activity,"Klinik sudah tutup. Silakan mendaftar kembali di jam operasional klinik.");
                                            }
                                            else
                                            {
                                                WebService webService2 = new WebService(waktu.activity, "/register_antrian?id_klinik_dokter="+waktu.id_klinik_dokter+"&id_user="+waktu.user_id,
                                                        new AsyncResponse() {
                                                            @Override
                                                            public void processFinish(String output) {
                                                                String json = output;
                                                                MethodLib.showToast(waktu.activity,json);
                                                                System.out.println(output);
                                                            }
                                                        });
                                                webService2.execute();
                                            }

                                            System.out.println(output);
                                        }
                                    });
                            webService.execute();

                        }
                    });
                }

            }
        } catch (ParseException e) {
            e.printStackTrace();
        }


        TextView hari_buka = (TextView) convertView.findViewById(R.id.hari_buka_text);
        TextView jam_buka = (TextView) convertView.findViewById(R.id.jam_buka_text);
        TextView jam_tutup = (TextView) convertView.findViewById(R.id.jam_tutup_text);

        // Populate the data into the template view using the data object
        hari_buka.setText(waktu.hari);
        jam_buka.setText(waktu.jam_buka);
        jam_tutup.setText(waktu.jam_tutup);


        return convertView;

    }

}