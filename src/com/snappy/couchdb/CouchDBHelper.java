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

package com.snappy.couchdb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.snappy.couchdb.model.ActivitySummary;
import com.snappy.couchdb.model.Attachment;
import com.snappy.couchdb.model.Comment;
import com.snappy.couchdb.model.Emoticon;
import com.snappy.couchdb.model.Group;
import com.snappy.couchdb.model.GroupCategory;
import com.snappy.couchdb.model.Member;
import com.snappy.couchdb.model.Message;
import com.snappy.couchdb.model.Notification;
import com.snappy.couchdb.model.NotificationMessage;
import com.snappy.couchdb.model.RecentActivity;
import com.snappy.couchdb.model.Server;
import com.snappy.couchdb.model.User;
import com.snappy.couchdb.model.UserGroup;
import com.snappy.extendables.SideBarActivity;
import com.snappy.management.UsersManagement;
import com.snappy.utils.Const;
import com.snappy.utils.Logger;

/**
 * CouchDBHelper
 * 
 * Used for parsing JSON response from server.
 */
public class CouchDBHelper {

	private static String TAG = "CouchDbHelper: ";

	private static final Gson sGsonExpose = new GsonBuilder()
			.excludeFieldsWithoutExposeAnnotation().create();

	/**
	 * Parse a single user JSON object
	 * 
	 * @param json
	 * @return
	 * @throws JSONException
	 */
	public static User parseSingleUserObject(JSONObject json)
			throws JSONException {
		User user = null;
		ArrayList<String> contactsIds = new ArrayList<String>();

		if (json != null) {
			
			if (json.has(Const.ERROR)) {
				appLogout(null, false, isInvalidToken(json));
				return null;
			}

			JSONArray rows = json.getJSONArray(Const.ROWS);
			JSONObject row = rows.getJSONObject(0);
			JSONObject userJson = row.getJSONObject(Const.VALUE);

			user = sGsonExpose.fromJson(userJson.toString(), User.class);
			
			if (userJson.has(Const.FAVORITE_GROUPS)) {
				JSONArray favorite_groups = userJson
						.getJSONArray(Const.FAVORITE_GROUPS);

				List<String> groups = new ArrayList<String>();

				for (int i = 0; i < favorite_groups.length(); i++) {
					groups.add(favorite_groups.getString(i));
				}

				user.setGroupIds(groups);
			}

			if (userJson.has(Const.CONTACTS)) {
				JSONArray contacts = userJson.getJSONArray(Const.CONTACTS);

				for (int i = 0; i < contacts.length(); i++) {
					contactsIds.add(contacts.getString(i));
				}

				user.setContactIds(contactsIds);
			}
		}

		return user;
	}

	   /**
     * Parse a single user JSON object
     * 
     * @param json
     * @return
     * @throws JSONException
	 * @throws SpikaException 
     */
    public static User parseSingleUserObjectWithoutRowParam(JSONObject userJson)
            throws JSONException, SpikaException {
        User user = null;
        ArrayList<String> contactsIds = new ArrayList<String>();

        if (userJson != null) {
        	
        	if (userJson.length() == 0) {
        		return null;
        	}
        	
        	if (userJson.has(Const.ERROR)) {
				appLogout(null, false, isInvalidToken(userJson));
				throw new SpikaException(ConnectionHandler.getError(userJson));
			}

            user = sGsonExpose.fromJson(userJson.toString(), User.class);
            
            if (userJson.has(Const.FAVORITE_GROUPS)) {
                JSONArray favorite_groups = userJson
                        .getJSONArray(Const.FAVORITE_GROUPS);

                List<String> groups = new ArrayList<String>();

                for (int i = 0; i < favorite_groups.length(); i++) {
                    groups.add(favorite_groups.getString(i));
                }

                user.setGroupIds(groups);
            }

            if (userJson.has(Const.CONTACTS)) {
                JSONArray contacts = userJson.getJSONArray(Const.CONTACTS);

                for (int i = 0; i < contacts.length(); i++) {
                    contactsIds.add(contacts.getString(i));
                }

                user.setContactIds(contactsIds);
            }
        }

        return user;
    }
    
	/**
	 * Parse multi JSON objects of type user
	 * 
	 * @param json
	 * @return
	 * @throws JSONException 
	 */
	public static List<User> parseMultiUserObjects(JSONObject json) throws JSONException {

		List<User> users = null;
		ArrayList<String> contactsIds = new ArrayList<String>();

		if (json != null) {
			
			if (json.has(Const.ERROR)) {
				appLogout(null, false, isInvalidToken(json));
				return null;
			}

			users = new ArrayList<User>();

			// Get the element that holds the users ( JSONArray )
			JSONArray rows = json.getJSONArray(Const.ROWS);

			for (int i = 0; i < rows.length(); i++) {

				JSONObject row = rows.getJSONObject(i);
				JSONObject userJson = row.getJSONObject(Const.VALUE);

				User user = new User();

				user = sGsonExpose
						.fromJson(userJson.toString(), User.class);

				if (userJson.has(Const.CONTACTS)) {

					JSONArray contacts = userJson
							.getJSONArray(Const.CONTACTS);

					for (int j = 0; j < contacts.length(); j++) {
						contactsIds.add(contacts.getString(j));
					}

					user.setContactIds(contactsIds);
				}

				if (userJson.has(Const.FAVORITE_GROUPS)) {
					JSONArray favorite_groups = userJson
							.getJSONArray(Const.FAVORITE_GROUPS);

					List<String> groups = new ArrayList<String>();

					for (int k = 0; k < favorite_groups.length(); k++) {
						groups.add(favorite_groups.getString(k));
					}

					user.setGroupIds(groups);
				}

				users.add(user);
			}
		}

		return users;
	}

