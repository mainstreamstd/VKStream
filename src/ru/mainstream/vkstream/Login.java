package ru.mainstream.vkstream;


import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import org.json.JSONException;

import com.actionbarsherlock.app.SherlockActivity;
import com.perm.kate.api.Api;
import com.perm.kate.api.Auth;
import com.perm.kate.api.KException;
import com.perm.kate.api.User;


import ru.mainstream.vkstream.R;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;

import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;



public class Login extends SherlockActivity {

	public static final String APP_ID = "2709622";
	public static final String settings = "audio,video,offline";

    private	WebView loginView;
    private Client client;
    

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		
		loginView = (WebView) findViewById(R.id.loginView);
		client = new Client();
		
		 String url = Auth.getUrl(APP_ID, settings);
		 
		 loginView.setWebViewClient(client);
		 loginView.getSettings().setJavaScriptEnabled(true);
		 loginView.loadUrl(url);
		 
		 
	}
	

	
	   @Override
	    protected Dialog onCreateDialog(int id)
	    {
	  	    	  ProgressDialog waitDialog = new ProgressDialog(
	  	   				Login.this);
	  	    	waitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	  	    	waitDialog.setMessage(getResources().getString(R.string.dialog_autorize_message));
	  	    	waitDialog.setCancelable(true);
	  	   		    return waitDialog;
	    	}
	   
	
	   
	   class Client extends WebViewClient {

			 @Override
		     public void onPageStarted(WebView view, String url, Bitmap favicon) {
		         super.onPageStarted(view, url, favicon);
		         showDialog(1);
		         parseUrl(url);
		     }
			 
			 @Override
			 public void onPageFinished(WebView view, String url)
			 {
				 if(url.startsWith("http://oauth.vk.com/oauth/")||url.startsWith("http://oauth.vk.com/authorize?"))
					 {
					  try{	dismissDialog(1); } catch (Exception ex) {}
					 }
			 }
			
			 private void parseUrl(String url) {
				
			        try {
			            if(url==null)
			                return;
			            if(url.startsWith(Auth.redirect_url))
			            {
			            	
			                if(!url.contains("error=")){
			                
			                	loginView.setVisibility(View.INVISIBLE);
			                	
			                    final String[] auth=Auth.parseRedirectUrl(url);
			                    final Intent intent=new Intent();
			                    intent.putExtra("token", auth[0]);
			                    intent.putExtra("user_id", Long.parseLong(auth[1]));
			                    
			                    new AsyncTask<Void, Void, User>() {

									@Override
									protected User doInBackground(Void... arg0) {
										
										Api api = new Api(auth[0], APP_ID);
										ArrayList<Long> uid = new ArrayList<Long>();
										uid.add(Long.parseLong(auth[1]));
										ArrayList<User> user = null;
										try {
											user = api.getProfiles(uid, null, "first_name,last_name", "nom");
										} catch (MalformedURLException e) {
											e.printStackTrace();
										} catch (IOException e) {
											e.printStackTrace();
										} catch (JSONException e) {
											e.printStackTrace();
										} catch (KException e) {
											e.printStackTrace();
										}
										
										return user.get(0);
									}
									
									@Override
									public void onPostExecute(User user)
									{
										intent.putExtra("first_name", user.first_name);
										intent.putExtra("last_name", user.last_name);
										
										setResult(Activity.RESULT_OK, intent);
										 try { dismissDialog(1); } catch (Exception ex) {}
							                finish();
									}
			                    	
			                    }.execute();
			                    
			                    
			                }
			               
			            } 
			            	
			            
			        } catch (Exception e) {
			            e.printStackTrace();
			        }
			    }
	   }
	
}

