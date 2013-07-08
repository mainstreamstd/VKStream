package ru.mainstream.vkstream;



import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Tracker;
import com.perm.kate.api.Api;
import com.perm.kate.api.Video;
import com.viewpagerindicator.TitlePageIndicator;

import ru.mainstream.vkstream.R;
import ru.mainstream.vkstream.DB.AppDB;
import ru.mainstream.vkstream.at.GetUserVideosAsync;
import ru.mainstream.vkstream.at.SearchVideosAsync;
import ru.mainstream.vkstream.tools.AppManager;
import ru.mainstream.vkstream.tools.FileSysHelper;
import ru.mainstream.vkstream.tools.VkPrefs;
import ru.mainstream.vkstream.video.Source;
import ru.mainstream.vkstream.video.VideoPlayerHacker;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;



public class VideoFinder extends SherlockActivity {

	View globalVideoMenu;
	View userVideoMenu;
	ViewPager viewPager;

	public Api api;
	
	Tracker mainTracker;
	
	private static final int IDD_VIDEO_SHOW_QUAL_CHOOSE_DIALOG = 0xacd1;
	private static final int IDD_VIDEO_LOAD_QUAL_CHOOSE_DIALOG = 0xacd2;
	private static final int IDD_VIDEO_QUAL_WAIT_DIALOG  = 0xbaa0;
	private static final int IDD_VIDEO_VOID_ALERT = 0xdee0;
	private static final int IDD_VIDEO_RUTUBE_ALERT = 0xdaf1;
	private final int LOGIN_CONFIM_DIALOG_ID = 0xbdaffa;
	
	BroadcastReceiver downloadReceiver;
	
	public boolean alreadyHaveGlobalRequestThread = false;
	public boolean alreadyHaveUserRequestThread = false;
	
	private final int REQUEST_LOGIN=1;
	
	private SearchVideosAsync sVideosAsync;
	private GetUserVideosAsync uVideosAsync;
	
	private AutoCompleteTextView searchTextInput;
	public ListView globalVideoList;
	public ArrayList<View> globalDefine = null;
	public TextView globalVideosNullAlert;
	public ProgressBar globalProgress;
	public CheckBox hdCheckBox;
	
	public ListView userVideoList;
	public ProgressBar userProgress;
	public ArrayList<View> userDefine = null;
	public Button userListRefreshButton;
	public TextView userVideosNullAlert;
	
	
	private VkPrefs vPrefs;

	public int lastErrorPage = 0; 
	
	VideoListAdapter globalAdapter;
	VideoListAdapter userAdapter;
	
	private Source currVideoSource;
	private Video currVideo;
	
	private ArrayList<Video> globalVideos;
	private ArrayList<Video> userVideos;
	
	private boolean stillPrepareVideo = false;
	private boolean qualDialogOpened = false;
	
	InputMethodManager inputManager;
	
	private int currPage = 0;
	
	public int currListType = 0;
	
	DownloadManager downloadManager;
	
	private HashMap<Long, Video> downloadMap = new HashMap<Long, Video>();
	
	NotificationManager notificationManager;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		 
		 setContentView(R.layout.video_finder_host);
		 
		 	vPrefs = new VkPrefs(this);
		 	notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	    	api = new Api(vPrefs.get().ACCESS_TOKEN, Login.APP_ID);
	        
	    	EasyTracker.getInstance().setContext(getApplicationContext());
			mainTracker = EasyTracker.getTracker();
			downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
		
		

			ConfigurationData config = (ConfigurationData) getLastNonConfigurationInstance(); 
			
			if(config!=null) 
				{
				doPostFlipActions(config);
				} else {
					initUI();
				}
			
