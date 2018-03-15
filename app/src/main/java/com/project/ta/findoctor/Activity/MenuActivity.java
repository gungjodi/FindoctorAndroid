package com.project.ta.findoctor.Activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.project.ta.findoctor.Adapter.AntrianAdapter;
import com.project.ta.findoctor.Interfaces.AsyncResponse;
import com.project.ta.findoctor.Models.ListAntrianModel;
import com.project.ta.findoctor.Services.BackgroundWebService;
import com.project.ta.findoctor.Services.MyFirebaseApp;
import com.project.ta.findoctor.R;
import com.project.ta.findoctor.Services.NetworkReceiver;
import com.project.ta.findoctor.Services.NetworkState;
import com.project.ta.findoctor.Services.WebService;
import com.project.ta.findoctor.Services.APIRequest;
import com.project.ta.findoctor.Utils.Constants;
import com.project.ta.findoctor.Utils.MethodLib;
import com.project.ta.findoctor.Utils.PicassoCache;
import com.project.ta.findoctor.Utils.SendBulkNotif;
import com.project.ta.findoctor.Utils.StringWithTag;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;


public class MenuActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "AuthStateChanged";
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;
    WebService getAccount,webService;
    BackgroundWebService backgroundWebService;
    FirebaseUser user;
    String emailtext="-",name="Guest",notlp,tempatlahir,tanggallahir,nama_kategori,document;
    TextView nav_user,nav_email,antrianAktif,antrianTotal,namaDokter,namaKlinik,perkiraanWaktu,antrianAnda,messageAntrian;
    EditText maxAntrian;
    ImageButton cancelButton,gotoMapButton;
    ImageView nav_dp;
    long id_user,tipe,id_klinik_selected,id_asisten_selected,id_klinik_dokter_aktif,id_klinik_aktif,id_dokter,id_klinik_dokter,id_antrian;
    int valid, status_antrian,max_antrian=0;
    AlertDialog.Builder build;
    private EventBus eventBus = EventBus.getDefault();
    private List<StringWithTag> klinikList = new ArrayList<StringWithTag>();
    private List<StringWithTag> asistenList = new ArrayList<StringWithTag>();
    private ArrayList<ListAntrianModel> antrianModelArrayList = new ArrayList<ListAntrianModel>();
    ListView antrianListView;
    AntrianAdapter antrianAdapter;
    ViewStub dokterViewStub,pasienViewStub,asistenViewStub,guestViewStub;
    SwipeRefreshLayout swipeContainer;
    NavigationView navigationView;
    ProgressDialog networkProgress;
    ProgressBar progressAntrian,progressAntrianAktif;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_menu);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        dokterViewStub = (ViewStub) findViewById(R.id.dokter_stub);
        pasienViewStub = (ViewStub) findViewById(R.id.pasien_stub);
        asistenViewStub = (ViewStub) findViewById(R.id.asisten_stub);
        guestViewStub = (ViewStub) findViewById(R.id.guest_stub);


        eventBus.register(this);
        networkProgress = new ProgressDialog(this);
        networkProgress.setTitle("Koneksi Terputus");
        networkProgress.setMessage("Menunggu koneksi tersambung");
        networkProgress.setCancelable(false);
        if(!MethodLib.isConnected(this))
        {
            networkProgress.show();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NetworkReceiver networkReceiver = new NetworkReceiver();
        networkReceiver.enable(MenuActivity.this);

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View hView =  navigationView.getHeaderView(0);

        nav_user = (TextView)hView.findViewById(R.id.userNameNav);
        nav_email = (TextView)hView.findViewById(R.id.emailNav);
        nav_dp = (ImageView)hView.findViewById(R.id.dpNav);

        nav_user.setText(name);
        nav_email.setText(emailtext);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mAuthListener = new FirebaseAuth.AuthStateListener(){
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        getAccount = new WebService(MenuActivity.this, "/getDateMysql", new AsyncResponse() {
            @Override
            public void processFinish(String res) {
                ((MyFirebaseApp)MenuActivity.this.getApplication()).setDate_now(res);
            }
        });
        getAccount.execute();

        Field[] fields = Build.VERSION_CODES.class.getFields();
        String deviceModel = Build.MANUFACTURER+" "+Build.MODEL+" "+fields[Build.VERSION.SDK_INT + 1].getName();
        ((MyFirebaseApp)this.getApplication()).setDevice_model(deviceModel);

        if (user != null)
        {
            if(((MyFirebaseApp)this.getApplication()).getFirebase_token().equals(""))
            {
                ((MyFirebaseApp)this.getApplication()).setFirebase_token(FirebaseInstanceId.getInstance().getToken());
            }
            System.out.println("FirebaseToken : "+((MyFirebaseApp)this.getApplication()).getFirebase_token());

            getAccount = new WebService(MenuActivity.this, "/getUserByEmail?email=" + user.getEmail(), new AsyncResponse() {
                @Override
                public void processFinish(String res) {
                    try {
                        // De-serialize the JSON string into an array of city objects
                        JSONArray jsonArray = new JSONArray(res);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObj = jsonArray.getJSONObject(i);
                            id_user = (long) jsonObj.getInt("id");
                            emailtext=user.getEmail();
                            name=jsonObj.getString("name");
                            notlp=jsonObj.getString("notlp");
                            tempatlahir=jsonObj.getString("tempatlahir");
                            tanggallahir=jsonObj.getString("tanggallahir");
                            tipe = jsonObj.getLong("tipe");
                            valid = jsonObj.getInt("valid");
                            nama_kategori= jsonObj.getString("kategori");
                            document= jsonObj.getString("document");
                            ((MyFirebaseApp)MenuActivity.this.getApplication()).setUser_id(id_user);
                            ((MyFirebaseApp)MenuActivity.this.getApplication()).setEmail(emailtext);
                            ((MyFirebaseApp)MenuActivity.this.getApplication()).setUser_name(name);
                            ((MyFirebaseApp)MenuActivity.this.getApplication()).setNotlp(notlp);
                            ((MyFirebaseApp)MenuActivity.this.getApplication()).setTanggallahir(tanggallahir);
                            ((MyFirebaseApp)MenuActivity.this.getApplication()).setTempatlahir(tempatlahir);
                            ((MyFirebaseApp)MenuActivity.this.getApplication()).setUser_tipe(tipe);
                            ((MyFirebaseApp)MenuActivity.this.getApplication()).setValid(valid);
                            ((MyFirebaseApp)MenuActivity.this.getApplication()).setNama_kategori(nama_kategori);
                            ((MyFirebaseApp)MenuActivity.this.getApplication()).setDocument(document);

                            nav_user.setText(name);
                            nav_email.setText(emailtext);

                            FirebaseStorage storage = FirebaseStorage.getInstance();
                            String ext =  "jpg";
                            StorageReference storageRef = storage.getReferenceFromUrl("gs://findoctor-142603.appspot.com/");
                            final StorageReference imagesRef = storageRef.child("images/"+id_user+"/display_picture."+ext);
                            imagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    PicassoCache.getPicassoInstance(MenuActivity.this).load(uri.toString()).into(nav_dp);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                }
                            });

                            if (tipe==1)
                            {
                                dokterViewStub.inflate();
                                updateKlinik();
                                updateAsisten();
                                hideItemMenu(R.id.nav_cari_dokter);
                                hideItemMenu(R.id.nav_registrasi_pasien);
                                hideItemMenu(R.id.nav_dokter_favorit);
                            }
                            else if (tipe==2)
                            {
                                pasienViewStub.inflate();
                                hideItemMenu(R.id.nav_registrasi_pasien);
                                getDashboardPasien();
                                getAntrianPasien();
                                hideItemMenu(R.id.nav_asisten_dokter);
                            }
                            else if (tipe==3)
                            {
                                asistenViewStub.inflate();
                                getKlinikAsisten();
                                hideItemMenu(R.id.nav_dokter_favorit);
                                hideItemMenu(R.id.nav_asisten_dokter);
                                hideItemMenu(R.id.nav_cari_dokter);
                                hideItemMenu(R.id.nav_share);
                            }
                            else
                            {
                                guestViewStub.inflate();
                                hideItemMenu(R.id.nav_registrasi_pasien);
                                hideItemMenu(R.id.nav_rekam_medik);
                                hideItemMenu(R.id.nav_dokter_favorit);
                                hideItemMenu(R.id.nav_asisten_dokter);
                            }
                        }

                    } catch (JSONException e) {
                        Log.e("ERROR", "Error processing JSON", e);
                    }
                }
            });
            getAccount.execute();
        }
        else
        {
            nav_user.setText(name);
            nav_email.setText(emailtext);
            guestViewStub.inflate();
            hideItemMenu(R.id.nav_registrasi_pasien);
            hideItemMenu(R.id.nav_rekam_medik);
            hideItemMenu(R.id.nav_dokter_favorit);
            hideItemMenu(R.id.nav_asisten_dokter);
            if(!((MyFirebaseApp)this.getApplication()).getFirebase_token().equals(""))
            {
                ((MyFirebaseApp)this.getApplication()).setFirebase_token("");
            }
            System.out.println("FirebaseToken : "+((MyFirebaseApp)this.getApplication()).getFirebase_token());
        }
    }

    void hideItemMenu(int menuID)
    {
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        Menu nav_Menu = navigationView.getMenu();
        nav_Menu.findItem(menuID).setVisible(false);
    }

    void updateAsisten()
    {
        asistenList.clear();
        webService = new WebService(MenuActivity.this, "/getMyAsisten?id_user=" + id_user, new AsyncResponse() {
            @Override
            public void processFinish(String output) {
                try{
                    Spinner asistenAktifSpinner = (Spinner) findViewById(R.id.asisten_aktif_spiner);
                    JSONArray jsonArrayKlinik = new JSONArray(output);
                    int aktifPos = 0;
                    asistenList.add(new StringWithTag("Tidak Ada Asisten Aktif",0));
                    for (int j = 0; j< jsonArrayKlinik.length(); j++) {
                        JSONObject jsonObjKlinik = jsonArrayKlinik.getJSONObject(j);
                        String asisten = jsonObjKlinik.getString("nama_asisten");
                        if(jsonObjKlinik.getInt("is_aktif")==1)
                        {
                            aktifPos = j+1;
                            asisten+=" (Aktif)";
                        }
                        asistenList.add(new StringWithTag(asisten, (long) jsonObjKlinik.getInt("id_asisten")));
                    }

                    ArrayAdapter<StringWithTag> adap = new ArrayAdapter<StringWithTag> (MenuActivity.this, android.R.layout.simple_spinner_dropdown_item, asistenList);
                    asistenAktifSpinner.setAdapter(adap);
                    asistenAktifSpinner.setSelection(aktifPos);
                    asistenAktifSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            StringWithTag s = (StringWithTag) parent.getItemAtPosition(position);
                            id_asisten_selected = s.id;
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });

                    Button setAsistenAktif = (Button) findViewById(R.id.set_asisten_button);
                    setAsistenAktif.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            webService = new WebService(MenuActivity.this, "/apiSetAktif?id_user=" + id_user + "&asisten_aktif=" + id_asisten_selected, new AsyncResponse() {
                                @Override
                                public void processFinish(String output) {
                                    if(output.equals("1"))
                                    {
                                        updateAsisten();
                                    }
                                    else
                                    {
                                        MethodLib.showToast(MenuActivity.this,output);
                                    }

                                }
                            });
                            webService.execute();
                        }
                    });

                }
                catch (JSONException e)
                {
                    Log.e("JSONException","Error Processing JSON",e);
                }
            }
        });
        webService.execute();
    }

    void updateKlinik()
    {
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swiperefreshantrian);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateKlinik();
            }
        });
        klinikList.clear();
        webService = new WebService(MenuActivity.this, "/getKlinikByUser?id_user=" + id_user, new AsyncResponse() {
            @Override
            public void processFinish(String output) {
                try{
                    Spinner klinikAktifSpinner = (Spinner) findViewById(R.id.klinik_aktif_spinner);
                    JSONArray jsonArrayKlinik = new JSONArray(output);
                    int aktifPos = 0;
                    klinikList.add(new StringWithTag("Tidak Ada Klinik Aktif",0));
                    for (int j = 0; j< jsonArrayKlinik.length(); j++) {
                        JSONObject jsonObjKlinik = jsonArrayKlinik.getJSONObject(j);
                        String klinik = jsonObjKlinik.getString("nama_klinik");
                        if(jsonObjKlinik.getInt("is_aktif")==1)
                        {
                            aktifPos = j+1;
                            klinik+=" (Aktif)";
                        }
                        klinikList.add(new StringWithTag(klinik, (long) jsonObjKlinik.getInt("id_klinik")));
                    }

                    ArrayAdapter<StringWithTag> adap = new ArrayAdapter<StringWithTag> (MenuActivity.this, android.R.layout.simple_spinner_dropdown_item, klinikList);
                    klinikAktifSpinner.setAdapter(adap);
                    klinikAktifSpinner.setSelection(aktifPos);
                    klinikAktifSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            StringWithTag s = (StringWithTag) parent.getItemAtPosition(position);
                            id_klinik_selected = s.id;
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });

                    Button setKlinikAktif = (Button) findViewById(R.id.set_klinik_button);
                    setKlinikAktif.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            webService = new WebService(MenuActivity.this, "/apiSetAktif?id_user=" + id_user + "&klinik_aktif=" + id_klinik_selected, new AsyncResponse() {
                                @Override
                                public void processFinish(String output) {
                                    updateKlinik();
                                }
                            });
                            webService.execute();
                        }
                    });

                    Button setNextAntrian = (Button) findViewById(R.id.set_next_antrian_button);
                    setNextAntrian.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setNextAntrian();
                        }
                    });

                    Button setMaxAntrian = (Button) findViewById(R.id.set_max_antrian_button);
                    setMaxAntrian.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setMaxAntrian();
                        }
                    });
                    swipeContainer.setRefreshing(false);
                }
                catch (JSONException e)
                {
                    Log.e("JSONException","Error Processing JSON",e);
                }
                updateAntrianList();
            }
        });
        webService.execute();

    }

    void getKlinikAsisten()
    {
        namaDokter = (TextView) findViewById(R.id.nama_dokter_text);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.add_pasien_antrian);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent(MenuActivity.this, PickDokterChatActivity.class);
