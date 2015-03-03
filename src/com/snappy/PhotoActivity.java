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
import java.util.List;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.snappy.adapters.CommentsAdapter;
import com.snappy.couchdb.CouchDB;
import com.snappy.couchdb.ResultListener;
import com.snappy.couchdb.model.Comment;
import com.snappy.couchdb.model.Message;
import com.snappy.extendables.SpikaActivity;
import com.snappy.lazy.ImageLoader;
import com.snappy.management.CommentManagement;
import com.snappy.messageshandling.GetCommentsAsync;
import com.snappy.utils.LayoutHelper;
import com.snappy.utils.Utils;

/**
 * PhotoActivity
 * 
 * Displays photo message and related comments.
 */

public class PhotoActivity extends SpikaActivity {

	private ImageView mIvPhotoImage;
	private EditText mEtComment;
	private Button mBtnSendComment;
	private ImageButton mBtnAvatarUser;
	private TextView mTvPostedBy;

	private ListView mLvComments;
	private Message mMessage;
	private CommentsAdapter mCommentsAdapter;
	private List<Comment> mComments;
	private Button mBtnBack;
	private ProgressBar mPbLoading;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_photo);

		mMessage = (Message) getIntent().getSerializableExtra("message");
		
		initialization();
		onClickListeners();

		mComments = new ArrayList<Comment>();
		new GetCommentsAsync(PhotoActivity.this, mMessage, mComments,
				mCommentsAdapter, mLvComments, true).execute(mMessage.getId());
		scrollListViewToBottom();
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

	private void initialization() {

		mLvComments = (ListView) findViewById(R.id.lvPhotoComments);
		LayoutInflater inflater = getLayoutInflater();
		ViewGroup photoHolder = (ViewGroup) inflater.inflate(
				R.layout.photo_holder, mLvComments, false);
		mLvComments.addHeaderView(photoHolder, null, false);
		mLvComments.setSelection(0);
		mLvComments.setCacheColorHint(0);

		mIvPhotoImage = (ImageView) findViewById(R.id.ivPhotoImage);
		mBtnAvatarUser = (ImageButton) findViewById(R.id.btnAvatarUser);
		mTvPostedBy = (TextView) findViewById(R.id.tvPostedBy);
		mEtComment = (EditText) findViewById(R.id.etComment);
		mEtComment.setTypeface(SpikaApp.getTfMyriadPro());
		mBtnSendComment = (Button) findViewById(R.id.btnSendComment);
		mBtnSendComment.setTypeface(SpikaApp.getTfMyriadProBold(),
				Typeface.BOLD);
		mBtnBack = (Button) findViewById(R.id.btnBack);
		mBtnBack.setTypeface(SpikaApp.getTfMyriadProBold(), Typeface.BOLD);

		mPbLoading = (ProgressBar) findViewById(R.id.pbLoadingForImage);

		if (mMessage != null) {
			Utils.displayImage(
						mMessage.getImageFileId(), mIvPhotoImage, mPbLoading,
						ImageLoader.LARGE, R.drawable.image_stub, false);
			
			LayoutHelper.scaleWidthAndHeight(this, 5f,
					mBtnAvatarUser);

			CouchDB.findAvatarIdAndDisplay(mMessage.getFromUserId(), mBtnAvatarUser, this);

			mTvPostedBy.setText(getSubTextDateAndUser(mMessage));
		}
	}
	
	void getAvatarAsync () {
		CouchDB.findAvatarIdAndDisplay(mMessage.getFromUserId(), mBtnAvatarUser, this);
	}

	private void onClickListeners() {

		mBtnSendComment.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String commentText = mEtComment.getText().toString();
				if (!commentText.equals("")) {
					Comment comment = CommentManagement
							.createComment(commentText, mMessage.getId());
					scrollListViewToBottom();
					
					CouchDB.createCommentAsync(comment, new CreateCommentFinish(), PhotoActivity.this, true);
					
					mEtComment.setText("");
					Utils.hideKeyboard(PhotoActivity.this);
				}

			}
		});

		mBtnBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PhotoActivity.this.finish();
			}
		});
	}

	public void setMessageFromAsync(Message message) {
		mMessage = message;
	}

	private String getSubTextDateAndUser(Message message) {
		String subText = null;
		long timeOfCreationOrUpdate = message.getCreated();
		subText = Utils.getFormattedDateTime(timeOfCreationOrUpdate) + R.string.photoactivity_by
				+ message.getFromUserName();
		return subText;
	}

	private class CreateCommentFinish implements ResultListener<String> {

		@Override
		public void onResultsSucceded(String commentId) {
			if (commentId != null) {
				new GetCommentsAsync(PhotoActivity.this, mMessage, mComments,
						mCommentsAdapter, mLvComments, false).execute(mMessage.getId());
			} else {
				Toast.makeText(PhotoActivity.this, "Error with creating comment", Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		public void onResultsFail() {			
		}
	}
}
