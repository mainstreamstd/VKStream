package ru.mainstream.vkstream;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import ru.mainstream.vkstream.tools.FileSysHelper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;

public class FoldersActivity extends SherlockActivity {

	
	private ListView foldersList;
	private TextView currPathText;
	
	private String rootDir = "/mnt";
	private String currPath = "/";
	private String absPath;
	
	private ArrayList<View> foldersViews;
	
	ArrayList<File> dirList = new ArrayList<File>();
	
	@Override
	public void onCreate(Bundle bundle)
	{
		super.onCreate(bundle);
		setContentView(R.layout.file_explorer);
		foldersList = (ListView)findViewById(R.id.folders_list);
		currPathText = (TextView)findViewById(R.id.curr_loc);
		
		if((String) getLastNonConfigurationInstance() != null) currPath = (String) getLastNonConfigurationInstance(); 
		absPath = rootDir + currPath;
		
		foldersViews = new ArrayList<View>();
		
		setPathView(currPath);
	}
	
	
	 Comparator<? super File> filecomparator = new Comparator<File>(){
		  
		  public int compare(File file1, File file2) {

		   if(file1.isDirectory()){
		    if (file2.isDirectory()){
		     return String.valueOf(file1.getName().toLowerCase()).compareTo(file2.getName().toLowerCase());
		    }else{
		     return -1;
		    }
		   }else {
		    if (file2.isDirectory()){
		     return 1;
		    }else{
		     return String.valueOf(file1.getName().toLowerCase()).compareTo(file2.getName().toLowerCase());
		    }
		   }
		    
		  }  
		 };

	
		 
	private void setPathView(String path)
	{
		foldersList.setAdapter(null);
		foldersViews = new ArrayList<View>();
		dirList = new ArrayList<File>();
		currPathText.setText(rootDir + path);
		
		
		
		ArrayList<File> filesList = (!path.equals("/")) ? FileSysHelper.getFilesInDir(new File(rootDir + path), false) : collectSdCards();
		
		
		final LayoutInflater vi = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		for(File file : filesList) if(file.isDirectory()) dirList.add(file);
		
		final File[] dirArray = FilesListToArray(dirList);
		Arrays.sort(dirArray, filecomparator);
		
		for(int i=0; i<dirList.size(); i++)
		{
			View view = vi.inflate(R.layout.folder_def, null);
			TextView folderTitle = (TextView) view.findViewById(R.id.folder_name);
			folderTitle.setText(dirArray[i].getName());
			foldersViews.add(view);
		}
		
		
		
		
		foldersList.setClickable(true);
		foldersList.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				
				currPath += dirArray[position].getName()+"/";
				absPath = rootDir + currPath;
				setPathView(currPath);
				
			}
			
		});
		foldersList.setAdapter(new FoldersAdapter(getApplicationContext(),R.layout.folder_def, foldersViews));
		
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
			try 
			{
				currPath = currPath.substring(0,currPath.length()-1);
				currPath = currPath.substring(0, currPath.lastIndexOf("/"));
				currPath += "/";
			absPath = currPath;
			setPathView(currPath);
		} catch (Exception e) { 
			Intent resultIntent = new Intent();
			setResult(Activity.RESULT_CANCELED, resultIntent);
			finish();
		}
		
		return true;
	}
	
	public void chooseFolder(View v)
	{
		Intent resultIntent = new Intent();
		resultIntent.putExtra("path", currPath);
		resultIntent.putExtra("abs_path", absPath);
		setResult(Activity.RESULT_OK, resultIntent);
		finish();
	}

	

	
	private File[] FilesListToArray(ArrayList<File> list)
	{
		File[] result = new File[list.size()];
		for(int i=0; i<list.size(); i++)
		{
			result[i] = list.get(i);
		}
		return result;
	}
	
	private ArrayList<File> collectSdCards()
	{
		File root = new File("/mnt/");
		ArrayList<File> result = new ArrayList<File>();
		
		for(File folder : root.listFiles())
		{
			if(folder.isDirectory() && (
					folder.getName().equals("sdcard") || 
					folder.getName().equals("external_sd") ||
					folder.getName().equals("usb_storage") ||
					folder.getName().equals("external") || 
					folder.getName().equals("extSdCard") ||
					folder.getName().equals("usb")))
			{
				result.add(folder);
			}
		}
		
		if(result.size()==0)
			result.add(Environment.getExternalStorageDirectory());
		
		return result;
	}

	@Override
	public String onRetainNonConfigurationInstance() {
		return currPath;
	}
}
