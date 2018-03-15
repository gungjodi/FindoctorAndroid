package com.project.ta.findoctor.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.project.ta.findoctor.Adapter.ChannelClassAdapter;
import com.project.ta.findoctor.Interfaces.AsyncResponse;
import com.project.ta.findoctor.Models.ChatModel;
import com.project.ta.findoctor.Services.MyFirebaseApp;
import com.project.ta.findoctor.Utils.ItemClickSupport;
import com.project.ta.findoctor.Models.ChannelModel;
import com.project.ta.findoctor.R;
import com.project.ta.findoctor.Services.WebService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import java.util.regex.Pattern;

public class ChatMenuActivity extends AppCompatActivity {
    DatabaseReference mRef,chatRef;
    FirebaseUser user;
    ArrayList<ChannelModel> channelList;
    long userId = 0,akses,myID;
    String userName="";
    WebService getAccount;
    long receiverUid;
    int PICK_DOKTER_CHAT = 1,CHAT_TO_USER=2;
    FloatingActionButton fab;
    ArrayList<Long> idList = new ArrayList<>();
    ArrayList<String> readStatusList = new ArrayList<>();
    ArrayList<String> senderIDList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_menu);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fab = (FloatingActionButton) findViewById(R.id.fab);

        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null)
        {
            myID = ((MyFirebaseApp)this.getApplication()).getUser_id();
            getAccount = new WebService(ChatMenuActivity.this, "/getUserByEmail?email=" + user.getEmail(), new AsyncResponse() {
                @Override
                public void processFinish(String res) {
                    try {
                        // De-serialize the JSON string into an array of city objects
                        JSONArray jsonArray = new JSONArray(res);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObj = jsonArray.getJSONObject(i);
                            userId = Long.valueOf(jsonObj.getInt("id")) ;
                            userName = (jsonObj.getString("name")) ;
                            akses = Long.valueOf(jsonObj.getString("tipe")) ;
                            getChatRooms();
                        }
                        fab.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(ChatMenuActivity.this, PickDokterChatActivity.class);
                                intent.putExtra("userId",userId);
                                startActivityForResult(intent,PICK_DOKTER_CHAT);
                            }
                        });
                    } catch (JSONException e) {
                        Log.e("ERROR", "Error processing JSON", e);
                    }
                }
            });
            getAccount.execute();
        }
        else
        {
            finish();
            Intent i = new Intent(this, AccountActivity.class);
            startActivity(i);
        }

    }

    void getChatRooms()
    {
        if(akses==1)
        {
            fab.setVisibility(View.INVISIBLE);
        }
        mRef = FirebaseDatabase.getInstance().getReferenceFromUrl("https://findoctor-142603.firebaseio.com/chat_rooms");
        channelList = new ArrayList<>();

        final ValueEventListener channelListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                channelList.clear();
                for ( DataSnapshot channel:dataSnapshot.getChildren() ) {
                    String channelKey = channel.getKey();
                    String [] data = channelKey.split(Pattern.quote("_"));
                    if(Long.valueOf(data[0])==userId)
                    {
                        chatRef = FirebaseDatabase.getInstance().getReferenceFromUrl("https://findoctor-142603.firebaseio.com/chat_rooms/"+channelKey);
                        chatRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                ChatModel chat = dataSnapshot.getValue(ChatModel.class);
                                if(!senderIDList.contains(chat.senderUid))
                                {
                                    senderIDList.add(chat.senderUid);
                                }
                                readStatusList.add(chat.readStatus);

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                        receiverUid = Long.valueOf(data[1]);
                        if(!idList.contains(receiverUid))
                        {
                            idList.add((receiverUid));
                            refreshChannelList();
                        }

                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mRef.addValueEventListener(channelListener);
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

    public void onActivityResult(int requestCode,int resultCode,Intent data)
    {
        if(resultCode==RESULT_OK)
        {
            if(requestCode==PICK_DOKTER_CHAT)
            {
                long receiver = data.getLongExtra("id_dokter",0);
                String receiverName = data.getStringExtra("nama_dokter");
                Intent intent = new Intent(ChatMenuActivity.this,ChatUserActivity.class);
                intent.putExtra("userId",userId);
                intent.putExtra("receiverUid",receiver);
                intent.putExtra("receiverName",receiverName);
                intent.putExtra("userName",userName);
                startActivity(intent);
            }

        }
        if (resultCode==RESULT_CANCELED)
        {
        }
    }

    public void refreshChannelList()
    {
        String ids = idList.toString();
        ids = ids.replaceAll("[\\[\\](){}]","");
        ids = ids.replaceAll(" ","");
        getAccount = new WebService(ChatMenuActivity.this, "/getUserBySomeId?ids=" + ids, new AsyncResponse() {
            @Override
            public void processFinish(String res) {
                channelList.clear();
                try {
                    // De-serialize the JSON string into an array of city objects
                    JSONArray jsonArray = new JSONArray(res);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObj = jsonArray.getJSONObject(i);
                        ChannelModel receiver = new ChannelModel(jsonObj.getLong("id"),jsonObj.getString("name"),jsonObj.getString("email"),(myID),readStatusList.get(i),ChatMenuActivity.this);
                        channelList.add(receiver);
                    }
                    ChannelClassAdapter adapter = new ChannelClassAdapter(ChatMenuActivity.this, channelList);

                    RecyclerView channelListView = (RecyclerView) findViewById(R.id.channel_list);
                    channelListView.setAdapter(adapter);
                    channelListView.setAdapter(adapter);
                    channelListView.setLayoutManager(new LinearLayoutManager(ChatMenuActivity.this));

                    ItemClickSupport.addTo(channelListView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                        @Override
                        public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                            long id_rekam = channelList.get(position).receiverUid;
                            String receiverName = channelList.get(position).receiverName;
                            Intent intent = new Intent(ChatMenuActivity.this,ChatUserActivity.class);
                            intent.putExtra("userId",userId);
                            intent.putExtra("receiverUid",id_rekam);
                            intent.putExtra("receiverName",receiverName);
                            intent.putExtra("userName",userName);
                            startActivityForResult(intent,CHAT_TO_USER);
                        }
                    });
                } catch (JSONException e) {
                    Log.e("ERROR", "Error processing JSON", e);
                }
            }
        });
        getAccount.execute();

    }
}

