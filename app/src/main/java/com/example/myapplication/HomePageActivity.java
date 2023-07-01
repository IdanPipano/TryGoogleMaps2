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
        FragmentContainerView fragmentContainer = findViewById(R.id.loggedInFragmentContainerView);
        LoggedInFragment loggedInFragment = new LoggedInFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.loggedInFragmentContainerView, loggedInFragment);
        transaction.commit();


    }
}