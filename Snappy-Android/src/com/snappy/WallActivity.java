/*
ž * The MIT License (MIT)
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
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SlidingDrawer;
import android.widget.TextView;


import com.snappy.adapters.WallMessagesAdapter;
import com.snappy.couchdb.CouchDB;
import com.snappy.couchdb.ResultListener;
import com.snappy.couchdb.SpikaAsyncTask;
import com.snappy.couchdb.model.Emoticon;
import com.snappy.couchdb.model.Group;
import com.snappy.couchdb.model.Message;
import com.snappy.couchdb.model.User;
import com.snappy.couchdb.model.WatchingGroupLog;
import com.snappy.dialog.HookUpAlertDialog;
import com.snappy.dialog.TempVideoChooseDialog;
import com.snappy.dialog.HookUpAlertDialog.ButtonType;
import com.snappy.extendables.SideBarActivity;
import com.snappy.lazy.Emoticons;
import com.snappy.management.SettingsManager;
import com.snappy.management.TimeMeasurer;
import com.snappy.management.UsersManagement;
import com.snappy.messageshandling.MessagesUpdater;
import com.snappy.messageshandling.SendMessageAsync;
import com.snappy.messageshandling.UpdateMessagesInListView;
import com.snappy.messageshandling.WallScrollListener;
import com.snappy.utils.Const;
import com.snappy.view.EmoticonsLayout;

/**
 * WallActivity
 * 
 * Displays a list of messages in private wall or other user/group wall; has
 * options for sending text, photo, video, voice, location and emoticon message.
 */

public class WallActivity extends SideBarActivity {

	public static ListView gLvWallMessages;
	public static WallMessagesAdapter gMessagesAdapter;
	public static ArrayList<Message> gCurrentMessages = null;
	public static ArrayList<Message> gNewMessages = null;
	public static boolean gIsRefreshUserProfile = false;
	public static boolean gIsVisible = false;

	public static UpdateMessagesInListView gUpdater = null;
	public static MessagesUpdater gMessagesUpdater = null;

	private Button mBtnCamera;
	private Button mBtnGallery;
	private Button mBtnVideo;
	private Button mBtnEmoji;
	private Button mBtnLocation;
	private Button mBtnRecord;
	private Button mBtnWallSend;
	private ImageButton mBtnOpenSlidingDrawer;
	private EditText mEtMessageText;
	private SlidingDrawer mSlidingDrawer;
	private RelativeLayout mRlBottom;

	private RelativeLayout.LayoutParams mParamsOpened;
	private RelativeLayout.LayoutParams mParamsClosed;

	private Intent mProfileIntent;
	private LinearLayout mButtonsLayout;
	private EmoticonsLayout mEmoticonsLayout;
	private HorizontalScrollView mScrollViewEmoticons;

	private static WallActivity sInstance = null;

	private static final int REQUEST_CODE_FROM_LOCATION = 1001;

	private static final int OPENED = 1003;
	private static final int CLOSED = 1004;

	private TempVideoChooseDialog mChooseDialog;
	private TextView mTvNoMessages;

	private View mViewDeletedGroup;

	private boolean mPushHandledOnNewIntent = false;
	private HookUpAlertDialog mDeletedGroupDialog;

