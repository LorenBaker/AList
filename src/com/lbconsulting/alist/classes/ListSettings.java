package com.lbconsulting.alist.classes;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;

import com.lbconsulting.alist.R;
import com.lbconsulting.alist.database.ListsTable;
import com.lbconsulting.alist.utilities.AListUtilities;

public class ListSettings {

	private Context mContext;
	private long mListID;
	private Cursor mListCursor;

	public ListSettings(Context context, long listID) {
		if (listID > 0) {
			this.mContext = context;
			this.mListID = listID;
			this.RefreshListSettings();
			if (this.getTitleBackgroundColor() == -1) {
				SetDefaultColors();
				ListsTable.setListPreferencesDefaults(mContext, listID);
				this.RefreshListSettings();
			}
		}
	}

	public void RefreshListSettings() {
		if (mListID > 0) {
			this.mListCursor = ListsTable.getList(mContext, mListID);
			if (mListCursor != null) {
				this.mListCursor.moveToFirst();
			}
		}
	}

	private void SetDefaultColors() {
		Resources res = mContext.getResources();
		if (mListID > 0) {
			// select the new list title's colors
			ContentValues newDefaultValues = new ContentValues();
			int selector = (int) mListID % 6;
			switch (selector) {
				case 0:
					// Sandy Stone
					newDefaultValues.put(ListsTable.COL_TITLE_BACKGROUND_COLOR,
							res.getColor(R.color.preset0_title_background));
					newDefaultValues.put(ListsTable.COL_TITLE_TEXT_COLOR, res.getColor(R.color.preset0_title_text));

					newDefaultValues.put(ListsTable.COL_LIST_BACKGROUND_COLOR,
							res.getColor(R.color.preset0_list_background));
					newDefaultValues.put(ListsTable.COL_ITEM_NORMAL_TEXT_COLOR,
							res.getColor(R.color.preset0_list_normal_text));
					newDefaultValues.put(ListsTable.COL_ITEM_STRIKEOUT_TEXT_COLOR,
							res.getColor(R.color.preset0_list_strikeout_text));

					newDefaultValues
							.put(ListsTable.COL_MASTER_LIST_BACKGROUND_COLOR,
									res.getColor(R.color.preset0_list_background));
					newDefaultValues.put(ListsTable.COL_MASTER_LIST_ITEM_NORMAL_TEXT_COLOR,
							res.getColor(R.color.preset0_list_strikeout_text));
					newDefaultValues.put(ListsTable.COL_MASTER_LIST_ITEM_SELECTED_TEXT_COLOR,
							res.getColor(R.color.preset0_list_normal_text));

					newDefaultValues.put(ListsTable.COL_SEPARATOR_BACKGROUND_COLOR,
							res.getColor(R.color.preset0_separator_background));
					newDefaultValues
							.put(ListsTable.COL_SEPARATOR_TEXT_COLOR, res.getColor(R.color.preset0_separator_text));

					ListsTable.UpdateListsTableFieldValues(mContext, mListID, newDefaultValues);

					break;

				case 1:
					// Blue Sky
					newDefaultValues.put(ListsTable.COL_TITLE_BACKGROUND_COLOR,
							res.getColor(R.color.preset1_title_background));
					newDefaultValues.put(ListsTable.COL_TITLE_TEXT_COLOR, res.getColor(R.color.preset1_title_text));

					newDefaultValues.put(ListsTable.COL_LIST_BACKGROUND_COLOR,
							res.getColor(R.color.preset1_list_background));
					newDefaultValues.put(ListsTable.COL_ITEM_NORMAL_TEXT_COLOR,
							res.getColor(R.color.preset1_list_normal_text));
					newDefaultValues.put(ListsTable.COL_ITEM_STRIKEOUT_TEXT_COLOR,
							res.getColor(R.color.preset1_list_strikeout_text));

					newDefaultValues
							.put(ListsTable.COL_MASTER_LIST_BACKGROUND_COLOR,
									res.getColor(R.color.preset1_list_background));
					newDefaultValues.put(ListsTable.COL_MASTER_LIST_ITEM_NORMAL_TEXT_COLOR,
							res.getColor(R.color.preset1_list_strikeout_text));
					newDefaultValues.put(ListsTable.COL_MASTER_LIST_ITEM_SELECTED_TEXT_COLOR,
							res.getColor(R.color.preset1_list_normal_text));

					newDefaultValues.put(ListsTable.COL_SEPARATOR_BACKGROUND_COLOR,
							res.getColor(R.color.preset1_separator_background));
					newDefaultValues
							.put(ListsTable.COL_SEPARATOR_TEXT_COLOR, res.getColor(R.color.preset1_separator_text));

					ListsTable.UpdateListsTableFieldValues(mContext, mListID, newDefaultValues);
					break;

				case 2:
					// This Green
					// Master Selected == Item Normal
					newDefaultValues.put(ListsTable.COL_TITLE_BACKGROUND_COLOR,
							res.getColor(R.color.preset2_title_background));
					newDefaultValues.put(ListsTable.COL_TITLE_TEXT_COLOR, res.getColor(R.color.preset2_title_text));

					newDefaultValues.put(ListsTable.COL_LIST_BACKGROUND_COLOR,
							res.getColor(R.color.preset2_list_background));
					newDefaultValues.put(ListsTable.COL_ITEM_NORMAL_TEXT_COLOR,
							res.getColor(R.color.preset2_list_normal_text));
					newDefaultValues.put(ListsTable.COL_ITEM_STRIKEOUT_TEXT_COLOR,
							res.getColor(R.color.preset2_list_strikeout_text));

					newDefaultValues
							.put(ListsTable.COL_MASTER_LIST_BACKGROUND_COLOR,
									res.getColor(R.color.preset2_list_background));
					newDefaultValues.put(ListsTable.COL_MASTER_LIST_ITEM_NORMAL_TEXT_COLOR,
							res.getColor(R.color.preset2_list_strikeout_text));
					newDefaultValues.put(ListsTable.COL_MASTER_LIST_ITEM_SELECTED_TEXT_COLOR,
							res.getColor(R.color.preset2_list_normal_text));

					newDefaultValues.put(ListsTable.COL_SEPARATOR_BACKGROUND_COLOR,
							res.getColor(R.color.preset2_separator_background));
					newDefaultValues
							.put(ListsTable.COL_SEPARATOR_TEXT_COLOR, res.getColor(R.color.preset2_separator_text));

					ListsTable.UpdateListsTableFieldValues(mContext, mListID, newDefaultValues);
					break;

				case 3:
					// Vintage Ralph Lauren
					newDefaultValues.put(ListsTable.COL_TITLE_BACKGROUND_COLOR,
							res.getColor(R.color.preset3_title_background));
					newDefaultValues.put(ListsTable.COL_TITLE_TEXT_COLOR, res.getColor(R.color.preset3_title_text));

					newDefaultValues.put(ListsTable.COL_LIST_BACKGROUND_COLOR,
							res.getColor(R.color.preset3_list_background));
					newDefaultValues.put(ListsTable.COL_ITEM_NORMAL_TEXT_COLOR,
							res.getColor(R.color.preset3_list_normal_text));
					newDefaultValues.put(ListsTable.COL_ITEM_STRIKEOUT_TEXT_COLOR,
							res.getColor(R.color.preset3_list_strikeout_text));

					newDefaultValues
							.put(ListsTable.COL_MASTER_LIST_BACKGROUND_COLOR,
									res.getColor(R.color.preset3_list_background));
					newDefaultValues.put(ListsTable.COL_MASTER_LIST_ITEM_NORMAL_TEXT_COLOR,
							res.getColor(R.color.preset3_list_strikeout_text));
					newDefaultValues.put(ListsTable.COL_MASTER_LIST_ITEM_SELECTED_TEXT_COLOR,
							res.getColor(R.color.preset3_list_normal_text));

					newDefaultValues.put(ListsTable.COL_SEPARATOR_BACKGROUND_COLOR,
							res.getColor(R.color.preset3_separator_background));
					newDefaultValues
							.put(ListsTable.COL_SEPARATOR_TEXT_COLOR, res.getColor(R.color.preset3_separator_text));

					ListsTable.UpdateListsTableFieldValues(mContext, mListID, newDefaultValues);

					break;

				case 4:
					// Five Oranges
					newDefaultValues.put(ListsTable.COL_TITLE_BACKGROUND_COLOR,
							res.getColor(R.color.preset4_title_background));
					newDefaultValues.put(ListsTable.COL_TITLE_TEXT_COLOR, res.getColor(R.color.preset4_title_text));

					newDefaultValues.put(ListsTable.COL_LIST_BACKGROUND_COLOR,
							res.getColor(R.color.preset4_list_background));
					newDefaultValues.put(ListsTable.COL_ITEM_NORMAL_TEXT_COLOR,
							res.getColor(R.color.preset4_list_normal_text));
					newDefaultValues.put(ListsTable.COL_ITEM_STRIKEOUT_TEXT_COLOR,
							res.getColor(R.color.preset4_list_strikeout_text));

					newDefaultValues
							.put(ListsTable.COL_MASTER_LIST_BACKGROUND_COLOR,
									res.getColor(R.color.preset4_list_background));
					newDefaultValues.put(ListsTable.COL_MASTER_LIST_ITEM_NORMAL_TEXT_COLOR,
							res.getColor(R.color.preset4_list_strikeout_text));
					newDefaultValues.put(ListsTable.COL_MASTER_LIST_ITEM_SELECTED_TEXT_COLOR,
							res.getColor(R.color.preset4_list_normal_text));

					newDefaultValues.put(ListsTable.COL_SEPARATOR_BACKGROUND_COLOR,
							res.getColor(R.color.preset4_separator_background));
					newDefaultValues
							.put(ListsTable.COL_SEPARATOR_TEXT_COLOR, res.getColor(R.color.preset4_separator_text));

					ListsTable.UpdateListsTableFieldValues(mContext, mListID, newDefaultValues);
					break;

				case 5:
					// Winter Road
					newDefaultValues.put(ListsTable.COL_TITLE_BACKGROUND_COLOR,
							res.getColor(R.color.preset5_title_background));
					newDefaultValues.put(ListsTable.COL_TITLE_TEXT_COLOR, res.getColor(R.color.preset5_title_text));

					newDefaultValues.put(ListsTable.COL_LIST_BACKGROUND_COLOR,
							res.getColor(R.color.preset5_list_background));
					newDefaultValues.put(ListsTable.COL_ITEM_NORMAL_TEXT_COLOR,
							res.getColor(R.color.preset5_list_normal_text));
					newDefaultValues.put(ListsTable.COL_ITEM_STRIKEOUT_TEXT_COLOR,
							res.getColor(R.color.preset5_list_strikeout_text));

					newDefaultValues
							.put(ListsTable.COL_MASTER_LIST_BACKGROUND_COLOR,
									res.getColor(R.color.preset5_list_background));
					newDefaultValues.put(ListsTable.COL_MASTER_LIST_ITEM_NORMAL_TEXT_COLOR,
							res.getColor(R.color.preset5_list_strikeout_text));
					newDefaultValues.put(ListsTable.COL_MASTER_LIST_ITEM_SELECTED_TEXT_COLOR,
							res.getColor(R.color.preset5_list_normal_text));

					newDefaultValues.put(ListsTable.COL_SEPARATOR_BACKGROUND_COLOR,
							res.getColor(R.color.preset5_separator_background));
					newDefaultValues
							.put(ListsTable.COL_SEPARATOR_TEXT_COLOR, res.getColor(R.color.preset5_separator_text));

					ListsTable.UpdateListsTableFieldValues(mContext, mListID, newDefaultValues);

					break;

				default:
					// Sandy Stone
					newDefaultValues.put(ListsTable.COL_TITLE_BACKGROUND_COLOR,
							res.getColor(R.color.preset0_title_background));
					newDefaultValues.put(ListsTable.COL_TITLE_TEXT_COLOR, res.getColor(R.color.preset0_title_text));

					newDefaultValues.put(ListsTable.COL_LIST_BACKGROUND_COLOR,
							res.getColor(R.color.preset0_list_background));
					newDefaultValues.put(ListsTable.COL_ITEM_NORMAL_TEXT_COLOR,
							res.getColor(R.color.preset0_list_normal_text));
					newDefaultValues.put(ListsTable.COL_ITEM_STRIKEOUT_TEXT_COLOR,
							res.getColor(R.color.preset0_list_strikeout_text));

					newDefaultValues
							.put(ListsTable.COL_MASTER_LIST_BACKGROUND_COLOR,
									res.getColor(R.color.preset0_list_background));
					newDefaultValues.put(ListsTable.COL_MASTER_LIST_ITEM_NORMAL_TEXT_COLOR,
							res.getColor(R.color.preset0_list_strikeout_text));
					newDefaultValues.put(ListsTable.COL_MASTER_LIST_ITEM_SELECTED_TEXT_COLOR,
							res.getColor(R.color.preset0_list_normal_text));

					newDefaultValues.put(ListsTable.COL_SEPARATOR_BACKGROUND_COLOR,
							res.getColor(R.color.preset0_separator_background));
					newDefaultValues
							.put(ListsTable.COL_SEPARATOR_TEXT_COLOR, res.getColor(R.color.preset0_separator_text));

					break;
			}
		}
	}

