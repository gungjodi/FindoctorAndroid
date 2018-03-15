package com.project.ta.findoctor.Activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.project.ta.findoctor.Adapter.DetailDokterAdapter;
import com.project.ta.findoctor.Adapter.DokterAdapter;
import com.project.ta.findoctor.BuildConfig;
import com.project.ta.findoctor.Interfaces.AsyncResponse;
import com.project.ta.findoctor.Models.DetailDokterModel;
import com.project.ta.findoctor.Models.SearchDokterModel;
import com.project.ta.findoctor.R;
import com.project.ta.findoctor.Services.DistanceParserService;
import com.project.ta.findoctor.Services.MyFirebaseApp;
import com.project.ta.findoctor.Services.WebService;
import com.project.ta.findoctor.Utils.MethodLib;
import com.project.ta.findoctor.Utils.PicassoCache;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DetailDokterActivity extends AppCompatActivity {
    long id_dokter;
    String nama_dokter, kategori_dokter,time_now;
    TextView namaText, kategoriText;
    ImageView photoDokter;
    double myLat, myLng;
    List<DetailDokterModel> data = new ArrayList<>();
    boolean mLocationPermissionGranted;
    private final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    WebService webService;
    private static final String TAG = DetailDokterActivity.class.getSimpleName();
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private FusedLocationProviderClient mFusedLocationClient;
    protected Location mLastLocation;
    long tipe,userid;
    String defaultLocation = "-8.7964117,115.1741751";
    String currentLocation = "-8.7964117,115.1741751";
    ToggleButton toggleButton;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_dokter);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        user = FirebaseAuth.getInstance().getCurrentUser();
        tipe =((MyFirebaseApp)DetailDokterActivity.this.getApplication()).getUser_tipe();
        userid =((MyFirebaseApp)DetailDokterActivity.this.getApplication()).getUser_id();


        Intent detail = getIntent();
        id_dokter = detail.getLongExtra("id_dokter", 0);
        nama_dokter = detail.getStringExtra("nama_dokter");
        kategori_dokter = detail.getStringExtra("kategori");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        namaText = (TextView) findViewById(R.id.nama_dokter);
        kategoriText = (TextView) findViewById(R.id.kategori_dokter);
        photoDokter = (ImageView) findViewById(R.id.photo_dokter);

        namaText.setText(nama_dokter);
        kategoriText.setText(kategori_dokter);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        String ext = "jpg";
        StorageReference storageRef = storage.getReferenceFromUrl("gs://findoctor-142603.appspot.com/");
        final StorageReference imagesRef = storageRef.child("images/" + id_dokter + "/display_picture." + ext);
        imagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                PicassoCache.getPicassoInstance(DetailDokterActivity.this).load(uri.toString()).into(photoDokter);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
//                Toast.makeText(current.activity,e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });

        webService = new WebService(this, "/phpdatetime/time", new AsyncResponse() {
            @Override
            public void processFinish(String output) {
                time_now = output;
            }
        });
        webService.execute();

        webService = new WebService(this, "/getDetailKlinikDokter?id_user=" + id_dokter + "&current_location="+currentLocation, new AsyncResponse() {
            @Override
            public void processFinish(String output) {
                JSONArray jArray = null;
                ArrayList<String> pointList = new ArrayList<>();
                try {
                    jArray = new JSONArray(output);
                    for (int i = 0; i < jArray.length(); i++) {
                        final JSONObject json_data = jArray.getJSONObject(i);
                        try {
                            DetailDokterModel detailDokterModel = new DetailDokterModel(
                                    DetailDokterActivity.this,
                                    json_data.getLong("id"),
                                    json_data.getLong("id_klinik"),
                                    json_data.getString("nama_klinik"),
                                    json_data.getString("jenis_klinik"),
                                    json_data.getDouble("latitude"),
                                    json_data.getDouble("longitude"),
                                    json_data.getString("distance"),
                                    json_data.getString("id_hari_list"),
                                    json_data.getString("jam_buka_list"),
                                    json_data.getString("jam_tutup_list"),
                                    json_data.getInt("is_buka"));
                            data.add(detailDokterModel);
                            RecyclerView mRVFish;
                            DetailDokterAdapter mAdapter;
                            mRVFish = (RecyclerView) findViewById(R.id.klinik_list);
                            mAdapter = new DetailDokterAdapter(DetailDokterActivity.this, data);
                            mRVFish.setAdapter(mAdapter);
                            mRVFish.setLayoutManager(new LinearLayoutManager(DetailDokterActivity.this));
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        webService.execute();

        toggleButton = (ToggleButton) findViewById(R.id.toggleButton);
        toggleButton.setChecked(false);
        toggleButton.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), android.R.drawable.btn_star_big_off));
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                {
                    toggleButton.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(),android.R.drawable.btn_star_big_on));
                }
                else
                {
                    toggleButton.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), android.R.drawable.btn_star_big_off));
                }
            }
        });

        if(user==null || tipe==1 || tipe==3)
        {
            toggleButton.setVisibility(View.INVISIBLE);
        }

        if(tipe == 2){
            webService = new WebService(this, "/getFavorite?id_pasien="+userid, new AsyncResponse() {
                @Override
                public void processFinish(String output) {
                    try {
                        JSONArray jArray = new JSONArray(output);
                        for(int i =0; i<jArray.length();i++)
                        {
                            final JSONObject json_data = jArray.getJSONObject(i);
                            if(json_data.getLong("id_dokter")==id_dokter)
                            {
                                toggleButton.setChecked(true);
                                break;
                            }

                        }
                        toggleButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(toggleButton.isChecked())
                                {
                                    setFavorite(true);
                                }
                                else
                                {
                                    setFavorite(false);
                                }
                            }
                        });
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            });
            webService.execute();
        }


    }

    void setFavorite(boolean isFavorite)
    {
        webService = new WebService(DetailDokterActivity.this, "/setFavorite?id_pasien=" + userid + "&id_dokter=" + id_dokter + "&isFavorite=" + isFavorite, new AsyncResponse() {
            @Override
            public void processFinish(String output) {
                MethodLib.showToast(DetailDokterActivity.this,output);
            }
        });
        webService.execute();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!checkPermissions()) {
            requestPermissions();
        } else {
            getLastLocation();
        }
    }

    @SuppressWarnings("MissingPermission")
    private void getLastLocation() {
        mFusedLocationClient.getLastLocation()
                .addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            mLastLocation = task.getResult();
                            System.out.println("LOCATION "+mLastLocation.getLatitude()+","+mLastLocation.getLongitude());
                            currentLocation=mLastLocation.getLatitude()+","+mLastLocation.getLongitude();
                        } else {
                            Log.w(TAG, "getLastLocation:exception", task.getException());
                            currentLocation=defaultLocation;
                        }
                    }
                });
    }


    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(DetailDokterActivity.this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");

            showSnackbar(R.string.permission_rationale, android.R.string.ok,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            startLocationPermissionRequest();
                        }
                    });

        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            startLocationPermissionRequest();
        }
    }

    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted.
                getLastLocation();
            } else {
                // Permission denied.

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                showSnackbar(R.string.permission_denied_explanation, R.string.settings,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
            }
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
}
