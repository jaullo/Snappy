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

package com.snappy.extendables;

import java.io.IOException;

import org.json.JSONException;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.os.AsyncTask;
import android.util.Log;


import com.snappy.R;
import com.snappy.SignInActivity;
import com.snappy.SpikaApp;
import com.snappy.couchdb.SpikaException;
import com.snappy.couchdb.SpikaForbiddenException;
import com.snappy.dialog.HookUpDialog;
import com.snappy.utils.Const;

/**
 * SpikaAsync
 * 
 * HookUp base AsyncTask checks network connection before execution.
 */

public abstract class SpikaAsync<Params, Progress, Result> extends
		AsyncTask<Params, Progress, Result> {

	protected Context mContext;
	private Exception exception;

	protected SpikaAsync(final Context context) {
		this.mContext = context;
	}

	@Override
	protected void onPreExecute() {
		if (SpikaApp.hasNetworkConnection()) {
			super.onPreExecute();
		} else {
			this.cancel(true);
		}
	}
	
	@Override
	protected Result doInBackground(Params... params) {
		Result result = null;
		try {
			result = (Result) backgroundWork(params);
		} catch (JSONException e) {
			exception = e;
			e.printStackTrace();
		} catch (IOException e) {
			exception = e;
			e.printStackTrace();
		} catch (SpikaException e) {
			exception = e;
			e.printStackTrace();
		} catch (NullPointerException e) {
			exception = e;
			e.printStackTrace();
		} catch (SpikaForbiddenException e) {
			exception = e;
			e.printStackTrace();
		}
		return result;
	}

	@Override
	protected void onPostExecute(Result result) {
		super.onPostExecute(result);
		if (exception != null)
		{
			String error = (exception.getMessage() != null) ? exception.getMessage() : mContext.getString(R.string.an_internal_error_has_occurred);
			
			Log.e(Const.ERROR, error);
			
			final HookUpDialog dialog = new HookUpDialog(mContext);
			String errorMessage = null;
			if (exception instanceof IOException){
			    errorMessage = mContext.getString(R.string.can_not_connect_to_server) + "\n" + exception.getClass().getName() + " " + error;
			}else if(exception instanceof JSONException){
			    errorMessage = mContext.getString(R.string.an_internal_error_has_occurred) + "\n" + exception.getClass().getName() + " " + error;
			}else if(exception instanceof NullPointerException){
			    errorMessage = mContext.getString(R.string.an_internal_error_has_occurred) + "\n" + exception.getClass().getName() + " " + error;
			}else if(exception instanceof SpikaException){
				errorMessage = mContext.getString(R.string.an_internal_error_has_occurred) + "\n" + error;
			}else if(exception instanceof SpikaForbiddenException){
				errorMessage = mContext.getString(R.string.an_internal_error_has_occurred) + "\n" + error;
			}else{
			    errorMessage = mContext.getString(R.string.an_internal_error_has_occurred) + "\n" + exception.getClass().getName() + " " + error;
			}	
			
			if (mContext instanceof Activity) {
				if (!((Activity)mContext).isFinishing())
				{
					if(exception instanceof SpikaForbiddenException){
						//token expired
						errorMessage=mContext.getString(R.string.token_expired_error);
						dialog.setOnDismissListener(new OnDismissListener() {
							
							@Override
							public void onDismiss(DialogInterface dialog) {
								mContext.startActivity(new Intent(mContext, SignInActivity.class)
										.putExtra(mContext.getString(R.string.token_expired_error), true));
								((Activity) mContext).finish();
							}
						});
					}
					dialog.showOnlyOK(errorMessage);
				}
			}
		}
	}

	protected Result backgroundWork(Params... params) throws JSONException, IOException, SpikaException, NullPointerException, SpikaForbiddenException {
		return null;
	}
	
}
