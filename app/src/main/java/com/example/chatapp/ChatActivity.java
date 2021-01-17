package com.example.chatapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

public class ChatActivity extends AppCompatActivity {

    //get the id and name of the friend user
    private String messageReceiverID, MessageReceiverName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        messageReceiverID = getIntent().getExtras().get("visit_user_id").toString();
        MessageReceiverName = getIntent().getExtras().get("visit_user_name").toString();

        Toast.makeText(this, messageReceiverID, Toast.LENGTH_SHORT).show();
        Toast.makeText(this, MessageReceiverName, Toast.LENGTH_SHORT).show();



    }
}