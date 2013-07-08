package ru.mainstream.vkstream;

import java.io.IOException;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import ru.mainstream.vkstream.tools.PlayerbarCloseTool;


import com.perm.kate.api.Audio;


import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

public class AudioPlayService extends IntentService {

	public static final int PLAYER_PREPARE_CALLBACK = 0xacfaa;
	public static final int PLAYER_PLAY_CALLBACK = 0xacfab;
	public static final int PLAYER_PAUSE_CALLBACK = 0xacfac;
	public static final int PLAYER_STOP_CALLBACK = 0xacfad;
	public static final int PLAYER_UPDATE_CALLBACK = 0xacfaf;
	public static final int PLAYER_COMPLETION_CALLBACK = 0xacfaaa;
	public static final int PLAYER_BUFFERED_CALLBACK = 0xacfaab;
	
	public static final int PLAY_SERVICE_STOP_REQUEST = 0xcca12f;
	
	public static final int AUDIO_PLAY_STATE = 101;
	public static final int AUDIO_PAUSE_STATE = 102;
	public static final int AUDIO_STOP_STATE = 103;
	public static final int AUDIO_PREPARING_STATE = 104;

	boolean preparing = false;
	
	 MediaPlayer player;
	
	Notification selfNotification;
	NotificationManager notificationManager = null;
	PendingIntent pIntent = null;
	
	PendingIntent callback;
	
	Audio currTrack;
	Audio lastTrack;

	HashMap<Long, Integer> audioStatesMap = new HashMap<Long, Integer>();
	Timer updateTimer;
	TimerTask updateTask;
	
	int currBufferPercent = 0;
	
	public AudioPlayService() { super("AudioPlayService"); }
	
	
	@Override
	public void onCreate()
	{
		super.onCreate();

		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			pIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(getApplicationContext(), AudioFinder.class), 0);
		else 
			pIntent = PendingIntent.getService(getApplicationContext(), 0, new Intent(getApplicationContext(), PlayerbarCloseTool.class), 0);
		
		selfNotification = new Notification(R.drawable.audio_playbar_play_indicator, getResources().getString(R.string.text_play_audio), System.currentTimeMillis());
		
		selfNotification.flags = selfNotification.flags | Notification.FLAG_ONGOING_EVENT;
		selfNotification.contentView = new RemoteViews(getPackageName(), R.layout.player_bar);
		selfNotification.contentIntent = pIntent;
		
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
		
