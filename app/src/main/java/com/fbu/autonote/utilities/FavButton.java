package com.fbu.autonote.utilities;

import android.content.Context;
import android.widget.ImageButton;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

public class FavButton extends androidx.appcompat.widget.AppCompatImageButton {
    boolean isFav;

    public FavButton(@NonNull @NotNull Context context) {
        super(context);
    }


}
