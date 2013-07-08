package ru.mainstream.vkstream;

import ru.mainstream.vkstream.DB.AppDB;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.widget.Toast;

public class SettingsActivity extends PreferenceActivity {

	Preference buyProPref;
	
	Preference audioFolderPref;
	Preference videoFolderPref;
	
	ListPreference audioSortPref;
	ListPreference videoSortPref;
	
	Preference clearAudioHistButton;
	Preference clearVideoHistButton;
	

	
	public static final int CLEAR_AUDIO_HIST_DIALOG_ID = 101;
    public static final int CLEAR_VIDEO_HIST_DIALOG_ID = 102;
    
    public static final int CHOOSE_AUDIO_FILE_REQUEST = 104;
    public static final int CHOOSE_VIDEO_FILE_REQUEST = 105;

   
    
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
	    super.onCreate(savedInstanceState);
	    addPreferencesFromResource(R.xml.settings);
	    
	    buyProPref = (Preference) findPreference(getResources().getString(R.string.pref_buy_pro_key));
	    audioFolderPref = (Preference) findPreference(getResources().getString(R.string.pref_audio_folder_key));
	    videoFolderPref = (Preference) findPreference(getResources().getString(R.string.pref_video_folder_key));
	    audioSortPref = (ListPreference) findPreference(getResources().getString(R.string.pref_audio_sort_key));
	    videoSortPref = (ListPreference) findPreference(getResources().getString(R.string.pref_video_sort_key));
	    clearAudioHistButton = (Preference)findPreference(getResources().getString(R.string.pref_audio_clear_history_key));
	    clearVideoHistButton = (Preference)findPreference(getResources().getString(R.string.pref_video_clear_history_key));
	    
	   
	    setAudioListPrefs(Integer.parseInt(Preferences.getAudioSortType(getApplicationContext())));
	    setVideoListPrefs(Integer.parseInt(Preferences.getVideoSortType(getApplicationContext())));
	    
