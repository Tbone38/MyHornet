package com.treshna.hornet;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class ReportDateOptionsActivity extends Activity {
	private HashMap<String,String> reportData = new HashMap<String,String>() ;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.setContentView(R.layout.report_user_date_options);
		super.onCreate(savedInstanceState);
		Intent intent = this.getIntent();
		TextView reportNameTxt = (TextView) findViewById(R.id.report_date_options_report_name);
		reportNameTxt.setText(intent.getStringExtra("report_name"));
		reportData.put("report_id", intent.getStringExtra("report_id"));
		reportData.put("report_name", intent.getStringExtra("report_name"));
		reportData.put("report_function_name", intent.getStringExtra("report_function_name"));
		renderReport();
	}
	
	private void renderReport(){
		for (Map.Entry param: reportData.entrySet()){
			System.out.println(param.getKey().toString()+ ": " + param.getValue().toString());
		}
	}
	
	
	
	

}
