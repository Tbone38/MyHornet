package com.treshna.hornet;

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
	
	public static String getMainQuery(Context context, String reportId) {
		resources = context.getResources();
		HashMap<String,String> queryMap = new HashMap<String,String>();
		System.out.println("Report_Id_" + reportId + "_Query" + context.getApplicationContext().getPackageName());
		int arrayId = context.getResources().getIdentifier("Report_Id_" + reportId + "_Query", "array", context.getApplicationContext().getPackageName());
		String[] query = resources.getStringArray(arrayId);
		String mainQuery = query[0].substring((query[0]).indexOf('|') + 1 );
	    queryMap.put((query[0]).substring(0,(query[0]).indexOf('|') ), (query[0]).substring((query[0]).indexOf('|') + 1 ));
	    return mainQuery;
	}
	
	public static HashMap<String,String> getAllQueryFields(Context context, String reportId) {
		resources = context.getResources();
		HashMap<String,String> fieldsMap = new HashMap<String,String>();
		System.out.println("Report_Id_" + reportId + "_Fields");
		int arrayId = context.getResources().getIdentifier("Report_Id_" + reportId + "_Fields", "array", context.getApplicationContext().getPackageName());
		String[] fields = resources.getStringArray(arrayId);
		String fieldKey = "";
		String fieldValue = "";
		for (int i = 0; i < fields.length; i++) {
			fieldKey = fields[i].substring(0,(fields[i]).indexOf('|'));
			fieldValue = fields[i].substring((fields[i]).indexOf('|')+1);
			fieldsMap.put(fieldKey, fieldValue);
		    }
		return fieldsMap;
	   }

}
