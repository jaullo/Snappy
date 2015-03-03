/*
 * The MIT License (MIT)
 * 
 * Copyright � 2013 Clover Studio Ltd. All rights reserved.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.snappy.utils;

import android.app.Application;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.TextView;

/**
 * LayoutHelper
 * 
 * Calculates new measures and scales views according to koeficient value.
 */

public class LayoutHelper {

	/**
	 * @author wolf for dynamic change imageview inside relativelayout
	 * @param context
	 * @param koef
	 *            - with this koeficient width of the screen will be divided
	 * @param imageView
	 */
	public static void scaleWidthAndHeight(Context context, float koef,
			View view) {
		int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
		view.getLayoutParams().width = (int) (screenWidth / koef);
		view.getLayoutParams().height = (int) (screenWidth / koef);
	}

	public static void scaleWidth(Context context, float koef, View view) {
		int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
		view.getLayoutParams().width = (int) (screenWidth / koef);
	}

	public static void scaleWidthAndHeightAbsolute(Context context, float koef,
			View view) {
		int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
		int screenHeight = context.getResources().getDisplayMetrics().heightPixels;
		view.getLayoutParams().width = (int) (screenWidth / koef);
		view.getLayoutParams().height = (int) (screenHeight / koef);
	}

	public static void scaleByWidthRecursive(Context context, View view) {

		if (view instanceof ViewGroup) {

		} else {

		}
	}

	public static void scaleViewAndChildren(View root, float scale) {
		// Retrieve the view's layout information
		ViewGroup.LayoutParams layoutParams = root.getLayoutParams();

		// Scale the view itself
		if (layoutParams.width > 0) {
			layoutParams.width *= scale;
		}
		if (layoutParams.height > 0) {
			layoutParams.height *= scale;
		}

		// If this view has margins, scale those too
		if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
			ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) layoutParams;
			marginParams.leftMargin *= scale;
			marginParams.rightMargin *= scale;
			marginParams.topMargin *= scale;
			marginParams.bottomMargin *= scale;
		}
		// Set the layout information back into the view
		root.setLayoutParams(layoutParams);
		
		// Scale the view's padding
		root.setPadding((int) (root.getPaddingLeft() * scale),
				(int) (root.getPaddingTop() * scale),
				(int) (root.getPaddingRight() * scale),
				(int) (root.getPaddingBottom() * scale));

		// If the root view is a TextView, scale the size of its text
		if (root instanceof TextView) {
			TextView textView = (TextView) root;
			textView.setTextSize(textView.getTextSize() * scale);
		}

		// If the root view is a ViewGroup, scale all of its children
		// recursively
		if (root instanceof ViewGroup) {
			ViewGroup groupView = (ViewGroup) root;
			for (int cnt = 0; cnt < groupView.getChildCount(); ++cnt)
				scaleViewAndChildren(groupView.getChildAt(cnt), scale);
		}
	}

	public static float getK(Context context) {
		int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
		float widthK = screenWidth / 640;
		return widthK;
	}

	/**
	 * @author wolf for dynamic change imageview inside linearlayout
	 * @param context
	 * @param koef
	 *            - with this koeficient width of the screen will be divided
	 * @param imageView
	 */
	// public static void scaleWidthAndHeightLinearLayout(Context context, float
	// koef, ImageView imageView) {
	// int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
	// android.widget.LinearLayout.LayoutParams params =
	// (android.widget.LinearLayout.LayoutParams) imageView.getLayoutParams();
	// params.width = (int) (screenWidth / koef);
	// params.height = (int) (screenWidth / koef);
	// imageView.setLayoutParams(params);
	// }

}
