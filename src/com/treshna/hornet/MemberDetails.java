package com.treshna.hornet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
	//TODO: more null handling
public class MemberDetails extends NFCActivity implements OnClickListener {
	Cursor cur;
	ContentResolver contentResolver;
	String COLOUR = "#FF5FBDBC";
	ViewPager mPager;
	ArrayList<ArrayList<String>> memberships;
	String memberID;
	
	private static final String TAG = "MemberDetails";
	
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN) 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Services.setContext(this);
		setContentView(R.layout.member_details);
		ActionBar actionBar = getSupportActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
		Intent intent = getIntent();
		
		contentResolver = this.getContentResolver();
		ArrayList<String> tagInfo = (ArrayList<String>) intent.getStringArrayListExtra(VisitorsViewAdapter.EXTRA_ID);
		memberID = tagInfo.get(0);
		String visitDate = tagInfo.get(1);
		
		cur = contentResolver.query(ContentDescriptor.Membership.CONTENT_URI, null, ContentDescriptor.Membership.Cols.MID+" = ?",
				new String[] {memberID}, null);
	
		memberships = new ArrayList<ArrayList<String>>();
		if (cur.getCount() > 0) {
			while (cur.moveToNext()) {
				ArrayList<String> membership = new ArrayList<String>();
				membership.add(cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.MSID)));
				membership.add(cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.PNAME)));
				membership.add(cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.VISITS)));
				membership.add(cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.MSSTART)));
				membership.add(cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.EXPIRERY)));
				
				memberships.add(membership);
			}
		}
		cur.close();
		
		Uri uri = Uri.withAppendedPath(ContentDescriptor.Image.IMAGE_JOIN_MEMBER_URI,
				memberID);
		String[] selection = {"*",ContentDescriptor.Member.Cols.PHCELL+" AS 'Mobile Number'",
				ContentDescriptor.Member.Cols.PHHOME+" AS 'Home Number'", 
				ContentDescriptor.Member.Cols.PHWORK+" AS 'Work Number' "};
		cur = contentResolver.query(uri, selection, null, null, null);
		
		if (cur == null) {
			return;
		}
		cur.moveToFirst();
		/*int j;
		for (j=0;j<cur.getColumnCount();j+=1) {
			System.out.print("\n\n ****COLUMN("+j+"): "+cur.getColumnName(j));
			System.out.print("\n\n ***VALUE: "+cur.getString(j));
		}*/
		memberID = cur.getString(0);
		TextView memberName = (TextView) this.findViewById(R.id.memberName);
		ImageView smileView = (ImageView) this.findViewById(R.id.smiley);
		LinearLayout memberDetails = (LinearLayout) this.findViewById(R.id.memberDetails);
		LinearLayout memberInfo = (LinearLayout) this.findViewById(R.id.memberinfo);
		
		memberName.setPadding(Services.convertdpToPxl(this, 25), 0, 0, 0);
		memberName.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.FNAME))
				+" "+cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.SNAME)));
		if (cur.isNull(cur.getColumnIndex(ContentDescriptor.Member.Cols.COLOUR)) == true) {
			memberName.setTextColor(Color.BLACK);
		} else {
			memberName.setTextColor(Color.parseColor(cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.COLOUR))));
		}
		
		AssetManager am = getResources().getAssets();
		String face = null;
		String visits = cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.HAPPINESS));
		if (visits.length() >= 2){	
			if (visits.substring(visits.length()-2, visits.length()).equals(":)")){
				face = "face-smile.png";
			} else if (visits.substring(visits.length()-2, visits.length()).equals(":|")){
				face = "face-plain.png";
			} else if (visits.substring(visits.length()-2, visits.length()).equals(":(")){
				face = "face-sad.png";
		}	}
		if (face != null) {
			InputStream is = null;
			try {
				is = am.open(face);
			} catch (IOException e) {
				e.printStackTrace();
			}
			Bitmap sm = BitmapFactory.decodeStream(is);
			smileView.setImageBitmap(sm);
		}
		LinearLayout.LayoutParams llparams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		TextView numberH = new TextView(this);
		numberH.setPadding(Services.convertdpToPxl(this, 35), 0, 0, 0);
		numberH.setText("Member Number");
		numberH.setTextSize(13);
		numberH.setLayoutParams(llparams);
		memberDetails.addView(numberH);
		
		llparams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		TextView number = new TextView(this);
		number.setPadding(Services.convertdpToPxl(this, 45), 0, 0, 0);
		number.setText(memberID);
		number.setTextSize(18);
		number.setLayoutParams(llparams);
		memberDetails.addView(number);
		
		/**
		 * membership details, for each membership.
		 */
		if (memberships.size() == 0) {
			TextView membershipH = (TextView) findViewById(R.id.memberinfoH);
			membershipH.setVisibility(View.GONE);
		}
		for (int i=0;i <memberships.size(); i +=1) {
			int null_count = 0;
			/**
			 * 1 = no membership name
			 * 2 = no started date
			 * 4 = no expirery
			 * 8 = no visits to date
			 */
			ArrayList<String> member = memberships.get(i);
			RelativeLayout msWindow = new RelativeLayout(this); //todo make this a relative layout
			llparams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			llparams.setMargins(7, 7, 7, 7);
			msWindow.setLayoutParams(llparams);
			msWindow.setBackgroundColor(Color.LTGRAY);
			//msWindow.setOrientation(LinearLayout.VERTICAL);
			
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			if (member.get(1) != null && member.get(1).compareTo("") != 0) {
				TextView membershipH = new TextView(this);
				membershipH.setPadding(Services.convertdpToPxl(this, 35), 0, 0, 0);
				membershipH.setText("Membership");
				membershipH.setTextSize(13);
				membershipH.setId(55000);
				membershipH.setLayoutParams(params);
				//memberInfo.addView(membershipH);
				msWindow.addView(membershipH);
				
				TextView membershipT = new TextView(this);
				membershipT.setPadding(Services.convertdpToPxl(this, 45), 0, 0, 0);
				membershipT.setText(member.get(1));
				membershipT.setTextSize(18);
				membershipT.setId(55001); 
				params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
				params.addRule(RelativeLayout.BELOW, 55000);
				params.setMargins(3, 3, 3, 3);
				membershipT.setLayoutParams(params);
				//memberInfo.addView(membershipT);
				msWindow.addView(membershipT);
			} else {
				Log.v(TAG+".createview", "No Membership");
				null_count += 1;
			}
			
			if ( member.get(3) != null && member.get(3).compareTo("null") != 0) {
				String date = Services.dateFormat(member.get(3), "yyyy-MM-dd", "dd MMM yy");
				
				TextView memberSH = new TextView(this);
				memberSH.setPadding(Services.convertdpToPxl(this, 35), 0, 0, 0);
				memberSH.setText("Membership Started");
				memberSH.setTextSize(13);
				memberSH.setId(55002);
				params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				params.addRule(RelativeLayout.BELOW, 55001);
				params.setMargins(3, 3, 3, 3);
				memberSH.setLayoutParams(params);
				//memberInfo.addView(memberSH);
				msWindow.addView(memberSH);
				
				TextView memberST = new TextView(this);
				memberST.setPadding(Services.convertdpToPxl(this, 45), 0, 0, 0);
				memberST.setText(date);
				memberST.setTextSize(18);
				memberST.setId(55003);
				params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				params.addRule(RelativeLayout.BELOW, 55002);
				params.setMargins(3, 3, 3, 3);
				memberST.setLayoutParams(params);
				//memberInfo.addView(memberST);
				msWindow.addView(memberST);
			} else {
				null_count += 2;
			}
			
			if (member.get(4) != null && member.get(4).compareTo("null") != 0) {
				
				TextView memberEH = new TextView(this);
				memberEH.setPadding(Services.convertdpToPxl(this, 35), 0, 0, 0);
				memberEH.setText("Membership Expires");
				memberEH.setTextSize(13);
				memberEH.setId(55004);
				params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				params.addRule(RelativeLayout.BELOW, 55001);
				params.addRule(RelativeLayout.RIGHT_OF, 55002);
				params.setMargins(3, 3, 3, 3);
				memberEH.setLayoutParams(params);
				//memberInfo.addView(memberEH);
				msWindow.addView(memberEH);
				
				String date = Services.dateFormat(cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.EXPIRERY)), "yyyy-MM-dd", "dd MMM yy");
				TextView memberET = new TextView(this);
				memberET.setPadding(Services.convertdpToPxl(this, 45), 0, 0, 0);
				memberET.setText(date);
				memberET.setTextSize(18);
				memberET.setId(55005);
				params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				params.addRule(RelativeLayout.BELOW, 55004);
				params.addRule(RelativeLayout.RIGHT_OF, 55003);
				params.addRule(RelativeLayout.ALIGN_LEFT, 55004);
				params.setMargins(3, 3, 3, 3);
				memberET.setLayoutParams(params);
				//memberInfo.addView(memberET);
				msWindow.addView(memberET);
			} else {
				null_count += 4;
			}
			
			if (member.get(2) != null && member.get(2).compareTo("null") != 0) {
				int below = 55005;
				TextView visitsH = new TextView(this);
				visitsH.setPadding(15, 15, 15, 15);
				visitsH.setText("Visits to Date");
				visitsH.setTextSize(18);
				visitsH.setId(55006);
				params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				View v = (View) msWindow.findViewById(55005);
				if (v == null) {
					below = 55003;
				}
				params.addRule(RelativeLayout.BELOW, below);
				params.setMargins(3, 3, 3, 3);
				visitsH.setLayoutParams(params);
				//memberInfo.addView(visitsH);
				msWindow.addView(visitsH);
				
				TextView visitsT = new TextView(this);
				visitsT.setPadding(15, 15, 15, 15);
				visitsT.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.VISITS)));
				visitsT.setTextSize(18);
				visitsT.setId(55007);
				params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				params.addRule(RelativeLayout.BELOW, below);
				params.addRule(RelativeLayout.RIGHT_OF, 55006);
				params.setMargins(3, 3, 3, 3);
				visitsT.setLayoutParams(params);
				//memberInfo.addView(visitsT);
				msWindow.addView(visitsT);
			} else {
				null_count +=8;
			}
			
			//add hold/edit membership button here.
			/*if (null_count < 8  && member.get(0) != null && member.get(0).compareTo("null") != 0) {
				Log.v(TAG, "creating HOLD Button.");
				TextView hold = new TextView(this);
				hold.setId(55008);
				hold.setTag(member.get(0));
				hold.setPadding(15, 15, 15, 15);
				hold.setText("Hold");
				hold.setTextSize(20);
				hold.setTextColor(Color.parseColor("#FF5FBDBC"));
				hold.setGravity(Gravity.CENTER);
				
				params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				params.addRule(RelativeLayout.BELOW, 55005);
				params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
				params.addRule(RelativeLayout.ALIGN_TOP, 55007);
				//params.addRule(RelativeLayout.ALIGN_BOTTOM, 55007);
				params.setMargins(3, 3, 13, 3);
				hold.setLayoutParams(params);
				hold.setClickable(true);
				hold.setOnClickListener(this);
				
				msWindow.addView(hold);
			}*/
			
			
			memberInfo.addView(msWindow);
		}
		
		LinearLayout notesGroup = (LinearLayout) this.findViewById(R.id.membernotes);
		if (cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.NOTES)) != null) {
			if (cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.NOTES)).compareTo("null") != 0) {
				
				TextView notesT = new TextView(this);
				notesT.setPadding(Services.convertdpToPxl(this, 45), 0, 0, 0);
				notesT.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.NOTES)));
				notesT.setTextSize(18);
				notesT.setLayoutParams(llparams);
				notesGroup.addView(notesT);
		} 	}
		/*
		 * The Below If-Statements might(?) hard crash the system if the item (e.g. string(17))
		 * is Null. Easiest Solution is nested IF's (see above), though best would be 
		 * to better handle null data on entry to database-cache. (so that it's an empty string)
		 */
		if ((cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.TASK1)) == null) 
				|| (cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.TASK1)).compareTo("null") == 0)
				|| (cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.TASK1)).length() == 0)){
			
			TextView tasks = new TextView(this);
			tasks.setPadding(Services.convertdpToPxl(this, 35), 0, 0, 0);
			tasks.setText("No Pending Tasks");
			tasks.setTextSize(13);
			tasks.setLayoutParams(llparams);
			notesGroup.addView(tasks);
			
		} else {
			TextView tasksH = new TextView(this);
			tasksH.setPadding(Services.convertdpToPxl(this, 35), 0, 0, 0);
			tasksH.setText("Tasks");
			tasksH.setTextSize(13);
			tasksH.setLayoutParams(llparams);
			notesGroup.addView(tasksH);
			
			int l;
			for(l=12;l<=14;l+=1){ //cur.getColumnIndex(ContentDescriptor.Member.Cols.TASK1)
				if (cur.getString(l) != null) {
					TextView tasks = new TextView(this);
					tasks.setPadding(45, 0, 0, 10);
					tasks.setText(cur.getString(l));
					tasks.setTextSize(16);
					llparams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
					llparams.setMargins(0, 0, 0, 5);
					tasks.setLayoutParams(llparams);
					notesGroup.addView(tasks);
				}
			}			
			
		}
		if ((cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.BOOK1)) == null) 
				|| (cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.BOOK1)).compareTo("null") == 0) 
				|| (cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.BOOK1)).length() == 0)){
			
			TextView bookings = new TextView(this);
			bookings.setPadding(Services.convertdpToPxl(this, 35), 0, 0, 0);
			bookings.setText("No Pending Bookings");
			bookings.setTextSize(13);
			bookings.setLayoutParams(llparams);
			notesGroup.addView(bookings);

		} else {
			TextView bookingsH = new TextView(this);
			bookingsH.setPadding(Services.convertdpToPxl(this, 35), 0, 0, 0);
			bookingsH.setText("Bookings");
			bookingsH.setTextSize(13);
			bookingsH.setLayoutParams(llparams);
			notesGroup.addView(bookingsH);
			
			int l;
			for(l=15;l<=17;l+=1){ //cur.getColumnIndex(ContentDescriptor.Member.Cols.BOOK1)
				if (cur.getString(l) != null) {
					TextView bookings = new TextView(this);
					bookings.setPadding(45, 0, 0, 0);
					bookings.setText(cur.getString(l));
					bookings.setTextSize(16);
					llparams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
					llparams.setMargins(0, 0, 0, 5);
					bookings.setLayoutParams(llparams);
					notesGroup.addView(bookings);
				}
			}
		}
		
		if (visitDate != null && visitDate.compareTo("") == 0) { //fix this
			TextView visitTH = new TextView(this);
			visitTH.setPadding(Services.convertdpToPxl(this, 35), 0, 0, 0);
			visitTH.setText("Visit Time");
			visitTH.setTextSize(13);
			visitTH.setLayoutParams(llparams);
			notesGroup.addView(visitTH);
			
			TextView visitT = new TextView(this);
			visitT.setPadding(Services.convertdpToPxl(this, 45), 0, 0, 0);
			visitDate = Services.dateFormat(visitDate, "yyyy-MM-dd HH:mm", "dd MMM yy 'at' HH:mm aa");
			visitT.setText(visitDate);
			visitT.setTextSize(18);
			visitT.setLayoutParams(llparams);
			notesGroup.addView(visitT);
		}
		
		if (!cur.isNull(cur.getColumnIndex(ContentDescriptor.Member.Cols.LASTVISIT)) 
				&& cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.LASTVISIT)).compareTo("") != 0) {
			String lastVisit = Services.dateFormat(cur.getString(19), "yyyy-MM-dd HH:mm", "dd MMM yy 'at' HH:mm aa");
			
			TextView lastVH = new TextView(this);
			lastVH.setPadding(Services.convertdpToPxl(this, 35), 0, 0, 0);
			lastVH.setText("Previous Visit");
			lastVH.setTextSize(13);
			lastVH.setLayoutParams(llparams);
			notesGroup.addView(lastVH);
			
			TextView lastVT = new TextView(this);
			lastVT.setPadding(Services.convertdpToPxl(this, 45), 0, 0, 0);
			lastVT.setText(lastVisit);
			lastVT.setTextSize(18);
			lastVT.setLayoutParams(llparams);
			notesGroup.addView(lastVT);
		}
		
		
		
		/*
		 * Dynamically build Views & Buttons as required.
		 */
		LinearLayout layout = (LinearLayout) findViewById(R.id.membercontact);
		layout.setOrientation(LinearLayout.VERTICAL);
		int i;
		
		for (i=7;i<=9;i+=1) {
			if (cur.getString(i) != null) {
				if (cur.getString(i).compareTo("") != 0) {
					TextView heading = new TextView(this);
					heading.setId(i+20);
					heading.setPadding(3, 5, 5, 0);
					RelativeLayout.LayoutParams rlayout = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
					rlayout.setMargins(100, 2, 15, 0); //hard-coded?
					heading.setLayoutParams(rlayout);
					heading.setTextSize(13);
					heading.setText(cur.getColumnName(i));
					
					TextView phone = new TextView(this);
					phone.setId(i+10);
					phone.setPadding(3, 0, 5, 5);
					rlayout = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
					rlayout.setMargins(100, 0, 15, 3);//hard-coded?
					rlayout.addRule(RelativeLayout.BELOW, heading.getId());
					phone.setLayoutParams(rlayout);
					phone.setTextSize(18);
					phone.setText(cur.getString(i));
		
					View bottom = new View(this);
					rlayout = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, 1);
					rlayout.addRule(RelativeLayout.BELOW, phone.getId());
					bottom.setBackgroundColor(Color.parseColor(COLOUR));
					bottom.setLayoutParams(rlayout);
					
					TextView call = new TextView(this);
					call.setPadding(20, 12, 30, 5);
					call.setText("Call");
					call.setTextSize(18);
					call.setId(i);
					
					rlayout = new RelativeLayout.LayoutParams
							(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
					rlayout.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
					rlayout.addRule(RelativeLayout.ALIGN_TOP, heading.getId());
					rlayout.addRule(RelativeLayout.ALIGN_BOTTOM, phone.getId());
					rlayout.setMargins(20, 0, 20, 0);
					call.setLayoutParams(rlayout);
					
					View line = new View(this);
					rlayout = new RelativeLayout.LayoutParams(1, LayoutParams.MATCH_PARENT);
					rlayout.addRule(RelativeLayout.LEFT_OF, call.getId());
					rlayout.addRule(RelativeLayout.ALIGN_TOP, heading.getId());
					rlayout.addRule(RelativeLayout.ALIGN_BOTTOM, phone.getId());
					line.setBackgroundColor(Color.parseColor(COLOUR));
					line.setLayoutParams(rlayout);
					
					RelativeLayout row = new RelativeLayout(this);
					row.setId(i+100);
					row.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
					row.addView(heading);
					row.addView(phone);
					row.addView(call);
					row.setOnClickListener(this);
					row.setClickable(true);
					row.setTag(cur.getString(i));
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
						row.setBackground(getResources().getDrawable(R.drawable.button));
					} else {
						row.setBackgroundDrawable(getResources().getDrawable(R.drawable.button));
					}
	
					row.addView(bottom);
					layout.addView(row);
				}
			}
		}		 
		
		// Null-Handling (shit-in, shit-out) - do it At the other end?
		if (cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.EMAIL)) != null) {
			if (cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.EMAIL)).compareTo("null") != 0) {
				
				TextView heading = new TextView(this);
				heading.setId(15+20);
				heading.setPadding(3, 5, 5, 0);
				RelativeLayout.LayoutParams rlayout = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
				rlayout.setMargins(100, 2, 15, 0); //hard-coded?
				heading.setLayoutParams(rlayout);
				heading.setTextSize(13);
				heading.setText("Email :");
				
				TextView email = new TextView(this);
				email.setId(15+10);
				rlayout = new RelativeLayout.LayoutParams
						(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
				rlayout.setMargins(100, 2, 15, 3); //hard-coded?
				rlayout.addRule(RelativeLayout.BELOW, heading.getId());
				email.setLayoutParams(rlayout);
				email.setTextSize(18);
				email.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.EMAIL)));
				
				TextView send = new TextView(this);
				send.setText("Email");
				send.setTextSize(18);
				send.setPadding(20, 12, 26, 5);
				send.setId(15);
				
				rlayout = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
				//rlayout.addRule(RelativeLayout.RIGHT_OF, line.getId());
				rlayout.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
				rlayout.addRule(RelativeLayout.ALIGN_TOP, heading.getId());
				rlayout.addRule(RelativeLayout.ALIGN_BOTTOM, email.getId());
				rlayout.setMargins(20, 0, 10, 0);
				send.setLayoutParams(rlayout);
				
				View line = new View(this);
				rlayout = new RelativeLayout.LayoutParams(1, LayoutParams.MATCH_PARENT);
				rlayout.addRule(RelativeLayout.LEFT_OF, send.getId());
				rlayout.addRule(RelativeLayout.ALIGN_TOP, heading.getId());
				rlayout.addRule(RelativeLayout.ALIGN_BOTTOM, email.getId());
				line.setBackgroundColor(Color.parseColor(COLOUR));
				line.setId(100);
				line.setLayoutParams(rlayout);
				
				RelativeLayout row = new RelativeLayout(this);
				row.setId(115);
				row.addView(heading);
				row.addView(email);
				row.addView(send);
				row.setTag(cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.EMAIL)));
				row.setClickable(true);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
					row.setBackground(getResources().getDrawable(R.drawable.button));
				} else {
					row.setBackgroundDrawable(getResources().getDrawable(R.drawable.button));
				}
				row.setOnClickListener(this);
				layout.addView(row);
		} }

		
		cur.close();
		String[] projection = {ContentDescriptor.Image.Cols.ID};
		cur = contentResolver.query(ContentDescriptor.Image.CONTENT_URI, projection,
				null, null,ContentDescriptor.Image.Cols.ID+" DESC LIMIT 1");
		cur.moveToFirst();
		int highestImgId = 0;
		if (cur.getCount() > 0) highestImgId = cur.getInt(0)+1;
		cur.close();
		List<String> images = new ArrayList<String>();
		i = 0; 
		for(i=0;i<highestImgId;i+=1){
			String imgDir = this.getExternalFilesDir(null)+"/"+i+"_"+memberID+".jpg";
			File imgFile = new File(imgDir);
			if (imgFile.exists() == true){
				images.add(imgDir);
			}
		}
		images.add("-1");
		
		mPager = (ViewPager) findViewById(R.id.gallery);
		
		 ImageAdapter adapter = new ImageAdapter(this, images, memberID);
		 mPager.setAdapter(adapter);
		 //Necessary or the pager will only have one extra page to show
		 // make this at least however many pages you can see
		 mPager.setOffscreenPageLimit(adapter.getCount());
		 //A little space between pages
		 mPager.setPageMargin(15);
		 mPager.setClipToPadding(false);
		 mPager.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
		 //mPager.set
		  
		TextView moveleft, moveright;
		moveleft = (TextView) this.findViewById(R.id.moveimageLeft);
		moveleft.setClickable(true);
		moveleft.setOnClickListener(this);
		
		moveright = (TextView) this.findViewById(R.id.moveimageRight);
		moveright.setClickable(true);
		moveright.setOnClickListener(this);
		
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}
	
	@Override
	public void onClick(View v) {//id == column from cursor.
		
		switch(v.getId()) {
		case (107): //if contacting doesn't work, ensure these numbers match the phone-row id. (100 +i)
		case (108):
		case (109):{
				String ph ="tel:"+ v.getTag().toString();
				Intent intent = new Intent(Intent.ACTION_DIAL); //ACTION_DIAL, OR ACTION_CALL
				intent.setData(Uri.parse(ph));
				startActivity(intent);
				break;
				}
		case (115):{
				String email="mailto:"+Uri.encode(v.getTag().toString())+"?subject="+Uri.encode("Gymmaster");
				Intent intent = new Intent(Intent.ACTION_SENDTO);
				intent.setData(Uri.parse(email));
				startActivity(intent);
				break;
				}
		case (R.id.moveimageLeft):{
			if (mPager.getCurrentItem() > 0) {
				mPager.setCurrentItem(mPager.getCurrentItem()-1);
			}
			break;
			}
		case (R.id.moveimageRight):{
			if (mPager.getCurrentItem() < (mPager.getChildCount()-1)) {
				mPager.setCurrentItem(mPager.getCurrentItem()+1);
			}
			break;
		}
		case (55008):{
			String membershipid = null;
			if (v.getTag() instanceof String) {
				membershipid = (String) v.getTag();
			}
			Intent i = new Intent(this, MembershipHold.class);
			i.putExtra(Services.Statics.KEY, memberID);
			i.putExtra(Services.Statics.MSID, membershipid);
			startActivity(i);
			break;
		}
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.not_main, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
	    case (R.id.action_home):{
	    	Intent i = new Intent (this, MainActivity.class);
	    	startActivity(i);
	    	return true;
	    }
	    case (R.id.action_createclass):{
	    	Intent i = new Intent(this, ClassCreate.class);
	    	startActivity(i);
	    	return true;
	    }
	    case (R.id.action_settings):
	    	Intent settingsIntent = new Intent(this, SettingsActivity.class);
	    	startActivity(settingsIntent);
	    	return true;
	    case (R.id.action_update): {
	    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		 	if (Integer.parseInt(preferences.getString("sync_frequency", "-1")) == -1) {
		 		Services.setPreference(this, "sync_frequency", "5");
		 	}
		 	PollingHandler polling = Services.getPollingHandler();
	    	polling.startService();
	    	return true;
	    }
	    case (R.id.action_halt): {
	    	PollingHandler polling = Services.getPollingHandler();
	    	polling.stopPolling(false);
	    	Services.setPreference(this, "sync_frequency", "-1");
	    	return true;
	    }
	    case (R.id.action_bookings):{
	    	Intent bookings = new Intent(this, HornetDBService.class);
			bookings.putExtra(Services.Statics.KEY, Services.Statics.BOOKING);
		 	this.startService(bookings);
	    	
		 	Intent intent = new Intent(this, BookingsSlidePager.class);
	       	startActivity(intent);
	       	return true;
	    }
	    case (R.id.action_addMember):{
	    	Intent intent = new Intent(this, MemberAdd.class);
	    	startActivity(intent);
	    	return true;
	    }	    
	    default:
	    	return super.onOptionsItemSelected(item);
	    }
	}
	
	
	/***************************/
	/**
	 * TODO: fix the broken swiping.
	 */
	//public class ImageAdapter extends FragmentStatePagerAdapter {
	public class ImageAdapter extends PagerAdapter {
		
		private List<String> imageList;
		private String memberID;
		private Context ctx;
		
		
		public ImageAdapter(Context c, List<String> images, String id) {
			//super(fm);
			this.imageList = images;
			this.memberID = id;
			this.ctx = c;
		}

		
		@Override
		public int getCount() {
			return imageList.size();
		}
		
		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			
			RelativeLayout rootView = new RelativeLayout(ctx);
			RelativeLayout.LayoutParams rlayout = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			
			
			ImageView img = new ImageView(ctx);
			int padding = 7;
			img.setPadding(padding, padding, padding, padding);			
			img.setId(position);
			
			List<String> tagInfo = new ArrayList<String>();
			tagInfo.add(memberID);
			tagInfo.add(Integer.toString(position));
			tagInfo.add(imageList.get(position));
			
			String imgDir = imageList.get(position);
			TextView imgText = null;
			File imgFile = null;
			if (imgDir.compareTo("-1") == 0){
				//add Image display
				imgText = new TextView(ctx);
				imgText.setText(R.string.button_add_picture);
				imgText.setTextSize(18);
				imgText.setGravity(Gravity.CENTER);
				rlayout = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				
				rlayout.addRule(RelativeLayout.ALIGN_LEFT, img.getId());
				rlayout.addRule(RelativeLayout.ALIGN_TOP, img.getId());
				rlayout.addRule(RelativeLayout.ALIGN_RIGHT, img.getId());
				rlayout.addRule(RelativeLayout.ALIGN_BOTTOM, img.getId());
				rlayout.addRule(RelativeLayout.CENTER_IN_PARENT);
				rlayout.setMargins(1, 1, 1, 1);
				imgText.setLayoutParams(rlayout);
				imgText.bringToFront();
				img.setPadding(5, 5, 5, 5);
			    
				try {
					imgFile = File.createTempFile("img", null);
				} catch (IOException e) {
					imgFile = new File(imgDir);
				}
			} else {
				img.bringToFront();
				imgFile = new File(imgDir);
			}
			final BitmapFactory.Options options = new BitmapFactory.Options();
		    options.inJustDecodeBounds = true;
		    BitmapFactory.decodeFile(imgFile.getAbsolutePath(), options);
		    options.inSampleSize = Services.calculateInSampleSize(options,500, 450);
		    options.inJustDecodeBounds = false;
		    Bitmap bm = BitmapFactory.decodeFile(imgFile.getAbsolutePath(), options);
		    img.setImageBitmap(bm);
		    //img.setPadding(5, 5, 5, 5);
		    rlayout = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		    rlayout.addRule(RelativeLayout.CENTER_VERTICAL);
			img.setLayoutParams(rlayout);
		  
		    rootView.addView(img);
		    if (imgText == null){
		    	rootView.setTag(tagInfo);
		    	
		    } else {
		    	rootView.addView(imgText);
		    	rootView.setTag(tagInfo);
		    }
		    
		    rootView.setClickable(true);
		    rootView.setOnClickListener(new OnClickListener() {

				@SuppressWarnings("unchecked")
				@Override
				public void onClick(View v) {
					List<String> tagInfo = null;
					String memberid, rowid, fileDir, selection, date, message;
					Intent camera;
					Cursor cur;
					ContentResolver cResolver = ctx.getContentResolver();
					if (v.getTag() instanceof List<?>) {
						tagInfo = (List<String>) v.getTag();
					}
					memberid = tagInfo.get(0);
					rowid = tagInfo.get(1);
					fileDir = tagInfo.get(2);
					if (fileDir.compareTo("-1") == 0){
						camera = new Intent(ctx, CameraWrapper.class);
						camera.putExtra(VisitorsViewAdapter.EXTRA_ID,memberid);
						ctx.startActivity(camera);
					} else {
						
						selection = ContentDescriptor.Image.Cols.ID+" = "+rowid
								+" AND "+ContentDescriptor.Image.Cols.MID+" = "+memberid;
						cur = cResolver.query(ContentDescriptor.Image.CONTENT_URI, null, selection, null, null);
						if (cur.getCount() <= 0) return;
						cur.moveToFirst();
						date = Services.dateFormat(cur.getString(2), "dd MMM yy hh:mm:ss aa", "yyyy-MM-dd");
						message = "Image Taken: "+date+ "\nImage Description: "+cur.getString(3);
		   				Toast.makeText(ctx, message, Toast.LENGTH_LONG).show();
					}
				}
		    	
		    });
		    
			((ViewPager) container).addView(rootView, 0);
			return rootView;
		}
		
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			((ViewPager) container).removeView((RelativeLayout) object);
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == ((RelativeLayout) object);
		} 
	}
	
}
