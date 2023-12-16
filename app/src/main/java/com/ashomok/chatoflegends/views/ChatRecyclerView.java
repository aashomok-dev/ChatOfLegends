package com.ashomok.chatoflegends.views;

import android.content.Context;
import android.util.AttributeSet;

import androidx.recyclerview.widget.RecyclerView;

//this class will prevent recyclerView from scrolling when
// the keyboard opens to keep the items in bounds
public class ChatRecyclerView extends RecyclerView {
    private int oldHeight;

    public ChatRecyclerView(Context context) {
        super(context);
    }

    public ChatRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChatRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        int delta = b - t - oldHeight;
        oldHeight = b - t;
        if (0 > delta) {
            scrollBy(0, -delta);
        }
    }
}