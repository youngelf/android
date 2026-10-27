package com.eggwall.metronome;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Insets;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.FrameLayout;

/**
 * Main host Activity for the Precision Metronome.
 *
 * <p>Displays the system status bar at the top, manages the lifecycle of the
 * sample-accurate 44.1 kHz {@link MetronomeSynthEngine}, and keeps the screen
 * awake ({@link WindowManager.LayoutParams#FLAG_KEEP_SCREEN_ON}) while practicing.
 */
public final class MetronomeActivity extends Activity {

    private MetronomeSynthEngine mEngine;
    private MetronomeView mView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mEngine = new MetronomeSynthEngine();
        mView = new MetronomeView(this, mEngine);
        setContentView(wrapWithStatusBarInsets(mView, 0xFF0B0E14));

        applyIntentExtras(getIntent());
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
        applyIntentExtras(intent);
    }

    private void applyIntentExtras(Intent intent) {
        if (intent == null || intent.getExtras() == null) {
            return;
        }
        Bundle b = intent.getExtras();
        if (b.containsKey("demo_mode")) {
            int bpm = b.getInt("bpm", 128);
            int timeSig = b.getInt("time_sig", 0);
            int subdiv = b.getInt("subdiv", 1);
            int soundKit = b.getInt("sound_kit", 0);
            int dropPct = b.getInt("drop_pct", 20);
            int trainer = b.getInt("trainer", 1);
            boolean playing = b.getBoolean("playing", true);
            boolean helpModal = b.getBoolean("help_modal", false);
            mView.configureDemoState(bpm, timeSig, subdiv, soundKit, dropPct, trainer, playing, helpModal);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mEngine.start();
    }

    @Override
    protected void onPause() {
        mView.savePreferences();
        mEngine.stop();
        super.onPause();
    }
}
