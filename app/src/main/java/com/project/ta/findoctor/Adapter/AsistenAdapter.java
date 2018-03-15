package com.project.ta.findoctor.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.project.ta.findoctor.Activity.MenuActivity;
import com.project.ta.findoctor.Activity.RekamMedikActivity;
import com.project.ta.findoctor.Models.AsistenModel;
import com.project.ta.findoctor.Models.SearchDokterModel;
import com.project.ta.findoctor.R;
import com.project.ta.findoctor.Utils.PicassoCache;

import java.util.Collections;
import java.util.List;

public class AsistenAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private LayoutInflater inflater;
    List<AsistenModel> data= Collections.emptyList();
    SearchDokterModel current;
    int currentPos=0;

    // create constructor to initialize context and data sent from MainActivity
    public AsistenAdapter(Context context, List<AsistenModel> data){
        this.context=context;
        inflater= LayoutInflater.from(context);
        this.data=data;
    }

    // Inflate the layout when ViewHolder created
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view=inflater.inflate(R.layout.content_search, parent,false);
        MyHolder holder=new MyHolder(view);
        return holder;
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        holder.itemView.setOnLongClickListener(null);
        super.onViewRecycled(holder);
    }
    // Bind data
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        // Get current position of item in RecyclerView to bind data and assign values from list
        final MyHolder myHolder= (MyHolder) holder;
        final AsistenModel current=data.get(position);

        if(current.is_aktif==1)
        {
            myHolder.nama.setText(current.name+" ("+current.email+")");
            myHolder.kategori.setText("Aktif");
        }
        else if(current.is_aktif==0)
        {
            myHolder.nama.setText(current.name+" ("+current.email+")");
            myHolder.kategori.setText("Tidak Aktif");
        }
        else
        {
            myHolder.nama.setText(current.name);
            myHolder.kategori.setText(current.email);
        }
        myHolder.activity = current.activity;

        FirebaseStorage storage = FirebaseStorage.getInstance();
        String ext =  "jpg";
        StorageReference storageRef = storage.getReferenceFromUrl("gs://findoctor-142603.appspot.com/");
        final StorageReference imagesRef = storageRef.child("images/"+current.id_asisten+"/display_picture."+ext);
        imagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                PicassoCache.getPicassoInstance(current.activity).load(uri.toString()).into(myHolder.foto);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    // return total item from List
    @Override
    public int getItemCount() {
        return data.size();
    }


    class MyHolder extends RecyclerView.ViewHolder {
        TextView nama;
        TextView kategori;
        ImageView foto ;
        Activity activity;
        // create constructor to get widget reference
        public MyHolder(View itemView) {
            super(itemView);
            nama= (TextView) itemView.findViewById(R.id.nama_dokter_list);
            kategori = (TextView) itemView.findViewById(R.id.spesialis_dokter_list);
            foto = (ImageView) itemView.findViewById(R.id.imageDokter);
        }
    }

}
