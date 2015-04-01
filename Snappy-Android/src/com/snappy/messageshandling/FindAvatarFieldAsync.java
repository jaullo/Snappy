package com.snappy.messageshandling;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import android.content.Context;

import com.snappy.couchdb.CouchDB;
import com.snappy.couchdb.SpikaException;
import com.snappy.couchdb.SpikaForbiddenException;
import com.snappy.extendables.SpikaAsync;

/**
 * FindAvatarFileIdAsync
 * 
 * AsyncTask for finding avatar file ID by user ID on CouchDB.
 */

public class FindAvatarFieldAsync extends SpikaAsync<String, Void, String> {

	public FindAvatarFieldAsync(Context context) {
		super(context);
	}

	@Override
	protected String doInBackground(String... params) {
		String userId = params[0];
		String image = null;
		try {
			image = CouchDB.findAvatarFileId(userId);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SpikaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SpikaForbiddenException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return image;
	}

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
	}
}