	    audioSortPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){

			@Override
			public boolean onPreferenceChange(Preference pref, Object newVal) {
				setAudioListPrefs(Integer.parseInt((String) newVal));
				return true;
			}
	    	
	    });
	    
	    
	    videoSortPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){

			@Override
			public boolean onPreferenceChange(Preference pref, Object newVal) {
				setVideoListPrefs(Integer.parseInt((String) newVal));
				return true;
			}
	    	
	    });
	    
	    audioFolderPref.setSummary(Preferences.getAudioDir(getApplicationContext()).getPath());
	    audioFolderPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference pref) {   
               startActivityForResult(new Intent(getApplicationContext(), FoldersActivity.class), CHOOSE_AUDIO_FILE_REQUEST);
                return true;
            }
        });
	    
	    videoFolderPref.setSummary(Preferences.getVideoDir(getApplicationContext()).getPath());
	    videoFolderPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference pref) {   
               startActivityForResult(new Intent(getApplicationContext(), FoldersActivity.class), CHOOSE_VIDEO_FILE_REQUEST);
                return true;
            }
        });
	    
	
	    
	    
	    buyProPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference pref) {          
            	Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse("market://details?id=ru.mainstream.vkstream_pro"));
				startActivity(intent);
                return true;
            }
        });
	    
	    clearAudioHistButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference pref) {          
               showDialog(CLEAR_AUDIO_HIST_DIALOG_ID);
                return true;
            }
        });
	    
	    clearVideoHistButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference pref) {          
            	showDialog(CLEAR_VIDEO_HIST_DIALOG_ID);
                return true;
            }
        });
	   
	    
	}
	
	private void setAudioListPrefs(int prefState)
	{
	    switch(prefState)
	    {
	    
	    case 0:
	    	audioSortPref.setSummary(getResources().getStringArray(R.array.audio_sort_items)[2]);
	    	break;
	    	
	    case 1:
	    	audioSortPref.setSummary(getResources().getStringArray(R.array.audio_sort_items)[1]);
	    	break;
	    	
	    case 2:
	    	audioSortPref.setSummary(getResources().getStringArray(R.array.audio_sort_items)[0]);
	    	break;
	    
	    }
	    
	}
	
	private void setVideoListPrefs(int prefState)
	{
	    switch(prefState)
	    {
	    
	    case 0:
	    	videoSortPref.setSummary(getResources().getStringArray(R.array.video_sort_items)[2]);
	    	break;
	    	
	    case 1:
	    	videoSortPref.setSummary(getResources().getStringArray(R.array.video_sort_items)[1]);
	    	break;
	    	
	    case 2:
	    	videoSortPref.setSummary(getResources().getStringArray(R.array.video_sort_items)[0]);
	    	break;
	    
	    }
	    
	}
	
	
	 @Override
	    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	        switch(requestCode)
	        {
	        	
	        case CHOOSE_AUDIO_FILE_REQUEST:
	        	switch(resultCode)
	        	{
	        	
	        	case RESULT_OK:
	        		Preferences.saveAudioDir(getApplicationContext(), data.getStringExtra("abs_path"));
	        		 audioFolderPref.setSummary(Preferences.getAudioDir(getApplicationContext()).getPath());
	        		break;
	        		
	        	case RESULT_CANCELED:
	        		Toast.makeText(getApplicationContext(), getResources().getString(R.string.text_choose_cancel_prompt), Toast.LENGTH_SHORT).show();
	        		break;
	        	
	        	}
	        	break;
	        	
	        case CHOOSE_VIDEO_FILE_REQUEST:
	        	switch(resultCode)
	        	{
	        	
	        	case RESULT_OK:
	        		Preferences.saveVideoDir(getApplicationContext(), data.getStringExtra("abs_path"));
	        		videoFolderPref.setSummary(Preferences.getVideoDir(getApplicationContext()).getPath());
	        		break;
	        		
	        	case RESULT_CANCELED:
	        		Toast.makeText(getApplicationContext(), getResources().getString(R.string.text_choose_cancel_prompt), Toast.LENGTH_SHORT).show();
	        		break;
	        	
	        	}
	        	break;
	        	
	        }
	        
	    }
	
	protected Dialog onCreateDialog(int id)
	{
	
		AlertDialog.Builder builder;
		
		switch(id)
		{
		case CLEAR_AUDIO_HIST_DIALOG_ID:
		
			builder = new AlertDialog.Builder(this);
			builder.setTitle(getResources().getString(R.string.dialog_confirm_action_title))
				   .setMessage(getResources().getString(R.string.dialog_clear_audio_hist_message))
				   .setPositiveButton(getResources().getString(R.string.button_positive), new OnClickListener(){

					   @Override
					   public void onClick(DialogInterface dialog, int which) {
						   AppDB.clearTable(getApplicationContext(), AppDB.AUDIO_SEARCH_TABLE_NAME);
					   }
					   
				   })
				   
				   .setNegativeButton(getResources().getString(R.string.button_negative), new OnClickListener(){

					   @Override
					   public void onClick(DialogInterface dialog, int which) {
						   
						  
					   }
					   
				   });
		
				return builder.create();
				
		case CLEAR_VIDEO_HIST_DIALOG_ID:
			
			builder = new AlertDialog.Builder(this);
			builder.setTitle(getResources().getString(R.string.dialog_confirm_action_title))
				   .setMessage(getResources().getString(R.string.dialog_clear_video_hist_message))
				   .setPositiveButton(getResources().getString(R.string.button_positive), new OnClickListener(){

					   @Override
					   public void onClick(DialogInterface dialog, int which) {
						   
						   AppDB.clearTable(getApplicationContext(), AppDB.VIDEO_SEARCH_TABLE_NAME);
					   }
					   
				   })
				   
				   .setNegativeButton(getResources().getString(R.string.button_negative), new OnClickListener(){

					   @Override
					   public void onClick(DialogInterface dialog, int which) {
						   
						  
					   }
					   
				   });
		
				return builder.create();
			
				
			
		}
		return null;
		
	}
}