			setDownloadManagerReceiver();

	}
	
	

	
	
	@Override
	public void onStart()
	{
		super.onStart();
		EasyTracker.getInstance().activityStart(this);
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		if((globalAdapter==null||userAdapter==null))
		{
		vPrefs = new VkPrefs(this);
		api = new Api(vPrefs.get().ACCESS_TOKEN, Login.APP_ID);
		}
	}
	
	@Override
	public void onStop()
	{
		super.onStop();
		EasyTracker.getInstance().activityStop(this);
	}
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		unregisterReceiver(downloadReceiver);
		if(!Preferences.isVideoCacheAllowed(getApplicationContext()))
			FileSysHelper.clearDir(getExternalCacheDir().getAbsolutePath());
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		
		
		menu.add(getResources().getString(R.string.menuitem_audio_label))
		.setIcon(R.drawable.ic_action_to_audios)
		.setOnMenuItemClickListener(new OnMenuItemClickListener(){

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				Intent i = new Intent(VideoFinder.this,AudioFinder.class);
				i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		    	String token = vPrefs.get().ACCESS_TOKEN;		
		    	i.putExtra("token",token);
		    	  startActivity(i);
				return true;
			} 
			
		})
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		
		if(currPage!=0)
		{
		menu.add(getResources().getString(R.string.menuitem_refresh_label))
		.setIcon(R.drawable.ic_action_refresh)
		.setOnMenuItemClickListener(new OnMenuItemClickListener(){

			@Override
			public boolean onMenuItemClick(MenuItem item) {

				RefreshCurrList();
				return true;
			}
			
		})
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		}
		
		menu.add(getResources().getString(R.string.menuitem_settings_label))
		.setIcon(R.drawable.ic_action_settings)
		
		.setOnMenuItemClickListener(new OnMenuItemClickListener(){

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				Intent i = new Intent(VideoFinder.this, SettingsActivity.class);
				startActivity(i);
				return true;
			}
			
		})
		
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		
		
		
		
		return true;
	}
	
	private void initUI()
	{
		LayoutInflater inflater = LayoutInflater.from(this);
        List<View> pages = new ArrayList<View>();
        
        
        globalVideoMenu  = inflater.inflate(R.layout.video_menu, null);
        
        inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		
    	searchTextInput = (AutoCompleteTextView)globalVideoMenu.findViewById(R.id.video_search_view);
    	searchTextInput.setOnKeyListener(new OnKeyListener(){

			public boolean onKey(View v, int keyCode, KeyEvent event) {
				 if(event.getAction() == KeyEvent.ACTION_DOWN && 
						    (keyCode == KeyEvent.KEYCODE_ENTER))
							{
							searchVideos(searchTextInput.getText().toString());
							inputManager.hideSoftInputFromWindow(searchTextInput.getWindowToken(), 0);
							ArrayList<String> sHistory = AppDB.readSearchHistory(getApplicationContext(), AppDB.VIDEO_SEARCH_TABLE_NAME);
							if(!sHistory.contains(searchTextInput.getText().toString()))
							{
							AppDB.incertSearchString(getApplicationContext(), searchTextInput.getText().toString(), AppDB.VIDEO_SEARCH_TABLE_NAME);
							sHistory.add(searchTextInput.getText().toString());
							searchTextInput.setAdapter(new ArrayAdapter<String> (VideoFinder.this, android.R.layout.simple_spinner_dropdown_item, sHistory));
							}
								return true;
							}
				return false;
			}
    		
    	});
    	
     	searchTextInput.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
				// TODO Auto-generated method stub
				
			}
    		
    	});
    
    	searchTextInput.setAdapter(new ArrayAdapter<String> (this, android.R.layout.simple_spinner_dropdown_item, AppDB.readSearchHistory(getApplicationContext(), AppDB.VIDEO_SEARCH_TABLE_NAME)));
        
        
		globalVideoList = (ListView)globalVideoMenu.findViewById(R.id.videoArray);
		globalVideosNullAlert = (TextView)globalVideoMenu.findViewById(R.id.video_null_text);
		globalProgress = (ProgressBar)globalVideoMenu.findViewById(R.id.global_videos_progressbar);
		hdCheckBox = (CheckBox) globalVideoMenu.findViewById (R.id.hd_check_box);
		globalVideosNullAlert.setVisibility(View.INVISIBLE);
      
        pages.add(globalVideoMenu);
        
        
        userVideoMenu = inflater.inflate(R.layout.my_video_menu, null);
        userVideoList = (ListView)userVideoMenu.findViewById(R.id.mVideoArray);
        userListRefreshButton = (Button)userVideoMenu.findViewById(R.id.refresh_uservideo_button);
        userProgress = (ProgressBar)userVideoMenu.findViewById(R.id.user_videos_progress_bar);
        
        userVideosNullAlert = (TextView)userVideoMenu.findViewById(R.id.video_user_null_text);
		
		userVideosNullAlert.setVisibility(View.INVISIBLE);
      
        pages.add(userVideoMenu);
        
        ViewPagerAdapter pagerAdapter = new ViewPagerAdapter(pages, ViewPagerAdapter.CONTENT_TYPE_VIDEO, getApplicationContext());
        viewPager  = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(1);     

        TitlePageIndicator titleIndicator = (TitlePageIndicator)findViewById(R.id.indicator);
        
        
        titleIndicator.setOnPageChangeListener(new OnPageChangeListener(){

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageSelected(int page) {
				currPage = page;
				invalidateOptionsMenu();
				switch(page)
				{
				case 0:
					if(searchTextInput.getText().toString().isEmpty())
					{
					searchTextInput.requestFocus();
					inputManager.showSoftInput(searchTextInput, 0);
					}
					break;
					
					default: 
			
							searchTextInput.clearFocus();
							inputManager.hideSoftInputFromWindow(searchTextInput.getWindowToken(),0);
						break;
				}
				
			}
        	
        });
        
        if(sVideosAsync != null && !sVideosAsync.completed) globalProgress.setVisibility(View.VISIBLE); 
    		
    		if(uVideosAsync != null && !uVideosAsync.completed)
    		{
    			userListRefreshButton.setVisibility(View.INVISIBLE);
    			userProgress.setVisibility(View.VISIBLE);
    		} 
        
        titleIndicator.setViewPager(viewPager);
        titleIndicator.setCurrentItem(0);
		
	}
	
	
	private void setDownloadManagerReceiver()
	{
		downloadReceiver = new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				
				if(intent.getAction().equals(DownloadManager.ACTION_NOTIFICATION_CLICKED))
					{
					startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));
					return;
					}
				
				if(intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
				{
					
					
					
					
					long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
		
					if(downloadMap.containsKey(downloadId))
					{
					DownloadManager.Query query = new DownloadManager.Query();
					query.setFilterById(downloadId);
					Cursor cursor = downloadManager.query(query);
					
					if(cursor.moveToFirst())
					{
						int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
						
						int status = cursor.getInt(columnIndex);
						
						if(status == DownloadManager.STATUS_SUCCESSFUL)
						{
				 			
							NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
							.setSmallIcon(android.R.drawable.ic_menu_save)
							.setAutoCancel(true)
							.setTicker(getResources().getString(R.string.text_load_complete))
							.setContentText(getResources().getString(R.string.text_load_video) + downloadMap.get(downloadId).title + " " + getResources().getString(R.string.text_load_complete_end))
							.setContentIntent(PendingIntent.getActivity(getApplicationContext(), 0, new Intent(), 0))
							.setWhen(System.currentTimeMillis())
							.setContentTitle(getResources().getString(R.string.app_name));
							

							notificationManager.notify(builder.getNotification().hashCode(), builder.getNotification());
							downloadMap.remove(downloadId);
							
						
							
						}
					}
				}
			}
			
				
				

				
			}
			
		};
		
		IntentFilter iFilter = new IntentFilter();
			iFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
			iFilter.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED);
		
			
		
		registerReceiver(downloadReceiver, iFilter);
		
	}
	
	public void searchVideos(String query)
	{
		if(!alreadyHaveGlobalRequestThread)
		{
		
		if(Net.isOnline(this))
		{
			
			if(query.isEmpty())
			{
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.text_empty_request_prompt), Toast.LENGTH_SHORT).show();
				return;
			} else {
				if(globalVideoList != null) globalVideoList.setAdapter(null);
				sVideosAsync = null;
				sVideosAsync = new SearchVideosAsync(this);
				sVideosAsync.execute(query);
			}
			
		} else {
			Toast.makeText(getApplicationContext(), getResources().getString(R.string.text_not_connection_prompt), Toast.LENGTH_SHORT).show();
		}
		}
	}
	
	
	public void getUserVideos(View v)
	{
		 if(!alreadyHaveUserRequestThread)
		 {
			 
		if(Net.isOnline(this))
		{
			 if(!vPrefs.get().ACCESS_TOKEN.equals(getResources().getString(R.string.default_token)))
			 {	 
			if(userVideoList != null) userVideoList.setAdapter(null);
			uVideosAsync = null;
			uVideosAsync = new GetUserVideosAsync(this);
			uVideosAsync.execute();
			  } else {
				  showDialog(LOGIN_CONFIM_DIALOG_ID);
				  lastErrorPage = 1;
			  }
		} else {
			Toast.makeText(getApplicationContext(), getResources().getString(R.string.text_not_connection_prompt), Toast.LENGTH_SHORT).show();
		}
	}
}
	
	public void onVideosLoaded(final ArrayList<Video> result, final int listType)
	{
		ArrayList<View> define = new ArrayList<View>();
		
		if(result!=null)
		{
			LayoutInflater vi = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
				for(int i=0; i<result.size(); i++)
				{
					final Video video = result.get(i);
					define.add(i,vi.inflate(R.layout.video_define, null ));
					
					TextView title =  (TextView) define.get(i).findViewById(R.id.video_label);
		            TextView dur = (TextView)  define.get(i).findViewById(R.id.video_duration);
		            TextView views = (TextView) define.get(i).findViewById(R.id.video_views);
		            
		            final ImageButton playButton = (ImageButton) define.get(i).findViewById(R.id.video_play_button);            
		            final ImageButton loadButton = (ImageButton) define.get(i).findViewById(R.id.video_load_button);
		            
		            title.setText(video.title);
		            dur.setText(AppManager.formatDuration(video.duration*1000));
		            views.setText("\n" + getResources().getString(R.string.text_views_ic) + "\n" + String.valueOf(video.views));
		            
		            if(video.player.startsWith("http://www.youtube.com/"))
		            {
		            	loadButton.setVisibility(View.INVISIBLE);
		            	playButton.setImageResource(R.drawable.video_youtube_play_button_indicator);
		            	
		            }
		           
		            playButton.setOnClickListener(new OnClickListener() {

						public void onClick(View v) {
							
							if(!stillPrepareVideo)
							{
								 
							new AsyncTask<String, Void, Source>()
							{
								@Override
								protected void onPreExecute()
								{
									stillPrepareVideo = true;
									currVideo = video;
									
						
									 if(currVideo.player.startsWith("http://www.youtube.com/")||currVideo.isRutube)
							            {
										 Intent showIntent = new Intent(Intent.ACTION_VIEW);
											showIntent.setData(Uri.parse(currVideo.player));	
											startActivity(showIntent);
											stillPrepareVideo = false;
							            	this.cancel(false);
							            	return;
							            } else {
							            	showDialog(IDD_VIDEO_QUAL_WAIT_DIALOG);
							            	
							            }
								}
								
								@Override
								protected Source doInBackground(String... link) {
									try {
										
										Source result;
										result = VideoPlayerHacker.hack(link[0], video.image, VideoFinder.this);
										
										if(!Net.isHostReachable(result.qual.get("240"))) result.qual.remove("240");
										if(!Net.isHostReachable(result.qual.get("360"))) result.qual.remove("360");
										if(!Net.isHostReachable(result.qual.get("480"))) result.qual.remove("480");
										
										return result;
										
									} catch (IOException e) {
										e.printStackTrace();
									}
									return null;
								}
								@Override
								protected void onPostExecute(Source source)
								{
									currVideoSource = source;
									
									stillPrepareVideo = false;
									removeDialog(IDD_VIDEO_QUAL_WAIT_DIALOG);
									if(source.rutube)
									{ 
										currVideo.isRutube = true;
										showDialog(IDD_VIDEO_RUTUBE_ALERT);
										loadButton.setVisibility(View.INVISIBLE);
						            	playButton.setImageResource(R.drawable.video_rutube_play_button_indicator);
									} else {
									showDialog(IDD_VIDEO_SHOW_QUAL_CHOOSE_DIALOG);
									}
								}
								
							}.execute(video.player);
							
							}
							
						}
		            	
		            });
		            
		            loadButton.setOnClickListener(new OnClickListener() {

						public void onClick(View v) {
							if(!stillPrepareVideo)
							{
								 
							new AsyncTask<String, Void, Source>()
							{
								@Override
								protected void onPreExecute()
								{
									stillPrepareVideo = true;
									currVideo = video;
									showDialog(IDD_VIDEO_QUAL_WAIT_DIALOG);
								}
								
								@Override
								protected Source doInBackground(String... link) {
									try {
										
										Source result;
										result = VideoPlayerHacker.hack(link[0], video.image, VideoFinder.this);
										
										if(!Net.isHostReachable(result.qual.get("240"))) result.qual.remove("240");
										if(!Net.isHostReachable(result.qual.get("360"))) result.qual.remove("360");
										if(!Net.isHostReachable(result.qual.get("480"))) result.qual.remove("480");
										
										return result;
										
									} catch (IOException e) {
										e.printStackTrace();
									}
									return null;
								}
								@Override
								protected void onPostExecute(Source source)
								{
									currVideoSource = source;
									
									stillPrepareVideo = false;
									removeDialog(IDD_VIDEO_QUAL_WAIT_DIALOG);
									showDialog(IDD_VIDEO_LOAD_QUAL_CHOOSE_DIALOG);
									
								}
								
							}.execute(video.player);
							
							}
							
						}
		            	
		            });
		            
				}
		
		
		} 
		
		switch(listType)
		{
		case 0:
			globalVideos= result;
			globalDefine = define;
			
			globalAdapter = new VideoListAdapter(getApplicationContext(),R.layout.video_define, define, result);
			globalVideoList.setAdapter(globalAdapter);
			globalVideoList.setFocusable(true);	
			
			break;
			
		case 1:
			userVideos = result;
			userDefine = define;
			
			userAdapter = new VideoListAdapter(getApplicationContext(),R.layout.video_define, define, result);
			userVideoList.setAdapter(userAdapter);
			userVideoList.setFocusable(true);	
			
			break;
			
		}
		
	}
	


	public void RefreshCurrList()
	{
		switch(currPage)
		{
		case 1:
			getUserVideos(null);
			break;
			
		}
	}
	
	
	public void startLoginActivity() 

	{
		Intent i = new Intent(this,Login.class);
		startActivityForResult(i, REQUEST_LOGIN);
	}
	
	private void doPostFlipActions(ConfigurationData config)
	{

		
		try{ globalDefine = (ArrayList<View>) config.globalVideoListItems; } catch(NullPointerException nex) {};
		try{ userDefine = (ArrayList<View>) config.userVideoListItems; } catch(NullPointerException nex) {}; 
		try{ globalVideos = (ArrayList<Video>) config.globalVideos; } catch(NullPointerException nex) {}; 
		try{ userVideos = (ArrayList<Video>) config.userVideos; } catch(NullPointerException nex) {}; 
		try{ currVideo = config.currVideo; } catch (NullPointerException npex){}
		try{ currVideoSource = config.currSource; } catch (NullPointerException npex){}
		qualDialogOpened = config.qualDialogOpened;
		
  		try{sVideosAsync = config.sVideosAsync;} catch(NullPointerException ex){}
  		try{uVideosAsync = config.uVideosAsync;} catch(NullPointerException ex){}
		
  		try{sVideosAsync.link(this);} catch(NullPointerException ex){}
  		try{uVideosAsync.link(this);} catch(NullPointerException ex){}
  		
		initUI();
		
		if(globalDefine!=null)
		{
		globalAdapter = new VideoListAdapter(getApplicationContext(),R.layout.video_define, globalDefine, globalVideos);
		globalVideoList.setAdapter(globalAdapter);
		globalVideoList.setFocusable(false);	
		}
		
		if(userDefine!=null)
		{
		userListRefreshButton.setVisibility(View.INVISIBLE);
		userAdapter = new VideoListAdapter(getApplicationContext(),R.layout.video_define, userDefine, userVideos);
		userVideoList.setAdapter(userAdapter);
		userVideoList.setFocusable(false);	
		}
		if(qualDialogOpened)
		{
			
		}
		


	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)

	{
		try {
		
		if (requestCode == REQUEST_LOGIN) {
            if (resultCode == RESULT_OK) {
            	
           VkData vkData = new VkData();
            	
            	vkData.ACCESS_TOKEN = data.getStringExtra("token");
            	vkData.USER_ID = String.valueOf(data.getLongExtra("user_id", 0));
            	vkData.USER_FIRST_NAME = data.getStringExtra("first_name");
            	vkData.USER_LAST_NAME = data.getStringExtra("last_name");
            	vkData.LAST_IP = Net.getLocalIpAddress();
            	vPrefs.save(vkData);
            	vPrefs = new VkPrefs(this);
            	api = new Api(vkData.ACCESS_TOKEN, Login.APP_ID);
            	switch(lastErrorPage)
            	{
            	case 0:
            		searchVideos(null);
            		break;
            	case 1:
            		getUserVideos(null);
            
            		break;
          
            	}
          
            }
        }
		
		} catch (NullPointerException ex){}
	}

	@Override
	 public ConfigurationData onRetainNonConfigurationInstance() {
		ConfigurationData config = new ConfigurationData(); 
		
		 try{config.globalVideoListItems = globalAdapter.getItems();} catch (NullPointerException npex){}
		 try{config.userVideoListItems = userAdapter.getItems();} catch (NullPointerException npex){}
		 try{config.globalVideos = globalVideos;} catch (NullPointerException npex){}
		 try{ config.userVideos = userVideos;} catch (NullPointerException npex){}
		 try{config.currSource = currVideoSource;} catch (NullPointerException npex){}
		 try{ config.currVideo = currVideo; } catch (NullPointerException npex){}
	  		try{config.sVideosAsync = sVideosAsync;} catch(NullPointerException ex){}
	  		try{config.uVideosAsync = uVideosAsync;} catch(NullPointerException ex){}
			   
	  		if(sVideosAsync != null)sVideosAsync.unLink();
	  		if(uVideosAsync != null)uVideosAsync.unLink();
		return config;
	 }
	
		@Override
		public Dialog onCreateDialog(int id)
		{
			AlertDialog.Builder builder = null;
			switch(id)
			{
				
			case IDD_VIDEO_SHOW_QUAL_CHOOSE_DIALOG:
				
				
				if(currVideoSource==null && currVideoSource.qual==null){
					qualDialogOpened = false;
					removeDialog(IDD_VIDEO_LOAD_QUAL_CHOOSE_DIALOG);
					showDialog(IDD_VIDEO_VOID_ALERT);
					return null;
				}
				
				
				qualDialogOpened = true;
				final String[] quals = {"flv", "240", "360", "480", "720"};
				final String[] userQuals = {getResources().getString(R.string.dialog_video_flv_item), "240p", "360p", "480p (SD)", "720p (HD)"};
			    String[] resultQuals = new String[currVideoSource.qual.size()];
				String qualArray[] = new String[currVideoSource.qual.size()];
				
				if(currVideoSource.qual.size()==0)
				{
					removeDialog(IDD_VIDEO_SHOW_QUAL_CHOOSE_DIALOG);
					showDialog(IDD_VIDEO_VOID_ALERT);
					return null;
				}
				
				for(int i = 0; i<currVideoSource.qual.size(); i++)
				{
				qualArray[i] = currVideoSource.qual.get(quals[i]);
					resultQuals[i]= userQuals[i];
				}
				
				
				builder = new AlertDialog.Builder(this);
				builder.setTitle(getResources().getString(R.string.dialog_choose_qual_title));
				
				builder.setItems(resultQuals, new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int item) {
						removeDialog(IDD_VIDEO_SHOW_QUAL_CHOOSE_DIALOG);
						
						Intent showIntent = new Intent(Intent.ACTION_VIEW);
						showIntent.setData(Uri.parse(currVideoSource.qual.get(quals[item])));	
						startActivity(showIntent);
					}
				});
				
			
				
				builder.setOnCancelListener(new OnCancelListener() {

					public void onCancel(DialogInterface di) {
						qualDialogOpened = false;
						removeDialog(IDD_VIDEO_SHOW_QUAL_CHOOSE_DIALOG);
					}
					
				});
				
	          
				break;
				
	
