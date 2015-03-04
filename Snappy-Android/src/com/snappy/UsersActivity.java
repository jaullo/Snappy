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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Intent;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;


import com.snappy.adapters.UsersAdapter;
import com.snappy.couchdb.CouchDB;
import com.snappy.couchdb.ResultListener;
import com.snappy.couchdb.model.Notification;
import com.snappy.couchdb.model.RecentActivity;
import com.snappy.couchdb.model.User;
import com.snappy.couchdb.model.UserSearch;
import com.snappy.extendables.SubMenuActivity;
import com.snappy.management.UsersManagement;
import com.snappy.utils.Const;
import com.snappy.view.GenderButton;
import com.snappy.view.GenderButton.ButtonType;

/**
 * UsersActivity
 * 
 * Shows a list of users that are added to login user's contacts by default; also contains a submenu with
 * options for searching and exploring users.
 */

public class UsersActivity extends SubMenuActivity {

	private ListView mLvUsers;
	private List<User> mUsers;
	private List<Notification> mUserNotifications;
	private UsersAdapter mUserListAdapter;

	private RelativeLayout mLayoutUserSearch;
	private RelativeLayout mLayoutUserExplore;

	private RelativeLayout mRlMyContacts;
	private RelativeLayout mRlSearch;

	private String mSearchGender;
	private RelativeLayout mRlExplore;
	private TextView mTvNoUsers;
	private TextView mTvFromAge;
	private TextView mTvToAge;

	private int mFullWidth;
	private boolean firstMeasure = true;
	private static final int FROM_AGE = 0;
	private static final int TO_AGE = 100;
	private boolean mOnlineUsersChecked;

	public static final int REQUEST_UPDATE_USERS = 8;
	
