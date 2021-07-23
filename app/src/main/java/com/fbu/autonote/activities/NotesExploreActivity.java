package com.fbu.autonote.activities;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fbu.autonote.R;
import com.fbu.autonote.adapters.NotesExploreAdapter;
import com.fbu.autonote.models.Note;
import com.fbu.autonote.utilities.Favorites;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
    Favorites favoritesManager;
    MaterialButtonToggleGroup toggleGroup;
    //Stores numeric indexes that point to notes that are marked as favorite, to avoid data duplication
    Set<Integer> notFavoritePointers;

    public static final String TAG = "NotesExploreActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        favoritesManager = new Favorites(context);
        notFavoritePointers = new HashSet<>();
        setContentView(R.layout.activity_notes_explore);
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        database = FirebaseDatabase.getInstance().getReference(userId);
        tvTopicTitle = findViewById(R.id.tvTopicTitle);
        rvNotes = findViewById(R.id.rvNoteCards);
        topic = getIntent().getExtras().getString("topic");
        tvTopicTitle.setText(topic);
        toggleGroup = findViewById(R.id.toggleGroupNotes);
        //Set up recyclerview
        notesExploreAdapter = new NotesExploreAdapter(this,
                favoritesManager,
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
                            showAll();
                        }
                        break;
                    case R.id.btnShowFavs:
                        if (isChecked) {
                            showFavoritesOnly();
                        }
                        break;
                }
            }
        });
    }

    private void showAll() {
        notesExploreAdapter.setShowFavoritesOnly(false);
        notesExploreAdapter.notifyDataSetChanged();
    }

    private void showFavoritesOnly() {
        notesExploreAdapter.setShowFavoritesOnly(true);
        notesExploreAdapter.notifyDataSetChanged();
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