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

import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.snappy.couchdb.CouchDB;
import com.snappy.couchdb.ResultListener;
import com.snappy.couchdb.model.User;
import com.snappy.dialog.HookUpAlertDialog;
import com.snappy.extendables.SpikaActivity;
import com.snappy.lazy.ImageLoader;
import com.snappy.management.SettingsManager;
import com.snappy.management.UsersManagement;
import com.snappy.utils.Const;
import com.snappy.utils.Utils;

/**
 * UserProfileActivity
 * 
 * Shows profile of a user; has an option for login user to remove/add this user
 * to favorites.
 */

public class UserProfileActivity extends SpikaActivity {

	private ImageView mIvUserImage;
	private TextView mTvUserName;
	private TextView mTvUserLastLogin;
	private TextView mTvUserAbout;
	private TextView mTvUserBirthday;
	private TextView mTvUserGender;
	private Button mBtnContacts;
	private Button mBtnBack;
	private ProgressBar mPbLoading;
	private User mUser;
	private RelativeLayout mRlAbout;
	private RelativeLayout mRlBirthday;
	private RelativeLayout mRlGender;
	private Spinner mSpinnerStatus;
	private String mUserOnlineStatus;
	private Button mBtnOpenWall;
	// private boolean mIsUpdated = false;

	private static final int ADD = 1000;
	private static final int REMOVE = 1001;

	private static final int NO_BIRTHDAY = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setResult(RESULT_CANCELED);
		setContentView(R.layout.activity_user_profile);
		Initialization();
		OnClickListeners();

