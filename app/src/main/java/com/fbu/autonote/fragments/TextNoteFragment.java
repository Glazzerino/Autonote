package com.fbu.autonote.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fbu.autonote.R;

import org.jetbrains.annotations.NotNull;

public class TextNoteFragment extends Fragment {
    TextView tvNoteText;
    public static final String TAG = "TextNoteFragment";
    public TextNoteFragment() {
    }

    public static TextNoteFragment newInstance(String textContent) {
        TextNoteFragment fragment = new TextNoteFragment();
        Bundle args = new Bundle();
        args.putString("textContent", textContent);
        fragment.setArguments(args);
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
        return inflater.inflate(R.layout.fragment_text_note, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvNoteText = view.findViewById(R.id.tvNoteText);
        String textContent = getArguments().getString("textContent");
        textContent = textContent.replace("\\n", " ");
        Log.d(TAG, textContent);
        tvNoteText.setText(textContent);
    }
}