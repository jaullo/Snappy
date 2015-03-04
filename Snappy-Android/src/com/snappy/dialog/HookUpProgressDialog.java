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

package com.snappy.dialog;

import com.snappy.R;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * HookUpProgressDialog
 * 
 * Shows custom HookUp progress dialog with animation.
 */

public class HookUpProgressDialog extends Dialog {

	private TextView mTvProgressTitle;
	private ImageView mIvAnimation;
	private AnimationDrawable mCloverAnimation;

	public HookUpProgressDialog(Context context) {
		super(context, R.style.Theme_Transparent);

		this.setContentView(R.layout.progress_dialog);
		mTvProgressTitle = (TextView) this.findViewById(R.id.tvProgressTitle);
		mTvProgressTitle.setVisibility(View.GONE);
		this.setCancelable(false);
		mIvAnimation = (ImageView) findViewById(R.id.ivAnim);
		mCloverAnimation = (AnimationDrawable) mIvAnimation.getDrawable();
		
	}

	/**
	 * Sets custom progress title
	 * 
	 * @param progressTitle
	 */
	public void setTitle(final String progressTitle) {
		if (mTvProgressTitle.getVisibility() == View.GONE) {
			mTvProgressTitle.setVisibility(View.VISIBLE);
			mTvProgressTitle.setText(progressTitle);
		}
	}

	/**
	 * Shows progress dialog with custom progress title
	 * 
	 * @param progressTitle
	 */
	public void show(final String progressTitle) {
		if (mTvProgressTitle.getVisibility() == View.GONE) {
			mTvProgressTitle.setVisibility(View.VISIBLE);
			mTvProgressTitle.setText(progressTitle);
		}
		HookUpProgressDialog.this.show();
	}
	
	@Override
	public void show() {
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {

			public void run() {

				mCloverAnimation.start();

			}
		}, 10);
		super.show();
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		Log.v("Dialog focus change", "" + hasFocus);
		if (!hasFocus) this.dismiss();
		super.onWindowFocusChanged(hasFocus);
	}
	
}
