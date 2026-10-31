package com.eggwall.pocketband;

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
 * Main host Activity for PocketBand.
 *
 * <p>Displays the system status bar at the top and keeps the screen awake
 * ({@link WindowManager.LayoutParams#FLAG_KEEP_SCREEN_ON}) so backing-track loops
 * continue uninterrupted during hands-free guitar or solo improvisation.
 */
public class PocketBandActivity extends Activity {

    private BandSynthEngine mEngine;
    private PocketBandView mView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mEngine = new BandSynthEngine();
        mView = new PocketBandView(this, mEngine);
        setContentView(wrapWithStatusBarInsets(mView, 0xFF0E121A));
        applyDemoIntentIfPresent(getIntent());
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
        applyDemoIntentIfPresent(intent);
    }

    private void applyDemoIntentIfPresent(Intent intent) {
        AppI18n.initFromIntent(this, intent);
        if (intent == null || mView == null) return;
        mView.invalidate();
        if (!intent.hasExtra("demo_style")
                && !intent.hasExtra("demo_help")
                && !intent.hasExtra("demo_key")) {
            return;
        }
        int style = intent.getIntExtra("demo_style", 0);
        int prog = intent.getIntExtra("demo_prog", -1);
        int key = intent.getIntExtra("demo_key", 9);
        int bpm = intent.getIntExtra("demo_bpm", 110);
        int bar = intent.getIntExtra("demo_bar", 0);
        int step = intent.getIntExtra("demo_step", 2);
        boolean help = intent.getBooleanExtra("demo_help", false);
        mView.configureDemoState(style, prog, key, bpm, bar, step, help);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mEngine != null) {
            mEngine.start();
        }
    }

    @Override
    protected void onPause() {
        if (mView != null) {
            mView.savePrefs();
        }
        if (mEngine != null) {
            mEngine.stop();
        }
        super.onPause();
    }
}
