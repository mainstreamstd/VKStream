package ru.mainstream.vkstream;




import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;



import ru.mainstream.vkstream.DB.AppDB;
import ru.mainstream.vkstream.at.GetRecommendsAudiosAsync;
import ru.mainstream.vkstream.at.GetUserAudiosAsync;
import ru.mainstream.vkstream.at.SearchAudiosAsync;
import ru.mainstream.vkstream.tools.AppManager;
import ru.mainstream.vkstream.tools.FileSysHelper;
import ru.mainstream.vkstream.tools.UserPrefs;
import ru.mainstream.vkstream.tools.VkPrefs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar; 
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import ru.mainstream.vkstream.R;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Tracker;
import com.perm.kate.api.Api;
import com.perm.kate.api.Audio;
import com.viewpagerindicator.TitlePageIndicator;




public class AudioFinder extends SherlockActivity {

	View globalAudioMenu;
	View userAudioMenu;
	View recommendsAudioMenu;
	ViewPager viewPager;
	
	ActionBar actionBar;                                                         
	
	public Api api;
	
	UserPrefs uPrefs;
	
	InputMethodManager inputManager;
	
	Tracker mainTracker;
	
	private final int WAIT_DIALOG_ID = 0x1a;
	private final int AUDIO_WAIT_DIALOG_ID = 0x3f;
	private final int SHAZAM_INSTALL_DIALOG_ID = 0xad;
	private final int LOGIN_CONFIM_DIALOG_ID = 0xbdaf;
	
	private boolean calledOnRetrain= false;
	

	
	private final int REQUEST_LOGIN=1;
	
	SearchAudiosAsync sAudiosAsync;
	private GetUserAudiosAsync uAudiosAsync;
	private GetRecommendsAudiosAsync gRecommendsAsync;
	
	public AutoCompleteTextView searchTextInput;
	public ProgressBar globalProgress;
	public ListView globalAudioList;
	public ArrayList<View> globalDefine = null;
	public TextView nullAudiosAlert;
	public ArrayList<Audio> globalAudios;
	public HashMap<Audio, File> globalFilesList;
	public HashMap<Long, Audio> globalAudiosMap = new HashMap<Long, Audio>();
	public HashMap<Audio, View> globalDefineMap = new HashMap<Audio, View>();
	
	public ListView userAudioList;
	public ProgressBar userProgress;
	public ArrayList<View> userDefine = null;
	public Button userListRefreshButton;
	public TextView nullUserAudioAlert;
	public ArrayList<Audio> userAudios;
	public HashMap<Audio, File> userFilesList;
	public HashMap<Long, Audio> userAudiosMap = new HashMap<Long, Audio>();
	public HashMap<Audio, View> userDefineMap = new HashMap<Audio, View>();
	
	public ListView recommendsAudioList;
	public ArrayList<View> recommendsDefine;
	public Button recommendsListRefreshButton;
	public ProgressBar recommendsProgress;
	public TextView nullRecommendsAudioAlert;
	public ArrayList<Audio> recommendsAudios;
	public HashMap<Audio, File> recommendsFilesList;
	public HashMap<Long, Audio> recommendsAudiosMap = new HashMap<Long, Audio>();
	public HashMap<Audio, View> recommendsDefineMap = new HashMap<Audio, View>();
	
	private HashMap<Long, Integer> defTypeMap = new HashMap<Long, Integer>();
	private HashMap<Long, String> totalAudiosUrlMap= new HashMap<Long, String>();
	
	HashMap<Long, Audio> downloadMap = new HashMap<Long, Audio>();
	
	public static final int DOWNLOAD_SERVICE_CALLBACK = 0xfd43;
	public static final int DOWNLOAD_SERVICE_CALLBACK_LOADED = 0xfd3f;
	public static final int PLAY_SERVICE_CALLBACK = 0xda12;
	
	
	
	BroadcastReceiver downloadReceiver;
	
	private VkPrefs vPrefs;
	
	public int currPage = 0;
	public int lastErrorPage = 0; 
	
	Audio currAudio;
	SeekBar currPlayProgress;
	ProgressBar currBufferProgress;
	ImageButton currPlayButton;
	
	AudioListAdapter globalAdapter;
	AudioListAdapter userAdapter;
	AudioListAdapter recommendsAdapter;
	
	DownloadManager downloadManager;
	
	NotificationManager notificationManager;
	
	
	
	@Override
	public void onCreate(Bundle savedInstanceState)
    {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.audio_finder_host);
        
        actionBar = getSupportActionBar();

        uPrefs = new UserPrefs("VKStreamPrefs",this);
        vPrefs = new VkPrefs(this);
    	api = new Api(vPrefs.get().ACCESS_TOKEN, Login.APP_ID);
    	downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
    	EasyTracker.getInstance().setContext(getApplicationContext());
		mainTracker = EasyTracker.getTracker();
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		

		ConfigurationData config = (ConfigurationData) getLastNonConfigurationInstance(); 
	

		setDownloadManagerReceiver();
		if(config!=null)
		{
			
			doPostFlipActions(config);

		}
        if(!calledOnRetrain)
        {
        	initUI();
        }
        
        Intent intent = getIntent();
        
        if((intent.getAction() != null) && intent.getAction().equals(ShazamShareAction.SHARE_ACTION))
		{
			searchAudios(intent.getStringExtra(ShazamShareAction.EXTRA_TITLE));
			setIntent(new Intent());
		}
		
