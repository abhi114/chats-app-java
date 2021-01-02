package com.example.chatapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {


    private Toolbar mToolbar;
    private ViewPager myViewPager; //view pager for scrolling between tabs
    private TabLayout myTabLayout; // for displaying the tabs
    private TabsAccessorAdapter myTabsAccessorAdapter; // adapter for viewing the tabs
    private FirebaseUser currentUser; //Represents a user's profile information in your Firebase project's user database.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        //setting the toolbar as the main app bar of the app
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("ChatApp");

        //getting the pager defined in main activity.xml
        myViewPager = (ViewPager) findViewById(R.id.main_tabs_pager);
        //Return the FragmentManager for interacting with fragments associated with this activity.
        myTabsAccessorAdapter = new TabsAccessorAdapter(getSupportFragmentManager());
        //setting the adapter
        myViewPager.setAdapter(myTabsAccessorAdapter);


        myTabLayout = (TabLayout) findViewById(R.id.main_tabs);
        myTabLayout.setupWithViewPager(myViewPager);

    }
    // authentication
    @Override
    protected void onStart() {
        super.onStart();
        //it means the user in not authenticated
        if(currentUser == null){
            SendUserToLoginActivity();
        }
    }

    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this,LoginActivity.class);
        startActivity(loginIntent);
    }
}