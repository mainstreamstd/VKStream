package ru.mainstream.vkstream;



import ru.mainstream.vkstream.tools.UserPrefs;
import ru.mainstream.vkstream.tools.VkPrefs;
import ru.mainstream.vkstream.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
public class VKstreamMain extends Activity {
    
	UserPrefs uPrefs;
	VkPrefs vkPrefs;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.init_screen);
        uPrefs = new UserPrefs("VKStreamPrefs",this);
        vkPrefs = new VkPrefs(this);
        if(!uPrefs.getBooleanPref("notFirst1640"))
        {
        	Preferences.saveAudioDir(getApplicationContext(),Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+getResources().getString(R.string.default_audio_folder_path));
        	Preferences.saveVideoDir(getApplicationContext(), Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+getResources().getString(R.string.default_video_folder_path));
        	uPrefs.savePref("notFirst1640", true);
        }
        
        if((vkPrefs.get().USER_ID.equals("-1") || vkPrefs.get().USER_ID.equals("N/A")) && !vkPrefs.get().ACCESS_TOKEN.equals(getResources().getString(R.string.default_token))) 
    		vkPrefs.reset();
        
        
      
        login();
        
    }
    


	public void login()
    {
              Intent i = new Intent(this, MenuAct.class);
    	      startActivity(i);
    	      this.finish();
    }
   
    
    
}
