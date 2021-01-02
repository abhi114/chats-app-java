package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {


    private Toolbar mToolbar;
    private ViewPager myViewPager; //view pager for scrolling between tabs
    private TabLayout myTabLayout; // for displaying the tabs
    private TabsAccessorAdapter myTabsAccessorAdapter; // adapter for viewing the tabs
    private FirebaseUser currentUser; //Represents a user's profile information in your Firebase project's user database.
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();



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

    //now to add the options at the appbar


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.options_menu,menu); //Interface for managing the items in a menu.


        return true;
    }

    //now to get the selected option


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        //matching the id of selected xml with each one
        if(item.getItemId() == R.id.main_logout_option){
            mAuth.signOut();
            SendUserToLoginActivity();
        }
        if(item.getItemId() == R.id.main_settings_option){

        }
        if(item.getItemId() == R.id.main_find_friends_option){

        }
        return  true;
    }
}