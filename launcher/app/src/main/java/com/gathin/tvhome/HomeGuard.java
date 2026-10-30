package com.gathin.tvhome;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

/**
 * Google TV's setup wizard registers a priority-1 HOME activity that shell can't disable, and Android
 * always resolves HOME to the highest-priority match before looking at the user's default, so a
 * sideloaded launcher can never be HOME there. The stock launcher stays enabled (it outranks the
 * wizard) and this service sends the Home button, and anything that lands on the stock home, here.
 */
public class HomeGuard extends AccessibilityService {

    private static final String STOCK_HOME = "com.google.android.apps.tv.launcherx";

    @Override
    protected void onServiceConnected() {
        // At boot the stock home is usually on screen before this service binds, so its window event
        // has already gone by. Check again a little later in case it was still drawing.
        Handler h = new Handler(Looper.getMainLooper());
        h.post(this::redirectIfStockHome);
        h.postDelayed(this::redirectIfStockHome, 3000);
    }

    private void redirectIfStockHome() {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root != null && root.getPackageName() != null && STOCK_HOME.contentEquals(root.getPackageName())) {
            goHome();
        }
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_HOME) {
            if (event.getAction() == KeyEvent.ACTION_UP) goHome();
            return true;
        }
        // Matched by Linux scan code: the button may have no Android keycode in the remote's key layout.
        int scan = getResources().getInteger(R.integer.shortcut_scancode);
        String input = getString(R.string.shortcut_input).trim();
        if (scan > 0 && !input.isEmpty() && event.getScanCode() == scan) {
            if (event.getAction() == KeyEvent.ACTION_UP) InputSwitch.open(this, input);
            return true;
        }
        return false;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                && event.getPackageName() != null
                && STOCK_HOME.contentEquals(event.getPackageName())) {
            goHome();
        }
    }

    private void goHome() {
        startActivity(new Intent(this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED));
    }

    @Override
    public void onInterrupt() {}
}
