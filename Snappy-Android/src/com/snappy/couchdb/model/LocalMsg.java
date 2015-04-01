package com.snappy.couchdb.model;

public class LocalMsg {

	private int ID;
	private String Message;

	// Constructor de un objeto Contactos
	public LocalMsg(int id, String message) {
		this.ID = id;
		this.Message = message;

	}

	// Recuperar/establecer ID
	public int getID() {
		return ID;
	}

	public void setID(int id) {
		this.ID = id;
	}

	public String getMessage() {
		return Message;
	}

	public void setMessage(String message) {
		this.Message = message;
	}
}
