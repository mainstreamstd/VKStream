package ru.mainstream.vkstream;



import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;


public class FoldersAdapter extends ArrayAdapter<View> {
	private List<View> listitems=null;
	

	
	
	public FoldersAdapter(Context context, int textViewResourceId, ArrayList<View> define) {
		super(context, textViewResourceId, define);
		this.listitems = define;
	}
 

	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
			return listitems.get(position);
}
	
	
	
	public List<View> getItems() {
		return listitems;
	}
	
	 
}
