package com.lbconsulting.alist.database;

import java.util.Calendar;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v4.content.CursorLoader;

import com.dropbox.sync.android.DbxDatastore;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxRecord;
import com.dropbox.sync.android.DbxTable;
import com.lbconsulting.alist.utilities.AListUtilities;
import com.lbconsulting.alist.utilities.MyLog;

public class ItemsTable {

	// Items data table
	// Version 1
	public static final String TABLE_ITEMS = "tblItems";
	public static final String COL_ITEM_ID = "_id";
	public static final String COL_ITEM_DROPBOX_ID = "dropboxID";
	public static final String COL_ITEM_NAME = "itemName";
	public static final String COL_ITEM_NUMBER = "itemNumber";
	public static final String COL_ITEM_NOTE = "itemNote";
	public static final String COL_LIST_ID = "listID";
	public static final String COL_GROUP_ID = "groupID";
	public static final String COL_SELECTED = "itemSelected";
	public static final String COL_STRUCK_OUT = "itemStruckOut";
	public static final String COL_CHECKED = "itemChecked";
	public static final String COL_MANUAL_SORT_ORDER = "manualSortOrder";
	public static final String COL_MANUAL_SORT_SWITCH = "manualSortSwitch";
	public static final String COL_DATE_TIME_LAST_USED = "dateTimeLastUsed";

	public static final String[] PROJECTION_ALL = { COL_ITEM_ID, COL_ITEM_DROPBOX_ID, COL_ITEM_NAME, COL_ITEM_NUMBER,
			COL_ITEM_NOTE, COL_LIST_ID,
			COL_GROUP_ID, COL_SELECTED, COL_STRUCK_OUT, COL_CHECKED, COL_MANUAL_SORT_ORDER, COL_MANUAL_SORT_SWITCH,
			COL_DATE_TIME_LAST_USED };

	public static final String[] PROJECTION_WITH_GROUP_NAME = {
			TABLE_ITEMS + "." + COL_ITEM_ID,
			TABLE_ITEMS + "." + COL_ITEM_NAME,
			TABLE_ITEMS + "." + COL_ITEM_NOTE,
			TABLE_ITEMS + "." + COL_LIST_ID,
			TABLE_ITEMS + "." + COL_GROUP_ID,
			TABLE_ITEMS + "." + COL_SELECTED,
			TABLE_ITEMS + "." + COL_STRUCK_OUT,
			TABLE_ITEMS + "." + COL_CHECKED,
			TABLE_ITEMS + "." + COL_MANUAL_SORT_ORDER,
			TABLE_ITEMS + "." + COL_MANUAL_SORT_SWITCH,
			GroupsTable.TABLE_GROUPS + "." + GroupsTable.COL_GROUP_NAME };

	public static final String[] PROJECTION_WITH_ITEM_NAME_AND_GROUP_NAME = {
			TABLE_ITEMS + "." + COL_ITEM_ID,
			TABLE_ITEMS + "." + COL_ITEM_NAME,
			GroupsTable.TABLE_GROUPS + "." + GroupsTable.COL_GROUP_NAME };

	public static final String[] PROJECTION_WITH_LOCATION_NAME = {
			TABLE_ITEMS + "." + COL_ITEM_ID,
			TABLE_ITEMS + "." + COL_ITEM_NAME,
			TABLE_ITEMS + "." + COL_ITEM_NOTE,
			TABLE_ITEMS + "." + COL_LIST_ID,
			TABLE_ITEMS + "." + COL_GROUP_ID,
			TABLE_ITEMS + "." + COL_SELECTED,
			TABLE_ITEMS + "." + COL_STRUCK_OUT,
			TABLE_ITEMS + "." + COL_CHECKED,
			TABLE_ITEMS + "." + COL_MANUAL_SORT_ORDER,
			TABLE_ITEMS + "." + COL_MANUAL_SORT_SWITCH,
			BridgeTable.TABLE_BRIDGE + "." + BridgeTable.COL_LOCATION_ID,
			LocationsTable.TABLE_LOCATIONS + "." + LocationsTable.COL_LOCATION_NAME };

	public static final String CONTENT_PATH = TABLE_ITEMS;
	public static final String CONTENT_PATH_ITEMS_WITH_GROUPS = "itemsWithGroups";
	public static final String CONTENT_PATH_ITEMS_WITH_LOCATIONS = "itemsWithLocations";

	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + "vnd.lbconsulting."
			+ TABLE_ITEMS;
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + "vnd.lbconsulting."
			+ TABLE_ITEMS;
	public static final Uri CONTENT_URI = Uri.parse("content://" + AListContentProvider.AUTHORITY + "/" + CONTENT_PATH);

	public static final Uri CONTENT_URI_ITEMS_WITH_GROUPS = Uri.parse("content://" + AListContentProvider.AUTHORITY
			+ "/" + CONTENT_PATH_ITEMS_WITH_GROUPS);

	public static final Uri CONTENT_URI_ITEMS_WITH_LOCATIONS = Uri.parse("content://" + AListContentProvider.AUTHORITY
			+ "/" + CONTENT_PATH_ITEMS_WITH_LOCATIONS);

	public static final String SORT_ORDER_ITEM_NAME = COL_ITEM_NAME + " ASC";
	public static final String SORT_ORDER_SELECTED_AT_TOP = COL_SELECTED + " DESC, " + SORT_ORDER_ITEM_NAME;
	public static final String SORT_ORDER_SELECTED_AT_BOTTOM = COL_SELECTED + " ASC, " + SORT_ORDER_ITEM_NAME;
	public static final String SORT_ORDER_LAST_USED = COL_DATE_TIME_LAST_USED + " DESC, " + SORT_ORDER_ITEM_NAME;
	public static final String SORT_ORDER_MANUAL = COL_MANUAL_SORT_ORDER + " ASC";

	// TODO: SORT by group name not id!
	// public static final String SORT_ORDER_BY_GROUP = COL_GROUP_ID + " ASC, "
	// + SORT_ORDER_ITEM_NAME;

	public static final int SELECTED_TRUE = 1;
	public static final int SELECTED_FALSE = 0;

	public static final int STRUCKOUT_TRUE = 1;
	public static final int STRUCKOUT_FALSE = 0;

	public static final int CHECKED_TRUE = 1;
	public static final int CHECKED_FALSE = 0;

	public static final int MANUAL_SORT_SWITCH_INVISIBLE = 0;
	public static final int MANUAL_SORT_SWITCH_VISIBLE = 1;
	public static final int MANUAL_SORT_SWITCH_ITEM_SWITCHED = 2;

	// private static final long milliSecondsPerDay = 1000;
	private static final long milliSecondsPerDay = 1000 * 60 * 60 * 24;

	// Database creation SQL statements
	private static final String DATATABLE_CREATE =
			"create table " + TABLE_ITEMS
					+ " ("
					+ COL_ITEM_ID + " integer primary key autoincrement, "
					+ COL_ITEM_DROPBOX_ID + " text, "
					+ COL_ITEM_NAME + " text collate nocase, "
					+ COL_ITEM_NUMBER + " integer, "
					+ COL_ITEM_NOTE + " text collate nocase, "
					+ COL_LIST_ID + " integer not null references " + ListsTable.TABLE_LISTS + " ("
					+ ListsTable.COL_LIST_ID + ") default 1, "
					+ COL_GROUP_ID + " integer not null references " + GroupsTable.TABLE_GROUPS + " ("
					+ GroupsTable.COL_GROUP_ID + ") default 1, "
					+ COL_SELECTED + " integer default 0, "
					+ COL_STRUCK_OUT + " integer default 0, "
					+ COL_CHECKED + " integer default 0, "
					+ COL_MANUAL_SORT_ORDER + " integer default -1, "
					+ COL_MANUAL_SORT_SWITCH + " integer default 1, "
					+ COL_DATE_TIME_LAST_USED + " integer"
					+ ");";

	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(DATATABLE_CREATE);
		MyLog.i("ItemsTable", "onCreate: " + TABLE_ITEMS + " created.");

