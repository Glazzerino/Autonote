package com.fbu.autonote.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.fbu.autonote.fragments.NotesFragment;
import com.fbu.autonote.R;
import com.fbu.autonote.fragments.ScanResultsFragment;
import com.geniusscansdk.core.GeniusScanSDK;
import com.geniusscansdk.core.LicenseException;
import com.geniusscansdk.scanflow.ScanConfiguration;
import com.geniusscansdk.scanflow.ScanFlow;
import com.geniusscansdk.scanflow.ScanResult;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import es.dmoral.toasty.Toasty;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";
    BottomNavigationView bottomMenu;
    Fragment fragment;
    FragmentManager fragmentManager;
    Context context;
    List<ScanResult.Scan> scans;
    FirebaseAuth authManager;
    DatabaseReference databaseReference;
    String userId;
    FirebaseStorage imageStorage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomMenu = findViewById(R.id.menuBottomNav);
        fragmentManager = getSupportFragmentManager();
        context = this;
        authManager = FirebaseAuth.getInstance();
        userId = authManager.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference(userId);
        imageStorage = FirebaseStorage.getInstance();
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
                        /**
                         * Due to GeniusSDK's dependence on onActivityResult, fragment assigning
                         * gets done from withing initScanner()
                         **/
                        initScanner();
                        break;
                }
                startFragment();
                return true;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            //Get results from scan and launch the scanResult fragment to upload data
            ScanResult result = ScanFlow.getScanResultFromActivityResult(data);
            scans = result.scans;
            fragment = ScanResultsFragment.newInstance(scans);
            startFragment();
            uploadImages();
        } catch (Exception e) {
            Log.e("MainActivity", e.toString());
        }
    }
    private void initScanner() {
        ScanConfiguration scanConfiguration = new ScanConfiguration();
        scanConfiguration.multiPage = true;
        ScanFlow.scanWithConfiguration(this, scanConfiguration);
    }

    private void startFragment() {
        if (fragment != null) {
            fragmentManager.beginTransaction()
                    .replace(R.id.flFragmentContainer, fragment)
                    .commit();
        }
    }
    private void uploadImages() {
        Toasty.info(context, "Uploading scans to the cloud", Toast.LENGTH_LONG).show();
        List<String> imagesUris = new ArrayList<>();
        String newNoteCollectionId = String.valueOf(Math.abs(new Random().nextLong()));
        StorageReference newNoteStorage = imageStorage
                .getReference(String.valueOf(userId))
                .child(newNoteCollectionId);

        //upload each file to the newNote directory
        for (int i = 0; i < scans.size(); i++) {
            File imageFile = scans.get(i).enhancedImageFile;
            //Reference to new file in Firebase. Give it the name of the on-device file
            StorageReference imageReference = newNoteStorage.child(imageFile.getName());
            //Convert to byte array
            Bitmap imageBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            byte[] byteArray = outputStream.toByteArray();

            UploadTask upload = imageReference.putBytes(byteArray);
            upload.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull @NotNull Exception e) {
                    Log.e(TAG,
                    String.format("Error uploading file %s : %s",
                            imageFile.getName(),
                            e.toString()));
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.d(TAG, "Success uploading file " + imageFile.getName());
                    imagesUris.add(newNoteStorage.getPath());
                }
            });
        }
    }
}
