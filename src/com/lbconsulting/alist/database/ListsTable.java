package com.lbconsulting.alist.database;

import java.util.ArrayList;
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

public class ListsTable {

	// ListsTable data table
	// Version 1
	public static final String TABLE_LISTS = "tblLists";
	public static final String COL_LIST_ID = "_id";
	public static final String COL_LIST_DROPBOX_ID = "listDropboxID";
	public static final String COL_LIST_TITLE = "listTitle";
	public static final String COL_ACTIVE_STORE_ID = "activeStoreID";
	// List Settings

	public static final String COL_DELETE_NOTE_UPON_DESELECTING_ITEM = "deleteNoteUponDeslectingItem";

	public static final String COL_LIST_SORT_ORDER = "listSortOrder";
	public static final String COL_MASTER_LIST_SORT_ORDER = "masterListSortOrder";

	public static final String COL_ALLOW_GROUP_ADDITIONS = "allowGroupAdditions";

	public static final String COL_TITLE_BACKGROUND_COLOR = "titleBackgroundColor";
	public static final String COL_TITLE_TEXT_COLOR = "titleTextColor";

	public static final String COL_SEPARATOR_BACKGROUND_COLOR = "separatorBackgroundColor";
	public static final String COL_SEPARATOR_TEXT_COLOR = "separatorTextColor";

	public static final String COL_LIST_BACKGROUND_COLOR = "listBackgroundColor";
	public static final String COL_ITEM_NORMAL_TEXT_COLOR = "itemNormalTextColor";
	public static final String COL_ITEM_STRIKEOUT_TEXT_COLOR = "itemStrikeoutTextColor";

	public static final String COL_MASTER_LIST_BACKGROUND_COLOR = "masterListBackgroundColor";
	public static final String COL_MASTER_LIST_ITEM_NORMAL_TEXT_COLOR = "masterListItemNormalTextColor";
	public static final String COL_MASTER_LIST_ITEM_SELECTED_TEXT_COLOR = "masterListItemStrikeoutTextColor";

	public static final String COL_LISTVIEW_FIRST_VISIBLE_POSITION = "listViewFirstVisiblePosition";
	public static final String COL_LISTVIEW_TOP = "listViewTop";

	public static final String COL_MASTER_LISTVIEW_FIRST_VISIBLE_POSITION = "masterListView1stVisPosition";
	public static final String COL_MASTER_LISTVIEW_TOP = "masterListViewTop";

	public static final String COL_MANAGE_ITEMS_GROUP_ID = "manageItemsGroupID";

	public static final String COL_IS_SYNCED_TO_DROPBOX = "isSyncedToDropbox";
	public static final String COL_IS_LIST_PREF_SYNCED_TO_DROPBOX = "isListPreferencesSyncedToDropbox";
	public static final String COL_IS_FIRST_TIME_SYNC = "isFirstTimeSync";

	public static final String[] PROJECTION_ALL = { COL_LIST_ID, COL_LIST_DROPBOX_ID, COL_LIST_TITLE,
			COL_ACTIVE_STORE_ID,
			COL_DELETE_NOTE_UPON_DESELECTING_ITEM,
			COL_LIST_SORT_ORDER, COL_MASTER_LIST_SORT_ORDER,
			COL_ALLOW_GROUP_ADDITIONS,
			COL_TITLE_BACKGROUND_COLOR, COL_TITLE_TEXT_COLOR,
			COL_SEPARATOR_BACKGROUND_COLOR, COL_SEPARATOR_TEXT_COLOR,
			COL_LIST_BACKGROUND_COLOR, COL_ITEM_NORMAL_TEXT_COLOR, COL_ITEM_STRIKEOUT_TEXT_COLOR,
			COL_MASTER_LIST_BACKGROUND_COLOR, COL_MASTER_LIST_ITEM_NORMAL_TEXT_COLOR,
			COL_MASTER_LIST_ITEM_SELECTED_TEXT_COLOR,
			COL_LISTVIEW_FIRST_VISIBLE_POSITION, COL_LISTVIEW_TOP,
			COL_MASTER_LISTVIEW_FIRST_VISIBLE_POSITION, COL_MASTER_LISTVIEW_TOP,
			COL_MANAGE_ITEMS_GROUP_ID,
			COL_IS_SYNCED_TO_DROPBOX, COL_IS_LIST_PREF_SYNCED_TO_DROPBOX, COL_IS_FIRST_TIME_SYNC
	};

	public static final String CONTENT_PATH = TABLE_LISTS;
	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + "vnd.lbconsulting."
			+ TABLE_LISTS;
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + "vnd.lbconsulting."
			+ TABLE_LISTS;
	public static final Uri CONTENT_URI = Uri.parse("content://" + AListContentProvider.AUTHORITY + "/" + CONTENT_PATH);

	public static final String SORT_ORDER_LIST_TITLE = COL_LIST_TITLE + " ASC";

	private final static long DEFAULT_LIST_PREFERENCES = 1;

	// Database creation SQL statements
	private static final String DATATABLE_CREATE = "create table "
			+ TABLE_LISTS
			+ " ("
			+ COL_LIST_ID + " integer primary key autoincrement, "
			+ COL_LIST_DROPBOX_ID + " text, "
			+ COL_LIST_TITLE + " text collate nocase, "
			+ COL_ACTIVE_STORE_ID + " integer not null references "
			+ StoresTable.TABLE_STORES + " (" + StoresTable.COL_STORE_ID + ") default 1, " // default [No Store]

			// List Settings

			+ COL_DELETE_NOTE_UPON_DESELECTING_ITEM + " integer default 1, " // default true = 1

			+ COL_LIST_SORT_ORDER + " integer default 0, " // default Alphabetically = 0
			+ COL_MASTER_LIST_SORT_ORDER + " integer default 0, " // default Alphabetically = 0

			+ COL_ALLOW_GROUP_ADDITIONS + " integer default 1, " // default true = 1

			+ COL_TITLE_BACKGROUND_COLOR + " integer default -1, "
			+ COL_TITLE_TEXT_COLOR + " integer default -1, "

