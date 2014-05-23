package com.lbconsulting.alist.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lbconsulting.alist.R;
import com.lbconsulting.alist.database.LocationsTable;
import com.lbconsulting.alist.utilities.MyLog;

public class LocationsSpinnerCursorAdapter extends CursorAdapter {

	public LocationsSpinnerCursorAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);
		MyLog.i("LocationsSpinnerCursorAdapter", "LocationsSpinnerCursorAdapter constructor.");
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {

		switch (view.getId()) {
			case R.id.llGroupRow:
				TextView tvGroupRow = (TextView) view.findViewById(R.id.tvGroupRow);
				if (tvGroupRow != null) {
					tvGroupRow
							.setText(cursor.getString(cursor.getColumnIndexOrThrow(LocationsTable.COL_LOCATION_NAME)));
				}
				break;
			case R.id.llGroupRowDropdown:
				TextView tvGroupRowDropdown = (TextView) view.findViewById(R.id.tvGroupRowDropdown);
				if (tvGroupRowDropdown != null) {
					tvGroupRowDropdown.setText(cursor.getString(cursor
							.getColumnIndexOrThrow(LocationsTable.COL_LOCATION_NAME)));
				}
				break;
			default:
				break;
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View v = null;
		LayoutInflater inflater = LayoutInflater.from(context);
		v = inflater.inflate(R.layout.row_groups_spinner, parent, false);
		bindView(v, context, cursor);
		return v;
	}

	@Override
	public View newDropDownView(Context context, Cursor cursor, ViewGroup parent) {
		View v = null;
		LayoutInflater inflater = LayoutInflater.from(context);
		v = inflater.inflate(R.layout.row_groups_spinner_dropdown, parent, false);
		bindView(v, context, cursor);
		return v;
	}

}
