package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LoggedInFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoggedInFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private Button btnToMap;

    public LoggedInFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LoggedInFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LoggedInFragment newInstance(String param1, String param2) {
        LoggedInFragment fragment = new LoggedInFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
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
                    Log.d("wtf", "entered onClic of btnToMap");
                    Intent intent = new Intent(getContext(), MapsActivity.class);
                    startActivity(intent);
                }
            });
        }

        // Inflate the layout for this fragment
        return view;
    }
}