package com.fbu.autonote.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.TextView;

import com.fbu.autonote.R;

public class NotesExploreActivity extends AppCompatActivity {
    RecyclerView rvNotes;
    TextView tvTopicTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes_explore);
    }
}