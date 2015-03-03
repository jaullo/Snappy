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
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.Preference;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.snappy.couchdb.CouchDB;
import com.snappy.couchdb.ResultListener;
import com.snappy.couchdb.model.ActivitySummary;
import com.snappy.couchdb.model.User;
import com.snappy.dialog.HookUpAlertDialog;
import com.snappy.dialog.HookUpDialog;
import com.snappy.dialog.HookUpProgressDialog;
import com.snappy.dialog.Tutorial;
import com.snappy.dialog.HookUpAlertDialog.ButtonType;
import com.snappy.management.FileManagement;
import com.snappy.management.UsersManagement;
import com.snappy.utils.Const;
import com.snappy.utils.ConstServer;
import com.snappy.utils.Utils;
import com.snappy.view.SimpleAutoFitTextView;

/**
 * SignInActivity
 * 
 * Allows user to sign in, sign up or receive an email with password if user
 * is already registered with a valid email.
 */

public class SignInActivity extends Activity {
	
	private static final int REQUEST_CODE_LIST_SERVERS = 11;

	private EditText mEtSignInEmail;
	private EditText mEtSignInPassword;
	private EditText mEtSignUpName;
	private EditText mEtSignUpEmail;
	private EditText mEtSignUpPassword;
	private EditText mEtSendPasswordEmail;
	private Button mBtnActive;
	private Button mBtnInactive;
	private Button mBtnForgotPassword;
	private Button mBtnBack;
	private Button mBtnSendPassword;
	private LinearLayout mLlSignIn;
	private LinearLayout mLlSignUp;
	private TextView mTvTitle;
	private TextView mTvSelectServer;

	private String mSignInEmail;
	private String mSignInPassword;
	private String mSignUpName;
	private String mSignUpEmail;
	private String mSignUpPassword;
	private String mSendPasswordEmail;

	private boolean mUserCreated = false;
	private boolean mUserSignedIn = false;
	private LinearLayout mLlForgotPassword;
	private static SignInActivity sInstance = null;
	private Screen mActiveScreen;
	private HookUpDialog mSendPasswordDialog;

