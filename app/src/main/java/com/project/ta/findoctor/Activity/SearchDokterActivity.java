package com.project.ta.findoctor.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.project.ta.findoctor.Adapter.DokterAdapter;
import com.project.ta.findoctor.BuildConfig;
import com.project.ta.findoctor.Interfaces.AsyncResponse;
import com.project.ta.findoctor.Models.SearchDokterModel;
import com.project.ta.findoctor.R;
import com.project.ta.findoctor.Services.DistanceParserService;
import com.project.ta.findoctor.Services.WebService;
import com.project.ta.findoctor.Utils.ItemClickSupport;
import com.project.ta.findoctor.Utils.MethodLib;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SearchDokterActivity extends AppCompatActivity {
    private RecyclerView mRVFish;
    private DokterAdapter mAdapter;
    Button search;
    EditText searchText;
    SearchView searchView = null;
    WebService webService;
    ImageView foto;
    String searchQuery;
    List<SearchDokterModel> data=new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_dokter_new);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        search = (Button) findViewById(R.id.search);
        searchText = (EditText) findViewById(R.id.TFsearch);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                data.clear();
                loadData();
            }
        });

        data.clear();
        loadData();

    }

    public void loadData()
    {
        data.clear();
        searchQuery = searchText.getText().toString();
        String uri = "/searchDokter?search_query=" + searchQuery;
        uri = uri.replaceAll(" ", "%20");
        webService = new WebService(SearchDokterActivity.this, uri, new AsyncResponse() {
            @Override
            public void processFinish(String output) {
                try {

                    JSONArray jArray = new JSONArray(output);

                    // Extract data from json and store into ArrayList as class objects
                    for (int i = 0; i < jArray.length(); i++) {
                        JSONObject json_data = jArray.getJSONObject(i);
                        SearchDokterModel searchDokterClass = new SearchDokterModel();
                        searchDokterClass.id_dokter = json_data.getLong("id");
                        searchDokterClass.name = json_data.getString("name");
                        searchDokterClass.kategori = json_data.getString("kategori");
                        searchDokterClass.id_kategori = json_data.getLong("id_kategori");
                        searchDokterClass.tipe = json_data.getLong("tipe");
                        searchDokterClass.activity = SearchDokterActivity.this;
                        data.add(searchDokterClass);

                    }

                    // Setup and Handover data to recyclerview
                    mRVFish = (RecyclerView) findViewById(R.id.searchdokter_lv);
                    mAdapter = new DokterAdapter(SearchDokterActivity.this, data);
                    mRVFish.setAdapter(mAdapter);
                    mRVFish.setLayoutManager(new LinearLayoutManager(SearchDokterActivity.this));

                    ItemClickSupport.addTo(mRVFish).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                        @Override
                        public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                            Intent intent = new Intent(SearchDokterActivity.this,DetailDokterActivity.class);
                            intent.putExtra("id_dokter",data.get(position).id_dokter);
                            intent.putExtra("nama_dokter",data.get(position).name);
                            intent.putExtra("id_kategori",data.get(position).id_kategori);
                            intent.putExtra("kategori",data.get(position).kategori);
                            startActivity(intent);
                        }
                    });

                } catch (JSONException e) {
                    // You to understand what actually error is and handle it appropriately
//                    Toast.makeText(SearchDokterActivity.this, e.toString(), Toast.LENGTH_LONG).show();
//                    Toast.makeText(SearchDokterActivity.this, output.toString(), Toast.LENGTH_LONG).show();
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
