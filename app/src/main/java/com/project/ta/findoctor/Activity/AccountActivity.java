package com.project.ta.findoctor.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.project.ta.findoctor.Interfaces.AsyncResponse;
import com.project.ta.findoctor.Services.APIRequest;
import com.project.ta.findoctor.Services.MyFirebaseApp;
import com.project.ta.findoctor.R;
import com.project.ta.findoctor.Services.WebService;
import com.project.ta.findoctor.Utils.Constants;
import com.project.ta.findoctor.Utils.MethodLib;
import com.project.ta.findoctor.Utils.PicassoCache;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class AccountActivity extends AppCompatActivity {

    private static final int PICKFILE_REQUEST_CODE = 1,PICKDOCUMENT_REQUEST_CODE=2;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private ProgressDialog progress;
    WebService getAccount;
    String name,notlp,tempatlahir,tanggallahir,nama_kategori="-",nama_tipe,document;
    ImageView foto;
    UploadTask uploadTask;
    long id,tipe;
    ProgressDialog networkProgress;
    CheckBox checkDocument;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progress = new ProgressDialog(AccountActivity.this);
        progress.setTitle("Loading");
        progress.setMessage("Retrieving user data");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();
        user = FirebaseAuth.getInstance().getCurrentUser();

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        if (user != null) {
            setContentView(R.layout.activity_account);
            id = ((MyFirebaseApp)this.getApplication()).getUser_id();
            name=((MyFirebaseApp)this.getApplication()).getUser_name();
            notlp=((MyFirebaseApp)this.getApplication()).getNotlp();
            tempatlahir=((MyFirebaseApp)this.getApplication()).getTempatlahir();
            tanggallahir=((MyFirebaseApp)this.getApplication()).getTanggallahir();
            tipe=((MyFirebaseApp)this.getApplication()).getUser_tipe();
            document=((MyFirebaseApp)this.getApplication()).getDocument();

            EditText email = (EditText) findViewById(R.id.v_email);
            EditText nama = (EditText) findViewById(R.id.v_nama);
            EditText tempat_lahir= (EditText) findViewById(R.id.v_tempatlahir);
            EditText tgllahir= (EditText) findViewById(R.id.v_tgllahir);
            EditText tlp= (EditText) findViewById(R.id.v_tlp);
            EditText kategori_user= (EditText) findViewById(R.id.kategori_tipe_text);

            checkDocument = (CheckBox) findViewById(R.id.document_check);
            checkDocument.setVisibility(View.INVISIBLE);
            email.setText(user.getEmail());
            nama.setText(name);
            tempat_lahir.setText(tempatlahir);
            tgllahir.setText(tanggallahir);
            tlp.setText(notlp);

            if(tipe==1)
            {
                nama_tipe="Dokter";
                nama_kategori=((MyFirebaseApp)this.getApplication()).getNama_kategori();
                kategori_user.setText(nama_kategori);
                checkDocument.setVisibility(View.VISIBLE);
            }
            else if(tipe==2)
            {
                nama_tipe="Pasien";
                kategori_user.setText(nama_tipe);
            }
            else if(tipe==3)
            {
                nama_tipe="Asisten";
                kategori_user.setText(nama_tipe);
            }
            else if (tipe==99)
            {
                nama_tipe="Admin";
                kategori_user.setText(nama_tipe);
            }

            Button logout = (Button) findViewById(R.id.logout);
            logout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    user = FirebaseAuth.getInstance().getCurrentUser();
                    try {
                        JSONObject requestBody = new JSONObject();
                        requestBody.put("firebase_id",FirebaseInstanceId.getInstance().getToken());
                        requestBody.put("device_model",((MyFirebaseApp)AccountActivity.this.getApplication()).getDevice_model());
                        requestBody.put("email",user.getEmail());
                        requestBody.put("method","LOGOUT");
                        APIRequest apiRequest= new APIRequest(AccountActivity.this, "/updateToken", requestBody, Constants.METHOD_POST, new AsyncResponse() {
                            @Override
                            public void processFinish(String output) {
                                try {
                                    JSONObject data = new JSONObject(output);
                                    if(data.getString("message").equals("OK"))
                                    {
                                        FirebaseAuth.getInstance().signOut();
                                        ((MyFirebaseApp)AccountActivity.this.getApplication()).setFirebase_token("");
                                        finish();
                                        Intent intent = new Intent(AccountActivity.this, MenuActivity.class);
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
            });

            progress.dismiss();
            foto = (ImageView) findViewById(R.id.foto);


            String ext =  "jpg";
            //get profile picture
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReferenceFromUrl("gs://findoctor-142603.appspot.com/");
            final StorageReference imagesRef = storageRef.child("images/"+id+"/display_picture."+ext);
            progress.show();
            imagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(AccountActivity.this)
                            .load(uri.toString())
                            .into(foto);
                    progress.dismiss();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progress.dismiss();
                }
            });


            foto.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onClick(View v) {
                    String[] permissions = {"android.permission.WRITE_EXTERNAL_STORAGE"};
                    ActivityCompat.requestPermissions(AccountActivity.this, permissions, PICKFILE_REQUEST_CODE);
                }
            });

            //get document
            FirebaseStorage doc_storage = FirebaseStorage.getInstance();
            String doc_ext =  "pdf";
            StorageReference doc_storageRef = doc_storage.getReferenceFromUrl("gs://findoctor-142603.appspot.com/");
            final StorageReference docRef = doc_storageRef.child("docs/"+id+"/documents."+doc_ext);
            progress.show();
            docRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    progress.dismiss();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progress.dismiss();
                }
            });
            checkDocumentToggle();

        }
        else
        {
            progress.dismiss();
            mAuth = FirebaseAuth.getInstance();
            setContentView(R.layout.activity_login);
            Button login = (Button) findViewById(R.id.login);
            Button register = (Button) findViewById(R.id.register);

            register.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                    Intent intent = new Intent(AccountActivity.this, RegisterActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
            });

            login.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditText emailx = (EditText) findViewById(R.id.email);
                    EditText passwordx = (EditText) findViewById(R.id.password);

                    final String emailin = emailx.getText().toString();
                    final String passwordin = passwordx.getText().toString();

                    if(emailin==null || emailin.matches(""))
                    {
                        Toast.makeText(AccountActivity.this, "You did not enter a username", Toast.LENGTH_SHORT).show();
                        emailx.requestFocus();
                        return;
                    }
                    if(passwordin==null || passwordin.matches(""))
                    {
                        Toast.makeText(AccountActivity.this, "You did not enter a password", Toast.LENGTH_SHORT).show();
                        passwordx.requestFocus();
                        return;
                    }

                    getAccount = new WebService(AccountActivity.this, "/getUserByEmail?email=" + emailin, new AsyncResponse() {
                        @Override
                        public void processFinish(String output) {
                            try {
                                // De-serialize the JSON string into an array of city objects
                                JSONArray jsonArray = new JSONArray(output);
                                JSONObject jsonObj = jsonArray.getJSONObject(0);
                                if(jsonObj.getString("response").equals("OK"))
                                {
                                    if(jsonObj.getInt("is_manual")==1)
                                    {
                                        MethodLib.showToast(AccountActivity.this,"Akun/Email ini telah terdaftar secara manual.\nSilakan melakukan registrasi Ulang");
                                        Intent register = new Intent(AccountActivity.this,RegisterActivity.class);
                                        register.putExtra("is_manual",1);
                                        register.putExtra("email",jsonObj.getString("email"));
                                        register.putExtra("name",jsonObj.getString("name"));
                                        register.putExtra("notlp",jsonObj.getString("notlp"));
                                        register.putExtra("tempatlahir",jsonObj.getString("tempatlahir"));
                                        register.putExtra("tanggallahir",jsonObj.getString("tanggallahir"));
                                        register.putExtra("is_register_ulang",1);
                                        startActivity(register);
                                    }
                                    else
                                    {
                                        progress = new ProgressDialog(AccountActivity.this);
                                        progress.setTitle("Logging In");
                                        progress.setMessage("Wait while logging in...");
                                        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
                                        progress.show();
                                        mAuth.signInWithEmailAndPassword(emailin, passwordin)
                                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                                        if (!task.isSuccessful()) {
                                                            MethodLib.showToast(AccountActivity.this,"Email belum terdaftar/Password salah");
                                                            progress.dismiss();
                                                        }
                                                        else
                                                        {
                                                            MethodLib.showToast(AccountActivity.this, "Login Success");
                                                            user = FirebaseAuth.getInstance().getCurrentUser();
                                                            ((MyFirebaseApp)AccountActivity.this.getApplication()).setFirebase_token(FirebaseInstanceId.getInstance().getToken());
                                                            try {
                                                                JSONObject requestBody = new JSONObject();
                                                                requestBody.put("firebase_id",FirebaseInstanceId.getInstance().getToken());
                                                                requestBody.put("device_model",((MyFirebaseApp)AccountActivity.this.getApplication()).getDevice_model());
                                                                requestBody.put("email",user.getEmail());
                                                                requestBody.put("method","LOGIN");
                                                                APIRequest apiRequest= new APIRequest(AccountActivity.this, "/updateToken", requestBody, Constants.METHOD_POST, new AsyncResponse() {
                                                                    @Override
                                                                    public void processFinish(String output) {
                                                                        try {
                                                                            JSONObject data = new JSONObject(output);
                                                                            if(data.getString("message").equals("OK"))
                                                                            {
                                                                                finish();
                                                                                Intent intent = new Intent(AccountActivity.this, MenuActivity.class);
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
                                                    }
                                                });
                                    }
                                }
                                else
                                {
                                    MethodLib.showToast(AccountActivity.this,jsonObj.getString("message"));
                                    progress.dismiss();
                                }

                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                    });
                    getAccount.execute();

                }
            });

        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    public void checkDocumentToggle()
    {
        if(document=="null")
        {
            checkDocument.setChecked(false);
        }
        else
        {
            checkDocument.setChecked(true);
        }

        checkDocument.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(document==null)
                {
                    checkDocument.setChecked(false);
                }
                else
                {
                    checkDocument.setChecked(true);
                }
                String[] permissions = {"android.permission.WRITE_EXTERNAL_STORAGE"};
                ActivityCompat.requestPermissions(AccountActivity.this, permissions, PICKDOCUMENT_REQUEST_CODE);
            }
        });
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PICKFILE_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, PICKFILE_REQUEST_CODE);
            } else {
                Toast.makeText(getApplicationContext(), "PERMISSION_DENIED", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == PICKDOCUMENT_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("application/pdf");
                startActivityForResult(intent, PICKDOCUMENT_REQUEST_CODE);
            } else {
                Toast.makeText(getApplicationContext(), "PERMISSION_DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICKFILE_REQUEST_CODE && resultCode == RESULT_OK && data != null){
            networkProgress = new ProgressDialog(this);
            networkProgress.setTitle("Loading");
            networkProgress.setMessage("Please wait . . .");
            networkProgress.setCancelable(false);
            networkProgress.show();
            Uri selectedImage = data.getData();
            try {
                InputStream imageStream = getContentResolver().openInputStream(selectedImage);
                FirebaseStorage storage = FirebaseStorage.getInstance();
                String ext =  "jpg";
                StorageReference storageRef = storage.getReferenceFromUrl("gs://findoctor-142603.appspot.com/");
                StorageReference imagesRef = storageRef.child("images/"+id+"/display_picture."+ext);
                uploadTask = imagesRef.putStream(imageStream);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        MethodLib.showToast(AccountActivity.this,"Gagal upload data");
                        networkProgress.dismiss();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        ImageView dp = (ImageView) findViewById(R.id.foto);
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        String imageURL = downloadUrl.toString();
                        System.out.println("URL DP NYAAAAAAAAAAAAAAAAAaaa : "+ imageURL);
                        PicassoCache.getPicassoInstance(AccountActivity.this).load(imageURL).into(dp);
                        networkProgress.dismiss();
                    }
                });
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        if(requestCode == PICKDOCUMENT_REQUEST_CODE && resultCode == RESULT_OK && data != null){
            networkProgress = new ProgressDialog(this);
            networkProgress.setTitle("Loading");
            networkProgress.setMessage("Uploading Document. . .");
            networkProgress.setCancelable(false);
            networkProgress.show();
            Uri selectedImage = data.getData();
            try {
                InputStream imageStream = getContentResolver().openInputStream(selectedImage);
                FirebaseStorage storage = FirebaseStorage.getInstance();
                String ext =  "pdf";
                StorageReference storageRef = storage.getReferenceFromUrl("gs://findoctor-142603.appspot.com/");
                StorageReference imagesRef = storageRef.child("docs/"+id+"/documents."+ext);
                uploadTask = imagesRef.putStream(imageStream);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        MethodLib.showToast(AccountActivity.this,"Gagal upload data");
                        networkProgress.dismiss();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        final String docURL = downloadUrl.toString();
                        WebService webService = new WebService(AccountActivity.this, "/updateDokumenSIP?id_dokter="+id+"&url=" + docURL, new AsyncResponse() {
                            @Override
                            public void processFinish(String output) {
                                MethodLib.showToast(AccountActivity.this,"Sukses Upload SIP");
                                document = docURL;
                                checkDocumentToggle();
                            }
                        });
                        webService.execute();
                        networkProgress.dismiss();
                    }
                });

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}