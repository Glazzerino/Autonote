package com.fbu.autonote.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fbu.autonote.R;
import com.fbu.autonote.adapters.NotesExploreAdapter;
import com.fbu.autonote.models.Note;
import com.fbu.autonote.utilities.Favorites;
import com.fbu.autonote.utilities.LRUCache;
import com.fbu.autonote.utilities.RecentNotesManager;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class ProfileFragment extends Fragment {
    TabLayout tabLayout;
    TabItem tabRecent;
    TabItem tabFavorites;
    RecyclerView rvProfileNotes;
    NotesExploreAdapter notesAdapter;
    TextView tvDisplayName;
    Context context;
    List<Note> recentNotes;
    List<DatabaseReference> references;
    LinearLayoutManager linearLayoutManager;
    LRUCache<String> cache;
    ImageButton btnLogout;
    public static final String TAG = "ProfileFragment";

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
        recentNotes = new ArrayList<>();
        references = new ArrayList<>();
        rvProfileNotes = view.findViewById(R.id.rvProfileNotes);
        context = getContext();
        notesAdapter = new NotesExploreAdapter(getContext());
        linearLayoutManager = new LinearLayoutManager(getContext());
        rvProfileNotes.setLayoutManager(linearLayoutManager);
        rvProfileNotes.setAdapter(notesAdapter);
        btnLogout = view.findViewById(R.id.btnLogout);
        tvDisplayName = view.findViewById(R.id.tvDisplayName);
        tvDisplayName.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        //Default to recent notes
        populateWithRecent();
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        populateWithRecent();
                        break;
                    case 1:
                        populateWithFavorites();
                        break;
                }
            }
            //Necessary
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Delete all cache and favorite data
                File appFiles = context.getFilesDir();
                for (String subFile : appFiles.list()) {
                    new File(appFiles, subFile).delete();
                }
                appFiles.delete();
                FirebaseAuth.getInstance().signOut();
                getActivity().finish();
            }
        });
    }

    private void populateWithFavorites() {
        List<String> paths = Favorites.getInstance().getAll();
        loadAdapterWithNotePaths(paths);
    }

    //Loads note references from Firebase using the path to the entries
    private void loadAdapterWithNotePaths(List<String> paths) {
        notesAdapter.clearContainer();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        List<Task<DataSnapshot>> taskList = new LinkedList<>();
        for (String path : paths) {
            DatabaseReference noteReference = database.getReference(path);
            Log.d(TAG, "Path: " + path);
            taskList.add(noteReference.get());
        }
        Task<List<Task<DataSnapshot>>> fetchTasks = Tasks.whenAllSuccess(taskList);
        fetchTasks.addOnSuccessListener(new OnSuccessListener<List<Task<DataSnapshot>>>() {
            @Override
            public void onSuccess(List<Task<DataSnapshot>> tasks) {
                for (Object raw : tasks) {
                    DataSnapshot snapshot = (DataSnapshot) raw;
                    Note note = Note.fromDataSnapshot(snapshot);
                    notesAdapter.addToNoteContainer(note);
                }
                notesAdapter.notifyDataSetChanged();
            }
        });
    }

    private void populateWithRecent() {
        RecentNotesManager recentNotesManager = RecentNotesManager.getInstance();
        List<Task<DataSnapshot>> loadNotesTaskList = new LinkedList<>();
        cache = recentNotesManager.getContainer();
        List<String> paths = new LinkedList<>();
        for (LRUCache<String> it = cache; it.hasNext(); ) {
            paths.add(it.next());
        }
        loadAdapterWithNotePaths(paths);
    }
}