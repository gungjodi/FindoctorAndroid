package com.project.ta.findoctor.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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
import com.project.ta.findoctor.Utils.MethodLib;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AsistenDokterActivity extends AppCompatActivity {
    private RecyclerView mRVFish;
    private AsistenAdapter mAdapter;
    Button search;
    EditText searchText;
    WebService webService;
    ImageView foto;
    String searchQuery;
    List<AsistenModel> data=new ArrayList<>();
    long userid;
    SwipeRefreshLayout swipeContainer;
    final int ADD_ASISTEN_ACTIVITY = 1;
    FloatingActionButton fab;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asisten_dokter);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        userid =((MyFirebaseApp)AsistenDokterActivity.this.getApplication()).getUser_id();

        fab = (FloatingActionButton) findViewById(R.id.fab);

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
        String uri = "/getMyAsisten?id_user="+userid+"&search_query=" + searchQuery;
        uri = uri.replaceAll(" ", "%20");
        webService = new WebService(AsistenDokterActivity.this, uri, new AsyncResponse() {
            @Override
            public void processFinish(String output) {
                try {

                    JSONArray jArray = new JSONArray(output);

                    // Extract data from json and store into ArrayList as class objects
                    for (int i = 0; i < jArray.length(); i++) {
                        JSONObject json_data = jArray.getJSONObject(i);
                        AsistenModel asistenModel = new AsistenModel(
                                json_data.getLong("id_asisten"),
                                json_data.getString("nama_asisten"),
                                json_data.getString("email"),
                                json_data.getInt("is_aktif"),
                                AsistenDokterActivity.this
                        );
                        data.add(asistenModel);
                    }

                    // Setup and Handover data to recyclerview
                    mRVFish = (RecyclerView) findViewById(R.id.searchasisten_lv);
                    mAdapter = new AsistenAdapter(AsistenDokterActivity.this, data);
                    mRVFish.setAdapter(mAdapter);
                    mRVFish.setLayoutManager(new LinearLayoutManager(AsistenDokterActivity.this));

                    ItemClickSupport.addTo(mRVFish).setOnItemLongClickListener(new ItemClickSupport.OnItemLongClickListener() {
                        @Override
                        public boolean onItemLongClicked(RecyclerView recyclerView, final int position, View v) {
                            final CharSequence[] items = {"Hapus asisten"};
                            AlertDialog.Builder builder = new AlertDialog.Builder(AsistenDokterActivity.this);
                            builder.setItems(items, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int item) {
                                    if(item==0)
                                    {
                                        final long id_asisten = data.get(position).id_asisten;
                                        AlertDialog.Builder builder = new AlertDialog.Builder(AsistenDokterActivity.this);
                                        builder.setTitle("Oops");
                                        builder.setMessage("Anda yakin ingin menghapus asisten "+data.get(position).name+" ?");
                                        builder.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                WebService webService = new WebService(AsistenDokterActivity.this, "/deleteAsisten?id_dokter=" + userid + "&id_asisten=" + id_asisten, new AsyncResponse() {
                                                    @Override
                                                    public void processFinish(String output) throws JSONException {
                                                        JSONObject jsonObject = new JSONObject(output);
                                                        if(jsonObject.getString("result").equals("OK"))
                                                        {
                                                            MethodLib.showToast(AsistenDokterActivity.this,jsonObject.getString("message"));
                                                            loadData();
                                                        }
                                                        else
                                                        {
                                                            MethodLib.showToast(AsistenDokterActivity.this,jsonObject.getString("message"));
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
                                    dialog.dismiss();
                                }
                            });
                            builder.show();
                            return false;
                        }
                    });
                } catch (JSONException e) {
                    Toast.makeText(AsistenDokterActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                }
                swipeContainer.setRefreshing(false);
            }
        });
        webService.execute();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addAsisten = new Intent(AsistenDokterActivity.this,AddAsistenActivity.class);
                startActivityForResult(addAsisten,ADD_ASISTEN_ACTIVITY);
            }
        });
    }

    public void onActivityResult(int requestCode,int resultCode,Intent data)
    {
        if(requestCode==ADD_ASISTEN_ACTIVITY)
        {
            if(resultCode==RESULT_OK)
            {
                loadData();
            }
        }
    }
}
