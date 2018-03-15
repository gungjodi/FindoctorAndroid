package com.project.ta.findoctor.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.project.ta.findoctor.Activity.MapsActivity;
import com.project.ta.findoctor.Models.DetailDokterModel;
import com.project.ta.findoctor.Models.SearchDokterModel;
import com.project.ta.findoctor.R;
import com.project.ta.findoctor.Utils.MethodLib;
import com.project.ta.findoctor.Utils.PicassoCache;

import java.util.Collections;
import java.util.List;

/**
 * Created by GungJodi on 7/24/2017.
 */

public class DetailDokterAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private LayoutInflater inflater;
    List<DetailDokterModel> data= Collections.emptyList();
    // create constructor to initialize context and data sent from MainActivity
    public DetailDokterAdapter(Context context, List<DetailDokterModel> data){
        this.context=context;
        inflater= LayoutInflater.from(context);
        this.data=data;
    }

    // Inflate the layout when ViewHolder created
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view=inflater.inflate(R.layout.detail_dokter_klinik_list, parent,false);
        DetailDokterAdapter.MyHolder holder=new DetailDokterAdapter.MyHolder(view);
        return holder;
    }

    // Bind data
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        // Get current position of item in RecyclerView to bind data and assign values from list
        final DetailDokterAdapter.MyHolder myHolder= (DetailDokterAdapter.MyHolder) holder;
        final DetailDokterModel current=data.get(position);
        myHolder.nama.setText(current.nama_klinik);
        myHolder.kategori.setText("Klinik "+current.jenis_klinik);
        myHolder.jarak.setText(current.jarak);
        myHolder.activity = current.activity;
        if(current.is_buka==0)
        {
            myHolder.isBuka.setImageResource(android.R.drawable.presence_busy);
        }
        myHolder.gotomap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MapsActivity.class);
                intent.putExtra("gotoLat",current.latitude);
                intent.putExtra("gotoLong",current.longitude);
                intent.putExtra("id_klinik",current.id_klinik);
                context.startActivity(intent);
            }
        });

    }

    // return total item from List
    @Override
    public int getItemCount() {
        return data.size();
    }


    class MyHolder extends RecyclerView.ViewHolder{
        TextView nama;
        TextView kategori;
        TextView jarak;
        ImageView gotomap;
        ImageView isBuka;
        Activity activity;

        public MyHolder(View itemView) {
            super(itemView);
            nama= (TextView) itemView.findViewById(R.id.nama_dokter_list);
            kategori = (TextView) itemView.findViewById(R.id.spesialis_dokter_list);
            jarak = (TextView) itemView.findViewById(R.id.jarak_dokter_text);
            gotomap = (ImageView) itemView.findViewById(R.id.goto_map);
            isBuka = (ImageView) itemView.findViewById(R.id.is_buka_image);
        }
    }

}
