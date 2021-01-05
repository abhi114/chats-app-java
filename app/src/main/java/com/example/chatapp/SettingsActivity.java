package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.connection.ListenHashProvider;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Button updateAccountSettings;
    private EditText userName, userStatus;
    private CircleImageView userProfileImage; // the instance of the circle image class added in the dependencies
    //get the user id
    private String currentUserID;
    private FirebaseAuth mAuth;
    //for saving the data
    private DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mAuth = FirebaseAuth.getInstance();
        //getting the current user id
        currentUserID = mAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();

        InitializeFields();

        updateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //method to update the profile details
                UpdateSettings();
            }
        });
        //to get back the previous info of the username and status
        RetrieveUserInfo();
    }

    //retrieve
    private void RetrieveUserInfo() {
        rootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //if the user exists and has name and image field
                //since status and username are compulsory
                if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name") && (dataSnapshot.hasChild("image")))){
                    //now display all the three as default
                    String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                    String retrieveStatus = dataSnapshot.child("status").getValue().toString();
                    String retrieveProfileImage = dataSnapshot.child("image").getValue().toString();

                }else if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name"))){
                    String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                    String retrieveStatus = dataSnapshot.child("status").getValue().toString();

                    userName.setText(retrieveUserName);
                    userStatus.setText(retrieveStatus);


                }else{ //if nothing is set in the settings it means its a new account
                    Toast.makeText(SettingsActivity.this, "Please set & update your profile information...", Toast.LENGTH_SHORT).show();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void UpdateSettings() {
        String setUserName = userName.getText().toString();
        String setUserStatus = userStatus.getText().toString();

        if(TextUtils.isEmpty(setUserName)){
            Toast.makeText(this, "Please write your username", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(setUserStatus)){
            Toast.makeText(this, "Please write your status..", Toast.LENGTH_SHORT).show();
        }else{
            //saving the data in the firebase using hashmap -> key value pair
            HashMap<String,String> profileMap = new HashMap<>(); //values for name and status
            profileMap.put("uid",currentUserID);
            profileMap.put("name",setUserName); // this id should be save as the one used for retrieving
            profileMap.put("status",setUserStatus); //whole profilemap contains the id name and status

            //now adding the value to the database
            rootRef.child("Users").child(currentUserID).setValue(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    //listener is used to check if the uploading task is successful or not
                    if(task.isSuccessful()){

                        SendUserToMainActivity();
                        Toast.makeText(SettingsActivity.this, "Task Completed Successfully....", Toast.LENGTH_SHORT).show();


                    }else{
                        String message = task.getException().toString();
                        Toast.makeText(SettingsActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();
                    }

                //after this is completed send the user to main activity
                }
            });

        }
    }

    private void InitializeFields() {
        updateAccountSettings = (Button) findViewById(R.id.update_settings_button);
        userName = (EditText) findViewById(R.id.set_user_name);
        userStatus = (EditText) findViewById(R.id.set_profile_status);
        userProfileImage = (CircleImageView) findViewById(R.id.profile_image);
    }

    private void SendUserToMainActivity(){
        Intent mainIntent = new Intent(SettingsActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}