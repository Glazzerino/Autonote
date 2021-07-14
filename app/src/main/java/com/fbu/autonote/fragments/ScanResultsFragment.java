package com.fbu.autonote.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.fbu.autonote.R;
import com.fbu.autonote.activities.MainActivity;
import com.fbu.autonote.adapters.ScanResultAdapter;
import com.geniusscansdk.scanflow.ScanConfiguration;
import com.geniusscansdk.scanflow.ScanFlow;
import com.geniusscansdk.scanflow.ScanResult;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ScanResultsFragment extends Fragment {
    Context context;
    ScanResultAdapter scansAdapter;
    GridLayoutManager gridLayoutManager;
    RecyclerView rvScanResults;
    public static final String TAG = "ScanResultsFragment";
    public List<ScanResult.Scan> scans;
    //Required empty constructor
    public ScanResultsFragment() { }

    public static ScanResultsFragment newInstance(List<ScanResult.Scan> scans) {
        ScanResultsFragment fragment = new ScanResultsFragment();
        Bundle args = new Bundle();
        fragment.scans = scans;
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "scanner fragment initiated!");
        super.onCreate(savedInstanceState);
        //Avoids excessive function calling
        context = getContext();
        Log.d(TAG, "SCANS LEN: " + String.valueOf(scans.size()));
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_scan_results, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Setup recyclervie, adapter and manager
        rvScanResults = view.findViewById(R.id.rvScanResults);
        gridLayoutManager = new GridLayoutManager(context, 3);
        scansAdapter = new ScanResultAdapter(context, scans);
        rvScanResults.setAdapter(scansAdapter);
        rvScanResults.setLayoutManager(gridLayoutManager);
    }
}