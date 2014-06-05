package com.lbconsulting.alist.database;

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

import com.dropbox.sync.android.DbxDatastore;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxRecord;
import com.dropbox.sync.android.DbxTable;
import com.lbconsulting.alist.utilities.MyLog;

public class BridgeTable {

	// BridgeRows data table
	// Version 4
	public static final String TABLE_BRIDGE = "tblBridge";
	public static final String COL_BRIDGE_ID = "_id";
	public static final String COL_BRIDGE_DROPBOX_ID = "bridgeDropboxID";
	public static final String COL_LIST_ID = "listID";
	public static final String COL_GROUP_ID = "groupID";
	public static final String COL_STORE_ID = "storeID";
	public static final String COL_LOCATION_ID = "locationID";

	public static final String[] PROJECTION_ALL = { COL_BRIDGE_ID, COL_BRIDGE_DROPBOX_ID, COL_LIST_ID, COL_GROUP_ID,
			COL_STORE_ID,
			COL_LOCATION_ID };

	public static final String CONTENT_PATH = TABLE_BRIDGE;

	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + "vnd.lbconsulting."
			+ TABLE_BRIDGE;
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/"
			+ "vnd.lbconsulting."
			+ TABLE_BRIDGE;
	public static final Uri CONTENT_URI = Uri.parse("content://" + AListContentProvider.AUTHORITY + "/" + CONTENT_PATH);

