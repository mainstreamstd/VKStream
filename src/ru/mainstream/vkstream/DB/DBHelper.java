package ru.mainstream.vkstream.DB;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

	protected DBHelper(Context context, String dbName, int dbVersion) {
	      super(context, dbName, null, dbVersion);
	    }
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		

		db.execSQL("create table " + AppDB.AUDIO_SEARCH_TABLE_NAME + "("
		          + "id integer primary key autoincrement," 
		          + "val text" +
		          ");");
		
		db.execSQL("create table " + AppDB.VIDEO_SEARCH_TABLE_NAME + "("
		          + "id integer primary key autoincrement," 
		          + "val text" +
		          ");");
	
	

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	

	}

}
