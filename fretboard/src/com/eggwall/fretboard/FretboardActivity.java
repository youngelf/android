/*
 * FretboardActivity.java - Main Android Activity for Fretboard Explorer
 *
 * Copyright 2026 Vikram Aggarwal
 * SPDX-License-Identifier: GPL-2.0-only
 */

package com.eggwall.fretboard;

import android.app.Activity;
import android.graphics.Insets;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.widget.FrameLayout;

/**
 * Host Activity managing {@link FretboardView} and the real-time
 * {@link GuitarSynthEngine} audio lifecycle.
 *
 * <p>Displays the system status bar at the top and respects the user's normal
 * Android screen timeout.
 */
public final class FretboardActivity extends Activity {
    private GuitarSynthEngine synthEngine;
    private FretboardView fretboardView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        synthEngine = new GuitarSynthEngine();
        fretboardView = new FretboardView(this, synthEngine);
        fretboardView.loadState(this);
        setContentView(wrapWithStatusBarInsets(fretboardView, 0xFF0D1117));
    }

    @SuppressWarnings("deprecation")
    private View wrapWithStatusBarInsets(View child, int statusBarBgColor) {
        Window window = getWindow();
        if (window != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(statusBarBgColor);
            window.setNavigationBarColor(statusBarBgColor);
        }
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(statusBarBgColor);
        root.addView(child, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            root.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                @Override
                public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        Insets sys = insets.getInsets(WindowInsets.Type.systemBars());
                        v.setPadding(sys.left, sys.top, sys.right, sys.bottom);
                    } else {
                        v.setPadding(
                                insets.getSystemWindowInsetLeft(),
                                insets.getSystemWindowInsetTop(),
                                insets.getSystemWindowInsetRight(),
                                insets.getSystemWindowInsetBottom());
                    }
                    return insets;
                }
            });
        }
        return root;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (synthEngine != null) {
            synthEngine.start();
        }
    }

    @Override
    protected void onPause() {
        if (fretboardView != null) {
            fretboardView.saveState(this);
        }
        if (synthEngine != null) {
            synthEngine.stop();
        }
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if (fretboardView != null && fretboardView.isShowingHelp()) {
            fretboardView.dismissHelp();
            return;
        }
        super.onBackPressed();
    }
}
