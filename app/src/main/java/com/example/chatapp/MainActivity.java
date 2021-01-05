package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {


    private Toolbar mToolbar;
    private ViewPager myViewPager; //view pager for scrolling between tabs
    private TabLayout myTabLayout; // for displaying the tabs
    private TabsAccessorAdapter myTabsAccessorAdapter; // adapter for viewing the tabs
    private FirebaseUser currentUser; //Represents a user's profile information in your Firebase project's user database.
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        RootRef = FirebaseDatabase.getInstance().getReference();



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
        // verify if user has give his username or not
        else{
            VerifyUserExistence();

        }
    }

    private void VerifyUserExistence() {
        //get the specific id of the user to check whether the id has the fields of username
        String currentUserId = mAuth.getCurrentUser().getUid();
        //now check using root ref
        //Add a listener for changes in the data at this location. Each time time the data changes, your listener will be called with an immutable snapshot of the data.

        RootRef.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //if the user name is available in the database
                if((dataSnapshot.child("name").exists())){
                    Toast.makeText(MainActivity.this, "Welcome", Toast.LENGTH_SHORT).show();

                }else{
                    SendUserToSettingsActivity();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });




    }

    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this,LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
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
            SendUserToSettingsActivity();


        }
        if(item.getItemId() == R.id.main_create_group_option){
            //create new group
            requestNewGroup();

        }
        if(item.getItemId() == R.id.main_find_friends_option){

        }
        return  true;
    }

    private void requestNewGroup()
    {
        //dialog to enter the group name
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this,R.style.AlertDialog); // using the theme resource
        //now styling the alert dialog
        builder.setTitle("Enter Group Name :");
        //edit text field
        final EditText groupNameField = new EditText(MainActivity.this);
        groupNameField.setHint("e.g Friends");
        builder.setView(groupNameField);

        //add 2 buttons
        //if user clicks on the create button
        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //take the group name from the edit text
                String groupName = groupNameField.getText().toString();
                //if user dosen't entered the group name
                if(TextUtils.isEmpty(groupName)){
                    Toast.makeText(MainActivity.this, "Please Write Group Name", Toast.LENGTH_SHORT).show();

                }else{
                    //if provided then add to database

                    CreateNewGroup(groupName);

                }
            }
        });

        //if clicked on cancel button

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //using the dialog field we can close the dialog
                dialog.cancel();
            }
        });
        //show the dialog to the user
        builder.show();

        



    }

    private void CreateNewGroup(final String groupName)
    {   //creating new child
        RootRef.child("Groups").child(groupName).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                //tell the user that the group is created
                if(task.isSuccessful()){
                    Toast.makeText(MainActivity.this, groupName+" is Created Successfully", Toast.LENGTH_SHORT).show();
                }

            }
        }); //group name is the key value

    }

    private void SendUserToSettingsActivity() {
        Intent SettingIntent = new Intent(MainActivity.this,SettingsActivity.class);
        //we dont want the user to go back to the main activity so added the flags
        SettingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(SettingIntent);
        finish();
    }
}