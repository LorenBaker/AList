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

public class GroupsTable {

	// Groups data table
	// Version 1
	public static final String TABLE_GROUPS = "tblGroups";
	public static final String COL_GROUP_ID = "_id";
	public static final String COL_GROUP_DROPBOX_ID = "groupDropboxID";
	public static final String COL_GROUP_NAME = "groupName";
	public static final String COL_LIST_ID = "listID";
	// Version 4 changes
	public static final String COL_CHECKED = "groupChecked";

	public static String DEFAULT_GROUP_VALUE = "[No Group]";

	public static final String[] PROJECTION_ALL = { COL_GROUP_ID, COL_GROUP_DROPBOX_ID, COL_GROUP_NAME, COL_LIST_ID,
			COL_CHECKED };
	// SELECT tblGroups._id, tblGroups.groupName, tblGroups.groupChecked
	// ,tblBridge.GroupID, tblGroups.GroupName
	public static final String[] PROJECTION_WITH_Group_NAME = {
			TABLE_GROUPS + "." + COL_GROUP_ID,
			TABLE_GROUPS + "." + COL_GROUP_NAME,
			TABLE_GROUPS + "." + COL_CHECKED,
			BridgeTable.TABLE_BRIDGE + "." + BridgeTable.COL_GROUP_ID,
			GroupsTable.TABLE_GROUPS + "." + GroupsTable.COL_GROUP_NAME
	};

	public static final String CONTENT_PATH = TABLE_GROUPS;

	public static final String CONTENT_PATH_GROUPS_WITH_LOCATIONS = "groupsWithLocations";
	public static final Uri CONTENT_URI_GROUPS_WITH_LOCATIONS = Uri.parse("content://" + AListContentProvider.AUTHORITY
			+ "/" + CONTENT_PATH_GROUPS_WITH_LOCATIONS);

	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + "vnd.lbconsulting."
			+ TABLE_GROUPS;
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + "vnd.lbconsulting."
			+ TABLE_GROUPS;
	public static final Uri CONTENT_URI = Uri.parse("content://" + AListContentProvider.AUTHORITY + "/" + CONTENT_PATH);

	public static final String SORT_ORDER_GROUP = COL_GROUP_NAME + " ASC";

	// Database creation SQL statements
	private static final String DATATABLE_CREATE = "create table "
			+ TABLE_GROUPS
			+ " ("
			+ COL_GROUP_ID + " integer primary key autoincrement, "
			+ COL_GROUP_DROPBOX_ID + " text, "
			+ COL_GROUP_NAME + " text collate nocase, "
			+ COL_LIST_ID + " integer not null references " + ListsTable.TABLE_LISTS + " (" + ListsTable.COL_LIST_ID
			+ ") default 1, "
			// Version 4 changes
			+ COL_CHECKED + " integer default 0 "
			+ ");";

	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(DATATABLE_CREATE);
		MyLog.i("GroupsTable", "onCreate: " + TABLE_GROUPS + " created.");
		ArrayList<String> sqlStatements = new ArrayList<String>();
		String insertProjection = "insert into "
				+ TABLE_GROUPS
				+ " ("
				+ COL_GROUP_ID + ", "
				+ COL_GROUP_NAME + ", "
				+ COL_LIST_ID + ") VALUES ";

		// Default Group
		sqlStatements.add(insertProjection + "(NULL, '" + DEFAULT_GROUP_VALUE + "', 1)");

		// Groups for Groceries List (2)
		/*
		 * sqlStatements.add(insertProjection + "(NULL, 'Aisle 1', 2)");
		 * sqlStatements.add(insertProjection + "(NULL, 'Aisle 2', 2)");
		 * sqlStatements.add(insertProjection + "(NULL, 'Aisle 3', 2)");
		 * sqlStatements.add(insertProjection + "(NULL, 'Aisle 4', 2)");
		 * sqlStatements.add(insertProjection + "(NULL, 'Aisle 5', 2)");
		 * sqlStatements.add(insertProjection + "(NULL, 'Produce', 2)");
		 * sqlStatements.add(insertProjection + "(NULL, 'Dairy', 2)");
		 * sqlStatements.add(insertProjection + "(NULL, 'Meats', 2)");
		 * sqlStatements.add(insertProjection + "(NULL, 'Bakery', 2)");
		 * 
		 * // Groups for ToDo List (3) sqlStatements.add(insertProjection +
		 * "(NULL, 'Group 4', 3)"); sqlStatements.add(insertProjection +
		 * "(NULL, 'Group 3', 3)"); sqlStatements.add(insertProjection +
		 * "(NULL, 'Group 2', 3)"); sqlStatements.add(insertProjection +
		 * "(NULL, 'Group 1', 3)");
		 */

