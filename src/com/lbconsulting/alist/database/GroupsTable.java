package com.lbconsulting.alist.database;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v4.content.CursorLoader;

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
	// ,tblBridge.locationID, tblLocations.locationName
	public static final String[] PROJECTION_WITH_LOCATION_NAME = {
			TABLE_GROUPS + "." + COL_GROUP_ID,
			TABLE_GROUPS + "." + COL_GROUP_NAME,
			TABLE_GROUPS + "." + COL_CHECKED,
			BridgeTable.TABLE_BRIDGE + "." + BridgeTable.COL_LOCATION_ID,
			LocationsTable.TABLE_LOCATIONS + "." + LocationsTable.COL_LOCATION_NAME
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
			 * SELECT tblGroups._id, tblGroups.groupName,tblBridge.locationID,
			 * tblLocations.locationName FROM tblGroups JOIN tblBridge ON
			 * tblGroups._id= tblBridge.groupID JOIN tblLocations ON
			 * tblLocations._id = tblBridge.locationID WHERE tblGroups.listID =
			 * 3 AND tblBridge.storeID=2 ORDER BY tblLocations.locationName,
			 * tblGroups.groupName
			 */

			Uri uri = CONTENT_URI_GROUPS_WITH_LOCATIONS;
			String[] projection = PROJECTION_WITH_LOCATION_NAME;
			String selection = TABLE_GROUPS + "." + COL_LIST_ID + " = ? AND "
					+ BridgeTable.TABLE_BRIDGE + "." + BridgeTable.COL_STORE_ID + " = ?";
			String selectionArgs[] = new String[] { String.valueOf(listID), String.valueOf(storeID) };
			String sortOrder = LocationsTable.SORT_ORDER_LOCATION + ", " + GroupsTable.SORT_ORDER_GROUP;
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
			 * SELECT tblGroups._id, tblGroups.groupName,tblBridge.locationID,
			 * tblLocations.locationName FROM tblGroups JOIN tblBridge ON
			 * tblGroups._id= tblBridge.groupID JOIN tblLocations ON
			 * tblLocations._id = tblBridge.locationID WHERE tblGroups.listID =
			 * 3 AND tblBridge.storeID=2 ORDER BY tblLocations.locationName,
			 * tblGroups.groupName
			 */

			Uri uri = CONTENT_URI_GROUPS_WITH_LOCATIONS;
			String[] projection = PROJECTION_WITH_LOCATION_NAME;
			String selection = TABLE_GROUPS + "." + COL_LIST_ID + " = ? AND "
					+ BridgeTable.TABLE_BRIDGE + "." + BridgeTable.COL_STORE_ID + " = ?";
			String selectionArgs[] = new String[] { String.valueOf(listID), String.valueOf(storeID) };
			String sortOrder = GroupsTable.SORT_ORDER_GROUP + ", " + LocationsTable.SORT_ORDER_LOCATION;
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
			long listID, long storeID, long locationID) {
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
					BridgeTable.SetRow(context, listID, storeID, groupID, locationID);
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

}
