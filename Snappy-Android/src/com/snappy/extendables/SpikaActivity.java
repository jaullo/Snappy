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

package com.snappy.extendables;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;


import com.snappy.GCMIntentService;
import com.snappy.PasscodeActivity;
import com.snappy.R;
import com.snappy.SpikaApp;
import com.snappy.WallActivity;
import com.snappy.couchdb.Command;
import com.snappy.couchdb.CouchDB;
import com.snappy.couchdb.ResultListener;
import com.snappy.couchdb.SpikaAsyncTask;
import com.snappy.couchdb.SpikaException;
import com.snappy.couchdb.SpikaForbiddenException;
import com.snappy.couchdb.model.ActivitySummary;
import com.snappy.couchdb.model.Group;
import com.snappy.couchdb.model.User;
import com.snappy.dialog.HookUpProgressDialog;
import com.snappy.dialog.PushNotification;
import com.snappy.dialog.Tutorial;
import com.snappy.management.ConnectionChangeReceiver;
import com.snappy.management.LogoutReceiver;
//import com.snappy.management.LogoutReceiver;
import com.snappy.management.UsersManagement;
import com.snappy.utils.Const;
import com.snappy.utils.Utils;

/**
 * SpikaActivity
 * 
 * HookUp base Activity, registers receivers, handles push notifications, connection changes and logout.
 */

public class SpikaActivity extends Activity {

	protected RelativeLayout mRlNoInternetNotification;
	protected RelativeLayout mRlPushNotification;
	protected HookUpProgressDialog mProgressDialog;
	private TranslateAnimation mSlideFromTop;
	private TranslateAnimation mSlideOutTop;
	private final IntentFilter mPushFilter = new IntentFilter(
			GCMIntentService.PUSH);
	private final IntentFilter mConnectionChangeFilter = new IntentFilter(
			ConnectionChangeReceiver.INTERNET_CONNECTION_CHANGE);

	private boolean tutorialShowed = false;
	
	private IntentFilter intentFilter = new IntentFilter(LogoutReceiver.LOGOUT);
	private LogoutReceiver logoutRec = new LogoutReceiver();
	
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		moveTaskToBack(false);
		
		mSlideFromTop = SpikaApp.getSlideFromTop();
		mSlideOutTop = SpikaApp.getSlideOutTop();
		mSlideFromTop.setAnimationListener(new AnimationListener() {

			public void onAnimationStart(Animation animation) {
				mRlNoInternetNotification.setVisibility(View.VISIBLE);
			}

			public void onAnimationRepeat(Animation animation) {
			}

			public void onAnimationEnd(Animation animation) {
			}
		});
		mSlideOutTop.setAnimationListener(new AnimationListener() {

			public void onAnimationStart(Animation animation) {
			}

			public void onAnimationRepeat(Animation animation) {
			}

			public void onAnimationEnd(Animation animation) {
				mRlNoInternetNotification.setVisibility(View.GONE);
			}
		});
		
