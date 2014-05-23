package com.lbconsulting.alist.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.lbconsulting.alist.R;
import com.lbconsulting.alist.database.BridgeTable;
import com.lbconsulting.alist.database.GroupsTable;
import com.lbconsulting.alist.database.LocationsTable;
import com.lbconsulting.alist.utilities.AListUtilities;
import com.lbconsulting.alist.utilities.MyLog;

public class ManageLocationsCursorAdaptor extends CursorAdapter {

	public ManageLocationsCursorAdaptor(Context context, Cursor c, int flags) {
		super(context, c, flags);
		MyLog.i("ManageLocationsCursorAdaptor", "ManageLocationsCursorAdaptor constructor.");
	}

	private boolean ShowLocationSeparator(TextView tv, Cursor listCursor) {
		boolean result = false;
		// SELECT tblGroups._id, tblGroups.groupName,tblBridge.locationID, tblLocations.locationName
		long currentLocationID = listCursor.getLong(listCursor.getColumnIndexOrThrow(BridgeTable.COL_LOCATION_ID));
		long previousLocationID = -1;
		if (listCursor.moveToPrevious()) {
			previousLocationID = listCursor.getLong(listCursor.getColumnIndexOrThrow(BridgeTable.COL_LOCATION_ID));
			listCursor.moveToNext();
			if (currentLocationID == previousLocationID) {
				tv.setVisibility(View.GONE);
				result = false;
			} else {
				tv.setVisibility(View.VISIBLE);
				result = true;
			}
		} else {
			tv.setVisibility(View.VISIBLE);
			listCursor.moveToFirst();
			result = true;
		}
		return result;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		if (cursor != null) {

			TextView tvListItemSeparator = (TextView) view.findViewById(R.id.tvListItemSeparator);
			if (tvListItemSeparator != null) {
				if (ShowLocationSeparator(tvListItemSeparator, cursor)) {
					try {
						tvListItemSeparator.setText(
								cursor.getString(cursor.getColumnIndexOrThrow(LocationsTable.COL_LOCATION_NAME)));
					} catch (IllegalArgumentException e) {
						MyLog.e("IllegalArgumentException error in ManageLocationsCursorAdaptor:",
								e.toString());
					}
				}

				int isCheckedValue = cursor.getInt(cursor.getColumnIndexOrThrow(GroupsTable.COL_CHECKED));
				boolean isChecked = AListUtilities.intToBoolean(isCheckedValue);
				CheckBox ckBox = (CheckBox) view.findViewById(R.id.ckBox);
				if (ckBox != null) {
					ckBox.setChecked(isChecked);
				}

				TextView tvGroupName = (TextView) view.findViewById(R.id.tvGroupName);
				if (tvGroupName != null) {
					tvGroupName.setText(cursor.getString(cursor.getColumnIndexOrThrow(GroupsTable.COL_GROUP_NAME)));
				}
			}
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.row_manage_locations, parent, false);
		return view;
	}

}
