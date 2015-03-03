package com.snappy.couchdb;

import java.io.IOException;

import org.json.JSONException;


import com.snappy.R;
import com.snappy.SignInActivity;
import com.snappy.SpikaApp;
import com.snappy.dialog.HookUpDialog;
import com.snappy.dialog.HookUpProgressDialog;
import com.snappy.utils.Const;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

public class SpikaAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result>{

	protected Command<Result> command;
	protected Context context;
	protected ResultListener<Result> resultListener;
	protected Exception exception;
	protected HookUpProgressDialog progressDialog;
	protected boolean showProgressBar = false;
	
	public SpikaAsyncTask(Command<Result> command, ResultListener<Result> resultListener, Context context, boolean showProgressBar) {
		super();
		this.command = command;
		this.resultListener = resultListener;
		this.context = context;
		this.showProgressBar = showProgressBar;
	}
	
//	public SpikaAsyncTask(Command<Result> command, Context context, boolean showProgressBar) {
//		super();
//		this.command = command;
//		this.context = context;
//		this.showProgressBar = showProgressBar;
//	}
//	
//	protected SpikaAsyncTask(Context context){
//		super();
//		this.context = context;
//	}
	
	@Override
	protected void onPreExecute() {
		if (SpikaApp.hasNetworkConnection()) {
			super.onPreExecute();
			if (showProgressBar)
			{
				progressDialog = new HookUpProgressDialog(context);
				if (!((Activity)context).isFinishing()) progressDialog.show();
			}
		} else {
			this.cancel(false);
			Log.e(Const.ERROR, Const.OFFLINE);
			final HookUpDialog dialog = new HookUpDialog(context);
			dialog.showOnlyOK(context.getString(R.string.no_internet_connection));
		}
	}

	@Override
	protected Result doInBackground(Params... params) {
		Result result = null;
		try {
			result = (Result) command.execute();
		} catch (JSONException e) {
			exception = e;
			e.printStackTrace();
		} catch (IOException e) {
			exception = e;
			e.printStackTrace();
		} catch (SpikaException e) {
			exception = e;
			e.printStackTrace();
		} catch (IllegalStateException e) {
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
		
//		final HookUpDialog _dialog = new HookUpDialog(context);
//		Log.e("is this the way", "active:" + _dialog.getWindow().isActive());
		
		if (showProgressBar)
		{
			if (!((Activity)context).isFinishing())
			{
				if (progressDialog.isShowing()) progressDialog.dismiss();
			}
		}
		
		if (exception != null)
		{
			String error = (exception.getMessage() != null) ? exception.getMessage() : context.getString(R.string.an_internal_error_has_occurred);
			
			Log.e(Const.ERROR, error);
			
			final HookUpDialog dialog = new HookUpDialog(context);
			String errorMessage = null;
			if (exception instanceof IOException){
			    errorMessage = context.getString(R.string.can_not_connect_to_server) + "\n" + exception.getClass().getName() + " " + error;
			}else if(exception instanceof JSONException){
			    errorMessage = context.getString(R.string.an_internal_error_has_occurred) + "\n" + exception.getClass().getName() + " " + error;
			}else if(exception instanceof NullPointerException){
			    errorMessage = context.getString(R.string.an_internal_error_has_occurred) + "\n" + exception.getClass().getName() + " " + error;
			}else if(exception instanceof SpikaException){
				errorMessage = error;
			}else if(exception instanceof SpikaForbiddenException){
				errorMessage = context.getString(R.string.an_internal_error_has_occurred) + "\n" + error;
			}else{
			    errorMessage = context.getString(R.string.an_internal_error_has_occurred) + "\n" + exception.getClass().getName() + " " + error;
			}	
			
			if (context instanceof Activity) {
				if (!((Activity)context).isFinishing())
				{
					if(exception instanceof SpikaForbiddenException){
						//token expired
						errorMessage=context.getString(R.string.token_expired_error);
						dialog.setOnDismissListener(new OnDismissListener() {
							
							@Override
							public void onDismiss(DialogInterface dialog) {
								context.startActivity(new Intent(context, SignInActivity.class)
										.putExtra(context.getString(R.string.token_expired_error), true));
								((Activity) context).finish();
							}
						});
					}
					dialog.showOnlyOK(errorMessage);
				}
			}
			
			if (resultListener != null) resultListener.onResultsFail();
		}
		else
		{
			if (resultListener != null) resultListener.onResultsSucceded(result);
		}
	}
}
