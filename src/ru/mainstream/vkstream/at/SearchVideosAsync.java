package ru.mainstream.vkstream.at;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import org.json.JSONException;

import ru.mainstream.vkstream.Preferences;
import ru.mainstream.vkstream.R;
import ru.mainstream.vkstream.VideoFinder;

import com.google.analytics.tracking.android.EasyTracker;
import com.perm.kate.api.KException;
import com.perm.kate.api.Video;

import android.os.AsyncTask;
import android.view.View;

	

public class SearchVideosAsync extends AsyncTask<String, Void, ArrayList<Video>> {

	VideoFinder parent;

	public boolean completed = true;
	
	public SearchVideosAsync(VideoFinder parent)
	{
		this.parent = parent;

	}
	
	  
   public void link(VideoFinder parent) {
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
		
		parent.globalVideosNullAlert.setVisibility(View.INVISIBLE);
		parent.globalProgress.setVisibility(View.VISIBLE);
		parent.alreadyHaveGlobalRequestThread = true;
	}
	
	@Override
	protected ArrayList<Video> doInBackground(String... text) {
		
		EasyTracker.getTracker().trackEvent("user_action", "search_videos", text[0], null);
		
		try {
			return parent.api.searchVideo(text[0], Preferences.getVideoSortType(parent.getApplicationContext()),
					parent.hdCheckBox.isChecked() ? "1" : "0",
					Long.valueOf(parent.getResources().getInteger(R.integer.max_videos_count)), null);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (KException e) {
		
			if(e.error_code == 5)
			{
				parent.lastErrorPage = 0;
				parent.startLoginActivity();
			}
			
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	protected void onPostExecute(ArrayList<Video> result)
	{
		completed = true;
		
		parent.alreadyHaveGlobalRequestThread = false;
		parent.currListType = 0;
		parent.globalProgress.setVisibility(View.INVISIBLE);
		if(result==null)
		{
			parent.globalVideosNullAlert.setVisibility(View.VISIBLE);
		}
		
		parent.onVideosLoaded(result, 0);
	}
	
	
}
