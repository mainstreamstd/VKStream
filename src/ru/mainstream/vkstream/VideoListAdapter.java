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

import com.perm.kate.api.Api;
import com.perm.kate.api.KException;
import com.perm.kate.api.Video;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Toast;

import ru.mainstream.vkstream.R;
import ru.mainstream.vkstream.tools.VkPrefs;

public class VideoListAdapter extends ArrayAdapter<View> {
	
	
	private final Context context;
	private List<View> listitems;
	private ArrayList<Video> video;
	private HashMap<Long, Video> videoTable;
	private HashMap<Long, ImageButton> imageTable;
	private VkData vkData;
	private Api api;
	
	ImageManager imageManager;
	
	private final Handler timerHandler = new Handler() {
        public void handleMessage (Message  msg) {
        	long vid = msg.getData().getLong("vid");
        	
        	ImageButton iv = imageTable.get(vid);
        	Video video = videoTable.get(vid);
        	
			File image = new File(context.getExternalCacheDir() + "/" + "v" + String.valueOf(video.vid));
			iv.setImageBitmap(BitmapFactory.decodeFile(image.getAbsolutePath()));
			iv.setOnClickListener(createBStep1Listener(iv, video));
			
        	
        }
    };
 
	public VideoListAdapter(Context context, int textViewResourceId, List<View> define, ArrayList<Video> vid) {
		super(context, textViewResourceId, define);
		this.context = context;
		this.listitems = define;
		this.video = vid;
		imageManager = new ImageManager(this.context);
		vkData = new VkPrefs(context).get();
		api = new Api(vkData.ACCESS_TOKEN, Login.APP_ID);
		videoTable = new HashMap<Long, Video>();
		imageTable = new HashMap<Long, ImageButton>();
	}
 

	@Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View v = convertView;

        
        	v = listitems.get(position);
        	final ImageButton iv = (ImageButton) v.findViewById(R.id.video_logo);
        	videoTable.put(video.get(position).vid, video.get(position));
        	imageTable.put(video.get(position).vid, iv);
        	if(!vkData.USER_ID.equals("N/A")) iv.setOnClickListener(createBStep1Listener(iv, video.get(position)));
        	imageManager.fetchImage(video.get(position).image, iv,  position, video.get(position).vid);
        
			return v;
			}
	
	
	private OnClickListener createBStep1Listener(final ImageButton iv, final Video video)
	{
		return new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				iv.setImageResource(Long.valueOf(vkData.USER_ID) != video.owner_id ?  R.drawable.add_video : R.drawable.remove_video);
			 	Timer sleepTimer = new Timer();
				sleepTimer.schedule(createIvSleepTimerTask(iv, video), 2000);
				iv.setOnClickListener(createBStep2Listener(iv, video));
			}
			
		};
	}
	
	private TimerTask createIvSleepTimerTask(final ImageButton iv, final Video video)
	{
		return  new TimerTask() {

			@Override
			public void run() {
			
				Bundle bundle = new Bundle();
				bundle.putLong("vid", video.vid);
				
				Message msg = new Message();
				msg.setData(bundle);		
				
			timerHandler.sendMessage(msg);
			}
			
		};
	}
	
	private OnClickListener createBStep2Listener(final ImageButton iv, final Video video)
	{
		return new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				new AsyncTask<Void, Void, Boolean>(){

					@Override
					protected void onPreExecute()
					{
						Toast.makeText(context, context.getResources().getString(R.string.text_wait_prompt), Toast.LENGTH_SHORT).show();
						File image = new File(context.getExternalCacheDir() + "/" + "v" + String.valueOf(video.vid));
						iv.setImageBitmap(BitmapFactory.decodeFile(image.getAbsolutePath()));
						iv.setOnClickListener(null);
					}
					
					@Override
					protected Boolean doInBackground(Void... v) {
						
						
						
						if(Long.valueOf(vkData.USER_ID) != video.owner_id)
						{
						try {
							api.addVideo(video.vid, video.owner_id);
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
								api.deleteVideo(video.vid, video.owner_id);
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
					
					@Override protected void onPostExecute(Boolean addVideo)
					{
					
						Toast.makeText(context, addVideo ? context.getResources().getString(R.string.text_add_item_prompt) : context.getResources().getString(R.string.text_remove_video_item_prompt), Toast.LENGTH_SHORT).show();
						File image = new File(context.getExternalCacheDir() + "/" + "v" + String.valueOf(video.vid));
						iv.setImageBitmap(BitmapFactory.decodeFile(image.getAbsolutePath()));
						video.owner_id = Long.valueOf(addVideo ? vkData.USER_ID : "0");
						iv.setOnClickListener(createBStep1Listener(iv, video));
					
					}
					
				}.execute();
				
			}
			
		};
	}

	
	
	
	public List<View> getItems() {
		return listitems;
	}
	
	

	
}