			+ COL_SEPARATOR_BACKGROUND_COLOR + " integer default -1, "
			+ COL_SEPARATOR_TEXT_COLOR + " integer default -1, "

			+ COL_LIST_BACKGROUND_COLOR + " integer default -1, "
			+ COL_ITEM_NORMAL_TEXT_COLOR + " integer default -1, "
			+ COL_ITEM_STRIKEOUT_TEXT_COLOR + " integer default -1, "

			+ COL_MASTER_LIST_BACKGROUND_COLOR + " integer default -1, "
			+ COL_MASTER_LIST_ITEM_NORMAL_TEXT_COLOR + " integer default -1, "
			+ COL_MASTER_LIST_ITEM_SELECTED_TEXT_COLOR + " integer default -1, "

			+ COL_LISTVIEW_FIRST_VISIBLE_POSITION + " integer default 0, "
			+ COL_LISTVIEW_TOP + " integer default 0,"

			+ COL_MASTER_LISTVIEW_FIRST_VISIBLE_POSITION + " integer default 0, "
			+ COL_MASTER_LISTVIEW_TOP + " integer default 0, "

			+ COL_MANAGE_ITEMS_GROUP_ID + " integer default 0, "

			+ COL_IS_SYNCED_TO_DROPBOX + " integer default 0, "
			+ COL_IS_LIST_PREF_SYNCED_TO_DROPBOX + " integer default 0, "
			+ COL_IS_FIRST_TIME_SYNC + " integer default 0"
			+ ");";

	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(DATATABLE_CREATE);
		MyLog.i("ListsTable", "onCreate: " + TABLE_LISTS + " created.");

		String insertProjection = "insert into "
				+ TABLE_LISTS
				+ " ("
				+ COL_LIST_ID + ", "
				+ COL_LIST_TITLE
				+ ") VALUES ";

