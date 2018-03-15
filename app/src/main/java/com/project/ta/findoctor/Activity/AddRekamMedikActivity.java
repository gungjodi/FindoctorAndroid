package com.project.ta.findoctor.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.project.ta.findoctor.Interfaces.AsyncResponse;
import com.project.ta.findoctor.R;
import com.project.ta.findoctor.Utils.MethodLib;
import com.project.ta.findoctor.Utils.StringWithTag;
import com.project.ta.findoctor.Services.WebService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AddRekamMedikActivity extends AppCompatActivity {
    private WebService webService;
    private List<StringWithTag> pasienList = new ArrayList<StringWithTag>();
    private List<StringWithTag> klinikList = new ArrayList<StringWithTag>();
    AutoCompleteTextView pasienAutoComplete;
    EditText tanggal;
    Spinner klinikSpinner;
    long id_pasien,user_id,id_klinik,id_pasien_dashboard,id_pasien_dashboard_pos,id_klinik_dashboard,id_klinik_dashboard_pos;
    String user_name,diagnosaVal,penangananVal,keteranganVal,tanggalVal,tanggal_registrasi_dashboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_rekam_medik);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        Intent data = getIntent();
        user_name = data.getStringExtra("user_name");
        user_id = data.getLongExtra("user_id",0);
        id_pasien_dashboard = data.getLongExtra("id_pasien_dashboard",0);
        id_klinik_dashboard = data.getLongExtra("id_klinik_dashboard",0);
        tanggal_registrasi_dashboard = data.getStringExtra("tanggal_registrasi_dashboard");
        tanggal = (EditText) findViewById(R.id.tanggal_periksa_text);
        tanggal.setText(tanggal_registrasi_dashboard);
        if(!tanggal.getText().toString().equals(""))
        {
            tanggal.setEnabled(false);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        pasienAutoComplete = (AutoCompleteTextView) findViewById(R.id.pasien_autocomplete);
        webService = new WebService(AddRekamMedikActivity.this, "/getAllPasien", new AsyncResponse() {
            @Override
            public void processFinish(String res) {
                try {
                    JSONArray jsonArray = new JSONArray(res);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObj = jsonArray.getJSONObject(i);
                        pasienList.add(new StringWithTag(jsonObj.getString("name"),Long.valueOf(jsonObj.getInt("id"))));
                        if(id_pasien_dashboard==Long.valueOf(jsonObj.getInt("id")))
                        {
                            id_pasien_dashboard_pos = i;
                        }
                    }
                    ArrayAdapter<StringWithTag> adap = new ArrayAdapter<StringWithTag> (AddRekamMedikActivity.this, android.R.layout.simple_list_item_1, pasienList);
                    pasienAutoComplete.setAdapter(adap);
                    pasienAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            StringWithTag s = (StringWithTag) parent.getItemAtPosition(position);
                            id_pasien = s.id;
                            System.out.println("ID PASIEN : "+id_pasien);
                        }
                    });
                    if(id_pasien_dashboard!=0)
                    {
                        pasienAutoComplete.setText(pasienList.get((int)id_pasien_dashboard_pos).toString());
                        pasienAutoComplete.setEnabled(false);
                        id_pasien=id_pasien_dashboard;
                        System.out.println("ID PASIEN DASHBOARD: "+id_pasien_dashboard_pos);
                    }
                } catch (JSONException e) {
                    Log.e("ERROR", "Error processing JSON", e);
                }
            }
        });
        webService.execute();



        klinikSpinner = (Spinner) findViewById(R.id.klinik_spinner);
        webService = new WebService(AddRekamMedikActivity.this, "/getKlinikByUser?id_user="+user_id, new AsyncResponse() {
            @Override
            public void processFinish(String res) {
                try {
                    JSONArray jsonArray = new JSONArray(res);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObj = jsonArray.getJSONObject(i);
                        klinikList.add(new StringWithTag(jsonObj.getString("nama_klinik"),Long.valueOf(jsonObj.getInt("id_klinik"))));
                        if(id_klinik_dashboard==Long.valueOf(jsonObj.getInt("id_klinik")))
                        {
                            id_klinik_dashboard_pos = i;
                        }
                    }
                    ArrayAdapter<StringWithTag> adap = new ArrayAdapter<StringWithTag> (AddRekamMedikActivity.this, android.R.layout.simple_spinner_dropdown_item, klinikList);
                    klinikSpinner.setAdapter(adap);
                    klinikSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            StringWithTag s = (StringWithTag) parent.getItemAtPosition(position);
                            id_klinik = s.id;
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });

                    if(id_klinik_dashboard!=0)
                    {
                        klinikSpinner.setSelection((int)id_klinik_dashboard_pos);
                        klinikSpinner.setEnabled(false);
                    }
                } catch (JSONException e) {
                    Log.e("ERROR", "Error processing JSON", e);
                }
            }
        });
        webService.execute();

        EditText userName = (EditText) findViewById(R.id.nama_dokter);


        userName.setText(user_name);
        userName.setEnabled(false);
        Button simpan = (Button) findViewById(R.id.simpanrekam);

        simpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText diagnosa = (EditText) findViewById(R.id.formdiagnosa);
                EditText penanganan = (EditText) findViewById(R.id.formpenanganan);
                EditText keterangan = (EditText) findViewById(R.id.formketerangan);

                diagnosaVal = diagnosa.getText().toString();
                penangananVal = penanganan.getText().toString();
                keteranganVal = keterangan.getText().toString();
                tanggalVal = tanggal.getText().toString();

                diagnosaVal = diagnosaVal.replaceAll(" ", "%20").replaceAll("\n","%0A");
                penangananVal = penangananVal.replaceAll(" ", "%20").replaceAll("\n","%0A");
                keteranganVal = keteranganVal.replaceAll(" ", "%20").replaceAll("\n","%0A");
                WebService webService = new WebService(AddRekamMedikActivity.this,"/tambahRekamMedik?" +
                        "id_dokter="+user_id+
                        "&id_klinik="+id_klinik+
                        "&id_pasien="+id_pasien+
                        "&diagnosa="+diagnosaVal+
                        "&penanganan="+penangananVal+
                        "&keterangan="+keteranganVal+
                        "&tanggal_periksa="+tanggalVal
                        ,new AsyncResponse(){
                    @Override
                    public void processFinish(String output) {
                        if(output!="0")
                        {
                            Intent intent = new Intent();
                            Toast.makeText(AddRekamMedikActivity.this,"Rekam Medik Berhasil Ditambahkan",Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    }
                });
                webService.execute();
            }
        });

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
