package com.snappy.dialog;


import com.snappy.R;
import com.snappy.couchdb.CouchDB;
import com.snappy.couchdb.ResultListener;
import com.snappy.messageshandling.MessagesUpdater;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ToggleButton;

public class DeleteMessageDialog extends Dialog implements View.OnClickListener {

	private ToggleButton [] deleteButtons;
	private Button closeDialog;
	
	private String messageId = null;
	private int deleteType = 0;
	private int oldDeleteType = 0;
	
	Context context;
	
	public DeleteMessageDialog(Activity activity, String messageId, int oldDeleteType) {
		super(activity, android.R.style.Theme_Translucent_NoTitleBar);
		setContentView(R.layout.dialog_delete_message);
				
		context = activity;
		this.messageId = messageId;
		this.oldDeleteType = oldDeleteType;
		this.deleteType = oldDeleteType;
		
		deleteButtons = new ToggleButton[6];
		
		deleteButtons[0] = (ToggleButton) findViewById(R.id.dont_delete);
		deleteButtons[1] = (ToggleButton) findViewById(R.id.delete_now);
		deleteButtons[2] = (ToggleButton) findViewById(R.id.delete_after_5_min);
		deleteButtons[3] = (ToggleButton) findViewById(R.id.delete_after_day);
		deleteButtons[4] = (ToggleButton) findViewById(R.id.delete_after_week);
		deleteButtons[5] = (ToggleButton) findViewById(R.id.delete_after_read);
		
		closeDialog = (Button) findViewById(R.id.close_dialog);
		
		for (Button button : deleteButtons) {
			button.setOnClickListener(this);
		}
		
		deleteButtons[oldDeleteType].setChecked(true);
		
		closeDialog.setOnClickListener(this);
	}


	@Override
	public void onClick(View v) {
		if (v.equals(closeDialog)) {
			if (oldDeleteType != deleteType) {
				callApi(deleteType);
			}
			DeleteMessageDialog.this.dismiss();
		}
		else {
			for (int i = 0; i < deleteButtons.length ; i++) {
				if (v.equals(deleteButtons[i])) {
					deleteType = i;
					deleteButtons[i].setChecked(true);
				}
				else {
					deleteButtons[i].setChecked(false);
				}
			}
		}
	}
	
	void callApi (int deleteType) {
		CouchDB.deleteMessageAsync(messageId, String.valueOf(deleteType), new DeleteResultListener(), context, true);
	}

	class DeleteResultListener implements ResultListener<Void> {

		@Override
		public void onResultsSucceded(Void result) {
			Log.e("*** API ***", "return success");
			MessagesUpdater.reload();
			
		}

		@Override
		public void onResultsFail() {
			Log.e("*** API ***", "return fail");
		}
	}
}