		AListUtilities.execMultipleSQL(database, sqlStatements);
	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		MyLog.w(TABLE_GROUPS, "Upgrading database from version " + oldVersion + " to version " + newVersion);
		int upgradeToVersion = oldVersion + 1;
		switch (upgradeToVersion) {
		// fall through each case to upgrade to the newVersion
			case 2:
			case 3:
			case 4:
				database.execSQL("ALTER TABLE " + TABLE_GROUPS + " ADD COLUMN " + COL_CHECKED + " integer default 0");
				MyLog.i(TABLE_GROUPS, "GroupChecked column added.");
				break;

			default:
				// upgrade version not found!
				MyLog.e(TABLE_GROUPS, "Upgrade version " + newVersion + " not found!");
				database.execSQL("DROP TABLE IF EXISTS " + TABLE_GROUPS);
				onCreate(database);
				break;
		}
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Create Methods
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	public static long CreateNewGroup(Context context, long listID, String groupName) {
		long newGroupID = -1;
		if (listID > 1) {
			// verify that the item does not already exist in the table
			Cursor cursor = getGroup(context, listID, groupName);
			if (cursor != null && cursor.getCount() > 0) {
				// the item exists in the table ... so return its id
				cursor.moveToFirst();
				newGroupID = cursor.getLong(cursor.getColumnIndexOrThrow(COL_GROUP_ID));
				cursor.close();
			} else {
				// group does not exist in the table ... so add it
				if (groupName != null) {
					groupName = groupName.trim();
					if (!groupName.isEmpty()) {
						try {
							ContentResolver cr = context.getContentResolver();
							Uri uri = CONTENT_URI;
							ContentValues values = new ContentValues();
							values.put(COL_LIST_ID, listID);
							values.put(COL_GROUP_NAME, groupName);
							Uri newListUri = cr.insert(uri, values);
							if (newListUri != null) {
								newGroupID = Long.parseLong(newListUri.getLastPathSegment());
							}
						} catch (Exception e) {
							MyLog.e("Exception error in CreateNewGroup. ", e.toString());
						}

						// add the new group to each store in the list with listID
						Cursor storesCursor = BridgeTable.getStoresInList(context, listID);
						if (storesCursor != null) {
							if (storesCursor.getCount() > 0 && newGroupID > 1) {
								storesCursor.moveToPosition(-1);
								long storeID = -1;
								while (storesCursor.moveToNext()) {
									storeID = storesCursor.getLong(storesCursor
											.getColumnIndexOrThrow(BridgeTable.COL_STORE_ID));
									BridgeTable.CreateNewBridgeRow(context, listID, storeID, newGroupID);
								}
							}
							storesCursor.close();
						}

					} else {
						MyLog.e("GroupsTable", "Error in CreateNewGroup; groupName is Empty!");
					}
				} else {
					MyLog.e("GroupsTable", "Error in CreateNewGroup; groupName is Null!");
				}
			}

			if (cursor != null) {
				cursor.close();
			}
		}
		return newGroupID;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Read Methods
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	public static Cursor getGroup(Context context, long groupID) {
		Cursor cursor = null;
		if (groupID > 0) {
			Uri uri = Uri.withAppendedPath(CONTENT_URI, String.valueOf(groupID));
			String[] projection = PROJECTION_ALL;
			String selection = null;
			String selectionArgs[] = null;
			String sortOrder = null;
			ContentResolver cr = context.getContentResolver();
			try {
				cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
			} catch (Exception e) {
				MyLog.e("Exception error in GroupsTable: getGroup. ", e.toString());
			}
		}
		return cursor;
	}

	public static Cursor getGroup(Context context, long listID, String groupName) {
		Cursor cursor = null;
		if (listID > 1) {
			Uri uri = CONTENT_URI;
			String[] projection = PROJECTION_ALL;
			String selection = COL_LIST_ID + " = ? AND " + COL_GROUP_NAME + " = ?";
			String selectionArgs[] = new String[] { String.valueOf(listID), groupName };
			String sortOrder = null;
			ContentResolver cr = context.getContentResolver();
			try {
				cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
			} catch (Exception e) {
				MyLog.e("Exception error in GroupsTable: getGroup. ", e.toString());
			}
		}
		return cursor;
	}

	public static Cursor getAllGroupsInListCursor(Context context, long listID, String sortOrder) {
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
				MyLog.e("Exception error in ItemsTable: getAllGroupsInListCursor.", "");
				e.printStackTrace();
			}
		}
		return cursor;
	}

	public static CursorLoader getAllGroupsInListIncludeDefault(Context context, long listID, String sortOrder) {
		CursorLoader cursorLoader = null;
		if (listID > 1) {
			Uri uri = CONTENT_URI;
			String[] projection = PROJECTION_ALL;
			String selection = COL_LIST_ID + " = ? OR " + COL_LIST_ID + " = ?";
			String selectionArgs[] = new String[] { String.valueOf(1), String.valueOf(listID) };
			try {
				cursorLoader = new CursorLoader(context, uri, projection, selection, selectionArgs, sortOrder);
			} catch (Exception e) {
				MyLog.e("Exception error in GroupsTable: getAllGroupsInList. ", e.toString());
			}
		}
		return cursorLoader;
	}

	public static CursorLoader getAllGroupsInListIncludeLocations(Context context, long listID, long storeID) {
		CursorLoader cursorLoader = null;
		if (listID > 1) {

			/*
			 * SELECT tblGroups._id, tblGroups.groupName,tblBridge.GroupID,
			 * tblGroups.GroupName FROM tblGroups JOIN tblBridge ON
			 * tblGroups._id= tblBridge.groupID JOIN tblGroups ON
			 * tblGroups._id = tblBridge.GroupID WHERE tblGroups.listID =
			 * 3 AND tblBridge.storeID=2 ORDER BY tblGroups.GroupName,
			 * tblGroups.groupName
			 */

			Uri uri = CONTENT_URI_GROUPS_WITH_LOCATIONS;
			String[] projection = PROJECTION_WITH_Group_NAME;
			String selection = TABLE_GROUPS + "." + COL_LIST_ID + " = ? AND "
					+ BridgeTable.TABLE_BRIDGE + "." + BridgeTable.COL_STORE_ID + " = ?";
			String selectionArgs[] = new String[] { String.valueOf(listID), String.valueOf(storeID) };
			String sortOrder = GroupsTable.SORT_ORDER_GROUP;
			try {
				cursorLoader = new CursorLoader(context, uri, projection, selection, selectionArgs, sortOrder);
			} catch (Exception e) {
				MyLog.e("Exception error  in ItemsTable: getAllGroupsInListIncludeLocations. ", e.toString());
			}
		}
		return cursorLoader;
	}

	public static Cursor getCursorAllGroupsInListIncludeLocations(Context context, long listID, long storeID) {
		Cursor cursor = null;
		if (listID > 1) {

			/*
			 * SELECT tblGroups._id, tblGroups.groupName,tblBridge.GroupID,
			 * tblGroups.GroupName FROM tblGroups JOIN tblBridge ON
			 * tblGroups._id= tblBridge.groupID JOIN tblGroups ON
			 * tblGroups._id = tblBridge.GroupID WHERE tblGroups.listID =
			 * 3 AND tblBridge.storeID=2 ORDER BY tblGroups.GroupName,
			 * tblGroups.groupName
			 */

			Uri uri = CONTENT_URI_GROUPS_WITH_LOCATIONS;
			String[] projection = PROJECTION_WITH_Group_NAME;
			String selection = TABLE_GROUPS + "." + COL_LIST_ID + " = ? AND "
					+ BridgeTable.TABLE_BRIDGE + "." + BridgeTable.COL_STORE_ID + " = ?";
			String selectionArgs[] = new String[] { String.valueOf(listID), String.valueOf(storeID) };
			String sortOrder = GroupsTable.SORT_ORDER_GROUP;
			ContentResolver cr = context.getContentResolver();
			try {
				cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
			} catch (Exception e) {
				MyLog.e("Exception error  in ItemsTable: getCursorAllGroupsInListIncludeLocations. ", e.toString());
			}
		}
		return cursor;
	}

	public static CursorLoader getAllGroupsInList(Context context, long listID, String sortOrder) {
		CursorLoader cursorLoader = null;
		if (listID > 1) {
			Uri uri = CONTENT_URI;
			String[] projection = PROJECTION_ALL;
			String selection = COL_LIST_ID + " = ?";
			String selectionArgs[] = new String[] { String.valueOf(listID) };
			try {
				cursorLoader = new CursorLoader(context, uri, projection, selection, selectionArgs, sortOrder);
			} catch (Exception e) {
				MyLog.e("Exception error in GroupsTable: getAllGroupsInList. ", e.toString());
			}
		}
		return cursorLoader;
	}

	public static Cursor getAllGroupIDsInList(Context context, long listID) {
		Cursor cursor = null;
		if (listID > 1) {
			Uri uri = CONTENT_URI;
			String[] projection = new String[] { COL_GROUP_ID };
			String selection = COL_LIST_ID + " = ?";
			String selectionArgs[] = new String[] { String.valueOf(listID) };
			String sortOrder = null;
			ContentResolver cr = context.getContentResolver();
			try {
				cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
			} catch (Exception e) {
				MyLog.e("Exception error in GroupsTable: getAllGroupIDsInList. ", e.toString());
			}
		}
		return cursor;
	}

	public static Cursor getAllCheckedGroups(Context context, long listID) {
		Cursor cursor = null;
		if (listID > 1) {
			Uri uri = CONTENT_URI;
			String[] projection = PROJECTION_ALL;
			String selection = COL_LIST_ID + " = ? AND " + COL_CHECKED + " = ?";
			String selectionArgs[] = new String[] { String.valueOf(listID), String.valueOf(1) };
			String sortOrder = null;
			ContentResolver cr = context.getContentResolver();
			try {
				cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
			} catch (Exception e) {
				MyLog.e("Exception error in GroupsTable: getAllCheckedGroups. ", e.toString());
			}
		}
		return cursor;
	}

	public static String getGroupName(Context context, long groupID) {
		String groupName = "";
		Cursor cursor = getGroup(context, groupID);
		if (cursor != null) {
			cursor.moveToFirst();
			groupName = cursor.getString(cursor.getColumnIndexOrThrow(COL_GROUP_NAME));
			cursor.close();
		}
		return groupName;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Update Methods
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	/*	public static int UpdateGroupName(Context context, long groupID, String groupName) {
			int numberOfUpdatedRecords = -1;
			// cannot update the default group with ID=1
			if (groupID > 1) {
				Uri uri = CONTENT_URI;
				String selection = COL_LIST_ID + " = ?";
				String selectionArgs[] = new String[] { String.valueOf(groupID) };
				ContentResolver cr = context.getContentResolver();

				ContentValues values = new ContentValues();
				values.put(COL_GROUP_NAME, groupName.trim());
				numberOfUpdatedRecords = cr.update(uri, values, selection, selectionArgs);
				if (numberOfUpdatedRecords != 1) {
					MyLog.e("GroupsTable: UpdateGroupName", "The number of records updated does not equal 1!");
				}
			}
			return numberOfUpdatedRecords;
		}*/

	public static int UnCheckAllCheckedGroups(Context context, long listID) {
		int numberOfUpdatedRecords = -1;
		// cannot update the default group with ID=1
		if (listID > 1) {
			Uri uri = CONTENT_URI;
			String selection = COL_LIST_ID + " = ? AND " + COL_CHECKED + " = ?";
			String selectionArgs[] = new String[] { String.valueOf(listID), String.valueOf(1) };
			ContentResolver cr = context.getContentResolver();

			ContentValues values = new ContentValues();
			values.put(COL_CHECKED, 0);
			numberOfUpdatedRecords = cr.update(uri, values, selection, selectionArgs);
		}
		return numberOfUpdatedRecords;
	}

	public static void ToggleCheckBox(Context context, long groupID) {
		Cursor cursor = getGroup(context, groupID);
		if (cursor != null) {
			cursor.moveToFirst();
			int columnIndex = cursor.getColumnIndexOrThrow(COL_CHECKED);
			int checkIntValue = cursor.getInt(columnIndex);
			boolean checkValue = checkIntValue > 0;
			cursor.close();
			CheckItem(context, groupID, !checkValue);
		}
	}

	public static int CheckItem(Context context, long groupID, boolean checked) {
		int numberOfUpdatedRecords = -1;
		if (groupID > 0) {
			try {
				ContentResolver cr = context.getContentResolver();
				Uri uri = CONTENT_URI;
				String where = COL_GROUP_ID + " = ?";
				String[] whereArgs = { String.valueOf(groupID) };

				ContentValues values = new ContentValues();
				int checkedValue = AListUtilities.boolToInt(checked);
				values.put(COL_CHECKED, checkedValue);
				numberOfUpdatedRecords = cr.update(uri, values, where, whereArgs);
			} catch (Exception e) {
				MyLog.e("Exception error in GroupsTable: CheckItem. ", e.toString());
			}
		}
		return numberOfUpdatedRecords;
	}

	public static int UpdateGroupTableFieldValues(Context context, long groupID, ContentValues newFieldValues) {
		int numberOfUpdatedRecords = -1;
		if (groupID > 1) {
			ContentResolver cr = context.getContentResolver();
			Uri defaultUri = Uri.withAppendedPath(CONTENT_URI, String.valueOf(groupID));
			String selection = null;
			String[] selectionArgs = null;
			numberOfUpdatedRecords = cr.update(defaultUri, newFieldValues, selection, selectionArgs);
		}
		return numberOfUpdatedRecords;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Delete Methods
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static int DeleteGroup(Context context, long groupID) {
		int numberOfDeletedRecords = -1;
		if (groupID > 1) {
			ContentResolver cr = context.getContentResolver();
			Uri uri = CONTENT_URI;
			String where = COL_GROUP_ID + " = ?";
			String[] selectionArgs = { String.valueOf(groupID) };
			numberOfDeletedRecords = cr.delete(uri, where, selectionArgs);
		}
		// reset the groupID to the default value
		ItemsTable.ResetGroupID(context, groupID);
		BridgeTable.ResetGroupID(context, groupID);
		return numberOfDeletedRecords;
	}

	public static int DeleteAllGroupsInList(Context context, long listID) {
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

	public static int ApplyLocationToCheckedGroups(Context context,
			long listID, long storeID, long GroupID) {
		int numberOfCheckedGroups = -1;

		// get all of the checked groups
		Cursor allCheckedGroupsCursor = getAllCheckedGroups(context, listID);
		if (allCheckedGroupsCursor != null) {
			if (allCheckedGroupsCursor.getCount() > 0) {
				allCheckedGroupsCursor.moveToPosition(-1);
				long groupID = -1;
				while (allCheckedGroupsCursor.moveToNext()) {
					groupID = allCheckedGroupsCursor
							.getLong(allCheckedGroupsCursor.getColumnIndexOrThrow(COL_GROUP_ID));
					BridgeTable.SetRow(context, listID, storeID, groupID, GroupID);
				}
			}
			allCheckedGroupsCursor.close();
			// un-check all checked groups
			numberOfCheckedGroups = UnCheckAllCheckedGroups(context, listID);
		}
		return numberOfCheckedGroups;
	}

	public static void dbxDeleteSingleRecord(Context mContext, String rowIDstring) {
		// TODO Auto-generated method stub

	}

	public static void dbxDeleteMultipleRecords(Context mContext, Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub

	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	// SQLite Methods that use Dropbox records
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static long CreateGroup(Context context, DbxRecord dbxRecord) {
		// a check to see if the Group is already in the database
		// was done prior to making this call ... so don't repeat it.
		long newGroupID = -1;

		ContentValues newFieldValues = setContentValues(dbxRecord);
		Uri uri = CONTENT_URI;
		ContentResolver cr = context.getContentResolver();
		Uri newGroupUri = cr.insert(uri, newFieldValues);
		if (newGroupUri != null) {
			newGroupID = Long.parseLong(newGroupUri.getLastPathSegment());
		}
		return newGroupID;
	}

	public static Cursor getGroupFromDropboxID(Context context, String dbxRecordID) {
		Uri uri = CONTENT_URI;
		String[] projection = PROJECTION_ALL;
		String selection = COL_GROUP_DROPBOX_ID + " = '" + dbxRecordID + "'";
		String selectionArgs[] = null;
		String sortOrder = SORT_ORDER_GROUP;

		ContentResolver cr = context.getContentResolver();
		Cursor cursor = null;
		try {
			cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
		} catch (Exception e) {
			MyLog.e("GroupsTable", "Exception error in getGroupFromDropboxID:");
			e.printStackTrace();
		}
		return cursor;
	}

	public static Uri getGroupUri(Context context, String dbxRecordID) {
		Uri GroupUri = null;
		Cursor cursor = getGroupFromDropboxID(context, dbxRecordID);
		if (cursor != null) {
			cursor.moveToFirst();
			long GroupID = cursor.getLong(cursor.getColumnIndexOrThrow(COL_GROUP_ID));
			GroupUri = ContentUris.withAppendedId(CONTENT_URI, GroupID);
			cursor.close();
		}
		return GroupUri;
	}

	public static String getDropboxID(Context context, long GroupID) {
		String dbxID = "";
		Cursor cursor = getGroup(context, GroupID);
		if (cursor != null) {
			if (cursor.getCount() > 0) {
				cursor.moveToFirst();
				dbxID = cursor.getString(cursor.getColumnIndexOrThrow(COL_GROUP_DROPBOX_ID));
			}
			cursor.close();
		}

		return dbxID;
	}

	public static int UpdateGroup(Context context, String dbxRecordID, DbxRecord dbxRecord) {
		int numberOfUpdatedRecords = -1;
		ContentResolver cr = context.getContentResolver();
		Uri GroupUri = getGroupUri(context, dbxRecordID);
		ContentValues newFieldValues = setContentValues(dbxRecord);
		String selection = null;
		String[] selectionArgs = null;
		numberOfUpdatedRecords = cr.update(GroupUri, newFieldValues, selection, selectionArgs);

		return numberOfUpdatedRecords;
	}

	public static int DeleteGroup(Context context, String dbxRecordID) {
		int numberOfDeletedRecords = -1;

		Uri GroupUri = getGroupUri(context, dbxRecordID);
		ContentResolver cr = context.getContentResolver();
		String where = null;
		String[] selectionArgs = null;
		numberOfDeletedRecords = cr.delete(GroupUri, where, selectionArgs);

		return numberOfDeletedRecords;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Dropbox DataGroup Methods
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static void dbxInsert(Context context, DbxDatastore DbxDatastore, long GroupID) throws DbxException {
		ContentValues values = setContentValues(context, GroupID);
		DbxRecord dbxRecord = dbxInsert(context, DbxDatastore, GroupID, values);
		setDbxRecordValues(dbxRecord, values);
		DbxDatastore.sync();
	}

	public static void dbxInsertAllGroups(Context context, DbxDatastore DbxDatastore, long listID) throws DbxException {
		Cursor groupsCursor = getAllGroupsInListCursor(context, listID, null);
		if (groupsCursor != null) {
			while (groupsCursor.moveToNext()) {
				ContentValues values = setContentValues(context, groupsCursor);
				long groupID = groupsCursor.getLong(groupsCursor.getColumnIndexOrThrow(COL_GROUP_ID));
				DbxRecord dbxRecord = dbxInsert(context, DbxDatastore, groupID, values);
				setDbxRecordValues(dbxRecord, values);
			}
			DbxDatastore.sync();
			groupsCursor.close();
		}
	}

	public static DbxRecord dbxInsert(Context context, DbxDatastore DbxDatastore, long GroupID, ContentValues values) {

		DbxRecord newGroupRecord = null;
		if (DbxDatastore != null) {
			DbxTable dbxActiveTable = DbxDatastore.getTable(TABLE_GROUPS);

			if (dbxActiveTable != null) {

				Set<Entry<String, Object>> s = values.valueSet();
				Iterator<Entry<String, Object>> itr = s.iterator();
				while (itr.hasNext()) {
					Entry<String, Object> me = itr.next();
					String key = me.getKey().toString();

					if (key.equals(COL_GROUP_NAME)) {
						String GroupName = (String) me.getValue();
						newGroupRecord = dbxActiveTable.insert()
								.set(key, GroupName)
								.set(COL_LIST_ID, 1)
								.set(COL_CHECKED, 0);

						// update the SQLite record with the dbxID
						AListContentProvider.setSuppressDropboxChanges(true);
						String dbxID = newGroupRecord.getId();
						ContentValues newFieldValues = new ContentValues();
						newFieldValues.put(COL_GROUP_DROPBOX_ID, dbxID);
						UpdateGroupTableFieldValues(context, GroupID, newFieldValues);

						MyLog.d("GroupsTable: dbxInsert ", key + ":" + GroupName);
						AListContentProvider.setSuppressDropboxChanges(false);

					} else if (key.equals(COL_LIST_ID)) {
						long listID = (Long) me.getValue();
						if (newGroupRecord != null) {
							newGroupRecord.set(key, listID);
							MyLog.d("GroupsTable: dbxInsert ", key + ":" + listID);
						}
					}
				}
			}
		} else {
			MyLog.e("GroupsTable: dbxInsert ", "Unable to insert record. DbxDatastore is null!");
		}
		return newGroupRecord;
	}

	public static void dbxDeleteSingleRecord(Context context, DbxDatastore DbxDatastore, String GroupIDstring) {
		if (DbxDatastore != null) {
			DbxTable dbxActiveTable = DbxDatastore.getTable(TABLE_GROUPS);

			String dbxRecordID = getDropboxID(context, Long.parseLong(GroupIDstring));
			if (dbxRecordID != null && !dbxRecordID.isEmpty()) {
				try {
					DbxRecord dbxRecord = dbxActiveTable.get(dbxRecordID);
					if (dbxRecord != null) {
						dbxRecord.deleteRecord();
						DbxDatastore.sync();
					}
				} catch (DbxException e) {
					MyLog.e("GroupsTable: dbxDeleteSingleRecord ",
							"DbxException while trying delete a dropbox record.");
				}
			}
		} else {
			MyLog.e("GroupsTable: dbxDeleteSingleRecord ", "Unable to delete record. DbxDatastore is null!");
		}
	}

	public static void dbxDeleteMultipleRecords(Context context, DbxDatastore DbxDatastore, Uri uri, String selection,
			String[] selectionArgs) {

		if (DbxDatastore != null) {
			DbxTable dbxActiveTable = DbxDatastore.getTable(TABLE_GROUPS);

			if (dbxActiveTable != null) {
				String projection[] = { COL_GROUP_ID, COL_GROUP_DROPBOX_ID };
				String sortOrder = null;
				String dbxID;
				DbxRecord dbxRecord;
				ContentResolver cr = context.getContentResolver();
				Cursor cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
				if (cursor != null) {
					try {
						while (cursor.moveToNext()) {
							dbxID = cursor.getString(cursor.getColumnIndexOrThrow(COL_GROUP_DROPBOX_ID));
							dbxRecord = dbxActiveTable.get(dbxID);
							if (dbxRecord != null) {
								dbxRecord.deleteRecord();
							}
						}

						DbxDatastore.sync();
					} catch (DbxException e) {
						MyLog.e("GroupsTable: dbxDeleteMultipleRecords ",
								"DbxException while trying to delete multiple dropbox records.");
						e.printStackTrace();

					} finally {
						cursor.close();
					}
				}
			}
		} else {
			MyLog.e("GroupsTable: dbxDeleteMultipleRecords ", "Unable to delete records. DbxDatastore is null!");
		}
	}

	public static void dbxDeleteAllRecords(DbxDatastore DbxDatastore) {
		if (DbxDatastore != null) {
			DbxTable dbxActiveTable = DbxDatastore.getTable(TABLE_GROUPS);
			if (dbxActiveTable != null) {
				try {
					DbxTable.QueryResult allRecords = dbxActiveTable.query();
					Iterator<DbxRecord> itr = allRecords.iterator();
					while (itr.hasNext()) {
						DbxRecord dbxRecord = itr.next();
						dbxRecord.deleteRecord();
					}

					DbxDatastore.sync();

				} catch (DbxException e) {
					MyLog.e("GroupsTable: dbxDeleteAllRecords ", "DbxException while deleteing all dropbox records.");
					e.printStackTrace();
				}
			}
		} else {
			MyLog.e("GroupsTable: dbxDeleteAllRecords ", "Unable to delete records. DbxDatastore is null!");
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

	public static void dbxUpdateMultipleRecords(Context context, DbxDatastore DbxDatastore, ContentValues values,
			Uri uri, String selection, String[] selectionArgs) {

		if (DbxDatastore != null) {
			DbxTable dbxActiveTable = DbxDatastore.getTable(TABLE_GROUPS);

			if (dbxActiveTable != null) {
				String projection[] = { COL_GROUP_ID, COL_GROUP_DROPBOX_ID };
				String sortOrder = null;
				String dbxID;
				DbxRecord dbxRecord;
				ContentResolver cr = context.getContentResolver();
				Cursor cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
				if (cursor != null) {
					try {
						while (cursor.moveToNext()) {
							dbxID = cursor.getString(cursor.getColumnIndexOrThrow(COL_GROUP_DROPBOX_ID));
							dbxRecord = dbxActiveTable.get(dbxID);
							if (dbxRecord != null) {
								setDbxRecordValues(dbxRecord, values);
							}
						}

						DbxDatastore.sync();
					} catch (DbxException e) {
						MyLog.e("GroupsTable: dbxUpdateMultipleRecords ",
								"DbxException while trying update records.");
						e.printStackTrace();

					} finally {
						cursor.close();
					}
				}
			}
		} else {
			MyLog.e("GroupsTable: dbxUpdateMultipleRecords ", "Unable to update records. DbxDatastore is null!");
		}
	}

	public static void dbxUpdateSingleRecord(Context context, DbxDatastore DbxDatastore, ContentValues values, Uri uri) {
		if (DbxDatastore != null) {
			DbxTable dbxActiveTable = DbxDatastore.getTable(TABLE_GROUPS);

			if (dbxActiveTable != null) {
				String rowIDstring = uri.getLastPathSegment();
				String dbxRecordID = getDropboxID(context, Long.parseLong(rowIDstring));
				if (dbxRecordID != null && !dbxRecordID.isEmpty()) {
					try {
						DbxRecord dbxRecord = dbxActiveTable.get(dbxRecordID);
						if (dbxRecord != null) {
							setDbxRecordValues(dbxRecord, values);
							DbxDatastore.sync();
						} else {
							// the dbxGroup has been deleted ...
							// but for some reason it has not been deleted from the sql database
							// so delete it now.
							sqlDeleteGroupAlreadyDeletedFromDropbox(context, dbxRecordID);
							// sync to hopefully capture other dropbox changes
							DbxDatastore.sync();
						}

					} catch (DbxException e) {
						MyLog.e("GroupsTable: dbxUpdateSingleRecord ", "DbxException while trying update records.");
						e.printStackTrace();
					}
				}
			}
		} else {
			MyLog.e("GroupsTable: dbxUpdateSingleRecord ", "Unable to update record. DbxDatastore is null!");
		}
	}

	private static void sqlDeleteGroupAlreadyDeletedFromDropbox(Context context, String dbxRecordID) {
		AListContentProvider.setSuppressDropboxChanges(true);
		DeleteGroup(context, dbxRecordID);
		AListContentProvider.setSuppressDropboxChanges(false);
	}

	private static ContentValues setContentValues(Context context, Cursor cursor) {
		ContentValues newFieldValues = new ContentValues();
		if (cursor != null) {
			for (String col : PROJECTION_ALL) {
				if (col.equals(COL_GROUP_ID) || col.equals(COL_GROUP_DROPBOX_ID)) {
					// do nothing
				} else if (col.equals(COL_GROUP_NAME)) {
					String value = cursor.getString(cursor.getColumnIndexOrThrow(col));
					newFieldValues.put(col, value);

				} else if (col.equals(COL_LIST_ID)) {
					long value = cursor.getLong(cursor.getColumnIndexOrThrow(col));
					newFieldValues.put(col, value);

				} else if (col.equals(COL_CHECKED)) {
					int value = cursor.getInt(cursor.getColumnIndexOrThrow(col));
					newFieldValues.put(col, value);
				}
			}
		}
		return newFieldValues;
	}

	/*		+ TABLE_GROUPS
	+ " ("
	+ COL_GROUP_ID + " integer primary key autoincrement, "
	+ COL_GROUP_DROPBOX_ID + " text, "
	+ COL_GROUP_NAME + " text collate nocase, "
	+ COL_LIST_ID + " integer not null references " + ListsTable.TABLE_LISTS + " (" + ListsTable.COL_LIST_ID
	+ ") default 1, "
	// Version 4 changes
	+ COL_CHECKED + " integer default 0 "*/

	private static ContentValues setContentValues(Context context, long GroupID) {
		ContentValues newFieldValues = null;
		Cursor cursor = getGroup(context, GroupID);
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
			newFieldValues.put(COL_GROUP_DROPBOX_ID, dbxID);

			if (dbxRecord.hasField(COL_GROUP_NAME)) {
				String groupName = dbxRecord.getString(COL_GROUP_NAME);
				newFieldValues.put(COL_GROUP_NAME, groupName);
			}

			if (dbxRecord.hasField(COL_LIST_ID)) {
				long listID = dbxRecord.getLong(COL_LIST_ID);
				newFieldValues.put(COL_LIST_ID, listID);
			}

			if (dbxRecord.hasField(COL_CHECKED)) {
				int checked = (int) dbxRecord.getLong(COL_CHECKED);
				newFieldValues.put(COL_CHECKED, checked);
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

				if (key.equals(COL_GROUP_NAME)) {
					String string = (String) me.getValue();
					if (string == null) {
						string = "";
					}
					dbxRecord.set(key, string);

				} else if (key.equals(COL_LIST_ID)) {
					long listID = (Long) me.getValue();
					dbxRecord.set(key, listID);

				} else if (key.equals(COL_CHECKED)) {
					int checked = (Integer) me.getValue();
					dbxRecord.set(key, checked);

				} else if (key.equals(COL_GROUP_DROPBOX_ID)) {
					// do nothing

				} else {
					MyLog.e("GroupsTable: setDbxRecordValues ", "Unknown column name:" + key);
				}
			}
		}
	}

	/*	public static void replaceSqlRecordsWithDbxRecords(Context context, DbxDatastore DbxDatastore) {
	MAY NEED TO CHECK IF SQL RECORD ALREADY EXISTS
			if (DbxDatastore != null) {
				DbxTable dbxActiveTable = DbxDatastore.getTable(TABLE_GROUPS);
				if (dbxActiveTable != null) {
					try {
						DbxTable.QueryResult allRecords = dbxActiveTable.query();
						Iterator<DbxRecord> itr = allRecords.iterator();
						while (itr.hasNext()) {
							DbxRecord dbxRecord = itr.next();
							CreateGroup(context, dbxRecord);
						}

					} catch (DbxException e) {
						MyLog.e("GroupsTable: replaceSqlRecordsWithDbxRecords ",
								"DbxException while replacing all sql records.");
						e.printStackTrace();
					}
				}

			} else {
				MyLog.e("GroupsTable: replaceSqlRecordsWithDbxRecords ",
						"Unable to replace sql records. DbxDatastore is null!");
			}
		}*/

	public static void validateSqlRecords(Context context, DbxTable dbxTable) {
		if (dbxTable != null) {

			// Iterate thru the SQL table records and verify if the SQL record exists in the Dbx table.
			// If not ... delete the SQL table record
			Cursor allDbxGroupsCursor = getAllDbxGroupsCursor(context);
			String dbxRecordID = "";
			long sqlRecordID = -1;
			DbxRecord dbxRecord = null;
			if (allDbxGroupsCursor != null && allDbxGroupsCursor.getCount() > 0) {
				while (allDbxGroupsCursor.moveToNext()) {

					try {
						dbxRecordID = allDbxGroupsCursor.getString(allDbxGroupsCursor
								.getColumnIndexOrThrow(COL_GROUP_DROPBOX_ID));
						dbxRecord = dbxTable.get(dbxRecordID);
						if (dbxRecord == null) {
							// the SQL table record does not exist in the Dbx table ... so delete it.
							sqlRecordID = allDbxGroupsCursor.getLong(allDbxGroupsCursor
									.getColumnIndexOrThrow(COL_GROUP_ID));
							DeleteGroup(context, sqlRecordID);
						}
					} catch (DbxException e) {
						MyLog.e("GroupsTable: validateSqlRecords ", "DbxException while iterating thru SQL table.");
						e.printStackTrace();
					}
				}
			}

			// Iterate thru the dbxTable updating or creating SQL records
			try {
				DbxTable.QueryResult allRecords = dbxTable.query();
				Iterator<DbxRecord> itr = allRecords.iterator();
				while (itr.hasNext()) {
					dbxRecord = itr.next();
					dbxRecordID = dbxRecord.getId();
					Cursor GroupCursor = getGroupFromDropboxID(context, dbxRecordID);
					if (GroupCursor != null && GroupCursor.getCount() > 0) {
						// update the existing record
						UpdateGroup(context, dbxRecordID, dbxRecord);
					} else {
						// create a new record
						CreateGroup(context, dbxRecord);
					}
					if (GroupCursor != null) {
						GroupCursor.close();
					}
				}
			} catch (DbxException e) {
				MyLog.e("GroupsTable: validateSqlRecords ", "DbxException while iterating thru DbxTable.");
				e.printStackTrace();
			}

			if (allDbxGroupsCursor != null) {
				allDbxGroupsCursor.close();
			}
		}

	}

	private static Cursor getAllDbxGroupsCursor(Context context) {
		Cursor cursor = null;

		Uri uri = CONTENT_URI;
		String[] projection = new String[] { COL_GROUP_ID, COL_GROUP_DROPBOX_ID };

		String selection = COL_GROUP_DROPBOX_ID + " != '' OR " + COL_GROUP_DROPBOX_ID + " NOT NULL";
		String selectionArgs[] = null;

		ContentResolver cr = context.getContentResolver();
		try {
			cursor = cr.query(uri, projection, selection, selectionArgs, SORT_ORDER_GROUP);
		} catch (Exception e) {
			MyLog.e("Exception error  in getAllDbxGroupsCursor. ", "");
			e.printStackTrace();
		}
		return cursor;
	}

}
