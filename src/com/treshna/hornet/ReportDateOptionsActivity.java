package com.treshna.hornet;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.*;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

public class ReportDateOptionsActivity extends Activity {
	private HashMap<String,Object> reportData = new HashMap<String,Object>() ;
	private DatePicker startDatePicker = null;
	private DatePicker endDatePicker = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.setContentView(R.layout.report_user_date_options);
		super.onCreate(savedInstanceState);
		Intent intent = this.getIntent();
		TextView reportNameTxt = (TextView) findViewById(R.id.report_date_options_report_name);
		startDatePicker = (DatePicker) findViewById(R.id.start_date);
		endDatePicker = (DatePicker) findViewById(R.id.end_date);
		Button nextButton =  (Button) findViewById(R.id.btnNext);	
		reportNameTxt.setText(intent.getStringExtra("report_name").trim());
		reportData.put("report_id", intent.getStringExtra("report_id"));
		reportData.put("report_name", intent.getStringExtra("report_name"));
		reportData.put("report_function_name", intent.getStringExtra("report_function_name"));
		nextButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				renderReport();
				
			}
		}); 
		//renderReport();
	}
	
	private Date  dateStringFromPicker (DatePicker datePicker) {
		int day = datePicker.getDayOfMonth();
		int month = datePicker.getMonth();
		int year = datePicker.getYear();
		Calendar cal = Calendar.getInstance();
		cal.set(year, month, day);
		return cal.getTime();	
	}
	
	
	
	private void renderReport(){
		/*JSONObject dateStringer = new JSONObject();
		dateStringer.put("startDate",this.dateStringFromPicker(startDatePicker));
		dateStringer.toString();*/
		/*SimpleDateFormat dateFormatter = new SimpleDateFormat("yy-MM-dd");
		String startDateString = dateFormatter.parse(startDatePicker);*/
		
		reportData.put("start_date",this.dateStringFromPicker(startDatePicker));
		reportData.put("end_date", this.dateStringFromPicker(endDatePicker));
		
		Intent reportColumnsIntent = new Intent(this.getApplicationContext(), ReportColumnOptionsActivity.class);
		
		for (Map.Entry param: reportData.entrySet()){
			//System.out.println(param.getKey().toString()+ ": " + param.getValue().toString());
			if ((param.getKey().toString().compareTo("start_date") == 0) || (param.getKey().toString().compareTo("end_date") == 0) ){
				reportColumnsIntent.putExtra(param.getKey().toString(), ((Date) param.getValue()).getTime());
			}
				
		}
						
		this.startActivity(reportColumnsIntent);
	}
	
	
	
	

}