	/**
	 * Parse multi JSON objects of type user for search users
	 * 
	 * @param json
	 * @return
	 * @throws JSONException 
	 */
	public static List<User> parseSearchUsersResult(JSONArray jsonArray) throws JSONException {

		List<User> users = null;
		ArrayList<String> contactsIds = new ArrayList<String>();

		if (jsonArray != null) {

			users = new ArrayList<User>();

			// Get the element that holds the users ( JSONArray )

			for (int i = 0; i < jsonArray.length(); i++) {

				JSONObject userJson = jsonArray.getJSONObject(i);

				User user = new User();

				user = sGsonExpose
						.fromJson(userJson.toString(), User.class);


				if (userJson.has(Const.CONTACTS)) {

					JSONArray contacts = userJson
							.getJSONArray(Const.CONTACTS);

					for (int j = 0; j < contacts.length(); j++) {
						contactsIds.add(contacts.getString(j));
					}

					user.setContactIds(contactsIds);
				}

				if (userJson.has(Const.FAVORITE_GROUPS)) {
					JSONArray favorite_groups = userJson
							.getJSONArray(Const.FAVORITE_GROUPS);

					List<String> groups = new ArrayList<String>();

					for (int k = 0; k < favorite_groups.length(); k++) {
						groups.add(favorite_groups.getString(k));
					}

					user.setGroupIds(groups);
				}

				users.add(user);
			}
		}

		return users;
	}

	/**
	 * Parse multi JSON objects of type group for search groups
	 * 
	 * @param json
	 * @return
	 * @throws JSONException 
	 */
	public static List<Group> parseSearchGroupsResult(JSONArray jsonArray) throws JSONException {

		List<Group> groups = null;

		if (jsonArray != null) {

			groups = new ArrayList<Group>();

			// Get the element that holds the groups ( JSONArray )

			for (int i = 0; i < jsonArray.length(); i++) {

				JSONObject groupJson = jsonArray.getJSONObject(i);

				Group group = new Group();

				group = sGsonExpose.fromJson(groupJson.toString(),
						Group.class);

				groups.add(group);
			}
			
		}

		return groups;
	}

