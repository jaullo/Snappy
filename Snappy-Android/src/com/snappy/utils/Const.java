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


/**
 * Const
 * 
 * This class contains all of the constant variables used through the
 * application.
 */
public class Const {

	/* Debug/Production variables */
	public static final boolean IS_DEBUG = true;

	
	
	public static final String SUPPORT_USER				= "1";
	public static final String API_FOLDER				= "api/";
	public static final String INFORMATION_FOLDER		= "page/information/";
//	public static final String API_URL                 	= ConstServer.BASE_URL;
	public static final String AUTH_URL                	= "auth?";
	public static final String SEARCH_USERS_URL        	= "searchUsers?";
	public static final String SEARCH_GROUPS_URL       	= "searchGroups/name/";
	public static final String FILE_UPLOADER_URL       	= "fileuploader";
    public static final String FILE_DOWNLOADER_URL     	= "filedownloader?";
//    public static final String CHECKUNIQUE_URL         	= /*ConstServer.BASE_URL + */"checkUnique?";
    public static final String FIND_USER_BY_NAME       	= "findUser/name/";
    public static final String FIND_USER_BY_EMAIL      	= "findUser/email/";
    public static final String FIND_USER_BY_ID			= "findUser/id/";
    public static final String FIND_USERACTIVITY_SUMMARY= "activitySummary";
    public static final String GET_AVATAR_FILE_ID		= "GetAvatarFileId/";
    public static final String FIND_MESSAGE_BY_ID		= "findMessageById/";
    public static final String FIND_COMMENTS_BY_MESSAGE_ID	= "comments/";
    public static final String COMMENTS_COUNT			= "commentsCount/";
    public static final String FIND_USER_MESSAGES		= "userMessages/";
    public static final String FIND_GROUP_MESSAGES		= "groupMessages/";
    public static final String FIND_ALL_EMOTICONS		= "Emoticons";
    public static final String FIND_GROUP_BY_ID			= "findGroup/id/";
    public static final String FIND_GROUP_BY_NAME		= "findGroup/name/";
    public static final String FIND_GROUP_BY_CATEGORY_ID= "findGroup/categoryId/";
    public static final String FIND_GROUP_CATEGORIES	= "findAllGroupCategory";
    public static final String DEFAULT_SERVER_NAME		= "Snappy Official";    
    public static final String FIND_MEMBERS             = "groupUsers/";
    
    
    public static final String PASSWORDREMINDER_URL    	= "resetPassword?";
    public static final String UNREGISTER_PUSH_URL     	= "unregistToken?";
    public static final String DATABASE                	= "snappy";
    
    public static final String CREATE_USER 				= "create_user";
	public static final String UPDATE_USER 				= "updateUser";
	public static final String SEND_MESSAGE_TO_USER		= "sendMessageToUser";
    public static final String SEND_MESSAGE_TO_GROUP	= "sendMessageToGroup";
    public static final String SEND_COMMENT 			= "sendComment";
    public static final String CREATE_GROUP				= "createGroup";
    public static final String SUBSCRIBE_GROUP 			= "subscribeGroup";
    public static final String UNSUBSCRIBE_GROUP 		= "unSubscribeGroup";
    public static final String DELETE_GROUP 			= "deleteGroup";
    public static final String UPDATE_GROUP				= "updateGroup";
    public static final String WATCH_GROUP				= "watchGroup";
    public static final String UNWATCH_GROUP			= "unWatchGroup";
    public static final String ADD_CONTACT				= "addContact";
    public static final String REMOVE_CONTACT			= "removeContact";
    public static final String SET_DELETE				= "setDelete";
    
    /* List Servers constants */
    public static final String SERVER_NAME = "server_name";
	public static final String SERVER_URL = "server_url";
    
	/* User constants */
	public static final String FAVORITE_GROUPS = "favorite_groups";
	public static final String CONTACTS = "contacts";
	public static final String NAME = "name";
	public static final String PASSWORD = "password";
	public static final String EMAIL = "email";
	public static final String LAST_LOGIN = "last_login";
	public static final String USER = "user";
	public static final String USER_AVATAR = "avatar.jpg";
	public static final String ABOUT = "about";
	public static final String BIRTHDAY = "birthday";
	public static final String GENDER = "gender";
	public static final String MALE = "male";
	public static final String FEMALE = "female";
	public static final String ONLINE = "online";
	public static final String OFFLINE = "offline";
	public static final String AWAY = "away";
	public static final String BUSY = "busy";
	public static final String ANDROID_PUSH_TOKEN = "android_push_token";
	public static final String TOKEN = "token";
	public static final String TOKEN_TIMESTAMP = "token_timestamp";
	public static final String AVATAR_NAME = "avatar_name";
	public static final String ONLINE_STATUS = "online_status";
	public static final String MAX_CONTACT_COUNT = "max_contact_count";
	public static final String MAX_FAVORITE_COUNT = "max_favorite_count";
	
	public static final int MAX_CONTACTS = 200;
	public static final int MAX_FAVORITES = 60;

