package com.fbu.autonote.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.transition.Explode;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fbu.autonote.R;
import com.fbu.autonote.adapters.NotesExploreAdapter;
import com.fbu.autonote.models.Note;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

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
    MaterialButtonToggleGroup toggleGroup;
    Set<Integer> notFavoritePointers;
    public static final String TAG = "NotesExploreActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAnimations();
        context = this;
        notFavoritePointers = new HashSet<>();
        setContentView(R.layout.activity_notes_explore);
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        database = FirebaseDatabase.getInstance().getReference(userId);
        tvTopicTitle = findViewById(R.id.tvTopicTitle);
        rvNotes = findViewById(R.id.rvNoteCards);
        topic = getIntent().getExtras().getString("topic");
        tvTopicTitle.setText(topic);
        toggleGroup = findViewById(R.id.toggleGroupNotes);

        notesExploreAdapter = new NotesExploreAdapter(this,
                topic);
        linearLayoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL,
                false);
        rvNotes.setAdapter(notesExploreAdapter);
        rvNotes.setLayoutManager(linearLayoutManager);
        populateAdapterContainer();

        toggleGroup.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                switch (checkedId) {
                    case R.id.btnShowAll:
                        if (isChecked) {
                            notesExploreAdapter.setShowFavoritesOnly(false);
                            notesExploreAdapter.notifyDataSetChanged();
                        }
                        break;
                    case R.id.btnShowFavs:
                        if (isChecked) {
                            notesExploreAdapter.setShowFavoritesOnly(true);
                            notesExploreAdapter.notifyDataSetChanged();
                        }
                        break;
                }
            }
        });
    }

    private void setAnimations() {
        Explode explode = new Explode();
        explode.setDuration(500);
        getWindow().setEnterTransition(explode);
    }

    //loads notes from firebase onto the adapter's container
    private void populateAdapterContainer() {
        database = database.child(topic);
        ValueEventListener valueEventListener = new ValueEventListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                notesExploreAdapter.clearContainer();
                //Each topic has a collection inside of it, so we must iterate over each collection
                for (DataSnapshot collection : snapshot.getChildren()) {
                    String date = collection.getKey();
                    for (DataSnapshot noteSnapshot : collection.getChildren()) {
                        //Collections hold notes, so we nest another loop inside
                        Note note = Note.fromDataSnapshot(noteSnapshot);
                        note.setUrl(noteSnapshot.getRef().getPath().toString());
                        notesExploreAdapter.addToNoteContainer(note);
                    }
                }
                notesExploreAdapter.notifyDataSetChanged();
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