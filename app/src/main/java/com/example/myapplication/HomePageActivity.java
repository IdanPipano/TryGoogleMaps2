package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

public class HomePageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        // #TODO: if user signed in then
//        FragmentContainerView fragmentContainer = findViewById(R.id.loggedInFragmentContainerView);
//        LoggedInFragment loggedInFragment = new LoggedInFragment();
//        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//        transaction.add(R.id.loggedInFragmentContainerView, loggedInFragment);
//        transaction.commit();

        // else:
        FragmentContainerView fragmentContainer = findViewById(R.id.loggedInFragmentContainerView);
        NotLoggedInFragment notLoggedInFragment = new NotLoggedInFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.loggedInFragmentContainerView, notLoggedInFragment);
        transaction.commit();

//        LoggedInFragment loggedInFragment = new LoggedInFragment();
//        FragmentTransaction transaction2 = getSupportFragmentManager().beginTransaction();
//        transaction2.replace(R.id.loggedInFragmentContainerView, loggedInFragment);
//        transaction2.commit();


    }
}