class ImageManager {
	
	public static final int MAX_LOADS = 6;
	
	final int TIMEOUT_CONNECTION = 5000;
	final int TIMEOUT_SOCKET = 30000;
	final int ISA_TIMEOUT = 10000;
	
	public HashMap<Long, Boolean> thumbsLoadBuffer = new HashMap<Long, Boolean>();
	
	public ArrayList<AsyncTask<Void, Void, String>> activeTasks = new ArrayList<AsyncTask<Void, Void, String>>();
	public ArrayList<AsyncTask<Void, Void, String>> remainTasks = new ArrayList<AsyncTask<Void, Void, String>>();
	
	  private final Context context;
	  static long startTime;
	 
	  public ImageManager(Context context)
	  {
		  this.context = context;
	  }
	  
	  public void fetchImage(final String iUrl, final ImageButton iView, final int position, final long vid) {
	    if ( iUrl == null || iView == null )
	      return;
	 
	    if((thumbsLoadBuffer.get(vid)!=null)&&thumbsLoadBuffer.get(vid)) return;
	    File image = new File(context.getExternalCacheDir() + "/" + "v" + String.valueOf(vid));
	    if(image.exists()) 
	    {
	    	Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath());
	        iView.setImageBitmap(bitmap);
	        thumbsLoadBuffer.put(vid, true);
	        return;
	    }
	    
	    AsyncTask<Void, Void, String> loadTask = 
	  
	    new AsyncTask<Void, Void, String>() {

	    	@Override
	    	protected void onPreExecute()
	    	{
	    		 startTime = System.currentTimeMillis();

	    	    	activeTasks.add(this);
	    		 
	    	}
	    	
			@Override
			protected String doInBackground(Void... params) {
				
				byte[] buffer = new byte[512];
			     URL imUrl = null;
			     InputStream is = null;
				 URLConnection ucon = null;
				 File image = new File(context.getExternalCacheDir() + "/" + "v" + String.valueOf(vid));
			
			     try {
					imUrl = new URL(iUrl);
					
				} catch (MalformedURLException e) {
					e.printStackTrace();
					return null;
				}

			     try {
						ucon = imUrl.openConnection();
					    ucon.setReadTimeout(TIMEOUT_CONNECTION);
					    ucon.setConnectTimeout(TIMEOUT_SOCKET);
					} catch (IOException e1) {
						e1.printStackTrace();
						return null;
					}

				   try {
					is = ucon.getInputStream();
				} catch (IOException e1) {
					e1.printStackTrace();
					return null;
				}
				
			       BufferedInputStream inStream = new BufferedInputStream(is, 512);
			       FileOutputStream outStream = null;
			       
			       try {
						outStream = new FileOutputStream(image);
					} catch (FileNotFoundException e1) {
						e1.printStackTrace();
					}
			       
			       int read = 0;
			       try {
						while ((read = inStream.read(buffer)) != -1)
						{
								if(!isCancelled())
								{
							    outStream.write(buffer,0,read); 
								} else {
									 try {
											outStream.flush();
											outStream.close();
											inStream.close();
										} catch (Exception e){
										e.printStackTrace();	
										}
									 image.delete();
									 activeTasks.remove(this);
									 remainTasks.add(this);
									 return null;
								}
	
						}
		   		
			       } catch (SocketTimeoutException e)
						{
							e.printStackTrace();
							image.delete();
							
							return null;
						} catch (IOException e1)
						{
							e1.printStackTrace();
						}
			       
			       try {
						outStream.flush();
						outStream.close();
						inStream.close();
					} catch (Exception e){
					e.printStackTrace();	
					}
			       
				try {
					return image.getCanonicalPath();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
		protected void onPostExecute(String filePath)
			{
				 Bitmap bitmap = BitmapFactory.decodeFile(filePath);
			        iView.setImageBitmap(bitmap);

			        activeTasks.remove(this);
			        thumbsLoadBuffer.put(vid, true);
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


