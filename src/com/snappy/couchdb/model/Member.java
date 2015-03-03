package com.snappy.couchdb.model;

public class Member {
	private static int totalMember = 0;
	private String id;
	private String name;
	private String image;
	private String online;

	public Member() {
		super();
	}

	public Member(String id, String name, String image, String online) {
		super();
		this.id = id;
		this.name = name;
		this.image = image;
		this.online = online;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getOnline() {
		return online;
	}

	public void setOnline(String online) {
		this.online = online;
	}
	
	public static void setTotalMember(int value) {
		totalMember = value;
	}
	
	public static int getTotalMember() {
		return totalMember;
	}

}
