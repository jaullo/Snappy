package com.snappy.couchdb;

import java.util.ArrayList;
import java.util.List;

import com.snappy.couchdb.model.LocalMsg;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LocalDatabaseHelper extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "GCM";
	private static final String TABLE_NAME = "messagesTable";
	
	public LocalDatabaseHelper(Context context) {
	    super(context, DATABASE_NAME, null, DATABASE_VERSION);
	    // TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
	    // TODO Auto-generated method stub
	    db.execSQL("CREATE TABLE " + TABLE_NAME
	            + "(ID INTEGER PRIMARY KEY AUTOINCREMENT, MESSAGE STRING)");
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	    // TODO Auto-generated method stub
	    db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
	    onCreate(db);
	}

	public void addMessage(String msg) {
	    SQLiteDatabase db = getWritableDatabase();
	    if(db != null){
	        ContentValues valores = new ContentValues();
	        valores.put("MESSAGE", msg);
	        db.insert(TABLE_NAME, null, valores);
	        db.close();   
	    }
	}
	
	public void deleteMessage(int id) {
	    SQLiteDatabase db = getWritableDatabase();
	    db.delete(TABLE_NAME, "_id="+id, null);
	    db.close();  
	}

	public LocalMsg getAllNotifications(int id) {
		SQLiteDatabase db = getReadableDatabase();
		String[] valores_recuperar = { "ID", "MESSAGE" };
		Cursor c = db.query(TABLE_NAME, valores_recuperar, "_id=" + id, null,
				null, null, null, null);
		if (c != null) {
			c.moveToFirst();
		}
		LocalMsg contactos = new LocalMsg(c.getInt(0), c.getString(1));
		db.close();
		c.close();
		return contactos;
	}

	public List<LocalMsg> getlAllNotificationsList() {
		SQLiteDatabase db = getReadableDatabase();
		List<LocalMsg> lista_contactos = new ArrayList<LocalMsg>();
		String[] valores_recuperar = { "ID", "MESSAGE" };
		Cursor c = db.query("contactos", valores_recuperar, null, null, null,
				null, null, null);
		c.moveToFirst();
		do {
			LocalMsg contactos = new LocalMsg(c.getInt(0), c.getString(1));
			lista_contactos.add(contactos);
		} while (c.moveToNext());
		db.close();
		c.close();
		return lista_contactos;
	}
}
