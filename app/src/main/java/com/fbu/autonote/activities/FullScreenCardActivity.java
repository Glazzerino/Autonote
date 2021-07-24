package com.fbu.autonote.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;

import com.fbu.autonote.R;
import com.fbu.autonote.fragments.FullCardNoteFragment;
import com.fbu.autonote.fragments.TextNoteFragment;
import com.fbu.autonote.models.Note;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.firebase.auth.FirebaseAuth;

public class FullScreenCardActivity extends AppCompatActivity {
    ImageButton btnDeleteNote;
    ImageButton btnFavNote;
    FragmentManager fragmentManager;
    Fragment fragment;
    MaterialButtonToggleGroup toggleGroup;

    public final static String TAG = "FullScreenCardActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_card);
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        btnDeleteNote = findViewById(R.id.btnDeleteNote);
        btnFavNote = findViewById(R.id.btnFav);
        toggleGroup = findViewById(R.id.toggleGroupFullCard);
        fragmentManager = getSupportFragmentManager();

        Note note = getIntent().getParcelableExtra("note");
        if (note == null) {
            Log.e(TAG, "Error: Note not found within intent");
        }

        fragment = FullCardNoteFragment.newInstance(note.getImageURL());
        startFragment();

        toggleGroup.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                switch(checkedId) {
                    case R.id.btnTogglePicture:
                        fragment = FullCardNoteFragment.newInstance(note.getImageURL());
                        break;
                    case R.id.btnToggleText:
                        fragment = TextNoteFragment.newInstance(note.getTextContent());
                        break;
                }
                startFragment();
            }
        });
    }

    private void startFragment() {
        if (fragment != null) {
            fragmentManager.beginTransaction()
                    .replace(R.id.flCardFragmentContainer, fragment)
                    .commit();
        }
    }
}