		showTutorial(getString(R.string.tutorial_userprofile));
	}

	@Override
	protected void setObjectsNull() {
		unbindDrawables(findViewById(R.id.ivUserImage));
		mRlBirthday = null;
		mRlAbout = null;
		mRlGender = null;
		super.setObjectsNull();
	}

	private void Initialization() {

		mIvUserImage = (ImageView) findViewById(R.id.ivProfileImage);
		mTvUserName = (TextView) findViewById(R.id.tvUserName);
		mTvUserLastLogin = (TextView) findViewById(R.id.tvUserLastLogin);
		mTvUserAbout = (TextView) findViewById(R.id.tvUserAbout);
		mTvUserBirthday = (TextView) findViewById(R.id.tvUserBirthday);
		mTvUserGender = (TextView) findViewById(R.id.tvUserGender);
		mBtnContacts = (Button) findViewById(R.id.btnContacts);
		mBtnContacts.setTypeface(SpikaApp.getTfMyriadProBold(), Typeface.BOLD);
		mBtnBack = (Button) findViewById(R.id.btnBack);
		mBtnBack.setTypeface(SpikaApp.getTfMyriadProBold(), Typeface.BOLD);
		mPbLoading = (ProgressBar) findViewById(R.id.pbLoadingForImage);
		mRlAbout = (RelativeLayout) findViewById(R.id.rlAbout);
		mRlGender = (RelativeLayout) findViewById(R.id.rlGender);
		mRlBirthday = (RelativeLayout) findViewById(R.id.rlBirthday);
		mBtnOpenWall = (Button) findViewById(R.id.btnOpenWall);
		mBtnOpenWall.setTypeface(SpikaApp.getTfMyriadPro());

		mBtnOpenWall.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				openUserWall(mUser);

			}
		});

		mSpinnerStatus = (Spinner) findViewById(R.id.spinnerStatus);
		mSpinnerStatus.setClickable(false);

		// If opened from link
		if (getIntent().getBooleanExtra(Const.USER_URI_INTENT, false)) {
			String userName = getIntent().getStringExtra(Const.USER_URI_NAME);
			findUserByName(userName);

			// If opened from another activity
		} else {
			String userId = getIntent().getStringExtra("user_id");
			if (getIntent().getStringExtra("user_id") != null) {
				getIntent().removeExtra("user_id");
			} else {
				userId = UsersManagement.getToUser().getId();
			}
			findUserByIdAsync(userId);
		}

		final ArrayAdapter<String> onlineStatusAdapter = new ArrayAdapter<String>(
				this, android.R.layout.simple_spinner_item, getResources()
						.getStringArray(R.array.online_status)) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View v = super.getView(position, convertView, parent);
				((TextView) v).setTextSize(16);
				((TextView) v).setTypeface(SpikaApp.getTfMyriadPro());

				Drawable statusIcon = null;

				switch (position) {
				case 0:
					statusIcon = getContext().getResources().getDrawable(
							R.drawable.user_online_icon);
					((TextView) v).setTextColor(getResources().getColor(
							R.color.hookup_positive));
					break;
				case 1:
					statusIcon = getContext().getResources().getDrawable(
							R.drawable.user_away_icon);
					((TextView) v).setTextColor(getResources().getColor(
							R.color.hookup_positive));
					break;
				case 2:
					statusIcon = getContext().getResources().getDrawable(
							R.drawable.user_busy_icon);
					((TextView) v).setTextColor(getResources().getColor(
							R.color.hookup_positive));
					break;
				case 3:
					statusIcon = getContext().getResources().getDrawable(
							R.drawable.user_offline_icon);
					((TextView) v).setTextColor(getResources().getColor(
							R.color.hookup_positive));
					break;
				default:
					((TextView) v).setTextColor(getResources().getColor(
							R.color.hookup_positive));
					break;
				}

				((TextView) v).setCompoundDrawablePadding(10);
				((TextView) v).setCompoundDrawablesWithIntrinsicBounds(
						statusIcon, null, null, null);

				return v;
			}

		};
		mSpinnerStatus.setAdapter(onlineStatusAdapter);

	}

	private void setUserProfile() {

		mUserOnlineStatus = mUser.getOnlineStatus();
		if (mUserOnlineStatus != null && !"".equals(mUserOnlineStatus)) {
			if (mUserOnlineStatus.equals(Const.ONLINE)) {
				mSpinnerStatus.setSelection(0);
			}
			if (mUserOnlineStatus.equals(Const.AWAY)) {
				mSpinnerStatus.setSelection(1);
			}
			if (mUserOnlineStatus.equals(Const.BUSY)) {
				mSpinnerStatus.setSelection(2);
			}
			if (mUserOnlineStatus.equals(Const.OFFLINE)) {
				mSpinnerStatus.setSelection(3);
			}
		} else {
			mSpinnerStatus.setSelection(3);
		}

		Utils.displayImage(mUser.getAvatarFileId(), mIvUserImage, mPbLoading,
				ImageLoader.LARGE, R.drawable.user_stub_large, false);

		mTvUserName.setText(mUser.getName());

		if (mUser.getLastLogin() != 0) {
			mTvUserLastLogin.setText(Utils.getFormattedDateTime(mUser
					.getLastLogin()));
		}
		if (mUser.getAbout() != null && !"".equals(mUser.getAbout())) {
			mRlAbout.setVisibility(View.VISIBLE);
			mTvUserAbout.setText(mUser.getAbout());
		} else {
			mRlAbout.setVisibility(View.GONE);
		}

		if (mUser.getBirthday() == NO_BIRTHDAY) {
			mRlBirthday.setVisibility(View.GONE);
		} else {
			mRlBirthday.setVisibility(View.VISIBLE);
			String stringBirthday = DateFormat.format(
					getString(R.string.hookup_date_format),
					mUser.getBirthday() * 1000).toString();
			mTvUserBirthday.setText(stringBirthday);
		}

		if (mUser.getGender() != null && !"".equals(mUser.getGender())) {
			mRlGender.setVisibility(View.VISIBLE);
			if (mUser.getGender().equalsIgnoreCase(Const.MALE)) {
				mTvUserGender.setText(R.string.male);
			}
			if (mUser.getGender().equalsIgnoreCase(Const.FEMALE)) {
				mTvUserGender.setText(R.string.female);
			}
		} else {
			mRlGender.setVisibility(View.GONE);
		}

		if (UsersManagement.getLoginUser().isInContacts(mUser)) {
			setButtonContacts(REMOVE);
		} else {
			setButtonContacts(ADD);
		}
	}

	private void OnClickListeners() {

		final HookUpAlertDialog alertDialog = new HookUpAlertDialog(this);

		mBtnContacts.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (UsersManagement.getLoginUser().isInContacts(mUser)) {
					
					updateContactAsync(REMOVE);

				} else {

					if (UsersManagement.getLoginUser().canAddContact()) {
						updateContactAsync(ADD);
					} else {
						alertDialog
								.show(getString(R.string.max_contacts_alert));
					}

				}
			}
		});

		mBtnBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				UserProfileActivity.this.finish();
			}
		});
	}

	private void setButtonContacts(int type) {

		mBtnContacts.setVisibility(View.VISIBLE);

		switch (type) {
		case ADD:
			mBtnContacts.setText(getString(R.string.ADD_CONTACT));
			mBtnContacts.setBackgroundResource(R.drawable.positive_selector);
			mBtnContacts.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
			break;
		case REMOVE:
			mBtnContacts.setText(getString(R.string.REMOVE_CONTACT));
			mBtnContacts.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
			mBtnContacts.setBackgroundResource(R.drawable.alert_selector);
			break;
		default:
			break;
		}
	}

	private void findUserByIdAsync (String id) {
		CouchDB.findUserByIdAsync(id, new FindUserByIdFinish(), UserProfileActivity.this, true);
	}
	
	private class FindUserByIdFinish implements ResultListener<User> {
		@Override
		public void onResultsSucceded(User result) {
			if (result != null) {
				mUser = result;
			}
			setUserProfile();
		}
		@Override
		public void onResultsFail() {
		}
	}

	private void findUserByName (String username) {
		CouchDB.findUserByNameAsync(username, new FindUserByNameFinish(), UserProfileActivity.this, true);
	}
	
	private class FindUserByNameFinish implements ResultListener<User> {
		@Override
		public void onResultsSucceded(User result) {
			if (result != null) {
				mUser = result;
			}
			setUserProfile();
		}

		@Override
		public void onResultsFail() {			
		}
	}

	private void updateContactAsync (int updateType) {
		if (updateType == ADD) {
			CouchDB.addUserContactAsync(mUser.getId(), new UpdateContactFinish(updateType), UserProfileActivity.this, true);
		} else if (updateType == REMOVE) {
			CouchDB.removeUserContactAsync(mUser.getId(), new UpdateContactFinish(updateType), UserProfileActivity.this, true);
		}
	}

	private class UpdateContactFinish implements ResultListener<Boolean> {

		int updateType;
		
		public UpdateContactFinish (int updateType) {
			this.updateType = updateType;
		}
		
		@Override
		public void onResultsSucceded(Boolean result) {
			if (result) {
				if (updateType == ADD) {

					setButtonContacts(REMOVE);
				}
				if (updateType == REMOVE) {

					setButtonContacts(ADD);
				}
				UserProfileActivity.this.setResult(RESULT_OK);
			} else {
				Toast.makeText(UserProfileActivity.this,
						getString(R.string.failed_to_update_contacts),
						Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		public void onResultsFail() {
		}
	}

	private void openUserWall(User user) {

		UsersManagement.setToUser(user);
		UsersManagement.setToGroup(null);

		SettingsManager.ResetSettings();
		if (WallActivity.gCurrentMessages != null) {
			WallActivity.gCurrentMessages.clear();
		}

		WallActivity.gIsRefreshUserProfile = true;

		UserProfileActivity.this.startActivity(new Intent(
				UserProfileActivity.this, WallActivity.class));

	}

}
