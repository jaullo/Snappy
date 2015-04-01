package com.snappy.couchdb.model;

public class LocalMessage {

	private int id;
	private String message;

	public LocalMessage(int id, String message) {
		this.id = id;
		this.message = message;

	}

	public int getID() {
		return id;
	}

	public void setID(int id) {
		this.id = id;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
