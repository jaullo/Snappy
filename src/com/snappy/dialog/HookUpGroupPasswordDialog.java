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

import android.R.integer;
import android.app.Activity;
import android.app.Dialog;
import android.graphics.Typeface;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.snappy.GroupProfileActivity;
import com.snappy.R;
import com.snappy.SpikaApp;
import com.snappy.management.FileManagement;

/**
 * HookUpGroupPasswordDialog
 * 
 * Shows dialog and lets user type password and checks if typed password is correct.
 */

public class HookUpGroupPasswordDialog extends Dialog {

	private EditText mEtGroupPassword;
	private Button mBtnOk;
	private Button mBtnCancel;
	private String mCurrentPassword;
	private String mGroupId;
	private boolean mIsSubscribe;
    private Activity mActivity;
	private static final String WRONG_PASSWORD = "";
	private static final String PASSWORD_SUCCESS = Integer.toString(R.string.hookup_password_success);

	public HookUpGroupPasswordDialog(final Activity activity) {
		super(activity, R.style.Theme_Transparent);

        mActivity = activity;

		setContentView(R.layout.group_password_dialog);

		mEtGroupPassword = (EditText) findViewById(R.id.etGroupPassword);
		mEtGroupPassword.setTypeface(SpikaApp.getTfMyriadPro());

		mBtnOk = (Button) findViewById(R.id.btnOk);
		mBtnOk.setTypeface(SpikaApp.getTfMyriadProBold(), Typeface.BOLD);
		mBtnOk.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				String passwordsResult = checkPasswords();
				if (passwordsResult.equals(PASSWORD_SUCCESS)) {

					if (activity instanceof GroupProfileActivity) {
						if (mIsSubscribe) {
							((GroupProfileActivity) activity).addToFavoritesAsync(mGroupId);
						} else {
							((GroupProfileActivity) activity).redirect();
						}
						HookUpGroupPasswordDialog.this.dismiss();
					}

				} else {
					Toast.makeText(activity, passwordsResult,
							Toast.LENGTH_SHORT).show();
				}

			}
		});
		mBtnCancel = (Button) this.findViewById(R.id.btnCancel);
		mBtnCancel.setTypeface(SpikaApp.getTfMyriadProBold(), Typeface.BOLD);
		mBtnCancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				HookUpGroupPasswordDialog.this.dismiss();

			}
		});

	}

	@Override
	public void dismiss() {
		mEtGroupPassword.setText(null);
		super.dismiss();
	}

	private String checkPasswords() {
		String currentPass = FileManagement.md5(mEtGroupPassword.getText().toString());
		if (!currentPass.equals(mCurrentPassword)) {
			return mActivity.getString(R.string.wrongpassword);
		}
		SpikaApp.getPreferences().savePasswordForGroup(mGroupId, mCurrentPassword);
		return PASSWORD_SUCCESS;
	}

	
	
	public void show(String groupId, String currentPassword, boolean isSubscribe) {
		mCurrentPassword = currentPassword;
		mGroupId = groupId;
		mIsSubscribe = isSubscribe;
		super.show();
	}

	public void show(String groupId, String currentPassword) {
		show(groupId, currentPassword, false);
	}
}
