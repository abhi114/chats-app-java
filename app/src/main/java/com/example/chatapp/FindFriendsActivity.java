package com.example.chatapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

public class FindFriendsActivity extends AppCompatActivity {

    //all the fields of xml
    private Toolbar mToolbar;
    private RecyclerView FindFriendsRecyclerList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        FindFriendsRecyclerList = (RecyclerView) findViewById(R.id.find_friends_recycler_list);
        FindFriendsRecyclerList.setLayoutManager(new LinearLayoutManager(this));


        mToolbar = (Toolbar) findViewById(R.id.find_friends_toolbar);
        setSupportActionBar(mToolbar);
        //true to show the user that selecting home will return one level up rather than to the top level of the app.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //Set whether to include the application home affordance in the action bar.
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Friends");
        //now specifiy to which activity we have to send when the user clicks on the back button



    }
}