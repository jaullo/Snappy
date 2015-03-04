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

package com.snappy;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.snappy.couchdb.CouchDB;
import com.snappy.couchdb.ResultListener;
import com.snappy.couchdb.model.User;
import com.snappy.dialog.HookUpDialog;
import com.snappy.extendables.SideBarActivity;
import com.snappy.management.UsersManagement;

/**
 * SettingsActivity
 * 
 * Has options for clearing memory cache, changing password and setting passcode protect.
 */

public class SettingsActivity extends SideBarActivity {

	private EditText mEtServerUrl;
	private ToggleButton mBtnPasscodeProtect;
	private Button mBtnClearCache;
	private HookUpDialog mClearCacheDialog;

	private static final int PASSCODE_PROTECT_ON = 1001;
	private static final int PASSCODE_PROTECT_OFF = 1002;

	private Button mBtnChangePassword;
	private ToggleButton mBtnShowPassword;
	private EditText mEtPassword;
//	private HookUpPasswordDialog mPasswordDialog;
	private HookUpDialog mSendPasswordDialog;
	private String mPassword;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		setSideBar(getString(R.string.SETTINGS));
		initialization();
	}

	private class SendPasswordListener implements ResultListener<Void> {
		@Override
		public void onResultsSucceded(Void result) {
		}
		@Override
		public void onResultsFail() {			
		}
	}
	
	private void initialization() {

		getLoginUserAsync();
		
		mSendPasswordDialog = new HookUpDialog(this);
		mSendPasswordDialog.setOnButtonClickListener(HookUpDialog.BUTTON_OK,
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						CouchDB.sendPassword(SpikaApp.getPreferences().getUserEmail(), new SendPasswordListener() , SettingsActivity.this, true);
						mSendPasswordDialog.dismiss();
					}
				});
		mSendPasswordDialog.setOnButtonClickListener(
				HookUpDialog.BUTTON_CANCEL, new OnClickListener() {

					@Override
					public void onClick(View v) {
						mSendPasswordDialog.dismiss();

					}
				});
		
//		mPasswordDialog = new HookUpPasswordDialog(this, false);

		mBtnChangePassword = (Button) findViewById(R.id.btnChangePassword);
		mBtnChangePassword.setTypeface(SpikaApp.getTfMyriadProBold(),
				Typeface.BOLD);
		mBtnChangePassword.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