case IDD_VIDEO_LOAD_QUAL_CHOOSE_DIALOG:
				
	if((currVideoSource==null || currVideoSource.qual==null) || currVideoSource.qual.size()==0){
		qualDialogOpened = false;
		removeDialog(IDD_VIDEO_LOAD_QUAL_CHOOSE_DIALOG);
		showDialog(IDD_VIDEO_VOID_ALERT);
		return null;
	}
	
				qualDialogOpened = true;
				final String[] lQuals = {"flv", "240", "360", "480", "720"};
				final String[] lUserQuals = {getResources().getString(R.string.dialog_video_load_flv_item), "240p", "360p", "480p (SD)", "720p (HD)"};
			    String[] lResultQuals = new String[currVideoSource.qual.size()];
				String lQualArray[] = new String[currVideoSource.qual.size()];
				
				
				for(int i = 0; i<currVideoSource.qual.size(); i++)
				{
				lQualArray[i] = currVideoSource.qual.get(lQuals[i]);
					lResultQuals[i]= lUserQuals[i];
				}
				
				builder = new AlertDialog.Builder(this);
				builder.setTitle(getResources().getString(R.string.dialog_choose_qual_title));
				
				builder.setItems(lResultQuals, new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int item) {
		
						if(currVideoSource==null && currVideoSource.qual==null){
							qualDialogOpened = false;
							removeDialog(IDD_VIDEO_LOAD_QUAL_CHOOSE_DIALOG);
							showDialog(IDD_VIDEO_VOID_ALERT);
							return;
						}
						
						if(currVideo.title.contains("/")) currVideo.title = currVideo.title.replaceAll("/", " ");
						String vidUrl = currVideoSource.qual.get(lQuals[item]);
						String fileExt = vidUrl.substring(vidUrl.length()-4);
						DownloadManager.Request request = new DownloadManager.Request(Uri.parse(vidUrl))
					    .setTitle(getResources().getString(R.string.text_load_video)) 
					    .setDescription(currVideo.title)
					    .setMimeType(fileExt == ".mp4" ?  "video/mp4" : "video/x-flv")
					    .setVisibleInDownloadsUi(true)
					    .setDestinationUri(Uri.fromFile(new File(String.valueOf(Preferences.getVideoDir(getApplicationContext())+"/"+currVideo.title +"["+currVideo.vid+"]"+fileExt))));
				
						if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
							request.allowScanningByMediaScanner();
						
						downloadMap.put(downloadManager.enqueue(request), currVideo);

						
					}
				});
				
			
				
				builder.setOnCancelListener(new OnCancelListener() {

					public void onCancel(DialogInterface di) {
						qualDialogOpened = false;
						removeDialog(IDD_VIDEO_LOAD_QUAL_CHOOSE_DIALOG);
					}
					
				});
				
	          
				break;
				
			case IDD_VIDEO_VOID_ALERT:
				builder = new AlertDialog.Builder(this);
				
				builder.setTitle(getResources().getString(R.string.dialog_video_locked_title));
				builder.setMessage(getResources().getString(R.string.dialog_video_locked_message));
				
				builder.setPositiveButton(getResources().getString(R.string.button_positive), new DialogInterface.OnClickListener(){

					public void onClick(DialogInterface dialog, int arg1) {
						viewPager.beginFakeDrag();
						viewPager.fakeDragBy(300);
						viewPager.endFakeDrag();
						searchTextInput.setText(currVideo.title);
						searchVideos(currVideo.title);
					}
					
				});
				
				builder.setNegativeButton(getResources().getString(R.string.button_negative), new DialogInterface.OnClickListener(){

					public void onClick(DialogInterface dialog, int arg1) {
						dismissDialog(IDD_VIDEO_VOID_ALERT);
					}
					
				});
				
				break;
				
			case LOGIN_CONFIM_DIALOG_ID:
    			
    			builder = new AlertDialog.Builder(this);
				
				builder.setTitle(getResources().getString(R.string.dialog_autorize_vk_title));
				builder.setMessage(getResources().getString(R.string.dialog_autorize_vk_message));
				
				builder.setPositiveButton(getResources().getString(R.string.button_positive), new DialogInterface.OnClickListener(){

					public void onClick(DialogInterface dialog, int arg1) {
						startLoginActivity();
					}
					
				});
    	
				
				
				builder.setNegativeButton(getResources().getString(R.string.button_negative), new DialogInterface.OnClickListener(){

					public void onClick(DialogInterface dialog, int arg1) {
						dismissDialog(LOGIN_CONFIM_DIALOG_ID);
						
					}
					
				});
  	   		   
				return builder.create();
				
			case IDD_VIDEO_RUTUBE_ALERT:
				
				if(currVideo == null)
				{
					dismissDialog(IDD_VIDEO_RUTUBE_ALERT);
				}

				
				builder = new AlertDialog.Builder(this);
				
				builder.setTitle(getResources().getString(R.string.dialog_rutube_alert_title));
				builder.setMessage(getResources().getString(R.string.dialog_rutube_alert_message));
				
				builder.setPositiveButton(getResources().getString(R.string.dialog_rutube_view), new DialogInterface.OnClickListener(){

					public void onClick(DialogInterface dialog, int arg1) {
						 Intent showIntent = new Intent(Intent.ACTION_VIEW);
							showIntent.setData(Uri.parse(currVideo.player));	
							startActivity(showIntent);
							
							dismissDialog(IDD_VIDEO_RUTUBE_ALERT);
					}
					
					
				});
				
				builder.setNegativeButton(getResources().getString(R.string.dialog_rutube_close), new DialogInterface.OnClickListener(
						) {
					
					public void onClick(DialogInterface dialog, int which) {
						dismissDialog(IDD_VIDEO_RUTUBE_ALERT);
						
					}
				});
				
				break;
				
			case IDD_VIDEO_QUAL_WAIT_DIALOG:
				ProgressDialog waitDialog = new ProgressDialog(VideoFinder.this);
				waitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				waitDialog.setMessage(getResources().getString(R.string.dialog_video_analize));
				waitDialog.setCancelable(false);
				return waitDialog;
				
			}
			return builder.create();	
		}
		

	class ConfigurationData {
		public List<View> globalVideoListItems;
		public List<View> userVideoListItems;
		public List<Video> globalVideos;
		public List<Video> userVideos;
		public boolean qualDialogOpened;
		public Video currVideo;
		public Source currSource;
		
		public SearchVideosAsync sVideosAsync;
		public GetUserVideosAsync uVideosAsync;
		
		
	}
	
}