//                intent.putExtra("userId",userId);
//                startActivityForResult(intent,PICK_DOKTER_CHAT);
                MethodLib.showToast(MenuActivity.this,"ADD MANUAL PASIEN TO ANTRIAN");
            }
        });
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swiperefreshantrian);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getKlinikAsisten();
            }
        });
        klinikList.clear();
        webService = new WebService(MenuActivity.this, "/getKlinikAsistenAktif?id_asisten=" + id_user, new AsyncResponse() {
            @Override
            public void processFinish(String output) {
                try{
                    Spinner klinikAktifSpinner = (Spinner) findViewById(R.id.klinik_aktif_spinner);
                    if (output.equals("null"))
                    {
                        klinikList.add(new StringWithTag("Tidak Ada Klinik Aktif",0));
                    }
                    else
                    {
                        JSONObject jsonObjKlinik = new JSONObject(output);
                        String klinik = jsonObjKlinik.getString("nama_klinik");
                        String nama_dokter = jsonObjKlinik.getString("nama_dokter");
                        ((MyFirebaseApp)MenuActivity.this.getApplication()).setId_dokter(jsonObjKlinik.getLong("id_dokter"));
                        ((MyFirebaseApp)MenuActivity.this.getApplication()).setNama_dokter(jsonObjKlinik.getString("nama_dokter"));
                        namaDokter.setText(nama_dokter);
                        id_dokter = jsonObjKlinik.getLong("id_dokter");
                        klinikList.add(new StringWithTag(klinik, (long) jsonObjKlinik.getInt("id_klinik")));
                    }

                    ArrayAdapter<StringWithTag> adap = new ArrayAdapter<StringWithTag> (MenuActivity.this, android.R.layout.simple_spinner_dropdown_item, klinikList);
                    klinikAktifSpinner.setAdapter(adap);
                    klinikAktifSpinner.setSelection(0);
                    klinikAktifSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            StringWithTag s = (StringWithTag) parent.getItemAtPosition(position);
                            id_klinik_selected = s.id;
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });

                    Button setNextAntrian = (Button) findViewById(R.id.set_next_antrian_button);
                    setNextAntrian.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setNextAntrian();
                        }
                    });
                    swipeContainer.setRefreshing(false);
                }
                catch (JSONException e)
                {
                    Log.e("JSONException","Error Processing JSON",e);
                }
                updateAntrianList();
            }
        });
        webService.execute();

    }

    void setNextAntrian()
    {
        webService = new WebService(MenuActivity.this, "/setNextAntrian/" + id_klinik_dokter_aktif, new AsyncResponse() {
            @Override
            public void processFinish(String output) {
                updateAntrianList();
            }
        });
        webService.execute();
    }

    void setMaxAntrian()
    {
        int max_antrian_set = Integer.valueOf(maxAntrian.getText().toString());
        webService = new WebService(MenuActivity.this, "/setMaxAntrian?id_klinik_dokter="+ id_klinik_dokter_aktif+"&max_antrian="+max_antrian_set, new AsyncResponse() {
            @Override
            public void processFinish(String output) throws JSONException {
                updateKlinik();
                updateAntrianList();
                JSONObject data = new JSONObject(output);
                MethodLib.showToast(MenuActivity.this,data.getString("message"));
            }
        });
        webService.execute();
    }

    void updateAntrianList()
    {
        antrianAktif = (TextView) findViewById(R.id.antrian_aktif_text);
        antrianTotal = (TextView) findViewById(R.id.total_antrian_text);
        maxAntrian = (EditText) findViewById(R.id.max_antrian_edittext);
        antrianModelArrayList.clear();
        antrianAdapter = new AntrianAdapter(MenuActivity.this, antrianModelArrayList);
        String urlAntrian = "/getKlinikAntrianAktif?id_user=" + id_user;
        if(tipe==3)
        {
            urlAntrian = "/getKlinikAntrianAktif?id_user=" + id_dokter;
        }
        webService = new WebService(this,urlAntrian, new AsyncResponse() {
            @Override
            public void processFinish(String output) {
                if(!output.equals(""))
                {
                    clearAntrian();
                    try {
                        JSONObject antrianKlinik = new JSONObject(output);
                        id_klinik_dokter_aktif = antrianKlinik.getLong("id_klinik_dokter");
                        id_klinik_aktif = antrianKlinik.getLong("id_klinik");
                        max_antrian = antrianKlinik.getInt("max_antrian");
                        maxAntrian.setText(String.valueOf(max_antrian));
                        String antrian_aktif = antrianKlinik.getString("antrian_aktif").equals("null")?"0":antrianKlinik.getString("antrian_aktif");
                        antrianAktif.setText(antrian_aktif);
                        antrianTotal.setText(antrianKlinik.getString("antrian_terakhir"));

                        if (antrianKlinik.getInt("antrian_terakhir")>0)
                        {
                            final JSONArray listAntrian = new JSONArray(antrianKlinik.getString("res"));
                            for (int j = 0; j< listAntrian.length(); j++) {
                                JSONObject jsonListAntrian = listAntrian.getJSONObject(j);
                                final long id_antrian = jsonListAntrian.getLong("id");
                                long id_klinik_dokter = jsonListAntrian.getLong("id_klinik_dokter");
                                long id_pasien= jsonListAntrian.getLong("id_pasien");
                                long no_antrian= jsonListAntrian.getLong("no_antrian");
                                long status_antrian= jsonListAntrian.getLong("status_antrian");

                                Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(jsonListAntrian.getString("waktu_registrasi"));
                                String tanggal_registrasi = new SimpleDateFormat("yyyy-MM-dd").format(date);
                                String waktu_registrasi = new SimpleDateFormat("HH:mm").format(date);
                                if(tanggal_registrasi.equals("0000-00-00"))
                                {
                                    waktu_registrasi="-";
                                }
                                date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(jsonListAntrian.getString("waktu_proses"));
                                String tanggal_proses = new SimpleDateFormat("yyyy-MM-dd").format(date);
                                String waktu_proses=new SimpleDateFormat("HH:mm").format(date);
                                if(tanggal_proses.equals("0002-11-30"))
                                {
                                    waktu_proses="-";
                                }

                                date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(jsonListAntrian.getString("waktu_selesai"));
                                String tanggal_selesai = new SimpleDateFormat("yyyy-MM-dd").format(date);
                                String waktu_selesai=new SimpleDateFormat("HH:mm").format(date);
                                if(tanggal_selesai.equals("0002-11-30"))
                                {
                                    waktu_selesai="-";
                                }

                                String nama_pasien=jsonListAntrian.getString("nama_pasien");
                                String nama_status=jsonListAntrian.getString("nama_status");
                                antrianModelArrayList.add(new ListAntrianModel(id_antrian,id_klinik_dokter,id_pasien,no_antrian,status_antrian,waktu_registrasi,waktu_proses,waktu_selesai,nama_pasien,nama_status,tanggal_registrasi));
                                antrianAdapter = new AntrianAdapter(MenuActivity.this, antrianModelArrayList);
                                antrianListView = (ListView) findViewById(R.id.antrian_list_view);
                                antrianListView.setAdapter(antrianAdapter);
                                registerForContextMenu(antrianListView);
                            }
                        }
                    } catch (JSONException e) {
                        Log.e("JSONException","ERROR GETTING JSON",e);
                    } catch (ParseException e) {
                        Log.e("DateTimeException","ERROR PARSING DATETIME",e);
                    }
                }
                else
                {
                    clearAntrian();
                }
            }
        });
        webService.execute();
    }

    void getDashboardPasien()
    {
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swiperefreshdashboard);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getAntrianPasien();
            }
        });
        cancelButton = (ImageButton) findViewById(R.id.button_cancel);
        gotoMapButton = (ImageButton) findViewById(R.id.button_goto_map);
        namaKlinik = (TextView) findViewById(R.id.text_nama_klinik);
        namaDokter = (TextView) findViewById(R.id.text_nama_dokter);
        perkiraanWaktu = (TextView) findViewById(R.id.text_perkiraan_waktu);
        antrianAnda = (TextView) findViewById(R.id.text_antrian_anda);
        antrianAktif = (TextView) findViewById(R.id.text_antrian_aktif);
        messageAntrian = (TextView) findViewById(R.id.text_message_antrian);
        progressAntrian = (ProgressBar) findViewById(R.id.progress_bar_antrian);
        progressAntrianAktif = (ProgressBar) findViewById(R.id.progress_bar_antrian_aktif);
    }

    void getAntrianPasien()
    {
        cancelButton.setVisibility(View.INVISIBLE);
        gotoMapButton.setVisibility(View.INVISIBLE);
        antrianAnda.setVisibility(View.INVISIBLE);
        antrianAktif.setVisibility(View.INVISIBLE);
        progressAntrian.setVisibility(View.VISIBLE);
        progressAntrianAktif.setVisibility(View.VISIBLE);

        webService = new WebService(this, "/getMyAntrianAktif?id_pasien="+id_user, new AsyncResponse() {
            @Override
            public void processFinish(String output) {
                try {
                    JSONObject data = new JSONObject(output);
                    id_antrian = data.getLong("id");
                    status_antrian = data.getInt("status_antrian");
                    String jam = data.getString("jam_buka")+" - "+data.get("jam_tutup");
                    id_klinik_dokter = data.getLong("id_klinik_dokter");
                    if(status_antrian==4)
                    {
                        cancelButton.setVisibility(View.INVISIBLE);
                        namaKlinik.setText("");
                        namaDokter.setText("");
                        antrianAnda.setText("0");
                    }
                    else
                    {
                        namaKlinik.setText(data.getString("nama_klinik")+" ("+jam+")");
                        namaDokter.setText(data.getString("nama_dokter"));
                        antrianAnda.setText(data.getString("no_antrian"));
                        cancelButton.setVisibility(View.VISIBLE);
                        gotoMapButton.setVisibility(View.INVISIBLE);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                backgroundWebService = new BackgroundWebService("/getEtaAntrian/" + id_klinik_dokter+"/"+id_user, new AsyncResponse() {
                    @Override
                    public void processFinish(String output) throws JSONException {
                        if(id_klinik_dokter!=0)
                        {
                            JSONObject data = new JSONObject(output);
                            perkiraanWaktu.setText(data.getString("message"));
                        }
                        else
                        {
                            perkiraanWaktu.setText("Belum terdaftar pada antrian");
                        }
                    }
                });
                backgroundWebService.execute();

                backgroundWebService = new BackgroundWebService("/getAntrianAktifPasien/" + id_klinik_dokter, new AsyncResponse() {
                    @Override
                    public void processFinish(String output) {
                        try {
                            JSONObject data = new JSONObject(output);
                            antrianAktif.setText(data.getString("no_antrian"));
                            messageAntrian.setText(data.getString("message"));
                            progressAntrian.setVisibility(View.INVISIBLE);
                            progressAntrianAktif.setVisibility(View.INVISIBLE);
                            antrianAnda.setVisibility(View.VISIBLE);
                            antrianAktif.setVisibility(View.VISIBLE);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                backgroundWebService.execute();
            }
        });
        webService.execute();

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webService = new WebService(MenuActivity.this, "/cancel_antrian?id_klinik_dokter=" + id_klinik_dokter + "&id_user=" + id_user, new AsyncResponse() {
                    @Override
                    public void processFinish(String output) throws JSONException {
                        getAntrianPasien();
                    }
                });
                webService.execute();
            }
        });

        gotoMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MethodLib.showToast(MenuActivity.this,"Tampilkan lokasi klinik pada peta");
            }
        });

        swipeContainer.setRefreshing(false);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if(v.getId()==R.id.antrian_list_view)
        {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            int position = info.position;
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.dashboard_antrian_menu, menu);
            if(antrianModelArrayList.get(position).status_antrian!=4)
            {
                menu.findItem(R.id.menu_rekam_medik).setVisible(true);
            }
            else
            {
                menu.findItem(R.id.menu_rekam_medik).setVisible(false);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.menu_pindah_antrian:
                pindahAntrian(info.position);
                return true;
            case R.id.menu_rekam_medik:
                Intent rekamMedik = new Intent(MenuActivity.this,RekamMedikActivity.class);
                rekamMedik.putExtra("id_pasien_dashboard",antrianModelArrayList.get(info.position).id_pasien);
                rekamMedik.putExtra("id_klinik_dashboard",id_klinik_aktif);
                rekamMedik.putExtra("tanggal_registrasi_dashboard",antrianModelArrayList.get(info.position).tanggal_registrasi);
                startActivity(rekamMedik);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    void pindahAntrian(int position)
    {
        if(antrianModelArrayList.size()>1&&(position+1)!=antrianModelArrayList.size())
        {
            long id_antrian = antrianModelArrayList.get(position).id;
            long no_antrian = antrianModelArrayList.get(position).no_antrian;
            long status_antrian = antrianModelArrayList.get(position).status_antrian;
            long id_antrian_next = antrianModelArrayList.get(position+1).id;
            long no_antrian_next = antrianModelArrayList.get(position+1).no_antrian;
            long status_antrian_next = antrianModelArrayList.get(position+1).status_antrian;
            long id_pasien = antrianModelArrayList.get(position).id_pasien;
            long id_klinik_dokter = antrianModelArrayList.get(position).id_klinik_dokter;
            webService = new WebService(MenuActivity.this, "/skipAntrian?" +
                "id_antrian=" + id_antrian +
                "&no_antrian=" + no_antrian +
                "&status_antrian=" +status_antrian+
                "&id_antrian_next=" + id_antrian_next +
                "&no_antrian_next=" + no_antrian_next+
                "&status_antrian_next="+status_antrian_next,
                new AsyncResponse() {
                    @Override
                    public void processFinish(String output) {
                        MethodLib.showToast(MenuActivity.this,output);
                        updateAntrianList();
                    }
                });
            webService.execute();
        }
    }

    void clearAntrian()
    {
        antrianAktif = (TextView) findViewById(R.id.antrian_aktif_text);
        antrianTotal = (TextView) findViewById(R.id.total_antrian_text);
        id_klinik_dokter_aktif = 0;
        antrianAktif.setText("0");
        antrianTotal.setText("0");
        antrianModelArrayList.clear();
        antrianAdapter = new AntrianAdapter(MenuActivity.this, antrianModelArrayList);
        antrianListView = (ListView) findViewById(R.id.antrian_list_view);
        antrianListView.setAdapter(antrianAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Oops");
            builder.setMessage("Anda yakin ingin keluar dari aplikasi?");
            builder.setPositiveButton("Keluar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    finish();
                }
            });
            builder.setNegativeButton("Batal", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_share) {
            Intent i = new Intent(MenuActivity.this, MapsActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_send) {
            Intent i = new Intent(MenuActivity.this, ChatMenuActivity.class);
            startActivity(i);
        }
        else if (id == R.id.nav_account) {
            Intent i = new Intent(MenuActivity.this, AccountActivity.class);
            startActivity(i);
        }else if (id == R.id.nav_rekam_medik) {
            Intent i = new Intent(MenuActivity.this, RekamMedikActivity.class);
            startActivity(i);
        }
        else if (id == R.id.nav_cari_dokter) {
            Intent i = new Intent(MenuActivity.this, SearchDokterActivity.class);
            startActivity(i);
        }
        else if (id == R.id.nav_registrasi_pasien) {
            Intent i = new Intent(MenuActivity.this, RegisterActivity.class);
            i.putExtra("is_manual", 1);
            i.putExtra("id_asisten", id_user);
            startActivity(i);
        }
        else if (id == R.id.nav_dokter_favorit) {
                Intent i = new Intent(MenuActivity.this, DokterFavoritActivity.class);
                startActivity(i);
        }
        else if (id == R.id.nav_asisten_dokter) {
            Intent i = new Intent(MenuActivity.this, AsistenDokterActivity.class);
            startActivity(i);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        eventBus.unregister(this);
    }

    @Subscribe
    public void onEvent(NetworkState events){
        final NetworkState event = events;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("EVENT ",event.getMessage());
                if(Objects.equals(event.getMessage(), "onLost"))
                {
                    networkProgress.show();
                }
                else if(Objects.equals(event.getMessage(), "onAvailable"))
                {
                    networkProgress.dismiss();
                }
            }
        });

    }

}
