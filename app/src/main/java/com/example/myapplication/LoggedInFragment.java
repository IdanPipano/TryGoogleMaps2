package com.example.myapplication;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LoggedInFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoggedInFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_USER_NAME = "hey?";
    //private static final String ARG_PARAM2 = "param2";


    // TODO: Rename and change types of parameters
    private String userName;
    //private String mParam2;

    private Button btnToMap;
    private Button btnLogout;
    private TextView txtView;

    public LoggedInFragment() {
        // Required empty public constructor
    }

    public LoggedInFragment(String userName) {
        this.userName = userName;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
//     * @param param2 Parameter 2.
     * @return A new instance of fragment LoggedInFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LoggedInFragment newInstance(String param1) {
        LoggedInFragment fragment = new LoggedInFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_NAME, param1);
//        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userName = getArguments().getString(ARG_USER_NAME);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("wtf", "entered onCreateView in LoggedInFragment");
        View view = inflater.inflate(R.layout.fragment_logged_in, container, false);;
        if (view == null){
            Log.d("wtf", "view == null");
        }
        else {
            btnToMap = view.findViewById(R.id.btnToMap);
            btnToMap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), MapsActivity.class);
                    intent.putExtra("userName", userName);
                    startActivity(intent);
                }
            });
        }

        txtView = view.findViewById(R.id.youAreLoggedInTxtView);
        if (this.userName == null) { // querying the db takes a second or so
            txtView.setText("You are logged in");
        }
        else {
            String originalText = "You are logged in as ";
            String finalText = originalText + this.userName;

            SpannableString spannableString = new SpannableString(finalText);
            spannableString.setSpan(new StyleSpan(Typeface.BOLD), originalText.length(), finalText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            txtView.setText(spannableString);

        }

        btnLogout = view.findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();  //will sign out the user from firebase
                Intent intent = new Intent(getContext(), HomePageActivity.class);
                startActivity(intent);
            }
        });


        // Inflate the layout for this fragment
        return view;
    }
}