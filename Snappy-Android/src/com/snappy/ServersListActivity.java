package com.snappy;

import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;


import com.snappy.couchdb.CouchDB;
import com.snappy.couchdb.ResultListener;
import com.snappy.couchdb.model.Server;
import com.snappy.utils.Const;

public class ServersListActivity extends ListActivity {
	
	private List<Server> mServersList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_servers_list);
		
		Button btnBack = (Button) findViewById(R.id.btnBack);
		btnBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		getServers();
	}
	
	private void getServers(){
		CouchDB.findAllServersAsync(new ResultListener<List<Server>>() {
			
			@Override
			public void onResultsSucceded(List<Server> result) {
				mServersList = result;
				fillData();
			}
			
			@Override
			public void onResultsFail() {
				
			}
		}, this, true);
	}
	
	private void fillData(){
		setListAdapter(new ServerListAdapter(this, 0));
		
		getListView().setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				
				setServerUrlAndName(mServersList.get(position).getName(), 
						mServersList.get(position).getUrl());
				
				Intent resultIntent=new Intent();
				resultIntent.putExtra(Const.SERVER_NAME, mServersList.get(position).getName());
				setResult(RESULT_OK, resultIntent);
				finish();
			}
		});
		
		setAddNewServerET();
	}
	
	private void setAddNewServerET(){
		EditText etAddNewServer = (EditText) findViewById(R.id.etAddServer);
		etAddNewServer.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if(actionId==EditorInfo.IME_ACTION_DONE){
					String url=v.getText().toString();
					setServerUrlAndName("", url);
					
					Intent resultIntent=new Intent();
					resultIntent.putExtra(Const.SERVER_NAME, url);
					setResult(RESULT_OK, resultIntent);
					finish();
				}
				return false;
			}
		});
	}
	
	private void setServerUrlAndName(String name, String url){
		SpikaApp.getInstance().setBaseUrl(url, name);
		
		CouchDB.setAuthUrl(SpikaApp.getInstance().getBaseUrlWithSufix(Const.AUTH_URL));
		CouchDB.setUrl(SpikaApp.getInstance().getBaseUrlWithApi());
	}
	
	class ServerListAdapter extends ArrayAdapter<Server>{
		
		private LayoutInflater mInflater;

		public ServerListAdapter(Context context, int resource) {
			super(context, resource);
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		@Override
		public int getCount() {
			if(mServersList!=null){
				return mServersList.size();
			}
			return 0;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			View view = convertView;
			ViewHolder holder = null;
			
			if(view == null){
				view = mInflater.inflate(R.layout.item_server, parent, false);
				
				holder = new ViewHolder();
				holder.serverName = (TextView) view.findViewById(R.id.tvServerName);
				holder.serverUrl = (TextView) view.findViewById(R.id.tvServerUrl);
				
				holder.serverName.setTypeface(SpikaApp.getTfMyriadPro());
				holder.serverUrl.setTypeface(SpikaApp.getTfMyriadPro());
				
				view.setTag(holder);
			} else {
				holder = (ViewHolder) view.getTag();
			}
			
			Server server = mServersList.get(position);
			
			holder.serverName.setText(server.getName());
			holder.serverUrl.setText(server.getUrl());
			
			return view;
		}
		
	}
	
	class ViewHolder {
		private TextView serverName;
		private TextView serverUrl;
	}

}