	// Database creation SQL statements
	private static final String DATATABLE_CREATE = "create table " + TABLE_BRIDGE + " ("
			+ COL_BRIDGE_ID + " integer primary key autoincrement, "
			+ COL_BRIDGE_DROPBOX_ID + " text, "
			+ COL_LIST_ID + " integer not null references " + ListsTable.TABLE_LISTS + " (" + ListsTable.COL_LIST_ID
			+ ") default 1, "
			+ COL_GROUP_ID + " integer not null references " + GroupsTable.TABLE_GROUPS + " ("
			+ GroupsTable.COL_GROUP_ID + ") default 1, "
			+ COL_STORE_ID + " integer not null references " + StoresTable.TABLE_STORES + " ("
			+ StoresTable.COL_STORE_ID + ") default 1, "
			+ COL_LOCATION_ID + " integer not null references " + LocationsTable.TABLE_LOCATIONS + " ("
			+ LocationsTable.COL_LOCATION_ID + ") default 1 "
			+ ");";

	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(DATATABLE_CREATE);
		MyLog.i("BridgeRowsTable", "onCreate: " + TABLE_BRIDGE + " created.");
	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {

		MyLog.w(TABLE_BRIDGE, "Upgrading database from version " + oldVersion + " to version " + newVersion);

		int upgradeToVersion = oldVersion + 1;
		switch (upgradeToVersion) {
		// fall through each case to upgrade to the newVersion
			case 2:
			case 3:
			case 4:
				// create new bridge table
				database.execSQL("DROP TABLE IF EXISTS " + TABLE_BRIDGE);
				onCreate(database);
				MyLog.i(TABLE_BRIDGE, "New " + TABLE_BRIDGE + " created.");
				break;

			default:
				// upgrade version not found!
				MyLog.e(TABLE_BRIDGE, "Upgrade version " + newVersion + " not found!");
				database.execSQL("DROP TABLE IF EXISTS " + TABLE_BRIDGE);
				onCreate(database);
				break;
		}

	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Create Methods
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * This method creates a new BridgeRow in the provided list.
	 * 
	 * @param context
	 * @param listID
	 * @param BridgeRowName
	 * @return Returns the new BridgeRow's ID.
	 */
	public static long CreateNewBridgeRow(Context context, long listID, long storeID, long groupID, long locationID) {

		// get the BridgeRowID ...
		// If the Bridge row already exists, returns that existing Bridge row ID
		// If the Bridge row does not exist, it creates a new Bridge row and returns its ID
		long newBridgeRowID = getBridgeTableRowID(context, listID, storeID, groupID);
		if (newBridgeRowID > 0) {
			// update the new row with its locationID
			ContentValues newFieldValues = new ContentValues();
			newFieldValues.put(COL_LOCATION_ID, locationID);
			UpdateBridgeRowFieldValues(context, newBridgeRowID, newFieldValues);
		}
		return newBridgeRowID;
	}

	public static long CreateNewBridgeRow(Context context, long listID, long storeID, long groupID) {
		long newBridgeRowID = -1;
		ContentResolver cr = context.getContentResolver();
		Uri uri = CONTENT_URI;
		ContentValues values = new ContentValues();
		values.put(COL_LIST_ID, listID);
		values.put(COL_STORE_ID, storeID);
		values.put(COL_GROUP_ID, groupID);
		try {
			Uri newBridgeRowUri = cr.insert(uri, values);
			if (newBridgeRowUri != null) {
				newBridgeRowID = Long.parseLong(newBridgeRowUri.getLastPathSegment());
			}
		} catch (Exception e) {
			MyLog.e("Exception error in CreateNewBridgeRow. ", e.toString());
		}
		return newBridgeRowID;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Read Methods
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	public static long getBridgeTableRowID(Context context, long listID, long storeID, long groupID) {
		long bridgeTableRowID = -1;
		Cursor cursor = null;
		Uri uri = CONTENT_URI;
		String[] projection = PROJECTION_ALL;
		String selection = COL_LIST_ID + " = ? AND " + COL_STORE_ID + " = ? AND " + COL_GROUP_ID + " = ?";
		String selectionArgs[] = new String[] { String.valueOf(listID), String.valueOf(storeID),
				String.valueOf(groupID) };
		String sortOrder = null;
		ContentResolver cr = context.getContentResolver();
		try {
			cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
		} catch (Exception e) {
			MyLog.e("Exception error in BridgeRowsTable: getBridgeRow. ", e.toString());
		}

		if (cursor == null || cursor.getCount() == 0) {
			// bridgeTableRow does not exist... so create one
			bridgeTableRowID = CreateNewBridgeRow(context, listID, storeID, groupID);
		} else {
			// bridgeTable row exists ... so return its ID
			cursor.moveToFirst();
			bridgeTableRowID = cursor.getLong(cursor.getColumnIndexOrThrow(COL_BRIDGE_ID));
		}

		if (cursor != null) {
			cursor.close();
		}
		return bridgeTableRowID;
	}

	public static Cursor getBridgeTableRow(Context context, long bridgeRowID) {
		Cursor cursor = null;
		if (bridgeRowID > 0) {
			Uri uri = Uri.withAppendedPath(CONTENT_URI, String.valueOf(bridgeRowID));
			String[] projection = PROJECTION_ALL;
			String selection = null;
			String selectionArgs[] = null;
			String sortOrder = null;
			ContentResolver cr = context.getContentResolver();
			try {
				cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
			} catch (Exception e) {
				MyLog.e("Exception error in BridgeRowsTable: getBridgeRow. ", e.toString());
			}
		}
		return cursor;
	}

	public static Cursor getStoresInList(Context context, long listID) {
		Cursor cursor = null;
		if (listID > 1) {
			Uri uri = CONTENT_URI;
			String[] projection = new String[] { "DISTINCT " + COL_STORE_ID };
			String selection = COL_LIST_ID + " = ?";
			String selectionArgs[] = { String.valueOf(listID) };
			String sortOrder = null;
			ContentResolver cr = context.getContentResolver();
			try {
				cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
			} catch (Exception e) {
				MyLog.e("Exception error in BridgeRowsTable: getStoresInList. ", e.toString());
			}
		}
		return cursor;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Update Methods
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static int UpdateBridgeRowFieldValues(Context context, long bridgeRowID, ContentValues newFieldValues) {
		int numberOfUpdatedRecords = -1;
		ContentResolver cr = context.getContentResolver();
		Uri BridgeRowUri = Uri.withAppendedPath(CONTENT_URI, String.valueOf(bridgeRowID));
		String selection = null;
		String[] selectionArgs = null;
		numberOfUpdatedRecords = cr.update(BridgeRowUri, newFieldValues, selection, selectionArgs);
		return numberOfUpdatedRecords;
	}

	public static void SetRow(Context context, long listID, long storeID, long groupID, long locationID) {
		long bridgeTableRowID = getBridgeTableRowID(context, listID, storeID, groupID);
		if (bridgeTableRowID > 0) {
			ContentValues values = new ContentValues();
			values.put(COL_LOCATION_ID, locationID);
			UpdateBridgeRowFieldValues(context, bridgeTableRowID, values);
		}
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

	public static int ResetLocationID(Context context, long locationID) {
		int numberOfUpdatedRecords = -1;
		if (locationID > 1) {
			ContentResolver cr = context.getContentResolver();
			Uri uri = CONTENT_URI;
			String where = COL_LOCATION_ID + " = ?";
			String[] whereArgs = new String[] { String.valueOf(locationID) };

			ContentValues values = new ContentValues();
			values.put(COL_LOCATION_ID, 1); // locationID = 1 is the default locationID
			numberOfUpdatedRecords = cr.update(uri, values, where, whereArgs);
		}
		return numberOfUpdatedRecords;
	}

	public static void ReviseBridgeRow(Context context, long listID, long storeID, long groupID, long locationID) {
		long bridgeRowID = getBridgeTableRowID(context, listID, storeID, groupID);
		ContentValues newFieldValues = new ContentValues();
		newFieldValues.put(COL_LOCATION_ID, locationID);
		UpdateBridgeRowFieldValues(context, bridgeRowID, newFieldValues);
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Delete Methods
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	public static int DeleteBridgeRow(Context context, long bridgeRowID) {
		int numberOfDeletedRecords = -1;
		if (bridgeRowID > 0) {
			ContentResolver cr = context.getContentResolver();
			Uri uri = Uri.withAppendedPath(CONTENT_URI, String.valueOf(bridgeRowID));
			String where = null;
			String[] selectionArgs = null;
			cr.delete(uri, where, selectionArgs);
		}
		return numberOfDeletedRecords;
	}

	public static int DeleteAllBridgeRowsInList(Context context, long listID) {
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

	public static int DeleteAllBridgeRowsInGroup(Context context, long groupID) {
		int numberOfDeletedRecords = -1;
		if (groupID > 1) {
			Uri uri = CONTENT_URI;
			String where = COL_GROUP_ID + " = ?";
			String selectionArgs[] = new String[] { String.valueOf(groupID) };
			ContentResolver cr = context.getContentResolver();
			numberOfDeletedRecords = cr.delete(uri, where, selectionArgs);
		}
		return numberOfDeletedRecords;
	}

	public static int DeleteAllBridgeRowsWithStore(Context context, long storeID) {
		int numberOfDeletedRecords = -1;
		if (storeID > 1) {
			Uri uri = CONTENT_URI;
			String where = COL_STORE_ID + " = ?";
			String selectionArgs[] = new String[] { String.valueOf(storeID) };
			ContentResolver cr = context.getContentResolver();
			numberOfDeletedRecords = cr.delete(uri, where, selectionArgs);
		}
		return numberOfDeletedRecords;
	}

	public static int DeleteAllBridgeRowsWithLocation(Context context, long locationID) {
		int numberOfDeletedRecords = -1;
		if (locationID > 1) {
			Uri uri = CONTENT_URI;
			String where = COL_LOCATION_ID + " = ?";
			String selectionArgs[] = new String[] { String.valueOf(locationID) };
			ContentResolver cr = context.getContentResolver();
			numberOfDeletedRecords = cr.delete(uri, where, selectionArgs);
		}
		return numberOfDeletedRecords;
	}

	/*	public static void dbxDeleteSingleRecord(Context mContext, String rowIDstring) {
			// TODO Auto-generated method stub

		}

		public static void dbxDeleteMultipleRecords(Context mContext, Uri uri, String selection, String[] selectionArgs) {
			// TODO Auto-generated method stub

		}*/

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	// SQLite Methods that use Dropbox records
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static long CreateBridgeRow(Context context, DbxRecord dbxRecord) {
		// a check to see if the BridgeRow is already in the database
		// was done prior to making this call ... so don't repeat it.
		long newBridgeRowID = -1;

		ContentValues newFieldValues = setContentValues(dbxRecord);
		Uri uri = CONTENT_URI;
		ContentResolver cr = context.getContentResolver();
		Uri newBridgeRowUri = cr.insert(uri, newFieldValues);
		if (newBridgeRowUri != null) {
			newBridgeRowID = Long.parseLong(newBridgeRowUri.getLastPathSegment());
		}
		return newBridgeRowID;
	}

	public static Cursor getBridgeRowFromDropboxID(Context context, String dbxRecordID) {
		Uri uri = CONTENT_URI;
		String[] projection = PROJECTION_ALL;
		String selection = COL_BRIDGE_DROPBOX_ID + " = '" + dbxRecordID + "'";
		String selectionArgs[] = null;
		String sortOrder = null;

		ContentResolver cr = context.getContentResolver();
		Cursor cursor = null;
		try {
			cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
		} catch (Exception e) {
			MyLog.e("BridgeRowsTable", "Exception error in getBridgeRowFromDropboxID:");
			e.printStackTrace();
		}
		return cursor;
	}

	public static Uri getBridgeRowUri(Context context, String dbxRecordID) {
		Uri BridgeRowUri = null;
		Cursor cursor = getBridgeRowFromDropboxID(context, dbxRecordID);
		if (cursor != null) {
			cursor.moveToFirst();
			long BridgeRowID = cursor.getLong(cursor.getColumnIndexOrThrow(COL_BRIDGE_ID));
			BridgeRowUri = ContentUris.withAppendedId(CONTENT_URI, BridgeRowID);
			cursor.close();
		}
		return BridgeRowUri;
	}

	public static String getDropboxID(Context context, long bridgeRowID) {
		String dbxID = "";
		Cursor cursor = getBridgeTableRow(context, bridgeRowID);
		if (cursor != null) {
			if (cursor.getCount() > 0) {
				cursor.moveToFirst();
				dbxID = cursor.getString(cursor.getColumnIndexOrThrow(COL_BRIDGE_DROPBOX_ID));
			}
			cursor.close();
		}
		return dbxID;
	}

	public static int UpdateBridgeRow(Context context, String dbxRecordID, DbxRecord dbxRecord) {
		int numberOfUpdatedRecords = -1;
		ContentResolver cr = context.getContentResolver();
		Uri BridgeRowUri = getBridgeRowUri(context, dbxRecordID);
		ContentValues newFieldValues = setContentValues(dbxRecord);
		String selection = null;
		String[] selectionArgs = null;
		numberOfUpdatedRecords = cr.update(BridgeRowUri, newFieldValues, selection, selectionArgs);

		return numberOfUpdatedRecords;
	}

	public static int DeleteBridgeRow(Context context, String dbxRecordID) {
		int numberOfDeletedRecords = -1;

		Uri BridgeRowUri = getBridgeRowUri(context, dbxRecordID);
		ContentResolver cr = context.getContentResolver();
		String where = null;
		String[] selectionArgs = null;
		numberOfDeletedRecords = cr.delete(BridgeRowUri, where, selectionArgs);

		return numberOfDeletedRecords;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Dropbox Datastore Methods
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static void dbxInsert(Context context, DbxDatastore dbxDatastore, long BridgeRowID) throws DbxException {
		ContentValues values = setContentValues(context, BridgeRowID);
		DbxRecord dbxRecord = dbxInsert(context, dbxDatastore, BridgeRowID, values);
		setDbxRecordValues(dbxRecord, values);
		dbxDatastore.sync();
	}

	/*	public static void dbxInsertAllBridgeRows(Context context, DbxDatastore dbxDatastore, long listID) throws DbxException {
			Cursor BridgeRowsCursor = getAllBridgeRowsInListCursor(context, listID, null);
			if (BridgeRowsCursor != null) {
				while (BridgeRowsCursor.moveToNext()) {
					ContentValues values = setContentValues(context, BridgeRowsCursor);
					long BridgeRowID = BridgeRowsCursor.getLong(BridgeRowsCursor.getColumnIndexOrThrow(COL_BRIDGE_ID));
					DbxRecord dbxRecord = dbxInsert(context, dbxDatastore, BridgeRowID, values);
					setDbxRecordValues(dbxRecord, values);
				}
				dbxDatastore.sync();
				BridgeRowsCursor.close();
			}
		}*/

	public static DbxRecord dbxInsert(Context context, DbxDatastore dbxDatastore, long bridgeRowID, ContentValues values) {

		DbxRecord newBridgeRowRecord = null;
		if (dbxDatastore != null) {
			DbxTable dbxActiveTable = dbxDatastore.getTable(TABLE_BRIDGE);

			if (dbxActiveTable != null) {

				long listID = -1;
				long groupID = -1;
				long storeID = -1;
				long locationID = -1;

				Set<Entry<String, Object>> s = values.valueSet();
				Iterator<Entry<String, Object>> itr = s.iterator();
				while (itr.hasNext()) {
					Entry<String, Object> me = itr.next();
					String key = me.getKey().toString();

					if (key.equals(COL_LIST_ID)) {
						listID = (Long) me.getValue();

					} else if (key.equals(COL_GROUP_ID)) {
						groupID = (Long) me.getValue();

					} else if (key.equals(COL_STORE_ID)) {
						storeID = (Long) me.getValue();

					} else if (key.equals(COL_LOCATION_ID)) {
						locationID = (Long) me.getValue();
					}
				}

				newBridgeRowRecord = dbxActiveTable.insert()
						.set(COL_LIST_ID, listID)
						.set(COL_GROUP_ID, groupID)
						.set(COL_STORE_ID, storeID)
						.set(COL_LOCATION_ID, locationID);

				// update the SQLite record with the dbxID
				String dbxID = newBridgeRowRecord.getId();
				ContentValues newFieldValues = new ContentValues();
				newFieldValues.put(COL_BRIDGE_DROPBOX_ID, dbxID);
				UpdateBridgeRowFieldValues(context, bridgeRowID, newFieldValues);
				AListContentProvider.setSuppressDropboxChanges(false);

			}
		} else {
			MyLog.e("BridgeRowsTable: dbxInsert ", "Unable to insert record. dbxDatastore is null!");
		}
		return newBridgeRowRecord;

	}

	public static void dbxDeleteSingleRecord(Context context, DbxDatastore dbxDatastore, String BridgeRowIDstring) {
		if (dbxDatastore != null) {
			DbxTable dbxActiveTable = dbxDatastore.getTable(TABLE_BRIDGE);

			String dbxRecordID = getDropboxID(context, Long.parseLong(BridgeRowIDstring));
			if (dbxRecordID != null && !dbxRecordID.isEmpty()) {
				try {
					DbxRecord dbxRecord = dbxActiveTable.get(dbxRecordID);
					if (dbxRecord != null) {
						dbxRecord.deleteRecord();
						dbxDatastore.sync();
					}
				} catch (DbxException e) {
					MyLog.e("BridgeRowsTable: dbxDeleteSingleRecord ",
							"DbxException while trying delete a dropbox record.");
				}
			}
		} else {
			MyLog.e("BridgeRowsTable: dbxDeleteSingleRecord ", "Unable to delete record. dbxDatastore is null!");
		}
	}

	public static void dbxDeleteMultipleRecords(Context context, DbxDatastore dbxDatastore, Uri uri, String selection,
			String[] selectionArgs) {

		if (dbxDatastore != null) {
			DbxTable dbxActiveTable = dbxDatastore.getTable(TABLE_BRIDGE);

			if (dbxActiveTable != null) {
				String projection[] = { COL_BRIDGE_ID, COL_BRIDGE_DROPBOX_ID };
				String sortOrder = null;
				String dbxID;
				DbxRecord dbxRecord;
				ContentResolver cr = context.getContentResolver();
				Cursor cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
				if (cursor != null) {
					try {
						while (cursor.moveToNext()) {
							dbxID = cursor.getString(cursor.getColumnIndexOrThrow(COL_BRIDGE_DROPBOX_ID));
							dbxRecord = dbxActiveTable.get(dbxID);
							if (dbxRecord != null) {
								dbxRecord.deleteRecord();
							}
						}

						dbxDatastore.sync();
					} catch (DbxException e) {
						MyLog.e("BridgeRowsTable: dbxDeleteMultipleRecords ",
								"DbxException while trying to delete multiple dropbox records.");
						e.printStackTrace();

					} finally {
						cursor.close();
					}
				}
			}
		} else {
			MyLog.e("BridgeRowsTable: dbxDeleteMultipleRecords ", "Unable to delete records. dbxDatastore is null!");
		}
	}

	public static void dbxDeleteAllRecords(DbxDatastore dbxDatastore) {
		if (dbxDatastore != null) {
			DbxTable dbxActiveTable = dbxDatastore.getTable(TABLE_BRIDGE);
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
					MyLog.e("BridgeRowsTable: dbxDeleteAllRecords ",
							"DbxException while deleteing all dropbox records.");
					e.printStackTrace();
				}
			}
		} else {
			MyLog.e("BridgeRowsTable: dbxDeleteAllRecords ", "Unable to delete records. dbxDatastore is null!");
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
			DbxTable dbxActiveTable = dbxDatastore.getTable(TABLE_BRIDGE);

			if (dbxActiveTable != null) {
				String projection[] = { COL_BRIDGE_ID, COL_BRIDGE_DROPBOX_ID };
				String sortOrder = null;
				String dbxID;
				DbxRecord dbxRecord;
				ContentResolver cr = context.getContentResolver();
				Cursor cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
				if (cursor != null) {
					try {
						while (cursor.moveToNext()) {
							dbxID = cursor.getString(cursor.getColumnIndexOrThrow(COL_BRIDGE_DROPBOX_ID));
							dbxRecord = dbxActiveTable.get(dbxID);
							if (dbxRecord != null) {
								setDbxRecordValues(dbxRecord, values);
							}
						}

						dbxDatastore.sync();
					} catch (DbxException e) {
						MyLog.e("BridgeRowsTable: dbxUpdateMultipleRecords ",
								"DbxException while trying update records.");
						e.printStackTrace();

					} finally {
						cursor.close();
					}
				}
			}
		} else {
			MyLog.e("BridgeRowsTable: dbxUpdateMultipleRecords ", "Unable to update records. dbxDatastore is null!");
		}
	}

	public static void dbxUpdateSingleRecord(Context context, DbxDatastore dbxDatastore, ContentValues values, Uri uri) {
		if (dbxDatastore != null) {
			DbxTable dbxActiveTable = dbxDatastore.getTable(TABLE_BRIDGE);

			if (dbxActiveTable != null) {
				String rowIDstring = uri.getLastPathSegment();
				String dbxRecordID = getDropboxID(context, Long.parseLong(rowIDstring));
				if (dbxRecordID != null && !dbxRecordID.isEmpty()) {
					try {
						DbxRecord dbxRecord = dbxActiveTable.get(dbxRecordID);
						if (dbxRecord != null) {
							setDbxRecordValues(dbxRecord, values);
							dbxDatastore.sync();
						} else {
							// the dbxBridgeRow has been deleted ...
							// but for some reason it has not been deleted from the sql database
							// so delete it now.
							sqlDeleteBridgeRowAlreadyDeletedFromDropbox(context, dbxRecordID);
							// sync to hopefully capture other dropbox changes
							dbxDatastore.sync();
						}

					} catch (DbxException e) {
						MyLog.e("BridgeRowsTable: dbxUpdateSingleRecord ", "DbxException while trying update records.");
						e.printStackTrace();
					}
				}
			}
		} else {
			MyLog.e("BridgeRowsTable: dbxUpdateSingleRecord ", "Unable to update record. dbxDatastore is null!");
		}
	}

	private static void sqlDeleteBridgeRowAlreadyDeletedFromDropbox(Context context, String dbxRecordID) {
		AListContentProvider.setSuppressDropboxChanges(true);
		DeleteBridgeRow(context, dbxRecordID);
		AListContentProvider.setSuppressDropboxChanges(false);
	}

	private static ContentValues setContentValues(Context context, Cursor cursor) {
		ContentValues newFieldValues = new ContentValues();
		if (cursor != null) {
			for (String col : PROJECTION_ALL) {
				if (col.equals(COL_BRIDGE_ID) || col.equals(COL_BRIDGE_DROPBOX_ID)) {
					// do nothing
				} else {
					long value = cursor.getLong(cursor.getColumnIndexOrThrow(col));
					newFieldValues.put(col, value);
				}
			}
		}
		return newFieldValues;
	}

	private static ContentValues setContentValues(Context context, long BridgeRowID) {
		ContentValues newFieldValues = null;
		Cursor cursor = getBridgeTableRow(context, BridgeRowID);
		if (cursor != null) {
			cursor.moveToFirst();
			newFieldValues = setContentValues(context, cursor);
			cursor.close();
		}
		return newFieldValues;
	}

	/*		+ COL_BRIDGE_ID + " integer primary key autoincrement, "
	+ COL_BRIDGE_DROPBOX_ID + " text, "
	+ COL_LIST_ID + " integer not null references " + ListsTable.TABLE_LISTS + " (" + ListsTable.COL_LIST_ID
	+ ") default 1, "
	+ COL_GROUP_ID + " integer not null references " + GroupsTable.TABLE_GROUPS + " ("
	+ GroupsTable.COL_GROUP_ID + ") default 1, "
	+ COL_STORE_ID + " integer not null references " + StoresTable.TABLE_STORES + " ("
	+ StoresTable.COL_STORE_ID + ") default 1, "
	+ COL_LOCATION_ID + " integer not null references " + LocationsTable.TABLE_LOCATIONS + " ("
	+ LocationsTable.COL_LOCATION_ID + ") default 1 "
	DbxRecord newBridgeRowRecord = null;*/

	private static ContentValues setContentValues(DbxRecord dbxRecord) {
		ContentValues newFieldValues = new ContentValues();

		if (dbxRecord != null) {
			String dbxID = dbxRecord.getId();
			newFieldValues.put(COL_BRIDGE_DROPBOX_ID, dbxID);

			if (dbxRecord.hasField(COL_LIST_ID)) {
				long value = dbxRecord.getLong(COL_LIST_ID);
				newFieldValues.put(COL_LIST_ID, value);
			}

			if (dbxRecord.hasField(COL_GROUP_ID)) {
				long value = dbxRecord.getLong(COL_GROUP_ID);
				newFieldValues.put(COL_GROUP_ID, value);
			}

			if (dbxRecord.hasField(COL_STORE_ID)) {
				long value = dbxRecord.getLong(COL_STORE_ID);
				newFieldValues.put(COL_STORE_ID, value);
			}

			if (dbxRecord.hasField(COL_LOCATION_ID)) {
				long value = dbxRecord.getLong(COL_LOCATION_ID);
				newFieldValues.put(COL_LOCATION_ID, value);
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

				if (key.equals(COL_LIST_ID)) {
					long listID = (Long) me.getValue();
					dbxRecord.set(key, listID);

				} else if (key.equals(COL_GROUP_ID)) {
					long groupID = (Long) me.getValue();
					dbxRecord.set(key, groupID);

				} else if (key.equals(COL_STORE_ID)) {
					long storeID = (Long) me.getValue();
					dbxRecord.set(key, storeID);

				} else if (key.equals(COL_LOCATION_ID)) {
					long locationID = (Long) me.getValue();
					dbxRecord.set(key, locationID);

				} else if (key.equals(COL_BRIDGE_DROPBOX_ID)) {
					// do nothing

				} else {
					MyLog.e("BridgeRowsTable: setDbxRecordValues ", "Unknown column name:" + key);
				}
			}
		}
	}

	/*	public static void replaceSqlRecordsWithDbxRecords(Context context, DbxDatastore dbxDatastore) {

			if (dbxDatastore != null) {
				DbxTable dbxActiveTable = dbxDatastore.getTable(TABLE_BRIDGE);
				if (dbxActiveTable != null) {
					try {
						DbxTable.QueryResult allRecords = dbxActiveTable.query();
						Iterator<DbxRecord> itr = allRecords.iterator();
						while (itr.hasNext()) {
							DbxRecord dbxRecord = itr.next();
							CreateBridgeRow(context, dbxRecord);
						}

					} catch (DbxException e) {
						MyLog.e("BridgeRowsTable: replaceSqlRecordsWithDbxRecords ",
								"DbxException while replacing all sql records.");
						e.printStackTrace();
					}
				}

			} else {
				MyLog.e("BridgeRowsTable: replaceSqlRecordsWithDbxRecords ",
						"Unable to replace sql records. dbxDatastore is null!");
			}
		}*/

	public static void validateSqlRecords(Context context, DbxTable dbxTable) {
		if (dbxTable != null) {

			// Iterate thru the SQL table records and verify if the SQL record exists in the Dbx table.
			// If not ... delete the SQL table record
			Cursor allDbxBridgeRowsCursor = getAllDbxBridgeRowsCursor(context);
			String dbxRecordID = "";
			long sqlRecordID = -1;
			DbxRecord dbxRecord = null;
			if (allDbxBridgeRowsCursor != null && allDbxBridgeRowsCursor.getCount() > 0) {
				while (allDbxBridgeRowsCursor.moveToNext()) {

					try {
						dbxRecordID = allDbxBridgeRowsCursor.getString(allDbxBridgeRowsCursor
								.getColumnIndexOrThrow(COL_BRIDGE_DROPBOX_ID));
						dbxRecord = dbxTable.get(dbxRecordID);
						if (dbxRecord == null) {
							// the SQL table record does not exist in the Dbx table ... so delete it.
							sqlRecordID = allDbxBridgeRowsCursor.getLong(allDbxBridgeRowsCursor
									.getColumnIndexOrThrow(COL_BRIDGE_ID));
							DeleteBridgeRow(context, sqlRecordID);
						}
					} catch (DbxException e) {
						MyLog.e("BridgeRowsTable: validateSqlRecords ", "DbxException while iterating thru SQL table.");
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
					Cursor BridgeRowCursor = getBridgeRowFromDropboxID(context, dbxRecordID);
					if (BridgeRowCursor != null && BridgeRowCursor.getCount() > 0) {
						// update the existing record
						UpdateBridgeRow(context, dbxRecordID, dbxRecord);
					} else {
						// create a new record
						CreateBridgeRow(context, dbxRecord);
					}
					if (BridgeRowCursor != null) {
						BridgeRowCursor.close();
					}
				}
			} catch (DbxException e) {
				MyLog.e("BridgeRowsTable: validateSqlRecords ", "DbxException while iterating thru DbxTable.");
				e.printStackTrace();
			}

			if (allDbxBridgeRowsCursor != null) {
				allDbxBridgeRowsCursor.close();
			}
		}

	}

	private static Cursor getAllDbxBridgeRowsCursor(Context context) {
		Cursor cursor = null;

		Uri uri = CONTENT_URI;
		String[] projection = new String[] { COL_BRIDGE_ID, COL_BRIDGE_DROPBOX_ID };

		String selection = COL_BRIDGE_DROPBOX_ID + " != '' OR " + COL_BRIDGE_DROPBOX_ID + " NOT NULL";
		String selectionArgs[] = null;

		ContentResolver cr = context.getContentResolver();
		try {
			cursor = cr.query(uri, projection, selection, selectionArgs, null);
		} catch (Exception e) {
			MyLog.e("Exception error  in getAllDbxBridgeRowsCursor. ", "");
			e.printStackTrace();
		}
		return cursor;
	}
}
