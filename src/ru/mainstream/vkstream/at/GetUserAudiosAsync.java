package ru.mainstream.vkstream.at;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import org.json.JSONException;

import ru.mainstream.vkstream.AudioFinder;
import ru.mainstream.vkstream.Net;
import ru.mainstream.vkstream.R;

import com.perm.kate.api.Audio;
import com.perm.kate.api.KException;

import android.os.AsyncTask;
import android.view.View;

	

public class GetUserAudiosAsync extends AsyncTask<Void, Void, ArrayList<Audio>> {

	AudioFinder parent;
	int offset;

	public boolean completed = true;
	
	public GetUserAudiosAsync(AudioFinder parent, int offset)
	{
		this.parent = parent;
		this.offset = offset;

	}
	
	  
   public void link(AudioFinder parent) {
      this.parent = parent;
    }
    
    public void unLink() {
    	parent.userListRefreshButton.setVisibility(View.INVISIBLE);
		if(offset==0) parent.userProgress.setVisibility(View.VISIBLE);
		
      parent = null;
    }
	
	@Override
   	protected void onPreExecute()
   	{
		completed = false;
		
   	    parent.userListRefreshButton.setVisibility(View.INVISIBLE);
   	    if(offset==0) parent.userProgress.setVisibility(View.VISIBLE);
   	    parent.nullUserAudioAlert.setVisibility(View.INVISIBLE);
   	}
   	
		@Override
		protected ArrayList<Audio> doInBackground(Void... v) {
			
			if(Net.isOnline(parent))
			{
				try {
							  
							  ArrayList<Audio> result =  parent.api.getAudio(null, null, null, null, offset, parent.getResources().getInteger(R.integer.max_audios_count));
							  return result;
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
			  }
				
			} else {
				return null;
			}

				return null;
		}
		
		@Override
		protected void onPostExecute(ArrayList<Audio> result)
		{
			
			completed = true;

			 parent.userProgress.setVisibility(View.INVISIBLE);
		    	if(result == null)
		    	{
		    		if(offset == 0)
		    		{
		    		 parent.userListRefreshButton.setVisibility(View.VISIBLE);
		    		}
		       		return;
		    	}
			 
			if(result.size() != 0)
			{
				 parent.userListRefreshButton.setVisibility(View.INVISIBLE);
				 parent.onAudiosLoaded(result, offset, 1, null);
			} else {
				
				if(offset == 0)
				{
				 parent.nullUserAudioAlert.setVisibility(View.VISIBLE);
				 parent.userListRefreshButton.setVisibility(View.VISIBLE);
				
				}
			}
		}
	
}
