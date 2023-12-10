package com.ashomok.heroai.views;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import androidx.core.os.BuildCompat;
import androidx.core.view.inputmethod.EditorInfoCompat;
import androidx.core.view.inputmethod.InputConnectionCompat;
import androidx.core.view.inputmethod.InputContentInfoCompat;

import com.aghajari.emojiview.view.AXEmojiEditText;

public class ChatEditText extends AXEmojiEditText {

    private String[] imgTypeString;
    final InputConnectionCompat.OnCommitContentListener callback =
            new InputConnectionCompat.OnCommitContentListener() {
                @Override
                public boolean onCommitContent(InputContentInfoCompat inputContentInfo,
                                               int flags, Bundle opts) {

                    // read and display inputContentInfo asynchronously
                    if (BuildCompat.isAtLeastNMR1() && 0 != (flags &
                            InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION)) {
                        try {
                            inputContentInfo.requestPermission();
                        } catch (Exception e) {
                            return false; // return false if failed
                        }
                    }
                    boolean supported = false;
                    for (String mimeType : imgTypeString) {
                        if (inputContentInfo.getDescription().hasMimeType(mimeType)) {
                            supported = true;
                            break;
                        }
                    }
                    return supported;// return true if succeeded
                }
            };

    public ChatEditText(Context context) {
        super(context);
        initView();
    }

    public ChatEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        imgTypeString = new String[]{
                "image/gif"
        };
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        InputConnection ic = super.onCreateInputConnection(outAttrs);
        EditorInfoCompat.setContentMimeTypes(outAttrs,
                imgTypeString);
        return InputConnectionCompat.createWrapper(ic, outAttrs, callback);
    }
}