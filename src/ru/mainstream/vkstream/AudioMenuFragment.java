package ru.mainstream.vkstream;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by User on 08.07.13.
 */
public class AudioMenuFragment extends ListFragment {

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);



        ListAdapter adapter = new ListAdapter(getActivity());
        for (int i = 0; i < 20; i++) {
            adapter.add(new MenuItem("Sample List", android.R.drawable.ic_menu_search));
        }
        setListAdapter(adapter);
    }


    private class MenuItem {

       public int iconResId;
       public String tag;



        public MenuItem(String tag, int iconResId)
        {
            this.tag = tag;
            this.iconResId = iconResId;
        }


    }


    class ListAdapter extends ArrayAdapter<MenuItem> {

        public ListAdapter(Context context) {
            super(context, 0);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.slidingmenu_item, null);
            }
            ImageView icon = (ImageView) convertView.findViewById(R.id.menuitem_image);
            icon.setImageResource(getItem(position).iconResId);
            TextView title = (TextView) convertView.findViewById(R.id.menuitem_text);
            title.setText(getItem(position).tag);

            return convertView;
        }

    }
}
