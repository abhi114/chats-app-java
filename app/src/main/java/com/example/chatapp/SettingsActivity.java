package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.connection.ListenHashProvider;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

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

    //request code for gallery Intent
    private static final int GalleryPic = 1;
    //create reference to user profile image
    private StorageReference UserProfileImageRef;
    //progress dialog for loading
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mAuth = FirebaseAuth.getInstance();
        //getting the current user id
        currentUserID = mAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        //Returns a new instance of StorageReference pointing to a child location of the current reference.
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images"); // creating a folder to store the profile images

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

        //click for profile image change
        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //send to mobile phone gallery
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT); //action_get_context - Allow the user to select a particular kind of data and return it.
                galleryIntent.setType("image/*");//This is used to create intents that only specify a type and not data, for example to indicate the type of data to return.
                // image/' = 'The MIME type of the data being handled by this intent. This value may be null.
                //By the help of android startActivityForResult() method, we can get result from another activity.
                startActivityForResult(galleryIntent,GalleryPic);
                //this result is catched in the onActivityResult()
            }
        });
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

                    userName.setText(retrieveUserName);
                    userStatus.setText(retrieveStatus);
                    Picasso.get().load(retrieveProfileImage).into(userProfileImage);

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
        loadingBar = new ProgressDialog(this);
    }

    private void SendUserToMainActivity(){
        Intent mainIntent = new Intent(SettingsActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //get the image selected
        //if the request code Intent matches
        if(requestCode == GalleryPic && resultCode == RESULT_OK && data != null){
            Uri ImageUri = data.getData(); //Retrieve data this intent is operating on.
            //his URI specifies the name of the data; often it uses the content:

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1); //aspect ratio for cropping
            //send the image to the crop activity
            CropImage.activity(ImageUri).start(this);

        }

        //this one is when the image is cropped
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK ){

                loadingBar.setTitle("Set Profile Image");
                loadingBar.setMessage("Please Wait , Your Profile Image is Updating...");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();
                //get the uri of the cropped image
                final Uri resultUri = result.getUri();


                //store in firebase database
                //Represents a reference to a Google Cloud Storage object. Developers can upload and download objects, get/set object metadata, and delete an object at a specified path.
                //we are storing the image by overwriting any previous image if present
                final StorageReference filePath = UserProfileImageRef.child(currentUserID + ".jpg");

                //put the file in database
                filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                final String downloadUrl = uri.toString();
                                rootRef.child("Users").child(currentUserID).child("image").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Toast.makeText(SettingsActivity.this, "Profile Image Stored to database", Toast.LENGTH_SHORT).show();
                                            loadingBar.dismiss();
                                        }else {
                                            String message = task.getException().toString();
                                            Toast.makeText(SettingsActivity.this, "Error :" + message, Toast.LENGTH_SHORT).show();
                                            loadingBar.dismiss();
                                        }
                                    }
                                });
                            }
                        });
                    }
                });


            }

        }

    }
}