package com.fbu.autonote.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;

import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.data.BufferedOutputStream;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.fbu.autonote.R;
import com.google.common.util.concurrent.ListenableFuture;
import com.labters.documentscanner.ImageCropActivity;
import com.labters.documentscanner.helpers.ScannerConstants;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ScanFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ScanFragment extends Fragment {

    private Context context;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    PreviewView pvCameraPreview;
    ImageCapture imageCapture;
    CameraSelector cameraSelector;
    ImageButton btnShutter;
    File rawDirectory;
    File croppedDirectory;
    public static String RAW_DIR_NAME = "not_cropped";
    public static String CROPPED_DIR_NAME = "cropped";
    public static String FILE_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS";
    public final String TAG = "ScanFragment";

    //code used to assert result from the cropping activity
    public static final int CROP_REQUEST = 12;

    // Required empty public constructor
    public ScanFragment() { }

    public static ScanFragment newInstance(Context context) {
        ScanFragment fragment = new ScanFragment();
        Bundle args = new Bundle();
        fragment.context = context;
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

        return inflater.inflate(R.layout.fragment_scan, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cameraProviderFuture = ProcessCameraProvider.getInstance(getContext());
        pvCameraPreview = view.findViewById(R.id.pvCameraPreview);
        btnShutter = view.findViewById(R.id.btnShutter);
        initializeDirectories();
        //Offload process to a new thread managed by the ListenableFuture class
        cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();
        cameraProviderFuture = ProcessCameraProvider.getInstance(getContext());
        imageCapture = new ImageCapture.Builder()
                .setTargetRotation(getContext().getDisplay().getRotation())
                .setTargetResolution(new Size(1280, 720))
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .build();

        //Permission logic
        if (!assertPermissionsGranted()) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        } else {
            startCamera();
        }
        btnShutter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                capturePhoto();
            }
        });
    }

    private void bindToPreview(ProcessCameraProvider cameraProvider) {

        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(pvCameraPreview.getSurfaceProvider());

        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, imageCapture, preview);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            Bitmap croppedBitmap = ScannerConstants.selectedImageBitmap;
            //Create new empty file
            File croppedFile = new File(croppedDirectory + "/" + getTimeString() + ".jpg");

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            byte[] imageData = outputStream.toByteArray();
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(croppedFile);
                fileOutputStream.write(imageData);
                fileOutputStream.flush();
                fileOutputStream.close();
            } catch (IOException e) {
                Log.e(TAG, "Error creating outputStream for cropped file!: " + e.toString());
            }
        }
    }

    //Request for permissions
    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    Toast.makeText(context,
                            "Permission to use the camera is required to use this feature",
                            Toast.LENGTH_SHORT)
                            .show();
                } else {
                    Toast.makeText(context, "Camera access granted", Toast.LENGTH_SHORT).show();
                    startCamera();
                }
            });

    //Check if storage and camera permissions are granted
    private boolean assertPermissionsGranted() {
        return (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.MANAGE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED);
    }

    private void startCamera() {
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindToPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                //this isn't supposed to ever happen
                Log.e(TAG, "Error during CameraX bind to preview: " + e.toString());
            }
        }, ContextCompat.getMainExecutor(getContext()));
    }

    private File capturePhoto() {
        //Create new file
        String time = getTimeString();
        File file = new File(context.getFilesDir(),time + ".jpg");

        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions
                .Builder(file)
                .build();

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(context),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(ImageCapture.OutputFileResults outputFileResults) {
                        //Freeze preview so it shows the taken image
                        try {
                            cameraProviderFuture.get().unbindAll();
                            startCropActivity(file);
                        } catch (ExecutionException | InterruptedException e) {
                            e.printStackTrace();
                            Log.e(TAG, "Could not unbind camera provider from preview view: " + e.toString());
                        }
                        Log.d(TAG, "image saved: " + file.getAbsolutePath());
                    }
                    @Override
                    public void onError(ImageCaptureException error) {
                        Log.e(TAG, "Failed to save capture image: " + error.toString());
                    }
                }
        );
        return file;
    }

    @NotNull
    private String getTimeString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(FILE_FORMAT, Locale.getDefault());
        String time = dateFormat.format(new Date());
        return time;
    }

    //Send a specific file to the cropping activity
    private void startCropActivity(File image) {
        Bitmap imageBitmap = BitmapFactory.decodeFile(image.getPath());
        Log.e(TAG, image.getPath());
        Glide.with(context)
                .asBitmap()
                .load("https://i.imgur.com/WYTknCd.jpeg")
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull @NotNull Bitmap resource, @Nullable @org.jetbrains.annotations.Nullable Transition<? super Bitmap> transition) {
                        ScannerConstants.selectedImageBitmap = resource;
                        Intent intent = new Intent(context, ImageCropActivity.class);
                        startActivityForResult(intent, CROP_REQUEST);
                    }

                    @Override
                    public void onLoadCleared(@Nullable @org.jetbrains.annotations.Nullable Drawable placeholder) {

                    }
                });
    }

//This function takes care of initializing directory objets for both kinds of images
    /**
     * Francisco: Normally using a string would suffice but
     * due to the possibility of the directories to not exist prior to their usage implementing
     * the File obj is the best and most readable (as far as I can tell) way to manage these
     * possibilities
     */
    private void initializeDirectories() {
        File cacheDir = context.getCacheDir();
        rawDirectory = new File(cacheDir + "/" + RAW_DIR_NAME);
        croppedDirectory = new File(cacheDir + "/" + CROPPED_DIR_NAME);
        if (!rawDirectory.exists()) { rawDirectory.mkdir(); }
        if (!croppedDirectory.exists()) { croppedDirectory.mkdir(); }
    }
}