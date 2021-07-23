package com.fbu.autonote.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fbu.autonote.R;
import com.fbu.autonote.adapters.TopicsAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

//This fragment acts as the greeting page of the app.
public class TopicSelectionFragment extends Fragment {
    private Context context;
    RecyclerView rvTopics;
    TextView tvBanner;
    DatabaseReference databaseReference;
    TopicsAdapter topicsAdapter;
    GridLayoutManager gridLayoutManager;

    public static final String TAG = "HomeFragment";
    public TopicSelectionFragment() {
        // Required empty public constructor
    }

    public static TopicSelectionFragment newInstance(Context context) {
        TopicSelectionFragment fragment = new TopicSelectionFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        fragment.context = context;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    //Gets topics inside user directory
    private void populateAdapterContainer(String userId) {
        databaseReference = FirebaseDatabase.getInstance().getReference(userId);
        //Get list of topics inside user directory
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for (DataSnapshot topicCollection : snapshot.getChildren()) {
                    topicsAdapter.addToContainer(topicCollection.getKey());
                }
                topicsAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Log.e(TAG, "Error getting list of topics: " + error.toString());
            }
        };
        databaseReference.addValueEventListener(eventListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        topicsAdapter = new TopicsAdapter(context);
        gridLayoutManager = new GridLayoutManager(context, 2);
        populateAdapterContainer(userId);
        rvTopics = view.findViewById(R.id.rvTopics);
        rvTopics.setAdapter(topicsAdapter);
        rvTopics.setLayoutManager(gridLayoutManager);
    }
}