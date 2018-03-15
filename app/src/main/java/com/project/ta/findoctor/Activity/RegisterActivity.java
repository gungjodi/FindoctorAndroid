package com.project.ta.findoctor.Activity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;
import com.project.ta.findoctor.Interfaces.AsyncResponse;
import com.project.ta.findoctor.R;
import com.project.ta.findoctor.Services.APIRequest;
import com.project.ta.findoctor.Services.MyFirebaseApp;
import com.project.ta.findoctor.Utils.Constants;
import com.project.ta.findoctor.Utils.MethodLib;
import com.project.ta.findoctor.Utils.StringWithTag;
import com.project.ta.findoctor.Services.WebService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    private ProgressDialog progress;
    private String email,nama,password,confirm_password,tempat_lahir,tanggal_lahir,no_telp,uri;
    private int jenis_user,year,month,day,is_manual=0,is_register_ulang=0,id_asisten;
    long id_kategori;
    private EditText bemail,bname,bpassword,bconfirm_password,btempat_lahir,btgl_lahir,bno_telp;
    Button do_register;
    Spinner kategoriSpinner;
    FirebaseAuth mAuth;
    WebService getAccount;
    private List<StringWithTag> subscriptionsStrings = new ArrayList<StringWithTag>();
    private DatePickerDialog tglLahirPicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        Intent data = getIntent();
        is_manual = data.getIntExtra("is_manual",0);
        is_register_ulang = data.getIntExtra("is_register_ulang",0);
        email = data.getStringExtra("email");
        nama = data.getStringExtra("name");
        no_telp = data.getStringExtra("notlp");
        tempat_lahir = data.getStringExtra("tempatlahir");
        tanggal_lahir = data.getStringExtra("tanggallahir");

        Spinner spinner = (Spinner) findViewById(R.id.jenis_user);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.jenis_user_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        bname = (EditText) findViewById(R.id.nama_text);
        bemail = (EditText) findViewById(R.id.s_email);
        bpassword = (EditText) findViewById(R.id.s_password);
        bconfirm_password = (EditText) findViewById(R.id.s_password_confirm);
        btempat_lahir = (EditText) findViewById(R.id.tempat_lahir);
        btgl_lahir = (EditText) findViewById(R.id.tgl_lahir);
        bno_telp = (EditText) findViewById(R.id.no_telp);
        kategoriSpinner = (Spinner) findViewById(R.id.kategori_spinner);

        bname.setText(nama);
        bemail.setText(email);
        if(!bemail.getText().toString().equals(""))
        {
            bemail.setEnabled(false);
        }
        bno_telp.setText(no_telp);
        btgl_lahir.setText(tanggal_lahir);
        btempat_lahir.setText(tempat_lahir);

        do_register = (Button) findViewById(R.id.do_register);

        getAccount = new WebService(RegisterActivity.this, "/getAllKategori", new AsyncResponse() {
            @Override
            public void processFinish(String res) {
                try {
                    JSONArray jsonArray = new JSONArray(res);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObj = jsonArray.getJSONObject(i);
                        subscriptionsStrings.add(new StringWithTag(jsonObj.getString("kategori"),Long.valueOf(jsonObj.getInt("id"))));
                    }

                    ArrayAdapter<StringWithTag> adap = new ArrayAdapter<StringWithTag> (RegisterActivity.this, android.R.layout.simple_spinner_dropdown_item, subscriptionsStrings);
                    kategoriSpinner.setAdapter(adap);
                    kategoriSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            StringWithTag s = (StringWithTag) parent.getItemAtPosition(position);
                            id_kategori = s.id;

                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                } catch (JSONException e) {
                    Log.e("ERROR", "Error processing JSON", e);
                }
            }
        });
        getAccount.execute();


        if(is_manual==1)
        {
            spinner.setSelection(1);
            spinner.setEnabled(false);
            kategoriSpinner.setEnabled(false);
        }

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int selected = parent.getSelectedItemPosition();
                jenis_user = selected+1;
                if(jenis_user==1)
                {
                    kategoriSpinner.setVisibility(View.VISIBLE);
                }
                else
                {
                    kategoriSpinner.setVisibility(View.INVISIBLE);
                }

                System.out.println("TIPE TERPILIH https://www.tipeterpilih.com: "+selected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        do_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = bemail.getText().toString();
                password = bpassword.getText().toString();
                confirm_password = bconfirm_password.getText().toString();
                nama = bname.getText().toString();
                tempat_lahir = btempat_lahir.getText().toString();
                tanggal_lahir =btgl_lahir.getText().toString().replaceAll("\\s+","");
                no_telp = bno_telp.getText().toString();
                mAuth = FirebaseAuth.getInstance();
                no_telp = bno_telp.getText().toString();
                uri = "/saveUser?is_register_ulang="+is_register_ulang+"&is_manual="+is_manual+"&name="+nama+"&email="+email+"&password="+password+"&notlp="+no_telp+"&tempatlahir="+tempat_lahir+"&tanggallahir="+tanggal_lahir+"&id_kategori="+id_kategori+"&tipe="+jenis_user;
                uri = uri.replaceAll(" ", "%20");
                if(!password.matches(confirm_password))
                {
                    Toast.makeText(RegisterActivity.this, "Confirm password tidak sama ",
                            Toast.LENGTH_SHORT).show();
                    bpassword.requestFocus();
                }
                else if(password.matches("") || password==null)
                {
                    Toast.makeText(RegisterActivity.this, "Masukkan Password",
                            Toast.LENGTH_SHORT).show();
                    bpassword.requestFocus();
                }
                else if(confirm_password.matches("") || confirm_password==null)
                {
                    Toast.makeText(RegisterActivity.this, "Masukkan Konfirmasi Password",
                            Toast.LENGTH_SHORT).show();
                    bconfirm_password.requestFocus();
                }
                else if(email.matches("") || email==null)
                {
                    Toast.makeText(RegisterActivity.this, "Masukkan Email",
                            Toast.LENGTH_SHORT).show();
                    bemail.requestFocus();
                }
                else
                {
                    progress = new ProgressDialog(RegisterActivity.this);
                    progress.setTitle("Loading");
                    progress.setMessage("Registering user...");
                    progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
                    progress.show();
                    getAccount = new WebService(RegisterActivity.this, uri, new AsyncResponse() {
                        @Override
                        public void processFinish(String output) throws JSONException {
                            JSONObject jsonObject = new JSONObject(output);
                            String message = jsonObject.getString("message");
                            String result = jsonObject.getString("result");
                            if(result.equals("OK"))
                            {
                                if(is_manual==0 || is_register_ulang==1)
                                {
                                    mAuth.createUserWithEmailAndPassword(email,password)
                                            .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                                                @Override
                                                public void onComplete(@NonNull Task<AuthResult> task) {
                                                    Log.d("REGISTEEEEEEEEERRRRRRRR", "createUserWithEmail:onComplete:" + task.isSuccessful());
                                                    if (!task.isSuccessful()) {
                                                        Toast.makeText(RegisterActivity.this, task.getException().getMessage(),
                                                                Toast.LENGTH_SHORT).show();
                                                    }
                                                    else
                                                    {
                                                        ((MyFirebaseApp)RegisterActivity.this.getApplication()).setFirebase_token(FirebaseInstanceId.getInstance().getToken());
                                                        try {
                                                            JSONObject requestBody = new JSONObject();
                                                            requestBody.put("firebase_id",FirebaseInstanceId.getInstance().getToken());
                                                            requestBody.put("device_model",((MyFirebaseApp)RegisterActivity.this.getApplication()).getDevice_model());
                                                            requestBody.put("email",email);
                                                            requestBody.put("method","LOGIN");
                                                            APIRequest apiRequest= new APIRequest(RegisterActivity.this, "/updateToken", requestBody, Constants.METHOD_POST, new AsyncResponse() {
                                                                @Override
                                                                public void processFinish(String output) {
                                                                    try {
                                                                        JSONObject data = new JSONObject(output);
                                                                        if(data.getString("message").equals("OK"))
                                                                        {
                                                                            Toast.makeText(RegisterActivity.this, "Registrasi sukses", Toast.LENGTH_SHORT).show();
                                                                            finish();
                                                                            Intent intent = new Intent(RegisterActivity.this, MenuActivity.class);
                                                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                                            startActivity(intent);
                                                                            finish();
                                                                        }
                                                                        progress.dismiss();

                                                                    } catch (JSONException e) {
                                                                        e.printStackTrace();
                                                                    }
                                                                }
                                                            });
                                                            apiRequest.execute();

                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                    progress.dismiss();
                                                }
                                            });
                                }
                                else
                                {
                                    Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
                                }
                            }
                            else
                            {
                                MethodLib.showToast(RegisterActivity.this,output);
                            }
                            progress.dismiss();
                        }
                    });
                    getAccount.execute();
                }
            }
        });
//
        btgl_lahir.setKeyListener(null);
        btgl_lahir.setOnClickListener(this);
        Calendar newCalendar = Calendar.getInstance();
        tglLahirPicker = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                btgl_lahir.setText(year+"-"+(monthOfYear+1)+"-"+dayOfMonth);
            }
        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
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


    @Override
    public void onClick(View view) {
        if(view == btgl_lahir) {
            tglLahirPicker.show();
        }
    }
}