package ru.mainstream.vkstream;



import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.google.analytics.tracking.android.EasyTracker;


import com.perm.kate.api.Api;

import ru.mainstream.vkstream.tools.UserPrefs;
import ru.mainstream.vkstream.tools.VkPrefs;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;


public class MenuAct extends SherlockActivity {



	WebView view;
	Button loginButton;
	TextView loginInfoText;
	
	private final int LOGIN_DIALOG_ID=101;
	private final int NEW_VERSION_MESSAGE_DIALOG_ID=102;
	private final int REQUEST_LOGIN=1;
	
	Api api;
	VkData vkData;
	VkPrefs prefs;
	UserPrefs uPrefs;
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);


        view = (WebView)findViewById(R.id.loginView);
        loginButton = (Button) findViewById(R.id.login_button);
        loginInfoText = (TextView) findViewById(R.id.login_info_text);
        uPrefs = new UserPrefs("VKStreamPrefs",this);
        prefs = new VkPrefs(this);



        if(!uPrefs.getBooleanPref("notFirst1800"))
        {
        	CookieManager.getInstance().removeAllCookie();
        	new VkPrefs(this).reset();
        	uPrefs.savePref("notFirst1800", true);
        }
        
        if(!uPrefs.getBooleanPref("notFirst"))
        {
        	showDialog(NEW_VERSION_MESSAGE_DIALOG_ID);
        	uPrefs.savePref("notFirst", true);
        }
        
        vkData = prefs.get();
      
      		if(!vkData.LAST_IP.equals("N/A") && !vkData.ACCESS_TOKEN.equals("") && !vkData.ACCESS_TOKEN.equals(getResources().getString(R.string.default_token)))
        	{
      			loginInfoText.setText(getResources().getString(R.string.text_autorize_prompt) + " \n" + 
      								  vkData.USER_FIRST_NAME + " " + vkData.USER_LAST_NAME);
      			loginButton.setText(getResources().getString(R.string.button_unlogin_text));
      			
      			loginButton.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						
		        		prefs.reset();
		        		CookieManager.getInstance().removeAllCookie();
		        		api = new Api(getResources().getString(R.string.default_token), Login.APP_ID);
		        		
		        		vkData.ACCESS_TOKEN = getResources().getString(R.string.default_token);
		        		vkData.USER_ID = "N/A";
		        		
		        		prefs.reset();
		        		prefs.save(vkData);
						loginInfoText.setText(getResources().getString(R.string.text_not_autorize_prompt));
	        			loginButton.setText(getResources().getString(R.string.button_login_text));
	        			
	        			loginButton.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View arg0) {
								startLoginActivity();
							}
	        				
	        			});
						 
					}
      				
      			});
      			
    	  		api = new Api(vkData.ACCESS_TOKEN, Login.APP_ID);
        		} else {
        			loginInfoText.setText(getResources().getString(R.string.text_not_autorize_prompt));
        			loginButton.setText(getResources().getString(R.string.button_login_text));
        			
        			loginButton.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							startLoginActivity();
						}
        				
        			});
        			api = new Api(getResources().getString(R.string.default_token), Login.APP_ID);
        			vkData.ACCESS_TOKEN = getResources().getString(R.string.default_token);
        			vkData.USER_ID = "N/A";
        			
        			prefs.reset();
        			prefs.save(vkData);
        			
        		}
    }
	
   
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		menu.add(getResources().getString(R.string.menuitem_settings_label))
		.setIcon(R.drawable.ic_action_settings)
		
		.setOnMenuItemClickListener(new OnMenuItemClickListener(){

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				Intent i = new Intent(MenuAct.this, SettingsActivity.class);
				startActivity(i);
				return true;
			}
			
		})
		
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		
		return true;
	}
	
	
	@Override
	public void onStart()
	{
		super.onStart();
		EasyTracker.getInstance().activityStart(this);
	}
	 
	 @Override
	 public void onStop()
	 {
		 super.onStop();
		 EasyTracker.getInstance().activityStop(this);
	 }
	
	 @Override
	 public void onResume()
	 {
		 super.onResume();
		 
		 prefs = new VkPrefs(this);
	        vkData = prefs.get();
	      
	      		if(!vkData.LAST_IP.equals("N/A") && !vkData.ACCESS_TOKEN.isEmpty() && !vkData.ACCESS_TOKEN.equals(getResources().getString(R.string.default_token)))
	        	{
	      			loginInfoText.setText(getResources().getString(R.string.text_autorize_prompt) + " \n" + 
	      								  vkData.USER_FIRST_NAME + " " + vkData.USER_LAST_NAME);
	      			loginButton.setText(getResources().getString(R.string.button_unlogin_text));
	      			
	      			loginButton.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							
			        		prefs.reset();
			        		CookieManager.getInstance().removeAllCookie();
			        		api = new Api(getResources().getString(R.string.default_token), Login.APP_ID);
			        		
			        		vkData.ACCESS_TOKEN = getResources().getString(R.string.default_token);
			        		vkData.USER_ID = "N/A";
			        		
			        		prefs.reset();
			        		prefs.save(vkData);
							loginInfoText.setText(getResources().getString(R.string.text_not_autorize_prompt));
		        			loginButton.setText(getResources().getString(R.string.button_login_text));
		        			
		        			loginButton.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View arg0) {
									startLoginActivity();
								}
		        				
		        			});
							 
						}
	      				
	      			});
	      			
	    	  		api = new Api(vkData.ACCESS_TOKEN, Login.APP_ID);
	        		} else {
	        			loginInfoText.setText(getResources().getString(R.string.text_not_autorize_prompt));
	        			loginButton.setText(getResources().getString(R.string.button_login_text));
	        			
	        			loginButton.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View arg0) {
								startLoginActivity();
							}
	        				
	        			});
	        			api = new Api(getResources().getString(R.string.default_token), Login.APP_ID);
	        			vkData.ACCESS_TOKEN = getResources().getString(R.string.default_token);
	        			vkData.USER_ID = "N/A";
	        			
	        			prefs.reset();
	        			prefs.save(vkData);
	        			
	        		}
		 
	 }
	 
	private void startLoginActivity() 
	{
		Intent i = new Intent(this,Login.class);
		startActivityForResult(i, REQUEST_LOGIN);
	}
	
	 @Override
	    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	        if (requestCode == REQUEST_LOGIN) {
	            if (resultCode == RESULT_OK) {
	            	
	            	vkData = new VkData();
	            	
	            	vkData.ACCESS_TOKEN = data.getStringExtra("token");
	            	vkData.USER_FIRST_NAME = data.getStringExtra("first_name");
	            	vkData.USER_LAST_NAME = data.getStringExtra("last_name");
	            	vkData.USER_ID = String.valueOf(data.getLongExtra("user_id", 0));
	            	vkData.LAST_IP = Net.getLocalIpAddress();
	            	prefs.save(vkData);
	            	
	            	loginInfoText.setText(getResources().getString(R.string.text_autorize_prompt) + " \n" + 
							  vkData.USER_FIRST_NAME + " " + vkData.USER_LAST_NAME);
	            	loginButton.setText(getResources().getString(R.string.button_unlogin_text));
	            	
	            	loginButton.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							
			        		prefs.reset();
			        		CookieManager.getInstance().removeAllCookie();
			          		api = new Api(getResources().getString(R.string.default_token), Login.APP_ID);
			        		vkData.ACCESS_TOKEN = getResources().getString(R.string.default_token);
			        		vkData.USER_ID = "N/A";
			        		
			        		prefs.reset();
			        		prefs.save(vkData);
							loginInfoText.setText(getResources().getString(R.string.text_not_autorize_prompt));
							loginButton.setText(getResources().getString(R.string.button_login_text));
							
						}
	      				
	      			});
	            	
	            	api = new Api(vkData.ACCESS_TOKEN, Login.APP_ID);
	            	
	            }
	        }
	    }
	
	
    public void goToAudio(View v)
	{

    	Intent i = new Intent(this,AudioFinder.class);
    	String token = vkData.ACCESS_TOKEN;
    	i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
    	i.putExtra("token",token);
    	  startActivity(i);
	}
    
    public void goToVideo(View v)
	{
    	Intent i = new Intent(this,VideoFinder.class);
    	
    	String token = vkData.ACCESS_TOKEN;
    	i.putExtra("token",token);
    	i.putExtra("toMusic", false);
    	  startActivity(i);
   
	}
    
   
    
    @Override
    protected Dialog onCreateDialog(int id)
    {
    	
    	AlertDialog.Builder builder;
    	
    	switch (id){
    		case LOGIN_DIALOG_ID:
  	    	  ProgressDialog mProgressDialog0 = new ProgressDialog(
  	   				MenuAct.this);
  	   		    mProgressDialog0.setProgressStyle(ProgressDialog.STYLE_SPINNER);
  	   		    mProgressDialog0.setMessage(getResources().getString(R.string.dialog_autorize_message));
  	   		    mProgressDialog0.setCancelable(false);
  	   		    return mProgressDialog0;
  	   		    
    		case NEW_VERSION_MESSAGE_DIALOG_ID:
    			
    			builder = new AlertDialog.Builder(this);
				
				builder.setTitle(getResources().getString(R.string.dialog_new_version_title));
				builder.setMessage(getResources().getString(R.string.dialog_new_version_message));
				
				builder.setPositiveButton(getResources().getString(R.string.button_ok), new DialogInterface.OnClickListener(){

					public void onClick(DialogInterface dialog, int arg1) {
						
							dismissDialog(NEW_VERSION_MESSAGE_DIALOG_ID);
					}
					
					
				});
				
				return builder.create();
  	   		    
  	   		   default:
  	   			   return null;
    	}
    }

}