		/*
		 * String insertProjection = "insert into " + TABLE_ITEMS + " (" +
		 * COL_ITEM_ID + ", " + COL_ITEM_NAME + ", " + COL_ITEM_NOTE + ", " +
		 * COL_LIST_ID + ", " + COL_SELECTED + ", " + COL_DATE_TIME_LAST_USED +
		 * ") VALUES ";
		 */

	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		MyLog.w(TABLE_ITEMS, "Upgrading database from version " + oldVersion + " to version " + newVersion);
		int upgradeToVersion = oldVersion + 1;
		switch (upgradeToVersion) {
		// fall through each case to upgrade to the newVersion
			case 2:
			case 3:
			case 4:
				// No changes in TABLE_ITEMS
				break;

			default:
				// upgrade version not found!
				MyLog.e(TABLE_ITEMS, "Upgrade version " + newVersion + " not found!");
				database.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
				onCreate(database);
				break;
		}
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Create Methods
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static long CreateNewItem(Context context, long listID, String itemName) {
		long newItemID = -1;
		if (listID > 1) {
			itemName = itemName.trim();
			// verify that the item does not already exist in the table
			if (itemName != null && !itemName.isEmpty()) {
				Cursor cursor = getItem(context, listID, itemName);
				if (cursor != null && cursor.getCount() > 0) {
					// the item exists in the table ... so return its id
					cursor.moveToFirst();
					newItemID = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ITEM_ID));
					cursor.close();
				} else {
					// item does not exist in the table ... so add it
					ContentResolver cr = context.getContentResolver();
					Uri uri = CONTENT_URI;
					ContentValues values = new ContentValues();
					values.put(COL_ITEM_NAME, itemName);
					values.put(COL_LIST_ID, listID);
					// Note: Content Provider inserts the current date/time when
					// creating a new item
					try {
						Uri newListUri = cr.insert(uri, values);
						if (newListUri != null) {
							newItemID = Long.parseLong(newListUri.getLastPathSegment());
							values = new ContentValues();
							values.put(COL_MANUAL_SORT_ORDER, newItemID);
							UpdateItemFieldValues(context, newItemID, values);
						}
					} catch (Exception e) {
						MyLog.e("Exception error in CreateNewList. ", "");
						e.printStackTrace();
					}
				}

				if (cursor != null) {
					cursor.close();
				}
			}
		}
		return newItemID;
	}

	public static long CreateNewItem(Context context, long listID, String itemName, long groupID) {
		long newItemID = CreateNewItem(context, listID, itemName);
		if (newItemID > 0) {
			ContentValues values = new ContentValues();
			values.put(COL_GROUP_ID, groupID);
			UpdateItemFieldValues(context, newItemID, values);
		}
		return newItemID;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Read Methods
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	public static Cursor getItem(Context context, long itemID) {
		Cursor cursor = null;
		if (itemID > 0) {
			Uri uri = Uri.withAppendedPath(CONTENT_URI, String.valueOf(itemID));
			String[] projection = PROJECTION_ALL;
			String selection = null;
			String selectionArgs[] = null;
			String sortOrder = null;
			ContentResolver cr = context.getContentResolver();
			try {
				cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
			} catch (Exception e) {
				MyLog.e("Exception error in ItemsTable: getItem. ", "");
				e.printStackTrace();
			}
		}
		return cursor;
	}

	public static Cursor getItem(Context context, long listID, String itemName) {
		Cursor cursor = null;
		if (listID > 1) {
			Uri uri = CONTENT_URI;
			String[] projection = PROJECTION_ALL;
			String selection = COL_LIST_ID + " = ? AND " + COL_ITEM_NAME + " = ?";
			String selectionArgs[] = new String[] { String.valueOf(listID), itemName };
			String sortOrder = null;
			ContentResolver cr = context.getContentResolver();
			try {
				cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
			} catch (Exception e) {
				MyLog.e("Exception error in ItemsTable: getItem. ", "");
				e.printStackTrace();
			}
		}
		return cursor;
	}

	public static long getListID(Context context, long itemID) {
		long listID = -1;
		Cursor cursor = getItem(context, itemID);
		if (cursor != null) {
			cursor.moveToFirst();
			listID = cursor.getLong(cursor.getColumnIndexOrThrow(COL_LIST_ID));
			cursor.close();
		}

		return listID;
	}

	public static Cursor getAllItemsInListCursor(Context context, long listID, String sortOrder) {
		Cursor cursor = null;
		if (listID > 1) {
			Uri uri = CONTENT_URI;
			String[] projection = PROJECTION_ALL;
			String selection = COL_LIST_ID + " = ?";
			String selectionArgs[] = new String[] { String.valueOf(listID) };
			ContentResolver cr = context.getContentResolver();
			try {
				cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
			} catch (Exception e) {
				MyLog.e("Exception error in ItemsTable: getAllItemsInListCursor.", "");
				e.printStackTrace();
			}
		}
		return cursor;
	}

	public static CursorLoader getAllItemsInList(Context context, long listID, String sortOrder) {
		CursorLoader cursorLoader = null;
		if (listID > 1) {
			Uri uri = CONTENT_URI;
			String[] projection = PROJECTION_ALL;
			String selection = COL_LIST_ID + " = ?";
			String selectionArgs[] = new String[] { String.valueOf(listID) };
			try {
				cursorLoader = new CursorLoader(context, uri, projection, selection, selectionArgs, sortOrder);
			} catch (Exception e) {
				MyLog.e("Exception error  in ItemsTable: getAllItemsInList. ", "");
				e.printStackTrace();
			}
		}
		return cursorLoader;
	}

	public static CursorLoader getAllItemsInList(Context context, long listID, String selection, String sortOrder) {
		CursorLoader cursorLoader = null;
		if (listID > 1) {
			Uri uri = CONTENT_URI;
			String[] projection = PROJECTION_ALL;
			if (selection != null) {
				selection = selection + " AND " + TABLE_ITEMS + "." + COL_LIST_ID + " = ?";
			} else {
				selection = TABLE_ITEMS + "." + COL_LIST_ID + " = ?";
			}
			String selectionArgs[] = new String[] { String.valueOf(listID) };
			try {
				cursorLoader = new CursorLoader(context, uri, projection, selection, selectionArgs, sortOrder);
			} catch (Exception e) {
				MyLog.e("Exception error  in ItemsTable: getAllItemsInList. ", "");
				e.printStackTrace();
			}
		}
		return cursorLoader;
	}

	public static CursorLoader getAllItemsInListWithGroups(Context context, long listID, String selection) {
		CursorLoader cursorLoader = null;
		if (listID > 1) {
			Uri uri = CONTENT_URI_ITEMS_WITH_GROUPS;
			String[] projection = ItemsTable.PROJECTION_WITH_GROUP_NAME;

			if (selection != null) {
				selection = selection + " AND " + TABLE_ITEMS + "." + COL_LIST_ID + " = ?";
			} else {
				selection = TABLE_ITEMS + "." + COL_LIST_ID + " = ?";
			}
			String selectionArgs[] = new String[] { String.valueOf(listID) };
			String sortOrder = GroupsTable.SORT_ORDER_GROUP + ", " + ItemsTable.SORT_ORDER_ITEM_NAME;
			try {
				cursorLoader = new CursorLoader(context, uri, projection, selection, selectionArgs, sortOrder);
			} catch (Exception e) {
				MyLog.e("Exception error  in ItemsTable: getAllItemsInListWithGroups. ", "");
				e.printStackTrace();
			}
		}
		return cursorLoader;
	}

	public static CursorLoader getAllItemsInListWithLocations(Context context, long listID, long storeID) {
		CursorLoader cursorLoader = null;
		if (listID > 1) {
			Uri uri = CONTENT_URI_ITEMS_WITH_LOCATIONS;
			String[] projection = ItemsTable.PROJECTION_WITH_LOCATION_NAME;
			String selection = TABLE_ITEMS + "." + COL_LIST_ID + " = ? AND "
					+ BridgeTable.TABLE_BRIDGE + "." + BridgeTable.COL_STORE_ID + " = ?";
			String selectionArgs[] = new String[] { String.valueOf(listID), String.valueOf(storeID) };
			String sortOrder = LocationsTable.SORT_ORDER_LOCATION + ", " + ItemsTable.SORT_ORDER_ITEM_NAME;
			try {
				cursorLoader = new CursorLoader(context, uri, projection, selection, selectionArgs, sortOrder);
			} catch (Exception e) {
				MyLog.e("Exception error  in ItemsTable: getAllItemsInListWithLocations. ", "");
				e.printStackTrace();
			}
		}
		return cursorLoader;
	}

	public static CursorLoader getAllSelectedItemsInList(Context context, long listID, boolean selected,
			String sortOrder) {
		CursorLoader cursorLoader = null;
		if (listID > 1) {
			int selectedValue = AListUtilities.boolToInt(selected);
			if (sortOrder == null) {
				sortOrder = SORT_ORDER_ITEM_NAME;
			}
			Uri uri = CONTENT_URI;
			String[] projection = PROJECTION_ALL;
			String selection = COL_LIST_ID + " = ? AND " + COL_SELECTED + " = ?";
			String selectionArgs[] = new String[] { String.valueOf(listID), String.valueOf(selectedValue) };

			try {
				cursorLoader = new CursorLoader(context, uri, projection, selection, selectionArgs, sortOrder);
			} catch (Exception e) {
				MyLog.e("Exception error  in ItemsTable: getAllSelectedItemsInList. ", "");
				e.printStackTrace();
			}
		}
		return cursorLoader;
	}

	public static CursorLoader getAllSelectedItemsInListWithGroups(Context context, long listID, boolean selected) {
		CursorLoader cursorLoader = null;
		if (listID > 1) {
			int selectedValue = AListUtilities.boolToInt(selected);
			Uri uri = CONTENT_URI_ITEMS_WITH_GROUPS;
			String[] projection = ItemsTable.PROJECTION_WITH_GROUP_NAME;
			String selection = TABLE_ITEMS + "." + COL_LIST_ID + " = ? AND "
					+ TABLE_ITEMS + "." + COL_SELECTED + " = ?";

			String selectionArgs[] = new String[] { String.valueOf(listID), String.valueOf(selectedValue) };
			String sortOrder = GroupsTable.SORT_ORDER_GROUP + ", " + ItemsTable.SORT_ORDER_ITEM_NAME;
			try {
				cursorLoader = new CursorLoader(context, uri, projection, selection, selectionArgs, sortOrder);
			} catch (Exception e) {
				MyLog.e("Exception error  in ItemsTable: getAllSelectedItemsInListWithGroups. ", "");
				e.printStackTrace();
			}
		}
		return cursorLoader;
	}

	public static CursorLoader getAllSelectedItemsInListWithLocations(Context context, long listID, long storeID,
			boolean selected) {
		CursorLoader cursorLoader = null;
		if (listID > 1) {
			int selectedValue = AListUtilities.boolToInt(selected);
			Uri uri = CONTENT_URI_ITEMS_WITH_LOCATIONS;
			String[] projection = ItemsTable.PROJECTION_WITH_LOCATION_NAME;
			String selection = TABLE_ITEMS + "." + COL_LIST_ID + " = ? AND "
					+ BridgeTable.TABLE_BRIDGE + "." + BridgeTable.COL_STORE_ID + " = ? AND "
					+ TABLE_ITEMS + "." + COL_SELECTED + " = ?";
			String selectionArgs[] = new String[] { String.valueOf(listID), String.valueOf(storeID),
					String.valueOf(selectedValue) };
			String sortOrder = LocationsTable.SORT_ORDER_LOCATION + ", " + ItemsTable.SORT_ORDER_ITEM_NAME;
			try {
				cursorLoader = new CursorLoader(context, uri, projection, selection, selectionArgs, sortOrder);
			} catch (Exception e) {
				MyLog.e("Exception error  in ItemsTable: getAllSelectedItemsInListWithLocations. ", "");
				e.printStackTrace();
			}
		}
		return cursorLoader;
	}

	public static Cursor getAllSelectedItems(Context context, long listID, boolean selected, String sortOrder) {
		Cursor cursor = null;
		if (listID > 1) {
			int selectedValue = AListUtilities.boolToInt(selected);
			if (sortOrder == null) {
				sortOrder = SORT_ORDER_ITEM_NAME;
			}
			Uri uri = CONTENT_URI;
			String[] projection = PROJECTION_ALL;
			String selection = COL_LIST_ID + " = ? AND " + COL_SELECTED + " = ?";
			String selectionArgs[] = new String[] { String.valueOf(listID), String.valueOf(selectedValue) };
			ContentResolver cr = context.getContentResolver();
			try {
				cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
			} catch (Exception e) {
				MyLog.e("Exception error  in ItemsTable: getAllSelectedItems. ", "");
				e.printStackTrace();
			}
		}
		return cursor;
	}

	public static Cursor getAllSelectedItemsWithGroups(Context context, long listID, boolean selected) {
		Cursor cursor = null;
		if (listID > 1) {

			Uri uri = CONTENT_URI_ITEMS_WITH_GROUPS;
			String[] projection = ItemsTable.PROJECTION_WITH_GROUP_NAME;
			int selectedValue = AListUtilities.boolToInt(selected);
			String selection = TABLE_ITEMS + "." + COL_LIST_ID + " = ? AND "
					+ TABLE_ITEMS + "." + COL_SELECTED + " = ?";

			String selectionArgs[] = new String[] { String.valueOf(listID), String.valueOf(selectedValue) };
			String sortOrder = GroupsTable.SORT_ORDER_GROUP + ", " + ItemsTable.SORT_ORDER_ITEM_NAME;
			ContentResolver cr = context.getContentResolver();
			try {
				cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
			} catch (Exception e) {
				MyLog.e("Exception error  in ItemsTable: getAllSelectedItemsWithGroups. ", "");
				e.printStackTrace();
			}
		}
		return cursor;
	}

	public static Cursor getAllSelectedItemsWithLocations(Context context, long listID, long storeID, boolean selected) {
		Cursor cursor = null;
		if (listID > 1) {
			int selectedValue = AListUtilities.boolToInt(selected);
			Uri uri = CONTENT_URI_ITEMS_WITH_LOCATIONS;
			String[] projection = ItemsTable.PROJECTION_WITH_LOCATION_NAME;
			String selection = TABLE_ITEMS + "." + COL_LIST_ID + " = ? AND "
					+ BridgeTable.TABLE_BRIDGE + "." + BridgeTable.COL_STORE_ID + " = ? AND "
					+ TABLE_ITEMS + "." + COL_SELECTED + " = ?";
			String selectionArgs[] = new String[] { String.valueOf(listID), String.valueOf(storeID),
					String.valueOf(selectedValue) };
			String sortOrder = LocationsTable.SORT_ORDER_LOCATION + ", " + ItemsTable.SORT_ORDER_ITEM_NAME;
			ContentResolver cr = context.getContentResolver();
			try {
				cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
			} catch (Exception e) {
				MyLog.e("Exception error  in ItemsTable: getAllSelectedItemsWithLocations. ", "");
				e.printStackTrace();
			}
		}
		return cursor;
	}

	private static Cursor getAllDbxItemsCursor(Context context) {
		Cursor cursor = null;

		Uri uri = CONTENT_URI;
		String[] projection = new String[] { COL_ITEM_ID, COL_ITEM_DROPBOX_ID };

		String selection = COL_ITEM_DROPBOX_ID + " != '' OR " + COL_ITEM_DROPBOX_ID + " NOT NULL";
		String selectionArgs[] = null;

		ContentResolver cr = context.getContentResolver();
		try {
			cursor = cr.query(uri, projection, selection, selectionArgs, SORT_ORDER_ITEM_NAME);
		} catch (Exception e) {
			MyLog.e("Exception error  in getAllDbxItemsCursor. ", "");
			e.printStackTrace();
		}
		return cursor;
	}

	public static Cursor getAllItemsWithGroups(Context context, long listID) {
		Cursor cursor = null;
		if (listID > 1) {
			Uri uri = CONTENT_URI_ITEMS_WITH_GROUPS;
			String[] projection = ItemsTable.PROJECTION_WITH_ITEM_NAME_AND_GROUP_NAME;
			String selection = TABLE_ITEMS + "." + COL_LIST_ID + " = ?";
			String selectionArgs[] = new String[] { String.valueOf(listID) };
			String sortOrder = ItemsTable.SORT_ORDER_ITEM_NAME + ", " + GroupsTable.SORT_ORDER_GROUP;
			ContentResolver cr = context.getContentResolver();
			try {
				cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
			} catch (Exception e) {
				MyLog.e("Exception error  in ItemsTable: getAllItemsWithGroups. ", "");
				e.printStackTrace();
			}
		}
		return cursor;
	}

	public static Cursor getAllItems(Context context) {
		Cursor cursor = null;

		Uri uri = CONTENT_URI;
		String[] projection = PROJECTION_ALL;
		String selection = null;
		String selectionArgs[] = null;
		String sortOrder = null;

		ContentResolver cr = context.getContentResolver();
		try {
			cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
		} catch (Exception e) {
			MyLog.e("Exception error  in ItemsTable: getAllItems. ", "");
			e.printStackTrace();
		}
		return cursor;
	}

	public static int getTotalNumberOfItems(Context context) {
		int totalNumberOfItems = 0;
		Cursor cursor = getAllItems(context);
		if (cursor != null) {
			totalNumberOfItems = cursor.getCount();
			cursor.close();
		}
		return totalNumberOfItems;
	}

	public static Cursor getAllCheckedItemsInList(Context context, long listID, boolean checked) {
		Cursor cursor = null;
		int checkedValue = AListUtilities.boolToInt(checked);
		if (listID > 1) {

			Uri uri = CONTENT_URI;
			String[] projection = PROJECTION_ALL;
			String selection = COL_LIST_ID + " = ? AND " + COL_CHECKED + " = ?";
			String selectionArgs[] = new String[] { String.valueOf(listID), String.valueOf(checkedValue) };
			String sortOrder = null;
			ContentResolver cr = context.getContentResolver();
			try {
				cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
			} catch (Exception e) {
				MyLog.e("Exception error  in ItemsTable: getAllCheckedItemsInList. ", "");
				e.printStackTrace();
			}
		}
		return cursor;
	}

	public static int getNumberOfCheckedItmes(Context context, long listID) {
		int numberOfCheckedItmes = -1;
		Cursor cursor = getAllCheckedItemsInList(context, listID, true);
		if (cursor != null) {
			numberOfCheckedItmes = cursor.getCount();
			cursor.close();
		}
		return numberOfCheckedItmes;
	}

	public static boolean isItemSwitched(Context context, long itemID) {
		boolean result = false;
		Cursor itemCursor = getItem(context, itemID);
		if (itemCursor != null) {
			itemCursor.moveToFirst();
			int switchValue = itemCursor.getInt(itemCursor.getColumnIndexOrThrow(COL_MANUAL_SORT_SWITCH));
			if (switchValue > 1) {
				result = true;
			}
			itemCursor.close();
		}
		return result;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Update Methods
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static int UpdateItemFieldValues(Context context, long itemID, ContentValues newFieldValues) {
		int numberOfUpdatedRecords = -1;
		if (itemID > 0) {
			ContentResolver cr = context.getContentResolver();
			Uri itemUri = Uri.withAppendedPath(CONTENT_URI, String.valueOf(itemID));
			String selection = null;
			String[] selectionArgs = null;
			numberOfUpdatedRecords = cr.update(itemUri, newFieldValues, selection, selectionArgs);
		}
		return numberOfUpdatedRecords;
	}

	public static void setItemInvisible(Context context, long itemID) {
		ContentValues newFieldValues = new ContentValues();
		newFieldValues.put(COL_MANUAL_SORT_SWITCH, MANUAL_SORT_SWITCH_INVISIBLE);
		UpdateItemFieldValues(context, itemID, newFieldValues);
	}

	public static void setItemVisible(Context context, long itemID) {
		ContentValues newFieldValues = new ContentValues();
		newFieldValues.put(COL_MANUAL_SORT_SWITCH, MANUAL_SORT_SWITCH_VISIBLE);
		UpdateItemFieldValues(context, itemID, newFieldValues);
	}

	public static int UpdateItem(Context context, long itemID, String itemName, String itemNote, long itemGroupID) {
		int numberOfUpdatedRecords = -1;
		if (itemID > 0) {
			itemName = itemName.trim();
			itemNote = itemNote.trim();
			try {
				ContentResolver cr = context.getContentResolver();
				Uri uri = CONTENT_URI;
				String where = COL_ITEM_ID + " = ?";
				String[] whereArgs = { String.valueOf(itemID) };
				ContentValues values = new ContentValues();
				values.put(COL_ITEM_NAME, itemName);
				values.put(COL_ITEM_NOTE, itemNote);
				values.put(COL_GROUP_ID, itemGroupID);
				numberOfUpdatedRecords = cr.update(uri, values, where, whereArgs);
			} catch (Exception e) {
				MyLog.e("Exception error in UpdateItem. ", "");
				e.printStackTrace();
			}
		}
		return numberOfUpdatedRecords;

	}

	public static int SelectItem(Context context, long itemID, boolean selected) {
		int numberOfUpdatedRecords = -1;
		if (itemID > 0) {
			try {
				ContentResolver cr = context.getContentResolver();
				Uri uri = CONTENT_URI;
				String where = COL_ITEM_ID + " = ?";
				String[] whereArgs = { String.valueOf(itemID) };

				ContentValues values = new ContentValues();
				int selectedValue = AListUtilities.boolToInt(selected);
				if (selected) {
					Calendar now = Calendar.getInstance();
					values.put(COL_DATE_TIME_LAST_USED, now.getTimeInMillis());
				}
				values.put(COL_SELECTED, selectedValue);
				numberOfUpdatedRecords = cr.update(uri, values, where, whereArgs);
			} catch (Exception e) {
				MyLog.e("Exception error in SelectItem. ", "");
				e.printStackTrace();

			}
		}
		return numberOfUpdatedRecords;
	}

	public static int DeselectAllItemsInList(Context context, long listID, boolean deleteNoteUponDeslectingItem) {
		int numberOfUpdatedRecords = -1;
		if (listID > 1) {
			try {
				ContentResolver cr = context.getContentResolver();
				Uri uri = CONTENT_URI;
				String where = COL_LIST_ID + " = ? AND " + COL_SELECTED + " = ?";
				String[] whereArgs = { String.valueOf(listID), String.valueOf(SELECTED_TRUE) };

				ContentValues values = new ContentValues();
				values.put(COL_STRUCK_OUT, STRUCKOUT_FALSE);
				values.put(COL_SELECTED, SELECTED_FALSE);
				if (deleteNoteUponDeslectingItem) {
					values.put(COL_ITEM_NOTE, "");
				}
				numberOfUpdatedRecords = cr.update(uri, values, where, whereArgs);
			} catch (Exception e) {
				MyLog.e("Exception error in DeselectAllItemsInList. ", "");
				e.printStackTrace();
			}
		}
		return numberOfUpdatedRecords;
	}

	public static int DeselectAllItemsInGroup(Context context, long groupID) {
		int numberOfUpdatedRecords = -1;
		if (groupID > 0) {
			try {
				ContentResolver cr = context.getContentResolver();
				Uri uri = CONTENT_URI;
				String where = COL_GROUP_ID + " = ? AND " + COL_SELECTED + " = ?";
				String[] whereArgs = { String.valueOf(groupID), String.valueOf(SELECTED_TRUE) };

				ContentValues values = new ContentValues();
				values.put(COL_SELECTED, SELECTED_FALSE);
				numberOfUpdatedRecords = cr.update(uri, values, where, whereArgs);
			} catch (Exception e) {
				MyLog.e("Exception error in DeselectAllItemsInGroup. ", "");
				e.printStackTrace();
			}
		}
		return numberOfUpdatedRecords;
	}

	public static void ToggleStrikeOut(Context context, long itemID) {
		Cursor cursor = getItem(context, itemID);
		if (cursor != null) {
			cursor.moveToFirst();
			int columnIndex = cursor.getColumnIndexOrThrow(COL_STRUCK_OUT);
			int strikeOutIntValue = cursor.getInt(columnIndex);
			boolean strikeOutValue = strikeOutIntValue > 0;
			cursor.close();
			StrikeItem(context, itemID, !strikeOutValue);
		}
	}

	public static void ToggleSelection(Context context, long itemID) {
		Cursor cursor = getItem(context, itemID);
		if (cursor != null) {
			cursor.moveToFirst();
			int columnIndex = cursor.getColumnIndexOrThrow(COL_SELECTED);
			int selectedIntValue = cursor.getInt(columnIndex);
			boolean selectedValue = selectedIntValue > 0;
			cursor.close();
			SelectItem(context, itemID, !selectedValue);
		}

	}

	public static void ToggleCheckBox(Context context, long itemID) {
		Cursor cursor = getItem(context, itemID);
		if (cursor != null) {
			cursor.moveToFirst();
			int columnIndex = cursor.getColumnIndexOrThrow(COL_CHECKED);
			int checkIntValue = cursor.getInt(columnIndex);
			boolean checkValue = checkIntValue > 0;
			cursor.close();
			CheckItem(context, itemID, !checkValue);
		}
	}

	public static int StrikeItem(Context context, long itemID, boolean struckOut) {
		int numberOfUpdatedRecords = -1;
		if (itemID > 0) {
			try {
				ContentResolver cr = context.getContentResolver();
				Uri uri = CONTENT_URI;
				String where = COL_ITEM_ID + " = ?";
				String[] whereArgs = { String.valueOf(itemID) };

				ContentValues values = new ContentValues();
				int struckOutValue = AListUtilities.boolToInt(struckOut);
				values.put(COL_STRUCK_OUT, struckOutValue);
				numberOfUpdatedRecords = cr.update(uri, values, where, whereArgs);
			} catch (Exception e) {
				MyLog.e("Exception error in StrikeItem. ", "");
				e.printStackTrace();
			}
		}
		return numberOfUpdatedRecords;
	}

	public static int UnStrikeAndDeselectAllStruckOutItems(Context context, long listID,
			boolean deleteNoteUponDeslectingItem) {
		int numberOfUpdatedRecords = -1;
		if (listID > 1) {
			try {
				ContentResolver cr = context.getContentResolver();
				Uri uri = CONTENT_URI;
				String where = COL_LIST_ID + " = ? AND " + COL_STRUCK_OUT + " = ?";
				String[] whereArgs = { String.valueOf(listID), String.valueOf(SELECTED_TRUE) };

				ContentValues values = new ContentValues();
				values.put(COL_STRUCK_OUT, STRUCKOUT_FALSE);
				values.put(COL_SELECTED, SELECTED_FALSE);
				if (deleteNoteUponDeslectingItem) {
					values.put(COL_ITEM_NOTE, "");
				}
				numberOfUpdatedRecords = cr.update(uri, values, where, whereArgs);
			} catch (Exception e) {
				MyLog.e("Exception error in UnStrikeAllItemsInList. ", "");
				e.printStackTrace();
			}
		}
		return numberOfUpdatedRecords;
	}

	public static int UnStrikeAllItemsInGroup(Context context, long groupID) {
		int numberOfUpdatedRecords = -1;
		if (groupID > 0) {
			try {
				ContentResolver cr = context.getContentResolver();
				Uri uri = CONTENT_URI;
				String where = COL_GROUP_ID + " = ? AND " + COL_STRUCK_OUT + " = ?";
				String[] whereArgs = { String.valueOf(groupID), String.valueOf(SELECTED_TRUE) };

				ContentValues values = new ContentValues();
				values.put(COL_STRUCK_OUT, SELECTED_FALSE);
				numberOfUpdatedRecords = cr.update(uri, values, where, whereArgs);
			} catch (Exception e) {
				MyLog.e("Exception error in UnStrikeAllItemsInGroup. ", "");
				e.printStackTrace();
			}
		}
		return numberOfUpdatedRecords;
	}

	public static int CheckItem(Context context, long itemID, boolean checked) {
		int numberOfUpdatedRecords = -1;
		if (itemID > 0) {
			try {
				ContentResolver cr = context.getContentResolver();
				Uri uri = CONTENT_URI;
				String where = COL_ITEM_ID + " = ?";
				String[] whereArgs = { String.valueOf(itemID) };

				ContentValues values = new ContentValues();
				int checkedValue = AListUtilities.boolToInt(checked);
				values.put(COL_CHECKED, checkedValue);
				numberOfUpdatedRecords = cr.update(uri, values, where, whereArgs);
			} catch (Exception e) {
				MyLog.e("Exception error in ItemsTable: CheckItem. ", "");
				e.printStackTrace();
			}
		}
		return numberOfUpdatedRecords;
	}

	public static int UnCheckAllItemsInList(Context context, long listID) {
		int numberOfUpdatedRecords = -1;
		if (listID > 1) {
			try {
				ContentResolver cr = context.getContentResolver();
				Uri uri = CONTENT_URI;
				String where = COL_LIST_ID + " = ? AND " + COL_CHECKED + " = ?";
				String[] whereArgs = { String.valueOf(listID), String.valueOf(CHECKED_TRUE) };

				ContentValues values = new ContentValues();
				values.put(COL_CHECKED, CHECKED_FALSE);
				numberOfUpdatedRecords = cr.update(uri, values, where, whereArgs);
			} catch (Exception e) {
				MyLog.e("Exception error in UnCheckAllItemsInList. ", "");
				e.printStackTrace();
			}
		}
		return numberOfUpdatedRecords;
	}

	public static int ApplyGroupToManageItems(Context context, long listID, long groupID) {
		int numberOfUpdatedRecords = -1;
		if (listID > 1) {
			try {
				ContentResolver cr = context.getContentResolver();
				Uri uri = CONTENT_URI;
				String where = COL_LIST_ID + " = ? AND " + COL_CHECKED + " = ?";
				String[] whereArgs = { String.valueOf(listID), String.valueOf(CHECKED_TRUE) };

				ContentValues values = new ContentValues();
				values.put(COL_GROUP_ID, groupID);
				values.put(COL_CHECKED, CHECKED_FALSE);
				numberOfUpdatedRecords = cr.update(uri, values, where, whereArgs);
			} catch (Exception e) {
				MyLog.e("Exception error in ApplyGroupToManageItems. ", "");
				e.printStackTrace();
			}
		}
		return numberOfUpdatedRecords;
	}

	public static int CheckItemsUnused(Context context, long listID, long numberOfDays) {
		int numberOfCheckedItems = -1;
		if (listID > 1) {

			long numberOfMilliSeconds = numberOfDays * milliSecondsPerDay;
			Calendar now = Calendar.getInstance();
			long dateTimeCutOff = now.getTimeInMillis() - numberOfMilliSeconds;

			ContentResolver cr = context.getContentResolver();
			Uri itemUri = CONTENT_URI;
			String selection = COL_DATE_TIME_LAST_USED + " < ?";
			String[] selectionArgs = { String.valueOf(dateTimeCutOff) };

			ContentValues values = new ContentValues();
			values.put(COL_CHECKED, CHECKED_TRUE);

			numberOfCheckedItems = cr.update(itemUri, values, selection, selectionArgs);
		}
		return numberOfCheckedItems;
	}

	public static void SwapManualSortOrder(Context context, long mobileItemID, long switchItemID,
			long previousSwitchItemID) {
		int numberOfUpdatedRecords = -1;
		if (mobileItemID > 0 && switchItemID > 0) {
			try {
				Cursor mobileItemCursor = getItem(context, mobileItemID);
				Cursor switchItemCursor = getItem(context, switchItemID);

				mobileItemCursor.moveToFirst();
				switchItemCursor.moveToFirst();

				int mobileItemManualSortOrder = mobileItemCursor.getInt(mobileItemCursor
						.getColumnIndexOrThrow(COL_MANUAL_SORT_ORDER));
				int switchItemManualSortOrder = switchItemCursor.getInt(switchItemCursor
						.getColumnIndexOrThrow(COL_MANUAL_SORT_ORDER));

				// TODO remove strings names
				String mobileItemName = mobileItemCursor.getString(mobileItemCursor
						.getColumnIndexOrThrow(COL_ITEM_NAME));
				String switchItemName = switchItemCursor.getString(switchItemCursor
						.getColumnIndexOrThrow(COL_ITEM_NAME));

				ContentResolver cr = context.getContentResolver();
				Uri uri = CONTENT_URI;
				String where = COL_ITEM_ID + " = ?";
				String[] whereArgsMobileItemCursor = { String.valueOf(mobileItemID) };
				ContentValues values = new ContentValues();
				values.put(COL_MANUAL_SORT_ORDER, switchItemManualSortOrder);
				numberOfUpdatedRecords = cr.update(uri, values, where, whereArgsMobileItemCursor);

				String[] whereArgsSwitchItemCursor = { String.valueOf(switchItemID) };
				values = new ContentValues();
				values.put(COL_MANUAL_SORT_ORDER, mobileItemManualSortOrder);
				values.put(COL_MANUAL_SORT_SWITCH, MANUAL_SORT_SWITCH_ITEM_SWITCHED);
				numberOfUpdatedRecords += cr.update(uri, values, where, whereArgsSwitchItemCursor);

				if (numberOfUpdatedRecords != 2) {
					MyLog.e("ItemsTable", "SwapManualSortOrder: Incorrect number of records updated.");
				}

				if (previousSwitchItemID > 0) {
					String[] whereArgsPreviousSwitchedItem = { String.valueOf(previousSwitchItemID) };
					values = new ContentValues();
					values.put(COL_MANUAL_SORT_SWITCH, MANUAL_SORT_SWITCH_VISIBLE);
					numberOfUpdatedRecords += cr.update(uri, values, where, whereArgsPreviousSwitchedItem);
				}

				mobileItemCursor.close();
				switchItemCursor.close();
				MyLog.i("ItemsTable",
						"SwapManualSortOrder: mobileItem:"
								+ mobileItemName + "(" + mobileItemManualSortOrder + ")"
								+ " MANUAL_SORT_ORDER swapped with switchItem:"
								+ switchItemName + "(" + switchItemManualSortOrder + ")");

			} catch (Exception e) {
				MyLog.e("Exception error in ItemsTable: CheckItem. ", "");
				e.printStackTrace();
			}
		}
	}

	public static int MoveItem(Context context, long itemID, long newListID) {
		int numberOfUpdatedRecords = 0;
		String existingItemName;
		Cursor existingItemCursor = null;
		Cursor newListCursor = null;

		if (itemID > 0 && newListID > 1) {
			existingItemCursor = getItem(context, itemID);
			if (existingItemCursor != null) {
				if (existingItemCursor.getCount() > 0) {

					existingItemCursor.moveToFirst();
					existingItemName = existingItemCursor
							.getString(existingItemCursor.getColumnIndexOrThrow(COL_ITEM_NAME));

					// verify that the item does not already exist in the new list
					newListCursor = getItem(context, newListID, existingItemName);
					if (newListCursor != null) {
						if (newListCursor.getCount() == 0) {
							// the item does not exists in the table ... so move it
							// by changing the listID
							numberOfUpdatedRecords = ChangeListID(context, itemID, newListID);

						} else {
							// the item exists in the new list ... so move it
							// by deleting it from the new list and changing the
							// existing item's listID
							long newListItemID = newListCursor
									.getLong(newListCursor.getColumnIndexOrThrow(COL_ITEM_ID));
							DeleteItem(context, newListItemID);
							numberOfUpdatedRecords = ChangeListID(context, itemID, newListID);
						}
					}
				}
			}
		}

		if (existingItemCursor != null) {
			existingItemCursor.close();
		}
		if (newListCursor != null) {
			newListCursor.close();
		}

		return numberOfUpdatedRecords;
	}

	private static int ChangeListID(Context context, long itemID, long newListID) {
		int numberOfUpdatedRecords = -1;
		ContentResolver cr = context.getContentResolver();
		Uri uri = CONTENT_URI;
		String where = COL_ITEM_ID + " = ?";
		String[] whereArgs = { String.valueOf(itemID) };

		ContentValues values = new ContentValues();
		values.put(COL_LIST_ID, newListID);
		values.put(COL_GROUP_ID, 1); // default group
		values.put(COL_CHECKED, CHECKED_FALSE); // clear the checked flag
		numberOfUpdatedRecords = cr.update(uri, values, where, whereArgs);

		return numberOfUpdatedRecords;
	}

	public static int MoveAllCheckedItemsInList(Context context, long listID, long newListID) {
		int numberOfUpdatedRecords = -1;
		Cursor checkedItemsCursor = null;
		if (listID > 1 && newListID > 1) {

			checkedItemsCursor = getAllCheckedItemsInList(context, listID, true);
			if (checkedItemsCursor != null && checkedItemsCursor.getCount() > 0) {
				numberOfUpdatedRecords = 0;
				int numberOfItemsMoved = 0;
				long itemID;
				checkedItemsCursor.moveToPosition(-1);
				while (checkedItemsCursor.moveToNext()) {
					itemID = checkedItemsCursor.getLong(checkedItemsCursor.getColumnIndexOrThrow(COL_ITEM_ID));
					numberOfItemsMoved = MoveItem(context, itemID, newListID);
					numberOfUpdatedRecords += numberOfItemsMoved;
				}
				if (numberOfUpdatedRecords != checkedItemsCursor.getCount()) {
					StringBuilder sb = new StringBuilder();
					sb.append("Error in MoveAllCheckedItemsInList: ");
					sb.append(System.getProperty("line.separator"));
					sb.append("Number of items moved does not match the number of checked items in the list!");
					sb.append(System.getProperty("line.separator"));
					sb.append("Number of items moved = " + numberOfItemsMoved);
					sb.append(System.getProperty("line.separator"));
					sb.append("Number of checked items in the list = " + checkedItemsCursor.getCount());
					MyLog.e("ItemsTable", sb.toString());
				}
			}
		}
		if (checkedItemsCursor != null) {
			checkedItemsCursor.close();
		}
		return numberOfUpdatedRecords;
	}

	public static int setManualSortOrder(Context context, long itemID, int manualSortOrder) {
		int numberOfUpdatedRecords = -1;
		if (itemID > 0) {
			try {
				ContentResolver cr = context.getContentResolver();
				Uri uri = CONTENT_URI;
				String where = COL_ITEM_ID + " = ?";
				String[] whereArgs = { String.valueOf(itemID) };

				ContentValues values = new ContentValues();
				values.put(COL_MANUAL_SORT_ORDER, manualSortOrder);
				numberOfUpdatedRecords = cr.update(uri, values, where, whereArgs);
			} catch (Exception e) {
				MyLog.e("Exception error in setManualSortOrder. ", "");
				e.printStackTrace();
			}
		}
		return numberOfUpdatedRecords;
	}

	public static int getManualSortOrder(Context context, long itemID) {
		int manualSortOrder = -1;
		if (itemID > 0) {
			Cursor cursor = getItem(context, itemID);
			if (cursor != null) {
				cursor.moveToFirst();
				manualSortOrder = cursor.getInt(cursor.getColumnIndexOrThrow(COL_MANUAL_SORT_ORDER));
				cursor.close();
			}
		}
		return manualSortOrder;
	}

	public static int ResetGroupID(Context context, long groupID) {
		int numberOfUpdatedRecords = -1;
		if (groupID > 1) {
			ContentResolver cr = context.getContentResolver();
			Uri uri = CONTENT_URI;
			String where = COL_GROUP_ID + " = ?";
			String[] whereArgs = { String.valueOf(groupID) };

			ContentValues values = new ContentValues();
			values.put(COL_GROUP_ID, 1); // groupID = 1 is the default groupID
			numberOfUpdatedRecords = cr.update(uri, values, where, whereArgs);
		}
		return numberOfUpdatedRecords;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Delete Methods
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	public static int DeleteItem(Context context, long itemID) {
		int numberOfDeletedRecords = -1;
		if (itemID > 0) {
			ContentResolver cr = context.getContentResolver();
			Uri uri = Uri.withAppendedPath(CONTENT_URI, String.valueOf(itemID));
			String where = null;
			String[] selectionArgs = null;
			cr.delete(uri, where, selectionArgs);
		}
		return numberOfDeletedRecords;
	}

	public static int DeleteAllItemsInList(Context context, long listID) {
		int numberOfDeletedRecords = -1;
		if (listID > 1) {
			Uri uri = CONTENT_URI;
			String where = COL_LIST_ID + " = ?";
			String selectionArgs[] = new String[] { String.valueOf(listID) };
			ContentResolver cr = context.getContentResolver();
			numberOfDeletedRecords = cr.delete(uri, where, selectionArgs);
		}
		return numberOfDeletedRecords;
	}

	public static int DeleteAllSelectedItemsInList(Context context, long listID) {
		int numberOfDeletedRecords = -1;
		if (listID > 1) {
			Uri uri = CONTENT_URI;
			String where = COL_LIST_ID + " = ? AND " + COL_SELECTED + " = ?";
			String selectionArgs[] = new String[] { String.valueOf(listID), String.valueOf(SELECTED_TRUE) };
			ContentResolver cr = context.getContentResolver();
			numberOfDeletedRecords = cr.delete(uri, where, selectionArgs);
		}
		return numberOfDeletedRecords;
	}

	public static int DeleteAllStruckOutItemsInList(Context context, long listID) {
		int numberOfDeletedRecords = -1;
		if (listID > 1) {
			Uri uri = CONTENT_URI;
			String where = COL_LIST_ID + " = ? AND " + COL_STRUCK_OUT + " = ?";
			String selectionArgs[] = new String[] { String.valueOf(listID), String.valueOf(STRUCKOUT_TRUE) };
			ContentResolver cr = context.getContentResolver();
			numberOfDeletedRecords = cr.delete(uri, where, selectionArgs);
		}
		return numberOfDeletedRecords;
	}

	public static int DeleteAllCheckedItemsInList(Context context, long listID) {
		int numberOfDeletedRecords = -1;
		if (listID > 1) {
			Uri uri = CONTENT_URI;
			String where = COL_LIST_ID + " = ? AND " + COL_CHECKED + " = ?";
			String selectionArgs[] = new String[] { String.valueOf(listID), String.valueOf(CHECKED_TRUE) };
			ContentResolver cr = context.getContentResolver();
			numberOfDeletedRecords = cr.delete(uri, where, selectionArgs);
		}
		return numberOfDeletedRecords;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	// SQLite Methods that use Dropbox records
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static long CreateItem(Context context, DbxRecord dbxRecord) {
		// a check to see if the item is already in the database
		// was done prior to making this call ... so don't repeat it.
		long newItemID = -1;

		ContentValues newFieldValues = setContentValues(dbxRecord);
		Uri uri = CONTENT_URI;
		ContentResolver cr = context.getContentResolver();
		Uri newItemUri = cr.insert(uri, newFieldValues);
		if (newItemUri != null) {
			newItemID = Long.parseLong(newItemUri.getLastPathSegment());
		}
		return newItemID;
	}

	public static Cursor getItemFromDropboxID(Context context, String dbxRecordID) {
		Uri uri = CONTENT_URI;
		String[] projection = PROJECTION_ALL;
		String selection = COL_ITEM_DROPBOX_ID + " = '" + dbxRecordID + "'";
		String selectionArgs[] = null;
		String sortOrder = SORT_ORDER_ITEM_NAME;

		ContentResolver cr = context.getContentResolver();
		Cursor cursor = null;
		try {
			cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
		} catch (Exception e) {
			MyLog.e("ItemsTable", "Exception error in getItemFromDropboxID:");
			e.printStackTrace();
		}
		return cursor;
	}

	public static Uri getItemUri(Context context, String dbxRecordID) {
		Uri itemUri = null;
		Cursor cursor = getItemFromDropboxID(context, dbxRecordID);
		if (cursor != null) {
			cursor.moveToFirst();
			long itemID = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ITEM_ID));
			itemUri = ContentUris.withAppendedId(ItemsTable.CONTENT_URI, itemID);
			cursor.close();
		}
		return itemUri;
	}

	public static String getDropboxID(Context context, long itemID) {
		String dbxID = "";
		Cursor cursor = getItem(context, itemID);
		if (cursor != null) {
			if (cursor.getCount() > 0) {
				cursor.moveToFirst();
				dbxID = cursor.getString(cursor.getColumnIndexOrThrow(COL_ITEM_DROPBOX_ID));
			}
			cursor.close();
		}

		return dbxID;
	}

	public static int UpdateItem(Context context, String dbxRecordID, DbxRecord dbxRecord) {
		int numberOfUpdatedRecords = -1;
		ContentResolver cr = context.getContentResolver();
		Uri itemUri = getItemUri(context, dbxRecordID);
		ContentValues newFieldValues = setContentValues(dbxRecord);
		String selection = null;
		String[] selectionArgs = null;
		numberOfUpdatedRecords = cr.update(itemUri, newFieldValues, selection, selectionArgs);

		return numberOfUpdatedRecords;
	}

	public static int DeleteItem(Context context, String dbxRecordID) {
		int numberOfDeletedRecords = -1;

		Uri itemUri = getItemUri(context, dbxRecordID);
		ContentResolver cr = context.getContentResolver();
		String where = null;
		String[] selectionArgs = null;
		numberOfDeletedRecords = cr.delete(itemUri, where, selectionArgs);

		return numberOfDeletedRecords;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Dropbox Datastore Methods
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static void dbxInsert(Context context, DbxDatastore dbxDatastore, long itemID) throws DbxException {
		ContentValues values = setContentValues(context, itemID);
		DbxRecord dbxRecord = dbxInsert(context, dbxDatastore, itemID, values);
		setDbxRecordValues(dbxRecord, values);
		dbxDatastore.sync();
	}

	public static void dbxInsertAllItems(Context context, DbxDatastore dbxDatastore, long listID) throws DbxException {
		Cursor itemsCursor = getAllItemsInListCursor(context, listID, null);
		if (itemsCursor != null) {
			while (itemsCursor.moveToNext()) {
				ContentValues values = setContentValues(context, itemsCursor);
				long itemID = itemsCursor.getLong(itemsCursor.getColumnIndexOrThrow(COL_ITEM_ID));
				DbxRecord dbxRecord = dbxInsert(context, dbxDatastore, itemID, values);
				setDbxRecordValues(dbxRecord, values);
			}
			dbxDatastore.sync();
			itemsCursor.close();
		}
	}

	public static DbxRecord dbxInsert(Context context, DbxDatastore dbxDatastore, long itemID, ContentValues values) {

		DbxRecord newItemRecord = null;
		if (dbxDatastore != null) {
			DbxTable dbxActiveTable = dbxDatastore.getTable(TABLE_ITEMS);

			if (dbxActiveTable != null) {
				Calendar now = Calendar.getInstance();
				long nowMillis = now.getTimeInMillis();

				Set<Entry<String, Object>> s = values.valueSet();
				Iterator<Entry<String, Object>> itr = s.iterator();
				while (itr.hasNext()) {
					Entry<String, Object> me = itr.next();
					String key = me.getKey().toString();

					if (key.equals(COL_ITEM_NAME)) {
						String itemName = (String) me.getValue();
						newItemRecord = dbxActiveTable.insert()
								.set(key, itemName)
								.set(COL_ITEM_NUMBER, -1)
								.set(COL_ITEM_NOTE, "")
								.set(COL_LIST_ID, 1)
								.set(COL_GROUP_ID, 1)
								.set(COL_SELECTED, false)
								.set(COL_STRUCK_OUT, false)
								.set(COL_CHECKED, false)
								.set(COL_MANUAL_SORT_ORDER, -1)
								.set(COL_MANUAL_SORT_SWITCH, 1)
								.set(COL_DATE_TIME_LAST_USED, -1);

						// update the SQLite record with the dbxID and nowMillis
						AListContentProvider.setSuppressDropboxChanges(true);
						String dbxID = newItemRecord.getId();
						ContentValues newFieldValues = new ContentValues();
						newFieldValues.put(COL_ITEM_DROPBOX_ID, dbxID);
						newFieldValues.put(COL_DATE_TIME_LAST_USED, nowMillis);
						UpdateItemFieldValues(context, itemID, newFieldValues);

						MyLog.d("ItemsTable: dbxInsert ", "Key:" + key + ", value:" + itemName);
						AListContentProvider.setSuppressDropboxChanges(false);

						// TODO: only needed for Locations Table!!!!!!!!
					} else if (key.equals(COL_ITEM_NUMBER)) {
						int itemNumber = (Integer) me.getValue();
						if (newItemRecord != null) {
							newItemRecord.set(key, itemNumber);
							MyLog.d("ItemsTable: dbxInsert ", "Key:" + key + ", value:" + itemNumber);
						}
					}
				}

				try {
					// update the dbx record with nowMillis
					if (newItemRecord != null) {
						newItemRecord.set(COL_DATE_TIME_LAST_USED, nowMillis);
					}
					dbxDatastore.sync();
				} catch (DbxException e) {
					MyLog.e("ItemsTable: dbxInsert ", "DbxException while trying dbxDatastore.sync().");
					e.printStackTrace();
				}
			}
		} else {
			MyLog.e("ItemsTable: dbxInsert ", "Unable to insert record. dbxDatastore is null!");
		}
		return newItemRecord;

	}

	public static void dbxDeleteSingleRecord(Context context, DbxDatastore dbxDatastore, String itemIDstring) {
		if (dbxDatastore != null) {
			DbxTable dbxActiveTable = dbxDatastore.getTable(TABLE_ITEMS);

			String dbxRecordID = getDropboxID(context, Long.parseLong(itemIDstring));
			if (!dbxRecordID.isEmpty()) {
				try {
					DbxRecord dbxRecord = dbxActiveTable.get(dbxRecordID);
					if (dbxRecord != null) {
						dbxRecord.deleteRecord();
						dbxDatastore.sync();
					}
				} catch (DbxException e) {
					MyLog.e("ItemsTable: dbxDeleteSingleRecord ", "DbxException while trying delete a dropbox record.");
				}
			}
		} else {
			MyLog.e("ItemsTable: dbxDeleteSingleRecord ", "Unable to delete record. dbxDatastore is null!");
		}
	}

	public static void dbxDeleteMultipleRecords(Context context, DbxDatastore dbxDatastore, Uri uri, String selection,
			String[] selectionArgs) {

		if (dbxDatastore != null) {
			DbxTable dbxActiveTable = dbxDatastore.getTable(TABLE_ITEMS);

			if (dbxActiveTable != null) {
				String projection[] = { COL_ITEM_ID, COL_ITEM_DROPBOX_ID };
				String sortOrder = null;
				String dbxID;
				DbxRecord dbxRecord;
				ContentResolver cr = context.getContentResolver();
				Cursor cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
				if (cursor != null) {
					try {
						while (cursor.moveToNext()) {
							dbxID = cursor.getString(cursor.getColumnIndexOrThrow(COL_ITEM_DROPBOX_ID));
							dbxRecord = dbxActiveTable.get(dbxID);
							if (dbxRecord != null) {
								dbxRecord.deleteRecord();
							}
						}

						dbxDatastore.sync();
					} catch (DbxException e) {
						MyLog.e("ItemsTable: dbxDeleteMultipleRecords ",
								"DbxException while trying to delete multiple dropbox records.");
						e.printStackTrace();

					} finally {
						cursor.close();
					}
				}
			}
		} else {
			MyLog.e("ItemsTable: dbxDeleteMultipleRecords ", "Unable to delete records. dbxDatastore is null!");
		}
	}

	public static void dbxDeleteAllRecords(DbxDatastore dbxDatastore) {
		if (dbxDatastore != null) {
			DbxTable dbxActiveTable = dbxDatastore.getTable(TABLE_ITEMS);
			if (dbxActiveTable != null) {
				try {
					DbxTable.QueryResult allRecords = dbxActiveTable.query();
					Iterator<DbxRecord> itr = allRecords.iterator();
					while (itr.hasNext()) {
						DbxRecord dbxRecord = itr.next();
						dbxRecord.deleteRecord();
					}

					dbxDatastore.sync();

				} catch (DbxException e) {
					MyLog.e("ItemsTable: dbxDeleteAllRecords ", "DbxException while deleteing all dropbox records.");
					e.printStackTrace();
				}
			}
		} else {
			MyLog.e("ItemsTable: dbxDeleteAllRecords ", "Unable to delete records. dbxDatastore is null!");
		}
	}

	public static int sqlDeleteAllRecords(Context context) {
		int numberOfDeletedRecords = -1;

		Uri uri = CONTENT_URI;
		String where = null;
		String selectionArgs[] = null;
		ContentResolver cr = context.getContentResolver();
		numberOfDeletedRecords = cr.delete(uri, where, selectionArgs);

		return numberOfDeletedRecords;
	}

	public static void dbxUpdateMultipleRecords(Context context, DbxDatastore dbxDatastore, ContentValues values,
			Uri uri, String selection, String[] selectionArgs) {

		if (dbxDatastore != null) {
			DbxTable dbxActiveTable = dbxDatastore.getTable(TABLE_ITEMS);

			if (dbxActiveTable != null) {
				String projection[] = { COL_ITEM_ID, COL_ITEM_DROPBOX_ID };
				String sortOrder = null;
				String dbxID;
				DbxRecord dbxRecord;
				ContentResolver cr = context.getContentResolver();
				Cursor cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
				if (cursor != null) {
					try {
						while (cursor.moveToNext()) {
							dbxID = cursor.getString(cursor.getColumnIndexOrThrow(COL_ITEM_DROPBOX_ID));
							dbxRecord = dbxActiveTable.get(dbxID);
							if (dbxRecord != null) {
								setDbxRecordValues(dbxRecord, values);
							}
						}

						dbxDatastore.sync();
					} catch (DbxException e) {
						MyLog.e("ItemsTable: dbxUpdateMultipleRecords ", "DbxException while trying update records.");
						e.printStackTrace();

					} finally {
						cursor.close();
					}
				}
			}
		} else {
			MyLog.e("ItemsTable: dbxUpdateMultipleRecords ", "Unable to update records. dbxDatastore is null!");
		}
	}

	public static void dbxUpdateSingleRecord(Context context, DbxDatastore dbxDatastore, ContentValues values, Uri uri) {
		if (dbxDatastore != null) {
			DbxTable dbxActiveTable = dbxDatastore.getTable(TABLE_ITEMS);

			if (dbxActiveTable != null) {
				String rowIDstring = uri.getLastPathSegment();
				String dbxRecordID = getDropboxID(context, Long.parseLong(rowIDstring));
				if (!dbxRecordID.isEmpty()) {
					try {
						DbxRecord dbxRecord = dbxActiveTable.get(dbxRecordID);
						if (dbxRecord != null) {
							setDbxRecordValues(dbxRecord, values);
							dbxDatastore.sync();
						} else {
							// the dbxItem has been deleted ...
							// but for some reason it has not been deleted from the sql database
							// so delete it now.
							sqlDeleteItemAlreadyDeletedFromDropbox(context, dbxRecordID);
							// sync to hopefully capture other dropbox changes
							dbxDatastore.sync();
						}

					} catch (DbxException e) {
						MyLog.e("ItemsTable: dbxUpdateSingleRecord ", "DbxException while trying update records.");
						e.printStackTrace();
					}
				}
			}
		} else {
			MyLog.e("ItemsTable: dbxUpdateSingleRecord ", "Unable to update record. dbxDatastore is null!");
		}
	}

	private static void sqlDeleteItemAlreadyDeletedFromDropbox(Context context, String dbxRecordID) {
		AListContentProvider.setSuppressDropboxChanges(true);
		DeleteItem(context, dbxRecordID);
		AListContentProvider.setSuppressDropboxChanges(false);
	}

	private static ContentValues setContentValues(Context context, Cursor cursor) {
		ContentValues newFieldValues = new ContentValues();
		if (cursor != null) {
			for (String col : PROJECTION_ALL) {
				if (col.equals(COL_ITEM_ID) || col.equals(COL_ITEM_DROPBOX_ID)) {
					// do nothing

				} else if (col.equals(COL_ITEM_NAME) || col.equals(COL_ITEM_NOTE)) {
					String value = cursor.getString(cursor.getColumnIndexOrThrow(col));
					newFieldValues.put(col, value);

				} else if (col.equals(COL_LIST_ID) || col.equals(COL_GROUP_ID) || col.equals(COL_DATE_TIME_LAST_USED)) {
					long value = cursor.getLong(cursor.getColumnIndexOrThrow(col));
					newFieldValues.put(col, value);

				} else {
					int value = cursor.getInt(cursor.getColumnIndexOrThrow(col));
					newFieldValues.put(col, value);
				}
			}
		}
		return newFieldValues;
	}

	private static ContentValues setContentValues(Context context, long itemID) {
		ContentValues newFieldValues = null;
		Cursor cursor = getItem(context, itemID);
		if (cursor != null) {
			cursor.moveToFirst();
			newFieldValues = setContentValues(context, cursor);
			cursor.close();
		}
		return newFieldValues;
	}

	private static ContentValues setContentValues(DbxRecord dbxRecord) {
		ContentValues newFieldValues = new ContentValues();

		if (dbxRecord != null) {
			String dbxID = dbxRecord.getId();
			newFieldValues.put(COL_ITEM_DROPBOX_ID, dbxID);

			if (dbxRecord.hasField(COL_ITEM_NAME)) {
				String itemName = dbxRecord.getString(COL_ITEM_NAME);
				newFieldValues.put(COL_ITEM_NAME, itemName);
			}

			if (dbxRecord.hasField(COL_ITEM_NUMBER)) {
				int itemNumber = (int) dbxRecord.getLong(COL_ITEM_NUMBER);
				newFieldValues.put(COL_ITEM_NUMBER, itemNumber);
			}

			if (dbxRecord.hasField(COL_ITEM_NOTE)) {
				String itemNote = dbxRecord.getString(COL_ITEM_NOTE);
				newFieldValues.put(COL_ITEM_NOTE, itemNote);
			}

			if (dbxRecord.hasField(COL_LIST_ID)) {
				long listID = dbxRecord.getLong(COL_LIST_ID);
				newFieldValues.put(COL_LIST_ID, listID);
			}
			if (dbxRecord.hasField(COL_GROUP_ID)) {
				long groupID = dbxRecord.getLong(COL_GROUP_ID);
				newFieldValues.put(COL_GROUP_ID, groupID);
			}

			if (dbxRecord.hasField(COL_SELECTED)) {
				boolean selected = dbxRecord.getBoolean(COL_SELECTED);
				int selectedValue = 0;
				if (selected) {
					selectedValue = 1;
				}
				newFieldValues.put(COL_SELECTED, selectedValue);
			}

			if (dbxRecord.hasField(COL_STRUCK_OUT)) {
				boolean struckOut = dbxRecord.getBoolean(COL_STRUCK_OUT);
				int struckOutValue = 0;
				if (struckOut) {
					struckOutValue = 1;
				}
				newFieldValues.put(COL_STRUCK_OUT, struckOutValue);
			}

			if (dbxRecord.hasField(COL_CHECKED)) {
				boolean checked = dbxRecord.getBoolean(COL_CHECKED);
				int checkedValue = 0;
				if (checked) {
					checkedValue = 1;
				}
				newFieldValues.put(COL_CHECKED, checkedValue);
			}

			if (dbxRecord.hasField(COL_MANUAL_SORT_ORDER)) {
				int sortOrder = (int) dbxRecord.getLong(COL_MANUAL_SORT_ORDER);
				newFieldValues.put(COL_MANUAL_SORT_ORDER, sortOrder);
			}

			if (dbxRecord.hasField(COL_MANUAL_SORT_SWITCH)) {
				boolean checked = dbxRecord.getBoolean(COL_MANUAL_SORT_SWITCH);
				int checkedValue = 0;
				if (checked) {
					checkedValue = 1;
				}
				newFieldValues.put(COL_MANUAL_SORT_SWITCH, checkedValue);
			}

			if (dbxRecord.hasField(COL_DATE_TIME_LAST_USED)) {
				long lastUsed = dbxRecord.getLong(COL_DATE_TIME_LAST_USED);
				newFieldValues.put(COL_DATE_TIME_LAST_USED, lastUsed);
			}
		}
		return newFieldValues;
	}

	private static void setDbxRecordValues(DbxRecord dbxRecord, ContentValues values) {
		if (dbxRecord != null) {
			Set<Entry<String, Object>> s = values.valueSet();
			Iterator<Entry<String, Object>> itr = s.iterator();
			while (itr.hasNext()) {
				Entry<String, Object> me = itr.next();
				String key = me.getKey().toString();

				if (key.equals(COL_ITEM_NAME)) {
					String itemName = (String) me.getValue();
					if (itemName == null) {
						itemName = "";
					}
					dbxRecord.set(key, itemName);

				} else if (key.equals(COL_ITEM_NUMBER)) {
					int itemNumber = (Integer) me.getValue();
					dbxRecord.set(key, itemNumber);

				} else if (key.equals(COL_ITEM_NOTE)) {
					String itemNote = (String) me.getValue();
					if (itemNote == null) {
						itemNote = "";
					}
					dbxRecord.set(key, itemNote);

				} else if (key.equals(COL_LIST_ID)) {
					long listID = (Long) me.getValue();
					dbxRecord.set(key, listID);

				} else if (key.equals(COL_GROUP_ID)) {
					long groupID = (Long) me.getValue();
					dbxRecord.set(key, groupID);

				} else if (key.equals(COL_SELECTED)) {
					int selectedValue = (Integer) me.getValue();
					boolean selected = false;
					if (selectedValue == 1) {
						selected = true;
					}
					dbxRecord.set(key, selected);

				} else if (key.equals(COL_STRUCK_OUT)) {
					int itemStrikoutValue = (Integer) me.getValue();
					boolean itemStrikout = false;
					if (itemStrikoutValue == 1) {
						itemStrikout = true;
					}
					dbxRecord.set(key, itemStrikout);

				} else if (key.equals(COL_CHECKED)) {
					int itemCheckedValue = (Integer) me.getValue();
					boolean itemChecked = false;
					if (itemCheckedValue == 1) {
						itemChecked = true;
					}
					dbxRecord.set(key, itemChecked);

				} else if (key.equals(COL_MANUAL_SORT_ORDER)) {
					int manualSortOrderValue = (Integer) me.getValue();
					dbxRecord.set(key, manualSortOrderValue);

				} else if (key.equals(COL_MANUAL_SORT_SWITCH)) {
					int manualSortSwitchValue = (Integer) me.getValue();
					boolean manualSortSwitch = false;
					if (manualSortSwitchValue == 1) {
						manualSortSwitch = true;
					}
					dbxRecord.set(key, manualSortSwitch);

				} else if (key.equals(COL_DATE_TIME_LAST_USED)) {
					long lastDate = (Long) me.getValue();
					dbxRecord.set(key, lastDate);

				} else {
					MyLog.e("ItemsTable: setDbxRecordValues ", "Unknown column name:" + key);
				}
			}
		}
	}

	public static void replaceSqlRecordsWithDbxRecords(Context context, DbxDatastore dbxDatastore) {

		if (dbxDatastore != null) {
			DbxTable dbxActiveTable = dbxDatastore.getTable(TABLE_ITEMS);
			if (dbxActiveTable != null) {
				try {
					DbxTable.QueryResult allRecords = dbxActiveTable.query();
					Iterator<DbxRecord> itr = allRecords.iterator();
					while (itr.hasNext()) {
						DbxRecord dbxRecord = itr.next();
						CreateItem(context, dbxRecord);
					}

				} catch (DbxException e) {
					MyLog.e("ItemsTable: replaceSqlRecordsWithDbxRecords ",
							"DbxException while replacing all sql records.");
					e.printStackTrace();
				}
			}

		} else {
			MyLog.e("ItemsTable: replaceSqlRecordsWithDbxRecords ",
					"Unable to replace sql records. dbxDatastore is null!");
		}
	}

	public static void validateSqlRecords(Context context, DbxTable dbxTable) {
		if (dbxTable != null) {

			// Iterate thru the SQL table records and verify if the SQL record exists in the Dbx table.
			// If not ... delete the SQL table record
			Cursor allItemsCursor = getAllDbxItemsCursor(context);
			String dbxRecordID = "";
			long sqlRecordID = -1;
			DbxRecord dbxRecord = null;
			if (allItemsCursor != null && allItemsCursor.getCount() > 0) {
				while (allItemsCursor.moveToNext()) {

					try {
						dbxRecordID = allItemsCursor.getString(allItemsCursor
								.getColumnIndexOrThrow(COL_ITEM_DROPBOX_ID));
						dbxRecord = dbxTable.get(dbxRecordID);
						if (dbxRecord == null) {
							// the SQL table record does not exist in the Dbx table ... so delete it.
							sqlRecordID = allItemsCursor.getLong(allItemsCursor.getColumnIndexOrThrow(COL_ITEM_ID));
							DeleteItem(context, sqlRecordID);
						}
					} catch (DbxException e) {
						MyLog.e("ItemsTable: validateSqlRecords ", "DbxException while iterating thru SQL table.");
						e.printStackTrace();
					}

				}

				// Iterate thru the dbxTable updating or creating SQL records
				try {
					DbxTable.QueryResult allRecords = dbxTable.query();
					Iterator<DbxRecord> itr = allRecords.iterator();
					while (itr.hasNext()) {
						dbxRecord = itr.next();
						dbxRecordID = dbxRecord.getId();
						Cursor itemCursor = getItemFromDropboxID(context, dbxRecordID);
						if (itemCursor != null && itemCursor.getCount() > 0) {
							// update the existing record
							UpdateItem(context, dbxRecordID, dbxRecord);
						} else {
							// create a new record
							CreateItem(context, dbxRecord);
						}
						if (itemCursor != null) {
							itemCursor.close();
						}
					}
				} catch (DbxException e) {
					MyLog.e("ItemsTable: validateSqlRecords ", "DbxException while iterating thru DbxTable.");
					e.printStackTrace();
				}
			}

			if (allItemsCursor != null) {
				allItemsCursor.close();
			}
		}

	}

}