	/**
	 * Parses a single activity summary JSON object
	 * 
	 * @param json
	 * @return
	 * @throws JSONException
	 * @throws SpikaException 
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	public static ActivitySummary parseSingleActivitySummaryObject(
			JSONObject json) throws JSONException, ClientProtocolException, IOException, SpikaException {

		ActivitySummary activitySummary = null;

		if (json != null) {
			
			if (json.has(Const.ERROR)) {
				appLogout(null, false, isInvalidToken(json));
				return null;
			}

			JSONArray rows = json.getJSONArray(Const.ROWS);

			if (rows.length() > 0) {
				JSONObject row = rows.getJSONObject(0);
				JSONObject activitySummaryJson = row
						.getJSONObject(Const.VALUE);

				activitySummary = new ActivitySummary();
				activitySummary = sGsonExpose.fromJson(
						activitySummaryJson.toString(),
						ActivitySummary.class);

				if (activitySummaryJson.has(Const.RECENT_ACTIVITY)) {
					JSONObject recentActivityListJson = activitySummaryJson
							.getJSONObject(Const.RECENT_ACTIVITY);
					List<RecentActivity> recentActivityList = CouchDBHelper
							.parseMultiRecentActivityObjects(recentActivityListJson);
					activitySummary
							.setRecentActivityList(recentActivityList);
				}
			}
		}

		return activitySummary;
	}

	/**
	 * Parses multi RecentActivity JSON Objects
	 * 
	 * @param recentActivityListJson
	 * @return
	 * @throws SpikaException 
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	public static List<RecentActivity> parseMultiRecentActivityObjects(
			JSONObject recentActivityListJson) throws ClientProtocolException, IOException, SpikaException {

		List<RecentActivity> recentActivityList = new ArrayList<RecentActivity>();

		@SuppressWarnings("unchecked")
		Iterator<String> iterator = recentActivityListJson.keys();
		while (iterator.hasNext()) {
			String key = iterator.next();
			try {
				JSONObject recentActivityJson = recentActivityListJson
						.getJSONObject(key);
				RecentActivity recentActivity = new RecentActivity();
				recentActivity = sGsonExpose.fromJson(
						recentActivityJson.toString(), RecentActivity.class);

				if (recentActivityJson.has(Const.NOTIFICATIONS)) {
					JSONArray notificationsJson = recentActivityJson
							.getJSONArray(Const.NOTIFICATIONS);
					recentActivity
							.set_notifications(parseMultiNotificationObjects(notificationsJson));
				}
				recentActivityList.add(recentActivity);

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return recentActivityList;
	}

	/**
	 * Parses multi notification objects
	 * 
	 * @param notificationsJson
	 * @return
	 * @throws SpikaException 
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	public static List<Notification> parseMultiNotificationObjects(
			JSONArray notificationsAry) throws ClientProtocolException, IOException, SpikaException {

		List<Notification> notifications = new ArrayList<Notification>();

		for(int i = 0 ; i < notificationsAry.length() ; i++){

			try {
				JSONObject notificationJson = (JSONObject) notificationsAry.get(i);
				Notification notification = new Notification();
				notification = sGsonExpose.fromJson(
						notificationJson.toString(), Notification.class);

				if (notificationJson.has(Const.MESSAGES)) {
					JSONArray messagesAry = notificationJson
							.getJSONArray(Const.MESSAGES);
					notification
							.setMessages(parseMultiNotificationMessageObjects(
							        messagesAry, notification.getTargetId()));
				}

				notifications.add(notification);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return notifications;
	}

	/**
	 * Parses multi notification message objects
	 * 
	 * @param messagesJson
	 * @return
	 * @throws SpikaException 
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	public static List<NotificationMessage> parseMultiNotificationMessageObjects(
			JSONArray messagesArray, String targetId) throws ClientProtocolException, IOException, SpikaException {

		List<NotificationMessage> messages = new ArrayList<NotificationMessage>();

		for(int i = 0; i < messagesArray.length() ; i++){
			try {
				JSONObject messageJson = (JSONObject) messagesArray.get(i);
				NotificationMessage notificationMessage = new NotificationMessage();
				notificationMessage = sGsonExpose.fromJson(
						messageJson.toString(), NotificationMessage.class);
				notificationMessage.setTargetId(targetId);
//				notificationMessage.setUserAvatarFileId(CouchDB
//						.findAvatarFileId(notificationMessage.getFromUserId()));
				messages.add(notificationMessage);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return messages;
	}

	/**
	 * Parse user JSON objects from get user contacts call
	 * 
	 * @param json
	 * @return
	 * @throws JSONException 
	 */
	public static List<User> parseUserContacts(JSONObject json) throws JSONException {

		List<User> users = null;

		if (json != null) {
			
			if (json.has(Const.ERROR)) {
				appLogout(null, false, isInvalidToken(json));
				return null;
			}

			users = new ArrayList<User>();

			// Get the element that holds the users ( JSONArray )
			JSONArray rows = json.getJSONArray(Const.ROWS);

			for (int i = 0; i < rows.length(); i++) {

				JSONObject row = rows.getJSONObject(i);
				if (!row.isNull(Const.DOC)) {
					JSONObject userJson = row.getJSONObject(Const.DOC);

					User user = new User();

					user = sGsonExpose.fromJson(userJson.toString(),
							User.class);

					if (userJson.has(Const.FAVORITE_GROUPS)) {
						JSONArray favorite_groups = userJson
								.getJSONArray(Const.FAVORITE_GROUPS);

						List<String> groups = new ArrayList<String>();

						for (int z = 0; z < favorite_groups.length(); z++) {
							groups.add(favorite_groups.getString(z));
						}

						user.setGroupIds(groups);
					}

					if (userJson.has(Const.CONTACTS)) {
						JSONArray contacts = userJson
								.getJSONArray(Const.CONTACTS);

						List<String> contactsIds = new ArrayList<String>();

						for (int j = 0; j < contacts.length(); j++) {
							contactsIds.add(contacts.getString(j));
						}
						user.setContactIds(contactsIds);
					}

					users.add(user);
				}
			}
		}

		return users;
	}

	/**
	 * Parse comment JSON objects from get message comments
	 * 
	 * @param json
	 * @return
	 * @throws JSONException 
	 */
	public static List<Comment> parseMessageComments(JSONObject json) throws JSONException {

		List<Comment> comments = null;

		if (json != null) {
			
			if (json.has(Const.ERROR)) {
				appLogout(null, false, isInvalidToken(json));
				return null;
			}

			comments = new ArrayList<Comment>();

			// Get the element that holds the users ( JSONArray )
			JSONArray rows = json.getJSONArray(Const.ROWS);

			for (int i = 0; i < rows.length(); i++) {

				JSONObject row = rows.getJSONObject(i);
				if (!row.isNull(Const.DOC)) {
					JSONObject commentJson = row.getJSONObject(Const.DOC);

					Comment comment = new Comment();
					comment = sGsonExpose.fromJson(commentJson.toString(),
							Comment.class);
					comments.add(comment);
				}
			}
		}

		return comments;
	}

