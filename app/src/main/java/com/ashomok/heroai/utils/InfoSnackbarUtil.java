package com.ashomok.heroai.utils;

import android.view.View;

import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import com.ashomok.heroai.R;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

/**
 * Created by iuliia on 10/12/17.
 */
public enum InfoSnackbarUtil {
    ;

    public static void showError(@StringRes int errorMessageRes, View mRootView) {
        if (null != mRootView) {
            Snackbar snackbar = Snackbar.make(mRootView, errorMessageRes, BaseTransientBottomBar.LENGTH_LONG);
            snackbar.getView().setBackgroundColor(ContextCompat.getColor(mRootView.getContext(), R.color.red_500));
            snackbar.show();
        }
    }

    public static void showError(Throwable throwable, View mRootView) {
        if (null != mRootView) {
            String localizedMessage = throwable.getLocalizedMessage();
            if (null != localizedMessage && !localizedMessage.isEmpty()) {
                InfoSnackbarUtil.showError(throwable.getLocalizedMessage(), mRootView);
            } else {
                InfoSnackbarUtil.showError(throwable.getMessage(), mRootView);
            }
        }
    }

    public static void showError(String errorMessage, View mRootView) {
        if (null != mRootView ) {
            if (null != errorMessage && !errorMessage.isEmpty()) {
                Snackbar snackbar = Snackbar.make(mRootView, errorMessage, BaseTransientBottomBar.LENGTH_LONG);
                snackbar.getView().setBackgroundColor(ContextCompat.getColor(mRootView.getContext(), R.color.red_500));
                snackbar.show();
            }
        }
    }

    public static void showWarning(@StringRes int messageRes, View mRootView) {
        if (null != mRootView) {
            Snackbar snackbar = Snackbar.make(mRootView, messageRes, BaseTransientBottomBar.LENGTH_LONG);
            snackbar.getView().setBackgroundColor(ContextCompat.getColor(mRootView.getContext(), R.color.orange_500));
            snackbar.show();
        }
    }

    public static void showInfo(@StringRes int messageRes, View mRootView) {
        if (null != mRootView) {
            Snackbar snackbar = Snackbar.make(mRootView, messageRes, BaseTransientBottomBar.LENGTH_LONG);
            snackbar.getView().setBackgroundColor(ContextCompat.getColor(mRootView.getContext(), R.color.green_500));
            snackbar.show();
        }
    }

    public static void showInfo(String message, View mRootView) {
        if (null != mRootView) {
            Snackbar snackbar = Snackbar.make(mRootView, message, BaseTransientBottomBar.LENGTH_LONG);
            snackbar.getView().setBackgroundColor(ContextCompat.getColor(mRootView.getContext(), R.color.green_500));
            snackbar.show();
        }
    }
}
