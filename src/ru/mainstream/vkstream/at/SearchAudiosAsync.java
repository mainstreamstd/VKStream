package ru.mainstream.vkstream.at;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import org.json.JSONException;

import ru.mainstream.vkstream.AudioFinder;
import ru.mainstream.vkstream.CapchaDialog;
import ru.mainstream.vkstream.Preferences;
import ru.mainstream.vkstream.R;

import com.google.analytics.tracking.android.EasyTracker;
import com.perm.kate.api.Audio;
import com.perm.kate.api.KException;

import android.os.AsyncTask;
import android.view.View;

	

public class SearchAudiosAsync extends AsyncTask<Void, Void, AudioResult> {

	AudioFinder parent;
	String query;
	String capchaSid;
	String capchaKey;

	int offset;
	
	public boolean completed = true;

	public SearchAudiosAsync(AudioFinder parent,String query , int offset, String capchaSid, String capchaKey)
	{
		this.parent = parent;
		this.offset = offset;
		this.query = query;
		this.capchaSid = capchaSid;
		this.capchaKey = capchaKey;

	}
	
	  
   public void link(AudioFinder parent) {
      this.parent = parent;
    }
    
    public void unLink() {
    	   parent.globalProgress.setVisibility(View.VISIBLE);
      parent = null;
    }
	
    @Override
	protected void onPreExecute()
	{
    	completed = false;
    	
		parent.nullAudiosAlert.setVisibility(View.INVISIBLE);
		if(offset==0) parent.globalProgress.setVisibility(View.VISIBLE);
		
	}
	
	@Override
	protected AudioResult doInBackground(Void... v) {
		
		EasyTracker.getTracker().sendEvent("user_action", "search_audios", query, null);
		
		AudioResult result = new AudioResult();
		
			try {
				
			
				result.audios = parent.api.searchAudio(query, Preferences.getAudioSortType(parent.getApplicationContext()),
						null, Long.valueOf(parent.getResources().getInteger(R.integer.max_audios_count)), Long.valueOf(offset), capchaSid, capchaKey);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (KException e) {
				
					result.audios = null;
					result.kExp = e;
				return result;
			}
			result.hasResult = true;
			return result;
	}
	
	@Override
	protected void onPostExecute(AudioResult result)
	{
    	completed = true;
    	
    	
    	
    	if(!result.hasResult)
    	{
       		switch(result.kExp.error_code)
       		{
       		case 5:
       			parent.lastErrorPage = 0;
				parent.startLoginActivity();
				break;
       		case 14:
       			
       			CapchaDialog capcha = new CapchaDialog(parent, result.kExp.captcha_img, result.kExp.captcha_sid, query);
       			capcha.show();
       			
       			break;
       		}
       		
    	}
		
		if(result.audios!=null && result.audios.size()!=0)
		{
			parent.nullAudiosAlert.setVisibility(View.INVISIBLE);
			parent.onAudiosLoaded(result.audios,offset, 0, query);
		} else {
			parent.nullAudiosAlert.setVisibility(View.VISIBLE);
       		parent.globalProgress.setVisibility(View.INVISIBLE);
		}
	}
	
}

class AudioResult
{
	public boolean hasResult = false;
	
	public ArrayList<Audio> audios;
	public KException kExp;
}
