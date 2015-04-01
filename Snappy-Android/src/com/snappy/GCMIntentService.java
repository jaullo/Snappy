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

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.InboxStyle;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;


import com.google.android.gcm.GCMBaseIntentService;
import com.snappy.couchdb.Command;
import com.snappy.couchdb.CouchDB;
import com.snappy.couchdb.LocalDB;
import com.snappy.couchdb.ResultListener;
import com.snappy.couchdb.SpikaAsyncTask;
import com.snappy.couchdb.SpikaException;
import com.snappy.couchdb.SpikaForbiddenException;
import com.snappy.couchdb.model.LocalMessage;
import com.snappy.couchdb.model.User;
import com.snappy.management.UsersManagement;
import com.snappy.utils.Const;
import com.snappy.utils.Logger;
import com.snappy.utils.Utils;

/**
 * GCMIntentService
 * 
 * Handles push broadcast and generates HookUp notification if application is in
 * foreground or Android notification if application is in background.
 */

public class GCMIntentService extends GCMBaseIntentService {

	private static int mNotificationCounter = 1;
	public final static String PUSH = "com.snappy.GCMIntentService.PUSH";
	private static final Intent mPushBroadcast = new Intent(PUSH);
	private static final String LOG_TAG = "GetAClue::GCMIntentService";
	private LocalDB db = null;
	
	public GCMIntentService() {
		super(Const.PUSH_SENDER_ID);
	}

	private final String TAG = "=== GCMIntentService ===";

	/**
	 * Method called on device registered
	 **/
	@Override
	protected void onRegistered(Context context, String registrationId) {

		if (!registrationId.equals(null)) {
			savePushTokenAsync(registrationId, Const.ONLINE, context);
		}
	}

	/**
	 * Method called on device unregistered
	 * */
	@Override
	protected void onUnregistered(Context context, String registrationId) {

		if (!registrationId.equals(null)) {
			removePushTokenAsync(context);
		}
	}

