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

package com.snappy.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Preferences
 * 
 * Holds and managed application's preferences.
 */

public class Preferences {

	// Defining SharedPreferences entries
	private static final String USER_PASSWORD = "password";
	private static final String USER_EMAIL = "email";
	private static final String USER_PUSH_TOKEN = "user_push_token";
    private static final String USER_TOKEN = "user_token";
    private static final String USER_ID = "user_id";
	private static final String PASSCODE = "passcode";
	private static final String PASSCODE_PROTECT = "passcode_protect";
	private static final String SHOW_TUTORIAL = "show_tutorial";
	private static final String SHOW_TUTORIAL_BOOT = "show_tutorial_BOOT";
	private static final String WATCHING_GROUP_ID = "watching_group_id";
	private static final String WATCHING_GROUP_REV = "watching_group_rev";
	private static final String GROUP_PASSWORD = "GROUP_PASSWORD";
	private static final String USER_SERVER_URL = "USER_SERVER_URL";
	private static final String USER_SERVER_NAME = "USER_SERVER_NAME";
	private static final String USER_PREFERRED_LANGUAGE = "USER_PREFERRED_LANGUAGE";
	

	private SharedPreferences sharedPreferences;

	public Preferences(Context context) {

		sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(context);
	}

		
	public String getUserPassword() {
		return sharedPreferences.getString(USER_PASSWORD, "");
	}

	public void setUserPassword(String pass) {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(USER_PASSWORD, pass);
		editor.commit();
	}

	public String getUserEmail() {
		return sharedPreferences.getString(USER_EMAIL, "");
	}

	public void setUserEmail(String email) {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(USER_EMAIL, email);
		editor.commit();
	}

	public String getUserPushToken() {
		return sharedPreferences.getString(USER_PUSH_TOKEN, "");
	}

	public void setUserPushToken(String pushToken) {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(USER_PUSH_TOKEN, pushToken);
		editor.commit();
	}

	public String getPasscode() {
		return sharedPreferences.getString(PASSCODE, "");
	}

	public void setPasscode(String passcode) {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(PASSCODE, passcode);
		editor.commit();
	}

	public boolean getPasscodeProtect() {
		return sharedPreferences.getBoolean(PASSCODE_PROTECT, false);
	}

	public void setPasscodeProtect(boolean hasPasscodeProtect) {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putBoolean(PASSCODE_PROTECT, hasPasscodeProtect);
		editor.commit();
	}

	public String getUserToken() {
		return sharedPreferences.getString(USER_TOKEN, "");
	}

	public void setUserToken(String token) {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(USER_TOKEN, token);
		editor.commit();
	}

	public boolean getShowTutorial(String activityName) {
		return sharedPreferences.getBoolean(SHOW_TUTORIAL + activityName, true);
	}

	public void setShowTutorial(boolean showTutorial, String activityName) {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putBoolean(SHOW_TUTORIAL + activityName, showTutorial);
		editor.commit();
	}

	public boolean getShowTutorialForBoot(String activityName) {

		Logger.debug("tag", SHOW_TUTORIAL_BOOT + activityName);
		return sharedPreferences.getBoolean(SHOW_TUTORIAL_BOOT + activityName,
				true);
	}

	public void setShowTutorialForBoot(boolean showTutorial, String activityName) {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putBoolean(SHOW_TUTORIAL_BOOT + activityName, showTutorial);
		editor.commit();
	}

	public void clearFlagsForTutorialEachBoot(String packageName) {

		this.setShowTutorialForBoot(true, packageName + ".UsersActivity");
		this.setShowTutorialForBoot(true, packageName + ".GroupsActivity");

	}

	public String getWatchingGroupId() {
		return sharedPreferences.getString(WATCHING_GROUP_ID, "");
	}

	public void setWatchingGroupId(String id) {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(WATCHING_GROUP_ID, id);
		editor.commit();
	}

	public String getWatchingGroupRev() {
		return sharedPreferences.getString(WATCHING_GROUP_REV, "");
	}

	public void setWatchingGroupRev(String rev) {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(WATCHING_GROUP_REV, rev);
		editor.commit();
	}

    public void setUserId(String id) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(USER_ID, id);
        editor.commit();
    }
    public String getUserId() {
        return sharedPreferences.getString(USER_ID, "");
    }

    public void savePasswordForGroup (String groupId, String password) {
    	SharedPreferences.Editor editor = sharedPreferences.edit();
    	editor.putString(GROUP_PASSWORD + groupId, password);
    	editor.commit();
    }
    
    public String getSavedPasswordForGroup (String groupId) {
    	return sharedPreferences.getString(GROUP_PASSWORD + groupId, "");
    }
    
    public void setUserServerURL (String url) {
    	SharedPreferences.Editor editor = sharedPreferences.edit();
    	editor.putString(USER_SERVER_URL, url);
    	editor.commit();
    }
    
    public String getUserServerURL () {
    	return sharedPreferences.getString(USER_SERVER_URL, ConstServer.BASE_URL);
    }
    
    public void setUserServerName (String name) {
    	SharedPreferences.Editor editor = sharedPreferences.edit();
    	editor.putString(USER_SERVER_NAME, name);
    	editor.commit();
    }
    
    public String getUserServerName () {
    	return sharedPreferences.getString(USER_SERVER_NAME, Const.DEFAULT_SERVER_NAME);
    }
    
    public void setUserPreferredLanguage (String language) {
    	SharedPreferences.Editor editor = sharedPreferences.edit();
    	editor.putString(USER_PREFERRED_LANGUAGE, language);
    	editor.commit();
    }
    
    public String getUserPreferredLaguage () {
    	return sharedPreferences.getString(USER_PREFERRED_LANGUAGE, "");
    }
    
}
