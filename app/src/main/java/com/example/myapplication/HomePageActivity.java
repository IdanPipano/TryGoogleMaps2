package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomePageActivity extends AppCompatActivity {

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            String userMail = currentUser.getEmail();
            Log.d("wtf", "currentUser is not null in onStart of RegisterActivity! His email: " + userMail);
            FragmentContainerView fragmentContainer = findViewById(R.id.loggedInFragmentContainerView);
            LoggedInFragment loggedInFragment = new LoggedInFragment(userMail);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.loggedInFragmentContainerView, loggedInFragment);
            transaction.commit();
        }
        else {  //No user is currentlylogged in:
            NotLoggedInFragment notLoggedInFragment = new NotLoggedInFragment();
            FragmentTransaction transaction2 = getSupportFragmentManager().beginTransaction();
            transaction2.replace(R.id.loggedInFragmentContainerView, notLoggedInFragment);
            transaction2.commit();
        }

//        Intent intent = getIntent();
//        String userMail = intent.getStringExtra("userMail");
//        if (userMail != null) { //if there is already a logged in user
//            Log.d("wtf", "userMail = " + userMail + "inHomePageActivity onCreate()");
//            FragmentContainerView fragmentContainer = findViewById(R.id.loggedInFragmentContainerView);
//            LoggedInFragment loggedInFragment = new LoggedInFragment(userMail);
//            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//            transaction.replace(R.id.loggedInFragmentContainerView, loggedInFragment);
//            transaction.commit();
//        }
//        else {
//            NotLoggedInFragment notLoggedInFragment = new NotLoggedInFragment();
//            FragmentTransaction transaction2 = getSupportFragmentManager().beginTransaction();
//            transaction2.replace(R.id.loggedInFragmentContainerView, notLoggedInFragment);
//            transaction2.commit();
//        }


    }
}