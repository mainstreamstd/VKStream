package ru.mainstream.vkstream.tools;

import java.util.List;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.util.Log;

public class AppManager {

	public static boolean isPackageInstalled(Context context, String packageName) {
	    PackageManager pm = context.getPackageManager();
	    boolean available = false;
	    try {
	        pm.getPackageInfo(packageName, 0);
	        available = true;
	    } catch (NameNotFoundException e) {
	    }
	        
	    return available;
	}
	
	public static void startApplication(Context context, String packageName) {
	    Intent intent = new Intent()
	        .setPackage(packageName)
	        .addCategory(Intent.CATEGORY_LAUNCHER)
	        .setAction(Intent.ACTION_MAIN)
	        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET | Intent.FLAG_ACTIVITY_NEW_TASK);
	            
	    PackageManager pm = context.getPackageManager();
	    List<ResolveInfo> list = pm.queryIntentActivities(intent, 0);
	            
	    if (list != null) {
	        for (ResolveInfo ri : list) {
	            intent.setClassName(ri.activityInfo.packageName, ri.activityInfo.name);
	            break;
	        }
	    } else {
	        Log.e("AppManager", String.format("Cannot resolve application '%s' activities", packageName));
	    }
	            
	    try {
	        context.startActivity(intent);
	    } catch (ActivityNotFoundException ex) {
	        Log.e("AppManager", ex.toString());
	    }
	}
	
	static public String formatDuration(long val) {
	    StringBuilder buf=new StringBuilder(20);
	    String sgn="";

	    if(val<0) { sgn="-"; val=Math.abs(val); }

	    if(val>=3600000) append(buf,sgn,0,( val/3600000));
	    append(buf,(val>=3600000) ? ":" : "",2,((val%3600000)/60000));
	    append(buf,":",2,((val%60000)/1000));
	    return buf.toString();
	    }
	
	static private void append(StringBuilder tgt, String pfx, int dgt, long val) {
	    tgt.append(pfx);
	    if(dgt>1) {
	        int pad=(dgt-1);
	        for(long xa=val; xa>9 && pad>0; xa/=10) { pad--;           }
	        for(int  xa=0;   xa<pad;        xa++  ) { tgt.append('0'); }
	        }
	    tgt.append(val);
	    }
	
}
