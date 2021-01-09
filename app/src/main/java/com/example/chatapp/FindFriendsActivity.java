package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity {

    //all the fields of xml
    private Toolbar mToolbar;
    private RecyclerView FindFriendsRecyclerList;
    //query for the firebaseoptions
    private DatabaseReference UsersRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        FindFriendsRecyclerList = (RecyclerView) findViewById(R.id.find_friends_recycler_list);
        FindFriendsRecyclerList.setLayoutManager(new LinearLayoutManager(this));


        mToolbar = (Toolbar) findViewById(R.id.find_friends_toolbar);
        setSupportActionBar(mToolbar);
        //true to show the user that selecting home will return one level up rather than to the top level of the app.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //Set whether to include the application home affordance in the action bar.
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Friends");
        //now specifiy to which activity we have to send when the user clicks on the back button in the android Manifest



    }

    @Override
    protected void onStart() {
        super.onStart();
        //configure the adapter using firebase recycler options
        //query is the reference to the firebase database
        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(UsersRef,Contacts.class)
                .build();
        //The FirebaseRecyclerAdapter binds a Query to a RecyclerView. When data is added, removed, or changed these updates are automatically applied to your UI in real time.
        // You should already have a ViewHolder subclass for displaying each item.
        FirebaseRecyclerAdapter<Contacts,FindFriendViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder>(options) {
            //Adapters provide a binding from an app-specific data set to views that are displayed within a RecyclerView
            @Override
            //it is called whenever the items are recycled.
            protected void onBindViewHolder(@NonNull FindFriendViewHolder holder, int position, @NonNull Contacts model) {
                //setting the values gained after querying the database to the fields
                holder.userName.setText(model.getName());
                holder.userStatus.setText(model.getStatus());
                //display image using picasso
                //if user didnt set the placeholder will take its place
                Picasso.get().load(model.getImage()).placeholder(R.drawable.profile_image).into(holder.profileImage);

            }

            @NonNull
            @Override
            //Called when RecyclerView needs a new RecyclerView.ViewHolder of the given type to represent an item.
            //ViewGroup: The ViewGroup into which the new View will be added after it is bound to an adapter position.
            public FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                //we are connecting the user display layout the find friend view holder class so that we can use this for each item and the highly processed findViewbyid can be avoided
                //inflate it from an XML layout file.
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);
                FindFriendViewHolder viewHolder = new FindFriendViewHolder(view);
                //A new ViewHolder that holds a View of the given view type.
                return viewHolder;

                //A new ViewHolder that holds a View of the given view type.

            }
        };

        FindFriendsRecyclerList.setAdapter(adapter);

        adapter.startListening();
    }

    //it is the viewHolder class
    //view holder is used because if several rows have the same ViewType then the same View can be reused for several rows.
    //A ViewHolder describes an item view and metadata about its place within the RecyclerView
    public static class FindFriendViewHolder extends RecyclerView.ViewHolder {
        //access the fields of the user display layout file

        TextView userName,userStatus;
        CircleImageView profileImage;



        public FindFriendViewHolder(@NonNull View itemView) {
            //initialize those varialbe using the itemview parameter
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);

        }
    }
}