//				mPasswordDialog.show(mPassword);
				mSendPasswordDialog.show(getString(R.string.confirm_email)
						+ "\n" + SpikaApp.getPreferences().getUserEmail());

			}
		});

		mEtPassword = (EditText) findViewById(R.id.etPassword);
		mEtPassword.setTypeface(SpikaApp.getTfMyriadPro());
		mEtPassword.setInputType(InputType.TYPE_NULL);
		mEtPassword.setTransformationMethod(PasswordTransformationMethod
				.getInstance());
		mEtPassword.setText(mPassword);

		mBtnShowPassword = (ToggleButton) findViewById(R.id.btnShowPassword);
		mBtnShowPassword
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked) {
							mEtPassword.setTransformationMethod(null);
						} else {
							mEtPassword
									.setTransformationMethod(PasswordTransformationMethod
											.getInstance());
						}

					}
				});
		mBtnShowPassword.setChecked(false);

		mClearCacheDialog = new HookUpDialog(this);
		mClearCacheDialog.setMessage(getString(R.string.clear_cache_message));
		mClearCacheDialog.setOnButtonClickListener(HookUpDialog.BUTTON_OK,
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						SpikaApp.clearCache();
						mClearCacheDialog.dismiss();

					}
				});
		mClearCacheDialog.setOnButtonClickListener(
				HookUpDialog.BUTTON_CANCEL, new OnClickListener() {

					@Override
					public void onClick(View v) {
						mClearCacheDialog.dismiss();

					}
				});

		mEtServerUrl = (EditText) findViewById(R.id.etServerUrl);
		mEtServerUrl.setTypeface(SpikaApp.getTfMyriadPro());
		mEtServerUrl.setText(CouchDB.getUrl());
		mBtnClearCache = (Button) findViewById(R.id.btnClearCache);
		mBtnClearCache.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mClearCacheDialog.show();

			}
		});

		mBtnPasscodeProtect = (ToggleButton) findViewById(R.id.btnPasscodeProtect);
		if (SpikaApp.getPreferences().getPasscodeProtect() == true) {
			mBtnPasscodeProtect.setChecked(true);
		} else {
			mBtnPasscodeProtect.setChecked(false);
		}
		
		mBtnPasscodeProtect.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent passcodeIntent = new Intent(
						SettingsActivity.this, PasscodeActivity.class);
				if (SpikaApp.getPreferences().getPasscodeProtect() == true) {
					
					startActivityForResult(passcodeIntent,
							PASSCODE_PROTECT_OFF);
				} else {
					SpikaApp.getPreferences().setPasscode("");
					startActivityForResult(passcodeIntent,
							PASSCODE_PROTECT_ON);
				}
				
			}
		});

		mBtnPasscodeProtect
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked) {
							mBtnPasscodeProtect
									.setBackgroundResource(R.drawable.toggle_button_on);
							
						} else {
							mBtnPasscodeProtect
									.setBackgroundResource(R.drawable.toggle_button_off);
							
						}

					}
				});

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == PASSCODE_PROTECT_ON) {
				mBtnPasscodeProtect.setChecked(true);
				SpikaApp.getPreferences().setPasscodeProtect(true);
				Toast.makeText(this, R.string.settingsactivity_pass_on, Toast.LENGTH_SHORT)
						.show();
			}
			if (requestCode == PASSCODE_PROTECT_OFF) {
				mBtnPasscodeProtect.setChecked(false);
				SpikaApp.getPreferences().setPasscodeProtect(false);
				Toast.makeText(this, R.string.settingsactivity_pass_off, Toast.LENGTH_SHORT)
						.show();
			}
		} else if (resultCode == Activity.RESULT_CANCELED) {
			if (SpikaApp.getPreferences().getPasscodeProtect() == true) {
				mBtnPasscodeProtect.setChecked(true);
			} else {
				mBtnPasscodeProtect.setChecked(false);
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void setObjectsNull() {
		mClearCacheDialog = null;
		super.setObjectsNull();
	}

	public void setNewPassword(String newPassword) {
		hideKeyboard();
		updatePasswordAsync(newPassword);
	}

	private void updatePasswordAsync (String newPassword) {
		String currentPassword = SpikaApp.getPreferences().getUserPassword();
		SpikaApp.getPreferences().setUserPassword(newPassword);
		CouchDB.updateUserAsync(UsersManagement.getLoginUser(), new UpdatePasswordFinish(currentPassword, newPassword), SettingsActivity.this, true);
	}
	
	private class UpdatePasswordFinish implements ResultListener<Boolean> {
		
		String currentPassword;
		String newPassword;
		
		public UpdatePasswordFinish(String currentPassword, String newPassword) {
			this.currentPassword = currentPassword;
			this.newPassword = newPassword;
		}
		
		@Override
		public void onResultsSucceded(Boolean result) {
			if (result) {
				/* update successful */

				mPassword = newPassword;
				mEtPassword.setText(newPassword);

				getLoginUserAsync();

				Toast.makeText(SettingsActivity.this, R.string.settingsactivity_conf_saved,
						Toast.LENGTH_SHORT).show();

			} else {
				/*
				 * something went wrong with update profile, returning logged in
				 * user to state before update
				 */

				Toast.makeText(SettingsActivity.this, R.string.settingsactivity_conf_error,
						Toast.LENGTH_SHORT).show();

				SpikaApp.getPreferences().setUserPassword(currentPassword);
				mPassword = currentPassword;
			}
		}

		@Override
		public void onResultsFail() {
			SpikaApp.getPreferences().setUserPassword(currentPassword);
			mPassword = currentPassword;
		}
	}

	private void getLoginUserAsync () {
		CouchDB.findUserByIdAsync(UsersManagement.getLoginUser().getId(), new GetLoginUserFinish(), SettingsActivity.this, true);
	}
	
	private class GetLoginUserFinish implements ResultListener<User> {
		@Override
		public void onResultsSucceded(User result) {
			UsersManagement.setLoginUser(result);
			mPassword = SpikaApp.getPreferences().getUserPassword();
		}
		@Override
		public void onResultsFail() {			
		}
	}
}
