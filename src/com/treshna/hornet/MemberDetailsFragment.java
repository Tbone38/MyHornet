package com.treshna.hornet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.treshna.hornet.R.color;
	//TODO: more null handling
public class MemberDetailsFragment extends Fragment implements OnClickListener {
	Cursor cur;
	ContentResolver contentResolver;
	String COLOUR = "#FF5FBDBC";
	ViewPager mPager;
	ArrayList<ArrayList<String>> memberships;
	String memberID;
	private View view;
	private String visitDate;
	RadioGroup rg;
	
	private static final String TAG = "MemberDetails";
	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN) 
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Services.setContext(getActivity());
		Log.i(TAG, "Created At:"+new Date().toString());
		contentResolver = getActivity().getContentResolver();
	
		memberID = this.getArguments().getString(Services.Statics.MID);
		visitDate = this.getArguments().getString(Services.Statics.KEY);
	}
	
	@SuppressLint("NewApi")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		  super.onCreateView(inflater, container, savedInstanceState);
	
		  view = inflater.inflate(R.layout.member_details, container, false);
	      view = setupView();
		  
		  return view;
	}
	
	
	private View setupView() {
		
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
				return view;
			}
			cur.moveToFirst();
			
			memberID = cur.getString(0);
			TextView memberName = (TextView) view.findViewById(R.id.memberName);
			LinearLayout memberDetails = (LinearLayout) view.findViewById(R.id.memberDetails);
			LinearLayout memberInfo = (LinearLayout) view.findViewById(R.id.memberinfo);
			
			memberName.setPadding(Services.convertdpToPxl(getActivity(), 25), 0, 0, 0);
			memberName.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.FNAME))
					+" "+cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.SNAME)));
			if (cur.isNull(cur.getColumnIndex(ContentDescriptor.Member.Cols.COLOUR)) == true) {
				memberName.setTextColor(Color.BLACK);
			} else {
				memberName.setTextColor(Color.parseColor(cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.COLOUR))));
			}
			
			
			LinearLayout.LayoutParams llparams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			TextView numberH = new TextView(getActivity());
			numberH.setPadding(Services.convertdpToPxl(getActivity(), 35), 0, 0, 0);
			numberH.setText("Member Number");
			numberH.setTextSize(13);
			numberH.setLayoutParams(llparams);
			memberDetails.addView(numberH);
			
			llparams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			TextView number = new TextView(getActivity());
			number.setPadding(Services.convertdpToPxl(getActivity(), 45), 0, 0, 0);
			number.setText(memberID);
			number.setTextSize(18);
			number.setLayoutParams(llparams);
			memberDetails.addView(number);
			
			/**
			 * membership details, for each membership.
			 */
			if (memberships.size() == 0) {
				TextView membershipH = (TextView) view.findViewById(R.id.memberinfoH);
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
				RelativeLayout msWindow = new RelativeLayout(getActivity()); //todo make this a relative layout
				llparams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				llparams.setMargins(7, 7, 7, 7);
				msWindow.setLayoutParams(llparams);
				msWindow.setBackgroundColor(getResources().getColor(color.member_grey));
				//msWindow.setOrientation(LinearLayout.VERTICAL);
				
				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				if (member.get(1) != null && member.get(1).compareTo("") != 0) {
					TextView membershipH = new TextView(getActivity());
					membershipH.setPadding(Services.convertdpToPxl(getActivity(), 35), 0, 0, 0);
					membershipH.setText("Membership");
					membershipH.setTextSize(13);
					membershipH.setId(55000);
					membershipH.setLayoutParams(params);
					//memberInfo.addView(membershipH);
					msWindow.addView(membershipH);
					
					TextView membershipT = new TextView(getActivity());
					membershipT.setPadding(Services.convertdpToPxl(getActivity(), 45), 0, 0, 0);
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
					if (date == null) {
						date = member.get(3);
					}
					TextView memberSH = new TextView(getActivity());
					memberSH.setPadding(Services.convertdpToPxl(getActivity(), 35), 0, 0, 0);
					memberSH.setText("Membership Started");
					memberSH.setTextSize(13);
					memberSH.setId(55002);
					params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
					params.addRule(RelativeLayout.BELOW, 55001);
					params.setMargins(3, 3, 3, 3);
					memberSH.setLayoutParams(params);
					//memberInfo.addView(memberSH);
					msWindow.addView(memberSH);
					
					TextView memberST = new TextView(getActivity());
					memberST.setPadding(Services.convertdpToPxl(getActivity(), 45), 0, 0, 0);
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
				
				if (member.get(4) != null && member.get(4).compareTo("null") != 0 && member.get(4).compareTo("") != 0) {
					
					TextView memberEH = new TextView(getActivity());
					memberEH.setPadding(Services.convertdpToPxl(getActivity(), 35), 0, 0, 0);
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
					if (date == null) {
						/*date = Services.dateFormat(cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.EXPIRERY)),
								"", "");*/
						date = cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.EXPIRERY));
					}
					TextView memberET = new TextView(getActivity());
					memberET.setPadding(Services.convertdpToPxl(getActivity(), 45), 0, 0, 0);
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
					TextView visitsH = new TextView(getActivity());
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
					
					TextView visitsT = new TextView(getActivity());
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
				
				memberInfo.addView(msWindow);
			}
						
			LinearLayout notesGroup = (LinearLayout) view.findViewById(R.id.membernotes);
			if (cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.NOTES)) != null) {
				if (cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.NOTES)).compareTo("null") != 0) {
					
					TextView notesT = new TextView(getActivity());
					notesT.setPadding(Services.convertdpToPxl(getActivity(), 45), 0, 0, 0);
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
				
				TextView tasks = new TextView(getActivity());
				tasks.setPadding(Services.convertdpToPxl(getActivity(), 35), 0, 0, 0);
				tasks.setText("No Pending Tasks");
				tasks.setTextSize(13);
				tasks.setLayoutParams(llparams);
				notesGroup.addView(tasks);
				
			} else {
				TextView tasksH = new TextView(getActivity());
				tasksH.setPadding(Services.convertdpToPxl(getActivity(), 35), 0, 0, 0);
				tasksH.setText("Tasks");
				tasksH.setTextSize(13);
				tasksH.setLayoutParams(llparams);
				notesGroup.addView(tasksH);
				
				
				int l;
				for(l=12;l<=14;l+=1){ //cur.getColumnIndex(ContentDescriptor.Member.Cols.TASK1)
					if (cur.getString(l) != null) {
						TextView tasks = new TextView(getActivity());
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
				
				TextView bookings = new TextView(getActivity());
				bookings.setPadding(Services.convertdpToPxl(getActivity(), 35), 0, 0, 0);
				bookings.setText("No Pending Bookings");
				bookings.setTextSize(13);
				bookings.setLayoutParams(llparams);
				notesGroup.addView(bookings);

			} else {
				TextView bookingsH = new TextView(getActivity());
				bookingsH.setPadding(Services.convertdpToPxl(getActivity(), 35), 0, 0, 0);
				bookingsH.setText("Bookings");
				bookingsH.setTextSize(13);
				bookingsH.setLayoutParams(llparams);
				notesGroup.addView(bookingsH);
				
				int l;
				for(l=15;l<=17;l+=1){ //cur.getColumnIndex(ContentDescriptor.Member.Cols.BOOK1)
					if (cur.getString(l) != null) {
						TextView bookings = new TextView(getActivity());
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
			
			if (visitDate != null && visitDate.compareTo("") != 0) { //fix this
				TextView visitTH = new TextView(getActivity());
				visitTH.setPadding(Services.convertdpToPxl(getActivity(), 35), 0, 0, 0);
				visitTH.setText("Visit Time");
				visitTH.setTextSize(13);
				visitTH.setLayoutParams(llparams);
				notesGroup.addView(visitTH);
				
				TextView visitT = new TextView(getActivity());
				visitT.setPadding(Services.convertdpToPxl(getActivity(), 45), 0, 0, 0);
				visitDate = Services.dateFormat(visitDate, "yyyy-MM-dd HH:mm", "dd MMM yy 'at' HH:mm aa");
				visitT.setText(visitDate);
				visitT.setTextSize(18);
				visitT.setLayoutParams(llparams);
				notesGroup.addView(visitT);
			}
			
			if (!cur.isNull(cur.getColumnIndex(ContentDescriptor.Member.Cols.LASTVISIT)) 
					&& cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.LASTVISIT)).compareTo("") != 0) {
				String lastVisit = Services.dateFormat(cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.LASTVISIT)), "yyyy-MM-dd HH:mm", "dd MMM yy 'at' HH:mm aa");
				
				TextView lastVH = new TextView(getActivity());
				lastVH.setPadding(Services.convertdpToPxl(getActivity(), 35), 0, 0, 0);
				lastVH.setText("Previous Visit");
				lastVH.setTextSize(13);
				lastVH.setLayoutParams(llparams);
				notesGroup.addView(lastVH);
				
				TextView lastVT = new TextView(getActivity());
				lastVT.setPadding(Services.convertdpToPxl(getActivity(), 45), 0, 0, 0);
				lastVT.setText(lastVisit);
				lastVT.setTextSize(18);
				lastVT.setLayoutParams(llparams);
				notesGroup.addView(lastVT);
			}
			
			LinearLayout addMembership = (LinearLayout) view.findViewById(R.id.button_add_membership);
			addMembership.setTag(memberID);
			addMembership.setOnClickListener(this);
			
			LinearLayout addPhoto = (LinearLayout) view.findViewById(R.id.button_add_image);
			addPhoto.setTag(memberID);
			addPhoto.setOnClickListener(this);
			
			
			LinearLayout email = (LinearLayout) view.findViewById(R.id.button_email);
			if (cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.EMAIL)) != null) {
				if (cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.EMAIL)).compareTo("null") != 0) {
					email.setOnClickListener(this);
					email.setTag(cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.EMAIL)));
				} else {
					email.setVisibility(View.GONE);
				}
			} else {
				email.setVisibility(View.GONE);
			}
			
			LinearLayout call = (LinearLayout) view.findViewById(R.id.button_call);
			LinearLayout sms = (LinearLayout) view.findViewById(R.id.button_sms);
			ArrayList<String> callTag = new ArrayList<String>();
			boolean has_number = false;
			
			if (cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.PHHOME)) != null &&
					cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.PHHOME)).compareTo("null") != 0) {
				
				callTag.add("Home: "+cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.PHHOME)));
				has_number = true;
			}
			if (cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.PHWORK)) != null &&
					cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.PHWORK)).compareTo("null") != 0) {
				
				callTag.add("Work: "+cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.PHWORK)));
				has_number = true;
			}
			if (cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.PHCELL)) != null &&
					cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.PHCELL)).compareTo("null") !=0) {
				
				sms.setTag(cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.PHCELL)));
				sms.setOnClickListener(this);
				
				callTag.add("Cell: "+cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.PHCELL)));
				has_number = true;
			} else {
				sms.setVisibility(View.GONE);
			}
			
			if (has_number) {
				call.setTag(callTag);
				call.setOnClickListener(this);
			} else {
				call.setVisibility(View.GONE);
			}
			
			LinearLayout hold = (LinearLayout) view.findViewById(R.id.button_hold);
			hold.setOnClickListener(this);

			
			cur.close();
			String[] projection = {ContentDescriptor.Image.Cols.DISPLAYVALUE};
			cur = contentResolver.query(ContentDescriptor.Image.CONTENT_URI, projection,
					null, null,ContentDescriptor.Image.Cols.DISPLAYVALUE+" DESC LIMIT 1");
			cur.moveToFirst();
			int highestImgId = 0;
			if (cur.getCount() > 0) highestImgId = cur.getInt(0)+1;
			cur.close();
			List<String> images = new ArrayList<String>();
			int i = 0; 
			for(i=0;i<highestImgId;i+=1){
				String imgDir = getActivity().getExternalFilesDir(null)+"/"+i+"_"+memberID+".jpg";
				File imgFile = new File(imgDir);
				if (imgFile.exists() == true){
					images.add(imgDir);
				}
			}
			
			mPager = (ViewPager) view.findViewById(R.id.gallery);
			
			 ImageAdapter adapter = new ImageAdapter(getActivity(), images, memberID);
			 mPager.setAdapter(adapter);
			 //Necessary or the pager will only have one extra page to show
			 // make this at least however many pages you can see
			 mPager.setOffscreenPageLimit(adapter.getCount());
			 //A little space between pages
			 mPager.setPageMargin(15);
			 mPager.setClipToPadding(false);
			 mPager.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
			 //mPager.set
			  
			ImageView moveleft, moveright;
			moveleft = (ImageView) view.findViewById(R.id.moveimageLeft);
			moveleft.setVisibility(View.GONE);
			//moveleft.setClickable(true);
			//moveleft.setOnClickListener(this);
			
			moveright = (ImageView) view.findViewById(R.id.moveimageRight);
			moveright.setVisibility(View.GONE);
			//moveright.setClickable(true);
			//moveright.setOnClickListener(this);
			
			
			
		  return view;
	}

	
	@SuppressWarnings("unchecked")
	@Override
	public void onClick(View v) {
		
		switch(v.getId()) {
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
		case (R.id.button_add_membership):{
			String memberid = null;
			if (v.getTag() instanceof String) {
				memberid = (String) v.getTag();
			}
			Intent i = new Intent(getActivity(), EmptyActivity.class);
			i.putExtra(Services.Statics.KEY, Services.Statics.FragmentType.MembershipAdd.getKey());
			i.putExtra(Services.Statics.MID, memberid);
			startActivity(i);
			break;
		}
		case (R.id.button_add_image):{
			String memberid = (String) v.getTag();
			Intent camera = new Intent(getActivity(), CameraWrapper.class);
			camera.putExtra(VisitorsViewAdapter.EXTRA_ID,memberid);
			getActivity().startActivity(camera);
			break;
		}
		case (R.id.button_email):{
			String email="mailto:"+Uri.encode(v.getTag().toString())+"?subject="+Uri.encode("Gym Details");
			Intent intent = new Intent(Intent.ACTION_SENDTO);
			intent.setData(Uri.parse(email));
			startActivity(intent);
			break;
		}
		case (R.id.button_sms):{
			String smsNo = (String) v.getTag();
			Intent smsIntent = new Intent(Intent.ACTION_VIEW);
			smsIntent.setType("vnd.android-dir/mms-sms");
			smsIntent.putExtra("address",smsNo);
			try {
				startActivity(smsIntent);
			} catch (ActivityNotFoundException e) {
				Toast.makeText(getActivity(), "Cannot send SMS from this device.", Toast.LENGTH_LONG).show();
			}
			break;
		}
		case (R.id.button_call):{
			ArrayList<String> tag = null;
			if (v.getTag() instanceof ArrayList<?>) {
				tag = (ArrayList<String>) v.getTag();
			}
			if (tag.size() == 1) {
				String ph ="tel:"+tag.get(0).substring(tag.get(0).indexOf(":")+1);
				Intent intent = new Intent(Intent.ACTION_DIAL);
				intent.setData(Uri.parse(ph));
				startActivity(intent);
			} 
			else {
				//show popup window, let user select the number to call.
				showWindow(tag);
			}
			break;
		}
		case (R.id.button_hold):{
			Intent i = new Intent(getActivity(), MembershipHold.class);
			i.putExtra(Services.Statics.KEY, memberID);
			startActivity(i);
			break;
		}
		}
	}
	
	private void showWindow(ArrayList<String> phones) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View layout = inflater.inflate(R.layout.alert_select_call, null);
		
		//do for loop, create and append radio option
		rg = (RadioGroup) layout.findViewById(R.id.alertrg);
		
		
		for (int i=0; i< phones.size(); i +=1) {
			RadioButton rb = new RadioButton(getActivity());
			rb.setText(phones.get(i));
			rb.setTag(phones.get(i).substring(phones.get(i).indexOf(":")+1));
			rg.addView(rb);
		}	
        builder.setView(layout);
        builder.setTitle("Select Number to Call");
        builder.setPositiveButton("Call", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
            		
            		String selectedNo = null;
	            	int cid = rg.getCheckedRadioButtonId();  
	            	if (cid == -1) {
	            		Toast.makeText(getActivity(), "Select a Phone Number", Toast.LENGTH_LONG).show();
	            		
	            	} else {
		            	RadioButton rb = (RadioButton) rg.findViewById(cid);
		            	selectedNo = (String) rb.getTag();
		    
		            	String ph ="tel:"+selectedNo;
						Intent intent = new Intent(Intent.ACTION_DIAL);
						intent.setData(Uri.parse(ph));
						startActivity(intent);
	            	}
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
        	@Override
            public void onClick(DialogInterface dialog, int id) {
        		dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
	}
	
	
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
			
				img.bringToFront();
				imgFile = new File(imgDir);

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
		    rootView.setTag(tagInfo);
		    	
		    
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
						
						selection = ContentDescriptor.Image.Cols.DISPLAYVALUE+" = "+rowid
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
