package ru.mainstream.vkstream.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;


import android.os.Environment;
import android.os.StatFs;

import com.perm.kate.api.Audio;

public class FileSysHelper {
	
	public static HashMap<Audio, File> getAudioFilesInDir(ArrayList<Audio> audios, File dir)
	{
		HashMap<Audio, File> files = new HashMap<Audio, File>();
		
		for(File file : dir.listFiles())
		{
			for(Audio audio : audios)
			{
				if(file.getName().equals(audio.title +"["+audio.aid+"]"+".mp3"))
				files.put(audio, file);
			}
		}
		return files;
	}
	
	public static void clearDir(String path)
	{
		File dir = new File(path);
		
		if(dir.isDirectory())
		{
			for(File file : dir.listFiles())
			{
				file.delete();
			}
		}
	}
	
	public static long getDirSize(File dir)
	{
		if(dir.isFile()) return 0;
		
		long totalSize = 0;
		ArrayList<File> files = getFilesInDir(dir, true);
		
		for(int i=0;i<files.size();i++)
		{
			totalSize+=files.get(i).length();
		}
		
		return totalSize;
	}
	
	public static String getStoragePath()
	{
		return Environment.getExternalStorageDirectory().getAbsolutePath();
	}
	
	  public static int getExternalFreeSpace() {
	    	
	    	final String path = Environment.getExternalStorageDirectory().getPath();
	    	final int mbfree = getSpaceForFile(path);
	    	
	    	return mbfree;
	    }
	    
	    public static int getInternalFreeSpace() {

	    	final String path = Environment.getDataDirectory().getPath();
	    	final int mbfree = getSpaceForFile(path);
	    	
	    	return mbfree;
	    }
	    
	    private static int getSpaceForFile(String path) {
	    	
	    	StatFs sf = new StatFs(path);
	    	final int blocks = sf.getAvailableBlocks();
	    	final int blockSize = sf.getBlockSize();
	    	final int totalBytes = blocks*blockSize;
	    	return totalBytes-(1024*1024);
	    }
	
	public static ArrayList<File> getFilesInDir(File dir, boolean showHidden)
	{
		if(!dir.isDirectory()) return null;
		ArrayList<File> result = new ArrayList<File>();
		for(File file : dir.listFiles()){
			
			if(!showHidden)
				{
				if(!file.isHidden()) 
					result.add(file);
				} else { 
					result.add(file);
				}
			
			}
		return result;
	}
	
	
}
