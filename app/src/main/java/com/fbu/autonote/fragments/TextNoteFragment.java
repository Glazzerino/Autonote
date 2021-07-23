package com.fbu.autonote.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fbu.autonote.R;

public class TextNoteFragment extends Fragment {

    private String mParam1;
    private String mParam2;

    public TextNoteFragment() { }

    public static TextNoteFragment newInstance(String param1, String param2) {
        TextNoteFragment fragment = new TextNoteFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_text_note, container, false);
    }
}