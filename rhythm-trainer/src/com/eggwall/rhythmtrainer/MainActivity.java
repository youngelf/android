package com.eggwall.rhythmtrainer;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Insets;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.FrameLayout;

/**
 * Main Activity for Rhythm Trainer.
 * Hosts RhythmTrainerView with status-bar/cutout inset protection on API 19 through API 36.
 */
public class MainActivity extends Activity {
    private RhythmTrainerView view;

    @SuppressWarnings("deprecation")
    private static View wrapWithStatusBarInsets(Activity activity, View contentView, int bgColor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window w = activity.getWindow();
            w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            w.setStatusBarColor(bgColor);
            w.setNavigationBarColor(bgColor);
        }
        final FrameLayout wrapper = new FrameLayout(activity);
        wrapper.setBackgroundColor(bgColor);
        wrapper.addView(contentView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        final float density = activity.getResources().getDisplayMetrics().density;
        final int fallbackTop = (int) (24f * density);
        int statusBarResId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        final int resTop = statusBarResId > 0 ? activity.getResources().getDimensionPixelSize(statusBarResId) : fallbackTop;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            wrapper.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                @Override
                public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        Insets sys = insets.getInsets(
                                WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
                        v.setPadding(sys.left, sys.top > 0 ? sys.top : resTop, sys.right, sys.bottom);
                    } else {
                        int top = insets.getSystemWindowInsetTop();
                        int bottom = insets.getSystemWindowInsetBottom();
                        int left = insets.getSystemWindowInsetLeft();
                        int right = insets.getSystemWindowInsetRight();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && insets.getDisplayCutout() != null) {
                            top = Math.max(top, insets.getDisplayCutout().getSafeInsetTop());
                            bottom = Math.max(bottom, insets.getDisplayCutout().getSafeInsetBottom());
                            left = Math.max(left, insets.getDisplayCutout().getSafeInsetLeft());
                            right = Math.max(right, insets.getDisplayCutout().getSafeInsetRight());
                        }
                        v.setPadding(left, top > 0 ? top : resTop, right, bottom);
                    }
                    return insets;
                }
            });
        } else {
            wrapper.setPadding(0, resTop, 0, 0);
        }
        return wrapper;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        AppI18n.initFromIntent(this, getIntent());
        getWindow().setBackgroundDrawableResource(android.R.color.black);

        view = new RhythmTrainerView(this);
        applyIntentExtras(getIntent());
        setContentView(wrapWithStatusBarInsets(this, view, Color.parseColor("#0B101B")));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        AppI18n.initFromIntent(this, intent);
        applyIntentExtras(intent);
    }

    private void applyIntentExtras(Intent intent) {
        if (view == null || intent == null) return;
        boolean showHelp = intent.getBooleanExtra("show_help", false)
                || intent.getBooleanExtra("demo_help", false);
        view.setShowHelp(showHelp);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (view != null) {
            view.stopAudio();
        }
    }
}
