package com.project.ta.findoctor.Adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.project.ta.findoctor.Models.ChannelModel;
import com.project.ta.findoctor.R;
import com.project.ta.findoctor.Utils.PicassoCache;

import java.util.Collections;
import java.util.List;

public class ChannelClassAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private LayoutInflater inflater;
    List<ChannelModel> data= Collections.emptyList();
    ChannelModel current;
    int currentPos=0;

    // create constructor to initialize context and data sent from MainActivity
    public ChannelClassAdapter(Context context, List<ChannelModel> data){
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

    // Bind data
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        // Get current position of item in RecyclerView to bind data and assign values from list
        final MyHolder myHolder= (MyHolder) holder;
        final ChannelModel current=data.get(position);
        final String readStatus = data.get(position).readStatus;

        myHolder.nama.setText(current.receiverName);
        myHolder.email.setText(current.receiverEmail);
        myHolder.activity = current.activity;

//        if(current.receiverUid!=current.senderID)
//        {
//            if (readStatus.equals("false"))
//            {
//                currentPos+=1;
//                myHolder.nama.setText(current.receiverName+" "+currentPos);
//            }
//        }


        FirebaseStorage storage = FirebaseStorage.getInstance();
        String ext =  "jpg";
        StorageReference storageRef = storage.getReferenceFromUrl("gs://findoctor-142603.appspot.com/");
        final StorageReference imagesRef = storageRef.child("images/"+current.receiverUid+"/display_picture."+ext);
        imagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                PicassoCache.getPicassoInstance(current.activity).load(uri.toString()).into(myHolder.foto);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(current.activity,e.getMessage(),Toast.LENGTH_SHORT).show();
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
        TextView email;
        ImageView foto ;
        Activity activity;
        // create constructor to get widget reference
        public MyHolder(View itemView) {
            super(itemView);
            nama= (TextView) itemView.findViewById(R.id.nama_dokter_list);
            email = (TextView) itemView.findViewById(R.id.spesialis_dokter_list);
            foto = (ImageView) itemView.findViewById(R.id.imageDokter);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(activity,"ID DOKTER : "+v.getId(),Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

}
