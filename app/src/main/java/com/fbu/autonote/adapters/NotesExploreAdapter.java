package com.fbu.autonote.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.bumptech.glide.Glide;
import com.fbu.autonote.R;
import com.fbu.autonote.models.Note;
import com.fbu.autonote.utilities.Favorites;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class NotesExploreAdapter extends RecyclerView.Adapter<NotesExploreAdapter.ViewHolder> {
    List<Note> notes;
    Context context;
    FirebaseStorage firebaseStorage;
    String userId;
    Favorites favoritesManager;

    //40MB
    public static final long BYTE_DOWNLOAD_LIMIT = 40000000;
    public NotesExploreAdapter(Context context) {
        this.context = context;
        notes = new ArrayList<>();
        firebaseStorage = FirebaseStorage.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        favoritesManager = new Favorites(context);
    }

    @NonNull
    @NotNull
    @Override
    public NotesExploreAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_note, parent, false);
        return new NotesExploreAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull NotesExploreAdapter.ViewHolder holder, int position) {
        Note note = notes.get(position);
        holder.bind(note);
    }

    public void addToNoteContainer(Note note) {
        notes.add(note);
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate;
        TextView tvKeywords;
        ImageView ivPreview;
        ImageButton btnFav;
        FirebaseStorage firebaseStorage;
        CircularProgressDrawable progressDrawable;
        Favorites favorites;
        boolean isFaved;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvKeywords = itemView.findViewById(R.id.tvKeywordsCard);
            btnFav = itemView.findViewById(R.id.btnFav);
            ivPreview = itemView.findViewById(R.id.ivNotePreview);
            firebaseStorage = FirebaseStorage.getInstance();
            progressDrawable = new CircularProgressDrawable(itemView.getContext());
            progressDrawable.setCenterRadius(40f);
            progressDrawable.setStrokeWidth(5f);
            progressDrawable.start();
            //Get reference to outer class's Favorites Manager object
            favorites = NotesExploreAdapter.this.favoritesManager;
        }

        //Image loading is done inside the bind method to avoid loading images not being shown
        //in the recyclerview
        protected void bind(Note note) {
            String keywords = new String();
            //Get 5 keywords at most
            if (favorites.checkIfFavorite(note.getImageURL())) {
                setBtnFav(true);
            }

            int counter = 0;
            for (String keyword : note.getKeywords()) {
                keywords += keyword;
                if (counter++ > 5) {
                    keywords += ".";
                    break;
                } else {
                    keywords += ", ";
                }
            }
            tvDate.setText(note.getDate());
            tvKeywords.setText(keywords);
            Glide.with(itemView)
                    .load(note.getImageURL())
                    .placeholder(progressDrawable)
                    .into(ivPreview);

            btnFav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean isFavorite = favorites.checkIfFavorite(note.getImageURL());
                    //if it's not in favorites list add it
                    if (!isFavorite) {
                        favorites.addFav(note.getImageURL());
                        setBtnFav(true);
                    } else {
                        favorites.remove(note.getImageURL());
                        setBtnFav(false);
                    }
                }
            });
        }

        //if true then replace drawable with filled star
        private void setBtnFav(boolean set) {
            int drawableId = set ? R.drawable.ic_baseline_star_24 : R.drawable.ic_baseline_star_outline_24;
            btnFav.setImageResource(drawableId);
        }
    }
}