	public long getListID() {
		return mListID;
	}

	public String getListTitle() {
		if (mListCursor != null) {
			return mListCursor.getString(mListCursor.getColumnIndexOrThrow(ListsTable.COL_LIST_TITLE));
		}
		return "";
	}

	public long getActiveStoreID() {
		if (mListCursor != null) {
			return mListCursor.getLong(mListCursor.getColumnIndexOrThrow(ListsTable.COL_ACTIVE_STORE_ID));
		}
		return -1;
	}

	public boolean isManualSort() {
		if (mListCursor != null) {
			int value = mListCursor.getInt(mListCursor.getColumnIndexOrThrow(ListsTable.COL_LIST_SORT_ORDER));
			return value == AListUtilities.LIST_SORT_MANUAL;
		}
		return false;
	}

	public boolean isGroupAdditonAllowed() {
		if (mListCursor != null) {
			int value = mListCursor.getInt(mListCursor.getColumnIndexOrThrow(ListsTable.COL_ALLOW_GROUP_ADDITIONS));
			return value == 1.;
		}
		return false;
	}

	public boolean getShowGroupsInListsFragment() {
		if (mListCursor != null) {
			int value = mListCursor.getInt(mListCursor.getColumnIndexOrThrow(ListsTable.COL_LIST_SORT_ORDER));
			return value == AListUtilities.LIST_SORT_BY_GROUP;
		}
		return false;
	}