	private ReloadTimerTask timerTask;
	private Timer timer;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		sInstance = this;
		setContentView(R.layout.activity_wall);
		setSideBar(getString(R.string.WALL));
		initialization();
		onClickListeners();

	}

	@Override
	protected void onNewIntent(Intent intent) {
		mPushHandledOnNewIntent = false;
		if (getIntent().getBooleanExtra(Const.PUSH_INTENT, false)) {
			mPushHandledOnNewIntent = true;
			intent.removeExtra(Const.PUSH_INTENT);
			openWallFromNotification(intent);
		}
		setWatchingGroupLog();
		super.onNewIntent(intent);
	}

	@Override
	public void onResume() {
		super.onResume();

		Log.e("RESUME", "TIMERS!");
		timerTask = new ReloadTimerTask();
		timer = new Timer();
		timer.schedule(timerTask, 5 * 1000, 5 * 1000);
		
		if (checkTimersIfReloadNeeded()) {
			return;
		}
		
		if (!mPushHandledOnNewIntent) {
			if (getIntent().getBooleanExtra(Const.PUSH_INTENT, false)) {
				mPushHandledOnNewIntent = false;
				openWallFromNotification(getIntent());
				getIntent().removeExtra(Const.PUSH_INTENT);
			}
		}

		if (mSlidingDrawer.isOpened()) {
			setSlidingDrawer(CLOSED);
		}

		if (SpikaApp.hasNetworkConnection()) {

			refreshWallMessages();
			setWatchingGroupLog();
			checkIfGroupIsDeleted();
			setTitle();

		}
	}

	private boolean checkTimersIfReloadNeeded () {
		int myUTC = (int) (System.currentTimeMillis() / 1000);
		
		if (gCurrentMessages != null) {
			for (Message message : gCurrentMessages) {
				if (message.getDelete() != 0) {
					if (message.getDelete() < myUTC) {
						Log.e("*** RELOAD ***", " " + myUTC + " " + message.getDelete());
						MessagesUpdater.reload();
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private void setTitle() {

		if (UsersManagement.getToUser() != null) {
			mTvTitle.setText(UsersManagement.getToUser().getName());
		} else if (UsersManagement.getToGroup() != null) {
			mTvTitle.setText(UsersManagement.getToGroup().getName());
		} else {
			mTvTitle.setText(getString(R.string.WALL));
		}
	}

	private void setWatchingGroupLog() {
		String watchingGroupId = SpikaApp.getPreferences()
				.getWatchingGroupId();

		User loginUser = UsersManagement.getLoginUser();
		Group group = UsersManagement.getToGroup();

		boolean alreadyHasWatchingGroup = !watchingGroupId.equals("");

		if (alreadyHasWatchingGroup) {
			CouchDB.deleteWatchingGroupLogAsync(SpikaApp.getPreferences()
					.getWatchingGroupId(), SpikaApp.getPreferences()
					.getWatchingGroupRev(), new DeleteWatchingLogFinish(), WallActivity.this, false);
			SpikaApp.getPreferences().setWatchingGroupId("");
			SpikaApp.getPreferences().setWatchingGroupRev("");
		}

		if (group != null) {
			boolean isInFavorites = loginUser.isInFavoriteGroups(group);
			if (!isInFavorites) {
				WatchingGroupLog watchingGroupLog = new WatchingGroupLog();
				watchingGroupLog.setGroupId(UsersManagement.getToGroup()
						.getId());
				watchingGroupLog.setUserId(UsersManagement.getLoginUser()
						.getId());
				CouchDB.createWatchingGroupLogAsync(watchingGroupLog, new CreateWatchingLogFinish(), WallActivity.this, false);
			}
		}
	}

	private boolean isGroupDeleted() {
		if (UsersManagement.getToGroup() != null) {
			return (UsersManagement.getToGroup().isDeleted());
		}
		return false;
	}

	private void checkIfGroupIsDeleted() {
		if (isGroupDeleted()) {
			if (mDeletedGroupDialog == null) {
				mDeletedGroupDialog = new HookUpAlertDialog(this);
				mDeletedGroupDialog
						.show(getString(R.string.this_group_is_deleted_please_unsubscribe),
								ButtonType.CLOSE);
			}
			mViewDeletedGroup.setVisibility(View.VISIBLE);
		} else {
			mViewDeletedGroup.setVisibility(View.GONE);
		}
	}

	private void openWallFromNotification(Intent intent) {

		String fromUserId = intent.getStringExtra(Const.PUSH_FROM_USER_ID);
		String fromType = intent.getStringExtra(Const.PUSH_FROM_TYPE);

		User fromUser = null;
		Group fromGroup = null;

		try {
//			fromUser = new GetUserByIdAsync(this).execute(fromUserId).get();
			 
			fromUser = new SpikaAsyncTask<Void, Void, User>(new CouchDB.FindUserById(fromUserId), null, WallActivity.this, true).execute().get();
			if (fromType.equals(Const.PUSH_TYPE_GROUP)) {
				String fromGroupId = intent
						.getStringExtra(Const.PUSH_FROM_GROUP_ID);
				
//				fromGroup = new GetGroupByIdAsync(this).execute(fromGroupId)
//						.get();
				
				fromGroup = new SpikaAsyncTask<Void, Void, Group>(new CouchDB.FindGroupById(fromGroupId), null, WallActivity.this, true).execute().get();
				
				UsersManagement.setToGroup(fromGroup);
				UsersManagement.setToUser(null);
				SettingsManager.ResetSettings();
				if (WallActivity.gCurrentMessages != null) {
					WallActivity.gCurrentMessages.clear();
				}

			}
			if (fromType.equals(Const.PUSH_TYPE_USER)) {

				UsersManagement.setToUser(fromUser);
				UsersManagement.setToGroup(null);
				SettingsManager.ResetSettings();
				if (WallActivity.gCurrentMessages != null) {
					WallActivity.gCurrentMessages.clear();
				}
				WallActivity.gIsRefreshUserProfile = true;

			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}
	
//	private class OpenWallFromNotification implements Command<>

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			gIsVisible = true;
		} else {
			gIsVisible = false;
		}
	}

	@Override
	protected void enableViews() {
		super.enableViews();
		gLvWallMessages.setEnabled(true);
		mBtnOpenSlidingDrawer.setEnabled(true);
	}

	@Override
	protected void disableViews() {
		super.disableViews();
		if (gLvWallMessages != null)
			gLvWallMessages.setEnabled(false);
		if (mSlidingDrawer.isOpened()) {
			setSlidingDrawer(CLOSED);
		}
		mBtnOpenSlidingDrawer.setEnabled(false);
		hideKeyboard();
	}

	public static WallActivity getInstance() {
		return sInstance;
	}

	@Override
	protected void refreshWallMessages() {
		super.refreshWallMessages();

		TimeMeasurer.start();

		if (gUpdater == null || gMessagesUpdater == null) {
			gUpdater = new UpdateMessagesInListView(this);
		}
		MessagesUpdater.update(true);
		if (gMessagesAdapter != null) {
			gMessagesAdapter.notifyDataSetChanged();
		}
		if (gIsRefreshUserProfile) {
			setWallMessages();
			gIsRefreshUserProfile = false;
		}
	}

	public void checkMessagesCount() {
		if (gCurrentMessages.size() > 0) {
			mTvNoMessages.setVisibility(View.GONE);
		} else {
			mTvNoMessages.setVisibility(View.VISIBLE);
		}
	}

	@Override
	protected void setObjectsNull() {
		gCurrentMessages = null;
		gUpdater = null;
		gMessagesUpdater = null;
		unbindDrawables(findViewById(R.id.galleryEmoticons));
		mEmoticonsLayout.removeAllViews();
		mEmoticonsLayout = null;
		sInstance = null;
		gLvWallMessages = null;
		gMessagesAdapter = null;
		mScrollViewEmoticons = null;
		mButtonsLayout.removeAllViews();
		mButtonsLayout = null;
		super.setObjectsNull();
	}

	private void onClickListeners() {

		mBtnWallSend.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				String body = mEtMessageText.getText().toString();

				if (!body.equals("") && !mSideBarOpened) {
					mEtMessageText.setText("");
					setSlidingDrawer(CLOSED);
					new SendMessageAsync(getApplicationContext(),
							SendMessageAsync.TYPE_TEXT).execute(body, false);
				}
			}
		});

		mBtnOpenSlidingDrawer.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (mSlidingDrawer.isOpened()) {
					setSlidingDrawer(CLOSED);
				} else {
					setSlidingDrawer(OPENED);
				}

			}
		});

		mEtMessageText.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (mSlidingDrawer.isOpened()) {
					setSlidingDrawer(CLOSED);
				}
				smoothScrollToBottom();
			}
		});

		mEtMessageText.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				smoothScrollToBottom();
			}
		});

		mBtnCamera.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent intent = new Intent(WallActivity.this,
						CameraCropActivity.class);
				intent.putExtra("type", "camera");
				WallActivity.this.startActivity(intent);

			}
		});

		mBtnEmoji.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				setEmoticonsLayout(OPENED);

			}
		});

		mBtnGallery.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent intent = new Intent(WallActivity.this,
						CameraCropActivity.class);
				intent.putExtra("type", "gallery");
				WallActivity.this.startActivity(intent);

			}
		});

		mBtnLocation.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivityForResult(new Intent(WallActivity.this,
						LocationActivity.class).putExtra("location",
						"myLocation"), REQUEST_CODE_FROM_LOCATION);
			}
		});

		mBtnRecord.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(WallActivity.this,
						RecordingActivity.class));
			}
		});

		mBtnVideo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mChooseDialog.show();
			}
		});
	}

	private void smoothScrollToBottom() {
		if (gLvWallMessages != null && gMessagesAdapter != null
				&& gMessagesAdapter.getCount() > 1) {
			gLvWallMessages
					.smoothScrollToPosition(gMessagesAdapter.getCount() - 1);
		}
	}

	private void initialization() {

		mTvNoMessages = (TextView) findViewById(R.id.tvNoMessages);
		mBtnOpenSlidingDrawer = (ImageButton) findViewById(R.id.btnSlideButton);
		mBtnWallSend = (Button) findViewById(R.id.btnWallSend);
		mBtnWallSend.setTypeface(SpikaApp.getTfMyriadProBold(), Typeface.BOLD);
		mEtMessageText = (EditText) findViewById(R.id.etWallMessage);
		mEtMessageText.setTypeface(SpikaApp.getTfMyriadPro());
		mSlidingDrawer = (SlidingDrawer) findViewById(R.id.slDrawer);
		mButtonsLayout = (LinearLayout) findViewById(R.id.llButtonsLayout);
		mEmoticonsLayout = (EmoticonsLayout) findViewById(R.id.galleryEmoticons);
		mScrollViewEmoticons = (HorizontalScrollView) findViewById(R.id.svEmoticons);
		mRlBottom = (RelativeLayout) findViewById(R.id.rlBottom);
		mBtnCamera = (Button) findViewById(R.id.btnCamera);
		mBtnGallery = (Button) findViewById(R.id.btnGallery);
		mBtnVideo = (Button) findViewById(R.id.btnVideo);
		mBtnEmoji = (Button) findViewById(R.id.btnEmoji);
		mBtnLocation = (Button) findViewById(R.id.btnLocation);
		mBtnRecord = (Button) findViewById(R.id.btnRecord);
		mViewDeletedGroup = (View) findViewById(R.id.viewDeletedGroup);

		mParamsClosed = new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		mParamsClosed.leftMargin = 2;
		mParamsOpened = new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		mParamsOpened.leftMargin = 2;
		mParamsClosed.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		mParamsOpened.addRule(RelativeLayout.ABOVE, mSlidingDrawer.getId());

		setWallMessages();

		mProfileIntent = new Intent(WallActivity.this, MyProfileActivity.class);
		mProfileIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

		CouchDB.findAllEmoticonsAsync(new FindAllEmoticonsFinish(), WallActivity.this, true);

		mChooseDialog = new TempVideoChooseDialog(this);
		mChooseDialog.setOnButtonClickListener(
				TempVideoChooseDialog.BUTTON_CAMERA, new OnClickListener() {

					@Override
					public void onClick(View v) {

						Intent recordVideoIntent = new Intent(
								WallActivity.this, RecordingVideoActivity.class);
						recordVideoIntent.putExtra("camera", true);
						startActivity(recordVideoIntent);
						mChooseDialog.dismiss();

					}
				});

		mChooseDialog.setOnButtonClickListener(
				TempVideoChooseDialog.BUTTON_GALLERY, new OnClickListener() {

					@Override
					public void onClick(View v) {

						Intent recordVideoIntent = new Intent(
								WallActivity.this, RecordingVideoActivity.class);
						recordVideoIntent.putExtra("gallery", true);
						startActivity(recordVideoIntent);
						mChooseDialog.dismiss();

					}
				});

		mChooseDialog.setCancelable(true);

	}

	public void setWallMessages() {
		if (gCurrentMessages == null) {
			gCurrentMessages = new ArrayList<Message>();
		}
		gLvWallMessages = (ListView) findViewById(R.id.lvWallMessages);
		gLvWallMessages.setOnScrollListener(new WallScrollListener());
		gMessagesAdapter = new WallMessagesAdapter(this, gCurrentMessages);
		gLvWallMessages.setAdapter(gMessagesAdapter);
	}

	public void resetBoard() {
		WallActivity.gUpdater = null;
		WallActivity.gMessagesUpdater = null;
		gIsRefreshUserProfile = true;
		gCurrentMessages = new ArrayList<Message>();
	}

	public static void redirect(Context context) {
		Intent intent = new Intent(context, WallActivity.class);
		context.startActivity(intent);
	}

	@Override
	public void onBackPressed() {
		if (emoticonsLayoutIsOpened()) {
			setEmoticonsLayout(CLOSED);
		} else if (mSlidingDrawer.isOpened()) {
			setSlidingDrawer(CLOSED);
		} else {
			super.onBackPressed();
		}
	}

	private class FindAllEmoticonsFinish implements ResultListener<List<Emoticon>> {

		@Override
		public void onResultsSucceded(List<Emoticon> emoticons) {
			if (emoticons != null) {
				Emoticons.getInstance().setEmoticons(emoticons);
			}
			fillEmoticonsGallery(emoticons);
		}
		@Override
		public void onResultsFail() {			
		}
	}

	private class CreateWatchingLogFinish implements ResultListener<String> {

		@Override
		public void onResultsSucceded(String result) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onResultsFail() {
			// TODO Auto-generated method stub
		}
	}
	
	private class DeleteWatchingLogFinish implements ResultListener<String> {

		@Override
		public void onResultsSucceded(String result) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onResultsFail() {
			// TODO Auto-generated method stub
		}
	}

	private void fillEmoticonsGallery(final List<Emoticon> emoticons) {

		if (mEmoticonsLayout != null) {
			for (int i = 0; i < emoticons.size(); i = i + 2) {
				if (i + 1 < emoticons.size()) {
					mEmoticonsLayout
							.add(emoticons.get(i), emoticons.get(i + 1));
				} else {
					mEmoticonsLayout.add(emoticons.get(i), null);
				}
			}
		}
	}

	/**
	 * Sends message type "emoticon" to CouchDB and closes the sliding drawer
	 * 
	 * @param Emoticon
	 */
	public void sendEmoticon(Emoticon emoticon) {
		if (mSlidingDrawer.isOpened()) {
			setSlidingDrawer(CLOSED);
		}
		new SendMessageAsync(getApplicationContext(),
				SendMessageAsync.TYPE_EMOTICON).execute(emoticon, false);
	}

	private void setSlidingDrawer(int state) {
		switch (state) {
		case OPENED:
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(mEtMessageText.getWindowToken(), 0);
			mEtMessageText.clearFocus();
			mBtnOpenSlidingDrawer
					.setImageResource(R.drawable.hide_more_button_selector);
			mSlidingDrawer.open();
			mRlBottom.setLayoutParams(mParamsOpened);
			mButtonsLayout.setVisibility(View.VISIBLE);
			mScrollViewEmoticons.setVisibility(View.GONE);
			break;
		case CLOSED:
			mBtnOpenSlidingDrawer
					.setImageResource(R.drawable.more_button_selector);
			mSlidingDrawer.close();
			mRlBottom.setLayoutParams(mParamsClosed);
			break;
		default:
			break;
		}
	}

	private void setEmoticonsLayout(int state) {
		switch (state) {
		case OPENED:
			mButtonsLayout.setVisibility(View.GONE);
			mScrollViewEmoticons.setVisibility(View.VISIBLE);
			break;
		case CLOSED:
			mScrollViewEmoticons.setVisibility(View.GONE);
			mButtonsLayout.setVisibility(View.VISIBLE);
			break;
		default:
			break;
		}
	}

	private boolean emoticonsLayoutIsOpened() {
		if (mScrollViewEmoticons.getVisibility() == View.VISIBLE) {
			return true;
		} else {
			return false;
		}
	}

	private class ReloadTimerTask extends TimerTask {
		@Override
		public void run() {
			runOnUiThread(new Runnable() {
	            @Override
	            public void run() {
	            	checkTimersIfReloadNeeded();
	            }
			});
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		
		Log.e("PAUSE", "TIMERS!");
		
		if (timerTask != null) timerTask.cancel();
		if (timer != null) timer.cancel();
	}
	
	
}
