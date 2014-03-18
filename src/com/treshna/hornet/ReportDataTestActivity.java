package com.treshna.hornet;

import java.sql.Date;

import com.treshna.hornet.ReportColumnOptionsActivity.GetReportDataByDateRange;

import android.os.Bundle;

public class ReportDataTestActivity extends ReportColumnOptionsActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void getReportData(String functionName, Date startDate,
			Date endDate) {
		GetReportData syncNames = new GetReportData(functionName, startDate , endDate);
		syncNames.execute(null,null);
	}
	
		private class GetReportData extends GetReportDataByDateRange {

			public GetReportData(String functionName, Date startDate,
					Date endDate) {
				super(functionName, startDate, endDate);
				// TODO Auto-generated constructor stub
			}
			
		}
	
	

}
