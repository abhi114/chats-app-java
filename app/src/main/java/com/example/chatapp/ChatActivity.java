package com.example.chatapp;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    //get the id and name,image of the friend user
    private String messageReceiverID, MessageReceiverName,MessageReceiverImage;

    //members of the private chat bar xml
    private TextView userName , userLastSeen;
    private CircleImageView userImage;

    private Toolbar ChatToolBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        messageReceiverID = getIntent().getExtras().get("visit_user_id").toString();
        MessageReceiverName = getIntent().getExtras().get("visit_user_name").toString();
        MessageReceiverImage = getIntent().getExtras().get("visit_image").toString();


        //Toast.makeText(this, messageReceiverID, Toast.LENGTH_SHORT).show();
        //Toast.makeText(this, MessageReceiverName, Toast.LENGTH_SHORT).show();

        InitializeControllers();

        userName.setText(MessageReceiverName);
        Picasso.get().load(MessageReceiverImage).placeholder(R.drawable.profile_image).into(userImage);



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



    }
}