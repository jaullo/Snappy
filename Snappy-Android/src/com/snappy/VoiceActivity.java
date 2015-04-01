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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.snappy.adapters.CommentsAdapter;
import com.snappy.couchdb.CouchDB;
import com.snappy.couchdb.ResultListener;
import com.snappy.couchdb.SpikaException;
import com.snappy.couchdb.SpikaForbiddenException;
import com.snappy.couchdb.model.Comment;
import com.snappy.couchdb.model.Message;
import com.snappy.extendables.SpikaActivity;
import com.snappy.lazy.ImageLoader;
import com.snappy.management.CommentManagement;
import com.snappy.management.UsersManagement;
import com.snappy.messageshandling.FindAvatarFieldAsync;
import com.snappy.messageshandling.GetCommentsAsync;
import com.snappy.utils.LayoutHelper;
import com.snappy.utils.Utils;

/**
 * VoiceActivity
 * 
 * Displays voice message and related comments.
 */

public class VoiceActivity extends SpikaActivity {
	
	private List<Comment> mComments;
	private CommentsAdapter mCommentsAdapter;
	private Bundle mExtras;
	private Handler mHandlerForProgressBar = new Handler();
	private int mIsPlaying = 0; // 0 - play is on stop, 1 - play is on pause, 2
	// - playing
	private ListView mLvComments;
	private Message mMessage;
	private ProgressBar mPbForPlaying;
	private ImageView mPlayPause;
	private MediaPlayer mPlayer = null;
	private Runnable mRunnForProgressBar;
	private ImageView mStopSound;
	private static String sFileName = null;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_voice);

		setInitComments();

		mExtras = getIntent().getExtras();

		setInitHeaderAndAvatar();

		setInitSoundControl();

	}

	private void setInitHeaderAndAvatar() {
		ImageView ivAvatar = (ImageView) findViewById(R.id.ivAvatarVoice);
		LayoutHelper.scaleWidthAndHeight(this, 5f, ivAvatar);

		TextView tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setText("VOICE");

		TextView tvNameOfUser = (TextView) findViewById(R.id.tvNameOfUserVoice);

		// message from somebody
		fileDownloadAsync(mMessage.getVoiceFileId(), new File(getHookUpPath(), "voice_download.wav"));

		String idOfUser = mExtras.getString("idOfUser");
		String nameOfUser = mExtras.getString("nameOfUser");
		
		//Log.i("Usuario Recuperado Audio",  avatarId);
		
		if (UsersManagement.getLoginUser().getId().equals(idOfUser))
		{
			Utils.displayImage(UsersManagement.getLoginUser().getAvatarFileId(), ivAvatar, ImageLoader.SMALL,
					R.drawable.user_stub, false);
		}
		else
			CouchDB.findAvatarIdAndDisplay(idOfUser, ivAvatar, this);
		
		///String avatarId = null;
		//try {
		//	avatarId = new FindAvatarFieldAsync(this).execute(idOfUser).get();	
		//	} catch (InterruptedException e) {
		//		e.printStackTrace();
	//		} catch (ExecutionException e) {
	//			e.printStackTrace();
	//		}
	
	//	Log.i("Usuario Recuperado Audio",  avatarId);
		
		//Utils.displayImage(avatarId, ivAvatar, ImageLoader.SMALL, R.drawable.user_stub, false);
		
		if (mMessage.getBody().equals(null) || mMessage.getBody().equals("")) {

			tvNameOfUser.setText(getResources().getString(R.string.voice_activity_voice) + " " + Utils.capitalizeFirstLetter(nameOfUser));
				
			//tvNameOfUser.setText(nameOfUser.toUpperCase(Locale.getDefault())
			//		+ "'S VOICE");
		} else {
			tvNameOfUser.setText(mMessage.getBody());
		}

		Button back = (Button) findViewById(R.id.btnBack);
		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
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

	private void setInitComments() {
		// for comment
		// **********************************************
		mLvComments = (ListView) findViewById(R.id.lvPhotoComments);
		mLvComments.setCacheColorHint(0);

		final EditText etComment = (EditText) findViewById(R.id.etComment);
		etComment.setTypeface(SpikaApp.getTfMyriadPro());

		mMessage = (Message) getIntent().getSerializableExtra("message");

		mComments = new ArrayList<Comment>();

		new GetCommentsAsync(VoiceActivity.this, mMessage, mComments,
				mCommentsAdapter, mLvComments, true).execute(mMessage.getId());
		scrollListViewToBottom();

		Button btnSendComment = (Button) findViewById(R.id.btnSendComment);
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
					
					CouchDB.createCommentAsync(comment, new CreateCommentFinish(), VoiceActivity.this, true);

					etComment.setText("");
					Utils.hideKeyboard(VoiceActivity.this);

				}

			}
		});

		// **********************************************
	}

	public void setMessageFromAsync(Message message) {
		mMessage = message;
	}

	private void setInitSoundControl() {
		mPbForPlaying = (ProgressBar) findViewById(R.id.pbVoice);
		mPlayPause = (ImageView) findViewById(R.id.ivPlayPause);
		mStopSound = (ImageView) findViewById(R.id.ivStopSound);

		mIsPlaying = 0;

		mPlayPause.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mIsPlaying == 2) {
					// pause
					mPlayPause.setImageResource(R.drawable.play_btn);
					onPlay(1);
					return;
				} else {
					// play
					mPlayPause.setImageResource(R.drawable.pause_btn);
					onPlay(0);
				}
			}
		});

		mStopSound.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mIsPlaying == 2 || mIsPlaying == 1) {
					// stop
					mPlayPause.setImageResource(R.drawable.play_btn);
					onPlay(2);
				}
			}
		});

	}

	private void onPlay(int playPauseStop) {

		if (playPauseStop == 0) {

			startPlaying();
			return;

		} 
		
		if (playPauseStop == 1) {

			pausePlaying();
			return;

		} 
//		{
//
			stopPlaying();
//
//		}
	}

	private void startPlaying() {
		if (mIsPlaying == 0) {
			mPlayer = new MediaPlayer();
			try {
				mPlayer.setDataSource(sFileName);
				mPlayer.prepare();
				mPlayer.start();
				mPbForPlaying.setMax((int) mPlayer.getDuration());

				mRunnForProgressBar = new Runnable() {

					@Override
					public void run() {
						mPbForPlaying.setProgress((int) mPlayer
								.getCurrentPosition());
						if (mPlayer.getDuration() - 99 > mPlayer
								.getCurrentPosition()) {
							mHandlerForProgressBar.postDelayed(
									mRunnForProgressBar, 100);
						} else {
							mPbForPlaying.setProgress((int) mPlayer
									.getDuration());
						}
					}
				};
				mHandlerForProgressBar.post(mRunnForProgressBar);
				mIsPlaying = 2;
				return;
			} catch (IOException e) {
				Log.e("LOG", "prepare() failed");
			}
		} else if (mIsPlaying == 1) {
			mPlayer.start();
			mHandlerForProgressBar.post(mRunnForProgressBar);
			mIsPlaying = 2;
		}
	}

	private void stopPlaying() {
		mPlayer.release();
		mHandlerForProgressBar.removeCallbacks(mRunnForProgressBar);
		mPbForPlaying.setProgress(0);
		mPlayer = null;
		mIsPlaying = 0;
	}

	private void pausePlaying() {
		mPlayer.pause();
		mHandlerForProgressBar.removeCallbacks(mRunnForProgressBar);
		mIsPlaying = 1;
	}

	public void onPause() {
		super.onPause();

		if (mPlayer != null) {
			mPlayer.release();
			mPlayer = null;
			mIsPlaying = 0;
			mPbForPlaying.setProgress(0);
			mPlayPause.setImageResource(R.drawable.play_btn);
		}
		mHandlerForProgressBar.removeCallbacks(mRunnForProgressBar);
	}

	private void fileDownloadAsync (String fileId, File file) {
		CouchDB.downloadFileAsync(fileId, file, new VoiceActivity.FileDownloadFinish(), VoiceActivity.this, true);
	}
	
	private class FileDownloadFinish implements ResultListener<File> {
		@Override
		public void onResultsSucceded(File result) {
						 
			sFileName = getHookUpPath().getAbsolutePath()
					+ "/voice_download.wav";
			
		}

		@Override
		public void onResultsFail() {
			Toast.makeText(VoiceActivity.this,
					R.string.voiceactivity_download_error, Toast.LENGTH_LONG)
					.show();
			mPlayPause.setClickable(false);
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
				new GetCommentsAsync(VoiceActivity.this, mMessage, mComments,
						mCommentsAdapter, mLvComments, false).execute(mMessage.getId());
			}
		}

		@Override
		public void onResultsFail() {			
		}
	}
}
