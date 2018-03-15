package com.project.ta.findoctor.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.project.ta.findoctor.Adapter.DokterAdapter;
import com.project.ta.findoctor.Interfaces.AsyncResponse;
import com.project.ta.findoctor.Models.SearchDokterModel;
import com.project.ta.findoctor.R;
import com.project.ta.findoctor.Services.MyFirebaseApp;
import com.project.ta.findoctor.Services.WebService;
import com.project.ta.findoctor.Utils.ItemClickSupport;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DokterFavoritActivity extends AppCompatActivity {
    private RecyclerView mRVFish;
    private DokterAdapter mAdapter;
    Button search;
    EditText searchText;
    SearchView searchView = null;
    WebService webService;
    ImageView foto;
    String searchQuery;
    List<SearchDokterModel> data=new ArrayList<>();
    long userid;
    SwipeRefreshLayout swipeContainer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dokter_favorit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        userid =((MyFirebaseApp)DokterFavoritActivity.this.getApplication()).getUser_id();

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

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.refresh_data);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
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
        String uri = "/getFavorite?id_pasien="+userid+"&search_query=" + searchQuery;
        uri = uri.replaceAll(" ", "%20");
        webService = new WebService(DokterFavoritActivity.this, uri, new AsyncResponse() {
            @Override
            public void processFinish(String output) {
                try {

                    JSONArray jArray = new JSONArray(output);

                    // Extract data from json and store into ArrayList as class objects
                    for (int i = 0; i < jArray.length(); i++) {
                        JSONObject json_data = jArray.getJSONObject(i);
                        SearchDokterModel searchDokterClass = new SearchDokterModel();
                        searchDokterClass.id_dokter = json_data.getLong("id_dokter");
                        searchDokterClass.name = json_data.getString("name");
                        searchDokterClass.kategori = json_data.getString("kategori");
                        searchDokterClass.id_kategori = json_data.getLong("id_kategori");
                        searchDokterClass.tipe = json_data.getLong("tipe");
                        searchDokterClass.activity = DokterFavoritActivity.this;
                        data.add(searchDokterClass);
                    }

                    // Setup and Handover data to recyclerview
                    mRVFish = (RecyclerView) findViewById(R.id.searchdokter_lv);
                    mAdapter = new DokterAdapter(DokterFavoritActivity.this, data);
                    mRVFish.setAdapter(mAdapter);
                    mRVFish.setLayoutManager(new LinearLayoutManager(DokterFavoritActivity.this));

                    ItemClickSupport.addTo(mRVFish).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                        @Override
                        public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                            Intent intent = new Intent(DokterFavoritActivity.this,DetailDokterActivity.class);
                            intent.putExtra("id_dokter",data.get(position).id_dokter);
                            intent.putExtra("nama_dokter",data.get(position).name);
                            intent.putExtra("id_kategori",data.get(position).id_kategori);
                            intent.putExtra("kategori",data.get(position).kategori);
                            startActivity(intent);
                        }
                    });

                } catch (JSONException e) {
                    // You to understand what actually error is and handle it appropriately
                    Toast.makeText(DokterFavoritActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                    Toast.makeText(DokterFavoritActivity.this, output.toString(), Toast.LENGTH_LONG).show();
                }
                swipeContainer.setRefreshing(false);
            }
        });
        webService.execute();
    }
}