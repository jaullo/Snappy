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

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;


import com.snappy.adapters.CommentsAdapter;
import com.snappy.couchdb.CouchDB;
import com.snappy.couchdb.ResultListener;
import com.snappy.couchdb.model.Comment;
import com.snappy.couchdb.model.Message;
import com.snappy.extendables.SpikaActivity;
import com.snappy.lazy.ImageLoader;
import com.snappy.management.CommentManagement;
import com.snappy.management.UsersManagement;
import com.snappy.messageshandling.GetCommentsAsync;
import com.snappy.utils.LayoutHelper;
import com.snappy.utils.Utils;

/**
 * VideoActivity
 * 
 * Displays video message and related comments.
 */

public class VideoActivity extends SpikaActivity {

	private static String sFileName = null;

	private VideoView mVideoView;

	private final int VIDEO_IS_PLAYING = 2;
	private final int VIDEO_IS_PAUSED = 1;
	private final int VIDEO_IS_STOPPED = 0;

	private int mIsPlaying = VIDEO_IS_STOPPED; // 0 - play is on stop, 1 - play
												// is on pause, 2
	// - playing

	private ProgressBar mPbForPlaying;
	private ImageView mPlayPause;
	private ImageView mStopVideo;

	private Handler mHandlerForProgressBar = new Handler();
	private Runnable mRunnForProgressBar;

	private long mDurationOfVideo = 0;

	private Message mMessage;
	private ListView mLvComments;

	private CommentsAdapter mCommentsAdapter;

