package com.treshna.hornet.report;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.widget.SimpleAdapter;

public class ReportsListAdapter extends SimpleAdapter {
	private HashMap<String,String> mapData = null;
	public ReportsListAdapter(Context context,
			List<? extends Map<String, ?>> data, int resource, String[] from,
			int[] to) 
		{
		super(context, data, resource, from, to);
		
		mapData = (HashMap<String, String>) data;
		
		// TODO Auto-generated constructor stub
	
		}

	@Override
	public void setViewBinder(ViewBinder viewBinder) {
		// TODO Auto-generated method stub
		SimpleAdapter.ViewBinder binder = this.getViewBinder();
		
		super.setViewBinder(viewBinder);
	}
}