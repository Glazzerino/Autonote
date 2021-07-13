package com.fbu.autonote;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

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
                        break;
                }
                fragmentManager.beginTransaction()
                        .replace(R.id.flFragmentContainer, fragment)
                        .commit();
                return true;
            }
        });
    }
}