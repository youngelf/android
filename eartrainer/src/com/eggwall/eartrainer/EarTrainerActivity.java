package com.eggwall.eartrainer;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Insets;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.widget.FrameLayout;

/**
 * Main Activity hosting the interactive Ear Trainer studio ({@link EarTrainerView})
 * and the real-time 44.1 kHz Karplus-Strong polyphonic audio synthesizer ({@link EarSynthEngine}).
 *
 * <p>Displays the system status bar at the top and respects the user's normal
 * Android screen timeout.
 */
public class EarTrainerActivity extends Activity {

    private EarTrainerView mView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        mView = new EarTrainerView(this);
        setContentView(wrapWithStatusBarInsets(mView, 0xFF0E121A));
        handleIntentExtras(getIntent());
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
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntentExtras(intent);
    }

    private void handleIntentExtras(Intent intent) {
        if (intent == null || intent.getExtras() == null || mView == null) {
            return;
        }
        Bundle b = intent.getExtras();
        if (b.containsKey("demo_state") || b.containsKey("mode") || b.containsKey("show_help")) {
            int mode = b.getInt("mode", EarTheory.MODE_INTERVALS);
            int tier = b.getInt("tier", EarTheory.TIER_ALL_12);
            boolean revealed = b.getBoolean("revealed", true);
            boolean showHelp = b.getBoolean("show_help", false);
            mView.configureDemoState(mode, tier, revealed, showHelp);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mView != null) {
            mView.requestFocus();
            mView.onResumeView();
        }
    }

    @Override
    protected void onPause() {
        if (mView != null) {
            mView.onPauseView();
        }
        super.onPause();
    }
}
