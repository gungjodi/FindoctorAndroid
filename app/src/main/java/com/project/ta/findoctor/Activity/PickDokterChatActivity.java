package com.project.ta.findoctor.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.project.ta.findoctor.Interfaces.AsyncResponse;
import com.project.ta.findoctor.Utils.ItemClickSupport;
import com.project.ta.findoctor.Models.SearchDokterModel;
import com.project.ta.findoctor.Adapter.PickDokterChatAdapter;
import com.project.ta.findoctor.R;
import com.project.ta.findoctor.Services.WebService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PickDokterChatActivity extends AppCompatActivity {
    private RecyclerView mRVFish;
    private PickDokterChatAdapter mAdapter;
    Button search;
    long userId;
    EditText searchText;
    SearchView searchView = null;
    WebService webService;
    ImageView foto;
    String searchQuery;
    List<SearchDokterModel> data=new ArrayList<>();

    public void showToast(String text)
    {
        Toast.makeText(this,text,Toast.LENGTH_SHORT).show();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_dokter_chat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent detail = getIntent();
        userId = detail.getLongExtra("userId",0);
        search = (Button) findViewById(R.id.search);
        searchText = (EditText) findViewById(R.id.TFsearch);

        data.clear();
        loadData();

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                data.clear();
                loadData();
            }
        });
    }

    public void loadData()
    {
        searchQuery = searchText.getText().toString();
        String uri = "/searchDokterAndAsisten?search_query=" + searchQuery+"&id="+userId;
        uri = uri.replaceAll(" ", "%20");
        webService = new WebService(this, uri, new AsyncResponse() {
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
                        searchDokterClass.activity = PickDokterChatActivity.this;
                        data.add(searchDokterClass);

                    }

                    // Setup and Handover data to recyclerview
                    mRVFish = (RecyclerView) findViewById(R.id.searchdokter_lv);
                    mAdapter = new PickDokterChatAdapter(PickDokterChatActivity.this, data);
                    mRVFish.setAdapter(mAdapter);
                    mRVFish.setLayoutManager(new LinearLayoutManager(PickDokterChatActivity.this));

                    ItemClickSupport.addTo(mRVFish).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                        @Override
                        public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                            Intent intent = getIntent();
                            intent.putExtra("id_dokter",data.get(position).id_dokter);
                            intent.putExtra("nama_dokter",data.get(position).name);
                            setResult(RESULT_OK,intent);
                            finish();
                        }
                    });

                } catch (JSONException e) {
                    // You to understand what actually error is and handle it appropriately
                    Toast.makeText(PickDokterChatActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                    Toast.makeText(PickDokterChatActivity.this, output.toString(), Toast.LENGTH_LONG).show();
                }
            }
        });
        webService.execute();
    }
}
