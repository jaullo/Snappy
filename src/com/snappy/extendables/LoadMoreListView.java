package com.snappy.extendables;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListAdapter;
import android.widget.ListView;

public class LoadMoreListView extends ListView implements OnScrollListener {

	private OnScrollListener mOnScrollListener;
	private OnLoadMoreListener mOnLoadMoreListener;

	private boolean mIsLoadingMore = false;
	private int mCurrentScrollState;
	private boolean mIsFinishLoadAll = false;

	public LoadMoreListView(Context context) {
		super(context);
		super.setOnScrollListener(this);
	}

	public LoadMoreListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		super.setOnScrollListener(this);
	}

	public LoadMoreListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		super.setOnScrollListener(this);
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		super.setAdapter(adapter);
	}

	@Override
	public void setOnScrollListener(OnScrollListener l) {
		mOnScrollListener = l;
	}

	public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
		mOnLoadMoreListener = onLoadMoreListener;
	}

	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		if (mOnScrollListener != null) {
			mOnScrollListener.onScroll(view, firstVisibleItem,
					visibleItemCount, totalItemCount);
		}
		if (mOnLoadMoreListener != null && mIsFinishLoadAll == false) {
			if (visibleItemCount == totalItemCount) {
				return;
			}
			boolean loadmore = firstVisibleItem + visibleItemCount >= totalItemCount;
			if (!mIsLoadingMore && loadmore
					&& mCurrentScrollState != SCROLL_STATE_IDLE) {
				mIsLoadingMore = true;
				onLoadMore();
			}
		}
	}

	public void onScrollStateChanged(AbsListView view, int scrollState) {
		mCurrentScrollState = scrollState;
		if (mOnScrollListener != null) {
			mOnScrollListener.onScrollStateChanged(view, scrollState);
		}
	}

	public void setFinishLoadAll() {
		mIsFinishLoadAll = true;
	}

	public void onLoadMore() {
		if (mOnLoadMoreListener != null) {
			mOnLoadMoreListener.onLoadMore();
		}
	}

	public void onLoadMoreComplete() {
		mIsLoadingMore = false;
	}

	public interface OnLoadMoreListener {
		public void onLoadMore();
	}

}