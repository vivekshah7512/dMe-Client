package com.decideme.client.attributes;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.GridView;

import com.decideme.client.R;


public class ExpandableHeightGridView extends GridView {


	private Context mContext;
	private String mFont;
	boolean expanded = false;
	
	public ExpandableHeightGridView(Context context) {
		super(context, null);
		mContext = context;
		init();
	}
	public boolean isExpanded() {
		return expanded;
	}

	public ExpandableHeightGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		TypedArray a = context.getTheme().obtainStyledAttributes(
		        attrs,
				R.styleable.CustomTextview,
		        0, 0);
		try {
			mFont = a.getString(R.styleable.CustomTextview_fontName);
		} finally {
			a.recycle();
		}
		init();
	}
	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// HACK! TAKE THAT ANDROID!
		if (isExpanded()) {
			// Calculate entire height by providing a very large height hint.
			// But do not use the highest 2 bits of this integer; those are
			// reserved for the MeasureSpec mode.
			int expandSpec = MeasureSpec.makeMeasureSpec(
					Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
			super.onMeasure(widthMeasureSpec, expandSpec);

			ViewGroup.LayoutParams params = getLayoutParams();
			params.height = getMeasuredHeight();
		} else {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
	}

	public void setExpanded(boolean expanded) {
		this.expanded = expanded;
	}
	
	public void init() {
		if (mFont != null) {
			Typeface tf = Typeface.createFromAsset(mContext.getAssets(), Constant.font_regular);
//			setTypeface(Typeface.createFromAsset(mContext.getAssets(), "fonts/"+ Constant.fontName));
//			setTypeface(tf, Typeface.NORMAL);
		}
	}
	
	
}