	/**
	 * Create user response object
	 * 
	 * @param json
	 * @return
	 * @throws JSONException 
	 */
	public static String createUser(JSONObject json) throws JSONException {

		boolean ok = false;
		String id = null;

		if (json != null) {
			
			if (json.has(Const.ERROR)) {
				appLogout(null, false, isInvalidToken(json));
				return null;
			}

			ok = json.getBoolean(Const.OK);
			id = json.getString(Const.ID);
		}

		if (!ok) {
			Logger.error(TAG + "createUser", "error in creating user");
		}

		return id;
	}

	/**
	 * Update user response object, the Const.REV value is important in order to
	 * continue using the application
	 * 
	 * If you are updating contacts or favorites on of them should be null
	 * 
	 * @param json
	 * @return
	 * @throws JSONException 
	 */
	public static boolean updateUser(JSONObject json, List<String> contactsIds,
			List<String> groupsIds) throws JSONException {

		String rev = "";

		if (json != null) {

			if (json.has(Const.ERROR)) {
				appLogout(false, true, isInvalidToken(json));
				return false;
			}

			rev = json.getString(Const._REV);

			UsersManagement.getLoginUser().setRev(rev);

			if (null != contactsIds) {
				UsersManagement.getLoginUser().setContactIds(
						contactsIds);
			}

			if (null != groupsIds) {
				UsersManagement.getLoginUser().setGroupIds(groupsIds);
			}

			return true;
		}

		return false;
	}

	/**
	 * JSON response from creating a group
	 * 
	 * @param json
	 * @return
	 * @throws JSONException 
	 */
	public static String createGroup(JSONObject json) throws JSONException {

		boolean ok = false;
		String id = null;

		if (json != null) {

			if (json.has(Const.ERROR)) {
				appLogout(null, false, isInvalidToken(json));
				return null;
			}

			ok = json.getInt(Const.OK) == 1 ? true : false;
			id = json.getString(Const.ID);
		}

		if (!ok) {
			Logger.error(TAG + "createGroup", "error in creating a group");
			return null;
		}

		return id;
	}

	/**
	 * JSON response from deleting a group
	 * 
	 * @param json
	 * @return
	 * @throws JSONException 
	 */
	public static boolean deleteGroup(JSONObject json) throws JSONException {

		boolean ok = false;

		if (json != null) {

		    if (json.has(Const.ERROR)) {
				appLogout(false, false, isInvalidToken(json));
				return false;
			}
			
			ok = json.getBoolean(Const.OK);
		}

		return ok;
	}

	public static String findAvatarFileId(JSONObject json) throws JSONException {
		String avatarFileId = null;

		if (json != null) {

			if (json.has(Const.ERROR)) {
				appLogout(null, false, isInvalidToken(json));
				return null;
			}

			JSONArray rows = json.getJSONArray(Const.ROWS);

			for (int i = 0; i < rows.length(); i++) 
			{
				JSONObject row = rows.getJSONObject(i);
				avatarFileId = row.getString(Const.VALUE);
			}
		}

		return avatarFileId;
	}

	/**
	 * JSON response from deleting a user group
	 * 
	 * @param json
	 * @return
	 * @throws JSONException 
	 */
	public static boolean deleteUserGroup(JSONObject json) throws JSONException {

		boolean ok = false;

		if (json != null) {

			if (json.has(Const.ERROR)) {
				appLogout(false, false, isInvalidToken(json));
				return false;
			}

			ok = json.getBoolean(Const.OK);
		}

		return ok;
	}

	/**
	 * JSON response from creating a user group
	 * 
	 * @param json
	 * @return
	 * @throws JSONException 
	 */
	public static String createUserGroup(JSONObject json) throws JSONException {

		boolean ok = false;
		String id = null;

		if (json != null) {

			if (json.has(Const.ERROR)) {
				appLogout(null, false, isInvalidToken(json));
				return null;
			}

			ok = json.getBoolean(Const.OK);
			id = json.getString(Const.ID);
		}

		if (!ok) {
			Logger.error(TAG + "createUserGroup", "error in creating a group");
			return null;
		}

		return id;
	}

	/**
	 * JSON response from creating a comment
	 * 
	 * @param json
	 * @return
	 * @throws JSONException 
	 */
	public static String createComment(JSONObject json) throws JSONException {

		boolean ok = false;
		String id = null;

		if (json != null) {

			if (json.has(Const.ERROR)) {
				appLogout(null, false, isInvalidToken(json));
				return null;
			}

			ok = json.getInt(Const.OK) == 1 ? true : false;
			id = json.getString(Const.ID);
		}

		if (!ok) {
			Logger.error(TAG + "createComment", "error in creating comment");
			return null;
		}

		return id;
	}

	/**
	 * JSON response from updating a group you own
	 * 
	 * @param json
	 * @return
	 * @throws JSONException 
	 */
	public static boolean updateGroup(JSONObject json) throws JSONException {

		boolean ok = false;

		if (json != null) {

			if (json.has(Const.ERROR)) {
				appLogout(false, false, isInvalidToken(json));
				return false;
			}

			
			ok = json.getInt(Const.OK) == 1 ? true : false;

			/* Important */
			UsersManagement.getToGroup().setRev(json.getString(Const.REV));
		}

		if (!ok) {
			Logger.error(TAG + "updateGroup", "error in updating a group");
		}

		return ok;
	}