	private static final String ALL_USERS = "all_users";
	private static final String CONTACTS = "contacts";
	private static final String SEARCH_USERS = "search_users";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_users);
		setSideBar(getString(R.string.USERS));
		initialization();
		initUserSearch();
		initUserExplore();

		showTutorial(getString(R.string.tutorial_users));
	}

	@Override
	protected void enableViews() {
		super.enableViews();
		mLvUsers.setEnabled(true);
	}

	@Override
	protected void disableViews() {
		super.disableViews();
		mLvUsers.setEnabled(false);
	}

	private void initialization() {
		super.setSubMenu();
		mOnlineUsersChecked = false;
		mLvUsers = (ListView) findViewById(R.id.lvUsers);
		mLayoutUserSearch = (RelativeLayout) findViewById(R.id.rlSearchUsers);
		mLayoutUserExplore = (RelativeLayout) findViewById(R.id.rlExploreUsers);

		mRlMyContacts = (RelativeLayout) findViewById(R.id.rlMyContacts);
		mRlMyContacts.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mTvNoUsers.setVisibility(View.GONE);
				mTvTitle.setText(getString(R.string.MY_CONTACTS));
				closeSubMenu();
				getUserContactsAsync();
				mLayoutUserSearch.setVisibility(View.GONE);
				mLayoutUserExplore.setVisibility(View.GONE);

			}
		});
		mRlSearch = (RelativeLayout) findViewById(R.id.rlSearch);
		mRlSearch.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mTvNoUsers.setVisibility(View.GONE);
				mTvTitle.setText(getString(R.string.USERS));
				closeSubMenu();
				mLayoutUserExplore.setVisibility(View.GONE);
				mLayoutUserSearch.setVisibility(View.VISIBLE);
				
				clearListView();
			}
		});
		mRlExplore = (RelativeLayout) findViewById(R.id.rlExplore);
		mRlExplore.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mTvNoUsers.setVisibility(View.GONE);
				mTvTitle.setText(getString(R.string.USERS));
				closeSubMenu();
				mLayoutUserSearch.setVisibility(View.GONE);
				mLayoutUserExplore.setVisibility(View.VISIBLE);

				clearListView();
			}
		});

		mTvNoUsers = (TextView) findViewById(R.id.tvNoUsers);
		mTvNoUsers.setVisibility(View.GONE);

		mTvTitle.setText(getString(R.string.MY_CONTACTS));
		closeSubMenu();
		getUserContactsAsync();
		mLayoutUserSearch.setVisibility(View.GONE);
		mLayoutUserExplore.setVisibility(View.GONE);
	}

	private void initUserSearch() {

		final EditText etSearchName = (EditText) findViewById(R.id.etSearchName);
		etSearchName.setTypeface(SpikaApp.getTfMyriadPro());
		final Button btnSearch = (Button) findViewById(R.id.btnSearch);
		btnSearch.setTypeface(SpikaApp.getTfMyriadPro(), Typeface.BOLD);
		btnSearch.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				UserSearch userSearch = new UserSearch();

				userSearch.setName(etSearchName.getText().toString());

				etSearchName.setText("");
				searchUsersAsync(userSearch);
			}
		});

		etSearchName.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {

					UserSearch userSearch = new UserSearch();

					userSearch.setName(etSearchName.getText().toString());

					etSearchName.setText("");

					hideKeyboard();
					searchUsersAsync(userSearch);
					return true;
				}
				return false;
			}
		});

	}

	private void initUserExplore() {

		final GenderButton btnMale = (GenderButton) findViewById(R.id.btnMale);
		btnMale.setType(ButtonType.LEFT);
		btnMale.setChecked(false);
		final GenderButton btnFemale = (GenderButton) findViewById(R.id.btnFemale);
		btnFemale.setType(ButtonType.MIDDLE);
		btnFemale.setChecked(false);
		final GenderButton btnAll = (GenderButton) findViewById(R.id.btnAll);
		btnAll.setType(ButtonType.RIGHT);
		btnAll.setChecked(true);

		btnMale.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				btnMale.setChecked(true);
				mSearchGender = Const.MALE;
				btnFemale.setChecked(false);
				btnAll.setChecked(false);

			}
		});
		btnFemale.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				btnMale.setChecked(false);
				mSearchGender = Const.FEMALE;
				btnFemale.setChecked(true);
				btnAll.setChecked(false);

			}
		});
		btnAll.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				btnMale.setChecked(false);
				mSearchGender = null;
				btnFemale.setChecked(false);
				btnAll.setChecked(true);

			}
		});

		final Button btnExplore = (Button) findViewById(R.id.btnUserExplore);
		btnExplore.setTypeface(SpikaApp.getTfMyriadPro(), Typeface.BOLD);
		btnExplore.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				UserSearch userSearch = new UserSearch();
				userSearch.setGender(mSearchGender);

				if (mTvFromAge.getText().toString()
						.equals(String.valueOf(FROM_AGE))) {
					userSearch.setFromAge(null);
				} else {
					userSearch.setFromAge(mTvFromAge.getText().toString());
				}
				if (mTvToAge.getText().toString()
						.equals(String.valueOf(TO_AGE))) {
					userSearch.setToAge(null);
				} else {
					userSearch.setToAge(mTvToAge.getText().toString());
				}
				if (mOnlineUsersChecked) {
					userSearch.setOnlineStatus(Const.ONLINE);
				} else {
					userSearch.setOnlineStatus("");
				}

				searchUsersAsync(userSearch);
			}
		});

		firstMeasure = true;
		final View seekBar = (View) findViewById(R.id.seekBar);
		final RelativeLayout rlSeekBar = (RelativeLayout) findViewById(R.id.rlSeekBar);

		final int MARGIN_SIZE = (int) getResources().getDimension(
				R.dimen.seekBar_margin) + 1;

		mTvFromAge = (TextView) findViewById(R.id.tvFromAge);
		mTvFromAge.setText(String.valueOf(FROM_AGE));
		mTvToAge = (TextView) findViewById(R.id.tvToAge);
		mTvToAge.setText(String.valueOf(TO_AGE));

		rlSeekBar.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {

				if (event.getAction() == MotionEvent.ACTION_MOVE
						|| event.getAction() == MotionEvent.ACTION_DOWN) {

					int xPointTouch = Math.round(event.getX());

					if (firstMeasure) {
						mFullWidth = seekBar.getWidth();
						firstMeasure = false;
					}

					final RelativeLayout.LayoutParams layoutParams = new LayoutParams(
							RelativeLayout.LayoutParams.WRAP_CONTENT,
							RelativeLayout.LayoutParams.MATCH_PARENT);
					layoutParams
							.addRule(RelativeLayout.RIGHT_OF, R.id.viewLeft);
					layoutParams
							.addRule(RelativeLayout.LEFT_OF, R.id.viewRight);

					RelativeLayout.LayoutParams currentParams = (RelativeLayout.LayoutParams) seekBar
							.getLayoutParams();

					Rect rectSeekBar = new Rect();
					seekBar.getLocalVisibleRect(rectSeekBar);
					int xPointLeft = MARGIN_SIZE + currentParams.leftMargin;
					int xPointRight = MARGIN_SIZE + mFullWidth
							- currentParams.rightMargin;

					int distanceLeft = Math.abs(xPointTouch - xPointLeft);
					int distanceRight = Math.abs(xPointTouch - xPointRight);

					int leftMargin = currentParams.leftMargin;
					int rightMargin = currentParams.rightMargin;

					if (distanceLeft < distanceRight) {
						leftMargin = 0;
						if (xPointTouch >= MARGIN_SIZE) {
							leftMargin = xPointTouch - MARGIN_SIZE;
						}
						if (xPointTouch >= MARGIN_SIZE + mFullWidth) {
							leftMargin = mFullWidth;
						}
						int fromAge = (int) Math.round((leftMargin * 1.0)
								/ mFullWidth * (TO_AGE - FROM_AGE));
						mTvFromAge.setText(String.valueOf(fromAge));
					} else {
						rightMargin = MARGIN_SIZE + mFullWidth - xPointTouch;
						if (xPointTouch >= MARGIN_SIZE + mFullWidth) {
							rightMargin = 0;
						}
						int toAge = (int) Math
								.round(((mFullWidth - rightMargin) * 1.0)
										/ mFullWidth * (TO_AGE - FROM_AGE));
						mTvToAge.setText(String.valueOf(toAge));
					}

					layoutParams.leftMargin = leftMargin;
					layoutParams.rightMargin = rightMargin;

					seekBar.setLayoutParams(layoutParams);
					seekBar.invalidate();

				}
				return true;
			}
		});

		final CheckBox checkBoxOnlineUsers = (CheckBox) findViewById(R.id.checkboxOnlineUsers);
		checkBoxOnlineUsers.setTypeface(SpikaApp.getTfMyriadPro());
	}

	public void onCheckboxClicked(View view) {
		// Is the view now checked?
		boolean checked = ((CheckBox) view).isChecked();

		// Check which checkbox was clicked
		switch (view.getId()) {
		case R.id.checkboxOnlineUsers:
			if (checked)
				mOnlineUsersChecked = true;
			else
				mOnlineUsersChecked = false;
			break;
		default:
			break;
		}
	}

	private void getUserContactsAsync () {
		CouchDB.findUserContactsAsync(UsersManagement.getLoginUser().getId(), new GetUserContactsFinish(), UsersActivity.this, true);
	}
	
	private class GetUserContactsFinish implements ResultListener<List<User>> {
		@Override
		public void onResultsSucceded(List<User> result) {
			if (result == null || result.size() == 0) {
				UsersActivity.this.showTutorialOnceAfterBoot(getString(R.string.tutorial_nocontact));
			}

			if (UsersManagement.getLoginUser().getActivitySummary() != null) {
				for (RecentActivity recentActivity : UsersManagement
						.getLoginUser().getActivitySummary()
						.getRecentActivityList()) {
					if (recentActivity.getTargetType().equals(Const.USER)) {
						mUserNotifications = recentActivity.getNotifications();
					}
				}
			}

			mUsers = (ArrayList<User>) result;

			if (mUsers.size() == 0) {
				mTvNoUsers.setVisibility(View.VISIBLE);
				mTvNoUsers.setText(getString(R.string.no_users_in_contacts));
			} else {
				mTvNoUsers.setVisibility(View.GONE);
			}

			// sorting users by name
			Collections.sort(mUsers, new Comparator<User>() {
				@Override
				public int compare(User lhs, User rhs) {
					return lhs.getName().compareToIgnoreCase(rhs.getName());
				}
			});

			if (mUserListAdapter == null) {
				mUserListAdapter = new UsersAdapter(UsersActivity.this, mUsers,
						mUserNotifications);
				mLvUsers.setAdapter(mUserListAdapter);
				mLvUsers.setOnItemClickListener(mUserListAdapter);
			} else {
				mUserListAdapter.setItems(mUsers, mUserNotifications);
			}
		}

		@Override
		public void onResultsFail() {
		}
	}

	private void clearListView() {
		mUserListAdapter = new UsersAdapter(UsersActivity.this,
				new ArrayList<User>(), new ArrayList<Notification>());
		mLvUsers.setAdapter(mUserListAdapter);
	}

	private void searchUsersAsync (UserSearch userSearch) {
		CouchDB.searchUsersAsync(userSearch, new SearchUsersFinish(), UsersActivity.this, true);
	}
	
	private class SearchUsersFinish implements ResultListener<List<User>> {

		@Override
		public void onResultsSucceded(List<User> result) {
			if (result != null) {
				if (UsersManagement.getLoginUser().getActivitySummary() != null) {
					for (RecentActivity recentActivity : UsersManagement
							.getLoginUser().getActivitySummary()
							.getRecentActivityList()) {
						if (recentActivity.getTargetType().equals(Const.USER)) {
							mUserNotifications = recentActivity
									.getNotifications();
						}
					}
				}
				mUsers = (ArrayList<User>) result;

				if (mUsers.size() == 0) {
					mTvNoUsers.setVisibility(View.VISIBLE);
					mTvNoUsers.setText(getString(R.string.no_users_found));
				} else {
					mTvNoUsers.setVisibility(View.GONE);
				}

				// sorting users by name
				Collections.sort(mUsers, new Comparator<User>() {
					@Override
					public int compare(User lhs, User rhs) {
						return lhs.getName().compareToIgnoreCase(rhs.getName());
					}
				});

				if (mUserListAdapter == null) {
					mUserListAdapter = new UsersAdapter(UsersActivity.this,
							mUsers, mUserNotifications);
					mLvUsers.setAdapter(mUserListAdapter);
					mLvUsers.setOnItemClickListener(mUserListAdapter);
				} else {
					mUserListAdapter.setItems(mUsers, mUserNotifications);
				}
			}
		}

		@Override
		public void onResultsFail() {			
		}	
	}

	@Override
	public void onBackPressed() {
		if (mLayoutUserSearch.getVisibility() == View.VISIBLE) {
			mLayoutUserSearch.setVisibility(View.GONE);
			getUserContactsAsync();
		} else if (mLayoutUserExplore.getVisibility() == View.VISIBLE) {
			mLayoutUserExplore.setVisibility(View.GONE);
			getUserContactsAsync();
		} else {
			super.onBackPressed();
		}
	}

	@Override
	protected void refreshActivitySummaryViews() {
		super.refreshActivitySummaryViews();

		if (UsersManagement.getLoginUser().getActivitySummary() != null) {
			for (RecentActivity recentActivity : UsersManagement.getLoginUser()
					.getActivitySummary().getRecentActivityList()) {
				if (recentActivity.getTargetType().equals(Const.USER)) {
					mUserNotifications = recentActivity.getNotifications();
				}
			}
		}

		if (mUserListAdapter != null) {
			mUserListAdapter.setItems(mUsers, mUserNotifications);
		}
	}

	@Override
	protected void setObjectsNull() {
		mLvUsers.setAdapter(null);
		mLvUsers = null;
		mLayoutUserSearch = null;
		mUserListAdapter = null;
		super.setObjectsNull();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// Check which request we're responding to
		if (requestCode == REQUEST_UPDATE_USERS) {
			// Make sure the request was successful
			if (resultCode == RESULT_OK) {
				mTvNoUsers.setVisibility(View.GONE);
				mTvTitle.setText(getString(R.string.MY_CONTACTS));
				getUserContactsAsync();
				mLayoutUserSearch.setVisibility(View.GONE);
				mLayoutUserExplore.setVisibility(View.GONE);
			}
		}
	}

}