package com.treshna.hornet.report;

import java.util.HashMap;

import android.content.Context;
import android.content.res.Resources;

public class ReportQueryResources {
	    private static Resources resources = null;
	    
	public static String getFilterQueryByName(Context context, String filterName){
		resources = context.getResources();
		filterName = filterName.replace(" ", "_").replace("-","_");
		System.out.println("Filter Name: " + filterName + " Context: " +context.getApplicationContext().getPackageName());
		int arrayId = context.getResources().getIdentifier(filterName, "array", context.getApplicationContext().getPackageName());
		String[] filterData = resources.getStringArray(arrayId);
		return filterData[0];
	}
	
	public static String getFilterTypeByName(Context context, String filterName){
		resources = context.getResources();
		filterName = filterName.replace(" ", "_").replace("-","_");
		System.out.println("Filter Name: " + filterName + " Context: " +context.getApplicationContext().getPackageName());
		int arrayId = context.getResources().getIdentifier(filterName, "array", context.getApplicationContext().getPackageName());
		String[] filterData = resources.getStringArray(arrayId);
		return filterData[1];
	}
	
}
