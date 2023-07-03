package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomePageActivity extends AppCompatActivity {

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();


        if(currentUser != null){

            String uid = currentUser.getUid();
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
            DatabaseReference userRef = dbRef.child("Users").child(uid);

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        String userName = user.getUserName();
                        Log.d("wtf", "userName= " + userName);
                        // Now you have the username, do whatever you want with it

                        FragmentContainerView fragmentContainer = findViewById(R.id.loggedInFragmentContainerView);
                        //LoggedInFragment loggedInFragment = ;
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.loggedInFragmentContainerView, LoggedInFragment.newInstance(userName));
                        try{
                            transaction.commit();
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }





                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle possible errors.
                    Log.w("wtf", "loadUser:onCancelled", databaseError.toException());
                }
            });

        }
        else {  //No user is currentlylogged in:
            NotLoggedInFragment notLoggedInFragment = new NotLoggedInFragment();
            FragmentTransaction transaction2 = getSupportFragmentManager().beginTransaction();
            transaction2.replace(R.id.loggedInFragmentContainerView, notLoggedInFragment);
            transaction2.commit();
        }

    }
}