package com.snappy.couchdb;

import java.util.ArrayList;
import java.util.List;

import com.snappy.couchdb.model.LocalMessage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LocalDB extends SQLiteOpenHelper {

	private static final int dbVersion = 1;

	private static final String dbName = "snappy.db";

	private static final String sentence = "CREATE TABLE messages"
			+ "(msgId INTEGER PRIMARY KEY AUTOINCREMENT, message TEXT)";

	private static final String tableName = "messages";

	public LocalDB(Context context) {
		super(context, dbName, null, dbVersion);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(sentence);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + tableName);
		onCreate(db);
	}

	public void writeMessage(String message) {
		SQLiteDatabase db = getWritableDatabase();
		//db.beginTransaction();
		
		if (db != null) {
			ContentValues valores = new ContentValues();
			valores.put("message", message);
			db.insert(tableName, null, valores);
			db.close();
		}
		
		// db.setTransactionSuccessful();// marks a commit
		 //db.endTransaction();
	}

	public void modifymessage(int id, String message) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues valores = new ContentValues();
		valores.put("message", message);
		db.update(tableName, valores, "msgId=" + id, null);
		db.close();
	}

	public void deleteMessage(int id) {
		SQLiteDatabase db = getWritableDatabase();
		db.delete(tableName, "msgId=" + id, null);
		db.close();
	}
	
	public void deleteAllMessages() {
		SQLiteDatabase db = getWritableDatabase();
		//db.beginTransaction();
		
		db.delete(tableName, null, null);
		db.close();
		
		// db.setTransactionSuccessful();// marks a commit
		// db.endTransaction();
	}

	public LocalMessage getMessageById(int id) {
		SQLiteDatabase db = getReadableDatabase();
		String[] valores_recuperar = { "msgId", "message" };
		Cursor c = db.query(tableName, valores_recuperar, "msgId=" + id, null,
				null, null, null, null);
		if (c != null) {
			c.moveToFirst();
		}
		LocalMessage messages = new LocalMessage(c.getInt(0), c.getString(1));
		db.close();
		c.close();
		return messages;
	}

	public List<LocalMessage> getAllMessages() {
		SQLiteDatabase db = getReadableDatabase();
		List<LocalMessage> lista_contactos = new ArrayList<LocalMessage>();
		String[] valores_recuperar = {"msgId", "message" };
		Cursor c = db.query(tableName, valores_recuperar, null, null, null,
				null, null, null);
		c.moveToFirst();
		do {
			LocalMessage messages = new LocalMessage(c.getInt(0),
					c.getString(1));
			lista_contactos.add(messages);
		} while (c.moveToNext());
		db.close();
		c.close();
		return lista_contactos;
	}
}
