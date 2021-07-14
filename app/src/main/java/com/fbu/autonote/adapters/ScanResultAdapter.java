package com.fbu.autonote.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fbu.autonote.R;
import com.geniusscansdk.scanflow.ScanResult;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class ScanResultAdapter extends RecyclerView.Adapter<ScanResultAdapter.ViewHolder> {

    public List<ScanResult.Scan> scans;
    Context context;
    public ScanResultAdapter(Context context, List<ScanResult.Scan> scans) {
        this.context = context;
        this.scans = scans;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        //TODO: add a fullscreen view mode upon view click
        View view = LayoutInflater.from(context).inflate(R.layout.item_scan_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        //get cropped image from the scan results
        File scan = scans.get(position).enhancedImageFile;
        holder.bind(scan);
    }

    @Override
    public int getItemCount() {
        return scans.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivScanResultPreview;
        TextView tvPositionResult;
        Bitmap scanBitmap;
        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            ivScanResultPreview = itemView.findViewById(R.id.ivResultPreview);
            tvPositionResult = itemView.findViewById(R.id.tvPostitionResult);
        }

        //Bind scan result to views inside the viewholder. Image and position in this case
        public void bind(File scan) {
            //Load bitmap from scan file
            scanBitmap = BitmapFactory.decodeFile(scan.getAbsolutePath());
            ivScanResultPreview.setImageBitmap(scanBitmap);
            //Display the number of page in document
            tvPositionResult.setText(String.valueOf(getAdapterPosition() + 1));
        }
    }

}
