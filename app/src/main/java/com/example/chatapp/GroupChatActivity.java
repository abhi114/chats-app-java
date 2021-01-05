package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChatActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageButton sendMessageButton;
    private EditText userMessageInput;
    private ScrollView mScrollView;
    private TextView  displayTextMessages;
    //firebase auth for id
    private FirebaseAuth mAuth;
    //database reference to link to the root node
    private DatabaseReference UsersRef;
    //database reference for group
    private DatabaseReference GroupNameRef,GroupMessageKeyRef;

    private String currentGroupName , currentUserID , currentUserName; // the group opened
    //for group chat
    //date
    private String currentDate,currentTime;



    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);
        currentGroupName = getIntent().getExtras().get("groupName").toString();

        Toast.makeText(this, currentGroupName, Toast.LENGTH_SHORT).show();

        //getting the current users unique id
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users"); // getting the root node of the users
        //getting the reference of the current group name
        GroupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);


        //getting the intent


        InitializeFields();

        getUserInfo();

        //when send message button is clicked
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveMessageInfoToDatabase();
                //empty the field after sending the message
                userMessageInput.setText("");
            }
        });




    }

    //on start to display the previous messages once the group is clicked

    @Override
    protected void onStart() {
        super.onStart();
        //reference to the group where are at
        GroupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            //This method is triggered when a new child is added to the location to which this listener was added.
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists()){
                    //if messages exists
                    DisplayMessages(dataSnapshot);
                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists()){
                    //if messages exists
                    DisplayMessages(dataSnapshot);
                }

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }




    //we first created a unique key for the message to be sent inside the Group field in the database , then we created a reference of that unique message key (messageKey) by the name of GroupMessageKeyref and then we entered the value

    private void SaveMessageInfoToDatabase() {
        String message = userMessageInput.getText().toString();
        //Create a reference to an auto-generated child location.
        // Locations generated on a single client will be sorted in the order that they are created, and will be sorted approximately in order across all clients.
        String messageKey = GroupNameRef.push().getKey();
        //get key- The last token in the location pointed to by this reference or null if this reference points to the database root


        if(TextUtils.isEmpty(message)){
            Toast.makeText(this, "Please write a Message First...", Toast.LENGTH_SHORT).show();

        }else{
            Calendar calForDate = Calendar.getInstance(); //first crreate and instance of calender class
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, yyyy"); //then define the format
            currentDate = currentDateFormat.format(calForDate.getTime());//gets the date

            //now for time
            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
            currentTime = currentTimeFormat.format(calForTime.getTime());

            //now save it in database

            //HashMap<String,Object> groupMessageKey = new HashMap<>();
            //GroupNameRef.updateChildren(groupMessageKey);

            GroupMessageKeyRef = GroupNameRef.child(messageKey);

            HashMap<String , Object> messageInfoMap = new HashMap<>();
            messageInfoMap.put("name",currentUserName);
            messageInfoMap.put("message",message);
            messageInfoMap.put("date",currentDate);
            messageInfoMap.put("time",currentTime);
            GroupMessageKeyRef.updateChildren(messageInfoMap);

        }
    }


    private void InitializeFields() {
        mToolbar = (Toolbar) findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(currentGroupName);

        sendMessageButton = (ImageButton) findViewById(R.id.send_message_button);
        userMessageInput = (EditText) findViewById(R.id.input_group_message);
        displayTextMessages = (TextView) findViewById(R.id.group_chat_text_display);
        mScrollView = (ScrollView) findViewById(R.id.my_scroll_view);
    }

    private void getUserInfo() {
        UsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    //it means user is there
                    currentUserName = dataSnapshot.child("name").getValue().toString();


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }
    private void DisplayMessages(DataSnapshot dataSnapshot) {
        //displaying the message using iterator
        Iterator iterator = dataSnapshot.getChildren().iterator();
        while(iterator.hasNext()){
            //iterator.next will only give the key get value will give the value for that date field
            String chatDate = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatMessage = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatName = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatTime = (String) ((DataSnapshot)iterator.next()).getValue();

            //display
            displayTextMessages.append(chatName + " :\n" + chatMessage + "\n" + chatTime + "    " + chatDate + "\n\n\n");

        }
    }
}