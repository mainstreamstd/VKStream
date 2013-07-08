package ru.mainstream.vkstream.video;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import android.app.Activity;



public class VideoPlayerHacker  {


	
	public static Source hack(String playerUrl, String thumbUrl,Activity act) throws IOException
	{
		Source result;
		 URL url = new URL(playerUrl);
         URLConnection conn = url.openConnection();
         InputStreamReader rd = new InputStreamReader(conn.getInputStream());
         StringBuilder allpage = new StringBuilder();
         int n = 0;
         char[] buffer = new char[40000];
         while (n >= 0)
         {
             n = rd.read(buffer, 0, buffer.length);
             if (n > 0)
             {
                 allpage.append(buffer, 0, n);              
             }
         }
		new LinkParser();
		result = LinkParser.getVideoSource(allpage.toString(),thumbUrl);
		return result;
	}

}
