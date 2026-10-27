package com.eggwall.licktrainer;

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
 * Main Activity hosting the interactive Guitar Lick Trainer, 6-String Tablature
 * Sequencer, and 15-Fret Neck Inspector ({@link LickTrainerView}) alongside the
 * real-time 12-voice Karplus-Strong physical-modeling guitar synthesizer
 * ({@link LickSynthEngine}).
 *
 * <p>Displays the system status bar at the top and respects the user's normal
 * Android screen timeout.
 */
public class LickTrainerActivity extends Activity {

    private LickTrainerView mTrainerView;
    private LickSynthEngine mSynthEngine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        mSynthEngine = new LickSynthEngine();
        mTrainerView = new LickTrainerView(this);
        mTrainerView.setSynthEngine(mSynthEngine);
        mTrainerView.loadState(this);

        setContentView(wrapWithStatusBarInsets(mTrainerView, 0xFF0A0D14));
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
            int catIdx      = b.getInt("cat_idx", 0);
            int lickInCat   = b.getInt("lick_in_cat", 0);
            int keyIdx      = b.getInt("key_idx", 5);
            int curStep     = b.getInt("cur_step", 0);
            int bpm         = b.getInt("bpm", -1);
            int ladderIdx   = b.getInt("ladder_idx", 0);
            int mixMode     = b.getInt("mix_mode", LickTheory.MIX_TAB_BACK_CLICK);
            boolean playing = b.getBoolean("playing", false);
            boolean loopBar = b.getBoolean("loop_bar", true);
            boolean showHelp = b.getBoolean("show_help", false);
            mTrainerView.configureDemoState(
                    catIdx, lickInCat, keyIdx, curStep, bpm,
                    ladderIdx, mixMode, playing, loopBar, showHelp);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mSynthEngine != null) {
            mSynthEngine.start();
        }
        if (mTrainerView != null) {
            mTrainerView.requestFocus();
            mTrainerView.previewCurrentStep();
        }
    }

    @Override
    protected void onPause() {
        if (mTrainerView != null) {
            mTrainerView.stopPlayback();
            mTrainerView.saveState(this);
        }
        if (mSynthEngine != null) {
            mSynthEngine.stop();
        }
        super.onPause();
    }
}
