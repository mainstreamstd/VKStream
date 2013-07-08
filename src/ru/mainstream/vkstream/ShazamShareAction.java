package ru.mainstream.vkstream;

import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockActivity;

public class ShazamShareAction extends SherlockActivity {

	public static String SHARE_ACTION = "VKS.shareTitle";
	public static String EXTRA_TITLE = "VKS.audioTitle";
	
	@Override
	public void onCreate(Bundle bundle)
	{
		
		super.onCreate(bundle);
		
		 Intent intent = getIntent();
		    String action = intent.getAction();
		    String type = intent.getType();

		    
		    if (Intent.ACTION_SEND.equals(action) && type != null) {
		        if ("text/plain".equals(type)) {
		        	
		        	String sharedTitle = intent.getStringExtra(Intent.EXTRA_SUBJECT);
		        	
		        	Intent audioFinderIntent = new Intent(getApplicationContext(), AudioFinder.class);
		        	audioFinderIntent.setAction(SHARE_ACTION);
		        	audioFinderIntent.putExtra(EXTRA_TITLE, sharedTitle);
		        	
		        	startActivity(audioFinderIntent);
		        	
		        	finish();
		        	
		        } 
		     } else {
		    	 finish();
		     }
	}
	
}
