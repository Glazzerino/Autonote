package com.fbu.autonote;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

//This fragment acts as the greeting page of the app.
public class NotesFragment extends Fragment {
    private Context context;

    public NotesFragment() {
        // Required empty public constructor
    }

    public static NotesFragment newInstance(Context context) {
        NotesFragment fragment = new NotesFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        fragment.context = context;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notes, container, false);
    }
}