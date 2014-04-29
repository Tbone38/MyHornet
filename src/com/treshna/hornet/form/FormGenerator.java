package com.treshna.hornet.form;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.treshna.hornet.R;

public class FormGenerator {
	private ContentValues mIdMap;
	private LinearLayout mView;
	private View theView;
	private LayoutInflater mInflater;
	private Context mContext;
	
	public interface FormBuilder 
	{
		View generateForm();
	}
	
	public FormGenerator(LayoutInflater layoutInflater, Context context) {
		mInflater = layoutInflater;
		
		mIdMap = new ContentValues();
		theView = mInflater.inflate(R.layout.empty_linear_layout, null);
		mView = (LinearLayout) theView.findViewById(R.id.empty_linear_layout);
		mContext = context;
	}
	
	public ContentValues getIds() {
		return mIdMap;
	}
	
	public View getForm() {
		return theView;
	}
	
	public void addEditText(String label_text, int id, String key, String default_text) {
		RelativeLayout editLayout = (RelativeLayout) mInflater.inflate(R.layout.edit_text, null);
		editLayout.setId(id);
		
		TextView label = (TextView) editLayout.findViewById(R.id.label);
		label.setText(label_text);
		
		if (default_text != null) {
			EditText editText = (EditText) editLayout.findViewById(R.id.edit_text);
			editText.setText(default_text);
		}
		
		mIdMap.put(key, id);
		mView.addView(editLayout);
	}
	
	public String getEditText(int id) {
		String result = null;
		RelativeLayout resource_name = (RelativeLayout) mView.findViewById(id);
		EditText name_edit = (EditText) resource_name.findViewById(R.id.edit_text);
		if (name_edit.getText().toString().isEmpty() || name_edit.getText().toString().replace(" ", "").isEmpty()) {
			TextView label = (TextView) resource_name.findViewById(R.id.label);
			label.setTextColor(Color.RED);
		} else {
			TextView label = (TextView) resource_name.findViewById(R.id.label);
			label.setTextColor(Color.BLACK);
			result = name_edit.getText().toString();
		}
		return result;
	}
	
	public void addSpinner(String label_text, int id, String key, ArrayList<String> contents) {
		RelativeLayout spinnerLayout = (RelativeLayout) mInflater.inflate(R.layout.spinner, null);
		spinnerLayout.setId(id);
		
		TextView label = (TextView) spinnerLayout.findViewById(R.id.label);
		label.setText(label_text);
		
		if (contents != null) {
			Spinner spinner = (Spinner) spinnerLayout.findViewById(R.id.spinner);
			
			ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(mContext,
					android.R.layout.simple_spinner_item, contents);
				dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				spinner.setAdapter(dataAdapter);
		}
		
		mIdMap.put(key, id);
		mView.addView(spinnerLayout);
	}
	
	public String getSpinnerItem(int id) {
		String result = null;
		RelativeLayout spinnerLayout = (RelativeLayout) mView.findViewById(id);
		Spinner spinner = (Spinner) spinnerLayout.findViewById(R.id.spinner);
		result = spinner.getSelectedItem().toString();
		
		return result;
	}
	
	public int getSpinnerPosition(int id) {
		RelativeLayout spinnerLayout = (RelativeLayout) mView.findViewById(id);
		Spinner spinner = (Spinner) spinnerLayout.findViewById(R.id.spinner);
		
		return spinner.getSelectedItemPosition();
	}
	
	public void addClickableText(String label_text, int layoutid, String key, String default_text, int clickid, OnClickListener clicklistener) {
		RelativeLayout clickLayout = (RelativeLayout) mInflater.inflate(R.layout.clickable_text, null);
		clickLayout.setId(layoutid);
		
		TextView label = (TextView) clickLayout.findViewById(R.id.label);
		label.setText(label_text);
		
		TextView click = (TextView) clickLayout.findViewById(R.id.clickable_text);
		click.setText(default_text);
		click.setId(clickid);
		if (clicklistener != null) {
			click.setOnClickListener(clicklistener);
		}
		
		mIdMap.put(key, layoutid);
		mView.addView(clickLayout);
	}
	
	public String getClickText(int id, String default_text) {
		String result = null;
		
		RelativeLayout clickLayout = (RelativeLayout) mView.findViewById(id);
		
		TextView click = (TextView) clickLayout.findViewById(R.id.clickable_text);
		if (click.getText().toString().compareTo(default_text) == 0 || 
				click.getText().toString().replace(" ", "").isEmpty()) {
			TextView label = (TextView) clickLayout.findViewById(R.id.label);
			label.setTextColor(Color.RED);
		} else {
			TextView label = (TextView) clickLayout.findViewById(R.id.label);
			label.setTextColor(Color.BLACK);
			result = click.getText().toString();
		}
		
		return result;
	}
	
	public void addHeading(String heading_text) {
		RelativeLayout textLayout = (RelativeLayout) mInflater.inflate(R.layout.clickable_text, null);
		
		TextView label = (TextView) textLayout.findViewById(R.id.label);
		label.setText(heading_text);
		label.setTextSize(24);
		label.setGravity(Gravity.CENTER_HORIZONTAL);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		label.setLayoutParams(params);
		
		TextView click = (TextView) textLayout.findViewById(R.id.clickable_text);
		click.setVisibility(View.GONE);
		
		mView.addView(textLayout);
	}
	
	public void addText(String label_text, int id, String key) {
		RelativeLayout textLayout = (RelativeLayout) mInflater.inflate(R.layout.clickable_text, null);
		textLayout.setId(id);
		
		TextView label = (TextView) textLayout.findViewById(R.id.label);
		label.setText(label_text);
		
		TextView click = (TextView) textLayout.findViewById(R.id.clickable_text);
		click.setVisibility(View.GONE);
		
		mIdMap.put(key, id);
		mView.addView(textLayout);
	}
	
	public void addButtonRow(String positive_text, String negative_text, int positive_id, int negative_id, OnClickListener clicklistener) {
		LinearLayout buttonRow = (LinearLayout) mInflater.inflate(R.layout.buttons_row, null);
		
		TextView positive = (TextView) buttonRow.findViewById(R.id.button_positive);
		positive.setText(positive_text);
		positive.setId(positive_id);
		positive.setOnClickListener(clicklistener);
		
		TextView negative = (TextView) buttonRow.findViewById(R.id.button_negative);
		negative.setText(negative_text);
		negative.setId(negative_id);
		negative.setOnClickListener(clicklistener);
		
		mView.addView(buttonRow);
	}
	
	public void addCheckBox(String label_text, int id, String key, boolean is_checked) {
		CheckBox check = (CheckBox) mInflater.inflate(R.layout.checkbox, mView, false);
		check.setText(label_text);
		check.setId(id);
		check.setChecked(is_checked);
		
		mIdMap.put(key, id);
		mView.addView(check);
	}
	
	public boolean getCheckBox(int id) {
		CheckBox check = (CheckBox) mView.findViewById(id);
		return check.isChecked();
	}
}