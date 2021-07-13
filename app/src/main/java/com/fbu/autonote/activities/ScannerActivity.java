package com.fbu.autonote.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.fbu.autonote.R;
import com.labters.documentscanner.helpers.ScannerConstants;

import org.jetbrains.annotations.NotNull;

public class ScannerActivity extends AppCompatActivity {
    ImageView ivTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        ivTest = findViewById(R.id.ivImageTest);

        Glide.with(this)
                .asBitmap()
                .load("https://imgur.com/qtt0yqH.jpeg")
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull @NotNull Bitmap resource, @Nullable @org.jetbrains.annotations.Nullable Transition<? super Bitmap> transition) {
                        ScannerConstants.selectedImageBitmap = resource;
                        Intent intent = new Intent(ScannerActivity.this, CropActivity.class);
                        startActivityForResult(intent, 1);
                    }

                    @Override
                    public void onLoadCleared(@Nullable @org.jetbrains.annotations.Nullable Drawable placeholder) {

                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==1 && resultCode== Activity.RESULT_OK )
        {
            if (ScannerConstants.selectedImageBitmap!=null)
                ivTest.setImageBitmap(ScannerConstants.selectedImageBitmap);
            else
                Toast.makeText(this,"Something went wrong.",Toast.LENGTH_LONG).show();
        }
    }
}