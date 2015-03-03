package com.snappy.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Layout;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

public class SimpleAutoFitTextView extends TextView {
	
	private int width=-1;
	private int height=-1;
	
	public SimpleAutoFitTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setSingleLine();
		setEllipsize(TruncateAt.END);
	}

	public SimpleAutoFitTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setSingleLine();
		setEllipsize(TruncateAt.END);
	}

	public SimpleAutoFitTextView(Context context) {
		super(context);
		setSingleLine();
		setEllipsize(TruncateAt.END);
	}
	
	@SuppressLint("WrongCall")
	public void callOnMeasure(){
		if(width!=-1 && height!=-1){
			onMeasure(width, height);
		}
	}

	@SuppressLint("WrongCall")
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		width=widthMeasureSpec;
		height=heightMeasureSpec;
		
		final Layout layout = getLayout();
		if (layout != null) {
			final int lineCount = layout.getLineCount();
			if (lineCount > 0) {
				final int ellipsisCount = layout.getEllipsisCount(lineCount - 1);
				if (ellipsisCount > 0) {

					final float textSize = getTextSize();
					// textSize is already expressed in pixels
					setTextSize(TypedValue.COMPLEX_UNIT_PX, (textSize - 1));
					onMeasure(widthMeasureSpec, heightMeasureSpec);
				}
			}
		}
	}
}
