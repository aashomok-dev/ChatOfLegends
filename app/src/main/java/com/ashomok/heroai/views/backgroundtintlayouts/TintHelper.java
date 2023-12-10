package com.ashomok.heroai.views.backgroundtintlayouts;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.ashomok.heroai.R;

//this will change the background tint color for the layouts
public class TintHelper {
    final ViewGroup viewGroup;
    final AttributeSet attrs;
    final Context context;
    int color = -1;

    public TintHelper(Context context, ViewGroup viewGroup, AttributeSet attrs) {
        this.viewGroup = viewGroup;
        this.attrs = attrs;
        this.context = context;
        //get the color from corresponding attribute file
        if (viewGroup instanceof LinearLayout) {
            if (null != attrs) {
                TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.LinearLayoutWithBackgroundTint, 0, 0);
                if (null != array) {
                    color = array.getColor(R.styleable.LinearLayoutWithBackgroundTint_linearBgTintColor, -1);
                    array.recycle();
                }
            }
        } else if (viewGroup instanceof FrameLayout) {
            if (null != attrs) {
                TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.FrameLayoutWithBackgroundTint, 0, 0);
                if (null != array) {
                    color = array.getColor(R.styleable.FrameLayoutWithBackgroundTint_frameBgTintColor, -1);
                    array.recycle();
                }
            }
        } else if (viewGroup instanceof RelativeLayout) {
            if (null != attrs) {
                TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.RelativeLayoutWithBackgroundTint, 0, 0);
                if (null != array) {
                    color = array.getColor(R.styleable.RelativeLayoutWithBackgroundTint_relativeBgTintColor, -1);
                    array.recycle();
                }
            }
        }

        setBackgroundColor(viewGroup, color);

    }

    //set background color
    private void setBackgroundColor(ViewGroup viewGroup, int color) {
        Drawable thumbDrawable = viewGroup.getBackground().mutate();
        thumbDrawable.setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN);
    }
}
