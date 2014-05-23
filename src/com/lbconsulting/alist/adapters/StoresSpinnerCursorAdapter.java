package com.lbconsulting.alist.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lbconsulting.alist.R;
import com.lbconsulting.alist.classes.ListSettings;
import com.lbconsulting.alist.database.StoresTable;
import com.lbconsulting.alist.utilities.MyLog;

public class StoresSpinnerCursorAdapter extends CursorAdapter {

	ListSettings mListSettings;

	public StoresSpinnerCursorAdapter(Context context, Cursor c, int flags, ListSettings listSettings) {
		super(context, c, flags);
		this.mListSettings = listSettings;
		MyLog.i("StoresSpinnerCursorAdapter", "StoresSpinnerCursorAdapter constructor.");
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {

		switch (view.getId()) {
			case R.id.rowLinearLayout:
				TextView tvStoreRow = (TextView) view.findViewById(R.id.rowTextView);
				if (tvStoreRow != null) {
					tvStoreRow.setText(getDisplayName(cursor));
					tvStoreRow.setTextColor(this.mListSettings.getTitleTextColor());
				}
				break;
			case R.id.rowLinearLayoutDropdown:
				TextView tvStoreRowDropdown = (TextView) view.findViewById(R.id.rowTextViewDropdown);
				if (tvStoreRowDropdown != null) {
					tvStoreRowDropdown.setText(getDisplayName(cursor));
					tvStoreRowDropdown.setTextColor(this.mListSettings.getTitleTextColor());
				}
				break;
			default:
				break;
		}
	}

	private CharSequence getDisplayName(Cursor cursor) {
		String displayName = "";
		if (cursor != null) {
			StringBuilder sb = new StringBuilder();
			sb.append(cursor.getString(cursor.getColumnIndexOrThrow(StoresTable.COL_STORE_NAME)));
			String city = cursor.getString(cursor.getColumnIndexOrThrow(StoresTable.COL_CITY));
			if (city != null && !city.isEmpty()) {
				sb.append(", ");
				sb.append(city);
			}
			String state = cursor.getString(cursor.getColumnIndexOrThrow(StoresTable.COL_STATE));
			if (state != null && !state.isEmpty()) {
				sb.append(", ");
				sb.append(state);
			}

			displayName = sb.toString();
		}
		return displayName;
	}

	/*	@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View v = null;
			LayoutInflater inflater = LayoutInflater.from(context);
			v = inflater.inflate(R.layout.row_stores_spinner, parent, false);
			v.setBackgroundColor(this.mListSettings.getTitleBackgroundColor());
			bindView(v, context, cursor);
			return v;
		}*/

	@Override
	public View newDropDownView(Context context, Cursor cursor, ViewGroup parent) {
		View v = null;
		LayoutInflater inflater = LayoutInflater.from(context);
		v = inflater.inflate(R.layout.row_spinner_dropdown, parent, false);
		v.setBackgroundColor(this.mListSettings.getTitleBackgroundColor());
		bindView(v, context, cursor);
		return v;
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View v = null;
		LayoutInflater inflater = LayoutInflater.from(context);
		v = inflater.inflate(R.layout.row_spinner, parent, false);
		v.setBackgroundColor(this.mListSettings.getTitleBackgroundColor());
		bindView(v, context, cursor);
		return v;
	}

}
