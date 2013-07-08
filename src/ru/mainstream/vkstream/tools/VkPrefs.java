package ru.mainstream.vkstream.tools;

import ru.mainstream.vkstream.Net;
import ru.mainstream.vkstream.R;
import ru.mainstream.vkstream.VkData;
import android.content.Context;
import android.content.SharedPreferences;


public class VkPrefs {
	
	private Context context;
	private SharedPreferences prefs;
	private SharedPreferences.Editor prefsEditor;
	
	private final String prefsName = "VkSettings";
	private final String tokenPublicName = "access";
	private final String userIdPublicName = "user";
	private final String accessTimePublicName = "lastTime";
	private final String localIpPublicName = "local_ip";
	private final String userFirstNamePublicName = "first_name";
	private final String userLastNamePublicName = "last_name";
	
	public VkPrefs(Context context)
	{
		this.context = context;
		prefs = this.context.getSharedPreferences(prefsName, 0);
		prefsEditor = prefs.edit();
	}

	public void save(VkData data)
	{
			prefsEditor.putString(tokenPublicName, data.ACCESS_TOKEN);
			prefsEditor.putString(userIdPublicName, data.USER_ID);
			prefsEditor.putString(userFirstNamePublicName, data.USER_FIRST_NAME);
			prefsEditor.putString(userLastNamePublicName, data.USER_LAST_NAME);
			prefsEditor.putLong(accessTimePublicName, System.currentTimeMillis());
			prefsEditor.putString(localIpPublicName, Net.getLocalIpAddress());
			prefsEditor.commit();
	}
	
	public void saveToken(String token)
	{
		prefsEditor.putString(tokenPublicName, token);
		prefsEditor.commit();
	}
	
	public VkData get()
	{
		VkData data = new VkData();
		
		data.ACCESS_TOKEN = prefs.getString(tokenPublicName, "");
		data.USER_ID = prefs.getString(userIdPublicName, "-1");
		data.LAST_IP = prefs.getString(localIpPublicName, "N/A");
		data.USER_FIRST_NAME = prefs.getString(userFirstNamePublicName, "N/A");
		data.USER_LAST_NAME = prefs.getString(userLastNamePublicName, "N/A");
		
		return data;
	}
	
	public void reset()
	{
		prefsEditor.putString(tokenPublicName, context.getResources().getString(R.string.default_token));
		prefsEditor.putString(userIdPublicName, "-1");
		prefsEditor.putLong(accessTimePublicName, 0);
		prefsEditor.putString(userFirstNamePublicName, "");
		prefsEditor.putString(userLastNamePublicName, "");
		prefsEditor.commit();
	}
}
