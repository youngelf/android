package com.eggwall.pomodoro;

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
 * Main host Activity for the Pomodoro Focus Timer & Work-Hours Tracker.
 *
 * <p>Displays the system status bar at the top, respects the user's normal
 * Android screen timeout, and delegates audio/timer lifecycle to {@link PomodoroView}.
 */
public final class PomodoroActivity extends Activity {

    private PomodoroView mView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        mView = new PomodoroView(this);
        setContentView(wrapWithStatusBarInsets(mView, 0xFF0E1218));
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
        if (b.getBoolean("demo_mode", false)) {
            int mode = b.getInt("mode", PomodoroModel.MODE_WORK);
            int status = b.getInt("status", PomodoroModel.STATUS_RUNNING);
            int remainingSec = b.getInt("remaining_sec", 18 * 60 + 42);
            int presetIdx = b.getInt("preset_idx", 0);
            int ambientMode = b.getInt("ambient_mode", PomodoroModel.AMBIENT_RAIN);
            int todayPoms = b.getInt("today_poms", 3);
            int totalPoms = b.getInt("total_poms", 47);
            long todayWorkSec = b.getLong("today_work_sec", 3L * 25L * 60L + 378L);
            long totalWorkSec = b.getLong("total_work_sec", 47L * 25L * 60L + 378L);
            boolean showHelp = b.getBoolean("show_help", false);
            mView.configureDemoState(
                    mode, status, remainingSec, presetIdx, ambientMode,
                    todayPoms, totalPoms, todayWorkSec, totalWorkSec, showHelp);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mView != null) {
            mView.onActivityResume();
        }
    }

    @Override
    protected void onPause() {
        if (mView != null) {
            mView.onActivityPause();
        }
        super.onPause();
    }
}
