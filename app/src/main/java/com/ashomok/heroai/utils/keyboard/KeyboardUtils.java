package com.ashomok.heroai.utils.keyboard;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;

import java.util.HashMap;

public class KeyboardUtils implements ViewTreeObserver.OnGlobalLayoutListener {
    private static final int MAGIC_NUMBER = 200;
    private static final HashMap<SoftKeyboardToggleListener, KeyboardUtils> sListenerMap = new HashMap<>();
    private SoftKeyboardToggleListener mCallback;
    private final View mRootView;
    private Boolean prevValue;
    private final float mScreenDensity;

    private KeyboardUtils(Activity act, SoftKeyboardToggleListener listener) {
        mCallback = listener;

        mRootView = ((ViewGroup) act.findViewById(android.R.id.content)).getChildAt(0);
        mRootView.getViewTreeObserver().addOnGlobalLayoutListener(this);

        mScreenDensity = act.getResources().getDisplayMetrics().density;
    }

    /**
     * Add a new keyboard listener
     *
     * @param act      calling activity
     * @param listener callback
     */
    public static void addKeyboardToggleListener(Activity act, SoftKeyboardToggleListener listener) {
        KeyboardUtils.removeKeyboardToggleListener(listener);

        KeyboardUtils.sListenerMap.put(listener, new KeyboardUtils(act, listener));
    }

    /**
     * Remove a registered listener
     *
     * @param listener {@link SoftKeyboardToggleListener}
     */
    public static void removeKeyboardToggleListener(SoftKeyboardToggleListener listener) {
        if (KeyboardUtils.sListenerMap.containsKey(listener)) {
            KeyboardUtils k = KeyboardUtils.sListenerMap.get(listener);
            k.removeListener();

            KeyboardUtils.sListenerMap.remove(listener);
        }
    }

    /**
     * Remove all registered keyboard listeners
     */
    public static void removeAllKeyboardToggleListeners() {
        for (SoftKeyboardToggleListener l : KeyboardUtils.sListenerMap.keySet())
            KeyboardUtils.sListenerMap.get(l).removeListener();

        KeyboardUtils.sListenerMap.clear();
    }

    /**
     * Manually toggle soft keyboard visibility
     *
     * @param context calling context
     */
    public static void toggleKeyboardVisibility(Context context) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (null != inputMethodManager)
            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    /**
     * Force closes the soft keyboard
     *
     * @param activeView the view with the keyboard focus
     */
    public static void forceCloseKeyboard(View activeView) {
        InputMethodManager inputMethodManager = (InputMethodManager) activeView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (null != inputMethodManager)
            inputMethodManager.hideSoftInputFromWindow(activeView.getWindowToken(), 0);
    }

    @Override
    public void onGlobalLayout() {
        Rect r = new Rect();
        mRootView.getWindowVisibleDisplayFrame(r);

        int heightDiff = mRootView.getRootView().getHeight() - (r.bottom - r.top);
        float dp = heightDiff / mScreenDensity;
        boolean isVisible = MAGIC_NUMBER < dp;

        if (null != mCallback && (null == prevValue || isVisible != prevValue)) {
            prevValue = isVisible;
            mCallback.onToggleSoftKeyboard(isVisible);
        }
    }

    private void removeListener() {
        mCallback = null;

        mRootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
    }

    public interface SoftKeyboardToggleListener {
        void onToggleSoftKeyboard(boolean isVisible);
    }

}
