package com.fbu.autonote.fragments;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.fbu.autonote.R;
import com.ortiz.touchview.TouchImageView;

import org.jetbrains.annotations.NotNull;

public class FullCardNoteFragment extends Fragment {

    Context context;
    TouchImageView touchImageView;
    public static final String TAG = "FullCardNoteFragment";

    public FullCardNoteFragment() {
    }

    public static FullCardNoteFragment newInstance(String iamgeUrl) {
        FullCardNoteFragment fragment = new FullCardNoteFragment();
        Bundle args = new Bundle();
        args.putString("imageUrl", iamgeUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_full_card_note, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        touchImageView = view.findViewById(R.id.tiNotePicture);
        String url = getArguments().getString("imageUrl");

        Glide.with(view)
                .asDrawable()
                .load(url)
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull @NotNull Drawable resource, @Nullable @org.jetbrains.annotations.Nullable Transition<? super Drawable> transition) {
                        touchImageView.setImageDrawable(resource);
                    }

                    @Override
                    public void onLoadCleared(@Nullable @org.jetbrains.annotations.Nullable Drawable placeholder) {
                        Log.d(TAG, "Load cleared");
                    }
                });
    }
}