		ArrayList<String> sqlStatements = new ArrayList<String>();
		// List 1 used as the default List Preferences
		sqlStatements.add(insertProjection + "(NULL, 'Default List Preferences')");
		AListUtilities.execMultipleSQL(database, sqlStatements);
	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		MyLog.w(TABLE_LISTS, "Upgrading database from version " + oldVersion + " to version " + newVersion);
		int upgradeToVersion = oldVersion + 1;
		switch (upgradeToVersion) {
		// fall through each case to upgrade to the newVersion
			case 2:
			case 3:
			case 4:
				// No changes in TABLE_LISTS
				break;

			default:
				// upgrade version not found!
				MyLog.e(TABLE_LISTS, "Upgrade version " + newVersion + " not found!");
				database.execSQL("DROP TABLE IF EXISTS " + TABLE_LISTS);
				onCreate(database);
				break;
		}
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Create Methods
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	public static long CreateNewList(Context context, String listTitle) {
		long newListID = -1;
		if (listTitle != null) {
			listTitle = listTitle.trim();
			if (!listTitle.isEmpty()) {

				Cursor cursor = getList(context, listTitle);
				if (cursor != null && cursor.getCount() > 0) {
					// listTitle already exists in the table
					cursor.moveToFirst();
					newListID = cursor.getLong(cursor.getColumnIndexOrThrow(COL_LIST_ID));
					cursor.close();
				} else {
					// listTitle does not exists in the table ... so add it
					ContentResolver cr = context.getContentResolver();
					Uri uri = CONTENT_URI;
					ContentValues values = new ContentValues();
					values.put(COL_LIST_TITLE, listTitle);
					try {
						Uri newListUri = cr.insert(uri, values);
						if (newListUri != null) {
							newListID = Long.parseLong(newListUri.getLastPathSegment());
						}
					} catch (Exception e) {
						MyLog.e("Exception error in CreateNewList. ", "");
						e.printStackTrace();
					}
				}

				if (cursor != null) {
					cursor.close();
				}

			} else {
				MyLog.e("ListTitlesTable", "Error in CreateNewList; groupName is Empty!");
			}

		} else {
			MyLog.e("ListTitlesTable", "Error in CreateNewList; groupName is Null!");
		}
		return newListID;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Read Methods
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static Cursor getList(Context context, long listID) {
		Cursor cursor = null;

		Uri uri = Uri.withAppendedPath(CONTENT_URI, String.valueOf(listID));
		String[] projection = PROJECTION_ALL;
		String selection = null;
		String selectionArgs[] = null;
		String sortOrder = null;

		ContentResolver cr = context.getContentResolver();

		try {
			if (listID > 0) {
				cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
			}
		} catch (Exception e) {
			MyLog.e("Exception error in ListTitlesTable: getList. ", "");
			e.printStackTrace();
		}
		return cursor;
	}

	public static Cursor getList(Context context, String listTitle) {
		Cursor cursor = null;
		if (listTitle != null) {
			listTitle = listTitle.trim();
			if (!listTitle.isEmpty()) {
				Uri uri = CONTENT_URI;
				String[] projection = PROJECTION_ALL;
				String selection = COL_LIST_TITLE + " = ?";
				String selectionArgs[] = { listTitle };
				String sortOrder = null;
				ContentResolver cr = context.getContentResolver();
				try {
					cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
				} catch (Exception e) {
					MyLog.e("Exception error in ListTitlesTable: getList. ", "");
					e.printStackTrace();
				}
			} else {
				MyLog.e("ListTitlesTable", "Error in getList; groupName is Empty!");
			}
		} else {
			MyLog.e("ListTitlesTable", "Error in getList; groupName is Null!");
		}
		return cursor;
	}

	public static Cursor getAllLists(Context context) {
		Uri uri = CONTENT_URI;
		String[] projection = PROJECTION_ALL;
		String selection = COL_LIST_ID + "> ?";
		String[] selectionArgs = { String.valueOf(DEFAULT_LIST_PREFERENCES) }; // DEFAULT_LIST_PREFERENCES=1
		String sortOrder = SORT_ORDER_LIST_TITLE;

		ContentResolver cr = context.getContentResolver();
		Cursor cursor = null;
		try {
			cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
		} catch (Exception e) {
			MyLog.e("Exception error in ListTitlesTable: getAllLists. ", "");
			e.printStackTrace();
		}
		return cursor;
	}

	public static CursorLoader loadAllLists(Context context) {
		CursorLoader cursorLoader = null;
		Uri uri = CONTENT_URI;
		String[] projection = { COL_LIST_ID, COL_LIST_TITLE };
		String selection = COL_LIST_ID + "> ?";
		String[] selectionArgs = { String.valueOf(DEFAULT_LIST_PREFERENCES) }; // DEFAULT_LIST_PREFERENCES=1
		String sortOrder = SORT_ORDER_LIST_TITLE;
		try {
			cursorLoader = new CursorLoader(context, uri, projection, selection, selectionArgs, sortOrder);
		} catch (Exception e) {
			MyLog.e("Exception error  in ListsTable: loadAllLists. ", "");
			e.printStackTrace();
		}
		return cursorLoader;
	}

	public static CursorLoader getMoveItemListSelection(Context context, long activeListID) {
		CursorLoader cursorLoader = null;
		Uri uri = CONTENT_URI;
		String[] projection = { COL_LIST_ID, COL_LIST_TITLE };
		String selection = COL_LIST_ID + "> ? AND " + COL_LIST_ID + " !=  ?";
		String[] selectionArgs = { String.valueOf(DEFAULT_LIST_PREFERENCES), String.valueOf(activeListID) }; // DEFAULT_LIST_PREFERENCES=1
		String sortOrder = SORT_ORDER_LIST_TITLE;
		try {
			cursorLoader = new CursorLoader(context, uri, projection, selection, selectionArgs, sortOrder);
		} catch (Exception e) {
			MyLog.e("Exception error in ListsTable: getMoveItemListSelection. ", "");
			e.printStackTrace();
		}
		return cursorLoader;
	}

	public static int getNumberOfLists(Context context) {
		int numberOfLists = -1;
		Cursor cursor = getAllLists(context);
		if (cursor != null) {
			cursor.moveToFirst();
			numberOfLists = cursor.getCount();
			cursor.close();
		}
		return numberOfLists;
	}

	public static String getListTitle(Context context, long listID) {
		String listTitle = "";
		Cursor cursor = getList(context, listID);
		if (cursor != null) {
			cursor.moveToFirst();
			listTitle = cursor.getString(cursor.getColumnIndexOrThrow(COL_LIST_TITLE));
			cursor.close();
		}
		return listTitle;
	}

	public static Cursor getDefaultListPreferencesCursor(Context context) {
		Uri uri = CONTENT_URI;
		String[] projection = PROJECTION_ALL;
		String selection = COL_LIST_ID + "= ?";
		String[] selectionArgs = { String.valueOf(DEFAULT_LIST_PREFERENCES) };
		String sortOrder = SORT_ORDER_LIST_TITLE;

		ContentResolver cr = context.getContentResolver();
		Cursor cursor = null;
		try {
			cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
		} catch (Exception e) {
			MyLog.e("Exception error in ListTitlesTable: getDefaultListPreferencesCursor. ", "");
			e.printStackTrace();
		}
		return cursor;
	}

	@SuppressWarnings("resource")
	public static long getFirstListID(Context context) {
		long firstListID = -1;
		Cursor allListsCursor = getAllLists(context);
		if (allListsCursor != null && allListsCursor.getCount() > 0) {
			allListsCursor.moveToFirst();
			firstListID = allListsCursor.getLong(allListsCursor.getColumnIndexOrThrow(COL_LIST_ID));
			allListsCursor.close();
		}
		return firstListID;
	}

	public static boolean isGroupAdditonAllowed(Context context, long listID) {
		boolean result = true;
		Cursor cursor = getList(context, listID);
		if (cursor != null) {
			cursor.moveToFirst();
			int value = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ALLOW_GROUP_ADDITIONS));
			result = value == 1;
			cursor.close();
		}
		return result;
	}

	public static boolean getDeleteNoteUponDeslectingItem(Context context, long listID) {
		boolean results = false;
		Cursor cursor = getList(context, listID);
		if (cursor != null) {
			cursor.moveToFirst();
			int value = cursor.getInt(cursor.getColumnIndexOrThrow(COL_DELETE_NOTE_UPON_DESELECTING_ITEM));
			results = value > 0;
			cursor.close();
		}
		return results;
	}

	public static boolean isAnyListSyncedToDropBox(Context context) {
		boolean syncedToDropbox = false;
		Cursor cursor = null;
		Uri uri = CONTENT_URI;
		String[] projection = new String[] { COL_LIST_ID };
		String selection = COL_IS_SYNCED_TO_DROPBOX + " = ?";
		String selectionArgs[] = { String.valueOf(1) };
		String sortOrder = null;
		ContentResolver cr = context.getContentResolver();
		try {
			cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
			if (cursor != null && cursor.getCount() > 0) {
				syncedToDropbox = true;
			}
		} catch (Exception e) {
			MyLog.e("Exception error in ListTitlesTable: getList. ", "");
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return syncedToDropbox;
	}

	public static Cursor getFirstTimeSyncLists(Context context) {
		Cursor cursor = null;

		Uri uri = CONTENT_URI;
		String[] projection = PROJECTION_ALL;
		String selection = COL_IS_FIRST_TIME_SYNC + " = ?";
		String selectionArgs[] = new String[] { String.valueOf(1) };
		String sortOrder = null;
		ContentResolver cr = context.getContentResolver();
		try {
			cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
		} catch (Exception e) {
			MyLog.e("Exception error in ListTitlesTable: getFirstTimeSyncLists. ", "");
			e.printStackTrace();
		}

		return cursor;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Update Methods
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static int UpdateListsTableFieldValues(Context context, long listID, ContentValues newFieldValues) {
		int numberOfUpdatedRecords = -1;
		if (listID > 1) {
			ContentResolver cr = context.getContentResolver();
			Uri defaultUri = Uri.withAppendedPath(CONTENT_URI, String.valueOf(listID));
			String selection = null;
			String[] selectionArgs = null;
			numberOfUpdatedRecords = cr.update(defaultUri, newFieldValues, selection, selectionArgs);
		}
		return numberOfUpdatedRecords;
	}

	public static void setListPreferencesDefaults(Context context, long listID) {
		if (listID > 0) {
			ContentResolver cr = context.getContentResolver();

			Cursor defaultCursor = getList(context, 1);
			if (defaultCursor != null) {
				defaultCursor.moveToFirst();
				ContentValues newListValues = new ContentValues();

				newListValues
						.put(COL_DELETE_NOTE_UPON_DESELECTING_ITEM,
								defaultCursor.getInt(defaultCursor
										.getColumnIndexOrThrow(COL_DELETE_NOTE_UPON_DESELECTING_ITEM)));

				newListValues.put(COL_LIST_SORT_ORDER,
						defaultCursor.getInt(defaultCursor.getColumnIndexOrThrow(COL_LIST_SORT_ORDER)));

				newListValues.put(COL_MASTER_LIST_SORT_ORDER,
						defaultCursor.getInt(defaultCursor.getColumnIndexOrThrow(COL_MASTER_LIST_SORT_ORDER)));

				Uri defaultUri = Uri.withAppendedPath(CONTENT_URI, String.valueOf(listID));
				String selection = null;
				String[] selectionArgs = null;
				int numberOfUpdatedRecords = cr.update(defaultUri, newListValues, selection, selectionArgs);
				if (numberOfUpdatedRecords != 1) {
					MyLog.e("ListTitlesTable: setListPreferencesDefaults", "The number of records does not equal 1!");
				}
				defaultCursor.close();
			}
		}
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Delete Methods
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static int DeleteList(Context context, long listID) {
		int numberOfDeletedRecords = -1;
		if (listID > 1) {
			// don't need to delete Location records because they are not associated with a list
			BridgeTable.DeleteAllBridgeRowsInList(context, listID);
			GroupsTable.DeleteAllGroupsInList(context, listID);
			ItemsTable.DeleteAllItemsInList(context, listID);
			StoresTable.DeleteAllStoresInList(context, listID);

			ContentResolver cr = context.getContentResolver();
			Uri uri = CONTENT_URI;
			String where = COL_LIST_ID + " = ?";
			String[] selectionArgs = { String.valueOf(listID) };
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
		long newlistID = -1;

		ContentValues newFieldValues = setContentValues(dbxRecord);
		Uri uri = CONTENT_URI;
		ContentResolver cr = context.getContentResolver();
		Uri newItemUri = cr.insert(uri, newFieldValues);
		if (newItemUri != null) {
			newlistID = Long.parseLong(newItemUri.getLastPathSegment());
		}
		return newlistID;
	}

	public static Cursor getListFromDropboxID(Context context, String dbxRecordID) {
		Uri uri = CONTENT_URI;
		String[] projection = PROJECTION_ALL;
		String selection = COL_LIST_DROPBOX_ID + " = '" + dbxRecordID + "'";
		String selectionArgs[] = null;
		String sortOrder = SORT_ORDER_LIST_TITLE;

		ContentResolver cr = context.getContentResolver();
		Cursor cursor = null;
		try {
			cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
		} catch (Exception e) {
			MyLog.e("ListsTable", "Exception error in getListFromDropboxID:");
			e.printStackTrace();
		}
		return cursor;
	}

	public static Uri getListUri(Context context, String dbxRecordID) {
		Uri itemUri = null;
		Cursor cursor = getListFromDropboxID(context, dbxRecordID);
		if (cursor != null) {
			cursor.moveToFirst();
			long listID = cursor.getLong(cursor.getColumnIndexOrThrow(COL_LIST_ID));
			itemUri = ContentUris.withAppendedId(ListsTable.CONTENT_URI, listID);
			cursor.close();
		}
		return itemUri;
	}

	public static String getDropboxID(Context context, long listID) {
		String dbxID = "";
		Cursor cursor = getList(context, listID);
		if (cursor != null) {
			if (cursor.getCount() > 0) {
				cursor.moveToFirst();
				dbxID = cursor.getString(cursor.getColumnIndexOrThrow(COL_LIST_DROPBOX_ID));
			}
			cursor.close();
		}

		return dbxID;
	}

	public static int UpdateList(Context context, String dbxRecordID, DbxRecord dbxRecord) {
		int numberOfUpdatedRecords = -1;
		ContentResolver cr = context.getContentResolver();
		Uri itemUri = getListUri(context, dbxRecordID);
		ContentValues newFieldValues = setContentValues(dbxRecord);
		String selection = null;
		String[] selectionArgs = null;
		numberOfUpdatedRecords = cr.update(itemUri, newFieldValues, selection, selectionArgs);

		return numberOfUpdatedRecords;
	}

	public static int DeleteList(Context context, String dbxRecordID) {
		int numberOfDeletedRecords = -1;
		Uri itemUri = getListUri(context, dbxRecordID);
		long listID = ContentUris.parseId(itemUri);

		numberOfDeletedRecords = DeleteList(context, listID);

		return numberOfDeletedRecords;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Dropbox Datastore Methods
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static void dbxInsert(Context context, DbxDatastore dbxDatastore, long listID) throws DbxException {
		ContentValues values = setContentValues(context, listID);
		DbxRecord dbxRecord = dbxInsert(context, dbxDatastore, listID, values);
		setDbxRecordValues(dbxRecord, values);
		dbxDatastore.sync();
	}

	public static DbxRecord dbxInsert(Context context, DbxDatastore dbxDatastore, long listID, ContentValues values)
			throws DbxException {
		// This method create a new dbx List record with default values except for the list title.
		// It quits after finding the list title from the content values
		// To update the dbx feilds after the dbx list record has been created, use setDbxRecordValues
		DbxRecord newItemRecord = null;
		if (dbxDatastore != null) {
			DbxTable dbxActiveTable = dbxDatastore.getTable(TABLE_LISTS);

			if (dbxActiveTable != null) {

				Set<Entry<String, Object>> s = values.valueSet();
				Iterator<Entry<String, Object>> itr = s.iterator();
				while (itr.hasNext()) {
					Entry<String, Object> me = itr.next();
					String key = me.getKey().toString();

					if (key.equals(COL_LIST_TITLE)) {
						String listTitle = (String) me.getValue();
						newItemRecord = dbxActiveTable.insert()
								.set(key, listTitle)
								.set(COL_ACTIVE_STORE_ID, 1)
								.set(COL_DELETE_NOTE_UPON_DESELECTING_ITEM, 1)
								.set(COL_LIST_SORT_ORDER, 0)
								.set(COL_MASTER_LIST_SORT_ORDER, 0)
								.set(COL_ALLOW_GROUP_ADDITIONS, 1)
								.set(COL_TITLE_BACKGROUND_COLOR, -1)
								.set(COL_TITLE_TEXT_COLOR, -1)
								.set(COL_SEPARATOR_BACKGROUND_COLOR, -1)
								.set(COL_SEPARATOR_TEXT_COLOR, -1)
								.set(COL_LIST_BACKGROUND_COLOR, -1)
								.set(COL_ITEM_NORMAL_TEXT_COLOR, -1)
								.set(COL_ITEM_STRIKEOUT_TEXT_COLOR, -1)
								.set(COL_MASTER_LIST_BACKGROUND_COLOR, -1)
								.set(COL_MASTER_LIST_ITEM_NORMAL_TEXT_COLOR, -1)
								.set(COL_MASTER_LIST_ITEM_SELECTED_TEXT_COLOR, -1)
								.set(COL_LISTVIEW_FIRST_VISIBLE_POSITION, 0)
								.set(COL_LISTVIEW_TOP, 0)
								.set(COL_MASTER_LISTVIEW_FIRST_VISIBLE_POSITION, 0)
								.set(COL_MASTER_LISTVIEW_TOP, 0)
								.set(COL_MANAGE_ITEMS_GROUP_ID, 0)
								.set(COL_IS_SYNCED_TO_DROPBOX, 0)
								.set(COL_IS_LIST_PREF_SYNCED_TO_DROPBOX, 0)
								.set(COL_IS_FIRST_TIME_SYNC, 0);

						// update the SQLite record with the dbxID
						AListContentProvider.setSuppressDropboxChanges(true);
						String dbxID = newItemRecord.getId();
						ContentValues newFieldValues = new ContentValues();
						newFieldValues.put(COL_LIST_DROPBOX_ID, dbxID);
						UpdateListsTableFieldValues(context, listID, newFieldValues);

						MyLog.d("ListsTable: dbxInsert ", "Key:" + key + ", value:" + listTitle);
						AListContentProvider.setSuppressDropboxChanges(false);
						dbxDatastore.sync();
					}
				}
			}

		} else {
			MyLog.e("ListsTable: dbxInsert ", "Unable to insert record. dbxDatastore is null!");
		}
		return newItemRecord;

	}

	public static void dbxDeleteSingleRecord(Context context, DbxDatastore dbxDatastore, String listIDstring) {
		if (dbxDatastore != null) {
			DbxTable dbxActiveTable = dbxDatastore.getTable(TABLE_LISTS);

			String dbxRecordID = getDropboxID(context, Long.parseLong(listIDstring));
			if (!dbxRecordID.isEmpty()) {
				try {
					DbxRecord dbxRecord = dbxActiveTable.get(dbxRecordID);
					if (dbxRecord != null) {
						dbxRecord.deleteRecord();
						dbxDatastore.sync();
					}
				} catch (DbxException e) {
					MyLog.e("ListsTable: dbxDeleteSingleRecord ", "DbxException while trying delete a dropbox record.");
				}
			}
		} else {
			MyLog.e("ListsTable: dbxDeleteSingleRecord ", "Unable to delete record. dbxDatastore is null!");
		}
	}

	public static void dbxDeleteMultipleRecords(Context context, DbxDatastore dbxDatastore, Uri uri, String selection,
			String[] selectionArgs) {

		if (dbxDatastore != null) {
			DbxTable dbxActiveTable = dbxDatastore.getTable(TABLE_LISTS);

			if (dbxActiveTable != null) {
				String projection[] = { COL_LIST_ID, COL_LIST_DROPBOX_ID };
				String sortOrder = null;
				String dbxID;
				DbxRecord dbxRecord;
				ContentResolver cr = context.getContentResolver();
				Cursor cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
				if (cursor != null) {
					try {
						while (cursor.moveToNext()) {
							dbxID = cursor.getString(cursor.getColumnIndexOrThrow(COL_LIST_DROPBOX_ID));
							dbxRecord = dbxActiveTable.get(dbxID);
							if (dbxRecord != null) {
								dbxRecord.deleteRecord();
							}
						}

						dbxDatastore.sync();
					} catch (DbxException e) {
						MyLog.e("ListsTable: dbxDeleteMultipleRecords ",
								"DbxException while deleteing multiple dropbox records.");
						e.printStackTrace();

					} finally {
						cursor.close();
					}
				}
			}
		} else {
			MyLog.e("ListsTable: dbxDeleteMultipleRecords ", "Unable to delete records. dbxDatastore is null!");
		}
	}

	public static void dbxDeleteAllRecords(DbxDatastore dbxDatastore) {
		if (dbxDatastore != null) {
			DbxTable dbxActiveTable = dbxDatastore.getTable(TABLE_LISTS);
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
					MyLog.e("ListsTable: dbxDeleteAllRecords ", "DbxException while deleteing all dropbox records.");
					e.printStackTrace();
				}
			}
		} else {
			MyLog.e("ListsTable: dbxDeleteAllRecords ", "Unable to delete records. dbxDatastore is null!");
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
			DbxTable dbxActiveTable = dbxDatastore.getTable(TABLE_LISTS);

			if (dbxActiveTable != null) {
				String projection[] = { COL_LIST_ID, COL_LIST_DROPBOX_ID };
				String sortOrder = null;
				String dbxID;
				DbxRecord dbxRecord;
				ContentResolver cr = context.getContentResolver();
				Cursor cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
				if (cursor != null) {
					try {
						while (cursor.moveToNext()) {
							dbxID = cursor.getString(cursor.getColumnIndexOrThrow(COL_LIST_DROPBOX_ID));
							dbxRecord = dbxActiveTable.get(dbxID);
							if (dbxRecord != null) {
								setDbxRecordValues(dbxRecord, values);
							}
						}

						dbxDatastore.sync();
					} catch (DbxException e) {
						MyLog.e("ListsTable: dbxUpdateMultipleRecords ", "DbxException while trying update records.");
						e.printStackTrace();

					} finally {
						cursor.close();
					}
				}
			}
		} else {
			MyLog.e("ListsTable: dbxUpdateMultipleRecords ", "Unable to update records. dbxDatastore is null!");
		}
	}

	public static void dbxUpdateSingleRecord(Context context, DbxDatastore dbxDatastore, ContentValues values, Uri uri) {
		if (dbxDatastore != null) {
			DbxTable dbxActiveTable = dbxDatastore.getTable(TABLE_LISTS);

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
							sqlDeleteListAlreadyDeletedFromDropbox(context, dbxRecordID);
							// sync to hopefully capture other dropbox changes
							dbxDatastore.sync();
						}

					} catch (DbxException e) {
						MyLog.e("ListsTable: dbxUpdateSingleRecord ", "DbxException while trying update records.");
						e.printStackTrace();
					}
				}
			}
		} else {
			MyLog.e("ListsTable: dbxUpdateSingleRecord ", "Unable to update record. dbxDatastore is null!");
		}
	}

	private static void sqlDeleteListAlreadyDeletedFromDropbox(Context context, String dbxRecordID) {
		AListContentProvider.setSuppressDropboxChanges(true);
		DeleteList(context, dbxRecordID);
		AListContentProvider.setSuppressDropboxChanges(false);
	}

	private static ContentValues setContentValues(Context context, long listID) {
		ContentValues newFieldValues = new ContentValues();
		Cursor cursor = getList(context, listID);
		if (cursor != null) {
			cursor.moveToFirst();
			for (String col : PROJECTION_ALL) {
				if (col.equals(COL_LIST_ID) || col.equals(COL_LIST_DROPBOX_ID)) {
					// do nothing

				} else if (col.equals(COL_LIST_TITLE)) {
					String value = cursor.getString(cursor.getColumnIndexOrThrow(col));
					newFieldValues.put(col, value);

				} else if (col.equals(COL_MANAGE_ITEMS_GROUP_ID) || col.equals(COL_ACTIVE_STORE_ID)) {
					long value = cursor.getLong(cursor.getColumnIndexOrThrow(col));
					newFieldValues.put(col, value);

				} else {
					int value = cursor.getInt(cursor.getColumnIndexOrThrow(col));
					newFieldValues.put(col, value);
				}
			}
			cursor.close();
		}

		return newFieldValues;
	}

	private static ContentValues setContentValues(DbxRecord dbxRecord) {
		ContentValues newFieldValues = new ContentValues();

		if (dbxRecord != null) {
			String dbxID = dbxRecord.getId();
			newFieldValues.put(COL_LIST_DROPBOX_ID, dbxID);

			if (dbxRecord.hasField(COL_LIST_TITLE)) {
				String listTitle = dbxRecord.getString(COL_LIST_TITLE);
				newFieldValues.put(COL_LIST_TITLE, listTitle);
			}

			if (dbxRecord.hasField(COL_ACTIVE_STORE_ID)) {
				long activeStoreID = dbxRecord.getLong(COL_ACTIVE_STORE_ID);
				newFieldValues.put(COL_ACTIVE_STORE_ID, activeStoreID);
			}

			if (dbxRecord.hasField(COL_DELETE_NOTE_UPON_DESELECTING_ITEM)) {
				int intField = (int) dbxRecord.getLong(COL_DELETE_NOTE_UPON_DESELECTING_ITEM);
				newFieldValues.put(COL_DELETE_NOTE_UPON_DESELECTING_ITEM, intField);
			}

			if (dbxRecord.hasField(COL_LIST_SORT_ORDER)) {
				int intField = (int) dbxRecord.getLong(COL_LIST_SORT_ORDER);
				newFieldValues.put(COL_LIST_SORT_ORDER, intField);
			}

			if (dbxRecord.hasField(COL_MASTER_LIST_SORT_ORDER)) {
				int intField = (int) dbxRecord.getLong(COL_MASTER_LIST_SORT_ORDER);
				newFieldValues.put(COL_MASTER_LIST_SORT_ORDER, intField);
			}

			if (dbxRecord.hasField(COL_ALLOW_GROUP_ADDITIONS)) {
				int intField = (int) dbxRecord.getLong(COL_ALLOW_GROUP_ADDITIONS);
				newFieldValues.put(COL_ALLOW_GROUP_ADDITIONS, intField);
			}

			if (dbxRecord.hasField(COL_TITLE_BACKGROUND_COLOR)) {
				int intField = (int) dbxRecord.getLong(COL_TITLE_BACKGROUND_COLOR);
				newFieldValues.put(COL_TITLE_BACKGROUND_COLOR, intField);
			}

			if (dbxRecord.hasField(COL_TITLE_TEXT_COLOR)) {
				int intField = (int) dbxRecord.getLong(COL_TITLE_TEXT_COLOR);
				newFieldValues.put(COL_TITLE_TEXT_COLOR, intField);
			}

			if (dbxRecord.hasField(COL_SEPARATOR_BACKGROUND_COLOR)) {
				int intField = (int) dbxRecord.getLong(COL_SEPARATOR_BACKGROUND_COLOR);
				newFieldValues.put(COL_SEPARATOR_BACKGROUND_COLOR, intField);
			}

			if (dbxRecord.hasField(COL_SEPARATOR_TEXT_COLOR)) {
				int intField = (int) dbxRecord.getLong(COL_SEPARATOR_TEXT_COLOR);
				newFieldValues.put(COL_SEPARATOR_TEXT_COLOR, intField);
			}

			if (dbxRecord.hasField(COL_LIST_BACKGROUND_COLOR)) {
				int intField = (int) dbxRecord.getLong(COL_LIST_BACKGROUND_COLOR);
				newFieldValues.put(COL_LIST_BACKGROUND_COLOR, intField);
			}

			if (dbxRecord.hasField(COL_ITEM_NORMAL_TEXT_COLOR)) {
				int intField = (int) dbxRecord.getLong(COL_ITEM_NORMAL_TEXT_COLOR);
				newFieldValues.put(COL_ITEM_NORMAL_TEXT_COLOR, intField);
			}

			if (dbxRecord.hasField(COL_ITEM_STRIKEOUT_TEXT_COLOR)) {
				int intField = (int) dbxRecord.getLong(COL_ITEM_STRIKEOUT_TEXT_COLOR);
				newFieldValues.put(COL_ITEM_STRIKEOUT_TEXT_COLOR, intField);
			}

			if (dbxRecord.hasField(COL_MASTER_LIST_BACKGROUND_COLOR)) {
				int intField = (int) dbxRecord.getLong(COL_MASTER_LIST_BACKGROUND_COLOR);
				newFieldValues.put(COL_MASTER_LIST_BACKGROUND_COLOR, intField);
			}

			if (dbxRecord.hasField(COL_MASTER_LIST_ITEM_NORMAL_TEXT_COLOR)) {
				int intField = (int) dbxRecord.getLong(COL_MASTER_LIST_ITEM_NORMAL_TEXT_COLOR);
				newFieldValues.put(COL_MASTER_LIST_ITEM_NORMAL_TEXT_COLOR, intField);
			}

			if (dbxRecord.hasField(COL_MASTER_LIST_ITEM_SELECTED_TEXT_COLOR)) {
				int intField = (int) dbxRecord.getLong(COL_MASTER_LIST_ITEM_SELECTED_TEXT_COLOR);
				newFieldValues.put(COL_MASTER_LIST_ITEM_SELECTED_TEXT_COLOR, intField);
			}

			if (dbxRecord.hasField(COL_LISTVIEW_FIRST_VISIBLE_POSITION)) {
				int intField = (int) dbxRecord.getLong(COL_LISTVIEW_FIRST_VISIBLE_POSITION);
				newFieldValues.put(COL_LISTVIEW_FIRST_VISIBLE_POSITION, intField);
			}

			if (dbxRecord.hasField(COL_LISTVIEW_TOP)) {
				int intField = (int) dbxRecord.getLong(COL_LISTVIEW_TOP);
				newFieldValues.put(COL_LISTVIEW_TOP, intField);
			}

			if (dbxRecord.hasField(COL_MASTER_LISTVIEW_FIRST_VISIBLE_POSITION)) {
				int intField = (int) dbxRecord.getLong(COL_MASTER_LISTVIEW_FIRST_VISIBLE_POSITION);
				newFieldValues.put(COL_MASTER_LISTVIEW_FIRST_VISIBLE_POSITION, intField);
			}

			if (dbxRecord.hasField(COL_MASTER_LISTVIEW_TOP)) {
				int intField = (int) dbxRecord.getLong(COL_MASTER_LISTVIEW_TOP);
				newFieldValues.put(COL_MASTER_LISTVIEW_TOP, intField);
			}

			if (dbxRecord.hasField(COL_MANAGE_ITEMS_GROUP_ID)) {
				int intField = (int) dbxRecord.getLong(COL_MANAGE_ITEMS_GROUP_ID);
				newFieldValues.put(COL_MANAGE_ITEMS_GROUP_ID, intField);
			}

			if (dbxRecord.hasField(COL_IS_SYNCED_TO_DROPBOX)) {
				int intField = (int) dbxRecord.getLong(COL_IS_SYNCED_TO_DROPBOX);
				newFieldValues.put(COL_IS_SYNCED_TO_DROPBOX, intField);
			}

			if (dbxRecord.hasField(COL_IS_LIST_PREF_SYNCED_TO_DROPBOX)) {
				int intField = (int) dbxRecord.getLong(COL_IS_LIST_PREF_SYNCED_TO_DROPBOX);
				newFieldValues.put(COL_IS_LIST_PREF_SYNCED_TO_DROPBOX, intField);
			}

			if (dbxRecord.hasField(COL_IS_FIRST_TIME_SYNC)) {
				int intField = (int) dbxRecord.getLong(COL_IS_FIRST_TIME_SYNC);
				newFieldValues.put(COL_IS_FIRST_TIME_SYNC, intField);
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

				if (key.equals(COL_LIST_TITLE)) {
					String listTitle = (String) me.getValue();
					dbxRecord.set(key, listTitle);
				} else if (key.equals(COL_ACTIVE_STORE_ID)) {
					long longValue = (Long) me.getValue();
					dbxRecord.set(key, longValue);
				} else if (key.equals(COL_DELETE_NOTE_UPON_DESELECTING_ITEM)) {
					int intValue = (Integer) me.getValue();
					dbxRecord.set(key, intValue);
				} else if (key.equals(COL_LIST_SORT_ORDER)) {
					int intValue = (Integer) me.getValue();
					dbxRecord.set(key, intValue);
				} else if (key.equals(COL_MASTER_LIST_SORT_ORDER)) {
					int intValue = (Integer) me.getValue();
					dbxRecord.set(key, intValue);
				} else if (key.equals(COL_ALLOW_GROUP_ADDITIONS)) {
					int intValue = (Integer) me.getValue();
					dbxRecord.set(key, intValue);
				} else if (key.equals(COL_TITLE_BACKGROUND_COLOR)) {
					int intValue = (Integer) me.getValue();
					dbxRecord.set(key, intValue);
				} else if (key.equals(COL_TITLE_TEXT_COLOR)) {
					int intValue = (Integer) me.getValue();
					dbxRecord.set(key, intValue);
				} else if (key.equals(COL_SEPARATOR_BACKGROUND_COLOR)) {
					int intValue = (Integer) me.getValue();
					dbxRecord.set(key, intValue);
				} else if (key.equals(COL_SEPARATOR_TEXT_COLOR)) {
					int intValue = (Integer) me.getValue();
					dbxRecord.set(key, intValue);
				} else if (key.equals(COL_LIST_BACKGROUND_COLOR)) {
					int intValue = (Integer) me.getValue();
					dbxRecord.set(key, intValue);
				} else if (key.equals(COL_ITEM_NORMAL_TEXT_COLOR)) {
					int intValue = (Integer) me.getValue();
					dbxRecord.set(key, intValue);
				} else if (key.equals(COL_ITEM_STRIKEOUT_TEXT_COLOR)) {
					int intValue = (Integer) me.getValue();
					dbxRecord.set(key, intValue);
				} else if (key.equals(COL_MASTER_LIST_BACKGROUND_COLOR)) {
					int intValue = (Integer) me.getValue();
					dbxRecord.set(key, intValue);
				} else if (key.equals(COL_MASTER_LIST_ITEM_NORMAL_TEXT_COLOR)) {
					int intValue = (Integer) me.getValue();
					dbxRecord.set(key, intValue);
				} else if (key.equals(COL_MASTER_LIST_ITEM_SELECTED_TEXT_COLOR)) {
					int intValue = (Integer) me.getValue();
					dbxRecord.set(key, intValue);
				} else if (key.equals(COL_LISTVIEW_FIRST_VISIBLE_POSITION)) {
					int intValue = (Integer) me.getValue();
					dbxRecord.set(key, intValue);
				} else if (key.equals(COL_LISTVIEW_TOP)) {
					int intValue = (Integer) me.getValue();
					dbxRecord.set(key, intValue);
				} else if (key.equals(COL_MASTER_LISTVIEW_FIRST_VISIBLE_POSITION)) {
					int intValue = (Integer) me.getValue();
					dbxRecord.set(key, intValue);
				} else if (key.equals(COL_MASTER_LISTVIEW_TOP)) {
					int intValue = (Integer) me.getValue();
					dbxRecord.set(key, intValue);
				} else if (key.equals(COL_MANAGE_ITEMS_GROUP_ID)) {
					long intValue = (Long) me.getValue();
					dbxRecord.set(key, intValue);
				} else if (key.equals(COL_IS_SYNCED_TO_DROPBOX)) {
					int intValue = (Integer) me.getValue();
					dbxRecord.set(key, intValue);
				} else if (key.equals(COL_IS_LIST_PREF_SYNCED_TO_DROPBOX)) {
					int intValue = (Integer) me.getValue();
					dbxRecord.set(key, intValue);
				} else if (key.equals(COL_IS_FIRST_TIME_SYNC)) {
					int intValue = (Integer) me.getValue();
					dbxRecord.set(key, intValue);

				} else {
					MyLog.e("ListsTable: setDbxRecordValues ", "Unknown column name:" + key);
				}
			}
		}
	}

	public static void replaceSqlRecordsWithDbxRecords(Context context, DbxDatastore dbxDatastore) {

		if (dbxDatastore != null) {
			DbxTable dbxActiveTable = dbxDatastore.getTable(TABLE_LISTS);
			if (dbxActiveTable != null) {
				try {
					DbxTable.QueryResult allRecords = dbxActiveTable.query();
					Iterator<DbxRecord> itr = allRecords.iterator();
					while (itr.hasNext()) {
						DbxRecord dbxRecord = itr.next();
						CreateItem(context, dbxRecord);
					}

				} catch (DbxException e) {
					MyLog.e("ListsTable: replaceSqlRecordsWithDbxRecords ",
							"DbxException while replacing all sql records.");
					e.printStackTrace();
				}
			}

		} else {
			MyLog.e("ListsTable: replaceSqlRecordsWithDbxRecords ",
					"Unable to replace sql records. dbxDatastore is null!");
		}
	}

	public static void validateSqlRecords(Context context, DbxTable dbxTable) {
		if (dbxTable != null) {

			// Iterate thru the SQL table records and verify if the SQL record exists in the Dbx table.
			// If not ... delete the SQL table record
			Cursor allItemsCursor = getAllDbxListsCursor(context);
			String dbxRecordID = "";
			long sqlRecordID = -1;
			DbxRecord dbxRecord = null;
			if (allItemsCursor != null && allItemsCursor.getCount() > 0) {
				while (allItemsCursor.moveToNext()) {

					try {
						dbxRecordID = allItemsCursor.getString(allItemsCursor
								.getColumnIndexOrThrow(COL_LIST_DROPBOX_ID));
						dbxRecord = dbxTable.get(dbxRecordID);
						if (dbxRecord == null) {
							// the SQL table record does not exist in the Dbx table ... so delete it.
							sqlRecordID = allItemsCursor.getLong(allItemsCursor.getColumnIndexOrThrow(COL_LIST_ID));
							DeleteList(context, sqlRecordID);
						}
					} catch (DbxException e) {
						MyLog.e("ListsTable: validateSqlRecords ", "DbxException while iterating thru SQL table.");
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
						Cursor itemCursor = getListFromDropboxID(context, dbxRecordID);
						if (itemCursor != null && itemCursor.getCount() > 0) {
							// update the existing record
							UpdateList(context, dbxRecordID, dbxRecord);
						} else {
							// create a new record
							CreateItem(context, dbxRecord);
						}
						if (itemCursor != null) {
							itemCursor.close();
						}
					}
				} catch (DbxException e) {
					MyLog.e("ListsTable: validateSqlRecords ", "DbxException while iterating thru DbxTable.");
					e.printStackTrace();
				}
			}

			if (allItemsCursor != null) {
				allItemsCursor.close();
			}
		}

	}

	private static Cursor getAllDbxListsCursor(Context context) {
		Cursor cursor = null;

		Uri uri = CONTENT_URI;
		String[] projection = new String[] { COL_LIST_ID, COL_LIST_DROPBOX_ID };

		String selection = COL_LIST_DROPBOX_ID + " != '' OR " + COL_LIST_DROPBOX_ID + " NOT NULL";
		String selectionArgs[] = null;

		ContentResolver cr = context.getContentResolver();
		try {
			cursor = cr.query(uri, projection, selection, selectionArgs, SORT_ORDER_LIST_TITLE);
		} catch (Exception e) {
			MyLog.e("Exception error  in getAllDbxListsCursor. ", "");
			e.printStackTrace();
		}
		return cursor;
	}

}
