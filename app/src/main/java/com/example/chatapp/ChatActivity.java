package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    //get the id and name,image of the friend user
    private String messageReceiverID, MessageReceiverName,MessageReceiverImage;

    //members of the private chat bar xml
    private TextView userName , userLastSeen;
    private CircleImageView userImage;

    private Toolbar ChatToolBar;
    //send message button
    private ImageButton SendMessageButton;
    private EditText MessageInputText;

    //for the current user id
    private FirebaseAuth mAuth;
    private String MessageSenderID; // current user id

    //reference to the root
    private DatabaseReference RootRef;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        MessageSenderID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference(); // reference to the root of the database

        messageReceiverID = getIntent().getExtras().get("visit_user_id").toString();
        MessageReceiverName = getIntent().getExtras().get("visit_user_name").toString();
        MessageReceiverImage = getIntent().getExtras().get("visit_image").toString();


        //Toast.makeText(this, messageReceiverID, Toast.LENGTH_SHORT).show();
        //Toast.makeText(this, MessageReceiverName, Toast.LENGTH_SHORT).show();

        InitializeControllers();

        userName.setText(MessageReceiverName);
        Picasso.get().load(MessageReceiverImage).placeholder(R.drawable.profile_image).into(userImage);

        //click listener for the button
        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessage();

            }
        });



    }



    private void InitializeControllers() {


        ChatToolBar = (Toolbar) findViewById(R.id.chat_toolbar);
        setSupportActionBar(ChatToolBar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        //Use with getSystemService(java.lang.String) to retrieve a LayoutInflater for inflating layout resources in this context.
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar,null);
        // Custom navigation views appear between the application icon and any action buttons and may use any space available there.
        actionBar.setCustomView(actionBarView);

        //this part should be after inflating the layout
        userImage = (CircleImageView) findViewById(R.id.custom_profile_image);
        userName = (TextView) findViewById(R.id.custom_profile_name);
        userLastSeen = (TextView) findViewById(R.id.custom__user_last_seen);

        SendMessageButton = (ImageButton) findViewById(R.id.send_message_btn);
        MessageInputText = (EditText) findViewById(R.id.input_message);



    }

    private void SendMessage() {
        String messageText = MessageInputText.getText().toString();
        if(TextUtils.isEmpty(messageText)){
            Toast.makeText(this, "First Write Your Message...", Toast.LENGTH_SHORT).show();

        }else{
            //creating the reference to the database
            //this one is for the current user
            String messageSenderRef = "Messages/" + MessageSenderID + "/" + messageReceiverID;
            //this one is for the friend user
            String messageReceiverRef = "Messages/" + messageReceiverID + "/" + MessageSenderID;

            //since there are many message so we have to make unique key so that no message matches with the previous one
            //Create a reference to an auto-generated child location.
            // The child key is generated client-side and incorporates an estimate of the server's time for sorting purposes.
            DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                    .child(MessageSenderID)
                    .child(messageReceiverID)
                    .push(); // it will create a random key inside all these fields

            //now get the key
            String messagePushID = userMessageKeyRef.getKey();//this is the unique key for each message

            //this is the body of the message that is saved in the database
            Map messageTextBody = new HashMap();
            messageTextBody.put("message",messageText);
            messageTextBody.put("type","text");
            messageTextBody.put("from",MessageSenderID); // sender id so that we can display the picture on the message

            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef + "/" + messagePushID,messageTextBody);
            messageBodyDetails.put(messageReceiverRef + "/" + messagePushID,messageTextBody);

            //here messagebodydetails contains the key as message sender ref + messagePushid and messageSenderRef and message reciever ref
            //points to the the "messages" + messageSenderId/messageReceiverId which ultimately points to the desired node in the database

            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        Toast.makeText(ChatActivity.this, "Message Send", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                    MessageInputText.setText("");
                }
            });


        }
    }
}