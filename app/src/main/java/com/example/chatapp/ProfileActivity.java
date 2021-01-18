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

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String receiverUserID;

    private CircleImageView userProfileImage;
    private TextView userProfileName , userProfileStatus;
    private Button SendMessageRequestButton, DeclineMessageRequestButton;
    //reference to database
    private DatabaseReference UserRef,ChatRequestRef;
    private FirebaseAuth mAuth;

    String Current_State , senderUserID; //senderuserid to prevent the user for sending the message to himself
    //to show in contacts fragment
    private DatabaseReference ContactsRef;
    //reference to create the notification node
    private DatabaseReference NotificationRef;



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
        //for the contacts
        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        NotificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");


        //Toast.makeText(this, "User id received: " + receiverUserID, Toast.LENGTH_SHORT).show();

        userProfileImage = (CircleImageView) findViewById(R.id.visit_profile_image);
        userProfileName = (TextView) findViewById(R.id.visit_user_name);
        userProfileStatus = (TextView) findViewById(R.id.visit_profile_status);
        SendMessageRequestButton = (Button) findViewById(R.id.send_message_request_button);
        Current_State = "new" ;//two users are new to each other
        //decline request button
        DeclineMessageRequestButton = (Button) findViewById(R.id.decline_message_request_button);


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
                    //this check is possible because sender is the current user id
                    else if(request_type.equals("received")){
                        Current_State = "request_received";
                        //now this button will see from the receiver point of view
                        SendMessageRequestButton.setText("Accept Chat Request");
                        //decline button
                        DeclineMessageRequestButton.setVisibility(View.VISIBLE);
                        DeclineMessageRequestButton.setEnabled(true);

                        //click listener to decline the request
                        DeclineMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //cancel method was already defined for the sender side it will be the same for the reciever side
                                //it will make the cancel request button invisible
                                CancelChatRequest();
                            }
                        });

                    }
                }else{
                    //if the receiver user id child has been removed it means that the request has been accepted
                    //and it present in the contacts
                    //so that the button remains same everytime we click on that friend
                    ContactsRef.child(senderUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            //You can use the onDataChange() method to read a static snapshot of the contents at a given path, as they existed at the time of the event.

                            if(dataSnapshot.hasChild(receiverUserID)){
                                Current_State = "friends";
                                SendMessageRequestButton.setText("Remove This Contact");
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
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
                    if(Current_State.equals("request_sent")){
                        //if the request is already sent
                        CancelChatRequest();
                    }
                    if(Current_State.equals("request_received")){
                        //if the receiver accepts the chat request
                        AcceptChatRequest();

                    }
                    //it means that the user are friends and this button has the label of remove from contacs
                    if(Current_State.equals("friends")){
                        RemoveSpecificContact();
                    }
                }
            });

        }else{
            SendMessageRequestButton.setVisibility(View.INVISIBLE);

        }

    }

    private void RemoveSpecificContact() {

        //remove from contacts in  databse
        ContactsRef.child(senderUserID).child(receiverUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    ContactsRef.child(receiverUserID).child(senderUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                //set the button text back to normal
                                SendMessageRequestButton.setEnabled(true);
                                Current_State = "new";
                                SendMessageRequestButton.setText("Send Message Request");

                                DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                DeclineMessageRequestButton.setEnabled(false);
                            }

                        }
                    });
                }

            }
        });
    }

    private void AcceptChatRequest() {
        //we will show that accepted person in the contacts fragment
        //Contacts ref is referencing the a new parent node of contacts
        //it is saved to the senders

        //it is because the sender will store the child of receiver to show the contacts of receiver and receiver will store the child of sender to show in contacts
        ContactsRef.child(senderUserID).child(receiverUserID).child("Contacts").setValue("Saved")
        .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    //now save it for the receiver because we have to show it to both the users
                    ContactsRef.child(receiverUserID).child(senderUserID).child("Contacts").setValue("Saved")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    //remove the chat request from the database
                                    if(task.isSuccessful()){
                                        //remove the chat request from the chats parent node
                                        ChatRequestRef.child(senderUserID).child(receiverUserID).removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                //now remove the request from the receiver end
                                                if(task.isSuccessful()){
                                                    ChatRequestRef.child(receiverUserID).child(senderUserID).removeValue()
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    //now the chat request is deleted from both the end
                                                                    SendMessageRequestButton.setEnabled(true);
                                                                    Current_State = "friends";
                                                                    //now both the users have the choice to remove them from their contacts
                                                                    SendMessageRequestButton.setText("Remove This Contact");
                                                                    DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                                    DeclineMessageRequestButton.setEnabled(false);


                                                                }
                                                            });
                                                }

                                            }
                                        });
                                    }


                                }
                            });


                }
            }
        });

    }

    private void CancelChatRequest() {
        //remove the sent value from the sender and the reciever in the database
        ChatRequestRef.child(senderUserID).child(receiverUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    ChatRequestRef.child(receiverUserID).child(senderUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                //make the button back to normal
                                SendMessageRequestButton.setEnabled(true);
                                Current_State = "new";
                                SendMessageRequestButton.setText("Send Message Request");
                                //for the receiver of the request if the reciever clicks on cancel request we have to remove the cancel request button

                                DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                DeclineMessageRequestButton.setEnabled(false);
                            }
                        }
                    });
                }


            }
        });
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
                                //we will create a new node for notification feature of the request
                                HashMap<String,String> chatNotificationMap = new HashMap<>();
                                //from which user we are recieving the request
                                chatNotificationMap.put("from",senderUserID);
                                chatNotificationMap.put("type","request");

                                NotificationRef.child(receiverUserID).push() //create a child for the receiver of the request with a unique key as its child
                                .setValue(chatNotificationMap).addOnCompleteListener(new OnCompleteListener<Void>() {
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
        });
    }
}