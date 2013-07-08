package ru.mainstream.vkstream.video;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.mainstream.vkstream.Net;

import android.util.Log;

public class LinkParser {
	
	private final static String YouTubeMatcher = "http://www.youtube.com([^\"]*)";
	private final static String RuTubeMatcher = "http://video.rutube.ru([^\']*)";
	private final static String VkMatcher = "(?i)src=\"([^\"]*.(240|360|480|720).(mp4|flv|mov))";
	



	
	protected static Source getVideoSource(String htmlPage,String thumb)
	{
		Source result= new Source();
		result.thumb = thumb;
		Pattern vk = Pattern.compile(VkMatcher);
		Matcher VkVideo = vk.matcher(htmlPage);
		
		Log.i("LinkParcer", "Thumb: " + thumb);
		
		if(VkVideo.find())
		{
			Pattern qual240p = Pattern.compile("(?i)src=\"([^\"]*.240.(mp4|flv|mov))");
			Matcher qual240 = qual240p.matcher(htmlPage);
			
			Pattern qual360p = Pattern.compile("(?i)src=\"([^\"]*.360.(mp4|flv|mov))");
			Matcher qual360 = qual360p.matcher(htmlPage);
			
			Pattern qual480p = Pattern.compile("(?i)src=\"([^\"]*.480.(mp4|flv|mov))");
			Matcher qual480 = qual480p.matcher(htmlPage);
			
			Pattern nonBuldedQual = Pattern.compile("(?i)src=\"([^\"]*.480.(mp4|flv|mov))");
			Matcher nonBuildedQualMatcher = nonBuldedQual.matcher(htmlPage);
			
			
			
			if(qual240.find())
			{
			  result.qual.put("240", qual240.group(1));
			  result.url = qual240.group(1);
			  
			  if(result.url.endsWith("-.240.mp4"))
			  {
				  String dmgUrl = result.url;
				  String healer = thumb;
				  
				  
				  healer = healer.substring(healer.indexOf("thumbnails/")+11);
				  healer = healer.substring(8);
				  healer = healer.substring(0, healer.indexOf("."));
				  
				  dmgUrl = dmgUrl.substring(0, dmgUrl.indexOf("-")+1) + healer + ".vk.flv";
				  dmgUrl = dmgUrl.replace("ruu0","ru");
				  dmgUrl = dmgUrl.replace("/videos/", "/assets/video/");
				  dmgUrl = "http://" + dmgUrl;
				  Log.w("LinkParser", "Dmg found: " + dmgUrl);
				  
				  
				  result.qual.put("flv", dmgUrl);
				  return result;
			  }
			  
			  Log.i("LinkParser", "qual 240 found!: " + qual240.group(1));
			  
			  result.qual.put("flv", result.url.replace(".240.mp4", ".flv"));

			  
			  
			  if(qual360.find())
				{
				  result.qual.put("360", qual360.group(1));
				  result.url = qual360.group(1);
				  Log.i("LinkParser", "qual 360 found!: " + qual360.group(1));
				}
				
				if(qual480.find())
				{
				  result.qual.put("480", qual480.group(1));
				  result.url = qual480.group(1);
				  Log.i("LinkParser", "qual 480 found!: " + qual480.group(1));
				}
				
				if(nonBuildedQualMatcher.find())
				{
				  String qual720 = nonBuildedQualMatcher.group(1);
				 qual720 = qual720.replace(".360.mp4", ".720.mp4");
				  if(Net.isHostReachable(qual720))
				  {
					  Log.i("LinkParser", "qual 720 found!: " + qual720);
					  result.url = qual720;
					  result.qual.put("720", qual720);

				  }
				}
			  
			} 

			
			
			
		} else {
			Pattern yt = Pattern.compile(YouTubeMatcher);
			Matcher YouTubeVideo = yt.matcher(htmlPage);
			if(YouTubeVideo.find())
			{
				Log.w("yt",YouTubeVideo.group(1));
				result.url = YouTubeVideo.group(1);
				result.vkontakte = false;
				result.youtube = true;
			} else {
				Pattern rt = Pattern.compile(RuTubeMatcher);
				Matcher RuTubeVideo = rt.matcher(htmlPage); 
				if(RuTubeVideo.find())
				{
				 result.url = RuTubeVideo.group(0);
					Log.v("rutube", result.url);
					result.vkontakte = false;
					result.rutube = true;
				}
			}
		}
		
		return result;
	}
	
	

}
