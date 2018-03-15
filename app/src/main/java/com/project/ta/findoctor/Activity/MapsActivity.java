package com.project.ta.findoctor.Activity;

import android.Manifest;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.project.ta.findoctor.Interfaces.AsyncResponse;
import com.project.ta.findoctor.Models.DetailDokterModel;
import com.project.ta.findoctor.Models.DokterInKlinikModel;
import com.project.ta.findoctor.Models.MarkerModel;
import com.project.ta.findoctor.R;
import com.project.ta.findoctor.Services.MyFirebaseApp;
import com.project.ta.findoctor.Services.WebService;
import com.project.ta.findoctor.Utils.MethodLib;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    public GoogleMap mMap;
    public Marker marker;
    private WebService webService;
    long user_id, user_tipe;
    int valid, id_jenis_klinik;
    WebService getAccount;
    EditText titleBox, nama_klinikBox;
    Spinner jenis_klinik_spin;
    AlertDialog.Builder build;
    private FirebaseUser user;
    String latitudex, longitudex, emailx = "";
    private ProgressDialog progress;
    private Map<Marker, MarkerModel> allMarkersMap = new HashMap<Marker, MarkerModel>();
    List<Marker> markerList = new ArrayList<>();

    MapFragment mMapFragment;
    BitmapDescriptor icon;
    final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION=1;
    void showToast(CharSequence msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.project.ta.findoctor.R.layout.activity_maps_new);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mMapFragment = MapFragment.newInstance();
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.content_maps_activity_new, mMapFragment);
        fragmentTransaction.commit();
        mMapFragment.getMapAsync(this);

        user_id = ((MyFirebaseApp) MapsActivity.this.getApplication()).getUser_id();
        user_tipe = ((MyFirebaseApp) MapsActivity.this.getApplication()).getUser_tipe();
        valid = ((MyFirebaseApp) MapsActivity.this.getApplication()).getValid();

        LayoutInflater li = LayoutInflater.from(MapsActivity.this);

        View promptsView = li.inflate(R.layout.add_marker_dialog_backup, null);
        titleBox = (EditText) promptsView.findViewById(R.id.latlongText);
        nama_klinikBox = (EditText) promptsView.findViewById(R.id.nama_klinik_field);

        jenis_klinik_spin = (Spinner) promptsView.findViewById(R.id.jenis_klinik_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.jenis_klinik_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        jenis_klinik_spin.setAdapter(adapter);

        jenis_klinik_spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                id_jenis_klinik = parent.getSelectedItemPosition() + 1;
                System.out.println("JENIS KLINIK TERPILIH https://www.tipeterpilih.com: " + id_jenis_klinik);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        progress = new ProgressDialog(MapsActivity.this);
        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            emailx = user.getEmail();
        }
        build = new AlertDialog.Builder(MapsActivity.this);
        build.setTitle("Tambah Klinik");
        build.setView(promptsView);
        build.setPositiveButton("SET",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (nama_klinikBox.getText().toString().matches("")) {
                            showToast("Masukkan nama klinik");
                            nama_klinikBox.requestFocus();
                        } else {
                            String nama_klinik = nama_klinikBox.getText().toString();
                            nama_klinik = nama_klinik.replaceAll(" ", "%20");
                            dialog.dismiss();
                            webService = new WebService(MapsActivity.this, "/saveMarker?latitude=" + latitudex + "&longitude=" + longitudex + "&email=" + emailx + "&nama_klinik=" + nama_klinik + "&id_pengelola=" + user_id + "&id_jenis_klinik=" + id_jenis_klinik, new AsyncResponse() {
                                @Override
                                public void processFinish(String json) {
                                    showToast(json);
                                    refreshMarker(false);
                                }
                            });
                            webService.execute();
                        }


                    }
                });

        build.setNegativeButton("CANCEL",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        dialog.dismiss();
                    }
                });


    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        final AlertDialog alert = build.create();

        if (user != null) {
            if (user_tipe == 1 && valid == 1) {
                mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                    @Override
                    public void onMapLongClick(LatLng latLng) {
                        System.out.println(user_tipe + " - " + valid);
                        latitudex = String.valueOf(latLng.latitude);
                        longitudex = String.valueOf(latLng.longitude);
                        titleBox.setText(latLng.latitude + ", " + latLng.longitude);
                        titleBox.setEnabled(false);
                        alert.show();
                    }
                });
            }
        }

        if (ContextCompat.checkSelfPermission(MapsActivity.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(MapsActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
        refreshMarker(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    refreshMarker(true);
                } else {
                    refreshMarker(true);
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void refreshMarker(final boolean move)
    {
        Intent detail = getIntent();
        final double gotoLat = detail.getDoubleExtra("gotoLat",0);
        final double gotoLong = detail.getDoubleExtra("gotoLong",0);
        final long gotoIDklinik = detail.getLongExtra("id_klinik",0);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
        allMarkersMap.clear();

        webService = new WebService(MapsActivity.this, "/getAllKlinik", new AsyncResponse() {
            @Override
            public void processFinish(String output) {
                String json=output;
                try {
                    // De-serialize the JSON string into an array of city objects
                    JSONArray jsonArray = new JSONArray(json);
                    for (int i = 0; i < jsonArray.length(); i++)
                    {
                        final JSONObject jsonObj = jsonArray.getJSONObject(i);

                        LatLng latLng = new LatLng(Double.valueOf(jsonObj.getString("latitude")) ,Double.valueOf(jsonObj.getString("longitude")));

                        if (i == 0)
                        {
                            if(move)
                            {
                                CameraPosition cameraPosition = new CameraPosition.Builder()
                                        .target(latLng).zoom(12).build();
                                mMap.animateCamera(CameraUpdateFactory
                                        .newCameraPosition(cameraPosition));
                            }

                        }
                        // Create a marker for each city in the JSON data.
                        final MarkerModel myMarker = new MarkerModel(jsonObj.getString("nama_klinik"),
                                jsonObj.getString("jenis_klinik"),
                                jsonObj.getInt("id_jenis_klinik"),jsonObj.getLong("id_pengelola"),
                                jsonObj.getString("nama_pengelola"),jsonObj.getLong("id"),
                                jsonObj.getString("dokters"),jsonObj.getString("kategoris"),
                                jsonObj.getString("id_dokters"),
                                jsonObj.getString("id_kategoris"),
                                jsonObj.getDouble("latitude"),
                                jsonObj.getDouble("longitude"),
                                jsonObj.getString("klinik_id"));

                        if(jsonObj.getInt("id_jenis_klinik")==1)
                        {
                            icon = BitmapDescriptorFactory.fromResource(R.drawable.icon_mandiri_file);
                        }
                        else if(jsonObj.getInt("id_jenis_klinik")==2)
                        {
                            icon = BitmapDescriptorFactory.fromResource(R.drawable.icon_bersama_file);
                        }

                        marker = mMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .icon(icon));

                        markerList.add(marker);
                        allMarkersMap.put(marker, myMarker);
                        marker.showInfoWindow();
                        if(gotoLat!=0 && gotoLong!=0)
                        {
                            if(marker.getPosition().latitude==gotoLat && marker.getPosition().longitude==gotoLong)
                            {
                                marker.setVisible(true);
                            }
                            else
                            {
                                marker.setVisible(false);
                            }
                        }
                        if(user!=null)
                        {
                            mMap.setOnInfoWindowClickListener(MapsActivity.this);
                        }


                    }
                    progress.dismiss();

                } catch (JSONException e) {
                    Log.e("ERROR JSON", "Error processing JSON", e);
                }

                mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                    private Map<Marker, MarkerModel> data = allMarkersMap;

                    @Override
                    public View getInfoWindow(Marker marker) {
                        return null;
                    }

                    @Override
                    public View getInfoContents(Marker marker) {
                        MarkerModel m = data.get(marker);

                        View v = getLayoutInflater().inflate(R.layout.info_window,null);
                        TextView title = (TextView) v.findViewById(R.id.info_title);
                        TextView jenis = (TextView) v.findViewById(R.id.info_nama);

                        title.setText(m.nama);
                        jenis.setText(m.jenis_klinik);

                        return v;
                    }

                });

                for (int pos = 0;pos<markerList.size();pos++)
                {
                    if(gotoLat!=0 && gotoLong!=0)
                    {
                        if(markerList.get(pos).getPosition().latitude==gotoLat && markerList.get(pos).getPosition().longitude==gotoLong)
                        {
                            LatLng gotoLatLng = new LatLng(gotoLat ,gotoLong);
                            CameraPosition cameraPosition = new CameraPosition.Builder()
                                    .target(gotoLatLng).zoom(12).build();
                            mMap.animateCamera(CameraUpdateFactory
                                    .newCameraPosition(cameraPosition));
                            if(markerList.get(pos).isInfoWindowShown())
                            {
                                markerList.get(pos).hideInfoWindow();
                            }
                            else
                            {
                                markerList.get(pos).showInfoWindow();
                            }
                            break;
                        }
                    }
                }
            }
        });
        mMap.clear();
        webService.execute();
    }

    @Override
    public void onInfoWindowClick(final Marker marker) {
        LayoutInflater li = LayoutInflater.from(MapsActivity.this);

        View promptsView = li.inflate(R.layout.add_marker_dialog, null);
        final MarkerModel m = allMarkersMap.get(marker);

        String[] id_dokters = m.dokters_id.split("#");
        String[] dokters = m.dokters_str.split("#");
        String[] id_klinik_dokters = m.dokter_klinik_id.split("#");
        String[] id_kategori = m.kategoris_id.split("#");
        String[] kategori = m.kategoris_str.split("#");


        int isExists = 0;

        ArrayList<DokterInKlinikModel> dokter_arr = new ArrayList<DokterInKlinikModel>();

        for(String s:id_dokters)
        {
            if(s.equals(String.valueOf(user_id)))
            {
                isExists++;
            }
        }

        for (int i =0; i<dokters.length;i++)
        {
            if(dokters[i]!="null" )
            {
                DokterInKlinikModel dok = new DokterInKlinikModel(
                        Long.valueOf(id_klinik_dokters[i]),
                        Long.valueOf(id_dokters[i]),
                        dokters[i],
                        Long.valueOf(id_kategori[i]),
                        kategori[i]
                );
                dokter_arr.add(dok);
            }
        }
        ListView lv = (ListView) promptsView.findViewById(R.id.dokters_list);
        final ArrayAdapter<DokterInKlinikModel> arrayAdapter = new ArrayAdapter<DokterInKlinikModel>(
                this,
                android.R.layout.simple_list_item_1,
                dokter_arr );

        lv.setAdapter(arrayAdapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                DokterInKlinikModel obj = (DokterInKlinikModel) arrayAdapter.getItem(i);
                long id_klinik_dokter = obj.id_klinik_dokter;
                long id_dokter = obj.id_dokter;
                String nama = (String)obj.nama_dokter;
                String kategori = (String)obj.kategori;
                Intent detailDokter = new Intent(MapsActivity.this,DetailKlinikDokterActivity.class);
                detailDokter.putExtra("id_dokter",id_dokter);
                detailDokter.putExtra("id_klinik_dokter",id_klinik_dokter);
                detailDokter.putExtra("nama_dokter",nama);
                detailDokter.putExtra("kategori",kategori);
                detailDokter.putExtra("user_tipe",user_tipe);
                startActivity(detailDokter);
            }
        });

        build = new AlertDialog.Builder(MapsActivity.this);
        build.setTitle("Dokter Praktik");
        build.setView(promptsView);
        build.setNeutralButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                dialog.dismiss();
            }
        });

        if(m.id_pengelola==user_id && user_tipe==1)
        {
            build.setPositiveButton("UPDATE",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

            build.setNegativeButton("DELETE",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            AlertDialog.Builder build2 = new AlertDialog.Builder(MapsActivity.this);
                            build2.setTitle("Action");
                            build2.setMessage("Hapus Data?");
                            build2.setPositiveButton("Hapus",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            webService = new WebService(MapsActivity.this, "/deleteMarker?id=" + m.id, new AsyncResponse() {
                                                @Override
                                                public void processFinish(String output) {
                                                    String json=output;
                                                    marker.remove();
                                                    allMarkersMap.remove(marker);
                                                    refreshMarker(false);
                                                    showToast(json);
                                                }
                                            });
                                            webService.execute();
                                        }
                                    });

                            build2.setNegativeButton("CANCEL",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                            dialog.dismiss();
                                        }
                                    });
                            final AlertDialog alert2 = build2.create();
                            alert2.show();
                        }
                    });
        }
        else if(m.id_pengelola!=user_id && user_tipe==1 && isExists==0 && valid==1 && m.id_jenis_klinik==2)
        {
            build.setPositiveButton("Join",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            webService = new WebService(MapsActivity.this, "/joinKlinik?id_klinik="+m.id+"&id_dokter=" + user_id, new AsyncResponse() {
                                @Override
                                public void processFinish(String json) {
                                    showToast(json);
                                    refreshMarker(false);
                                }
                            });
                            webService.execute();
                        }
                    });
        }

        final AlertDialog alert = build.create();
        alert.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.maps_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.do_refresh_map) {
            refreshMarker(false);
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

