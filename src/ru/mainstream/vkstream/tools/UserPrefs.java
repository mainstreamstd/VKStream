package ru.mainstream.vkstream.tools;

import android.content.Context;
import android.content.SharedPreferences;

public class UserPrefs {

	private Context ctx;
	private static SharedPreferences _prefs;
	private SharedPreferences.Editor _editor;
	
	public UserPrefs(String prefsName,Context context)
	{
		ctx = context;
		_prefs = ctx.getSharedPreferences(prefsName, 0);
		_editor = _prefs.edit();
	}
	
	public void savePref(String key,String value)
	{
		_editor.putString(key, value);
		_editor.commit();
	}
	
	public void savePref(String key,int value)
	{
		_editor.putInt(key, value);
		_editor.commit();
	}
	
	public void savePref(String key,boolean value)
	{
		_editor.putBoolean(key, value);
		_editor.commit();
	}
	
	public String getStringPref(String key)
	{
		return _prefs.getString(key, "NULL");
	}
	
	public int getIntPref(String key)
	{
		return _prefs.getInt(key, 0);
	}
	
	public boolean getBooleanPref(String key)
	{
		return _prefs.getBoolean(key, false);
	}
}
