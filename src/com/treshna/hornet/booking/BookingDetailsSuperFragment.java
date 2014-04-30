package com.treshna.hornet.booking;

import java.util.ArrayList;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.database.Cursor;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.treshna.hornet.MainActivity.TagFoundListener;
import com.treshna.hornet.R;
import com.treshna.hornet.classes.ClassDetailsSuperFragment;
import com.treshna.hornet.member.MembersFindFragment.OnMemberSelectListener;
import com.treshna.hornet.services.Services;
import com.treshna.hornet.sqlite.ContentDescriptor;


public class BookingDetailsSuperFragment extends Fragment implements OnMemberSelectListener, TagFoundListener {
	
	private String bookingID;
	private String starttime;
	private String selectedID;
	private String selectedMS;
	private String selectedMSID;
	
	FragmentManager frm;
	RadioGroup rg;
	ContentResolver contentResolver;
	
	private TagFoundListener tagFoundListener;
	int classid = 0;
	private View view;
	private LayoutInflater mInflater;
	
	 @Override
	 public View onCreateView(LayoutInflater inflater, ViewGroup container,
			 Bundle savedInstanceState) {
		 	//Intent intent = getIntent();
			Services.setContext(getActivity());
			ArrayList<String> tagInfo = this.getArguments().getStringArrayList(Services.Statics.KEY);
			bookingID = tagInfo.get(0);
			
			contentResolver = getActivity().getContentResolver();
			frm = this.getChildFragmentManager();
			FragmentTransaction ft = frm.beginTransaction();
		
			//setContentView(R.layout.empty_activity);
			mInflater = inflater;
			view = mInflater.inflate(R.layout.activity_empty, null);
			if (savedInstanceState == null) {
				if (Integer.parseInt(bookingID) > 0) {
					Cursor cur;
					
					bookingID = tagInfo.get(1);
					
					cur = contentResolver.query(ContentDescriptor.Booking.CONTENT_URI,null, 
							ContentDescriptor.Booking.Cols.BID+" = "+bookingID, null, null);
					if (cur.moveToFirst()) {
						classid = cur.getInt(cur.getColumnIndex(ContentDescriptor.Booking.Cols.CLASSID));
						System.out.print("\n\n** CLASS ID:"+classid+" **\n\n");
					}
					
					if (classid > 0) {
						//it's a class, show the class-booking page instead.
						//ClassDetailsFragment f;
						ClassDetailsSuperFragment f;
						Bundle bdl;
						
						//f = new ClassDetailsFragment();
						f = new ClassDetailsSuperFragment();
						tagFoundListener = (TagFoundListener) f;
						bdl = new Bundle(1);
						bdl.putString(Services.Statics.KEY, bookingID);
						f.setArguments(bdl);
						ft.add(R.id.empty_layout, f);
					} else {
						bookingID = tagInfo.get(1);
						BookingDetailsFragment f = new BookingDetailsFragment();
						Bundle bdl = new Bundle(1);
			            bdl.putString(Services.Statics.KEY, bookingID);
			            f.setArguments(bdl);
						ft.add(R.id.empty_layout, f);
					}
				} else {
					//add Booking
					BookingAddFragment f = (BookingAddFragment) getChildFragmentManager().findFragmentByTag("AddBooking");
					if (f == null) {
						f = new BookingAddFragment();
						starttime = tagInfo.get(1);
						Bundle bdl = new Bundle(2);
			            bdl.putString(Services.Statics.KEY, starttime);
			            bdl.putString(Services.Statics.DATE, tagInfo.get(2));
			            f.setArguments(bdl);
						ft.add(R.id.empty_layout,f, "AddBooking");
						ft.addToBackStack(null);
					}
				}
				ft.commit();
			}
			return view;
	 }
	
	

	@Override
	public void onMemberSelect(String id) {

		selectedID = id;
		Cursor cur;
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		
		View layout = mInflater.inflate(R.layout.alert_select_call, null);
		
		//do for loop, create and append radio option
		rg = (RadioGroup) layout.findViewById(R.id.alertrg);
		
		cur = contentResolver.query(ContentDescriptor.Membership.CONTENT_URI, null, ContentDescriptor.Membership.Cols.MID+" = ? AND "
				+ContentDescriptor.Membership.Cols.HISTORY+" = 'f'",
				new String[] {id}, null);
		
		if (cur.getCount() == 1) { //theirs only 1 membership, just use that.
			cur.moveToFirst();
			selectedMSID = cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.MSID));
			cur.close();
			
