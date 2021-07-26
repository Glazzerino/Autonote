package com.fbu.autonote.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
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
import com.fbu.autonote.activities.FullScreenCardActivity;
import com.fbu.autonote.models.Note;
import com.fbu.autonote.utilities.Favorites;
import com.fbu.autonote.utilities.GetListOfKeywordsString;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @about Manages the note cards inside the Note Exploration Activity
 */
public class NotesExploreAdapter extends RecyclerView.Adapter<NotesExploreAdapter.ViewHolder> {
    List<Note> notes;
    Context context;
    FirebaseStorage firebaseStorage;
    String userId;
    Favorites favoritesManager;
    String topic;
    boolean showFavoritesOnly;

    //40MB
    public static final long BYTE_DOWNLOAD_LIMIT = 40000000;
    public NotesExploreAdapter(Context context, Favorites favoritesManager, String topic) {
        this.context = context;
        notes = new ArrayList<>();
        firebaseStorage = FirebaseStorage.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        this.favoritesManager = favoritesManager;
        this.topic = topic;
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
        if (!favoritesManager.checkIfFavorite(note.getUrl(), topic) && showFavoritesOnly) {
            holder.disableVisibility();
        } else {
            holder.enableVisibility();
        }
        holder.bind(note);
    }

    public void clearContainer() {
        notes.clear();
    }
    public void addToNoteContainer(Note note) {
        notes.add(note);
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public void setShowFavoritesOnly(boolean set) {
        showFavoritesOnly = set;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate;
        TextView tvKeywords;
        ImageView ivPreview;
        ImageButton btnFav;
        FirebaseStorage firebaseStorage;
        CircularProgressDrawable progressDrawable;
        Favorites favorites;
        String topic;
        public boolean isFavorite;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            isFavorite = false;
            tvKeywords = itemView.findViewById(R.id.tvKeywordsCard);
            btnFav = itemView.findViewById(R.id.btnFav);
            ivPreview = itemView.findViewById(R.id.ivNotePreview);
            firebaseStorage = FirebaseStorage.getInstance();
            progressDrawable = new CircularProgressDrawable(itemView.getContext());
            progressDrawable.setCenterRadius(40f);
            progressDrawable.setStrokeWidth(5f);
            progressDrawable.start();
            this.favorites = NotesExploreAdapter.this.favoritesManager;
            topic = NotesExploreAdapter.this.topic;
        }

        protected void bind(Note note) {
            String keywords = new String();
            isFavorite = favorites.checkIfFavorite(note.getUrl(), topic);
            if (showFavoritesOnly && !isFavorite) {
                disableVisibility();
            }
            setBtnFav(isFavorite);
            //Get 5 keywords at most
            int counter = 0;
            List<String> keywordsRaw = note.getKeywords();
            keywords = GetListOfKeywordsString.getString(keywordsRaw, keywordsRaw.size());
            tvDate.setText(note.getDate());
            tvKeywords.setText(keywords);
            Glide.with(itemView)
                    .load(note.getImageURL())
                    .placeholder(progressDrawable)
                    .into(ivPreview);
            Log.d("ViewHolder", String.format("Note id: %s. isFaved: %s", note.getNoteId(), isFavorite));
            btnFav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //if it's not in favorites list add it
                    if (!isFavorite) {
                        favorites.addFav(note.getUrl(), topic);
                        isFavorite = true;
                        setBtnFav(true);
                    } else {
                        favorites.remove(note.getUrl(), topic);
                        setBtnFav(false);
                        isFavorite = false;
                    }
                }
            });
            this.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, FullScreenCardActivity.class);
                    intent.putExtra("note", note);
                    context.startActivity(intent);
                }
            });
        }

        public void disableVisibility() {
            this.itemView.setVisibility(View.GONE);
            ViewGroup.LayoutParams params = this.itemView.getLayoutParams();
            params.height = 0;
            params.width = 0;
            this.itemView.setLayoutParams(params);
        }

        public void enableVisibility() {
            this.itemView.setVisibility(View.VISIBLE);
            ViewGroup.LayoutParams params = this.itemView.getLayoutParams();
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            this.itemView.setLayoutParams(params);
        }

        //if true then replace drawable with filled star
        private void setBtnFav(boolean set) {
            int drawableId = set ? R.drawable.ic_baseline_star_24 : R.drawable.ic_baseline_star_outline_24;
            btnFav.setImageResource(drawableId);
        }
    }
}
