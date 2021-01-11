package com.example.chatapp;

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


public class ContactsFragment extends Fragment {

    //view for the fragment
    private View ContactsView;
    private RecyclerView myContactsList;
    private DatabaseReference ContactsRef;
    //to get the current user id
    private FirebaseAuth mAuth;
    private String currentUserID;
    //to show the friends profile status
    private DatabaseReference usersRef;




    public ContactsFragment() {
        // Required empty public constructor
    }





    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ContactsView = inflater.inflate(R.layout.fragment_contacts, container, false);

        //setting the layout manager for the view
        myContactsList = (RecyclerView) ContactsView.findViewById(R.id.contacts_list);
        // a reference to the contacts to get the current user
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        //A LayoutManager is responsible for measuring and positioning item views within a RecyclerView
        // as well as determining the policy for when to recycle item views that are no longer visible to the user.

        myContactsList.setLayoutManager(new LinearLayoutManager(getContext()));
        //Return the View for the fragment's UI
        return ContactsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        //since we are displaying the contacts fragment and we have already defined the contacts model class
        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ContactsRef,Contacts.class) //reference to the contacts field in database
                .build();

        FirebaseRecyclerAdapter<Contacts,ContactsViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactsViewHolder contactsViewHolder, int i, @NonNull Contacts contacts) {
                //the id of the users that the current user are friends with
                //the query will query each of the sub-id and will give the key of each from the position
                String usersId = getRef(i).getKey();
                //from the users is the database
                //event listener if the name changes
                usersRef.child(usersId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.hasChild("image")){
                            String ContactsProfileImage = dataSnapshot.child("image").getValue().toString();
                            String ContactsName = dataSnapshot.child("name").getValue().toString();
                            String ContactsStatus = dataSnapshot.child("status").getValue().toString();

                            contactsViewHolder.userName.setText(ContactsName);
                            contactsViewHolder.userStatus.setText(ContactsStatus);

                            Picasso.get().load(ContactsProfileImage).placeholder(R.drawable.profile_image).into(contactsViewHolder.profileImage);

                        }else{
                            String ContactsName = dataSnapshot.child("name").getValue().toString();
                            String ContactsStatus = dataSnapshot.child("status").getValue().toString();

                            contactsViewHolder.userName.setText(ContactsName);
                            contactsViewHolder.userStatus.setText(ContactsStatus);

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                //it will create the view to be displayed
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);

                ContactsViewHolder viewHolder = new ContactsViewHolder(view);
                return viewHolder;


            }
        };

        myContactsList.setAdapter(adapter);
        adapter.startListening();
    }


    public static class ContactsViewHolder extends RecyclerView.ViewHolder {
        //use the user display layout
        TextView userName , userStatus;
        CircleImageView profileImage;
        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);

        }
    }
}