package com.example.chatapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsFragment extends Fragment {
    //view
    private View PrivateChatsView;
    //recycler view to display the chats list using the firebase recycler adapter
    RecyclerView chatsList;
    //query to the database
    private DatabaseReference chatsRef;
    //for current user Id
    private FirebaseAuth mAuth;
    private String currentUserID;
    //users node reference
    private DatabaseReference usersRef;







    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //for current user
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        // Inflate the layout for this fragment
        PrivateChatsView =  inflater.inflate(R.layout.fragment_chats, container, false);
        //chats recycler view
        chatsList = (RecyclerView) PrivateChatsView.findViewById(R.id.chats_list);
        chatsList.setLayoutManager(new LinearLayoutManager(getContext()));
        //retrieve the chats from the contacts node using the firebase recycler adapter
        //refering to the current logged user
        chatsRef  = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        return PrivateChatsView;
    }

    @Override
    public void onStart() {
        super.onStart();
        //when started display the chats
        //query for the adapter to display the chats
        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(chatsRef,Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,chatsViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, chatsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final chatsViewHolder holder, int position, @NonNull Contacts model) {
                //first we will get each id present in the contacts of the current user and then from the users node
                //we will display it
                //it will get each child under current user under  the contacts line by line
                final String usersIds = getRef(position).getKey(); //key means unique id

                //for every data change in these user will be reflected in the fragment
                usersRef.child(usersIds).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //now display the info
                        if(snapshot.exists()){
                            if(snapshot.hasChild("image"))
                            {
                                final String retImage = snapshot.child("image").getValue().toString();
                                final String retName = snapshot.child("name").getValue().toString();
                                final String retStatus = snapshot.child("status").getValue().toString();

                                //now display using the holder
                                Picasso.get().load(retImage).placeholder(R.drawable.profile_image).into(holder.profileImage);
                                holder.userName.setText(retName);
                                holder.userStatus.setText("Last Seen : " + "\n" + "Date " + "Time");


                            }else{
                                final String retName = snapshot.child("name").getValue().toString();
                                final String retStatus = snapshot.child("status").getValue().toString();
                                holder.userName.setText(retName);
                                holder.userStatus.setText("Last Seen : " + "\n" + "Date " + "Time");

                            }
                            final String sendName = snapshot.child("name").getValue().toString();
                            //send to private chat
                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v)
                                {
                                    Intent chatIntent = new Intent(getContext(),ChatActivity.class);
                                    //send the name and the id of the user to the chat fragment
                                    //it will send the userid and the name of the clicked user
                                    chatIntent.putExtra("visit_user_id",usersIds);
                                    chatIntent.putExtra("visit_user_name",sendName);
                                    startActivity(chatIntent);

                                }
                            });
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


            }

            @NonNull
            @Override
            public chatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);
                return new chatsViewHolder(view);
            }
        };

        chatsList.setAdapter(adapter);
        adapter.startListening();

    }


    //class for the view holder of the chats
    public static class chatsViewHolder extends RecyclerView.ViewHolder {
        //include the layout of the user display layout
        CircleImageView profileImage;
        TextView userStatus , userName;


        public chatsViewHolder(@NonNull View itemView) {
            super(itemView);
            //initialize the fields by using the itemView object which will contain the view

            profileImage = itemView.findViewById(R.id.users_profile_image);
            userStatus = itemView.findViewById(R.id.user_status);
            userName = itemView.findViewById(R.id.user_profile_name);

        }
    }
}


