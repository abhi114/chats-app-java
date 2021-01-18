package com.example.chatapp;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    //here we will work to receive all the messages

    //create the list of type messages class
    //which is basically the list of messages
    private List<Messages> userMessagesList;
    //database reference to retireve and show the messages
    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef;

    //constructor to retireve the list of messages
    public MessageAdapter (List<Messages> userMessagesList){

        this.userMessagesList = userMessagesList;
    }

    ////now access all fields of custom messages layout
    //
    //    //view holder to hold the items of custom message layout

    public class MessageViewHolder extends RecyclerView.ViewHolder{

        public TextView senderMessageText,receiverMessageText;
        public CircleImageView receiverProfileImage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMessageText = (TextView) itemView.findViewById(R.id.sender_message_text);
            receiverMessageText = (TextView) itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage = (CircleImageView) itemView.findViewById(R.id.message_profile_image);

        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_messages_layout,parent,false);
        //at the time of creating the view we have to create the database reference
        mAuth = FirebaseAuth.getInstance();


        return new MessageViewHolder(view);
    }


    //Called by RecyclerView to display the data at the specified position.
    // This method should update the contents of the RecyclerView.ViewHolder.itemView
    // to reflect the item at the given position.
    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position) {
        //now we will retrieve and display the messages
        //currentUser
        String messageSenderId = mAuth.getCurrentUser().getUid();
        //object of the message class
        //get the message 1 by 1 and displays it
        Messages messages = userMessagesList.get(position);
        //get from is the method defined in messages class which gives the uid of the sender of the message
        String fromUserID  = messages.getFrom();
        //get the type of message eg - text of image
        String fromMessageType = messages.getType();

        //retrieve the profile picture of the friend from the users node in database
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);
        //everytime the value of the users change it will be called and the respected value will be displayed
        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild("image")){
                    String receiverImage = snapshot.child("image").getValue().toString();
                    Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).into(holder.receiverProfileImage);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if(fromMessageType.equals("text"))
        {   //we are first making the friends message area invisible
            holder.receiverMessageText.setVisibility(View.INVISIBLE);
            holder.receiverProfileImage.setVisibility(View.INVISIBLE);
            holder.senderMessageText.setVisibility(View.INVISIBLE);

            //now if the sender of the message is the current user
            //we will let the receiver area be invisible
            //and we will set the background resource to sender message layout
            if(fromUserID.equals(messageSenderId)){
                holder.senderMessageText.setVisibility(View.VISIBLE);
                holder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout);
                holder.senderMessageText.setTextColor(Color.BLACK);
                holder.senderMessageText.setText(messages.getMessage());


            }else{
                //if its the friends message
                //make the receiver are visible
                //make the current user side invisible text only as it does not contains image for current user side

                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.receiverMessageText.setVisibility(View.VISIBLE);

                //now display the messages
                holder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);
                holder.receiverMessageText.setTextColor(Color.BLACK);
                holder.receiverMessageText.setText(messages.getMessage());






            }
        }

    }

    @Override
    public int getItemCount() {
        //how much messages are there
        return userMessagesList.size();
    }






}