	public boolean getShowGroupsInMasterListFragment() {
		if (mListCursor != null) {
			int value = mListCursor.getInt(mListCursor.getColumnIndexOrThrow(ListsTable.COL_MASTER_LIST_SORT_ORDER));
			return value == AListUtilities.MASTER_LIST_SORT_BY_GROUP;
		}
		return false;
	}

	public boolean getShowStores() {
		if (mListCursor != null) {
			int value = mListCursor.getInt(mListCursor.getColumnIndexOrThrow(ListsTable.COL_LIST_SORT_ORDER));
			return value == AListUtilities.LIST_SORT_BY_STORE_LOCATION;
		}
		return false;
	}

	public boolean getDeleteNoteUponDeselectingItem() {
		if (mListCursor != null) {
			int value = mListCursor.getInt(mListCursor
					.getColumnIndexOrThrow(ListsTable.COL_DELETE_NOTE_UPON_DESELECTING_ITEM));
			return AListUtilities.intToBoolean(value);
		}
		return false;
	}

	public int getListSortOrder() {
		if (mListCursor != null) {
			return mListCursor.getInt(mListCursor.getColumnIndexOrThrow(ListsTable.COL_LIST_SORT_ORDER));
		}
		return -1;
	}

	public int getMasterListSortOrder() {
		if (mListCursor != null) {
			return mListCursor.getInt(mListCursor.getColumnIndexOrThrow(ListsTable.COL_MASTER_LIST_SORT_ORDER));
		}
		return -1;
	}

