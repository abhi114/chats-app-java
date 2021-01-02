package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

//helps in switching between tabs
//This version of the pager is best for use when there are a handful of typically more static fragments to be paged through, such as a set of tabs.
public class TabsAccessorAdapter extends FragmentPagerAdapter {

    //constructor fot the FragmentPagerAdapter
    public TabsAccessorAdapter(@NonNull FragmentManager fm) {
        super(fm);

    }


    //switch statement for give the instance of the particular fragment
    @NonNull
    @Override
    public Fragment getItem(int position) {

        switch (position)
        {
            case 0:
                ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;

            case 1:
                groupsFragment GroupsFragment = new groupsFragment();
                return GroupsFragment;

            case 2:
                ContactsFragment contactsFragment = new ContactsFragment();
                return contactsFragment;

            default:
                return null;
        }


    }

    @Override
    public int getCount() {
        return 3;
    }

    //method to set title for these pages


    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        //switch to set title
        switch (position)
        {
            case 0:
                    return "Chats";

            case 1:
                    return "Groups";


            case 2:
                    return "Contacts";


            default:
                return null;
        }

    }
}
