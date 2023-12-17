package com.ashomok.chatoflegends.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.ashomok.chatoflegends.R;

//Compat Class to make Vector Drawables work on Older APIs when using DrawabeLeft,Right,Top,Bottom
public class TextViewDrawableCompat extends androidx.appcompat.widget.AppCompatTextView {
    public TextViewDrawableCompat(Context context) {
        super(context);
        initAttrs(context, null);
    }

    public TextViewDrawableCompat(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context, attrs);
    }

    public TextViewDrawableCompat(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
    }

    void initAttrs(Context context, AttributeSet attrs) {
        if (null != attrs) {
            TypedArray attributeArray = context.obtainStyledAttributes(
                    attrs,
                    R.styleable.TextViewDrawableCompat);

            Drawable drawableStart = null;
            Drawable drawableEnd = null;
            Drawable drawableBottom = null;
            Drawable drawableTop = null;
            drawableStart = attributeArray.getDrawable(R.styleable.TextViewDrawableCompat_drawableStartCompat);
            drawableEnd = attributeArray.getDrawable(R.styleable.TextViewDrawableCompat_drawableEndCompat);
            drawableBottom = attributeArray.getDrawable(R.styleable.TextViewDrawableCompat_drawableBottomCompat);
            drawableTop = attributeArray.getDrawable(R.styleable.TextViewDrawableCompat_drawableTopCompat);

            int tintColor = attributeArray.getColor(R.styleable.TextViewDrawableCompat_drawableTintCompat, -1);
            if (-1 != tintColor) {
                if (null != drawableStart)
                    drawableStart.setColorFilter(tintColor, PorterDuff.Mode.SRC_IN);
                if (null != drawableEnd)
                    drawableEnd.setColorFilter(tintColor, PorterDuff.Mode.SRC_IN);
                if (null != drawableTop)
                    drawableTop.setColorFilter(tintColor, PorterDuff.Mode.SRC_IN);
                if (null != drawableBottom)
                    drawableBottom.setColorFilter(tintColor, PorterDuff.Mode.SRC_IN);
            }

            // to support rtl
            setCompoundDrawablesRelativeWithIntrinsicBounds(drawableStart, drawableTop, drawableEnd, drawableBottom);
            attributeArray.recycle();
        }
    }
}


