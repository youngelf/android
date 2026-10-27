package com.eggwall.chordwheel;

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
 * Main Activity hosting the interactive Circle of Fifths, Guitar Voicing Inspector,
 * and 4-Bar Progression Looper (`ChordWheelView`) alongside the real-time 6-string
 * Karplus-Strong physical-modeling guitar synthesizer (`GuitarSynthEngine`).
 *
 * <p>Displays the system status bar at the top and respects the user's normal
 * Android screen timeout.
 */
public class ChordWheelActivity extends Activity {

    private ChordWheelView mWheelView;
    private GuitarSynthEngine mSynthEngine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        mSynthEngine = new GuitarSynthEngine();
        mWheelView = new ChordWheelView(this);
        mWheelView.setSynthEngine(mSynthEngine);
        mWheelView.loadState(this);

        setContentView(wrapWithStatusBarInsets(mWheelView, 0xFF0E1117));
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
        if (intent == null || intent.getExtras() == null) {
            return;
        }
        Bundle b = intent.getExtras();
        if (b.containsKey("demo_state")) {
            int keySec  = b.getInt("key_sector", 0);
            int curSec  = b.getInt("cur_sector", keySec);
            int curRing = b.getInt("cur_ring", 0);
            boolean locked  = b.getBoolean("key_locked", false);
            boolean seventh = b.getBoolean("seventh_mode", false);
            int voicing = b.getInt("voicing_idx", 0);
            int preset  = b.getInt("preset_idx", 0);
            boolean loopPlaying = b.getBoolean("loop_playing", false);
            int loopStep = b.getInt("loop_step", 0);
            boolean showHelp = b.getBoolean("show_help", false);
            boolean showGuide = b.getBoolean("show_guide", false);
            mWheelView.configureDemoState(
                    keySec, curSec, curRing, locked, seventh,
                    voicing, preset, loopPlaying, loopStep, showHelp, showGuide);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mSynthEngine != null) {
            mSynthEngine.start();
        }
        if (mWheelView != null) {
            mWheelView.requestFocus();
            mWheelView.strumCurrentSelection();
        }
    }

    @Override
    protected void onPause() {
        if (mWheelView != null) {
            mWheelView.stopLoop();
            mWheelView.saveState(this);
        }
        if (mSynthEngine != null) {
            mSynthEngine.stop();
        }
        super.onPause();
    }
}
