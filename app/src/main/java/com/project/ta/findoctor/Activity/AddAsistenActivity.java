package com.project.ta.findoctor.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.project.ta.findoctor.Adapter.AsistenAdapter;
import com.project.ta.findoctor.Interfaces.AsyncResponse;
import com.project.ta.findoctor.Models.AsistenModel;
import com.project.ta.findoctor.R;
import com.project.ta.findoctor.Services.MyFirebaseApp;
import com.project.ta.findoctor.Services.WebService;
import com.project.ta.findoctor.Utils.ItemClickSupport;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AddAsistenActivity extends AppCompatActivity {
    private RecyclerView mRVFish;
    private AsistenAdapter mAdapter;
    Button search;
    EditText searchText;
    WebService webService;
    ImageView foto;
    String searchQuery;
    List<AsistenModel> data=new ArrayList<>();
    long id_dokter,id_asisten;
    SwipeRefreshLayout swipeContainer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_asisten);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        id_dokter = ((MyFirebaseApp)this.getApplication()).getUser_id();
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
        webService = new WebService(AddAsistenActivity.this, "/getAllAsistenAvailable?search_query=" + searchQuery, new AsyncResponse() {
            @Override
            public void processFinish(String output) {
                try {

                    JSONArray jArray = new JSONArray(output);

                    // Extract data from json and store into ArrayList as class objects
                    for (int i = 0; i < jArray.length(); i++) {
                        JSONObject json_data = jArray.getJSONObject(i);
                        AsistenModel asistenModel = new AsistenModel(
                                json_data.getLong("id"),
                                json_data.getString("name"),
                                json_data.getString("email"),
                                99,
                                AddAsistenActivity.this
                        );
                        data.add(asistenModel);
                    }

                    // Setup and Handover data to recyclerview
                    mRVFish = (RecyclerView) findViewById(R.id.searchasisten_lv);
                    mAdapter = new AsistenAdapter(AddAsistenActivity.this, data);
                    mRVFish.setAdapter(mAdapter);
                    mRVFish.setLayoutManager(new LinearLayoutManager(AddAsistenActivity.this));

                    ItemClickSupport.addTo(mRVFish).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                        @Override
                        public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                            id_asisten = data.get(position).id_asisten;
                            AlertDialog.Builder builder = new AlertDialog.Builder(AddAsistenActivity.this);
                            builder.setTitle("Oops");
                            builder.setMessage("Anda yakin ingin menambah asisten "+data.get(position).name+" ?");
                            builder.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    WebService webService = new WebService(AddAsistenActivity.this, "/addAsisten?id_dokter="+id_dokter+"&id_asisten="+id_asisten, new AsyncResponse() {
                                        @Override
                                        public void processFinish(String output) throws JSONException {
                                            JSONObject jsonObject = new JSONObject(output);
                                            if(jsonObject.getString("result").equals("OK"))
                                            {
                                                Intent intent = new Intent();
                                                Toast.makeText(AddAsistenActivity.this,jsonObject.getString("message"),Toast.LENGTH_SHORT).show();
                                                setResult(RESULT_OK, intent);
                                                finish();
                                            }
                                            else
                                            {
                                                Toast.makeText(AddAsistenActivity.this,jsonObject.getString("message"),Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                    webService.execute();
                                }
                            });
                            builder.setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                            AlertDialog alertDialog = builder.create();
                            alertDialog.show();
                        }
                    });
                } catch (JSONException e) {
                    Toast.makeText(AddAsistenActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                }
                swipeContainer.setRefreshing(false);
            }
        });
        webService.execute();
    }
}
