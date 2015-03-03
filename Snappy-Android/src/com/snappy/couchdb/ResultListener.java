package com.snappy.couchdb;

public interface ResultListener<T> {
	public void onResultsSucceded(T result);
	public void onResultsFail();
}
