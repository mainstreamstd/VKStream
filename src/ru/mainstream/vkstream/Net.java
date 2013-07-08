package ru.mainstream.vkstream;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.Enumeration;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class Net {
	
	private Net(){};
	
	public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo nInfo = cm.getActiveNetworkInfo();
        if (nInfo != null && nInfo.isConnected()) {
            return true;
        }
        else {
            return false;
        }
        
    }
	
	public static String getLocalIpAddress() {
	    try {
	        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
	            NetworkInterface intf = en.nextElement();
	            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
	                InetAddress inetAddress = enumIpAddr.nextElement();
	                if (!inetAddress.isLoopbackAddress()) {
	                    return inetAddress.getHostAddress().toString();
	                }
	            }
	        }
	    } catch (SocketException ex) {}
	    
	    return null;
	}
	
	public static boolean isHostReachable(String address) {
	    try {
	        URL url = new URL(address);
	 Log.d("Net", "Trying host: "+address);
	        HttpURLConnection urlc =
	            (HttpURLConnection) url.openConnection();
	        urlc.setRequestProperty("User-Agent", "userAgent");
	        urlc.setRequestProperty("Connection", "close");
	        urlc.setConnectTimeout(1000 * 10);
	        urlc.connect();
	        if (urlc.getResponseCode() == 200) {
	            urlc.disconnect();
	            Log.i("Net", "Host " + address + " reachable");
	            return true;
	        }
	    } catch (MalformedURLException e) {
	    	Log.w("Net", "Host " + address + " not reachable");
	    } catch (IOException e) {
	    	Log.w("Net", "Host " + address + " not reachable");
	    }
	    return false;
	}
}