	private HookUpProgressDialog mProgressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sign_in);
		
		Log.e("Dir", FileManagement._mainDirName);
		
		initialization();
		sInstance = this;
		SpikaApp.gOpenFromBackground = true;

		showTutorial(getString(R.string.tutorial_login));
		
		moveTaskToBack(false);
	}

	private void showTutorial(String textTutorial) {
		if (SpikaApp.getPreferences().getShowTutorial(
				Utils.getClassNameInStr(this))) {
			Tutorial.show(this, textTutorial);
			SpikaApp.getPreferences().setShowTutorial(false,
					Utils.getClassNameInStr(this));
		}
	}

	private void initialization() {

		// initialize singletons CouchDB & UsersManagement
		new CouchDB();
		new UsersManagement();
		new FileManagement(getApplicationContext());

		mSendPasswordDialog = new HookUpDialog(this);
		mSendPasswordDialog.setOnButtonClickListener(HookUpDialog.BUTTON_OK,
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						CouchDB.sendPassword(mEtSendPasswordEmail.getText().toString(), new SendPasswordListener() ,SignInActivity.this, true);
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

		mEtSignInEmail = (EditText) findViewById(R.id.etSignInEmail);
		mEtSignInPassword = (EditText) findViewById(R.id.etSignInPassword);
		mEtSignUpName = (EditText) findViewById(R.id.etSignUpName);
		mEtSignUpEmail = (EditText) findViewById(R.id.etSignUpEmail);
		mEtSignUpPassword = (EditText) findViewById(R.id.etSignUpPassword);
		mEtSendPasswordEmail = (EditText) findViewById(R.id.etForgotPasswordEmail);
		mTvSelectServer = (TextView) findViewById(R.id.tvServerSelect);
		mBtnBack = (Button) findViewById(R.id.btnBack);
		mBtnBack.setTypeface(SpikaApp.getTfMyriadProBold(), Typeface.BOLD);
		mBtnBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mActiveScreen == Screen.FORGOT_PASSWORD) {
					setActiveScreen(Screen.SIGN_IN);
				}

			}
		});
		mBtnActive = (Button) findViewById(R.id.btnActive);
		mBtnActive.setTypeface(SpikaApp.getTfMyriadProBold(), Typeface.BOLD);
		mBtnActive
				.setBackgroundResource(R.drawable.rounded_rect_positive_selector);
		mBtnInactive = (Button) findViewById(R.id.btnInactive);
		mBtnInactive.setTypeface(SpikaApp.getTfMyriadProBold(), Typeface.BOLD);
		mBtnInactive
				.setBackgroundResource(R.drawable.rounded_rect_neutral_selector);
		mBtnForgotPassword = (Button) findViewById(R.id.btnForgotPassword);
		mBtnForgotPassword
				.setTextColor(new ColorStateList(new int[][] {
						new int[] { android.R.attr.state_pressed },
						new int[] {} }, new int[] { Color.rgb(190, 190, 190),
						Color.rgb(125, 125, 125), }));
		mBtnForgotPassword.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setActiveScreen(Screen.FORGOT_PASSWORD);

			}
		});
		mBtnSendPassword = (Button) findViewById(R.id.btnSendPassword);
		mBtnSendPassword.setTypeface(SpikaApp.getTfMyriadProBold(),
				Typeface.BOLD);
		mBtnSendPassword.setVisibility(View.GONE);
		
		
		
		mBtnSendPassword.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (isEmailValid(mEtSendPasswordEmail.getText().toString())) 
				{
					mSendPasswordDialog.show(getString(R.string.confirm_email)
							+ "\n" + mEtSendPasswordEmail.getText().toString());
				} else {

					final HookUpDialog dialog = new HookUpDialog(
							SignInActivity.this);
					dialog.showOnlyOK(getString(R.string.email_not_valid));
				}
			}
		});
		
		mLlSignIn = (LinearLayout) findViewById(R.id.llSignInBody);
		mLlSignIn.setVisibility(View.VISIBLE);
		mLlSignUp = (LinearLayout) findViewById(R.id.llSignUpBody);
		mLlSignUp.setVisibility(View.GONE);
		mLlForgotPassword = (LinearLayout) findViewById(R.id.llForgotPasswordBody);
		mLlForgotPassword.setVisibility(View.GONE);
		mTvTitle = (TextView) findViewById(R.id.tvSignInTitle);
		mTvTitle.setText(getString(R.string.SIGN_IN));
		mTvTitle.setTypeface(SpikaApp.getTfMyriadPro());

		mEtSignInEmail.setTypeface(SpikaApp.getTfMyriadPro());
		mEtSignInPassword.setTypeface(SpikaApp.getTfMyriadPro());
		mEtSignUpName.setTypeface(SpikaApp.getTfMyriadPro());
		mEtSignUpEmail.setTypeface(SpikaApp.getTfMyriadPro());
		mEtSignUpPassword.setTypeface(SpikaApp.getTfMyriadPro());
		mEtSendPasswordEmail.setTypeface(SpikaApp.getTfMyriadPro());
		mTvSelectServer.setTypeface(SpikaApp.getTfMyriadPro());
		
		mTvSelectServer.setText(SpikaApp.getPreferences().getUserServerName());
		findViewById(R.id.rlServerField).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivityForResult(new Intent(SignInActivity.this, ServersListActivity.class), REQUEST_CODE_LIST_SERVERS);
			}
		});
		
		getEmailAndPasswordFromIntent();
		checkToken();

		setActiveScreen(Screen.SIGN_IN);
		
		//check if token is expired to set edittexts from shared preferences
		if(getIntent().getBooleanExtra(getString(R.string.token_expired_error), false) == true){
			mEtSignInEmail.setText(SpikaApp.getPreferences().getUserEmail());
			mEtSignInPassword.setText(SpikaApp.getPreferences().getUserPassword());
		}
	}

	private void getEmailAndPasswordFromIntent() {
		String passwordFromPrefs = getIntent().getStringExtra(
				"password_from_prefs");
		String emailFromPrefs = getIntent().getStringExtra("email_from_prefs");
		if (passwordFromPrefs != null && emailFromPrefs != null) {
			mEtSignInEmail.setText(emailFromPrefs);
			mEtSignInPassword.setText(passwordFromPrefs);
		}
	}

	private void checkToken() {
		if (isInvalidToken()) {
			final HookUpAlertDialog invalidTokenDialog = new HookUpAlertDialog(
					this);
			invalidTokenDialog.show(getString(R.string.invalid_token_message),
					ButtonType.CLOSE);
		}
	}

	private boolean isInvalidToken() {
		return getIntent().getBooleanExtra("invalid_token", false);
	}

	private void setActiveScreen(Screen activeScreen) {
		mActiveScreen = activeScreen;
		switch (activeScreen) {
		case SIGN_IN:
			mTvTitle.setText(getString(R.string.SIGN_IN));
			mLlSignIn.setVisibility(View.VISIBLE);
			mLlSignUp.setVisibility(View.GONE);
			mBtnActive.setVisibility(View.VISIBLE);
			mBtnInactive.setVisibility(View.VISIBLE);
			mBtnForgotPassword.setVisibility(View.VISIBLE);
			mBtnActive.setText(getString(R.string.SIGN_IN));
			mBtnInactive.setText(getString(R.string.SIGN_UP));
			mBtnBack.setVisibility(View.GONE);
			mLlForgotPassword.setVisibility(View.GONE);
			mBtnSendPassword.setVisibility(View.GONE);
			mBtnActive.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					mSignInEmail = mEtSignInEmail.getText().toString();
					mSignInPassword = mEtSignInPassword.getText().toString();

					if (!mSignInPassword.equals("") && !mSignInEmail.equals("")) {
						CouchDB.authAsync(mSignInEmail, mSignInPassword, new AuthListener(), SignInActivity.this, true);
					}
				}
			});
			mBtnInactive.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					if (mLlSignIn.getVisibility() == View.VISIBLE) {
						SignInActivity.this.setActiveScreen(Screen.SIGN_UP);
					}

				}
			});
			break;
		case SIGN_UP:
			mTvTitle.setText(getString(R.string.SIGN_UP));
			mLlSignUp.setVisibility(View.VISIBLE);
			mLlSignIn.setVisibility(View.GONE);
			mBtnActive.setVisibility(View.VISIBLE);
			mBtnInactive.setVisibility(View.VISIBLE);
			mBtnForgotPassword.setVisibility(View.VISIBLE);
			mBtnActive.setText(getString(R.string.SIGN_UP));
			mBtnInactive.setText(getString(R.string.SIGN_IN));
			mBtnBack.setVisibility(View.GONE);
			mLlForgotPassword.setVisibility(View.GONE);
			mBtnSendPassword.setVisibility(View.GONE);
			mBtnActive.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					mSignUpName = mEtSignUpName.getText().toString();
					mSignUpEmail = mEtSignUpEmail.getText().toString();
					mSignUpPassword = mEtSignUpPassword.getText().toString();

					if (isNameValid(mSignUpName) && isEmailValid(mSignUpEmail) && isPasswordValid(mSignUpPassword)) {
						CouchDB.createUserAsync(mSignUpName, mSignUpEmail, mSignUpPassword, new CreateUserListener(), SignInActivity.this, true);
//						checkAvailability(mSignUpName, mSignUpEmail);
					}

				}
			});
			mBtnInactive.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					if (mLlSignUp.getVisibility() == View.VISIBLE) {
						SignInActivity.this.setActiveScreen(Screen.SIGN_IN);
					}

				}
			});
			break;
		case FORGOT_PASSWORD:
			mTvTitle.setText(getString(R.string.FORGOT_PASSWORD));
			mLlSignUp.setVisibility(View.GONE);
			mLlSignIn.setVisibility(View.GONE);
			mBtnActive.setVisibility(View.GONE);
			mBtnInactive.setVisibility(View.GONE);
			mBtnForgotPassword.setVisibility(View.GONE);
			mBtnBack.setVisibility(View.VISIBLE);
			mLlForgotPassword.setVisibility(View.VISIBLE);
			mBtnSendPassword.setVisibility(View.VISIBLE);
			break;
		default:
			break;
		}
	}

	private boolean isNameValid(String name) {
		String nameResult = Utils.checkName(this, name);
		if (!nameResult.equals(getString(R.string.name_ok))) {

			final HookUpDialog dialog = new HookUpDialog(SignInActivity.this);
			dialog.showOnlyOK(nameResult);

			return false;
		} else {
			return true;
		}
	}

	private boolean isPasswordValid(String password) {
		String passwordResult = Utils.checkPassword(this, password);
		if (!passwordResult.equals(getString(R.string.password_ok))) {

			final HookUpDialog dialog = new HookUpDialog(SignInActivity.this);
			dialog.showOnlyOK(passwordResult);

			return false;

		} else {
			return true;
		}
	}

	private boolean isEmailValid(String email) {
		String emailResult = Utils.checkEmail(this, email);

		if (!emailResult.equals(getString(R.string.email_ok))) {

			final HookUpDialog dialog = new HookUpDialog(SignInActivity.this);
			dialog.showOnlyOK(emailResult);

			return false;
		} else {
			return true;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			if (mActiveScreen == Screen.FORGOT_PASSWORD) {
				setActiveScreen(Screen.SIGN_IN);
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onBackPressed() {
		if (mActiveScreen == Screen.FORGOT_PASSWORD) {
			setActiveScreen(Screen.SIGN_IN);
		} else {
			super.onBackPressed();
		}
	}

	private void signIn() {
		if (UsersManagement.getLoginUser() != null) {
			 CouchDB.findUserActivitySummary(UsersManagement.getLoginUser().getId(), new FindUserActivitySummaryListener(), SignInActivity.this, true);
		}
	}

	public static SignInActivity getInstance() {
		return sInstance;
	}

	@Override
	protected void onDestroy() {
		sInstance = null;
		mLlSignIn = null;
		mLlSignUp = null;
		super.onDestroy();
	}

	private enum Screen {
		SIGN_IN, SIGN_UP, FORGOT_PASSWORD
	}
	
	private class AuthListener implements ResultListener<String>
	{

		@Override
		public void onResultsSucceded(String result) {
			if (result == Const.LOGIN_SUCCESS)
			{
				signIn();
			}
			else
			{
				final HookUpDialog dialog = new HookUpDialog(SignInActivity.this);
				dialog.showOnlyOK(getString(R.string.no_user_registered));
			}
		}

		@Override
		public void onResultsFail() {			
		}
	}
	
	private class CreateUserListener implements ResultListener<String>
	{
		@Override
		public void onResultsSucceded(String result) {
			if (result != null)
			{
				CouchDB.authAsync(mSignUpEmail, mSignUpPassword, new AuthListener(), SignInActivity.this, true);
			}
			else
			{
				final HookUpDialog dialog = new HookUpDialog(SignInActivity.this);
				dialog.showOnlyOK(getString(R.string.an_internal_error_has_occurred));
			}
		}

		@Override
		public void onResultsFail() {
		}
	}
	
	private class FindUserActivitySummaryListener implements ResultListener<ActivitySummary>
	{
		@Override
		public void onResultsSucceded(ActivitySummary result) {
			ActivitySummary loginUserActivitySummary = result;
			UsersManagement.getLoginUser().setActivitySummary(loginUserActivitySummary);
			
			Intent intent = new Intent(SignInActivity.this, RecentActivityActivity.class);
			intent.putExtra(Const.SIGN_IN, true);
			SignInActivity.this.startActivity(intent);

			if (SpikaApp.getPreferences().getPasscodeProtect() == true) {
				Intent passcode = new Intent(SignInActivity.this, PasscodeActivity.class);
				passcode.putExtra("protect", true);
				SignInActivity.this.startActivity(passcode);
			}
			SignInActivity.this.finish();
		}

		@Override
		public void onResultsFail() {
		}
	}
	
	private class SendPasswordListener implements ResultListener<Void>
	{
		@Override
		public void onResultsSucceded(Void result) {
			final HookUpAlertDialog emailSentDialog = new HookUpAlertDialog(
			SignInActivity.this);
			emailSentDialog.show(getString(R.string.email_sent), ButtonType.OK);
		}

		@Override
		public void onResultsFail() {
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == REQUEST_CODE_LIST_SERVERS){
			if(resultCode == RESULT_OK){
				if(data != null){
					mTvSelectServer.setText(data.getStringExtra(Const.SERVER_NAME));
					mTvSelectServer.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
					((SimpleAutoFitTextView)mTvSelectServer).callOnMeasure();
				}
			}
		}
	}
}
