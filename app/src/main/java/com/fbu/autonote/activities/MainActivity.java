package com.fbu.autonote.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.fbu.autonote.fragments.NotesFragment;
import com.fbu.autonote.R;
import com.fbu.autonote.fragments.ScanFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.labters.documentscanner.helpers.ScannerConstants;

import org.jetbrains.annotations.NotNull;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView bottomMenu;
    Fragment fragment;
    FragmentManager fragmentManager;
    Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomMenu = findViewById(R.id.menuBottomNav);
        fragmentManager = getSupportFragmentManager();
        context = this;

        //Set bottom menu button actions
        bottomMenu.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.itemNotes:
                        fragment = NotesFragment.newInstance(context);
                        break;
                    case R.id.itemScan:
<<<<<<< HEAD:app/src/main/java/com/fbu/autonote/activities/MainActivity.java
                        fragment = ScanFragment.newInstance(context);
                        break;
                }
                if (fragment != null) {
                    fragmentManager.beginTransaction()
                            .replace(R.id.flFragmentContainer, fragment)
                            .commit();
=======
                        Intent intent = new Intent(context, ScannerActivity.class);
                        startActivity(intent);
                        break;
                }
                if (fragment != null) {

                fragmentManager.beginTransaction()
                        .replace(R.id.flFragmentContainer, fragment)
                        .commit();
>>>>>>> 07d90641a60b5ae956a95e2b554711b3dd3f8fa1:app/src/main/java/com/fbu/autonote/MainActivity.java
                }

                return true;
            }
        });
    }
}