		// Logout finish activity
		this.registerReceiver(logoutRec, intentFilter);
	};

	@Override
	protected void onStart() {
		super.onStart();
		handlePasscode();
	}

	protected void handlePasscode() {
		if (SpikaApp.gOpenFromBackground) {
			SpikaApp.gOpenFromBackground = false;

			if (getIntent().getBooleanExtra(Const.SIGN_IN, false) == false) {
				if (SpikaApp.getPreferences().getPasscodeProtect() == true) {
					Intent passcode = new Intent(SpikaActivity.this,
							PasscodeActivity.class);
					passcode.putExtra("protect", true);
					startActivity(passcode);
				}
			} else {
				getIntent().removeExtra(Const.SIGN_IN);
			}

		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		mRlNoInternetNotification = (RelativeLayout) findViewById(R.id.rlNoInternetNotification);
		mRlPushNotification = (RelativeLayout) findViewById(R.id.rlPushNotification);

		SpikaApp.getLocalBroadcastManager().registerReceiver(mPushReceiver,
				mPushFilter);

		SpikaApp.getLocalBroadcastManager().registerReceiver(
				mConnectionChangeReceiver, mConnectionChangeFilter);
		
		mProgressDialog = new HookUpProgressDialog(this);

		checkInternetConnection();

	}

	@Override
	protected void onPause() {
		super.onPause();
		SpikaApp.getLocalBroadcastManager().unregisterReceiver(mPushReceiver);
	}

	@Override
	protected void onDestroy() {
		setObjectsNull();
		this.unregisterReceiver(logoutRec);
		super.onDestroy();
	}

	@Override
	protected void onStop() {
		super.onStop();
		checkIfAppIsInForeground();
		if (mProgressDialog != null) {
			if (mProgressDialog.isShowing())
				mProgressDialog.dismiss();
		}
	}

	protected void checkIfAppIsInForeground() {
		try {
			boolean appIsInForeground = new SpikaApp.ForegroundCheckAsync()
					.execute(getApplicationContext()).get();
			if (!appIsInForeground) {
				SpikaApp.gOpenFromBackground = true;
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

	private BroadcastReceiver mPushReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			handlePushNotification(intent);
		}
	};

	private void handlePushNotification(Intent intent) {
		getActivitySummary();
		new SpikaAsyncTask<Void, Void, PushNotificationData>(new HandlePushNotification(intent), new HandlePushNotificationFinish(), SpikaActivity.this, false).execute();
	}
	
	protected class PushNotificationData {
		
		public String message;
		public User fromUser;
		public Group fromGroup;
		public String fromType;
		public String fromUserId;
		public String fromGroupId;
		
		public PushNotificationData(String message, User fromUser, Group fromGroup, String fromType, String fromUserId, String fromGroupId) {
			this.message = message;
			this.fromUser = fromUser;
			this.fromGroup = fromGroup;
			this.fromType = fromType;
			this.fromUserId = fromUserId;
			this.fromGroupId = fromGroupId;
		}
	}
	
	private class HandlePushNotification implements Command<PushNotificationData> {

		Intent intent;
		
		public HandlePushNotification (Intent intent) {
			this.intent = intent;
		}
		
		@Override
		public PushNotificationData execute() throws JSONException, IOException, SpikaException, IllegalStateException, SpikaForbiddenException {
			
			String message = intent.getStringExtra(Const.PUSH_MESSAGE);
			String fromUserId = intent.getStringExtra(Const.PUSH_FROM_USER_ID);
			String fromType = intent.getStringExtra(Const.PUSH_FROM_TYPE);
			String fromGroupId = intent.getStringExtra(Const.PUSH_FROM_GROUP_ID);
			String fromUserName = intent.getStringExtra("fromUserName");
			String finalMessage = null;
			
			User fromUser = null;
			Group fromGroup = null;
			
			if (mRlPushNotification != null) {

				fromUser = CouchDB.findUserById(fromUserId);
				
				if (fromType.equals(Const.PUSH_TYPE_GROUP)) {
					fromGroup = CouchDB.findGroupById(fromGroupId);
					finalMessage = fromUserName + " " + SpikaApp.getContext().getString(R.string.push_new_message_message_group_upper);
				}
				else
				{
					finalMessage = SpikaApp.getContext().getString(R.string.push_new_message_message_upper);
				}
				
									
				return new PushNotificationData(finalMessage, fromUser, fromGroup, fromType, fromUserId, fromGroupId);
			}
			return null;
		}
	}
	
	private class HandlePushNotificationFinish implements ResultListener<PushNotificationData> {
		
		@Override
		public void onResultsSucceded(PushNotificationData result) {
			
			if (result == null) return;
			
			if (result.fromType.equals(Const.PUSH_TYPE_GROUP)) {
				if (UsersManagement.getToGroup() != null) {
					boolean isGroupWallOpened = result.fromGroupId
							.equals(UsersManagement.getToGroup().getId())
							&& WallActivity.gIsVisible;
					if (isGroupWallOpened) {
						refreshWallMessages();
						return;
					}
				}
			}
			if (result.fromType.equals(Const.PUSH_TYPE_USER)) {

				if (UsersManagement.getToUser() != null) {

					boolean isUserWallOpened = result.fromUserId
							.equals(UsersManagement.getToUser().getId())
							&& WallActivity.gIsVisible;
					if (isUserWallOpened) {
						WallActivity.gIsRefreshUserProfile = false;
						refreshWallMessages();
						return;
					}
				}
			}
			PushNotification.show(SpikaActivity.this, mRlPushNotification, result.message, result.fromUser,
					result.fromGroup, result.fromType);
		}

		@Override
		public void onResultsFail() {
		}
	}

	private BroadcastReceiver mConnectionChangeReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {

			if (intent.getBooleanExtra(
					ConnectionChangeReceiver.HAS_INTERNET_CONNECTION, true) == true) {
				hideNoInternetNotification();
			} else {
				showNoInternetNotification();
			}

		}
	};

	private void checkInternetConnection() {

		if (SpikaApp.hasNetworkConnection()) {
			hideNoInternetNotification();
		} else {
			showNoInternetNotification();
		}
	}

	private void showNoInternetNotification() {
		if (mRlNoInternetNotification != null) {
			if (mRlNoInternetNotification.getVisibility() == View.GONE) {
				mRlNoInternetNotification.startAnimation(mSlideFromTop);
			} else {
				mRlNoInternetNotification.setVisibility(View.VISIBLE);
			}
		}

	}

	private void hideNoInternetNotification() {
		if (mRlNoInternetNotification != null) {
			if (mRlNoInternetNotification.getVisibility() == View.VISIBLE) {
				mRlNoInternetNotification.startAnimation(mSlideOutTop);
			}
		}
	}

	protected class GetUserByIdAsync extends SpikaAsync<String, Void, User> {

		public GetUserByIdAsync(Context context) {
			super(context);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected User backgroundWork(String... params) throws ClientProtocolException, JSONException, IOException, SpikaException, IllegalStateException, SpikaForbiddenException {
			String userId = params[0];
			
			return CouchDB.findUserById(userId);
		}

		@Override
		protected void onPostExecute(User result) {
			super.onPostExecute(result);
		}
	}

	protected class GetGroupByIdAsync extends SpikaAsync<String, Void, Group> {

		public GetGroupByIdAsync(Context context) {
			super(context);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}
		
		@Override
		protected Group backgroundWork(String... params) throws ClientProtocolException, IOException, JSONException, SpikaException, IllegalStateException, SpikaForbiddenException {
			String id = params[0];
			return CouchDB.findGroupById(id);
		}

		@Override
		protected void onPostExecute(Group group) {
			super.onPostExecute(group);
		}
	}
	
	protected class GetGroupByNameAsync extends SpikaAsync<String, Void, Group> {

	    private HookUpProgressDialog mProgressDialog;
	    
		public GetGroupByNameAsync(Context context) {
			super(context);
			mProgressDialog = new HookUpProgressDialog(context);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}
		
		@Override
		protected Group backgroundWork(String... params) throws ClientProtocolException, IOException, JSONException, SpikaException, IllegalStateException, SpikaForbiddenException {
			String name = params[0];
			return CouchDB.findGroupsByName(name).get(0);
		}

		@Override
		protected void onPostExecute(Group group) {
			super.onPostExecute(group);
			mProgressDialog.dismiss();
		}
	}

	protected void refreshActivitySummaryViews() {
	}

	protected void refreshWallMessages() {
	}

	protected void getActivitySummary () {
		if (UsersManagement.getLoginUser() != null) {
			String id = UsersManagement.getLoginUser().getId();
			CouchDB.findUserActivitySummary(id, new GetActivitySummaryListener(), SpikaActivity.this, false);
		}
	}
	
	private class GetActivitySummaryListener implements ResultListener<ActivitySummary> {
		
		@Override
		public void onResultsSucceded(ActivitySummary activitySummary) {
			if (activitySummary != null) {
				UsersManagement.getLoginUser().setActivitySummary(
						activitySummary);

				SpikaActivity.this.refreshActivitySummaryViews();
			}
		}

		@Override
		public void onResultsFail() {
		}
	}

	protected void unbindDrawables(View view) {
		if (view.getBackground() != null) {
			view.getBackground().setCallback(null);
		}
		if (view instanceof ViewGroup) {
			for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
				unbindDrawables(((ViewGroup) view).getChildAt(i));
			}
			((ViewGroup) view).removeAllViews();
		}
	}

	protected void setObjectsNull() {
		mPushReceiver = null;
		mSlideFromTop = null;
		mSlideOutTop = null;
		LocalBroadcastManager.getInstance(this).unregisterReceiver(
				mConnectionChangeReceiver);
	}
	
	protected void hideKeyboard() {
		InputMethodManager inputMethodManager = (InputMethodManager) this
				.getSystemService(Activity.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus()
				.getWindowToken(), 0);
	}
	
    protected void showTutorial(String textTutorial) {
        
        if (tutorialShowed == false && SpikaApp.getPreferences().getShowTutorial(Utils.getClassNameInStr(this))) {
            Tutorial.show(this, textTutorial);
            SpikaApp.getPreferences().setShowTutorial(false, Utils.getClassNameInStr(this));
            tutorialShowed = true;
        }
    }
    
    protected void showTutorialOnceAfterBoot(String textTutorial) {

        if (tutorialShowed == false && SpikaApp.getPreferences().getShowTutorialForBoot(Utils.getClassNameInStr(this))) {
            Tutorial.show(this, textTutorial);
            SpikaApp.getPreferences().setShowTutorialForBoot(false, Utils.getClassNameInStr(this));
            tutorialShowed = true;
        }
    }
}