	public int getTitleBackgroundColor() {
		if (mListCursor != null) {
			return mListCursor.getInt(mListCursor.getColumnIndexOrThrow(ListsTable.COL_TITLE_BACKGROUND_COLOR));
		}
		return -1;
	}

	public int getTitleTextColor() {
		if (mListCursor != null) {
			return mListCursor.getInt(mListCursor.getColumnIndexOrThrow(ListsTable.COL_TITLE_TEXT_COLOR));
		}
		return -1;
	}

	public int getSeparatorBackgroundColor() {
		if (mListCursor != null) {
			return mListCursor.getInt(mListCursor.getColumnIndexOrThrow(ListsTable.COL_SEPARATOR_BACKGROUND_COLOR));
		}
		return -1;
	}

	public int getSeparatorTextColor() {
		if (mListCursor != null) {
			return mListCursor.getInt(mListCursor.getColumnIndexOrThrow(ListsTable.COL_SEPARATOR_TEXT_COLOR));
		}
		return -1;
	}

	public int getListBackgroundColor() {
		if (mListCursor != null) {
			return mListCursor.getInt(mListCursor.getColumnIndexOrThrow(ListsTable.COL_LIST_BACKGROUND_COLOR));
		}
		return -1;
	}

	public int getItemNormalTextColor() {
		if (mListCursor != null) {
			return mListCursor.getInt(mListCursor.getColumnIndexOrThrow(ListsTable.COL_ITEM_NORMAL_TEXT_COLOR));
		}
		return -1;
	}