	/* Message constants */
	public static final String MESSAGE_TYPE = "message_type";
	public static final String TEXT = "text";
	public static final String IMAGE = "image";
	public static final String VOICE = "voice";
	public static final String VIDEO = "video";
	public static final String LOCATION = "location";
    public static final String EMOTICON = "emoticon";
    public static final String NEWS = "news";
	public static final String MODIFIED = "modified";
	public static final String FROM_USER_NAME = "from_user_name";
	public static final String FROM_USER_ID = "from_user_id";
	public static final String VALID = "valid";
	public static final String MESSAGE_TARGET_TYPE = "message_target_type";
	public static final String CREATED = "created";
	public static final String TO_USER_NAME = "to_user_name";
	public static final String TO_USER_ID = "to_user_id";
	public static final String BODY = "body";
	public static final String LATITUDE = "latitude";
	public static final String LONGITUDE = "longitude";
	public static final String COMMENTS = "comments";
	public static final String EMOTICON_IMAGE_URL = "emoticon_image_url";
	public static final String _ATTACHMENTS = "_attachments";
	public static final String TO_GROUP_ID = "to_group_id";
	public static final String TO_GROUP_NAME = "to_group_name";
	public static final String USER_IMAGE_URL = "user_image_url";
	public static final String DELETE_TYPE = "delete_type";
	public static final String DELETE_AT = "delete_at";
	public static final String READ_AT = "read_at";
	public static final String COMMENT_COUNT = "comment_count";
	
	/* Group constants */
	public static final String GROUP = "group";
	public static final String DESCRIPTION = "description";
	public static final String USER_ID = "user_id";
	public static final String GROUP_AVATAR = "group_image.jpg";
	public static final String GROUP_PASSWORD= "group_password";
	public static final String CATEGORY_ID = "category_id";
	public static final String CATEGORY_NAME = "category_name";
	public static final String DELETED = "deleted";
	
	/* Group category constants */
	public static final String GROUP_CATEGORY = "group_category";
	public static final String TITLE = "title";
	public static final String GROUP_CATEGORY_AVATAR = "picture.png";
	
	/* User group constants */
	public static final String USER_GROUP = "user_group";
	public static final String GROUP_ID = "group_id";
	public static final String USER_NAME = "user_name";

	/* General constants */
	public static final String ROWS = "rows";
	public static final String KEY = "key";
	public static final String VALUE = "value";
	public static final String ATTACHMENTS = "_attachments";
	public static final String OK = "ok";
	public static final String REV = "rev";
	public static final String ID = "id";
	public static final String DOC = "doc";
	public static final String TYPE = "type";
	public static final String NULL = "null";
	public static final String _ID = "_id";
	public static final String _REV = "_rev";
	public static final String DATA = "data";
	public static final String CONTENT_TYPE = "content_type";
	public static final String REVPOS = "revpos";
	public static final String STUB = "stub";
	public static final String LENGTH = "length";
	
	
	/* Recent activity constants */
	public static final String ACTIVITY_SUMMARY = "activity_summary";
	public static final String RECENT_ACTIVITY = "recent_activity";
	public static final String TARGET_ID = "target_id";
	public static final String TARGET_TYPE = "target_type";
	public static final String NOTIFICATIONS = "notifications";
	public static final String MESSAGES = "messages";
	public static final String MESSAGE = "message";
	public static final String COUNT = "count";
	
	/* Comment constants */
	public static final String COMMENT = "comment";
	public static final String MESSAGE_ID = "message_id";
	
	/* Push notification constants */
	public static final String PUSH_SENDER_ID = "932669755483";
	public static final String PUSH_MESSAGE = "message";
	public static final String PUSH_FROM_USER_ID = "fromUser";
	public static final String PUSH_FROM_NAME= "fromUserName";
	public static final String PUSH_FROM_MESSAGE_FLAG= "messageFlag";
	public static final String PUSH_FROM_MESSAGE_BODY= "messageBody";
	public static final String PUSH_FROM_GROUP_ID = "groupId";
	public static final String PUSH_FROM_TYPE = "type";
	public static final String PUSH_TYPE_USER = "user";
	public static final String PUSH_TYPE_GROUP = "group";
	public static final String PUSH_FROM_USER_EMAIL = "fromEmail";
	
	public static final String SIGN_IN = "sign_in";
	public static final String PUSH_INTENT = "push_intent";

	/* hookup://user/[ime korisnika] URI constant */
	public static final String USER_URI_INTENT = "user_uri_intent";
	public static final String USER_URI_NAME = "user_uri_name";
	
	/* hookup://group/[ime grupe] URI constant */
	public static final String GROUP_URI_INTENT = "group_uri_intent";
	public static final String GROUP_URI_NAME = "group_uri_name";
	
	/* Login error */
	public static final String LOGIN_ERROR = "login";
	public static final String LOGIN_SUCCESS = "login_success";
	
	/* Crittercism constants */
	public static final String CRITTERCISM_APP_ID = "51938f2e97c8f20789000010";
	
	/* Display image size constants */
	public static final int PROFILE_REQUIRED_SIZE = 400;
	public static final int LIST_AVATAR_REQUIRED_SIZE = 140;
    public static final int PICTURE_SIZE = 640;
    public static final int AVATAR_THUMB_SIZE = 120;
    public static final int PICTURE_THUMB_SIZE = 240;
	
	/* File handler constants */
	public static final String FILE = "file";
    public static final String AVATAR_FILE_ID = "avatar_file_id";
    public static final String AVATAR_THUMB_FILE_ID = "avatar_thumb_file_id";
    public static final String PICTURE_FILE_ID = "picture_file_id";
    public static final String PICTURE_THUMB_FILE_ID = "picture_thumb_file_id";
	public static final String VOICE_FILE_ID = "voice_file_id";
	public static final String VIDEO_FILE_ID = "video_file_id";
	public static final String TMP_BITMAP_FILENAME = "hutmpfile.jpeg";
	public static final String UPLOADS = "uploads";
	
	/* Recording constants */
	public static final long MAX_RECORDING_TIME_VIDEO = 600; // seconds
	public static final long MAX_RECORDING_TIME_VOICE = 600000; // milliseconds
	
	/* Watching group log constants */
	public static final String WATCHING_GROUP_LOG = "watching_group_log";
	
	/* Error constants */
	public static final String ERROR = "error";
	public static final String INVALID_TOKEN = "Invalid token";



}

