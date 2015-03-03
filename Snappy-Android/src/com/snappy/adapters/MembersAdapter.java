package com.snappy.adapters;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.snappy.R;
import com.snappy.couchdb.model.Member;
import com.snappy.lazy.ImageLoader;
import com.snappy.utils.Utils;

public class MembersAdapter extends ArrayAdapter<Member> {

	private Context context;
	private int layoutResourceId;
	private ArrayList<Member> array;

	public MembersAdapter(Context context, int layoutResourceId, ArrayList<Member> array) {
		super(context, layoutResourceId, array);
		this.layoutResourceId = layoutResourceId;
		this.context = context;
		this.array = array;
	}

	public Member getItem(int position) {
		return array.get(position);
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View row = convertView;
		MemberHolder holder = null;

		if (row == null) {
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);
			holder = new MemberHolder();
			holder.image = (ImageView) row.findViewById(R.id.ivUserImage);
			holder.name = (TextView) row.findViewById(R.id.tvUser);
			holder.loading = (ProgressBar) row
					.findViewById(R.id.pbLoadingForImage);
			holder.loading.setVisibility(View.VISIBLE);
			holder.online = (ImageView) row.findViewById(R.id.ivOnlineStatus);
			row.setTag(holder);
		} else {
			holder = (MemberHolder) row.getTag();
		}
		Member member = array.get(position);
		holder.name.setText(member.getName());
		Utils.displayImage(member.getImage(), holder.image, holder.loading,
				ImageLoader.SMALL, R.drawable.user_stub, false);
		if (member.getOnline() != null) {
			holder.online.setVisibility(View.VISIBLE);
			if (member.getOnline().equals(context.getString(R.string.online))) {
				holder.online.setImageResource(R.drawable.user_online_icon);
			} else if (member.getOnline().equals(context.getString(R.string.away))) {
				holder.online.setImageResource(R.drawable.user_away_icon);
			} else if (member.getOnline().equals(context.getString(R.string.busy))) {
				holder.online.setImageResource(R.drawable.user_busy_icon);
			} else if (member.getOnline().equals(context.getString(R.string.offline))) {
				holder.online.setImageResource(R.drawable.user_offline_icon);
			}
		} else {
			holder.online.setImageResource(R.drawable.user_offline_icon);
		}
		return row;
	}

	static class MemberHolder {
		ImageView image;
		TextView name;
		ProgressBar loading;
		ImageView online;
	}

}
