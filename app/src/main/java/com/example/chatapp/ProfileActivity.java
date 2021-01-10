package com.example.chatapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

public class ProfileActivity extends AppCompatActivity {

    private String receiverUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        //this is the unique id which is send to the activity by clicking on a friend and from this we will query every info about the friend and display it
        receiverUserID = getIntent().getExtras().get("visit_user_id").toString();

        Toast.makeText(this, "User id received: " + receiverUserID, Toast.LENGTH_SHORT).show();
    }
}