			cur = contentResolver.query(ContentDescriptor.Member.CONTENT_URI,new String[] {ContentDescriptor.Member.Cols.FNAME, 
					ContentDescriptor.Member.Cols.SNAME}, ContentDescriptor.Member.Cols.MID+" = ?",new String[] {id}, null);
			cur.moveToFirst();

			String fname = cur.getString(0), sname = cur.getString(1);
			cur.close();
			
			frm = getActivity().getSupportFragmentManager();
    		BookingAddFragment f = (BookingAddFragment) getChildFragmentManager().findFragmentByTag("AddBooking");
    		f.setName(fname, sname);
    		f.setMembership(selectedMSID);
    		return;
		}
		
		while (cur.moveToNext()) {
			if (cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.PNAME)) != null 
					&& cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.PNAME)).compareTo("") !=0) {
				RadioButton rb = new RadioButton(getActivity());
				rb.setText(cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.PNAME)));
				rb.setTag(cur.getString(cur.getColumnIndex(ContentDescriptor.Membership.Cols.MSID)));
				rg.addView(rb);
			}
		}	
		cur.close();
        builder.setView(layout);
        builder.setTitle("Select Membership for Booking");
        builder.setPositiveButton("Select", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
            	
            	if (rg.getChildCount()<=0) {
            		//no memberships found for this member, ignore and continue?
            		selectedMSID = null;
            	} else if (rg.getChildCount() > 0) {
	            	
	            	int cid = rg.getCheckedRadioButtonId();  
	            	if (cid == -1) {
	            		selectedMSID = null;
	            	} else {
		            	RadioButton rb = (RadioButton) rg.findViewById(cid);
		            	selectedMS = (String) rb.getText();
		            	selectedMSID = (String) rb.getTag();
		            	System.out.print("\n\nSelected Membership:"+selectedMS+" with ID:"+selectedMSID);
	            	}
            	}
	            /**TODO: Rabbit Hole: fix this by changing the member name handling as well.
	             * 
	             */
            	Cursor cur;
            	cur = contentResolver.query(ContentDescriptor.Member.CONTENT_URI, null, "m."+ContentDescriptor.Member.Cols.MID+" = ?", 
        				new String[] {selectedID}, null);
        		if (cur.getCount() <= 0) {
        			// what should I do?
        		}
        		String fname = null;
        		String sname = null;
        		cur.moveToFirst();
        		fname = cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.FNAME));
        		sname = cur.getString(cur.getColumnIndex(ContentDescriptor.Member.Cols.SNAME));
        		
        		//frm = getChildFragmentManager();
        		//BookingAddFragment f = (BookingAddFragment)frm.findFragmentByTag("AddBooking");
        		frm = getActivity().getSupportFragmentManager();
        		
        		BookingAddFragment f = (BookingAddFragment) getChildFragmentManager().findFragmentByTag("AddBooking");
        		f.setName(fname, sname);
        		f.setMembership(selectedMSID);
        		frm.popBackStackImmediate();
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
	public final String getID(Tag tag){
    	StringBuilder sb = new StringBuilder();
       	for (byte b : tag.getId()) {
       		sb.append(String.format("%02X", b));
       	}
       	System.out.println("**TAG ID: "+sb.toString());
       	String cardID = null;
       	if(tag.getId().length == 4) {
       		String temp = sb.toString().substring(0, sb.toString().length() - 2).toLowerCase(Locale.US);
    	   	cardID = "Mx"+temp;
       	} else if(tag.getId().length == 7){
       		String temp = sb.toString().toLowerCase(Locale.US);
       		cardID = "Mv"+temp;
       	}
       	System.out.println(cardID);
    	return cardID;
    }

	@Override
	public boolean onNewTag(String serial) {
		Log.d("BOOKINGDETAILSSUPERFRAGMENT", "BOOKINGS DETAILS SUPER FRAG GOT NEW TAG");
		if (tagFoundListener != null) {
			return tagFoundListener.onNewTag(serial);
		} else {
			return false;
		}
	}
}
