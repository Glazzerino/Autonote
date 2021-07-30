package com.fbu.autonote.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.fbu.autonote.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

public class ConfirmDeleteFragment extends DialogFragment {
    DatabaseReference noteReference;
    public static final String TAG = "DialogFragment";

    public ConfirmDeleteFragment() {
    }

    public interface DialogClickListener {
        void onDialogPositiveClick(DialogFragment dialog);

        void onDialogNegativeClick(DialogFragment dialog);
    }

    DialogClickListener dialogListener;

    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);

        try {
            dialogListener = (DialogClickListener) context;
        } catch (ClassCastException e) {
            Log.e(TAG, e.toString());
        }
    }

    @NonNull
    @NotNull
    @Override
    public Dialog onCreateDialog(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        noteReference = FirebaseDatabase.getInstance().getReference(userId);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Delete note?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //noteReference.child()
                        dialogListener.onDialogPositiveClick(ConfirmDeleteFragment.this);
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialogListener.onDialogNegativeClick(ConfirmDeleteFragment.this);
            }
        });
        return builder.create();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_confirm_delete, container, false);
    }
}