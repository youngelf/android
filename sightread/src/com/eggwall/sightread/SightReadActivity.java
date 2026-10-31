package com.eggwall.sightread;

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
 * Main host Activity for {@link SightReadView}.
 *
 * <p>Displays the system status bar at the top and respects the user's normal
 * Android screen timeout.
 */
public class SightReadActivity extends Activity {

    private SightReadView mView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        mView = new SightReadView(this);
        setContentView(wrapWithStatusBarInsets(mView, 0xFF1E242E));
        applyDemoExtras(getIntent());
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
        applyDemoExtras(intent);
    }

    private void applyDemoExtras(Intent intent) {
        AppI18n.initFromIntent(this, intent);
        if (intent == null || mView == null) return;
        mView.invalidate();
        boolean hasDemo = intent.hasExtra("demo_help")
                || intent.hasExtra("demo_clef")
                || intent.hasExtra("demo_key")
                || intent.hasExtra("demo_pos")
                || intent.hasExtra("demo_ex")
                || intent.hasExtra("demo_mode");
        if (hasDemo) {
            int clef = intent.getIntExtra("demo_clef", SightReadTheory.CLEF_TREBLE);
            int key = intent.getIntExtra("demo_key", 1);
            int pos = intent.getIntExtra("demo_pos", 0);
            int ex = intent.getIntExtra("demo_ex", 4);
            int mode = intent.getIntExtra("demo_mode", SightReadTheory.MODE_ETUDE);
            boolean showHelp = intent.getBooleanExtra("demo_help", false);
            mView.configureDemoState(clef, key, pos, ex, mode, showHelp);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mView != null) {
            mView.onHostPause();
        }
    }

    @Override
    protected void onDestroy() {
        if (mView != null) {
            mView.onHostDestroy();
        }
        super.onDestroy();
    }
}
