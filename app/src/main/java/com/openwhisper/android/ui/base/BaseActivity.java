package com.openwhisper.android.ui.base;

import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public abstract class BaseActivity extends AppCompatActivity {

    protected void setupEdgeToEdge() {
        EdgeToEdge.enable(this);
    }

    protected void applyRootSystemBarPadding(View root, int extraPaddingPx) {
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                    bars.left + extraPaddingPx,
                    bars.top + extraPaddingPx,
                    bars.right + extraPaddingPx,
                    bars.bottom + extraPaddingPx);
            return insets;
        });
        ViewCompat.requestApplyInsets(root);
    }

    protected void applyTopSystemBarPadding(View view) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), bars.top, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });
        ViewCompat.requestApplyInsets(view);
    }

    protected void applyImePadding(View view) {
        final int initialLeft = view.getPaddingLeft();
        final int initialTop = view.getPaddingTop();
        final int initialRight = view.getPaddingRight();
        final int initialBottom = view.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(
                    initialLeft + bars.left,
                    initialTop,
                    initialRight + bars.right,
                    initialBottom + bars.bottom);
            return insets;
        });
        ViewCompat.requestApplyInsets(view);
    }

    protected void applyImeOnlyPadding(View view) {
        final int initialLeft = view.getPaddingLeft();
        final int initialTop = view.getPaddingTop();
        final int initialRight = view.getPaddingRight();
        final int initialBottom = view.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            v.setPadding(initialLeft, initialTop, initialRight, initialBottom + ime.bottom);
            return insets;
        });
        ViewCompat.requestApplyInsets(view);
    }

    protected void applyBottomNavInsets(View bottomNav) {
        ViewCompat.setOnApplyWindowInsetsListener(bottomNav, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
        ViewCompat.requestApplyInsets(bottomNav);
    }
}
