package com.fbu.autonote.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.fbu.autonote.R;
import com.fbu.autonote.models.Note;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FullScreenCardActivity extends AppCompatActivity {
    ImageView ivNoteImage;
    DatabaseReference firebaseDatabase;
    TextView tvKeywords;
    ImageButton btnDeleteNote;
    ImageButton btnFavNote;
    public final static String TAG = "FullScreenCardActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_card);
        ivNoteImage = findViewById(R.id.ivFullCardImage);
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        tvKeywords = findViewById(R.id.tvKeywords);
        btnDeleteNote = findViewById(R.id.btnDeleteNote);
        btnFavNote = findViewById(R.id.btnFav);

        Note note = getIntent().getParcelableExtra("note");
        if (note == null) {
            Log.e(TAG, "Error: Note not found within intent");
        }

        Glide.with(this)
                .load(note.getImageURL())
                .into(ivNoteImage);

    }
}