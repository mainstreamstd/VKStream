package ru.mainstream.vkstream.at;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import org.json.JSONException;

import ru.mainstream.vkstream.AudioFinder;

import com.perm.kate.api.Audio;
import com.perm.kate.api.KException;

import android.os.AsyncTask;
import android.view.View;

	

public class GetRecommendsAudiosAsync extends AsyncTask<Void, Void, ArrayList<Audio>> {

	AudioFinder parent;
	
	public boolean completed = true;

	public GetRecommendsAudiosAsync(AudioFinder parent)
	{
		this.parent = parent;

	}
	
	  
   public void link(AudioFinder parent) {
      this.parent = parent;
    }
    
    public void unLink() {
    	parent.recommendsListRefreshButton.setVisibility(View.INVISIBLE);
		parent.recommendsProgress.setVisibility(View.VISIBLE);
      parent = null;
    }
	
 	@Override
   	protected void onPreExecute()
   	{
 		completed = false;
 		
   		parent.recommendsListRefreshButton.setVisibility(View.INVISIBLE);
   		parent.recommendsProgress.setVisibility(View.VISIBLE);
   		parent.nullRecommendsAudioAlert.setVisibility(View.INVISIBLE);
   	}
   	
		@Override
		protected ArrayList<Audio> doInBackground(Void... v) {
				try {
					return parent.api.getAudioRecommendations();
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (KException e) {
					if(e.error_code == 5)
					{
						parent.lastErrorPage = 2;
						parent.startLoginActivity();
					}
				}
				
				return null;
		}
		
	    @Override
		protected void onPostExecute(ArrayList<Audio> result)
		{
	    	completed = true;
	    	
	    	if(result == null)
	    	{
	    		parent.recommendsListRefreshButton.setVisibility(View.VISIBLE);
	    		parent.nullRecommendsAudioAlert.setVisibility(View.VISIBLE);
	    		parent.recommendsProgress.setVisibility(View.INVISIBLE);
	       		return;
	    	}
	    	
			if(result.size()!=0)
			{
			parent.nullAudiosAlert.setVisibility(View.INVISIBLE);
			parent.onAudiosLoaded(result,0, 2, null);
			} else {
				parent.recommendsListRefreshButton.setVisibility(View.VISIBLE);
				parent.nullRecommendsAudioAlert.setVisibility(View.VISIBLE);
				parent.recommendsProgress.setVisibility(View.INVISIBLE);
			}
		}
	
}
