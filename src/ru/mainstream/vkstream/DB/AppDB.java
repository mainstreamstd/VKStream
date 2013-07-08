package ru.mainstream.vkstream.DB;

import java.util.ArrayList;

import com.perm.kate.api.Audio;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class AppDB {

	
	public static final String DB_NAME = "AppDB.db";
	public static final int DB_VERSION = 160;
	
	
	public static final String AUDIO_SEARCH_TABLE_NAME = "audio_search";
	public static final String VIDEO_SEARCH_TABLE_NAME = "video_search";

	
	public static void incertSearchString(Context context, String value, String tableName)
	{
		DBHelper dbHelper = new DBHelper(context, DB_NAME, DB_VERSION);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		
		
		ContentValues cv = new ContentValues();
		cv.put("val",  value);
		db.insert(tableName, null, cv);
		db.close();
		dbHelper.close();
	}
	
	public static ArrayList<String> readSearchHistory (Context context, String tableName)
	{
		DBHelper dbHelper = new DBHelper(context, DB_NAME, DB_VERSION);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		ArrayList<String> result = new ArrayList<String>();
		
		 Cursor c = db.query(tableName, null, null, null, null, null, null);
		
		 if (c.moveToFirst()) {

		        int valColIndex = c.getColumnIndex("val");

		        do {
		          result.add(c.getString(valColIndex));
		        } while (c.moveToNext());
		      } 
		      c.close();
		   db.close();
		 dbHelper.close();
		 
		return result;
	}
	

		public static void clearTable(Context context, String tableName)
	{
		
		DBHelper dbHelper = new DBHelper(context, DB_NAME, DB_VERSION);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.delete(tableName, null, null);
		db.close();
		dbHelper.close();
	
	}
		
	public static boolean tableExists(Context context, String tableName)
	{
		DBHelper dbHelper = new DBHelper(context, DB_NAME, DB_VERSION);
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		
		Cursor c = db.query(tableName, null, null, null, null, null, null);
		
		boolean result = c.moveToFirst();
		
		c.close();
		db.close();
		dbHelper.close();
		
		return result;
	}
}
