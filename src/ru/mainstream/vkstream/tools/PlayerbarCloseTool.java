package ru.mainstream.vkstream.tools;

import ru.mainstream.vkstream.AudioPlayService;
import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

public class PlayerbarCloseTool extends IntentService {

	public PlayerbarCloseTool() { super("PlayerbarCloseTool"); }

	@Override
	public void onCreate()
	{
		super.onCreate();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		 Intent serviceIntent = new Intent(getApplicationContext(), AudioPlayService.class);
		 Bundle bundle = new Bundle();
		 bundle.putBoolean("stop_service", true);
		 serviceIntent.putExtras(bundle);
		 startService(serviceIntent);
		 
		 stopSelf();
		
		return START_NOT_STICKY;
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
	}

}
