package com.lbconsulting.alist.classes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.lbconsulting.alist.database.ItemsTable;

public class ItemSettings {

	private Context context;
	private long itemID;
	private Cursor itemCursor;

	public ItemSettings(Context context, long itemID) {
		this.context = context;
		this.itemID = itemID;
		updateItemsCurosr();
	}

	private void updateItemsCurosr() {
		this.itemCursor = ItemsTable.getItem(context, itemID);
		if (itemCursor != null) {
			this.itemCursor.moveToFirst();
		}
	}

	public long getItemID() {
		return itemID;
	}

	public String getItemName() {
		return itemCursor.getString(itemCursor.getColumnIndexOrThrow(ItemsTable.COL_ITEM_NAME));
	}

	public String getItemNote() {
		return itemCursor.getString(itemCursor.getColumnIndexOrThrow(ItemsTable.COL_ITEM_NOTE));
	}

	public long getListID() {
		return itemCursor.getLong(itemCursor.getColumnIndexOrThrow(ItemsTable.COL_LIST_ID));
	}

	public long getGroupID() {
		return itemCursor.getLong(itemCursor.getColumnIndexOrThrow(ItemsTable.COL_GROUP_ID));
	}

	public boolean isItemSelected() {
		int itemSelectedValue = itemCursor.getInt(itemCursor.getColumnIndexOrThrow(ItemsTable.COL_SELECTED));
		return itemSelectedValue > 0;
	}

	public boolean isItemStruckOut() {
		int ItemStruckOutValue = itemCursor.getInt(itemCursor.getColumnIndexOrThrow(ItemsTable.COL_STRUCK_OUT));
		return ItemStruckOutValue > 0;
	}

	public boolean isItemChecked() {
		int ItemCheckedValue = itemCursor.getInt(itemCursor.getColumnIndexOrThrow(ItemsTable.COL_CHECKED));
		return ItemCheckedValue > 0;
	}

	public int getManualSortOrder() {
		return itemCursor.getInt(itemCursor.getColumnIndexOrThrow(ItemsTable.COL_MANUAL_SORT_ORDER));
	}

	public long getDateTimeLastUsed() {
		return itemCursor.getLong(itemCursor.getColumnIndexOrThrow(ItemsTable.COL_DATE_TIME_LAST_USED));
	}

	public void updateItemFieldValues(ContentValues newFieldValues) {
		ItemsTable.UpdateItemFieldValues(context, itemID, newFieldValues);
		this.updateItemsCurosr();
	}

	protected void finalize() {
		if (this.itemCursor != null) {
			this.itemCursor.close();
		}
	}

}
