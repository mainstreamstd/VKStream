package ru.mainstream.vkstream;

import java.io.File;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

public class Preferences {
	
	public static final String prefsName = "ru.mainstream.vkstream_preferences";
	
	private static SharedPreferences.Editor prefsEditor;
	
	public static File getAudioDir(Context context)
	{
		
	 String path = context.getSharedPreferences(prefsName, 0)
			 .getString(context.getString(R.string.pref_audio_folder_key),
					 Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+context.getResources().getString(R.string.default_audio_folder_path));
	 
	 if(!path.startsWith(Environment.getExternalStorageDirectory().getAbsolutePath()))
		 path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+context.getResources().getString(R.string.default_audio_folder_path);
	 
	 File mFile = new File(path);
	 if(!(mFile.exists()&&mFile.isDirectory()))  mFile.mkdir();
	 return mFile;
	}
	
	
	
	public static void saveAudioDir(Context context, String path)
	{
		
		prefsEditor = context.getSharedPreferences(prefsName, 0).edit();
		prefsEditor.putString(context.getString(R.string.pref_audio_folder_key), path);
		prefsEditor.commit();
	}
	
	
	public static File getVideoDir(Context context)
	{
	 String path = context.getSharedPreferences(prefsName, 0)
			 .getString(context.getString(R.string.pref_video_folder_key),
					 Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+ context.getResources().getString(R.string.default_video_folder_path));
	 
	 if(!path.startsWith(Environment.getExternalStorageDirectory().getAbsolutePath()))
		 path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+context.getResources().getString(R.string.default_audio_folder_path);
	 
	 File vFile = new File(path);
	 if(!(vFile.exists()&&vFile.isDirectory()))  vFile.mkdir();
	 return vFile;
	}
	
	
	
	public static void saveVideoDir(Context context, String path)
	{
		
		prefsEditor = context.getSharedPreferences(prefsName, 0).edit();
		prefsEditor.putString(context.getString(R.string.pref_video_folder_key), path);
		prefsEditor.commit();
	}
	
	
	
	public static boolean isAudioHistorySaving(Context context)
	{
		return context.getSharedPreferences(prefsName, 0).
				getBoolean(context.getString(R.string.pref_audio_history_key),
						context.getResources().getBoolean(R.bool.pref_audio_history_default));
	}
	
	public static boolean isVideoHistorySaving(Context context)
	{
		return context.getSharedPreferences(prefsName, 0).
				getBoolean(context.getString(R.string.pref_video_history_key),
						context.getResources().getBoolean(R.bool.pref_video_history_default));
	}
	
	public static boolean isVideoCacheAllowed(Context context)
	{
		return context.getSharedPreferences(prefsName, 0).
				getBoolean(context.getString(R.string.pref_video_allowcache_key),
						context.getResources().getBoolean(R.bool.pref_video_cache_default));
	}
	
	
	
	public static String getAudioSortType(Context context)
	{
		return context.getSharedPreferences(prefsName, 0).
				getString(context.getString(R.string.pref_audio_sort_key),
						context.getResources().getString(R.string.pref_audio_sort_default));
	}
	
	public static String getVideoSortType(Context context)
	{
		return context.getSharedPreferences(prefsName, 0).
				getString(context.getString(R.string.pref_video_sort_key),
						context.getResources().getString(R.string.pref_video_sort_default));
	}
	
}
