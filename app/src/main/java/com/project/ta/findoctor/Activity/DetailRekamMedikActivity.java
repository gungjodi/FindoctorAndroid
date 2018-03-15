package com.project.ta.findoctor.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.project.ta.findoctor.Interfaces.AsyncResponse;
import com.project.ta.findoctor.R;
import com.project.ta.findoctor.Services.WebService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DetailRekamMedikActivity extends AppCompatActivity {

    WebService webService;
    String nama_klinik,nama_pasien,nama_dokter,tanggal_periksa, diagnosa,penanganan,keterangan;
    EditText nama_klinik_text,nama_pasien_text,nama_dokter_text,tanggal_periksa_text, diagnosa_text,penanganan_text,keterangan_text;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detil_rekam_medik);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent detail = getIntent();
        long id_rekam = detail.getLongExtra("id_rekam",0);

        webService = new WebService(DetailRekamMedikActivity.this,"/getDetailRekamMedik?id_rekam="+id_rekam, new AsyncResponse() {
            @Override
            public void processFinish(String json) {
                if(json!="0")
                {
                    try {
                        // De-serialize the JSON string into an array of city objects
                        JSONArray jsonArray = new JSONArray(json);
                        for (int i = 0; i < jsonArray.length(); i++)
                        {
                            final JSONObject jsonObj = jsonArray.getJSONObject(i);
                            nama_klinik=jsonObj.getString("nama_klinik");
                            nama_pasien=jsonObj.getString("nama_pasien");
                            nama_dokter=jsonObj.getString("nama_dokter");
                            tanggal_periksa=jsonObj.getString("tanggal_periksa");
                            diagnosa=jsonObj.getString("diagnosa");
                            penanganan=jsonObj.getString("penanganan");
                            keterangan=jsonObj.getString("keterangan");
                        }
                        nama_klinik_text = (EditText) findViewById(R.id.editText);
                        nama_klinik_text.setText(nama_klinik);
                        nama_pasien_text = (EditText) findViewById(R.id.editText3);
                        nama_pasien_text.setText(nama_pasien);
                        nama_dokter_text= (EditText) findViewById(R.id.editText2);
                        nama_dokter_text.setText(nama_dokter);
                        tanggal_periksa_text= (EditText) findViewById(R.id.editText4);
                        tanggal_periksa_text.setText(tanggal_periksa);
                        diagnosa_text= (EditText) findViewById(R.id.editText5);
                        diagnosa_text.setText(diagnosa);
                        penanganan_text= (EditText) findViewById(R.id.editText6);
                        penanganan_text.setText(penanganan);
                        keterangan_text= (EditText) findViewById(R.id.editText7);
                        keterangan_text.setText(keterangan);
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }
        });
        webService.execute();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
