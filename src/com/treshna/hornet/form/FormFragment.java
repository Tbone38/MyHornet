package com.treshna.hornet.form;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.treshna.hornet.form.FormGenerator.FormBuilder;
import com.treshna.hornet.services.Services;

/** TODO: we need to get an array or items we want generated. we also need a title for the form, and the name of a function to run **/
public class FormFragment extends Fragment {
	
	public static final int RESOURCE = 1;
	public static final int PROGRAMMEGROUP = 2;
	public static final int BOOKINGTYPE = 3;
	public static final int DOOR = 4;
	
	private View page;
	private FormBuilder builder;
	int buildertype;
	int id;
	String statusMessage;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		buildertype = this.getArguments().getInt(Services.Statics.KEY);
		if (this.getArguments().containsKey(Services.Statics.ID_KEY)) {
			id = this.getArguments().getInt(Services.Statics.ID_KEY);
		} else {
			id = -1;
		}
		switch (buildertype){
		case (RESOURCE):{
			builder = new ResourceBuilder(getActivity(), id);
			break;
		}
		case (PROGRAMMEGROUP):{
			builder = new ProgrammeGroupBuilder(getActivity(), id);
			break;
		}
		case (BOOKINGTYPE):{
			builder = new BookingtypeBuilder(getActivity(), id);
			break;
		}
		case (DOOR):{
			builder = new DoorBuilder(getActivity(), id);
			break;
		}
		}
		
		page = builder.generateForm();
		return page;
	}	 
	
}
