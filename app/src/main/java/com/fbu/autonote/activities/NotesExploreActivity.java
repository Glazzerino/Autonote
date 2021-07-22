package com.fbu.autonote.activities;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fbu.autonote.R;
import com.fbu.autonote.adapters.NotesExploreAdapter;
import com.fbu.autonote.models.Note;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import es.dmoral.toasty.Toasty;

public class NotesExploreActivity extends AppCompatActivity {
    RecyclerView rvNotes;
    TextView tvTopicTitle;
    DatabaseReference database;
    String userId;
    NotesExploreAdapter notesExploreAdapter;
    LinearLayoutManager linearLayoutManager;
    String topic;
    Context context;
    public static final String TAG = "NotesExploreActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_notes_explore);
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        database = FirebaseDatabase.getInstance().getReference(userId);
        tvTopicTitle = findViewById(R.id.tvTopicTitle);
        rvNotes = findViewById(R.id.rvNoteCards);
        topic = getIntent().getExtras().getString("topic");
        tvTopicTitle.setText(topic);
        //Set up recyclerview
        notesExploreAdapter = new NotesExploreAdapter(this);
        linearLayoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL,
                false);
        rvNotes.setAdapter(notesExploreAdapter);
        rvNotes.setLayoutManager(linearLayoutManager);
        populateAdapterContainer();
    }

    //loads notes from firebase onto the adapter's container
    private void populateAdapterContainer() {
        //Switch to the current topic directory and get notes
        database = database.child(topic);
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                //Each topic has a collection inside of it, so we must iterate over each collection
                int position = 0; //keep track of the number of additions to avoid excessive function calling
                for (DataSnapshot collection : snapshot.getChildren()) {
                    String date = collection.getKey();
                    for (DataSnapshot noteSnapshot : collection.getChildren()) {
                        //Collections hold notes, so we nest another loop inside
                        Note note = Note.fromDataSnapshot(noteSnapshot, date);
                        Log.d(TAG, "Path to note: " + noteSnapshot.getRef().toString());
                        notesExploreAdapter.addToNoteContainer(note);
                        notesExploreAdapter.notifyItemInserted(position++);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Log.d(TAG, error.toString());
                Toasty.error(context, "Error fetching notes!", Toasty.LENGTH_SHORT).show();
            }
        };
        database.addValueEventListener(valueEventListener);
    }
}