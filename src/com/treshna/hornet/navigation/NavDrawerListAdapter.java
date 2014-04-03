package com.treshna.hornet.navigation;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.treshna.hornet.R;

public class NavDrawerListAdapter extends BaseAdapter {
    
    private Context context;
    private ArrayList<NavDrawerItem> navDrawerItems;
     
    public NavDrawerListAdapter(Context context, ArrayList<NavDrawerItem> navDrawerItems){
        this.context = context;
        this.navDrawerItems = navDrawerItems;
    }
 
    @Override
    public int getCount() {
        return navDrawerItems.size();
    }
 
    @Override
    public Object getItem(int position) {       
        return navDrawerItems.get(position);
    }
 
    @Override
    public long getItemId(int position) {
        return position;
    }
 
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        
    	if (navDrawerItems.get(position).getHeader()) { //show a header instead of our regular view.
    		LayoutInflater mInflater = (LayoutInflater)
                    context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.drawer_list_header, null);
            
            TextView txtTitle = (TextView) convertView.findViewById(R.id.title);
            txtTitle.setText(navDrawerItems.get(position).getTitle());
            
            convertView.setClickable(false);
            convertView.setEnabled(false);
            convertView.setOnClickListener(null);
    	} else {
    		
	    	if (convertView == null) {
	            LayoutInflater mInflater = (LayoutInflater)
	                    context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
	            convertView = mInflater.inflate(R.layout.drawer_list_item, null);
	        }
	    	
	    	if (navDrawerItems.get(position).getTitle() == null) {
	    		convertView = new View(context);
	    		convertView.setVisibility(View.GONE);
	    		return convertView;
	    	}
	          
	        ImageView imgIcon = (ImageView) convertView.findViewById(R.id.icon);
	        TextView txtTitle = (TextView) convertView.findViewById(R.id.title);
	        TextView txtCount = (TextView) convertView.findViewById(R.id.counter);
	          
	        imgIcon.setImageResource(navDrawerItems.get(position).getIcon());        
	        txtTitle.setText(navDrawerItems.get(position).getTitle());
	         
	        // displaying count
	        // check whether it set visible or not
	        if(navDrawerItems.get(position).getCounterVisibility()){
	            txtCount.setText(navDrawerItems.get(position).getCount());
	        }else{
	            // hide the counter view
	            txtCount.setVisibility(View.GONE);
	        }
    	}
    	   return convertView;
    }
 
}