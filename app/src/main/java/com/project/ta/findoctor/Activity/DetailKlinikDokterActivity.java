package com.project.ta.findoctor.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.project.ta.findoctor.Adapter.WaktuPraktikAdapter;
import com.project.ta.findoctor.Interfaces.AsyncResponse;
import com.project.ta.findoctor.Utils.IdHariSorter;
import com.project.ta.findoctor.Models.WaktuPraktikModel;
import com.project.ta.findoctor.Services.MyFirebaseApp;
import com.project.ta.findoctor.R;
import com.project.ta.findoctor.Services.WebService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;

public class DetailKlinikDokterActivity extends AppCompatActivity {
    WebService webService;
    long user_id,id_klinik_dokter,user_tipe,id_dokter;
    int ADD_WAKTU_ACTIVITY = 15;

    ArrayList<WaktuPraktikModel> waktu_praktik = new ArrayList<>();

    String no_antrian,antrian_terakhir,my_antrian,rata_antrian;
    String nama_dokter,kategori;
    FloatingActionButton fab;
    SwipeRefreshLayout swipeContainer;

    void showToast(CharSequence msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_klinik_dokter);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent detail = getIntent();
        user_id = ((MyFirebaseApp)DetailKlinikDokterActivity.this.getApplication()).getUser_id();
        id_dokter = detail.getLongExtra("id_dokter", 0);
        user_tipe = detail.getLongExtra("user_tipe", 0);
        id_klinik_dokter = detail.getLongExtra("id_klinik_dokter", 0);
        nama_dokter = detail.getStringExtra("nama_dokter");
        kategori = detail.getStringExtra("kategori");
        TextView nama_dokternya = (TextView) findViewById(R.id.nama_dokternya);
        TextView kategori_dokter = (TextView) findViewById(R.id.kategorinya);

        nama_dokternya.setText(nama_dokter);
        kategori_dokter.setText(kategori);

        start();

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swiperefreshwaktupraktik);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                start();
            }
        });

    }

    public void start()
    {
        waktu_praktik.clear();
        final String hari_now = ((MyFirebaseApp)DetailKlinikDokterActivity.this.getApplication()).getDate_now();

        webService = new WebService(DetailKlinikDokterActivity.this, "/getAntrianAktif/"+id_klinik_dokter, new AsyncResponse() {
            @Override
            public void processFinish(String output) {
                String json = output;
                no_antrian = "Antrian aktif : "+json;
            }
        });
        webService.execute();

        webService = new WebService(DetailKlinikDokterActivity.this, "/getAntrianTerakhir/"+id_klinik_dokter, new AsyncResponse() {
            @Override
            public void processFinish(String output) {
                String json = output;
                antrian_terakhir = "Antrian terakhir : "+json;

            }
        });
        webService.execute();

        webService = new WebService(DetailKlinikDokterActivity.this, "/getMyAntrian/"+id_klinik_dokter+"/"+user_id, new AsyncResponse() {
            @Override
            public void processFinish(String output) {
                String json = output;
                my_antrian = json;
            }
        });
        webService.execute();

        if(ADD_WAKTU_ACTIVITY==15)
        {
            webService = new WebService(DetailKlinikDokterActivity.this, "/getHariBuka?id_klinik_dokter="+id_klinik_dokter, new AsyncResponse() {
                @Override
                public void processFinish(String output) {
                    String json = output;
                    if(json!="0")
                    {
                        try {
                            // De-serialize the JSON string into an array of city objects
                            JSONArray jsonArray = new JSONArray(json);
                            for (int i = 0; i < jsonArray.length(); i++)
                            {
                                final JSONObject jsonObj = jsonArray.getJSONObject(i);
                                String id_hari = jsonObj.getString("id_hari");
                                String hari = jsonObj.getString("nama_hari");
                                String jam_buka=jsonObj.getString("jam_buka");
                                String jam_tutup=jsonObj.getString("jam_tutup");
                                String[] id_hari_arr = id_hari.split(",");
                                String[] hari_buka = hari.split(",");
                                String[] jam_buka_arr = jam_buka.split("#");
                                String[] jam_tutup_arr = jam_tutup.split("#");
                                for(int j=0;j<hari_buka.length;j++)
                                {
                                    if(hari_buka[j]!="null")
                                    {
                                        WaktuPraktikModel waktu = new WaktuPraktikModel(DetailKlinikDokterActivity.this,user_id,id_klinik_dokter,Long.valueOf(id_hari_arr[j]),user_tipe,hari_buka[j],jam_buka_arr[j],jam_tutup_arr[j],hari_now,no_antrian,antrian_terakhir,my_antrian);
                                        waktu_praktik.add(waktu);
                                    }
                                }
                            }

                            Collections.sort(waktu_praktik,new IdHariSorter());
                            WaktuPraktikAdapter adapter = new WaktuPraktikAdapter(DetailKlinikDokterActivity.this, waktu_praktik);
                            // Attach the adapter to a ListView
                            ListView listView = (ListView) findViewById(R.id.hari_list_view);
                            listView.setAdapter(adapter);
                            swipeContainer.setRefreshing(false);

                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            webService.execute();
        }

        webService = new WebService(DetailKlinikDokterActivity.this, "/getRataAntrian/"+id_klinik_dokter, new AsyncResponse() {
            @Override
            public void processFinish(String output) {
                String json = output;
                rata_antrian = json;
                TextView rataAntrian = (TextView) findViewById(R.id.rata_antrian_text);
                rataAntrian.setText(rata_antrian);
            }
        });
        webService.execute();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addWaktu = new Intent(DetailKlinikDokterActivity.this,AddWaktuActivity.class);
                addWaktu.putExtra("id_klinik_dokter",id_klinik_dokter);
                addWaktu.putExtra("waktu_praktik",waktu_praktik);
                addWaktu.putExtra("user_id",user_id);
                addWaktu.putExtra("user_tipe",user_tipe);
                startActivityForResult(addWaktu,ADD_WAKTU_ACTIVITY);
            }
        });

        if(user_id!=id_dokter)
        {
            fab.setVisibility(View.INVISIBLE);
        }

    }

    public void onActivityResult(int requestCode,int resultCode,Intent data)
    {
        if(requestCode==ADD_WAKTU_ACTIVITY)
        {
            if(resultCode==RESULT_OK)
            {
                waktu_praktik = (ArrayList<WaktuPraktikModel>) data.getSerializableExtra("waktu_new");
                WaktuPraktikAdapter adapter = new WaktuPraktikAdapter(DetailKlinikDokterActivity.this, waktu_praktik);

                ListView listView = (ListView) findViewById(R.id.hari_list_view);

                listView.setAdapter(adapter);
                JSONArray jsonArray = new JSONArray();
                for (int i=0; i < waktu_praktik.size(); i++) {
                    jsonArray.put(waktu_praktik.get(i).getJSONObject());
                }
                String waktu_array = jsonArray.toString();
                try {
                    String URL_ARRAY = URLEncoder.encode(waktu_array, "UTF-8");
                    webService = new WebService(DetailKlinikDokterActivity.this, "/saveWaktuPraktik?waktu_json="+ URL_ARRAY,
                            new AsyncResponse() {
                                @Override
                                public void processFinish(String output) {
                                    String json = output;
                                }
                            });
                    webService.execute();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail_dokter_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.reload_detail_dokter) {
            start();
            return true;
        }
        else if(id==android.R.id.home)
        {
            onBackPressed();
            return true;
        }


        return super.onOptionsItemSelected(item);
    }
}