	/**
	 * Parse single JSON object of type Group
	 * 
	 * @param json
	 * @return
	 * @throws JSONException 
	 */
	public static Group parseSingleGroupObject(JSONObject json) throws JSONException {

		Group group = null;

		if (json != null) {
			
			if (json.has(Const.ERROR)) {
				appLogout(null, false, isInvalidToken(json));
				return null;
			}

			JSONArray rows = json.getJSONArray(Const.ROWS);
			JSONObject row = rows.getJSONObject(0);

			JSONObject groupJson = row.getJSONObject(Const.VALUE);
			group = sGsonExpose.fromJson(groupJson.toString(), Group.class);
		}

		return group;
	}

	   /**
     * Parse single JSON object of type Group
     * 
     * @param json
     * @return
     */
    public static Group parseSingleGroupObjectWithoutRowParam(JSONObject json) {

        Group group = null;

        if (json != null) {
        	
        	if (json.has(Const.ERROR)) {
				appLogout(null, false, isInvalidToken(json));
				return null;
			}

        	if (json.length() == 0) {
        		return null;
        	}
        	
        	if (json.has(Const.NAME)) {
        		group = sGsonExpose.fromJson(json.toString(), Group.class);   
        	}            
        }

        return group;
    }
    
	/**
	 * Parse multi JSON objects of type Group
	 * 
	 * @param json
	 * @return
	 * @throws JSONException 
	 */
	public static List<Group> parseMultiGroupObjects(JSONObject json) throws JSONException {

		List<Group> groups = null;

		if (json != null) {

			if (json.has(Const.ERROR)) {
				appLogout(null, false, isInvalidToken(json));
				return null;
			}

			groups = new ArrayList<Group>();

			JSONArray rows = json.getJSONArray(Const.ROWS);

			for (int i = 0; i < rows.length(); i++) {

				JSONObject row = rows.getJSONObject(i);
				String key = row.getString(Const.KEY);

				if (!key.equals(Const.NULL)) {

					JSONObject groupJson = row.getJSONObject(Const.VALUE);

					Group group = sGsonExpose.fromJson(
							groupJson.toString(), Group.class);

					groups.add(group);
				}
			}
		}

		return groups;
	}

	/**
	 * Parse favorite groups JSON objects
	 * 
	 * @param json
	 * @return
	 * @throws JSONException 
	 */
	public static List<Group> parseFavoriteGroups(JSONObject json) throws JSONException {

		List<Group> groups = null;

		if (json != null) {

			if (json.has(Const.ERROR)) {
				appLogout(null, false, isInvalidToken(json));
				return null;
			}
			
			groups = new ArrayList<Group>();

			JSONArray rows = json.getJSONArray(Const.ROWS);

			for (int i = 0; i < rows.length(); i++) {

				JSONObject row = rows.getJSONObject(i);

				JSONObject groupJson = row.getJSONObject(Const.DOC);

				String type = groupJson.getString(Const.TYPE);
				if (!type.equals(Const.GROUP)) {
					continue;
				}

				Group group = sGsonExpose.fromJson(
						groupJson.toString(), Group.class);

				groups.add(group);	
			}
		}

		return groups;
	}

	/**
	 * Parse multi JSON objects of type UserGroup
	 * 
	 * @param json
	 * @return
	 * @throws JSONException 
	 */
	public static List<UserGroup> parseMultiUserGroupObjects(JSONObject json) throws JSONException {

		List<UserGroup> usersGroup = null;

		if (json != null) {

			if (json.has(Const.ERROR)) {
				appLogout(null, false, isInvalidToken(json));
				return null;
			}

			usersGroup = new ArrayList<UserGroup>();

			JSONArray rows = json.getJSONArray(Const.ROWS);

			for (int i = 0; i < rows.length(); i++) {

				JSONObject row = rows.getJSONObject(i);
				String key = row.getString(Const.KEY);

				if (!key.equals(Const.NULL)) {

					JSONObject userGroupJson = row
							.getJSONObject(Const.VALUE);

					UserGroup userGroup = sGsonExpose.fromJson(
							userGroupJson.toString(), UserGroup.class);
					usersGroup.add(userGroup);
				}
			}
			
		}

		return usersGroup;
	}
	
