package com.lbconsulting.alist.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lbconsulting.alist.R;
import com.lbconsulting.alist.classes.ListSettings;
import com.lbconsulting.alist.database.GroupsTable;
import com.lbconsulting.alist.database.ItemsTable;
import com.lbconsulting.alist.utilities.MyLog;

public class MasterListCursorAdaptor extends CursorAdapter {

	Context mAdaptorContext;
	static ListSettings mListSettings;

	public MasterListCursorAdaptor(Context context, Cursor c, int flags, ListSettings listSettings) {
		super(context, c, flags);
		this.mAdaptorContext = context;
		this.mListSettings = listSettings;
		MyLog.i("MasterListCursorAdaptor", "MasterListCursorAdaptor constructor.");
	}

	public static void RefreshListSettings() {
		mListSettings.RefreshListSettings();
	}

	private boolean ShowSeparator(TextView tv, Cursor listCursor) {
		boolean result = false;
		long currentGroupID = listCursor.getLong(listCursor.getColumnIndexOrThrow(ItemsTable.COL_GROUP_ID));
		long previousGroupID = -1;
		if (listCursor.moveToPrevious()) {
			previousGroupID = listCursor.getLong(listCursor.getColumnIndexOrThrow(ItemsTable.COL_GROUP_ID));
			listCursor.moveToNext();
			if (currentGroupID == previousGroupID) {
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
				if (mListSettings.getShowGroupsInMasterListFragment()) {
					if (ShowSeparator(tvListItemSeparator, cursor)) {
						try {
							tvListItemSeparator.setText(cursor.getString(cursor
									.getColumnIndexOrThrow(GroupsTable.COL_GROUP_NAME)));
						} catch (IllegalArgumentException e) {
							MyLog.e("IllegalArgumentException error in ItemsCursorAdaptor:bindView ", e.toString());
						}
					}
				} else {
					tvListItemSeparator.setVisibility(View.GONE);
				}
			}

			int isSelected = cursor.getInt(cursor.getColumnIndex(ItemsTable.COL_SELECTED));
			TextView tvItemName = (TextView) view.findViewById(R.id.tvItemName);
			if (tvItemName != null) {
				tvItemName.setText(cursor.getString(cursor.getColumnIndexOrThrow(ItemsTable.COL_ITEM_NAME)));

				if (isSelected > 0) {
					// item has been Selected
					tvItemName.setTypeface(null, Typeface.ITALIC);
					tvItemName.setTextColor(this.mListSettings.getMasterListItemSelectedTextColor());
				} else {
					// item is NOT selected
					tvItemName.setTypeface(null, Typeface.NORMAL);
					tvItemName.setTextColor(this.mListSettings.getMasterListItemNormalTextColor());
				}
			}

			TextView tvItemNote = (TextView) view.findViewById(R.id.tvItemNote);
			if (tvItemNote != null) {
				// if a note exists ... then show it
				String note = cursor.getString(cursor.getColumnIndexOrThrow(ItemsTable.COL_ITEM_NOTE));
				if (note != null && !note.isEmpty()) {
					StringBuilder sb = new StringBuilder();
					sb.append("(");
					sb.append(cursor.getString(cursor.getColumnIndexOrThrow(ItemsTable.COL_ITEM_NOTE)));
					sb.append(")");
					tvItemNote.setText(sb.toString());
					if (isSelected > 0) {
						// item has been selected
						tvItemNote.setTypeface(null, Typeface.ITALIC);
						tvItemNote.setTextColor(this.mListSettings.getMasterListItemSelectedTextColor());

					} else {
						// item is NOT selected
						tvItemNote.setTypeface(null, Typeface.NORMAL);
						tvItemNote.setTextColor(this.mListSettings.getMasterListItemNormalTextColor());

					}
					tvItemNote.setVisibility(View.VISIBLE);
				} else {
					// no note exists ...
					tvItemNote.setVisibility(View.GONE);
				}
			}
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.row_master_list_item, parent, false);
		return view;
	}

}
