package com.fbu.autonote.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.fbu.autonote.fragments.NotesFragment;
import com.fbu.autonote.R;
import com.fbu.autonote.fragments.ScanFragment;
import com.geniusscansdk.core.GeniusScanSDK;
import com.geniusscansdk.core.LicenseException;
import com.geniusscansdk.scanflow.ScanConfiguration;
import com.geniusscansdk.scanflow.ScanFlow;
import com.geniusscansdk.scanflow.ScanResult;
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

        //Init GeniusSDK
        try {
            GeniusScanSDK.init(context, getString(R.string.genius_apikey));
            Log.d("MainActivity", "GeniusSDK initialized");
        } catch (LicenseException exception) {
            Log.e("MainActivity", exception.toString());
        }

        //Set bottom menu button actions
        //TODO: PROFILE VIEW
        //TODO: NOTES VIEW
        bottomMenu.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.itemNotes:
                        fragment = NotesFragment.newInstance(context);
                        break;
                    case R.id.itemScan:
                        ScanConfiguration scanConfiguration = new ScanConfiguration();
                        scanConfiguration.multiPage = true;
                        ScanFlow.scanWithConfiguration(MainActivity.this, scanConfiguration);
                        break;
                }
                if (fragment != null) {
                    fragmentManager.beginTransaction()
                            .replace(R.id.flFragmentContainer, fragment)
                            .commit();
                }
                return true;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            ScanResult result = ScanFlow.getScanResultFromActivityResult(data);
            Toast.makeText(this, "Scanned!", Toast.LENGTH_SHORT).show();
            
        } catch (Exception e) {
            // There was an error during the scan flow. Check the exception for more details.
        }
    }
}