        inputManager.hideSoftInputFromWindow(searchTextInput.getWindowToken(), 0);
    }
	

	public void startShazam()
	{
		if(AppManager.isPackageInstalled(getApplicationContext(), "com.shazam.encore.android") || AppManager.isPackageInstalled(getApplicationContext(), "com.shazam.android"))
		{

		
			Toast.makeText(getApplicationContext(), getResources().getString(R.string.text_shazam_usage_prompt), Toast.LENGTH_LONG).show();
			
		if(AppManager.isPackageInstalled(getApplicationContext(), "com.shazam.encore.android"))
		{
			AppManager.startApplication(getApplicationContext(), "com.shazam.encore.android");
		} else {
			if(AppManager.isPackageInstalled(getApplicationContext(), "com.shazam.android"))
			{
				AppManager.startApplication(getApplicationContext(), "com.shazam.android");
			}
		}
	} else {
		showDialog(SHAZAM_INSTALL_DIALOG_ID);
	}
			
	}


	private void setDownloadManagerReceiver()	{
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
							String pathToFile = onAudioDownloadCompleted(downloadMap.get(downloadId).aid);
							
							if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
								sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, 
								        Uri.parse("file://" + Environment.getExternalStorageDirectory()))); 
							
							
							
							NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
							.setSmallIcon(android.R.drawable.ic_menu_save)
							.setAutoCancel(true)
							.setTicker(getResources().getString(R.string.text_load_complete))
							.setContentText(getResources().getString(R.string.text_load_audio) + downloadMap.get(downloadId).title + " " + getResources().getString(R.string.text_load_complete_end))
							.setContentIntent(PendingIntent.getActivity(getApplicationContext(), 0, (pathToFile!=null) ? new Intent()
																												.setAction(android.content.Intent.ACTION_VIEW)
																												.setDataAndType(Uri.fromFile(new File(pathToFile)), "audio/mp3")
																												: new Intent(), 0))
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
	

	
	private String onAudioDownloadCompleted(long aid)
	{
		
		Audio audio = null;  
		View view = null;
		File audioFile = null;
		
		switch(defTypeMap.get(aid))
		{
		case 0:
			audio = globalAudiosMap.get(aid);
			view =  globalDefineMap.get(audio);
			globalAdapter = new AudioListAdapter(getApplicationContext(),R.layout.audio_define,globalDefine, globalAudios);
			globalFilesList = FileSysHelper.getAudioFilesInDir(globalAudios, Preferences.getAudioDir(this));
			audioFile = globalFilesList.get(audio);
			break;
		case 1:
			audio = userAudiosMap.get(aid);
			view =  userDefineMap.get(audio);
			userAdapter = new AudioListAdapter(getApplicationContext(),R.layout.audio_define,userDefine, userAudios);
			userFilesList = FileSysHelper.getAudioFilesInDir(userAudios, Preferences.getAudioDir(this));
			audioFile = userFilesList.get(audio);
			break;
		case 2:
			audio = recommendsAudiosMap.get(aid);
			view =  recommendsDefineMap.get(audio);
			recommendsAdapter = new AudioListAdapter(getApplicationContext(),R.layout.audio_define,recommendsDefine, recommendsAudios);
			recommendsFilesList = FileSysHelper.getAudioFilesInDir(recommendsAudios, Preferences.getAudioDir(this));
			audioFile = recommendsFilesList.get(audio);
			break;
		}
		

	         final ImageButton loadButton = (ImageButton) view.findViewById(R.id.audio_load_button);
	         final ImageButton playButton = (ImageButton) view.findViewById(R.id.audio_play_button);
	         final File delFile = audioFile;
	       

	         
	       
	         final Audio loadAudio = audio;
	         try { loadAudio.url = audioFile.getAbsolutePath(); } catch (NullPointerException ex) {}
	         
	         OnClickListener playListener = new OnClickListener()
		      {

				public void onClick(View v) {
				 Intent serviceIntent = new Intent(getApplicationContext(), AudioPlayService.class);
				 PendingIntent pi = createPendingResult(PLAY_SERVICE_CALLBACK, serviceIntent, 0);
				 Bundle bundle = new Bundle();
				 bundle.putParcelable("audio", loadAudio);
				 bundle.putParcelable("callback", pi);
				 serviceIntent.putExtras(bundle);
				 startService(serviceIntent);
				 
				}
		    	  
		      };
	         
	         
	          final OnClickListener loadListener = new OnClickListener()
		      {

				public void onClick(View v) {
				 
				
					DownloadManager.Request request = new DownloadManager.Request(Uri.parse(loadAudio.url))
				    .setTitle(loadAudio.artist + " - " + loadAudio.title) 
				    .setDescription(getResources().getString(R.string.text_load))
				    .setMimeType("audio/mp3")
				    .setVisibleInDownloadsUi(true)
				     .setDestinationUri(Uri.fromFile(new File(String.valueOf(Preferences.getAudioDir(getApplicationContext())+"/"+loadAudio.title +"["+loadAudio.aid+"]"+".mp3"))));
			
					if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
						request.allowScanningByMediaScanner();
					 long id = downloadManager.enqueue(request);
					 downloadMap.put(id, loadAudio);
					
				}
		    	  
		      };
	          
	         OnClickListener deleteListener = new OnClickListener()
		      {

				@Override
				public void onClick(View v) {
					delFile.delete();
					loadAudio.url = totalAudiosUrlMap.get(loadAudio.aid);
					loadButton.setImageResource(R.drawable.load_button_indicator);
					loadButton.setOnClickListener(loadListener);
					Toast.makeText(getApplicationContext(), getResources().getString(R.string.text_delete_complete), Toast.LENGTH_SHORT).show();
				}
		    	  
		      };
		      
		      playButton.setOnClickListener(playListener);
		        loadButton.setOnClickListener(deleteListener); 
				 loadButton.setImageResource(R.drawable.delete);
				 
				try { return audioFile.getAbsolutePath(); } catch (NullPointerException ex)
				{
					return null;
				}
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		
		
		menu.add(getResources().getString(R.string.menuitem_video_label))
		.setIcon(R.drawable.ic_action_to_videos)
		.setOnMenuItemClickListener(new OnMenuItemClickListener(){

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				Intent i = new Intent(AudioFinder.this,VideoFinder.class);
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
		} else {
			menu.add(getResources().getString(R.string.menuitem_shazam_label))
			.setIcon(R.drawable.shazam)
			.setOnMenuItemClickListener(new OnMenuItemClickListener(){

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				startShazam();
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
				Intent i = new Intent(AudioFinder.this, SettingsActivity.class);
				startActivity(i);
				return true;
			}
			
		})
		
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		
		
		
		return true;
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
		if(!calledOnRetrain&&(globalAdapter==null||userAdapter==null))
		{
		vPrefs = new VkPrefs(this);
		api = new Api(vPrefs.get().ACCESS_TOKEN, Login.APP_ID);
		}
		
		if(!AppDB.tableExists(this, AppDB.AUDIO_SEARCH_TABLE_NAME)) 
			searchTextInput.setAdapter(new ArrayAdapter<String> (AudioFinder.this, android.R.layout.simple_spinner_dropdown_item, new ArrayList<String>()));
	
			inputManager.hideSoftInputFromWindow(searchTextInput.getWindowToken(), 0);
		
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
	}

	
	private void initUI()
	{
		LayoutInflater inflater = LayoutInflater.from(this);
        List<View> pages = new ArrayList<View>();
        

        globalAudioMenu  = inflater.inflate(R.layout.audio_menu, null);
        globalProgress = (ProgressBar)globalAudioMenu.findViewById(R.id.global_audios_progressbar);
		globalAudioList = (ListView)globalAudioMenu.findViewById(R.id.audioArray);
		nullAudiosAlert = (TextView)globalAudioMenu.findViewById(R.id.global_audio_null_text);
		nullAudiosAlert.setVisibility(View.INVISIBLE);
		
	
	     inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		
	    	searchTextInput = (AutoCompleteTextView)globalAudioMenu.findViewById(R.id.audio_search_view);
	    	searchTextInput.setOnKeyListener(new OnKeyListener(){

				public boolean onKey(View v, int keyCode, KeyEvent event) {
					 if(event.getAction() == KeyEvent.ACTION_DOWN && 
							    (keyCode == KeyEvent.KEYCODE_ENTER))
								{
								searchAudios(searchTextInput.getText().toString());
								
								ArrayList<String> sHistory = AppDB.readSearchHistory(getApplicationContext(), AppDB.AUDIO_SEARCH_TABLE_NAME);
								if(!sHistory.contains(searchTextInput.getText().toString()))
								{
								AppDB.incertSearchString(getApplicationContext(), searchTextInput.getText().toString(), AppDB.AUDIO_SEARCH_TABLE_NAME);
								sHistory.add(searchTextInput.getText().toString());
								searchTextInput.setAdapter(new ArrayAdapter<String> (AudioFinder.this, android.R.layout.simple_spinner_dropdown_item, sHistory));
							
								}
								
									return true;
								}
					return false;
				}
	    		
	    	});
	    	
	    
	    searchTextInput.setAdapter(new ArrayAdapter<String> (this, android.R.layout.simple_spinner_dropdown_item, AppDB.readSearchHistory(getApplicationContext(), AppDB.AUDIO_SEARCH_TABLE_NAME)));
      
        pages.add(globalAudioMenu);
        
        userAudioMenu = inflater.inflate(R.layout.my_audio_menu, null);
        userAudioList = (ListView)userAudioMenu.findViewById(R.id.mAudioArray);
        userListRefreshButton = (Button)userAudioMenu.findViewById(R.id.refresh_useraudio_button);
        userProgress = (ProgressBar)userAudioMenu.findViewById(R.id.user_audios_progress_bar);
        nullUserAudioAlert = (TextView)userAudioMenu.findViewById(R.id.null_my_audio_text);
        
        nullUserAudioAlert.setVisibility(View.INVISIBLE);
      
        pages.add(userAudioMenu);
        
        recommendsAudioMenu = inflater.inflate(R.layout.recommends_audio_menu, null);
        recommendsAudioList = (ListView)recommendsAudioMenu.findViewById(R.id.rAudioArray);
        recommendsListRefreshButton = (Button)recommendsAudioMenu.findViewById(R.id.refresh_recommends_button);
        recommendsProgress = (ProgressBar)recommendsAudioMenu.findViewById(R.id.recommends_audios_progress_bar);
        nullRecommendsAudioAlert = (TextView)recommendsAudioMenu.findViewById(R.id.null_recommends_audio_text);
        
        nullRecommendsAudioAlert.setVisibility(View.INVISIBLE);
        
        pages.add(recommendsAudioMenu);
	
        
        ViewPagerAdapter pagerAdapter = new ViewPagerAdapter(pages, ViewPagerAdapter.CONTENT_TYPE_AUDIO, getApplicationContext());
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
						if(searchTextInput.getText().toString().isEmpty() && globalAudioList.getAdapter() == null)
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
        
	if(sAudiosAsync != null && !sAudiosAsync.completed) globalProgress.setVisibility(View.VISIBLE); else 
		if(sAudiosAsync!=null && sAudiosAsync.completed) sAudiosAsync = null;
		
		if(uAudiosAsync != null && !uAudiosAsync.completed)
		{
			userListRefreshButton.setVisibility(View.INVISIBLE);
			userProgress.setVisibility(View.VISIBLE);
		} else if(uAudiosAsync!=null && uAudiosAsync.completed) uAudiosAsync = null;
		
		if(gRecommendsAsync != null && !gRecommendsAsync.completed)
		{
			recommendsListRefreshButton.setVisibility(View.INVISIBLE);
			recommendsProgress.setVisibility(View.VISIBLE);
		}	else if(gRecommendsAsync!=null && gRecommendsAsync.completed) gRecommendsAsync = null;
		
        
        titleIndicator.setViewPager(viewPager);
        titleIndicator.setCurrentItem(0);
        
	}

	public void searchAudios(final String query)
	{
		
		
		if(Net.isOnline(this))
		  {
			searchTextInput.setText(query);
			if(globalAudioList != null) globalAudioList.setAdapter(null);
			sAudiosAsync = null;
			sAudiosAsync = new SearchAudiosAsync(this, query, 0, null, null);
			inputManager.hideSoftInputFromWindow(searchTextInput.getWindowToken(), 0);
			sAudiosAsync.execute();
		  } else {
				Toast.makeText(this, getResources().getString(R.string.text_not_connection_prompt), Toast.LENGTH_SHORT).show();
				return;
		  }
		
	}
	
	
	 public void getUserAudios(View v)
	  {
		  if(Net.isOnline(this))
		  {
		  if(!vPrefs.get().ACCESS_TOKEN.equals(getResources().getString(R.string.default_token)))
		  {
		  	if(userAudioList != null) userAudioList.setAdapter(null);
		  	uAudiosAsync = null;
			uAudiosAsync = new GetUserAudiosAsync(this, 0);
			uAudiosAsync.execute();
		  } else {
			  showDialog(LOGIN_CONFIM_DIALOG_ID);
			  lastErrorPage = 1;
		  }
		  } else {
			  Toast.makeText(getApplicationContext(), getResources().getString(R.string.text_not_connection_prompt), Toast.LENGTH_LONG).show();
			  return;
		  }
		 
	}
	
	public void getRecommendsAudios(View v)
	{
		
		if(Net.isOnline(this))
		  {
			
			if(!vPrefs.get().ACCESS_TOKEN.equals(getResources().getString(R.string.default_token)))
			  {
			
			if(recommendsAudioList != null) recommendsAudioList.setAdapter(null);
			gRecommendsAsync = null;
			gRecommendsAsync = new GetRecommendsAudiosAsync(this);
			gRecommendsAsync.execute();
			  } else {
				  showDialog(LOGIN_CONFIM_DIALOG_ID);
				  lastErrorPage = 2;
			  }
		  } else {
				Toast.makeText(this, getResources().getString(R.string.text_not_connection_prompt), Toast.LENGTH_SHORT).show();
				return;
		  }
		
	}
	

	
	public void RefreshCurrList()
	{
		switch(currPage)
		{
		case 1:
			getUserAudios(null);
			break;
			
		case 2:
			getRecommendsAudios(null);
		}
	}
	
	public void onAudiosLoaded(final ArrayList<Audio> result, final int offset ,final int listType, String sQuery)
	{

		if(result!=null)
		{
				inflateAudios(result, offset, listType, sQuery);	
		} else {
			calledOnRetrain = false;
			startLoginActivity();
		}
	}

	public void inflateAudios(ArrayList<Audio> content, final int offset, final int listType, final String sQuery)
	{
		
		ArrayList<Audio> audios;
		audios = content;
		
		for(Audio audio : audios)
		{
			if(audio.title.length() >= 45) audio.title = audio.title.substring(0, 45) + "...";
			if(audio.artist.length() >= 40) audio.artist = audio.artist.substring(0, 40)+ "...";
			
			totalAudiosUrlMap.put(audio.aid, audio.url);
			
		}
		
		
		final ArrayList<View> define;
		final HashMap<Audio, File> filesList;
		final HashMap<Long, Audio> audiosMap;
		final HashMap<Audio, View> defineMap;
		
		if(offset == 0)
		{
		define = new ArrayList<View>();
		filesList = FileSysHelper.getAudioFilesInDir(content, Preferences.getAudioDir(this));
		audiosMap = new HashMap<Long, Audio>();
		defineMap = new HashMap<Audio, View>();
		} else {
			switch(listType)
			{
			case 0:
				
			filesList = globalFilesList;
			audiosMap = globalAudiosMap;
			defineMap =	globalDefineMap;
			define = globalDefine;
			content = globalAudios;	
			break;
				
			case 1:
			filesList =	userFilesList;
			audiosMap = userAudiosMap;
			defineMap = userDefineMap;
			define = userDefine;
			content = userAudios;
			break;
				
			case 2:
				filesList = recommendsFilesList;
				audiosMap =  recommendsAudiosMap;
				defineMap = recommendsDefineMap;
				define = recommendsDefine;
				content = recommendsAudios;
				break;
				
				default:
					filesList = null;
					audiosMap = null;
					define = null;
					defineMap = null;
					content = null;
			}
			
			content.addAll(audios);
			define.remove(define.size()-1);
		}
	

	final LayoutInflater vi = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	

		for(final Audio audio : audios)
		{
			
			View def = vi.inflate(R.layout.audio_define, null);
			
			
			TextView artist =  (TextView) def.findViewById(R.id.audio_label);
            TextView title = (TextView) def.findViewById(R.id.audio_description);
            TextView dur = (TextView)  def.findViewById(R.id.audio_duration_text);
            final ImageButton playButton = (ImageButton) def.findViewById(R.id.audio_play_button);
            final ImageButton loadButton = (ImageButton)def.findViewById(R.id.audio_load_button);
            
            artist.setText(audio.artist);
            title.setText(audio.title);
            dur.setText(AppManager.formatDuration(audio.duration*1000));
            
            define.add(def);
            
            
      OnClickListener playListener = new OnClickListener()
      {

		public void onClick(View v) {
		 Intent serviceIntent = new Intent(getApplicationContext(), AudioPlayService.class);
		 PendingIntent pi = createPendingResult(PLAY_SERVICE_CALLBACK, serviceIntent, 0);
		 Bundle bundle = new Bundle();
		 bundle.putParcelable("audio", audio);
		 bundle.putParcelable("callback", pi);
		 serviceIntent.putExtras(bundle);
		 startService(serviceIntent);
		 
		}
    	  
      };
    
      final OnClickListener loadListener = new OnClickListener()
      {

		public void onClick(View v) {
			
			DownloadManager.Request request = new DownloadManager.Request(Uri.parse(audio.url))
		    .setTitle(audio.artist + " - " + audio.title) 
		    .setDescription(getResources().getString(R.string.text_load))
		    .setMimeType("audio/mp3")
		    .setVisibleInDownloadsUi(true)
		    .setDestinationUri(Uri.fromFile(new File(String.valueOf(Preferences.getAudioDir(getApplicationContext())+"/"+audio.title +"["+audio.aid+"]"+".mp3"))));
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
				request.allowScanningByMediaScanner();
			
			long id = downloadManager.enqueue(request);
			 downloadMap.put(id, audio);

			
			 
		}
    	  
      };
      
      OnClickListener deleteListener = new OnClickListener()
      {

		@Override
		public void onClick(View v) {

					filesList.get(audio).delete();
					audio.url = totalAudiosUrlMap.get(audio.aid);
					loadButton.setImageResource(R.drawable.load_button_indicator);
					loadButton.setOnClickListener(loadListener);
					Toast.makeText(getApplicationContext(), getResources().getString(R.string.text_delete_complete), Toast.LENGTH_SHORT).show();
				}
		

    	  
      };
            
   playButton.setOnClickListener(playListener);   
 if(!filesList.containsKey(audio)){
	 loadButton.setOnClickListener(loadListener);
	 }
 else 
 {
audio.url = filesList.get(audio).getAbsolutePath();
 loadButton.setOnClickListener(deleteListener); 
 loadButton.setImageResource(R.drawable.delete);
 }
 

	audiosMap.put(audio.aid, audio);
	defineMap.put(audio, def);
	defTypeMap.put(audio.aid, listType);
}			
	
	if(listType!=2)
	{
			
		
			final View expandView = vi.inflate(R.layout.list_extend_action_view, null);
			final Button expandButton = (Button) expandView.findViewById(R.id.expand_button);
			final ProgressBar expandProgress = (ProgressBar) expandView.findViewById(R.id.expand_progress);
			
			
			expandButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					
					switch(listType){
					
					case 0:
						
						sAudiosAsync = null;
						sAudiosAsync = new SearchAudiosAsync(AudioFinder.this,sQuery, offset + getResources().getInteger(R.integer.max_audios_count), null, null);
						inputManager.hideSoftInputFromWindow(searchTextInput.getWindowToken(), 0);
						sAudiosAsync.execute();
						
						break;
						
					case 1:
						
						uAudiosAsync = null;
						uAudiosAsync = new GetUserAudiosAsync(AudioFinder.this, offset + getResources().getInteger(R.integer.max_audios_count));
						uAudiosAsync.execute();
						
						break;
					
					}
					
					
					
					expandButton.setVisibility(View.INVISIBLE);
					expandProgress.setVisibility(View.VISIBLE);
				
				}
			
			});
					
			define.add(expandView);
			
	}

		switch(listType)
		{
		
		case 0:
			
			globalFilesList = filesList;
			globalAudiosMap = audiosMap;
			globalDefineMap = defineMap;
			globalDefine = define;
			globalAudios = content;
		
			Parcelable state0 = globalAudioList.onSaveInstanceState();
			
			globalAdapter = new AudioListAdapter(getApplicationContext(),R.layout.audio_define,define, globalAudios);
			globalAudioList.setAdapter(globalAdapter);
			globalAudioList.setFocusable(false);	
			globalProgress.setVisibility(View.INVISIBLE);
			
			globalAudioList.onRestoreInstanceState(state0);
			
			break;
			
		case 1:
			userFilesList = filesList;
			userAudiosMap = audiosMap;
			userDefineMap = defineMap;
			userDefine= define;
			userAudios = content;
			
			Parcelable state1 = userAudioList.onSaveInstanceState();
			
			userAdapter = new AudioListAdapter(getApplicationContext(),R.layout.audio_define,define, userAudios);
			userAudioList.setAdapter(userAdapter);
			userAudioList.setFocusable(false);	
			userProgress.setVisibility(View.INVISIBLE);
			
			userAudioList.onRestoreInstanceState(state1);
			
			
			break;
			
		case 2:
			recommendsFilesList = filesList;
			recommendsAudiosMap = audiosMap;
			recommendsDefineMap = defineMap;
			recommendsDefine = define;
			recommendsAudios = content;
		
			Parcelable state2 = recommendsAudioList.onSaveInstanceState();
			
			recommendsAdapter = new AudioListAdapter(getApplicationContext(),R.layout.audio_define,define, recommendsAudios);
			recommendsAudioList.setAdapter(recommendsAdapter);
			recommendsAudioList.setFocusable(false);	
			recommendsProgress.setVisibility(View.INVISIBLE);
			
			recommendsAudioList.onRestoreInstanceState(state2);
			
			break;
		}
		
	
	}
	
	
	public void startLoginActivity() 

	{
		Intent i = new Intent(this,Login.class);
		startActivityForResult(i, REQUEST_LOGIN);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, final Intent data)
	{
		try {
		
		switch(requestCode)
		{
		
		case REQUEST_LOGIN:
			 if (resultCode == RESULT_OK) {
	            	
		           VkData vkData = new VkData();
		            	
		            	vkData.ACCESS_TOKEN = data.getStringExtra("token");
		            	vkData.USER_ID = String.valueOf(data.getLongExtra("user_id",0));
		            	vkData.LAST_IP = Net.getLocalIpAddress();
		            	vkData.USER_FIRST_NAME = data.getStringExtra("first_name");
		            	vkData.USER_LAST_NAME = data.getStringExtra("last_name");
		            	vPrefs.save(vkData);
		            	vPrefs = new VkPrefs(this);
		            	api = new Api(vkData.ACCESS_TOKEN, Login.APP_ID);
		            	
		            	switch(lastErrorPage)
		            	{
		            	
		            	case 0:
		            		searchAudios(null);
		            		break;
		            	case 1:
		            		getUserAudios(null);
		            		break;
		            	case 2:
		            		getRecommendsAudios(null);
		            		break;
		            	}
		          
		            }
			 break;
			 
			
		case PLAY_SERVICE_CALLBACK:
		   	Audio audio = null;
		    long aid = data.getExtras().getLong("_audio_aid");
			 if((currAudio==null)||currAudio.aid != aid)
			 {
			
			
			 View view = null;
			 ImageButton playButton;
			 SeekBar playBar;
			 ProgressBar bufferBar;
			 
			 switch(defTypeMap.get(aid))
				{
				case 0:
					audio = globalAudiosMap.get(aid);
					view =  globalDefineMap.get(audio);
					break;
				case 1:
					audio = userAudiosMap.get(aid);
					view =  userDefineMap.get(audio);
					break;
				case 2:
					audio = recommendsAudiosMap.get(aid);
					view =  recommendsDefineMap.get(audio);
					break;
				}
			 
			 currAudio = audio;
			 
			    playButton = (ImageButton) view.findViewById(R.id.audio_play_button);
			    playBar = (SeekBar) view.findViewById(R.id.audio_playing_progress);
			    bufferBar = (ProgressBar) view.findViewById(R.id.audio_buffered_progress);
			   
			    
				 currPlayProgress = playBar;
				 currBufferProgress = bufferBar;
				 currPlayButton = playButton;
				 

				currBufferProgress.setMax(100);
				 currBufferProgress.setIndeterminate(false);
				
				 currPlayProgress.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){

					@Override
					public void onProgressChanged(SeekBar seekBar, int pos,
							boolean fromUser) {
						
					}

					@Override
					public void onStartTrackingTouch(SeekBar arg0) {
						
						
					}

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						Intent serviceIntent = new Intent(getApplicationContext(), AudioPlayService.class);
						 PendingIntent pi = createPendingResult(PLAY_SERVICE_CALLBACK, serviceIntent, 0);
						Bundle bundle = new Bundle();
							bundle.putBoolean("change_position", true);
							bundle.putInt("pos", seekBar.getProgress());
							bundle.putParcelable("callback", pi);
						serviceIntent.putExtras(bundle);
						
						startService(serviceIntent);
						
						 if(!currAudio.url.startsWith("http://")) currBufferProgress.setProgress(seekBar.getProgress());

						
					}
					 
				 });

			 }
			 

			    
			switch(resultCode)
			{
			
			case AudioPlayService.PLAYER_PREPARE_CALLBACK:
				
				currBufferProgress.setVisibility(View.VISIBLE);
				currBufferProgress.setIndeterminate(true);
				
				break;
			
			case AudioPlayService.PLAYER_PLAY_CALLBACK:
				
				currPlayButton.setImageResource(R.drawable.pause);
				currBufferProgress.setIndeterminate(false);
				
	
				break;
				
			case AudioPlayService.PLAYER_PAUSE_CALLBACK:
				
				currPlayButton.setImageResource(R.drawable.play);
				
				break;
				
			case AudioPlayService.PLAYER_STOP_CALLBACK:
				currPlayButton.setImageResource(R.drawable.play);
				currPlayProgress.setProgress(0);
				currPlayProgress.setVisibility(View.INVISIBLE);
				currBufferProgress.setVisibility(View.INVISIBLE);
				
				break;
				
			case AudioPlayService.PLAYER_UPDATE_CALLBACK:
				currPlayProgress.setFocusable(false);
				currBufferProgress.setIndeterminate(false);
				int pos = data.getExtras().getInt("curr_player_pos");
				int dur = data.getExtras().getInt("total_duration");
				currPlayProgress.setVisibility(View.VISIBLE);
				currPlayProgress.setMax(dur);
				currPlayProgress.setProgress(pos);
				 if(!currAudio.url.startsWith("http://"))
					 {
					 
					 currBufferProgress.setMax(dur);
					 currBufferProgress.setProgress(pos);
					 
					 
					 }
				
				break;
				
			case AudioPlayService.PLAYER_COMPLETION_CALLBACK:
				
				currPlayButton.setImageResource(R.drawable.play);
				currPlayProgress.setProgress(0);
				
				break;
				
			case AudioPlayService.PLAYER_BUFFERED_CALLBACK:
				int percent = data.getExtras().getInt("percent");
				currBufferProgress.setProgress(percent);
				
				break;
			}
			
				break;
		}
		
		} catch (NullPointerException ex)
		{
			
		}
		
           
		
	}
	
	private void doPostFlipActions(ConfigurationData config)
	{
		try{globalDefine = (ArrayList<View>) config.globalAudioListItems;} catch(NullPointerException nex) {};
		try{userDefine = (ArrayList<View>) config.userAudioListItems;} catch(NullPointerException nex) {}; 
		try{recommendsDefine = (ArrayList<View>) config.recommendsListItems;} catch(NullPointerException nex) {}; 
		try{globalAudiosMap = config.globalAudiosMap;} catch(NullPointerException nex) {}; 
		try{globalDefineMap = config.globalDefineMap;}catch(NullPointerException nex) {}; 
		try{userAudiosMap = config.userAudiosMap;} catch(NullPointerException nex) {}; 
		try{userDefineMap = config.userDefineMap;}catch(NullPointerException nex) {}; 
		try{recommendsAudiosMap = config.recommendsAudiosMap;} catch(NullPointerException nex) {}; 
		try{recommendsDefineMap = config.recommendsDefineMap;}catch(NullPointerException nex) {}; 
		try{globalAudios = config.globalAudios;} catch(NullPointerException ex){}
  		try{userAudios = config.userAudios;} catch(NullPointerException ex){}
  		try{recommendsAudios = config.recommendsAudios;} catch(NullPointerException ex){}
  		try{defTypeMap = config.defTypeMap;} catch(NullPointerException ex){}
  		try{currAudio = config.currAudio;} catch(NullPointerException ex){}
  		try{currPlayProgress = config.currPlayProgress;} catch(NullPointerException ex){}
  		try{currBufferProgress = config.currBufferProgress;} catch(NullPointerException ex){}
  		try{currPlayButton = config.currPlayButton;} catch(NullPointerException ex){}
  		try{globalAudioList.onRestoreInstanceState(config.globalListState);} catch(NullPointerException ex){}
  		try{userAudioList.onRestoreInstanceState(config.userListState);} catch(NullPointerException ex){}
  		try{recommendsAudioList.onRestoreInstanceState(config.recommendsListState);} catch(NullPointerException ex){}
  		try{sAudiosAsync = config.sAudiosAsync;} catch(NullPointerException ex){}
  		try{uAudiosAsync = config.uAudiosAsync;} catch(NullPointerException ex){}
  		try{gRecommendsAsync = config.gRecommendsAsync;} catch(NullPointerException ex){}
  		
  		try{sAudiosAsync.link(this);} catch(NullPointerException ex){}
  		try{uAudiosAsync.link(this);} catch(NullPointerException ex){}
  		try{gRecommendsAsync.link(this);} catch(NullPointerException ex){}
  	
  		
  		

  		
		calledOnRetrain = true;
		initUI();
		
		
		
		if(globalDefine!=null)
		{
		globalAdapter = new AudioListAdapter(getApplicationContext(),R.layout.audio_define, globalDefine, globalAudios);
		globalAudioList.setAdapter(globalAdapter);
		globalAudioList.setFocusable(false);	
		globalAudioList.onRestoreInstanceState(config.globalListState);
		}
		
		if(userDefine!=null)
		{
		userListRefreshButton.setVisibility(View.INVISIBLE);
		userAdapter = new AudioListAdapter(getApplicationContext(),R.layout.audio_define, userDefine, userAudios);
		userAudioList.setAdapter(userAdapter);
		userAudioList.setFocusable(false);	
		userAudioList.onRestoreInstanceState(config.userListState);
		}
		
		if(recommendsDefine!=null)
		{
		recommendsListRefreshButton.setVisibility(View.INVISIBLE);
		recommendsAdapter = new AudioListAdapter(getApplicationContext(),R.layout.audio_define, recommendsDefine, recommendsAudios);
		recommendsAudioList.setAdapter(recommendsAdapter);
		recommendsAudioList.setFocusable(false);	
		recommendsAudioList.onRestoreInstanceState(config.recommendsListState);
		}
	}
	
	@Override
	  public Object  onRetainNonConfigurationInstance() {
		  ConfigurationData config = new ConfigurationData();
		  		try{config.globalAudioListItems = globalAdapter.getItems();} catch(NullPointerException ex){}
		  		try{config.userAudioListItems = userAdapter.getItems();} catch(NullPointerException ex){}
		  		try{config.recommendsListItems = recommendsAdapter.getItems();} catch(NullPointerException ex){}
		  		try{config.globalAudiosMap = globalAudiosMap;} catch(NullPointerException ex){}
		  		try{config.globalDefineMap = globalDefineMap;} catch(NullPointerException ex){}
		  		try{config.userAudiosMap = userAudiosMap;} catch(NullPointerException ex){}
		  		try{config.userDefineMap = userDefineMap;} catch(NullPointerException ex){}
		  		try{config.recommendsAudiosMap = recommendsAudiosMap;} catch(NullPointerException ex){}
		  		try{config.recommendsDefineMap = recommendsDefineMap;} catch(NullPointerException ex){}
		  		try{config.defTypeMap = defTypeMap;} catch(NullPointerException ex){}
		  		try{config.globalAudios = globalAudios;} catch(NullPointerException ex){}
		  		try{config.userAudios = userAudios;} catch(NullPointerException ex){}
		  		try{config.recommendsAudios = recommendsAudios;} catch(NullPointerException ex){}
		  		try{config.currAudio = currAudio;} catch(NullPointerException ex){}
		  		try{config.currPlayProgress = currPlayProgress;} catch(NullPointerException ex){}
		  		try{config.currBufferProgress = currBufferProgress;} catch(NullPointerException ex){}
		  		try{config.currPlayButton = currPlayButton;} catch(NullPointerException ex){}
		  		try{config.globalListState = globalAudioList.onSaveInstanceState();} catch(NullPointerException ex){}
		  		try{config.userListState = userAudioList.onSaveInstanceState();} catch(NullPointerException ex){}
		  		try{config.recommendsListState = recommendsAudioList.onSaveInstanceState();} catch(NullPointerException ex){}
		  		
		  		if(gRecommendsAsync != null)gRecommendsAsync.unLink();
		  		if(sAudiosAsync != null)sAudiosAsync.unLink();
		  		if(uAudiosAsync != null)uAudiosAsync.unLink();
		  		
		  		try{config.sAudiosAsync = sAudiosAsync;} catch(NullPointerException ex){}
		  		try{config.uAudiosAsync = uAudiosAsync;} catch(NullPointerException ex){}
		  		try{config.gRecommendsAsync = gRecommendsAsync;} catch(NullPointerException ex){}
		  		
		  		return config;
		  		
		  
		}
	
	   @Override
	    protected Dialog onCreateDialog(int id)
	    {
		   AlertDialog.Builder builder = null;

	    	switch (id){
	    		case WAIT_DIALOG_ID:
	  	    	  ProgressDialog waitDialog = new ProgressDialog(
	  	   				AudioFinder.this);
	  	    	waitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	  	    	waitDialog.setMessage(getResources().getString(R.string.dialog_load_message));
	  	    	waitDialog.setCancelable(true);
	

				
	  	   		    return waitDialog;
	  	   		    
	    		case AUDIO_WAIT_DIALOG_ID:
		  	    	  ProgressDialog audioWaitDialog = new ProgressDialog(
		  	   				AudioFinder.this);
		  	    	audioWaitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		  	    	audioWaitDialog.setMessage(getResources().getString(R.string.dialog_audio_prepare_message));
		  	    	audioWaitDialog.setCancelable(true);
		  	   		    return audioWaitDialog;
		  	   		    
	    		case SHAZAM_INSTALL_DIALOG_ID:
	    			
	    			builder = new AlertDialog.Builder(this);
					
					builder.setTitle(getResources().getString(R.string.dialog_not_shazam_title));
					builder.setMessage(getResources().getString(R.string.dialog_not_shazam_message));
					
					builder.setPositiveButton(getResources().getString(R.string.button_positive), new DialogInterface.OnClickListener(){

						public void onClick(DialogInterface dialog, int arg1) {
							Intent intent = new Intent(Intent.ACTION_VIEW);
							intent.setData(Uri.parse("market://details?id=com.shazam.android"));
							startActivity(intent);
							
						}
						
					});
	    	
					
					
					builder.setNegativeButton(getResources().getString(R.string.button_negative), new DialogInterface.OnClickListener(){

						public void onClick(DialogInterface dialog, int arg1) {
							dismissDialog(SHAZAM_INSTALL_DIALOG_ID);
							
						}
						
					});
	  	   		   
					return builder.create();
					
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
	    			
					
	  	   		   default:
	  	   			   return null;
	    			}
	    }
	   
	   class ConfigurationData {
			public List<View> globalAudioListItems;
			public List<View> userAudioListItems;
			public List<View> recommendsListItems;
			
		    public ArrayList<Audio> globalAudios;
		    public ArrayList<Audio> userAudios;
		    public ArrayList<Audio> recommendsAudios;
			
		    public Parcelable globalListState;
		    public Parcelable userListState;
		    public Parcelable recommendsListState;
		    
			public HashMap<Long, Audio> globalAudiosMap;
			public HashMap<Audio, View> globalDefineMap;
			public HashMap<Long, Audio> userAudiosMap;
			public HashMap<Audio, View> userDefineMap;
			public HashMap<Long, Audio> recommendsAudiosMap;
			public HashMap<Audio, View> recommendsDefineMap;
			public HashMap<Long, Integer> defTypeMap;
			
			public Audio currAudio;
			public SeekBar currPlayProgress;
			public ProgressBar currBufferProgress;
			public ImageButton currPlayButton;
			
			public SearchAudiosAsync sAudiosAsync;
			public GetUserAudiosAsync uAudiosAsync;
			public GetRecommendsAudiosAsync gRecommendsAsync;
			
		

		}
}

	