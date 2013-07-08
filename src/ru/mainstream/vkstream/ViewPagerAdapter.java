package ru.mainstream.vkstream;

import java.util.List;

import android.content.Context;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

public class ViewPagerAdapter extends PagerAdapter{
	
	public static final int CONTENT_TYPE_AUDIO = 0x000001;
	public static final int CONTENT_TYPE_VIDEO = 0x000002;
	public static final int CONTENT_TYPE_GROUPS = 0x000003;

	 List<View> pages = null;
	 protected final String[] AUDIO_CONTENT; 
	 protected final String[] VIDEO_CONTENT; 
	 protected final String[] GROUPS_CONTENT; 
	 
	 
	 private int contentType;
	 
	 public ViewPagerAdapter(List<View> pages, int contentType, Context context)
	 {
		 this.pages = pages;
		 this.contentType = contentType;

		 AUDIO_CONTENT = new String[] { context.getResources().getString(R.string.header_search_audios), context.getResources().getString(R.string.header_my_audios), context.getResources().getString(R.string.header_recommends_audios)};
		 VIDEO_CONTENT = new String[] { context.getResources().getString(R.string.header_search_videos), context.getResources().getString(R.string.header_my_videos)};
		 GROUPS_CONTENT = new String[] { context.getResources().getString(R.string.header_my_friends), context.getResources().getString(R.string.header_my_groups)};
	 }
	 
	 @Override
	    public Object instantiateItem(View pCollection, int pPosition) {
	            View view = pages.get(pPosition);
	            ((ViewPager) pCollection).addView(view, 0);
	           
	            return view;
	    }

	    @Override
	    public void destroyItem(View pCollection, int pPosition, Object pView) {
	            ((ViewPager) pCollection).removeView((View) pView);
	    }

	    @Override
	    public int getCount() {
	            return pages.size();
	    }

	    @Override
	    public boolean isViewFromObject(View pView, Object pObject) {
	            return pView.equals(pObject);
	    }

	    @Override
	    public void finishUpdate(View pView) {
	    }

	    @Override
	    public void restoreState(Parcelable pParcelable, ClassLoader pLoader) {
	    }

	    @Override
	    public Parcelable saveState() {
	            return null;
	    }

	    @Override
	    public void startUpdate(View pView) {
	    }

	    
	    @Override
	    public CharSequence getPageTitle(int position) {
	    	switch(contentType)
	    	{
	    	
	    	case ViewPagerAdapter.CONTENT_TYPE_AUDIO:
	    		return this.AUDIO_CONTENT[position];
	    		
	    	case ViewPagerAdapter.CONTENT_TYPE_VIDEO:
	    		return this.VIDEO_CONTENT[position];
	    		
	    	case ViewPagerAdapter.CONTENT_TYPE_GROUPS:
	    		return this.GROUPS_CONTENT[position];
	    	}
	    	
	    	return null;
	        
	    }
	
}
