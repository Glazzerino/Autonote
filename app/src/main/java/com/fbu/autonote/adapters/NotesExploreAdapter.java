package com.fbu.autonote.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.fbu.autonote.R;
import com.fbu.autonote.models.Note;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class NotesExploreAdapter extends RecyclerView.Adapter<NotesExploreAdapter.ViewHolder> {
    List<Note> notes;
    Context context;
    FirebaseStorage firebaseStorage;
    String userId;
    //40MB
    public static final long BYTE_DOWNLOAD_LIMIT = 40000000;
    public NotesExploreAdapter(Context context) {
        this.context = context;
        notes = new ArrayList<>();
        firebaseStorage = FirebaseStorage.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate;
        TextView tvKeywords;
        ImageView ivPreview;
        ImageButton btnFav;
        FirebaseStorage firebaseStorage;
        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvKeywords = itemView.findViewById(R.id.tvKeywordsCard);
            btnFav = itemView.findViewById(R.id.btnFav);
            ivPreview = itemView.findViewById(R.id.ivNotePreview);
            firebaseStorage = FirebaseStorage.getInstance();
            btnFav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toasty.info(v.getContext(), "Hello!", Toasty.LENGTH_SHORT).show();
                }
            });
        }

        //Image loading is done inside the bind method to avoid loading images not being shown
        //in the recyclerview
        protected void bind(Note note) {
            String keywords = new String();

            //Get only 5 keywords at most
            int counter = 0;
            for (String keyword : note.getKeywords()) {
                keywords += (keyword + ", ");
                if (counter++ > 5) {
                    break;
                }
            }
            
            tvKeywords.setText(keywords);
            StorageReference imageDownloadTask = firebaseStorage.getReferenceFromUrl(note.getImageURL());
            //Using Glide to load the image since Firebase Storage offers no caching features
            Glide.with(itemView)
                    .load(note.getImageURL())
                    .into(ivPreview);

        }
    }
}
