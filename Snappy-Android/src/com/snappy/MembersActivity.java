package com.snappy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;


import com.snappy.adapters.MembersAdapter;
import com.snappy.couchdb.CouchDB;
import com.snappy.couchdb.SpikaException;
import com.snappy.couchdb.SpikaForbiddenException;
import com.snappy.couchdb.model.Member;
import com.snappy.dialog.HookUpProgressDialog;
import com.snappy.extendables.LoadMoreListView;
import com.snappy.extendables.SpikaActivity;
import com.snappy.extendables.LoadMoreListView.OnLoadMoreListener;

/**
 * MembersActivity
 * 
 * Shows a list of members
 */
public class MembersActivity extends SpikaActivity {
	private static final int LOAD_MORE_COUNT = 20;

	private LoadMoreListView listView;
	private MembersAdapter adapter;
	private ArrayList<Member> listMembers;
	private String groupId;
	private int offset = 0;
	private boolean isLoadMore = true;

	private HookUpProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_members);

		groupId = getIntent().getStringExtra("group_id");

		listMembers = new ArrayList<Member>();
		adapter = new MembersAdapter(this, R.layout.user_item, listMembers);
		listView = (LoadMoreListView) findViewById(R.id.listView);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent(MembersActivity.this,
						UserProfileActivity.class);
				intent.putExtra("user_id", listMembers.get(position).getId());
				startActivity(intent);
			}
		});
		listView.setOnLoadMoreListener(new OnLoadMoreListener() {

			@Override
			public void onLoadMore() {
				if (isLoadMore) {
					new GetMembers().execute();
					listView.onLoadMoreComplete();
				}
			}
		});
		// Get data first time
		new GetMembers().execute();
	}

	/**
	 * Get list members from service
	 * @author Thuan
	 *
	 */
	class GetMembers extends AsyncTask<Void, Void, List<Member>> {

		@Override
		protected void onPreExecute() {
			if (progressDialog == null) {
				progressDialog = new HookUpProgressDialog(MembersActivity.this);
			}
			progressDialog.show();
		}

		protected List<Member> doInBackground(Void... v) {
			try {
				return CouchDB.findMembersByGroupId(groupId, offset,
						LOAD_MORE_COUNT);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (SpikaException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SpikaForbiddenException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		protected void onPostExecute(List<Member> listData) {
			if (listData != null && !listData.isEmpty()) {
				offset += listData.size();
				listMembers.addAll(listData);
				adapter.notifyDataSetChanged();

				if (listMembers.size() >= Member.getTotalMember()) {
					isLoadMore = false;
				}
			}
			if (progressDialog != null && progressDialog.isShowing()) {
				progressDialog.dismiss();
			}
		}
	}
}
