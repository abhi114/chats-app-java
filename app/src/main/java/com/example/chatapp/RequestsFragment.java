package com.example.chatapp;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class RequestsFragment extends Fragment {

    private View RequestsFragmentView;
    private RecyclerView myRequestsList;
    //query to chat requests field
    private DatabaseReference ChatRequestsRef;
    private FirebaseAuth mAuth;
    private String currentUserID;

    //to display the request users
    private  DatabaseReference UsersRef;
    //to add the profile to contact database after it is accepted
    private DatabaseReference ContactsRef;




    public RequestsFragment() {
        // Required empty public constructor
    }






    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        RequestsFragmentView =  inflater.inflate(R.layout.fragment_requests, container, false);
        //recycler view for displaying the requests
        //creating the reference
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        ChatRequestsRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");

        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        myRequestsList = (RecyclerView) RequestsFragmentView.findViewById(R.id.chat_requests_list);
        myRequestsList.setLayoutManager(new LinearLayoutManager(getContext()));


        return RequestsFragmentView;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        //using firebase recycler adapter
        //query to databse
        //contacts is the model through which we will get the info
        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ChatRequestsRef.child(currentUserID),Contacts.class) // querying the current user to find all those who had send him request
                .build();


        FirebaseRecyclerAdapter<Contacts,RequestViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, RequestViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final RequestViewHolder requestViewHolder, int i, @NonNull Contacts contacts) {
                //bind the view
                requestViewHolder.itemView.findViewById(R.id.request_accept_btn).setVisibility(View.VISIBLE);
                requestViewHolder.itemView.findViewById(R.id.request_cancel_btn).setVisibility(View.VISIBLE);

                //id of all the user present in the chat request of the current user and will display all the received type
                //getting the position of the first sub-category
                final String list_user_id = getRef(i).getKey();
                //a reference to the request type
                final DatabaseReference getTypeRef = getRef(i).child("request_type").getRef();
                //listener because the value can change and the the requests will automatically update
                getTypeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            String type = dataSnapshot.getValue().toString();
                            if(type.equals("received")){
                                //now retrieve the value of that user from the users node
                                UsersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        //since image can be optional
                                        if(dataSnapshot.hasChild("image")){

                                            final String requestUserProfileImage = dataSnapshot.child("image").getValue().toString();

                                            //now display the value
                                            Picasso.get().load(requestUserProfileImage).placeholder(R.drawable.profile_image).into(requestViewHolder.profileImage);


                                        }
                                        final String requestUserName = dataSnapshot.child("name").getValue().toString();
                                        final String requestUserStatus = dataSnapshot.child("status").getValue().toString();
                                        requestViewHolder.userName.setText(requestUserName);
                                        requestViewHolder.userStatus.setText("wants to Connect With You");

                                        requestViewHolder.AcceptButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                //options for dialog box
                                                BuilderShowDialog(list_user_id,currentUserID,requestUserName);


                                            }
                                        });
                                        requestViewHolder.CancelButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                BuilderShowDialog(list_user_id,currentUserID,requestUserName);
                                            }
                                        });

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });


                            }
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                //Obtains the LayoutInflater from the given context.
                // 	getContext()
                //
                //Return the context we are running in, for access to resources, class loader
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);
                RequestViewHolder holder = new RequestViewHolder(view);
                return holder;
            }
        };

        myRequestsList.setAdapter(adapter);
        adapter.startListening();


    }

    private void BuilderShowDialog(String list_user_id, String currentUserID,String requestUserName) {
        final String currentUserIDCopy = currentUserID;
        final String list_user_id_copy = list_user_id;
        CharSequence options[] = new CharSequence[]{
                "Accept",
                "Decline"

        };
        //alert dialog to confirm the option
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle( requestUserName +" Chat Request");
        //we had passed the charSequence to the dialog as positive and negative buttons
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == 0){
                    //it means that the user has clicked on the accept as the index of accept is 0
                    //remove the user from request and add to contacts fragment
                    //currentuserid to friend user id and save the value of contacts to saved
                    ContactsRef.child(currentUserIDCopy).child(list_user_id_copy).child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                //now save for the friend also in the database
                                ContactsRef.child(list_user_id_copy).child(currentUserIDCopy).child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        //now remove from the request fragment
                                        if(task.isSuccessful()){
                                            ChatRequestsRef.child(currentUserIDCopy).child(list_user_id_copy).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        ChatRequestsRef.child(list_user_id_copy).child(currentUserIDCopy).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){
                                                                    Toast.makeText(getContext(), "Contacts Saved", Toast.LENGTH_SHORT).show();
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
                    });


                }
                if(which == 1){
                    //clicked on cancel
                    //remove from the request
                    ChatRequestsRef.child(currentUserIDCopy).child(list_user_id_copy).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                ChatRequestsRef.child(list_user_id_copy).child(currentUserIDCopy).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Toast.makeText(getContext(), "Request Canceled", Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                });
                            }

                        }
                    });

                }

            }
        });

        builder.show();
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder{
        TextView userName ,userStatus;
        CircleImageView profileImage;
        Button AcceptButton ,CancelButton;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            AcceptButton = itemView.findViewById(R.id.request_accept_btn);
            CancelButton = itemView.findViewById(R.id.request_cancel_btn);


        }
    }
}