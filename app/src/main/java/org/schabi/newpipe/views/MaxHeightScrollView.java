package org.schabi.newpipe.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ScrollView;

import org.schabi.newpipe.R;

public class MaxHeightScrollView extends ScrollView {

    private int maxHeight;
    private final int defaultHeight = 700;

    public MaxHeightScrollView(final Context context) {
        super(context);
    }

    public MaxHeightScrollView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            init(context, attrs);
        }
    }

    public MaxHeightScrollView(final Context context,
                               final AttributeSet attrs,
                               final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!isInEditMode()) {
            init(context, attrs);
        }
    }

    private void init(final Context context, final AttributeSet attrs) {
        if (attrs != null) {
            final TypedArray styledAttrs
                    = context.obtainStyledAttributes(attrs, R.styleable.MaxHeightScrollView);
            //400 is a defualt value
            maxHeight = styledAttrs.getDimensionPixelSize(
                            R.styleable.MaxHeightScrollView_maxHeight, defaultHeight);

            styledAttrs.recycle();
        }
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        int heightMeasureSpecb = heightMeasureSpec;
        heightMeasureSpecb = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, heightMeasureSpecb);
    }
}
