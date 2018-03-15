package com.project.ta.findoctor.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.project.ta.findoctor.Adapter.RekamMedikAdapter;
import com.project.ta.findoctor.Interfaces.AsyncResponse;
import com.project.ta.findoctor.Services.MyFirebaseApp;
import com.project.ta.findoctor.R;
import com.project.ta.findoctor.Models.RekamMedikModel;
import com.project.ta.findoctor.Services.WebService;
import com.project.ta.findoctor.Utils.MethodLib;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class RekamMedikActivity extends AppCompatActivity {
    private ActionBarActivity mClass;
    private FirebaseUser user;
    private WebService webService;
    private long user_id,akses,id_dokter;
    String user_name,nama_dokter,tanggal_registrasi_dashboard;
    int ADD_REKAM_MEDIK_ACTIVITY=1;
    Button search;
    EditText searchText;
    String searchQuery;
    ArrayList<RekamMedikModel> data=new ArrayList<>();
    FloatingActionButton fab;
    long id_pasien_dashboard,id_klinik_dashboard;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rekam_medik);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.add_rekam_medik);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent detail = getIntent();
        id_pasien_dashboard = detail.getLongExtra("id_pasien_dashboard",0);
        id_klinik_dashboard = detail.getLongExtra("id_klinik_dashboard",0);
        tanggal_registrasi_dashboard = detail.getStringExtra("tanggal_registrasi_dashboard");

        search = (Button) findViewById(R.id.search);
        searchText = (EditText) findViewById(R.id.TFsearch);

        data.clear();
        getData();

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                data.clear();
                getData();
            }
        });
    }

    public void getData()
    {
        user = FirebaseAuth.getInstance().getCurrentUser();
        searchQuery = searchText.getText().toString();
        data.clear();

        if (user != null)
        {
            user_id = ((MyFirebaseApp)RekamMedikActivity.this.getApplication()).getUser_id();
            akses = ((MyFirebaseApp)RekamMedikActivity.this.getApplication()).getUser_tipe();
            user_name = ((MyFirebaseApp)RekamMedikActivity.this.getApplication()).getUser_name();
            if(akses==2)
            {
                fab.setVisibility(View.INVISIBLE);
            }
            if(akses==3)
            {
                id_dokter = ((MyFirebaseApp)RekamMedikActivity.this.getApplication()).getId_dokter();
                nama_dokter = ((MyFirebaseApp)RekamMedikActivity.this.getApplication()).getNama_dokter();
            }
            String uri = "/searchRekamMedik?name=" + searchQuery + "&id_user=" + user_id + "&akses="+akses;
            if(id_pasien_dashboard!=0)
            {
                uri = "/searchRekamMedik?name=" + searchQuery + "&id_user=" + user_id + "&akses="+akses+"&id_pasien_dashboard="+id_pasien_dashboard;
            }
            webService = new WebService(RekamMedikActivity.this, uri, new AsyncResponse() {
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
                                long id_rekam=jsonObj.getLong("id");
                                long id_dokter=jsonObj.getLong("id_dokter");
                                long id_klinik=jsonObj.getLong("id_klinik");
                                long id_pasien=jsonObj.getLong("id_pasien");
                                String diagnosa=jsonObj.getString("diagnosa");
                                String tanggal_periksa=jsonObj.getString("tanggal_periksa");
                                String penanganan=jsonObj.getString("penanganan");
                                String keterangan=jsonObj.getString("keterangan");
                                String nama_klinik=jsonObj.getString("nama_klinik");
                                String nama_user=jsonObj.getString("name");

                                RekamMedikModel rekamMedikClass = new RekamMedikModel(RekamMedikActivity.this,id_dokter,id_klinik,id_pasien,
                                        diagnosa,tanggal_periksa,penanganan,keterangan,nama_klinik,nama_user,id_rekam);
                                data.add(rekamMedikClass);
                            }

                            RekamMedikAdapter adapter = new RekamMedikAdapter(RekamMedikActivity.this, data);
                            // Attach the adapter to a ListView
                            ListView listView = (ListView) findViewById(R.id.rekam_medik_list);
                            listView.setAdapter(adapter);
                            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    long id_rekam = data.get(position).id_rekam;
                                    Intent detail_rekam = new Intent(RekamMedikActivity.this,DetailRekamMedikActivity.class);
                                    detail_rekam.putExtra("id_rekam",id_rekam);
                                    startActivity(detail_rekam);
                                }
                            });

                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            webService.execute();
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent addRekamMedik = new Intent(RekamMedikActivity.this,AddRekamMedikActivity.class);
                    if(akses==3)
                    {
                        addRekamMedik.putExtra("user_id",id_dokter);
                        addRekamMedik.putExtra("user_name",nama_dokter);
                        addRekamMedik.putExtra("id_pasien_dashboard",id_pasien_dashboard);
                        addRekamMedik.putExtra("id_klinik_dashboard",id_klinik_dashboard);
                        addRekamMedik.putExtra("tanggal_registrasi_dashboard",tanggal_registrasi_dashboard);
                    }
                    else
                    {
                        addRekamMedik.putExtra("user_id",user_id);
                        addRekamMedik.putExtra("user_name",user_name);
                        addRekamMedik.putExtra("id_pasien_dashboard",id_pasien_dashboard);
                        addRekamMedik.putExtra("id_klinik_dashboard",id_klinik_dashboard);
                        addRekamMedik.putExtra("tanggal_registrasi_dashboard",tanggal_registrasi_dashboard);
                    }

                    startActivityForResult(addRekamMedik,ADD_REKAM_MEDIK_ACTIVITY);

                }
            });
        }
        else
        {
            finish();
            Intent i = new Intent(RekamMedikActivity.this, AccountActivity.class);
            startActivity(i);
        }
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

    public void onActivityResult(int requestCode,int resultCode,Intent data)
    {
        if(requestCode==ADD_REKAM_MEDIK_ACTIVITY)
        {
            if(resultCode==RESULT_OK)
            {
                getData();
            }
        }
    }
}