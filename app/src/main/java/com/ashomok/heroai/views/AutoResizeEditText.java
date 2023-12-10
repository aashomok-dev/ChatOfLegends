package com.ashomok.heroai.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.util.TypedValue;

import com.aghajari.emojiview.view.AXEmojiEditText;

/*this AutoResize Features was created by @ViksaaSkool
https://github.com/ViksaaSkool/AutoFitEditText
i just copied and pasted it to make it extends EmojiconEditText to support emojis
 */
public class AutoResizeEditText extends AXEmojiEditText {
    private final RectF _availableSpaceRect;
    private final SparseIntArray _textCachedSizes;
    private final AutoResizeEditText.SizeTester _sizeTester;
    private float _maxTextSize;
    private float _spacingMult;
    private float _spacingAdd;
    private Float _minTextSize;
    private int _widthLimit;
    private int _maxLines;
    private boolean _enableSizeCache;
    private boolean _initiallized;
    private TextPaint paint;


    public AutoResizeEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        _availableSpaceRect = new RectF();
        _textCachedSizes = new SparseIntArray();
        _spacingMult = 1.0F;
        _spacingAdd = 0.0F;
        _enableSizeCache = true;
        _initiallized = false;
        _minTextSize = TypedValue.applyDimension(2, 12.0F, getResources().getDisplayMetrics());
        _maxTextSize = getTextSize();
        if (0 == _maxLines) {
            _maxLines = -1;
        }

        _sizeTester = new AutoResizeEditText.SizeTester() {
            final RectF textRect = new RectF();

            public int onTestSize(int suggestedSize, RectF availableSPace) {
                paint.setTextSize(suggestedSize);
                String text = getText().toString();
                boolean singleline = 1 == getMaxLines();
                if (singleline) {
                    textRect.bottom = paint.getFontSpacing();
                    textRect.right = paint.measureText(text);
                } else {
                    StaticLayout layout = new StaticLayout(text, paint, _widthLimit, Layout.Alignment.ALIGN_NORMAL, _spacingMult, _spacingAdd, true);
                    if (-1 != getMaxLines() && layout.getLineCount() > getMaxLines()) {
                        return 1;
                    }

                    textRect.bottom = layout.getHeight();
                    int maxWidth = -1;

                    for (int i = 0; i < layout.getLineCount(); ++i) {
                        if (maxWidth < layout.getLineWidth(i)) {
                            maxWidth = (int) layout.getLineWidth(i);
                        }
                    }

                    textRect.right = maxWidth;
                }

                textRect.offsetTo(0.0F, 0.0F);
                return availableSPace.contains(textRect) ? -1 : 1;
            }
        };
        _initiallized = true;
    }

    public void setTypeface(Typeface tf) {
        if (null == paint) {
            paint = new TextPaint(getPaint());
        }

        paint.setTypeface(tf);
        super.setTypeface(tf);
    }

    public void setTextSize(float size) {
        _maxTextSize = size;
        _textCachedSizes.clear();
        adjustTextSize();
    }

    public int getMaxLines() {
        return _maxLines;
    }

    public void setMaxLines(int maxlines) {
        super.setMaxLines(maxlines);
        _maxLines = maxlines;
        reAdjust();
    }

    public void setSingleLine() {
        super.setSingleLine();
        _maxLines = 1;
        reAdjust();
    }

    public void setSingleLine(boolean singleLine) {
        super.setSingleLine(singleLine);
        if (singleLine) {
            _maxLines = 1;
        } else {
            _maxLines = -1;
        }

        reAdjust();
    }

    public void setLines(int lines) {
        super.setLines(lines);
        _maxLines = lines;
        reAdjust();
    }

    public void setTextSize(int unit, float size) {
        Context c = getContext();
        Resources r;
        if (null == c) {
            r = Resources.getSystem();
        } else {
            r = c.getResources();
        }

        _maxTextSize = TypedValue.applyDimension(unit, size, r.getDisplayMetrics());
        _textCachedSizes.clear();
        adjustTextSize();
    }

    public void setLineSpacing(float add, float mult) {
        super.setLineSpacing(add, mult);
        _spacingMult = mult;
        _spacingAdd = add;
    }

    private void reAdjust() {
        adjustTextSize();
    }

    private void adjustTextSize() {
        if (_initiallized) {
            int startSize = Math.round(_minTextSize);
            int heightLimit = getMeasuredHeight() - getCompoundPaddingBottom() - getCompoundPaddingTop();
            _widthLimit = getMeasuredWidth() - getCompoundPaddingLeft() - getCompoundPaddingRight();
            if (0 < _widthLimit) {
                _availableSpaceRect.right = _widthLimit;
                _availableSpaceRect.bottom = heightLimit;
                super.setTextSize(0, efficientTextSizeSearch(startSize, (int) _maxTextSize, _sizeTester, _availableSpaceRect));
            }
        }
    }

    private int efficientTextSizeSearch(int start, int end, AutoResizeEditText.SizeTester sizeTester, RectF availableSpace) {
        if (!_enableSizeCache) {
            return binarySearch(start, end, sizeTester, availableSpace);
        } else {
            String text = getText().toString();
            int key = null == text ? 0 : text.length();
            int size = _textCachedSizes.get(key);
            if (0 != size) {
                return size;
            } else {
                size = binarySearch(start, end, sizeTester, availableSpace);
                _textCachedSizes.put(key, size);
                return size;
            }
        }
    }

    private int binarySearch(int start, int end, AutoResizeEditText.SizeTester sizeTester, RectF availableSpace) {
        int lastBest = start;
        int lo = start;
        int hi = end - 1;

        while (lo <= hi) {
            int mid = lo + hi >>> 1;
            int midValCmp = sizeTester.onTestSize(mid, availableSpace);
            if (0 > midValCmp) {
                lastBest = lo;
                lo = mid + 1;
            } else {
                if (0 >= midValCmp) {
                    return mid;
                }

                hi = mid - 1;
                lastBest = hi;
            }
        }

        return lastBest;
    }

    protected void onTextChanged(CharSequence text, int start, int before, int after) {
        super.onTextChanged(text, start, before, after);
        reAdjust();
    }

    protected void onSizeChanged(int width, int height, int oldwidth, int oldheight) {
        _textCachedSizes.clear();
        super.onSizeChanged(width, height, oldwidth, oldheight);
        if (width != oldwidth || height != oldheight) {
            reAdjust();
        }

    }

    private interface SizeTester {
        int onTestSize(int var1, RectF var2);
    }

}
