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

import com.fbu.autonote.R;
import com.fbu.autonote.models.Note;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class NotesExploreAdapter extends RecyclerView.Adapter<NotesExploreAdapter.ViewHolder> {
    List<Note> notes;
    Context context;

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

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate;
        TextView tvKeywords;
        ImageView ivPreview;
        ImageButton btnFav;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvKeywords = itemView.findViewById(R.id.tvKeywordsCard);
            btnFav = itemView.findViewById(R.id.btnFav);

        }

        protected void bind(Note note) {

        }
    }
}
