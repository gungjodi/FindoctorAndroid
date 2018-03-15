package com.project.ta.findoctor.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.project.ta.findoctor.Models.ChatModel;
import com.project.ta.findoctor.R;
import com.project.ta.findoctor.Utils.Constants;
import com.project.ta.findoctor.Utils.MethodLib;
import com.project.ta.findoctor.Utils.SendBulkNotif;

import java.util.HashMap;
import java.util.Map;


public class ChatUserActivity extends AppCompatActivity {
    private static final String TAG = "GET MESSAGE : ";
    DatabaseReference mRef1;
    DatabaseReference mRef2;
    String userName,receiverName;
    private String mTime;
    long userId,receiverUid;
    RecyclerView mMessages;
    FirebaseRecyclerAdapter<ChatModel, ChatHolder> mRecycleViewAdapter;
    LinearLayoutManager layoutManager;
    Query mChatRef1;
    Button mSendButton;
    EditText mMessageEdit;
    public void showToast(String text)
    {
        Toast.makeText(this,text,Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_user);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent detail = getIntent();
        userName = detail.getStringExtra("userName");
        userId = detail.getLongExtra("userId",0);
        receiverUid = detail.getLongExtra("receiverUid",0);
        receiverName= detail.getStringExtra("receiverName");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        fab.setVisibility(View.INVISIBLE);
        getMessageFromFirebaseUser(String.valueOf(userId),String.valueOf(receiverUid));

        mMessages = (RecyclerView) findViewById(R.id.messagesList);
        mMessages.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        mMessages.setLayoutManager(layoutManager);

        showToast("SENDER : "+userId+userName+" RECEIVER : "+receiverUid+receiverName);

        checkNewMessage(String.valueOf(userId),String.valueOf(receiverUid));

        mSendButton = (Button) findViewById(R.id.sendButton);
        mMessageEdit = (EditText) findViewById(R.id.messageEdit);

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatModel chat = new ChatModel(userName,receiverName,String.valueOf(userId),String.valueOf(receiverUid),mMessageEdit.getText().toString(),System.currentTimeMillis(), mTime, Constants.READ_STATUS_FALSE);
                sendMessageToFirebaseUser(ChatUserActivity.this,chat,"");
                mMessageEdit.setText("");
            }
        });
    }

    public void checkNewMessage(final String senderUid, final String receiverUid)
    {
        final String room_type_1 =  senderUid + "_" + receiverUid;
        final DatabaseReference check = FirebaseDatabase.getInstance().getReferenceFromUrl("https://findoctor-142603.firebaseio.com/chat_rooms/"+room_type_1);
        check.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                layoutManager.smoothScrollToPosition(mMessages,null,layoutManager.getItemCount()+1);
                if(layoutManager.getItemCount()==0)
                {
                    getMessageFromFirebaseUser(senderUid,receiverUid);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void getMessageFromFirebaseUser(final String senderUid, String receiverUid) {
        final String room_type_1 =  senderUid + "_" + receiverUid;
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("chat_rooms")
                .getRef()
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(room_type_1)) {
                            mRef1 = FirebaseDatabase.getInstance().getReferenceFromUrl("https://findoctor-142603.firebaseio.com/chat_rooms/"+room_type_1);
                            mChatRef1 = mRef1.limitToLast(50);

                            mRecycleViewAdapter = new FirebaseRecyclerAdapter<ChatModel, ChatHolder>(ChatModel.class, R.layout.text_message, ChatHolder.class, mChatRef1) {
                                @Override
                                public void populateViewHolder(ChatHolder chatView, final ChatModel chat, int position) {
                                    chatView.setText(chat.message);
                                    chatView.setName(chat.sender);
                                    chatView.setTime(chat.getFormattedTime());
                                    if (chat.senderUid.equals(String.valueOf(userId))) {
                                        chatView.setIsSender(true);
                                    } else {
                                        chatView.setIsSender(false);
                                    }
                                }
                            };
                        }
                        mMessages.setAdapter(mRecycleViewAdapter);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Unable to get message
                    }
                });

    }

    public void sendMessageToFirebaseUser(final Context context, final ChatModel chat, final String receiverFirebaseToken) {
        final String room_type_1 = chat.senderUid + "_" + chat.receiverUid;
        final String room_type_2 = chat.receiverUid + "_" + chat.senderUid;
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("chat_rooms")
                .child(room_type_1)
                .push()
                .setValue(chat).addOnCompleteListener(ChatUserActivity.this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                databaseReference.child("chat_rooms")
                        .child(room_type_2)
                        .push()
                        .setValue(chat)
                        .addOnCompleteListener(ChatUserActivity.this, new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                SendBulkNotif.send(Long.valueOf(chat.receiverUid),Long.valueOf(chat.senderUid),chat.sender,chat.receiver,chat.message);
                            }
                        });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(layoutManager.getItemCount()>0)
        {
            mRecycleViewAdapter.cleanup();
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


    public static class ChatHolder extends RecyclerView.ViewHolder {
        View mView;

        public ChatHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setIsSender(Boolean isSender) {
            FrameLayout left_arrow = (FrameLayout) mView.findViewById(R.id.left_arrow);
            FrameLayout right_arrow = (FrameLayout) mView.findViewById(R.id.right_arrow);
            RelativeLayout messageContainer = (RelativeLayout) mView.findViewById(R.id.message_container);
            LinearLayout message = (LinearLayout) mView.findViewById(R.id.message);


            if (isSender) {
                left_arrow.setVisibility(View.GONE);
                right_arrow.setVisibility(View.VISIBLE);
                messageContainer.setGravity(Gravity.RIGHT);
            } else {
                left_arrow.setVisibility(View.VISIBLE);
                right_arrow.setVisibility(View.GONE);
                messageContainer.setGravity(Gravity.LEFT);
            }
        }

        public void setName(String name) {
            TextView field = (TextView) mView.findViewById(R.id.name_text);
            field.setText(name);
        }

        public void setText(String text) {
            TextView field = (TextView) mView.findViewById(R.id.message_text);
            field.setText(text);
        }

        public void setTime(String time) {
            TextView field = (TextView) mView.findViewById(R.id.time_text);
            field.setText(time);
        }
    }

}
