package ru.mainstream.vkstream.at;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import org.json.JSONException;

import ru.mainstream.vkstream.VideoFinder;
import com.perm.kate.api.KException;
import com.perm.kate.api.Video;

import android.os.AsyncTask;
import android.view.View;

	

public class GetUserVideosAsync extends AsyncTask<Void, Void, ArrayList<Video>> {

	VideoFinder parent;

	public boolean completed = true;
	
	public GetUserVideosAsync(VideoFinder parent)
	{
		this.parent = parent;

	}
	
	  
   public void link(VideoFinder parent) {
      this.parent = parent;
    }
    
    public void unLink() {
    	parent.userListRefreshButton.setVisibility(View.INVISIBLE);
		parent.userProgress.setVisibility(View.VISIBLE);
		parent = null;
    }
	
    @Override
	protected void onPreExecute()
	{
    	completed = false;
    	
    	parent.userProgress.setVisibility(View.VISIBLE);
    	parent.userListRefreshButton.setVisibility(View.INVISIBLE);
    	parent.userVideosNullAlert.setVisibility(View.INVISIBLE);
    	parent.alreadyHaveUserRequestThread = true;
	}
	
	@Override
	protected ArrayList<Video> doInBackground(Void... v) {
		try {
			return parent.api.getVideo(null, null, null, null, Long.valueOf(100), null);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (KException e) {
		
			if(e.error_code == 5)
			{
				parent.lastErrorPage = 1;
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
		
		parent.userProgress.setVisibility(View.INVISIBLE);
		parent.userListRefreshButton.setVisibility(View.INVISIBLE);
		parent.currListType = 1;
		parent.alreadyHaveUserRequestThread = false;
		
		if(result==null || result.size() == 0)
		{
			parent.userVideosNullAlert.setVisibility(View.VISIBLE);
			parent.userListRefreshButton.setVisibility(View.VISIBLE);
		}
		
		parent.onVideosLoaded(result, 1);
	}
	
}
