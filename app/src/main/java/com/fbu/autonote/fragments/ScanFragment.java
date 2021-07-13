package com.fbu.autonote.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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

import com.fbu.autonote.R;
import com.google.common.util.concurrent.ListenableFuture;
import com.labters.documentscanner.ImageCropActivity;

import org.jetbrains.annotations.NotNull;

import java.io.File;
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
    public static String FILE_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS";
    public final String TAG = "ScanFragment";

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
        SimpleDateFormat dateFormat = new SimpleDateFormat(FILE_FORMAT, Locale.getDefault());
        String time = dateFormat.format(new Date());
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
}