package com.fbu.autonote.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fbu.autonote.R;
import com.fbu.autonote.adapters.NotesExploreAdapter;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;

import org.jetbrains.annotations.NotNull;

import es.dmoral.toasty.Toasty;


public class ProfileFragment extends Fragment {
    TabLayout tabLayout;
    TabItem tabRecent;
    TabItem tabFavorites;
    RecyclerView rvProfileNotes;
    NotesExploreAdapter notesAdapter;
    TextView tvDisplayName;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance() {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tabLayout = view.findViewById(R.id.tabLayout);
        tabFavorites = view.findViewById(R.id.tabFavorites);
        tabRecent = view.findViewById(R.id.tabRecent);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    //Recent
                    case 0:
                        Toasty.info(getContext(), "Hello recents", Toasty.LENGTH_SHORT).show();
                        populateWithRecent();
                        break;
                    //Favorites
                    case 1:
                        Toasty.info(getContext(), "Hello Favs", Toasty.LENGTH_SHORT).show();
                        break;
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void populateWithRecent() {

    }
}