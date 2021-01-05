package com.example.chatapp;

import android.icu.text.Edits;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class groupsFragment extends Fragment {

    private View groupFragmentView;
    //list view and adapter for displaying the list items
    // Returns a view for each object in a collection of data objects you provide, and can be used with list-based user interface widgets such as ListView
    private ListView list_view;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> list_of_groups = new ArrayList<>();
    //create a reference to the root node of database
    private DatabaseReference GroupRef;


    public groupsFragment() {
        // Required empty public constructor
    }





    //creates and returns the view hierarchy associated with the fragment.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        groupFragmentView = inflater.inflate(R.layout.fragment_groups, container, false);

        GroupRef = FirebaseDatabase.getInstance().getReference().child("Groups"); // making the reference to the group field so that we can take all its sub-values


        InitializeFields();

        RetrieveAndDisplayGroups();

        return groupFragmentView;
    }



    private void InitializeFields() {
        list_view = (ListView) groupFragmentView.findViewById(R.id.list_view);
        //in fragments we get the context using getContext
        arrayAdapter = new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,list_of_groups);
        list_view.setAdapter(arrayAdapter);
    }

    private void RetrieveAndDisplayGroups() {

        //Add a listener for changes in the data at this location. Each time time the data changes, your listener will be called with an immutable snapshot of the data.

        GroupRef.addValueEventListener(new ValueEventListener() {
            @Override
            //This method will be called with a snapshot of the data at this location.
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //using an iterator that will go through the list and display line by line
                Set<String> set = new HashSet<>();
                Iterator iterator = dataSnapshot.getChildren().iterator();//every child can be read using iterator
                //every time a new group gets added the iterator add the entire data and hence duplicates will be there so we had used sets
                while (iterator.hasNext()){
                    //it will prevent the duplication of data
                    //The key name for the source location of this snapshot or null if this snapshot points to the database root.
                    //here key means the name of the group
                    set.add(((DataSnapshot)iterator.next()).getKey());

                }
                //clear it to remove duplicates
                list_of_groups.clear();
                list_of_groups.addAll(set);
                //notifies the attached observers that the underlying data has been changed and any View reflecting the data set should refresh itself.
                arrayAdapter.notifyDataSetChanged();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}