	private ArrayList<Comment> mComments;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_video);

		setGeneralInit();

		setInitComments();

		setHeaderAndAvatar();

		setVideoControl();

	}

	private void setHeaderAndAvatar() {
		findViewById(R.id.btnSend).setVisibility(View.GONE);

		ImageView ivAvatar = (ImageView) findViewById(R.id.ivAvatarVideo);
		LayoutHelper.scaleWidthAndHeight(this, 5f, ivAvatar);

		TextView tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setText(getString(R.string.video));

		TextView tvNameOfUserVideo = (TextView) findViewById(R.id.tvNameOfUserVideo);

		Button back = (Button) findViewById(R.id.btnBack);
		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

		Bundle extras = getIntent().getExtras();
		String idOfUser = extras.getString("idOfUser");
		String nameOfUser = extras.getString("nameOfUser");

		String avatarId = null;
		if (extras.getBoolean("videoFromUser")) {
			CouchDB.findAvatarIdAndDisplay(idOfUser, ivAvatar, this);
		} else {
			avatarId = UsersManagement.getLoginUser().getAvatarFileId();
			Utils.displayImage(avatarId, ivAvatar, ImageLoader.SMALL,
					R.drawable.user_stub, false);
		}

		if (mMessage.getBody().equals(null) || mMessage.getBody().equals("")) {
			tvNameOfUserVideo.setText(nameOfUser.toUpperCase(Locale.getDefault()) + "'S VIDEO");
		} else {
			tvNameOfUserVideo.setText(mMessage.getBody());
		}

		fileDownloadAsync(mMessage.getVideoFileId(), new File(getHookUpPath(), "video_download.mp4"));

	}

	private void scrollListViewToBottom() {
		mLvComments.post(new Runnable() {
			@Override
			public void run() {
				// Select the last row so it will scroll into view...
				mLvComments.setSelection(mLvComments.getCount() - 1);
			}
		});
	}

	private void setGeneralInit() {
		View mTop = getLayoutInflater().inflate(
				R.layout.header_for_listview_in_video_activity, mLvComments,
				false);
		mVideoView = (VideoView) mTop.findViewById(R.id.videoViewForVideo);
		mPlayPause = (ImageView) mTop.findViewById(R.id.ivPlayPause);
		mStopVideo = (ImageView) mTop.findViewById(R.id.ivStopSound);
		mPbForPlaying = (ProgressBar) mTop.findViewById(R.id.pbVoice);
		mLvComments = (ListView) findViewById(R.id.lvPhotoComments);

		LinearLayout relativeLayoutForVideo = (LinearLayout) mTop
				.findViewById(R.id.videoWrapper);
		LayoutParams params = (LayoutParams) relativeLayoutForVideo
				.getLayoutParams();
		// android.widget.LinearLayout.LayoutParams params =
		// (android.widget.LinearLayout.LayoutParams)
		// mVideoView.getLayoutParams();
		params.height = getResources().getDisplayMetrics().heightPixels / 2;
		relativeLayoutForVideo.setLayoutParams(params);
		mVideoView.setBackgroundColor(Color.TRANSPARENT);

		mLvComments.addHeaderView(mTop);

	}

	private void setVideoControl() {
		mIsPlaying = VIDEO_IS_STOPPED;

		mPlayPause.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mIsPlaying == VIDEO_IS_PLAYING) {
					// pause
					mPlayPause.setImageResource(R.drawable.play_btn);
					onPlay(1);
				} else {
					// play
					mPlayPause.setImageResource(R.drawable.pause_btn);
					onPlay(0);
				}
			}
		});

		mStopVideo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mIsPlaying == VIDEO_IS_PLAYING
						|| mIsPlaying == VIDEO_IS_PAUSED) {
					// stop
					mPlayPause.setImageResource(R.drawable.play_btn);
					onPlay(2);
				}
			}
		});
	}

	private void setInitComments() {
		// for comment
		// **********************************************

		mLvComments.setSelection(0);
		mLvComments.setCacheColorHint(0);

		final EditText etComment = (EditText) findViewById(R.id.etComment);
		etComment.setTypeface(SpikaApp.getTfMyriadPro());

		mMessage = (Message) getIntent().getSerializableExtra("message");

		mComments = new ArrayList<Comment>();

		new GetCommentsAsync(VideoActivity.this, mMessage, mComments,
				mCommentsAdapter, mLvComments, true)
				.execute(mMessage.getId());
		scrollListViewToBottom();

		final Button btnSendComment = (Button) findViewById(R.id.btnSendComment);
		btnSendComment.setTypeface(SpikaApp.getTfMyriadProBold(),
				Typeface.BOLD);

		btnSendComment.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String commentText = etComment.getText().toString();
				if (!commentText.equals("")) {
					Comment comment = CommentManagement.createComment(
							commentText, mMessage.getId());
					scrollListViewToBottom();
					
					CouchDB.createCommentAsync(comment, new CreateCommentFinish(), VideoActivity.this, true);

					etComment.setText("");
					Utils.hideKeyboard(VideoActivity.this);
				}

			}
		});

		// **********************************************
	}

	public void setMessageFromAsync(Message message) {
		mMessage = message;
	}

	private void onPlay(int playPauseStop) {
		if (playPauseStop == 0) {
			startPlaying();
		} else if (playPauseStop == 1) {
			pausePlaying();
		} else {
			stopPlaying();

		}
	}

	private void startPlaying() {
		if (mIsPlaying == VIDEO_IS_STOPPED) {
			mVideoView.requestFocus();

			 mVideoView.setVideoURI(Uri.parse(sFileName));
			mVideoView.setVideoPath(sFileName);

			mVideoView.start();

			mVideoView.setOnPreparedListener(new OnPreparedListener() {

				@Override
				public void onPrepared(MediaPlayer mp) {
					mDurationOfVideo = mVideoView.getDuration();
					mPbForPlaying.setMax((int) mDurationOfVideo);

					mRunnForProgressBar = new Runnable() {

						@Override
						public void run() {
							mPbForPlaying.setProgress((int) mVideoView
									.getCurrentPosition());
							if (mDurationOfVideo - 99 > mVideoView
									.getCurrentPosition()) {
								mHandlerForProgressBar.postDelayed(
										mRunnForProgressBar, 100);
							} else {
								mPbForPlaying.setProgress((int) mVideoView
										.getDuration());
								new Handler().postDelayed(new Runnable() {
									// *******wait for video to finish
									@Override
									public void run() {
										mPlayPause
												.setImageResource(R.drawable.play_btn);
										onPlay(2);
									}
								}, 120);
							}
						}
					};
					mHandlerForProgressBar.post(mRunnForProgressBar);
					mIsPlaying = VIDEO_IS_PLAYING;
				}
			});

		} else if (mIsPlaying == VIDEO_IS_PAUSED) {
			mVideoView.start();
			mHandlerForProgressBar.post(mRunnForProgressBar);
			mIsPlaying = VIDEO_IS_PLAYING;
		}
	}

	private void stopPlaying() {
		mVideoView.stopPlayback();
		mHandlerForProgressBar.removeCallbacks(mRunnForProgressBar);
		mPbForPlaying.setProgress(0);
		mIsPlaying = VIDEO_IS_STOPPED;
	}

	private void pausePlaying() {
		mVideoView.pause();
		mHandlerForProgressBar.removeCallbacks(mRunnForProgressBar);
		mIsPlaying = VIDEO_IS_PAUSED;
	}

	private void fileDownloadAsync (String fileId, File file) {
		CouchDB.downloadFileAsync(fileId, file, new FileDownloadFinish(), VideoActivity.this, true);
	}
	
	private class FileDownloadFinish implements ResultListener<File> {
		@Override
		public void onResultsSucceded(File result) {
			sFileName = getHookUpPath().getAbsolutePath()
					+ "/video_download.mp4";
			
		}

		@Override
		public void onResultsFail() {
			Toast.makeText(VideoActivity.this,
					R.string.videoactivity_download_error, Toast.LENGTH_LONG)
					.show();
			mPlayPause.setClickable(false);
			mStopVideo.setClickable(false);
		}
	}

	private File getHookUpPath() {
		File root = android.os.Environment.getExternalStorageDirectory();
		File dir = new File(root.getAbsolutePath() + "/HookUp");
		if (dir.exists() == false) {
			dir.mkdirs();
		}
		return dir;
	}

	private class CreateCommentFinish implements ResultListener<String> {

		@Override
		public void onResultsSucceded(String commentId) {
			if (commentId != null) {
				new GetCommentsAsync(VideoActivity.this, mMessage, mComments,
						mCommentsAdapter, mLvComments, false).execute(mMessage.getId());
			}
		}

		@Override
		public void onResultsFail() {			
		}
	}
}