	public int getItemStrikeoutTextColor() {
		if (mListCursor != null) {
			return mListCursor.getInt(mListCursor.getColumnIndexOrThrow(ListsTable.COL_ITEM_STRIKEOUT_TEXT_COLOR));
		}
		return -1;
	}

	public int getMasterListBackgroundColor() {
		if (mListCursor != null) {
			return mListCursor.getInt(mListCursor.getColumnIndexOrThrow(ListsTable.COL_MASTER_LIST_BACKGROUND_COLOR));
		}
		return -1;
	}

	public int getMasterListItemNormalTextColor() {
		if (mListCursor != null) {
			return mListCursor.getInt(mListCursor
					.getColumnIndexOrThrow(ListsTable.COL_MASTER_LIST_ITEM_NORMAL_TEXT_COLOR));
		}
		return -1;
	}

	public int getMasterListItemSelectedTextColor() {
		if (mListCursor != null) {
			return mListCursor.getInt(mListCursor
					.getColumnIndexOrThrow(ListsTable.COL_MASTER_LIST_ITEM_SELECTED_TEXT_COLOR));
		}
		return -1;
	}

	public int getListViewFirstVisiblePosition() {
		if (mListCursor != null) {
			return mListCursor
					.getInt(mListCursor.getColumnIndexOrThrow(ListsTable.COL_LISTVIEW_FIRST_VISIBLE_POSITION));
		}
		return -1;
	}

	public int getListViewTop() {
		if (mListCursor != null) {
			return mListCursor.getInt(mListCursor.getColumnIndexOrThrow(ListsTable.COL_LISTVIEW_TOP));
		}
		return -1;
	}

	public int getMasterListViewFirstVisiblePosition() {
		if (mListCursor != null) {
			return mListCursor.getInt(mListCursor
					.getColumnIndexOrThrow(ListsTable.COL_MASTER_LISTVIEW_FIRST_VISIBLE_POSITION));
		}
		return -1;
	}

	public int getMasterListViewTop() {
		if (mListCursor != null) {
			return mListCursor.getInt(mListCursor.getColumnIndexOrThrow(ListsTable.COL_MASTER_LISTVIEW_TOP));
		}
		return -1;
	}

	public void updateListsTableFieldValues(ContentValues newFieldValues) {
		if (mListCursor != null) {
			ListsTable.UpdateListsTableFieldValues(mContext, mListID, newFieldValues);
			this.RefreshListSettings();
		}
	}

	@Override
	protected void finalize() {
		if (this.mListCursor != null) {
			this.mListCursor.close();
		}
	}
}
