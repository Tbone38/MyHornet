package com.treshna.hornet.form;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.treshna.hornet.form.FormGenerator.FormBuilder;

/** TODO: we need to get an array or items we want generated. we also need a title for the form, and the name of a function to run **/
public class FormFragment extends Fragment {
	
	private Cursor cur;
	private ContentResolver contentResolver;
	private View page;
	private FormBuilder builder;
	
	String statusMessage;
	
	@Override
	 public View onCreateView(LayoutInflater inflater, ViewGroup container,
			 Bundle savedInstanceState) {
		 // Inflate the layout for this fragment
		 contentResolver = getActivity().getContentResolver();
		 
		 builder = new ResourceBuilder(getActivity(), -1);
		 page = builder.generateForm();
		 
		 return page;
	}	 
	
}
