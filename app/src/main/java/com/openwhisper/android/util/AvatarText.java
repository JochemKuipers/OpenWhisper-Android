package com.openwhisper.android.util;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.openwhisper.android.R;

public final class AvatarText {

    private AvatarText() {}

    @NonNull
    public static String initials(@NonNull String name) {
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            return "?";
        }
        String[] parts = trimmed.split("\\s+");
        if (parts.length >= 2) {
            return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
        }
        return ("" + trimmed.charAt(0)).toUpperCase();
    }

    @ColorInt
    public static int colorFor(@NonNull Context context, @NonNull String name) {
        int[] colors = context.getResources().getIntArray(R.array.ow_avatar_colors);
        if (colors.length == 0) {
            return ContextCompat.getColor(context, R.color.ow_primary);
        }
        int index = Math.abs(name.hashCode()) % colors.length;
        return colors[index];
    }

    public static void apply(@NonNull android.widget.TextView avatarView, @NonNull String name) {
        avatarView.setText(initials(name));
        GradientDrawable background = (GradientDrawable) avatarView.getBackground().mutate();
        background.setColor(colorFor(avatarView.getContext(), name));
    }
}