	/**
	 * Parse multi JSON objects of type GroupCategory
	 * 
	 * @param json
	 * @return
	 * @throws JSONException 
	 */
	public static List<GroupCategory> parseMultiGroupCategoryObjects(JSONObject json) throws JSONException {
		List<GroupCategory> groupCategories = null;

		if (json != null) {

			if (json.has(Const.ERROR)) {
				appLogout(null, false, isInvalidToken(json));
				return null;
			}

			groupCategories = new ArrayList<GroupCategory>();

			JSONArray rows = json.getJSONArray(Const.ROWS);

			for (int i = 0; i < rows.length(); i++) {

				JSONObject row = rows.getJSONObject(i);
				String key = row.getString(Const.KEY);

				if (!key.equals(Const.NULL)) {

					JSONObject groupCategoryJson = row.getJSONObject(Const.VALUE);

					GroupCategory groupCategory = sGsonExpose.fromJson(
							groupCategoryJson.toString(), GroupCategory.class);

					if (groupCategoryJson.has(Const.ATTACHMENTS)) {
						List<Attachment> attachments = new ArrayList<Attachment>();

						JSONObject json_attachments = groupCategoryJson
								.getJSONObject(Const.ATTACHMENTS);

						@SuppressWarnings("unchecked")
						Iterator<String> keys = json_attachments.keys();
						while (keys.hasNext()) {
							String attachmentKey = keys.next();
							try {
								JSONObject json_attachment = json_attachments
										.getJSONObject(attachmentKey);
								Attachment attachment = sGsonExpose
										.fromJson(
												json_attachment.toString(),
												Attachment.class);
								attachment.setName(attachmentKey);
								attachments.add(attachment);
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
						groupCategory.setAttachments(attachments);	
					}

					groupCategories.add(groupCategory);
				}
			}
		}

		return groupCategories;
	}
	
	public static List<Member> parseMemberObjects(JSONObject jsonData) throws JSONException {
		// Get total member
		int totalMember = jsonData.getInt("count");
		Member.setTotalMember(totalMember);
		
		// Get list member
		List<Member> listMembers = null;
		JSONArray jsonArray = jsonData.getJSONArray("users");
		if (jsonArray != null) {
			listMembers = new ArrayList<Member>();
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject row = jsonArray.getJSONObject(i);
				try {
					String id = row.getString("_id");
					String name = row.getString("name");
					String image = row.getString("avatar_thumb_file_id");
					String online = row.getString("online_status");
					Member member = new Member(id, name, image, online);
					listMembers.add(member);
				} catch (JSONException e) {
				}
			}
		}

		return listMembers;
	}

	/**
	 * 
	 * @param json
	 * @return
	 * @throws JSONException 
	 */
	public static int getCommentCount(JSONObject json) throws JSONException {

		int count = 0;

		if (json != null) {

			if (json.has(Const.ERROR)) {
				appLogout(null, false, isInvalidToken(json));
				return 0;
			}

			JSONArray rows = json.getJSONArray(Const.ROWS);

			for (int i = 0; i < rows.length(); i++) {

				JSONObject row = rows.getJSONObject(i);
				count = row.getInt(Const.VALUE);

			}
		}

		return count;
	}

	/**
	 * Find a single Message object
	 * 
	 * @param json
	 * @return
	 * @throws SpikaException 
	 * @throws JSONException 
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	public static Message findMessage(JSONObject json) throws ClientProtocolException, IOException, JSONException, SpikaException {
		
		if (json.has(Const.ERROR)) {
			appLogout(null, false, isInvalidToken(json));
			return null;
		}
		
		return parseMessageObject(json, false, false, false);
	}

	/**
	 * Find all messages for current user
	 * 
	 * @param json
	 * @return
	 * @throws JSONException 
	 * @throws SpikaException 
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	public static ArrayList<Message> findMessagesForUser(JSONObject json) throws JSONException, ClientProtocolException, IOException, SpikaException {
		ArrayList<Message> messages = null;

		if (json != null) {

			if (json.has(Const.ERROR)) {
				appLogout(null, false, isInvalidToken(json));
				return null;
			}

			messages = new ArrayList<Message>();

			JSONArray rows = json.getJSONArray(Const.ROWS);

			for (int i = 0; i < rows.length(); i++) {

				JSONObject row = rows.getJSONObject(i);
				JSONObject msgJson = row.getJSONObject(Const.VALUE);

				Message message = null;

				String messageType = msgJson
						.getString(Const.MESSAGE_TYPE);

				if (messageType.equals(Const.TEXT)) {

					message = new Gson().fromJson(msgJson.toString(),
							Message.class);

				} else if (messageType.equals(Const.IMAGE)) {

					message = parseMessageObject(msgJson, true, false,
							false);

				} else if (messageType.equals(Const.VOICE)) {

					message = parseMessageObject(msgJson, false, true,
							false);

				} else if (messageType.equals(Const.VIDEO)) {

					message = parseMessageObject(msgJson, false, false,
							true);
				}
				else if (messageType.equals(Const.EMOTICON)) {

					message = parseMessageObject(msgJson, false, false,
							false);
				} else {

					message = new Gson().fromJson(msgJson.toString(),
							Message.class);

				}

				if (message == null) 
				{
					continue;	
				} 
				else 
				{
					messages.add(message);
				}
			}
		}

		if (null != messages) {
			Collections.sort(messages);
		}

		return messages;
	}

	/**
	 * Parse a single JSON object of Message type
	 * 
	 * @param json
	 * @param image
	 * @param voice
	 * @return
	 * @throws SpikaException 
	 * @throws JSONException 
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	private static Message parseMessageObject(JSONObject json, boolean image,
			boolean voice, boolean video) throws ClientProtocolException, IOException, JSONException, SpikaException {

		Message message = new Message();

		if (json == null) {
			return message;
		}

		if (json.has(Const.ERROR)) {
			appLogout(null, false, isInvalidToken(json));
			return null;
		}

		try {
			message.setId(json.getString(Const._ID));
		} catch (JSONException e) {
			message.setId("");
		}

		try {
			message.setRev(json.getString(Const._REV));
		} catch (JSONException e) {
			message.setRev("");
		}

		try {
			message.setType(json.getString(Const.TYPE));
		} catch (JSONException e) {
			message.setType("");
		}

		try {
			message.setMessageType(json.getString(Const.MESSAGE_TYPE));
		} catch (JSONException e) {
			message.setMessageType("");
		}

		try {
			message.setMessageTargetType(json
					.getString(Const.MESSAGE_TARGET_TYPE));
		} catch (JSONException e) {
			message.setMessageTargetType("");
		}

		try {
			message.setBody(json.getString(Const.BODY));
		} catch (JSONException e) {
			message.setBody("");
		}

		try {
			message.setFromUserId(json.getString(Const.FROM_USER_ID));
		} catch (JSONException e) {
			message.setFromUserId("");
		}

		try {
			message.setFromUserName(json.getString(Const.FROM_USER_NAME));
		} catch (JSONException e) {
			message.setFromUserName("");
		}

		try {
			message.setToUserId(json.getString(Const.TO_USER_ID));
		} catch (JSONException e) {
			message.setToUserId("");
		}

		try {
			message.setToGroupName(json.getString(Const.TO_USER_NAME));
		} catch (JSONException e) {
			message.setToGroupName("");
		}

		try {
			message.setToGroupId(json.getString(Const.TO_GROUP_ID));
		} catch (JSONException e) {
			message.setToGroupId("");
		}

		try {
			message.setToGroupName(json.getString(Const.TO_GROUP_NAME));
		} catch (JSONException e) {
			message.setToGroupName("");
		}

		try {
			message.setCreated(json.getLong(Const.CREATED));
		} catch (JSONException e) {
			return null;
		}

		try {
			message.setModified(json.getLong(Const.MODIFIED));
		} catch (JSONException e) {
			return null;
		}

		try {
			message.setValid(json.getBoolean(Const.VALID));
		} catch (JSONException e) {
			message.setValid(true);
		}

		try {
			message.setAttachments(json.getJSONObject(Const.ATTACHMENTS)
					.toString());
		} catch (JSONException e) {
			message.setAttachments("");
		}

		try {
			message.setLatitude(json.getString(Const.LATITUDE));
		} catch (JSONException e) {
			message.setLatitude("");
		}

		try {
			message.setLongitude(json.getString(Const.LONGITUDE));
		} catch (JSONException e) {
			message.setLongitude("");
		}

		try {
			message.setImageFileId((json.getString(Const.PICTURE_FILE_ID)));
		} catch (JSONException e) {
			message.setImageFileId("");
		}

       try {
                message.setImageThumbFileId((json.getString(Const.PICTURE_THUMB_FILE_ID)));
        } catch (JSONException e) {
                message.setImageThumbFileId("");
        }
	      
		try {
			message.setVideoFileId((json.getString(Const.VIDEO_FILE_ID)));
		} catch (JSONException e) {
			message.setVideoFileId("");
		}

		try {
			message.setVoiceFileId((json.getString(Const.VOICE_FILE_ID)));
		} catch (JSONException e) {
			message.setVoiceFileId("");
		}

		try {
			message.setEmoticonImageUrl(json
					.getString(Const.EMOTICON_IMAGE_URL));
		} catch (JSONException e) {
			message.setEmoticonImageUrl("");
		}

		try {
			message.setAvatarFileId(json.getString(Const.AVATAR_THUMB_FILE_ID));
		} catch (JSONException e) {
			message.setAvatarFileId("");
		}
		
		try {
			message.setDeleteType(json.getInt(Const.DELETE_TYPE));
		} catch (JSONException e) {
			message.setDeleteType(0);
		}
		
		try {
			message.setDelete(json.getInt(Const.DELETE_AT));
		} catch (JSONException e) {
			message.setDelete(0);
		}
		
		try {
			message.setReadAt(json.getInt(Const.READ_AT));
		} catch (JSONException e) {
			message.setReadAt(0);
		}
		
		
		try {
			message.setCommentCount(json.getInt(Const.COMMENT_COUNT));
		} catch (JSONException e) {
			message.setCommentCount(0);
		}
		
		
//		if (image || video || voice) {
//			message.setCommentCount(CouchDB.getCommentCount(message.getId()));
//		}

		return message;
	}

	/**
	 * Parse comments Json
	 * 
	 * @param json
	 * @return
	 * @throws JSONException 
	 */
	public static List<Comment> parseCommentsJson(JSONObject json) throws JSONException {

		List<Comment> comments = null;

		if (json != null) {

			if (json.has(Const.ERROR)) {
				appLogout(null, false, isInvalidToken(json));
				return null;
			}

			comments = new ArrayList<Comment>();

			JSONArray rows = json.getJSONArray(Const.COMMENTS);

			for (int i = 0; i < rows.length(); i++) {

				JSONObject commentJson = rows.getJSONObject(i);

				Comment comment = sGsonExpose.fromJson(
						commentJson.toString(), Comment.class);

				comments.add(comment);
			}
		}

		return comments;
	}

	/**
	 * Parse multi comment objects
	 * 
	 * @param json
	 * @return
	 * @throws JSONException 
	 */
	public static List<Comment> parseMultiCommentObjects(JSONObject json) throws JSONException {

		List<Comment> comments = null;

		if (json != null) {

			if (json.has(Const.ERROR)) {
				appLogout(null, false, isInvalidToken(json));
				return null;
			}

			comments = new ArrayList<Comment>();

			JSONArray rows = json.getJSONArray(Const.ROWS);

			for (int i = 0; i < rows.length(); i++) {

				JSONObject row = rows.getJSONObject(i);

				String key = row.getString(Const.KEY);

				if (!"null".equals(key)) {

					JSONObject commentJson = row.getJSONObject(Const.VALUE);

					Comment comment = sGsonExpose.fromJson(
							commentJson.toString(), Comment.class);

					comments.add(comment);
				}
			}
		}

		return comments;
	}

	/**
	 * Parse multi emoticon objects
	 * 
	 * @param json
	 * @return
	 * @throws JSONException 
	 */
	public static List<Emoticon> parseMultiEmoticonObjects(JSONObject json) throws JSONException {

		List<Emoticon> emoticons = null;

		if (json != null) {
			
			if (json.has(Const.ERROR)) {
				appLogout(null, false, isInvalidToken(json));
				return null;
			}

			emoticons = new ArrayList<Emoticon>();

			JSONArray rows = json.getJSONArray(Const.ROWS);

			for (int i = 0; i < rows.length(); i++) {

				JSONObject row = rows.getJSONObject(i);

				String key = row.getString(Const.KEY);

				if (!"null".equals(key)) {

					JSONObject emoticonJson = row
							.getJSONObject(Const.VALUE);

					Emoticon emoticon = sGsonExpose.fromJson(
							emoticonJson.toString(), Emoticon.class);

					emoticons.add(emoticon);

//						SpikaApp.getFileDir().saveFile(
//								emoticon.getIdentifier(),
//								emoticon.getImageUrl());
				}
			}
		}

		return emoticons;
	}
	
	/**
	 * JSON response from creating a watching group log
	 * 
	 * @param json
	 * @return
	 * @throws JSONException 
	 */
	public static String createWatchingGroupLog(String result) throws JSONException {

//		boolean ok = false;
//		String id = null;
//		String rev = null;
//
//		if (result != null) {
//
//			if (json.has(Const.ERROR)) {
//				appLogout(null, false, isInvalidToken(json));
//				return null;
//			}
//
//			ok = json.getBoolean(Const.OK);
//			id = json.getString(Const.ID);
//			rev = json.getString(Const.REV);
//			
//			SpikaApp.getPreferences().setWatchingGroupId(id);
//			SpikaApp.getPreferences().setWatchingGroupRev(rev);
//			
//		}
//
//		if (!ok) {
//			Logger.error(TAG + "createWatchingGroupLog", "error in creating a watching group log");
//			return null;
//		}

		return result;
	}
	
	/**
	 * JSON response from deleting a watching group log
	 * 
	 * @param json
	 * @return
	 * @throws JSONException 
	 */
	public static String deleteWatchingGroupLog(String result) throws JSONException {

//		boolean ok = false;
//
//		if (json != null) {
//
//			if (json.has(Const.ERROR)) {
//				appLogout(false, false, isInvalidToken(json));
//				return false;
//			}
//
//			ok = json.getBoolean(Const.OK);	
//			
//		}

		return result;
	}
	
	private static boolean isInvalidToken(JSONObject json) {
		if (json.has(Const.MESSAGE)) {
			try {
				String errorMessage = json.getString(Const.MESSAGE);
				if (errorMessage.equalsIgnoreCase(Const.INVALID_TOKEN)) {
					return true;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public static Object appLogout(Object object, boolean isUserUpdateConflict, boolean isInvalidToken) {
		SideBarActivity.appLogout(isUserUpdateConflict, true, isInvalidToken);
		return object;
	}
	
	/**
	 * Parse server objects
	 * 
	 * @param json
	 * @return
	 * @throws JSONException 
	 */
	public static List<Server> parseServers(JSONArray json) throws JSONException {

		List<Server> servers = null;

		if (json != null) {
			
			servers = new ArrayList<Server>();

			for (int i = 0; i < json.length(); i++) {

				JSONObject row = json.getJSONObject(i);
				
				servers.add(sGsonExpose.fromJson(row.toString(), Server.class));

			}
		} else{
			appLogout(null, false, false);
			return null;
		}

		return servers;
	}
	

}
