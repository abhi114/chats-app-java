package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String receiverUserID;

    private CircleImageView userProfileImage;
    private TextView userProfileName , userProfileStatus;
    private Button SendMessageRequestButton;
    //reference to database
    private DatabaseReference UserRef,ChatRequestRef;
    private FirebaseAuth mAuth;

    String Current_State , senderUserID; //senderuserid to prevent the user for sending the message to himself



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        //this is the unique id which is send to the activity by clicking on a friend and from this we will query every info about the friend and display it
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        //creates a new reference to the database with the name of chat requests
        ChatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        receiverUserID = getIntent().getExtras().get("visit_user_id").toString();
        //to stop the user to send message to himself
        senderUserID = mAuth.getCurrentUser().getUid();


        //Toast.makeText(this, "User id received: " + receiverUserID, Toast.LENGTH_SHORT).show();

        userProfileImage = (CircleImageView) findViewById(R.id.visit_profile_image);
        userProfileName = (TextView) findViewById(R.id.visit_user_name);
        userProfileStatus = (TextView) findViewById(R.id.visit_profile_status);
        SendMessageRequestButton = (Button) findViewById(R.id.send_message_request_button);
        Current_State = "new" ;//two users are new to each other


        //retrieve info from database
        RetrieveUserInfo();
    }

    private void RetrieveUserInfo() {
        UserRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //if that unique id exists
                //if the users has given the image for the status
                if((dataSnapshot.exists()) && (dataSnapshot.hasChild("image"))){
                    //url to download the image
                    String userImage = dataSnapshot.child("image").getValue().toString();
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userProfileImage);
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    //send chat request
                    ManageChatRequests();

                }else{
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    ManageChatRequests();

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    private void ManageChatRequests() {
        //to check that the button should remain cancel request once the request is sent
        ChatRequestRef.child(senderUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //retrieve the request type
                //if the senderuserId contains the reciever userid it means that chat request has been send
                if(dataSnapshot.hasChild(receiverUserID)){
                    String request_type = dataSnapshot.child(receiverUserID).child("request_type").getValue().toString();

                    if(request_type.equals("sent")){
                        Current_State = "request_sent";
                        SendMessageRequestButton.setText("Cancel Chat Request");
                    }
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //not to send message to you id
        if(!senderUserID.equals(receiverUserID)){

            SendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SendMessageRequestButton.setEnabled(false);

                    if(Current_State.equals("new")){
                        //if the two users are new to each other
                        SendChatRequest();

                    }
                }
            });

        }else{
            SendMessageRequestButton.setVisibility(View.INVISIBLE);

        }

    }

    private void SendChatRequest() {
        //database reference to the new parent node
        //it will create the sub-parts with sender it and the friends id
        ChatRequestRef.child(senderUserID).child(receiverUserID).child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    //when the message request is send from the user successfully add in the database the reciever user id with sender user id to show that reciever got the request
                    ChatRequestRef.child(receiverUserID).child(senderUserID).child("request_type").setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                SendMessageRequestButton.setEnabled(true);
                                Current_State = "request_sent";
                                SendMessageRequestButton.setText("Cancel Chat Request");
                            }
                        }
                    });
                }
            }
        });
    }
}