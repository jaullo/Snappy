package com.snappy.dialog;



import com.snappy.R;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class DeleteInformationDialog extends Dialog implements View.OnClickListener {

	private TextView textView;
	private Button button;
	
	public DeleteInformationDialog(Context context, int deleteAt, int deleteType) {
		super(context, R.style.Theme_Transparent);
		setContentView(R.layout.dialog_delete_information);
		
		button = (Button) findViewById(R.id.close_delete_information_dialog);
		textView = (TextView) findViewById(R.id.delete_information_text);
		
		button.setOnClickListener(this);
		setText(deleteAt, deleteType);
	}

	@Override
	public void onClick(View v) {
		DeleteInformationDialog.this.dismiss();
	}
	
	void setText (int deleteAt, int deleteType) {
		
		if (deleteType > 0) {
			textView.setText(calculateTimeToDelete(deleteAt));
		}
		else {
			textView.setText(R.string.deleteinformation_activity_set_text);
		}
	}
	
	String calculateTimeToDelete (int deleteAt) {
		
		int now = (int) (System.currentTimeMillis() / 1000);
		int seconds = deleteAt - now;
		int minutes = seconds / 60;
		int hours = minutes / 60;
		int days = hours / 24;
		
		if (days > 0) return "in " + days + " days";
		else if (hours > 0) return "in " + hours + " hours";
		else if (minutes > 0) return "in " + minutes + " minutes";
		else if (seconds > 0) return "in " + seconds + " seconds";
		else return "after read";		
	} 
}
