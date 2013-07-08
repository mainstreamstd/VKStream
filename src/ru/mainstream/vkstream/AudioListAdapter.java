package ru.mainstream.vkstream;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;

import ru.mainstream.vkstream.tools.VkPrefs;

import ru.mainstream.vkstream.R;

import com.perm.kate.api.Api;
import com.perm.kate.api.Audio;
import com.perm.kate.api.KException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


public class AudioListAdapter extends ArrayAdapter<View> {
	private List<View> listitems=null;
	
	private Context context;
	private ArrayList<Audio> audios;
	private HashMap<Long, Audio> audioTable;
	private HashMap<Long, ImageButton> imageTable;
	private VkData vkData;
	private Api api;
	
	 
	private final Handler timerHandler = new Handler() {
        public void handleMessage (Message  msg) {
        	long aid = msg.getData().getLong("aid");
        	
  
        	
        	ImageButton iv = imageTable.get(aid);
        	Audio audio = audioTable.get(aid);
        	
			iv.setOnClickListener(createBStep1Listener(iv,audio));
			iv.setImageResource(R.drawable.audio_image);
        	
        }
    };
	
	public AudioListAdapter(Context context, int textViewResourceId, List<View> define, ArrayList<Audio> audios) {
		super(context, textViewResourceId, define);
		this.listitems = define;
		this.context = context;
		this.audios = audios;
		
		
		
		vkData = new VkPrefs(context).get();
		api = new Api(vkData.ACCESS_TOKEN, Login.APP_ID);
		
		audioTable = new HashMap<Long, Audio>();
		imageTable = new HashMap<Long, ImageButton>();
		
	}
 

	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
		
		 View v = convertView;

	        
     	v = listitems.get(position);
     	
     	if(position < audios.size())
     	{
     	final ImageButton iv = (ImageButton) v.findViewById(R.id.audio_logo);
     	final TextView tv = (TextView) v.findViewById(R.id.audio_duration_text);
     	audioTable.put(audios.get(position).aid, audios.get(position));
     	imageTable.put(audios.get(position).aid, iv);
     	if(!vkData.USER_ID.isEmpty() && !vkData.USER_ID.equals("N/A")) iv.setOnClickListener(createBStep1Listener(iv,audios.get(position)));
     	BitrateChecker.getAudioBitrate(audios.get(position).url, audios.get(position).duration, audios.get(position).aid, tv);
     	}
			return listitems.get(position);
			}
	
	
	private OnClickListener createBStep1Listener(final ImageButton iv, final Audio audio)
	{
		return new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				iv.setImageResource(Long.valueOf(vkData.USER_ID) != audio.owner_id ?  R.drawable.add_music : R.drawable.remove_music);
			 	Timer sleepTimer = new Timer();
				sleepTimer.schedule(createIvSleepTimerTask(iv, audio), 2000);
				iv.setOnClickListener(createBStep2Listener(iv, audio));
			}
			
		};
	}
	
	private TimerTask createIvSleepTimerTask(final ImageButton iv, final Audio audio)
	{
		return  new TimerTask() {

			@Override
			public void run() {
			
				Bundle bundle = new Bundle();
				bundle.putLong("aid", audio.aid);
				
				Message msg = new Message();
				msg.setData(bundle);		
				
			timerHandler.sendMessage(msg);
			}
			
		};
	}
	
	private OnClickListener createBStep2Listener(final ImageButton iv, final Audio audio)
	{
		return new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				new AsyncTask<Void, Void, Boolean>(){

					@Override
					protected void onPreExecute()
					{
						Toast.makeText(context, context.getResources().getString(R.string.text_wait_prompt), Toast.LENGTH_SHORT).show();
						iv.setImageResource(R.drawable.audio_image);
						iv.setOnClickListener(null);
					}
					
					@Override
					protected Boolean doInBackground(Void... v) {
						
						
						
						if(Long.valueOf(vkData.USER_ID) != audio.owner_id)
						{
						try {
							api.addAudio(audio.aid, audio.owner_id, null);
						} catch (MalformedURLException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						} catch (JSONException e) {
							e.printStackTrace();
						} catch (KException e) {
							e.printStackTrace();
						}
						return true;
						} else {
							try {
								api.deleteAudio(audio.aid, audio.owner_id);
							} catch (MalformedURLException e) {
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							} catch (JSONException e) {
								e.printStackTrace();
							} catch (KException e) {
								e.printStackTrace();
							}
							return false;
						}
					}
					
					@Override protected void onPostExecute(Boolean addAudio)
					{
					
						Toast.makeText(context, addAudio ? context.getResources().getString(R.string.text_add_item_prompt) : context.getResources().getString(R.string.text_remove_audio_item_prompt), Toast.LENGTH_SHORT).show();
						iv.setImageResource(R.drawable.audio_image);
						audio.owner_id = Long.valueOf(addAudio ? vkData.USER_ID : "0");
						iv.setOnClickListener(createBStep1Listener(iv, audio));
					
					}
					
				}.execute();
				
			}
			
		};
	}

	
	public List<View> getItems() {
		return listitems;
	}
	
	 
}


class BitrateChecker {

	public static final int MAX_LOADS = 6;
	
	final int TIMEOUT_CONNECTION = 5000;
	final int TIMEOUT_SOCKET = 30000;
	final int ISA_TIMEOUT = 10000;
	
	public static HashMap<Long, Boolean> bitrateLoadBuffer = new HashMap<Long, Boolean>();
	
	public static ArrayList<AsyncTask<Void, Void, Integer>> activeTasks = new ArrayList<AsyncTask<Void, Void, Integer>>();
	public static ArrayList<AsyncTask<Void, Void, Integer>> remainTasks = new ArrayList<AsyncTask<Void, Void, Integer>>();

	
	  
	  public static void getAudioBitrate(final String aUrl, final long aDuration, final long aid, final TextView tv) {
	   
	    if((bitrateLoadBuffer.get(aid)!=null)&&bitrateLoadBuffer.get(aid)) return;
	    
	    AsyncTask<Void, Void, Integer> loadTask = 
	  
	    new AsyncTask<Void, Void, Integer>() {

	    	@Override
	    	protected void onPreExecute()
	    	{
	    	    	activeTasks.add(this);
	    	}
	    	
			@Override
			protected Integer doInBackground(Void... params) {
				
			     URL imUrl = null;
				 URLConnection ucon = null;
			
			     try {
					imUrl = new URL(aUrl);
					
				} catch (MalformedURLException e) {
					e.printStackTrace();
					return null;
				}

			     try {
						ucon = imUrl.openConnection();
					} catch (IOException e1) {
						e1.printStackTrace();
						return null;
					}

				 int length = ucon.getContentLength() / 1024;
				 int bitrate =  length / (int) aDuration;
				 
				 return bitrate*10;

			}

			@Override
		protected void onPostExecute(Integer bitrate)
			{
		
			        activeTasks.remove(this);
			        bitrateLoadBuffer.put(aid, true);
			        
			        tv.setText(tv.getText().toString() + " | " +String.valueOf(bitrate));
			}
			

			@Override
		protected  void onCancelled()
		{
				 super.onCancelled();
				activeTasks.remove(this);
				if(!remainTasks.contains(this)) remainTasks.add(this);
		}
	    	
	    };
	    
	   loadTask.execute();
	   try {
		   activeTasks.get(activeTasks.size()-1-MAX_LOADS).cancel(true);
	   } catch (ArrayIndexOutOfBoundsException ex){}

	  }
	  

}