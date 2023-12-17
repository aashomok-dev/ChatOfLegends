package com.ashomok.chatoflegends.utils.keyboard;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * Created by Devlomi on 29/10/2017.
 */

//this class will show or hide the keyboard when needed
public enum KeyboardHelper {
    ;

    public static void hideSoftKeyboard(Context context, View view) {
        if (null == context) {
            return;
        }

        InputMethodManager imm = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

    }

    public static void hideSoftKeyboard(Context context, EditText editText) {

        InputMethodManager imm = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }


    public static void openSoftKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (null != imm) {
            imm.showSoftInput(view, 0);
        }
    }
}