		PendingIntent playerbarPlay = PendingIntent.getService(getApplicationContext(), 0, new Intent(getApplicationContext(), AudioPlayService.class),0);
	    selfNotification.contentView.setOnClickPendingIntent(R.id.playerbar_play_button, playerbarPlay);
	    
	   
	    PendingIntent playerbarStop = PendingIntent.getService(getApplicationContext(), 0, new Intent(getApplicationContext(), PlayerbarCloseTool.class), 0);
	    selfNotification.contentView.setOnClickPendingIntent(R.id.playerbar_close_button, playerbarStop);
	    
		} else {
			selfNotification.contentView.setViewVisibility(R.id.playerbar_play_button, View.INVISIBLE);
			selfNotification.contentView.setViewVisibility(R.id.playerbar_close_button, View.INVISIBLE);
		}
		player = new MediaPlayer();
		player.reset();
		updateTimer = new Timer();
		
		updateTask = createNewUpdateTimerTask();

		
	} 
	

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{

		
		
		if(intent.getExtras() !=null && intent.getExtras().getBoolean("stop_service"))
		{
			stopAudio(currTrack);
			stopForeground(true);
			stopSelf();
			return START_NOT_STICKY;
		}
		
	
		
		try {
		callback = intent.getExtras().getParcelable("callback");
		} catch (NullPointerException ex)
		{	
			doCurrTrackAction();
			startForeground(1, selfNotification);
			return START_REDELIVER_INTENT;
		}
		
		if(intent.getExtras().getBoolean("change_position"))
		{
			player.seekTo(intent.getExtras().getInt("pos"));
			callback = intent.getExtras().getParcelable("callback");
			
			final Bundle playBundle = new Bundle();
			playBundle.putLong("_audio_aid", currTrack.aid);
			
			final Intent prepareIntent = new Intent();
			prepareIntent.putExtras(playBundle);
			
			try { callback.send(getApplicationContext(), PLAYER_PREPARE_CALLBACK, prepareIntent); } catch (CanceledException e1) { e1.printStackTrace(); }
			
			player.setOnSeekCompleteListener(new OnSeekCompleteListener(){

				@Override
				public void onSeekComplete(MediaPlayer mp) {
					
					try { callback.send(getApplicationContext(), PLAYER_PLAY_CALLBACK, prepareIntent);
							player.start();
							selfNotification.contentView.setImageViewResource(R.id.playerbar_play_button, R.drawable.audio_playbar_pause_indicator);
							} catch (CanceledException e1) { e1.printStackTrace(); }
					
				}
				
			});
			
			return START_REDELIVER_INTENT;
		}
		
		Audio audio = intent.getExtras().getParcelable("audio");
		
		if(!Net.isOnline(getApplicationContext()) && !audio.url.contains(Environment.getExternalStorageDirectory().getAbsolutePath()))
		{
			Toast.makeText(getApplicationContext(), getResources().getString(R.string.text_not_connection_prompt_2), Toast.LENGTH_SHORT).show();
			return START_NOT_STICKY;
		}
		
		currTrack = audio;
		
		selfNotification.contentView.setTextViewText(R.id.playerbar_artist_text, currTrack.artist);
	    selfNotification.contentView.setTextViewText(R.id.playerbar_title_text, currTrack.title);
	    
		if(lastTrack!=null)
		{
			if(lastTrack.aid != currTrack.aid || (preparing&&lastTrack.aid != currTrack.aid))
			{
				stopAudio(lastTrack);
			}
			
		}
		lastTrack = currTrack;

		doCurrTrackAction();
		
		player.setOnCompletionListener(new OnCompletionListener(){

			@Override
			public void onCompletion(MediaPlayer mp) {
				player.seekTo(0);
				Bundle bundle = new Bundle();
				bundle.putLong("_audio_aid", currTrack.aid);
				Intent i = new Intent();
				i.putExtras(bundle);
				try {
					callback.send(getApplicationContext(), PLAYER_COMPLETION_CALLBACK, i);
				} catch (CanceledException e) {
					e.printStackTrace();
				}

				updateTimer.cancel();
				updateTask = createNewUpdateTimerTask();
				stopForeground (true);
			}
			
			
			
		});
		
		player.setOnBufferingUpdateListener(new OnBufferingUpdateListener(){

			@Override
			public void onBufferingUpdate(MediaPlayer mp, int percent) {
				
				if(currBufferPercent<100)
				{
					currBufferPercent = percent;
				Bundle bundle = new Bundle();
				bundle.putLong("_audio_aid", currTrack.aid);
				bundle.putInt("percent", percent);
				Intent i = new Intent();
				i.putExtras(bundle);
				try {
					callback.send(getApplicationContext(), PLAYER_BUFFERED_CALLBACK, i);
				} catch (CanceledException e) {
					e.printStackTrace();
				}
				}
			}
			
		});
		
		startForeground(1, selfNotification);
		return START_REDELIVER_INTENT ;
	}

	
	private void doCurrTrackAction()
	{

			
		
		
			if(audioStatesMap.get(currTrack.aid) == null) audioStatesMap.put(currTrack.aid, AUDIO_STOP_STATE);
			
			switch(audioStatesMap.get(currTrack.aid))
			{
			
			case AUDIO_STOP_STATE:
				playAudio(currTrack);
					break;
					
			case AUDIO_PAUSE_STATE:
				unpauseAudio(currTrack);
				break;
					
			case AUDIO_PLAY_STATE:
				pauseAudio(currTrack);
				break;
			}
	}
	
	
	
	private void playAudio(final Audio audio)
	{
		if(audio!=null)
		{
			final Bundle playBundle = new Bundle();
			playBundle.putLong("_audio_aid", audio.aid);
		
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) selfNotification.contentView.setImageViewResource(R.id.playerbar_play_button, R.drawable.audio_playbar_pause_indicator);
			
		Intent prepareIntent = new Intent();
			prepareIntent.putExtras(playBundle);
			currBufferPercent = 0;
		
			try{ player.setDataSource(audio.url); } catch (IllegalArgumentException e1) { e1.printStackTrace(); } catch (SecurityException e1) { e1.printStackTrace(); } catch (IllegalStateException e1) { e1.printStackTrace(); } catch (IOException e1) { e1.printStackTrace(); }
			
			try { callback.send(getApplicationContext(), PLAYER_PREPARE_CALLBACK, prepareIntent); } catch (CanceledException e1) { e1.printStackTrace(); }
			preparing = true;
			audioStatesMap.put(audio.aid, AUDIO_PREPARING_STATE);
			try { player.prepare(); } catch (IllegalStateException e1) { e1.printStackTrace(); } catch (IOException e1) { e1.printStackTrace(); }
		
			player.setOnPreparedListener(new OnPreparedListener() {

				@Override
				public void onPrepared(MediaPlayer player) {
					player.start();	
					
					preparing = false;
					updateTimer = new Timer();
					updateTimer.schedule(updateTask, 0, 1000);
					
					audioStatesMap.put(audio.aid, AUDIO_PLAY_STATE);
					
					playBundle.putInt("total_duration", player.getDuration());
				
						
					Intent playIntent = new Intent();
						playIntent.putExtras(playBundle);
						
						try { callback.send(getApplicationContext(), PLAYER_PLAY_CALLBACK, playIntent); } catch (CanceledException e1) { e1.printStackTrace(); }
				}
				
			});
		
		}
	}
	
	
	private void pauseAudio(Audio audio)
	{
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) selfNotification.contentView.setImageViewResource(R.id.playerbar_play_button, R.drawable.audio_playbar_play_indicator);
		if(audioStatesMap.get(audio.aid) == AUDIO_PREPARING_STATE)
		{
			stopAudio(audio);
			preparing = false;
			return;
		}

		player.pause();
		updateTimer.cancel();
		updateTask = createNewUpdateTimerTask();
		audioStatesMap.put(audio.aid, AUDIO_PAUSE_STATE);
		final Bundle pauseBundle = new Bundle();
		pauseBundle.putLong("_audio_aid", audio.aid);
	
	Intent pauseIntent = new Intent();
		pauseIntent.putExtras(pauseBundle);
		
		try { callback.send(getApplicationContext(), PLAYER_PAUSE_CALLBACK, pauseIntent); } catch (CanceledException e1) { e1.printStackTrace(); }
	}
	
	private void unpauseAudio(Audio audio)
	{
		
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) selfNotification.contentView.setImageViewResource(R.id.playerbar_play_button, R.drawable.audio_playbar_pause_indicator);
		player.start();
		updateTimer = new Timer();
		updateTimer.schedule(updateTask, 0, 1000);
		audioStatesMap.put(audio.aid, AUDIO_PLAY_STATE);
		final Bundle unpauseBundle = new Bundle();
		unpauseBundle.putLong("_audio_aid", audio.aid);
	
	Intent unpauseIntent = new Intent();
		unpauseIntent.putExtras(unpauseBundle);
		
		try { callback.send(getApplicationContext(), PLAYER_PLAY_CALLBACK, unpauseIntent); } catch (CanceledException e1) { e1.printStackTrace(); }
		
	}
	
	private void stopAudio(Audio audio)
	{
		if(audio!=null)
		{
		
		Bundle audioBundle = new Bundle();
		audioBundle.putLong("_audio_aid", audio.aid);
		
	Intent stopIntent = new Intent();
		stopIntent.putExtras(audioBundle);
		
		audioStatesMap.put(audio.aid, AUDIO_STOP_STATE);
		player.stop();
		player.reset();
		currBufferPercent = 0;
		updateTimer.cancel();
		updateTask = createNewUpdateTimerTask();
	    try { callback.send(getApplicationContext(), PLAYER_STOP_CALLBACK, stopIntent); } catch (CanceledException e1) { e1.printStackTrace(); }
	    
		}
	}
	
	private TimerTask createNewUpdateTimerTask()
	{
	return	new TimerTask() {

			@Override
			public void run() {

				selfNotification.contentView.setProgressBar(R.id.playerbar_progress, player.getDuration(), player.getCurrentPosition(), false);
				startForeground(1, selfNotification);
				Bundle bundle = new Bundle();
				bundle.putLong("_audio_aid", currTrack.aid);
				bundle.putInt("total_duration", player.getDuration());
				bundle.putInt("curr_player_pos", player.getCurrentPosition());
				Intent i = new Intent();
				i.putExtras(bundle);
	
				try {
					callback.send(getApplicationContext(), PLAYER_UPDATE_CALLBACK, i);
				} catch (CanceledException e) {
					e.printStackTrace();
				}
				
				
			}
			
		};
	}
	
	
	@Override
	protected void onHandleIntent(Intent intent) {}

}