	/**
	 * Method called on Receiving a new message
	 * */
	@Override
	protected void onMessage(Context context, Intent intent) {
		db = new LocalDB(context);
		Bundle pushExtras = intent.getExtras();
		String pushMessage = intent.getStringExtra(Const.PUSH_MESSAGE);
		String pushFromName = intent.getStringExtra(Const.PUSH_FROM_NAME);
		String pushMessageBody = intent.getStringExtra(Const.PUSH_FROM_MESSAGE_BODY);
		
		String pushMessaBodyStyled = pushFromName + ": " + pushMessageBody;
		db.writeMessage(pushMessaBodyStyled);	
		
		try {
			boolean appIsInForeground = new SpikaApp.ForegroundCheckAsync()
					.execute(getApplicationContext()).get();
			
			boolean screenLocked = ((KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE))
					.inKeyguardRestrictedInputMode();
			
		
			if (appIsInForeground && !screenLocked) {
				mPushBroadcast.replaceExtras(pushExtras);
				LocalBroadcastManager.getInstance(this).sendBroadcast(mPushBroadcast);
				generateNotification(this, pushMessage, pushFromName, pushExtras, pushMessaBodyStyled);
			} else {
				//triggerNotification(this, pushMessage, pushFromName, pushExtras, pushMessaBodyStyled);
				generateNotification(this, pushMessage, pushFromName, pushExtras, pushMessaBodyStyled);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		
		
	}

	/**
	 * Method called on Error
	 * */
	@Override
	protected void onError(Context arg0, String errorId) {
		Logger.error(TAG, "Received error: " + errorId);
	}

	@Override
	protected boolean onRecoverableError(Context context, String errorId) {
		return super.onRecoverableError(context, errorId);
	}

	@SuppressWarnings("deprecation")
	public void triggerNotification(Context context, String message, String fromName, Bundle pushExtras, String messageData) {

		if (fromName != null) {
			final NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			
			Notification notification = new Notification(
					R.drawable.icon_notification, message,
					System.currentTimeMillis());
			
			notification.number = mNotificationCounter + 1;
			mNotificationCounter = mNotificationCounter + 1;

			Intent intent = new Intent(this, SplashScreenActivity.class);
			intent.replaceExtras(pushExtras);
			intent.putExtra(Const.PUSH_INTENT, true);
			intent.setAction(Long.toString(System.currentTimeMillis()));
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK 
					| Intent.FLAG_FROM_BACKGROUND
					| Intent.FLAG_ACTIVITY_TASK_ON_HOME);
			
			PendingIntent pendingIntent = PendingIntent.getActivity(this,
					notification.number, intent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			
			notification.setLatestEventInfo(this,
					context.getString(R.string.app_name), messageData,
					pendingIntent);
			
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			notification.defaults |= Notification.DEFAULT_VIBRATE;
			notification.defaults |= Notification.DEFAULT_SOUND;
			notification.defaults |= Notification.DEFAULT_LIGHTS;
			String notificationId = Double.toString(Math.random());
			notificationManager.notify(notificationId, 0, notification);

		}
		
		Log.i(LOG_TAG,  message);
		Log.i(LOG_TAG,  fromName);
		
	}

	/**
	 * Issues a notification to inform the user that server has sent a message.
	 */
	private void generateNotification(Context context, String message,
			String fromName, Bundle pushExtras, String body) {

		
		db = new LocalDB(context);
		List<LocalMessage> myMessages = db.getAllMessages();
		
		// Open a new activity called GCMMessageView
		Intent intento = new Intent(this, SplashScreenActivity.class);
		intento.replaceExtras(pushExtras);
		// Pass data to the new activity
		intento.putExtra(Const.PUSH_INTENT, true);
		intento.setAction(Long.toString(System.currentTimeMillis()));
		intento.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_FROM_BACKGROUND
				| Intent.FLAG_ACTIVITY_TASK_ON_HOME);

		// Starts the activity on notification click
		PendingIntent pIntent = PendingIntent.getActivity(this, 0, intento,
				PendingIntent.FLAG_UPDATE_CURRENT);

		// Create the notification with a notification builder
		NotificationCompat.Builder notification = new NotificationCompat.Builder(this)
				.setSmallIcon(R.drawable.icon_notification)
				.setWhen(System.currentTimeMillis())
				.setContentTitle(myMessages.size() + " " + this.getString(R.string.push_new_message_message))
				.setStyle(new NotificationCompat.BigTextStyle().bigText("Mas mensajes"))
				.setAutoCancel(true)
				.setDefaults(Notification.DEFAULT_VIBRATE |
                                Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS)
				.setContentText(body).setContentIntent(pIntent);

		NotificationCompat.InboxStyle inboxStyle =  new NotificationCompat.InboxStyle();
		inboxStyle.setBigContentTitle(myMessages.size() + " " + this.getString(R.string.push_new_message_message));
		
		for (int i=0; i < myMessages.size(); i++) {

		    inboxStyle.addLine(myMessages.get(i).getMessage());
		}
		
		notification.setStyle(inboxStyle);
		
		// Remove the notification on click
		//notification.flags |= Notification.FLAG_AUTO_CANCEL;
		//notification.defaults |= Notification.DEFAULT_VIBRATE;
		//notification.defaults |= Notification.DEFAULT_SOUND;
		//notification.defaults |= Notification.DEFAULT_LIGHTS;

		NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		manager.notify(R.string.app_name, notification.build());
		{
			// Wake Android Device when notification received
			PowerManager pm = (PowerManager) context
					.getSystemService(Context.POWER_SERVICE);
			
			final PowerManager.WakeLock mWakelock = pm.newWakeLock(
					PowerManager.FULL_WAKE_LOCK
							| PowerManager.ACQUIRE_CAUSES_WAKEUP, "GCM_PUSH");
			mWakelock.acquire();
			// Timer before putting Android Device to sleep mode.
			Timer timer = new Timer();
			TimerTask task = new TimerTask() {
				public void run() {
					mWakelock.release();
				}
			};
			timer.schedule(task, 5000);
		}
	}
	


 
	private void savePushTokenAsync (String pushToken, String onlineStatus, Context context) {
		new SpikaAsyncTask<Void, Void, Boolean>(new SavePushToken(pushToken, onlineStatus), new SavePushTokenListener(pushToken), context, false).execute();
	}
	
	private class SavePushToken implements Command<Boolean>{

		String pushToken;
		String onlineStatus;
		
		public SavePushToken (String pushToken, String onlineStatus) {
			this.pushToken = pushToken;
			this.onlineStatus = onlineStatus;
		}
		
		@Override
		public Boolean execute() throws JSONException, IOException,
				SpikaException, IllegalStateException, SpikaForbiddenException {

			/* set new androidToken and onlineStatus */
			UsersManagement.getLoginUser().setOnlineStatus(onlineStatus);
			SpikaApp.getPreferences().setUserEmail(UsersManagement.getLoginUser().getEmail());
			SpikaApp.getPreferences().setUserPushToken(pushToken);
			return CouchDB.updateUser(UsersManagement.getLoginUser());
		}
	}
	
	private class SavePushTokenListener implements ResultListener<Boolean>{

		String currentPushToken;
		
		public SavePushTokenListener (String currentPushToken) {
			this.currentPushToken = currentPushToken;
		}
		
		@Override
		public void onResultsSucceded(Boolean result) {
			if (result) {
			} else {
				SpikaApp.getPreferences().setUserPushToken(currentPushToken);
			}
		}

		@Override
		public void onResultsFail() {
		}
		
	}
	
	private void removePushTokenAsync (Context context) {
		SpikaApp.getPreferences().setUserPushToken("");
		if (UsersManagement.getLoginUser() != null) {
			CouchDB.unregisterPushTokenAsync(UsersManagement.getLoginUser().getId(), new RemovePushTokenListener(), context, false);
		}
	}
	
	private class RemovePushTokenListener implements ResultListener<String> {
		@Override
		public void onResultsSucceded(String result) {
			if (result != null && result.contains("OK")) {
				SpikaApp.getPreferences().setUserEmail("");
				SpikaApp.getPreferences().setUserPassword("");
			}
		}
		@Override
		public void onResultsFail() {
		}
	}
}
