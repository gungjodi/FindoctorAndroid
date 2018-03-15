package com.project.ta.findoctor.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.project.ta.findoctor.Utils.IdHariSorter;
import com.project.ta.findoctor.Models.WaktuPraktikModel;
import com.project.ta.findoctor.Services.MyFirebaseApp;
import com.project.ta.findoctor.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class AddWaktuActivity extends AppCompatActivity {
    AlertDialog.Builder build;
    EditText jam_buka,jam_tutup;
    TextView hari_text;
    int current_hari_checked=0;
    String hari_uncheck;
    LinearLayout checkboxes_hari;

    long id_klinik_dokter,user_id,user_tipe;
    Intent detail;
    String[] hari = new String[]{"Minggu","Senin","Selasa","Rabu","Kamis","Jumat","Sabtu"};
    ArrayList<WaktuPraktikModel> waktu_praktik = new ArrayList<>();
    ArrayList<CheckBox> checkBoxArrayList = new ArrayList<>();

    void showToast(CharSequence msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_waktu);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Collections.sort(waktu_praktik,new IdHariSorter());
                Intent intent = new Intent();
                intent.putExtra("waktu_new",waktu_praktik);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        detail = getIntent();
        id_klinik_dokter = detail.getLongExtra("id_klinik_dokter",0);
        user_id = detail.getLongExtra("user_id",0);
        user_tipe = detail.getLongExtra("user_tipe",0);
        waktu_praktik = (ArrayList<WaktuPraktikModel>) detail.getSerializableExtra("waktu_praktik");

        final List<String> hari_checked = new ArrayList<String>();
        checkboxes_hari = (LinearLayout) findViewById(R.id.hari_buka_checkbox);

        LayoutInflater li = LayoutInflater.from(AddWaktuActivity.this);
        View promptsView = li.inflate(R.layout.add_waktu_dialog, null);
        jam_buka = (EditText) promptsView.findViewById(R.id.jam_bukaText);
        jam_tutup = (EditText) promptsView.findViewById(R.id.jam_tutupText);
        hari_text = (TextView) promptsView.findViewById(R.id.hariText);
        build = new AlertDialog.Builder(AddWaktuActivity.this);
        build.setTitle("Waktu Klinik");
        build.setView(promptsView);
        build.setPositiveButton("SET",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if(jam_buka.getText().toString().matches(""))
                        {
                            showToast("Masukkan jam buka praktik");
                            jam_buka.requestFocus();
//                            return;
                        }
                        else if(jam_tutup.getText().toString().matches(""))
                        {
                            showToast("Masukkan jam tutup praktik");
                            jam_tutup.requestFocus();
//                            return;
                        }
                        else
                        {
                            final String hari_now = ((MyFirebaseApp)AddWaktuActivity.this.getApplication()).getDate_now();
                            CheckBox check = (CheckBox) findViewById(current_hari_checked);
                            check.setText(hari[current_hari_checked] + " ("+jam_buka.getText().toString() +" - "+ jam_tutup.getText().toString() +")");
                            WaktuPraktikModel waktu = new WaktuPraktikModel(AddWaktuActivity.this,user_id,id_klinik_dokter,current_hari_checked+1,user_tipe,hari[current_hari_checked],jam_buka.getText().toString(),jam_tutup.getText().toString(),hari_now,"","","");
                            waktu_praktik.add(waktu);
                            dialog.dismiss();
                        }
                    }
                });

        build.setNegativeButton("CANCEL",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        dialog.dismiss();
                        CheckBox check = (CheckBox) findViewById(current_hari_checked);
                        boolean checked = (check.isChecked());
                        check.setChecked(!checked);

                        Collections.sort(hari_checked);
                        System.out.println("HARI DIPILIH ADALAAAAAAAAAAAAAAAAAH : "+hari_checked);
                        current_hari_checked=0;

                    }
                });
        final AlertDialog alert = build.create();


        for(int i=0;i<hari.length;i++)
        {
            final CheckBox hari_check = new CheckBox(this);
            hari_check.setId(i);
            hari_check.setText(hari[i]);
            checkboxes_hari.addView(hari_check);
            checkBoxArrayList.add(hari_check);
            hari_check.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(hari_check.isChecked())
                    {
                        current_hari_checked=hari_check.getId();
                        hari_text.setText(hari[hari_check.getId()]);
                        alert.show();
                    }
                    else
                    {
                        Iterator<WaktuPraktikModel> it = waktu_praktik.iterator();
                        while (it.hasNext()) {
                            WaktuPraktikModel user = it.next();
                            if (user.hari.equals(hari[hari_check.getId()])) {
                                it.remove();
                            }
                        }
                        hari_check.setText(hari[hari_check.getId()]);
                    }
                    System.out.println("HARI DIPILIH ADALAAAAAAAAAAAAAAAAAH : "+hari_checked);
                }
            });
        }

        for(WaktuPraktikModel waktu : waktu_praktik)
        {
            checkBoxArrayList.get((int)waktu.id_hari-1).setChecked(true);
            checkBoxArrayList.get((int)waktu.id_hari-1).setText(waktu.hari + " ("+waktu.jam_buka+" - "+waktu.jam_tutup+")");
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
