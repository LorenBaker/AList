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

public class StoresTable {

	// Lists data table
	public static final String TABLE_STORES = "tblStores";
	public static final String COL_STORE_ID = "_id"; // 0
	public static final String COL_STORE_DROPBOX_ID = "storeDropboxID";
	public static final String COL_STORE_NAME = "storeName"; // 1
	public static final String COL_LIST_ID = "listTitleID"; // 2
	public static final String COL_STREET1 = "street1"; // 3
	public static final String COL_STREET2 = "street2"; // 4
	public static final String COL_CITY = "city"; // 5
	public static final String COL_STATE = "state"; // 6
	public static final String COL_ZIP = "zip"; // 7
	public static final String COL_GPS_LATITUDE = "gpsLatitude"; // 8
	public static final String COL_GPS_LONGITUDE = "gpsLongitude"; // 9
	public static final String COL_WEBSITE_URL = "websiteURL"; // 10
	public static final String COL_PHONE_NUMBER = "phoneNumber"; // 11
	public static final String COL_LAST_STORE_LOCATION_ID = "lastStoreLocationID";

	public static final String[] PROJECTION_ALL = { COL_STORE_ID, COL_STORE_DROPBOX_ID, COL_STORE_NAME, COL_LIST_ID,
			COL_STREET1, COL_STREET2, COL_CITY, COL_STATE, COL_ZIP,
			COL_GPS_LATITUDE, COL_GPS_LONGITUDE, COL_WEBSITE_URL, COL_PHONE_NUMBER, COL_LAST_STORE_LOCATION_ID
	};

	public static final String CONTENT_PATH = TABLE_STORES;
	public static final String CONTENT_LIST_WITH_GROUP = "listWithGroup";

	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + "vnd.lbconsulting."
			+ TABLE_STORES;
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + "vnd.lbconsulting."
			+ TABLE_STORES;
	public static final Uri CONTENT_URI = Uri.parse("content://" + AListContentProvider.AUTHORITY + "/" + CONTENT_PATH);
	public static final Uri LIST_WITH_group_URI = Uri.parse("content://" + AListContentProvider.AUTHORITY + "/"
			+ CONTENT_LIST_WITH_GROUP);

	// Version 1
	public static final String SORT_ORDER_STORE_NAME = COL_STORE_NAME + " ASC";
	public static final String SORT_ORDER_CITY = COL_CITY + " ASC";
	public static final String SORT_ORDER_STATE = COL_STATE + " ASC";
	public static final String SORT_ORDER_ZIP = COL_ZIP + " ASC";

	// Database creation SQL statements
	private static final String DATATABLE_CREATE =
			"create table " + TABLE_STORES
					+ " ("
					+ COL_STORE_ID + " integer primary key autoincrement, "
					+ COL_STORE_DROPBOX_ID + " text, "
					+ COL_STORE_NAME + " text collate nocase, "
					+ COL_LIST_ID + " integer not null references "
					+ ListsTable.TABLE_LISTS + " (" + ListsTable.COL_LIST_ID + "), "
					+ COL_STREET1 + " text, "
					+ COL_STREET2 + " text, "
					+ COL_CITY + " text collate nocase, "
					+ COL_STATE + " text collate nocase, "
					+ COL_ZIP + " text collate nocase, "
					+ COL_GPS_LATITUDE + " text, "
					+ COL_GPS_LONGITUDE + " text, "
					+ COL_WEBSITE_URL + " text, "
					+ COL_PHONE_NUMBER + " text, "
					+ COL_LAST_STORE_LOCATION_ID + " integer default 1"
					+ ");";

	private static String defalutStoreValue = "[No Store]";

	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(DATATABLE_CREATE);
		MyLog.i("StoresTable", "onCreate: " + TABLE_STORES + " created.");

		String insertProjection = "insert into "
				+ TABLE_STORES
				+ " ("
				+ COL_STORE_ID + ", "
				+ COL_LIST_ID + ", "
				+ COL_STORE_NAME + ", "
				+ COL_CITY + ", "
				+ COL_STATE
				+ ") VALUES ";

		ArrayList<String> sqlStatements = new ArrayList<String>();
		sqlStatements.add(insertProjection + "(NULL,1, '" + defalutStoreValue + "', '', '')");

		// Stores for Groceries List (2)
		/*
		 * sqlStatements.add(insertProjection +
		 * "(NULL,2, 'Safeway-Factoria', 'Bellevue', 'WA')");
		 * sqlStatements.add(insertProjection +
		 * "(NULL,2, 'QFC-Factoria', 'Bellevue', 'WA')");
		 * sqlStatements.add(insertProjection +
		 * "(NULL,2, 'QFC-Issaquah', 'Bellevue', 'WA')");
		 * sqlStatements.add(insertProjection +
		 * "(NULL,2, 'Albertson-Eastgate', 'Bellevue', 'WA')");
		 * 
		 * // Stores for ToDo List (3) sqlStatements.add(insertProjection +
		 * "(NULL,3, 'ToDo Store 1', 'Anywhere 1', 'OR')");
		 * sqlStatements.add(insertProjection +
		 * "(NULL,3, 'ToDo Store 2', 'Anywhere 2', 'WA')");
		 * sqlStatements.add(insertProjection +
		 * "(NULL,3, 'ToDo Store 3', 'Anywhere 3', 'CA')");
		 * sqlStatements.add(insertProjection +
		 * "(NULL,3, 'ToDo Store 4', 'Anywhere 4', 'NY')");
		 * sqlStatements.add(insertProjection +
		 * "(NULL,3, 'ToDo Store 5', 'Anywhere 5', 'UT')");
		 */

		AListUtilities.execMultipleSQL(database, sqlStatements);
	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		/*
		 * database.execSQL("DROP TABLE IF EXISTS " + TABLE_STORES);
		 * onCreate(database);
		 */
		MyLog.w(TABLE_STORES, ": Upgrading database from version " + oldVersion + " to version " + newVersion
				+ ". NO CHANGES REQUIRED.");

		int upgradeToVersion = oldVersion + 1;
		switch (upgradeToVersion) {
		// fall through each case to upgrade to the newVersion
			case 2:
			case 3:
			case 4:
				// No changes in TABLE_STORES
				break;

			default:
				// upgrade version not found!
				MyLog.e(TABLE_STORES, "Upgrade version " + newVersion + " not found!");
				database.execSQL("DROP TABLE IF EXISTS " + TABLE_STORES);
				onCreate(database);
				break;
		}
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Create Methods
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	public static long CreateNewStore(Context context, long listID, String storeName) {
		long newStoreID = -1;
		if (listID > 1) {
			// verify that the store does not already exist in the table
			Cursor cursor = getStore(context, listID, storeName);
			if (cursor != null && cursor.getCount() > 0) {
				// the store exists in the table ... so return its id
				cursor.moveToFirst();
				newStoreID = cursor.getLong(cursor.getColumnIndexOrThrow(COL_STORE_ID));
				cursor.close();
			} else {
				// store does not exist in the table ... so add it
				if (storeName != null) {
					storeName = storeName.trim();
					if (!storeName.isEmpty()) {
						try {
							ContentResolver cr = context.getContentResolver();
							Uri uri = CONTENT_URI;
							ContentValues values = new ContentValues();
							values.put(COL_LIST_ID, listID);
							values.put(COL_STORE_NAME, storeName);
							Uri newListUri = cr.insert(uri, values);
							if (newListUri != null) {

								newStoreID = Long.parseLong(newListUri.getLastPathSegment());
							}
						} catch (Exception e) {
							MyLog.e("Exception error in CreateNewStore. ", e.toString());
						}

					} else {
						MyLog.e("StoresTable", "Error in CreateNewStore; storeName is Empty!");
					}
				} else {
					MyLog.e("StoresTable", "Error in CreateNewStore; storeName is Null!");
				}
			}

			if (cursor != null) {
				cursor.close();
			}

			// Fill the bridge table with default location
			if (newStoreID > 0) {
				Cursor groupsCursor = GroupsTable.getAllGroupIDsInList(context, listID);
				if (groupsCursor != null) {
					if (groupsCursor.getCount() > 0) {
						groupsCursor.moveToPosition(-1);
						long groupID = -1;
						while (groupsCursor.moveToNext()) {
							groupID = groupsCursor
									.getLong(groupsCursor.getColumnIndexOrThrow(GroupsTable.COL_GROUP_ID));
							BridgeTable.CreateNewBridgeRow(context, listID, newStoreID, groupID, 1);
						}
					}
					groupsCursor.close();
				}
			}
		}
		return newStoreID;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Read Methods
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	public static Cursor getStore(Context context, long storeID) {
		Cursor cursor = null;
		if (storeID > 0) {
			Uri uri = Uri.withAppendedPath(CONTENT_URI, String.valueOf(storeID));
			String[] projection = PROJECTION_ALL;
			String selection = null;
			String selectionArgs[] = null;
			String sortOrder = null;
			ContentResolver cr = context.getContentResolver();
			try {
				cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
			} catch (Exception e) {
				MyLog.e("Exception error in StoresTable: getStore. ", e.toString());
			}
		}
		return cursor;
	}

	public static Cursor getStore(Context context, long listID, String storeName) {
		Cursor cursor = null;
		if (listID > 1) {
			Uri uri = CONTENT_URI;
			String[] projection = PROJECTION_ALL;
			String selection = COL_LIST_ID + " = ? AND " + COL_STORE_NAME + " = ?";
			String selectionArgs[] = new String[] { String.valueOf(listID), storeName };
			String sortOrder = null;
			ContentResolver cr = context.getContentResolver();
			try {
				cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
			} catch (Exception e) {
				MyLog.e("Exception error in StoresTable: getStore. ", e.toString());
			}
		}
		return cursor;
	}

	public static Cursor getAllStoresInListCursor(Context context, long listID, String sortOrder) {
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
				MyLog.e("Exception error in StoresTable: getAllStoresInListCursor. ", e.toString());
			}
		}
		return cursor;
	}

	public static CursorLoader getAllStoresInListExcludeDefaultStore(Context context, long listID,
			String sortOrder) {
		CursorLoader cursorLoader = null;
		if (listID > 1) {
			Uri uri = CONTENT_URI;
			String[] projection = PROJECTION_ALL;
			String selection = COL_LIST_ID + " = ?";
			String selectionArgs[] = new String[] { String.valueOf(listID) };
			try {
				cursorLoader = new CursorLoader(context, uri, projection, selection, selectionArgs, sortOrder);

			} catch (Exception e) {
				MyLog.e("Exception error in StoresTable: getAllStoresInListExcludeDefaultStore. ", e.toString());
			}
		}
		return cursorLoader;
	}

	public static String getStoreDisplayName(Context context, long storeID) {
		String displayName = "";
		Cursor cursor = getStore(context, storeID);

		if (cursor != null) {
			cursor.moveToFirst();
			StringBuilder sb = new StringBuilder();
			sb.append(cursor.getString(cursor.getColumnIndexOrThrow(COL_STORE_NAME)));
			String city = cursor.getString(cursor.getColumnIndexOrThrow(COL_CITY));
			if (city != null && !city.isEmpty()) {
				sb.append(", ");
				sb.append(city);
			}
			String state = cursor.getString(cursor.getColumnIndexOrThrow(COL_STATE));
			if (state != null && !state.isEmpty()) {
				sb.append(", ");
				sb.append(state);
			}
			cursor.close();
			displayName = sb.toString();
		}
		return displayName;
	}

	public static String getStoreName(Context context, long storeID) {
		String storeName = "";
		Cursor cursor = getStore(context, storeID);
		if (cursor != null) {
			cursor.moveToFirst();
			storeName = cursor.getString(cursor.getColumnIndexOrThrow(COL_STORE_NAME));
			cursor.close();
		}
		return storeName;
	}

	public static int getStoresCountInList(Context context, long listID) {
		int storeCount = -1;
		Cursor cursor = getAllStoresInListCursor(context, listID, null);
		if (cursor != null) {
			storeCount = cursor.getCount();
			cursor.close();
		}
		return storeCount;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Update Methods
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static int UpdateAllStoreFields(
			Context context,
			long storeID,
			String storeName,
			String street1,
			String street2,
			String city,
			String state,
			String zip,
			String gpsLatitude,
			String gpsLongitude,
			String websiteURL,
			String phoneNumber
			) {
		int numberOfUpdatedRecords = -1;
		if (storeID > 1) {
			try {
				ContentResolver cr = context.getContentResolver();
				Uri uri = Uri.withAppendedPath(CONTENT_URI, String.valueOf(storeID));
				String where = null;
				String[] whereArgs = null;

				if (storeName == null) {
					storeName = "";
				}
				if (street1 == null) {
					street1 = "";
				}
				if (street2 == null) {
					street2 = "";
				}
				if (city == null) {
					city = "";
				}
				if (state == null) {
					state = "";
				}
				if (zip == null) {
					zip = "";
				}
				if (gpsLatitude == null) {
					gpsLatitude = "";
				}
				if (gpsLongitude == null) {
					gpsLongitude = "";
				}
				if (websiteURL == null) {
					websiteURL = "";
				}
				if (phoneNumber == null) {
					phoneNumber = "";
				}

				ContentValues values = new ContentValues();
				values.put(COL_STORE_NAME, storeName.trim());
				values.put(COL_STREET1, street1.trim());
				values.put(COL_STREET2, street2.trim());
				values.put(COL_CITY, city.trim());
				values.put(COL_STATE, state.trim());
				values.put(COL_ZIP, zip.trim());
				values.put(COL_GPS_LATITUDE, gpsLatitude.trim());
				values.put(COL_GPS_LONGITUDE, gpsLongitude.trim());
				values.put(COL_WEBSITE_URL, websiteURL.trim());
				values.put(COL_PHONE_NUMBER, phoneNumber.trim());

				numberOfUpdatedRecords = cr.update(uri, values, where, whereArgs);
			} catch (Exception e) {
				MyLog.e("Exception error in StoresTable: UpdateAllStoreFields. ", e.toString());
			}
		}
		return numberOfUpdatedRecords;
	}

	public static String getStoreField(Context context, long storeID, String ColumnName) {
		String result = "";
		Cursor cursor = getStore(context, storeID);

		if (cursor != null) {
			cursor.moveToFirst();
			int position = cursor.getColumnIndex(ColumnName);
			if (position != -1) {
				switch (position) {
					case 1:
					case 3:
					case 4:
					case 5:
					case 6:
					case 7:
					case 8:
					case 9:
					case 10:
					case 11:
						result = cursor.getString(position);
						break;

					default:
						break;
				}
			} else {
				// invalid ColumnName
				MyLog.e("StoresTable: getStoreField", "ColumnName " + "\"" + ColumnName + "\"" + " is not valid!");
			}
			cursor.close();
		}
		return result;
	}

	public static int UpdateStoreTableFieldValues(Context context, long storeID, ContentValues newFieldValues) {
		int numberOfUpdatedRecords = -1;
		if (storeID > 1) {
			ContentResolver cr = context.getContentResolver();
			Uri defaultUri = Uri.withAppendedPath(CONTENT_URI, String.valueOf(storeID));
			String selection = null;
			String[] selectionArgs = null;
			numberOfUpdatedRecords = cr.update(defaultUri, newFieldValues, selection, selectionArgs);
		}
		return numberOfUpdatedRecords;
	}

	public static int setStoreField(Context context, long storeID, String ColumnName, String storeFieldValue) {
		int numberOfUpdatedRecords = -1;
		if (storeID > 1) {
			ContentResolver cr = context.getContentResolver();
			Uri uri = Uri.withAppendedPath(CONTENT_URI, String.valueOf(storeID));
			ContentValues values = new ContentValues();
			values.put(ColumnName, storeFieldValue);
			String selection = null;
			String[] selectionArgs = null;
			try {
				numberOfUpdatedRecords = cr.update(uri, values, selection, selectionArgs);
				if (numberOfUpdatedRecords != 1) {
					MyLog.e("StoresTable: setStoreField", "The number of updated Store records does not equal 1!");
				}
			} catch (Exception e) {
				MyLog.e("Exception error in StoresTable: setStoreField. ", e.toString());
			}

		}
		return numberOfUpdatedRecords;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Delete Methods
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static int DeleteStore(Context context, long storeID) {
		int numberOfDeletedRecords = -1;
		if (storeID > 1) {
			ContentResolver cr = context.getContentResolver();
			Uri uri = CONTENT_URI;
			String where = COL_STORE_ID + " = ?";
			String[] selectionArgs = { String.valueOf(storeID) };
			numberOfDeletedRecords = cr.delete(uri, where, selectionArgs);
		}
		BridgeTable.DeleteAllBridgeRowsWithStore(context, storeID);
		return numberOfDeletedRecords;
	}

	public static int DeleteAllStoresInList(Context context, long listID) {
		int numberOfDeletedRecords = -1;
		if (listID > 1) {
			Uri uri = CONTENT_URI;
			String where = COL_LIST_ID + " = ?";
			String selectionArgs[] = new String[] { String.valueOf(listID) };
			ContentResolver cr = context.getContentResolver();
			numberOfDeletedRecords = cr.delete(uri, where, selectionArgs);
		}
		// Note: Bridge Table rows associated with store
		// have already been deleted by ListTable.DeleteList
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

	public static long CreateStore(Context context, DbxRecord dbxRecord) {
		// a check to see if the Store is already in the database
		// was done prior to making this call ... so don't repeat it.
		long newStoreID = -1;

		ContentValues newFieldValues = setContentValues(dbxRecord);
		Uri uri = CONTENT_URI;
		ContentResolver cr = context.getContentResolver();
		Uri newStoreUri = cr.insert(uri, newFieldValues);
		if (newStoreUri != null) {
			newStoreID = Long.parseLong(newStoreUri.getLastPathSegment());
		}
		return newStoreID;
	}

	public static Cursor getStoreFromDropboxID(Context context, String dbxRecordID) {
		Uri uri = CONTENT_URI;
		String[] projection = PROJECTION_ALL;
		String selection = COL_STORE_DROPBOX_ID + " = '" + dbxRecordID + "'";
		String selectionArgs[] = null;
		String sortOrder = SORT_ORDER_STORE_NAME;

		ContentResolver cr = context.getContentResolver();
		Cursor cursor = null;
		try {
			cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
		} catch (Exception e) {
			MyLog.e("StoresTable", "Exception error in getStoreFromDropboxID:");
			e.printStackTrace();
		}
		return cursor;
	}

	public static Uri getStoreUri(Context context, String dbxRecordID) {
		Uri StoreUri = null;
		Cursor cursor = getStoreFromDropboxID(context, dbxRecordID);
		if (cursor != null) {
			cursor.moveToFirst();
			long StoreID = cursor.getLong(cursor.getColumnIndexOrThrow(COL_STORE_ID));
			StoreUri = ContentUris.withAppendedId(StoresTable.CONTENT_URI, StoreID);
			cursor.close();
		}
		return StoreUri;
	}

	public static String getDropboxID(Context context, long StoreID) {
		String dbxID = "";
		Cursor cursor = getStore(context, StoreID);
		if (cursor != null) {
			if (cursor.getCount() > 0) {
				cursor.moveToFirst();
				dbxID = cursor.getString(cursor.getColumnIndexOrThrow(COL_STORE_DROPBOX_ID));
			}
			cursor.close();
		}

		return dbxID;
	}

	public static int UpdateStore(Context context, String dbxRecordID, DbxRecord dbxRecord) {
		int numberOfUpdatedRecords = -1;
		ContentResolver cr = context.getContentResolver();
		Uri StoreUri = getStoreUri(context, dbxRecordID);
		ContentValues newFieldValues = setContentValues(dbxRecord);
		String selection = null;
		String[] selectionArgs = null;
		numberOfUpdatedRecords = cr.update(StoreUri, newFieldValues, selection, selectionArgs);

		return numberOfUpdatedRecords;
	}

	public static int DeleteStore(Context context, String dbxRecordID) {
		int numberOfDeletedRecords = -1;

		Uri StoreUri = getStoreUri(context, dbxRecordID);
		ContentResolver cr = context.getContentResolver();
		String where = null;
		String[] selectionArgs = null;
		numberOfDeletedRecords = cr.delete(StoreUri, where, selectionArgs);

		return numberOfDeletedRecords;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Dropbox Datastore Methods
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static void dbxInsert(Context context, DbxDatastore dbxDatastore, long StoreID) throws DbxException {
		ContentValues values = setContentValues(context, StoreID);
		DbxRecord dbxRecord = dbxInsert(context, dbxDatastore, StoreID, values);
		setDbxRecordValues(dbxRecord, values);
		dbxDatastore.sync();
	}

	public static void dbxInsertAllStores(Context context, DbxDatastore dbxDatastore, long listID) throws DbxException {
		Cursor StoresCursor = getAllStoresInListCursor(context, listID, null);
		if (StoresCursor != null) {
			while (StoresCursor.moveToNext()) {
				ContentValues values = setContentValues(context, StoresCursor);
				long StoreID = StoresCursor.getLong(StoresCursor.getColumnIndexOrThrow(COL_STORE_ID));
				DbxRecord dbxRecord = dbxInsert(context, dbxDatastore, StoreID, values);
				setDbxRecordValues(dbxRecord, values);
			}
			dbxDatastore.sync();
			StoresCursor.close();
		}
	}

	public static DbxRecord dbxInsert(Context context, DbxDatastore dbxDatastore, long StoreID, ContentValues values) {

		DbxRecord newStoreRecord = null;
		if (dbxDatastore != null) {
			DbxTable dbxActiveTable = dbxDatastore.getTable(TABLE_STORES);

			if (dbxActiveTable != null) {

				Set<Entry<String, Object>> s = values.valueSet();
				Iterator<Entry<String, Object>> itr = s.iterator();
				while (itr.hasNext()) {
					Entry<String, Object> me = itr.next();
					String key = me.getKey().toString();

					if (key.equals(COL_STORE_NAME)) {
						String StoreName = (String) me.getValue();
						newStoreRecord = dbxActiveTable.insert()
								.set(key, StoreName)
								.set(COL_LIST_ID, -1)
								.set(COL_STREET1, "")
								.set(COL_STREET2, "")
								.set(COL_CITY, "")
								.set(COL_STATE, "")
								.set(COL_ZIP, "")
								.set(COL_GPS_LATITUDE, "")
								.set(COL_GPS_LONGITUDE, "")
								.set(COL_WEBSITE_URL, "")
								.set(COL_PHONE_NUMBER, "")
								.set(COL_LAST_STORE_LOCATION_ID, 1);

						// update the SQLite record with the dbxID
						AListContentProvider.setSuppressDropboxChanges(true);
						String dbxID = newStoreRecord.getId();
						ContentValues newFieldValues = new ContentValues();
						newFieldValues.put(COL_STORE_DROPBOX_ID, dbxID);
						UpdateStoreTableFieldValues(context, StoreID, newFieldValues);

						MyLog.d("StoresTable: dbxInsert ", key + ":" + StoreName);
						AListContentProvider.setSuppressDropboxChanges(false);

					} else if (key.equals(COL_LIST_ID)) {
						long listID = (Long) me.getValue();
						if (newStoreRecord != null) {
							newStoreRecord.set(key, listID);
							MyLog.d("StoresTable: dbxInsert ", key + ":" + listID);
						}
					}
				}
			}
		} else {
			MyLog.e("StoresTable: dbxInsert ", "Unable to insert record. dbxDatastore is null!");
		}
		return newStoreRecord;

	}

	public static void dbxDeleteSingleRecord(Context context, DbxDatastore dbxDatastore, String StoreIDstring) {
		if (dbxDatastore != null) {
			DbxTable dbxActiveTable = dbxDatastore.getTable(TABLE_STORES);

			String dbxRecordID = getDropboxID(context, Long.parseLong(StoreIDstring));
			if (dbxRecordID != null && !dbxRecordID.isEmpty()) {
				try {
					DbxRecord dbxRecord = dbxActiveTable.get(dbxRecordID);
					if (dbxRecord != null) {
						dbxRecord.deleteRecord();
						dbxDatastore.sync();
					}
				} catch (DbxException e) {
					MyLog.e("StoresTable: dbxDeleteSingleRecord ", "DbxException while trying delete a dropbox record.");
				}
			}
		} else {
			MyLog.e("StoresTable: dbxDeleteSingleRecord ", "Unable to delete record. dbxDatastore is null!");
		}
	}

	public static void dbxDeleteMultipleRecords(Context context, DbxDatastore dbxDatastore, Uri uri, String selection,
			String[] selectionArgs) {

		if (dbxDatastore != null) {
			DbxTable dbxActiveTable = dbxDatastore.getTable(TABLE_STORES);

			if (dbxActiveTable != null) {
				String projection[] = { COL_STORE_ID, COL_STORE_DROPBOX_ID };
				String sortOrder = null;
				String dbxID;
				DbxRecord dbxRecord;
				ContentResolver cr = context.getContentResolver();
				Cursor cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
				if (cursor != null) {
					try {
						while (cursor.moveToNext()) {
							dbxID = cursor.getString(cursor.getColumnIndexOrThrow(COL_STORE_DROPBOX_ID));
							dbxRecord = dbxActiveTable.get(dbxID);
							if (dbxRecord != null) {
								dbxRecord.deleteRecord();
							}
						}

						dbxDatastore.sync();
					} catch (DbxException e) {
						MyLog.e("StoresTable: dbxDeleteMultipleRecords ",
								"DbxException while trying to delete multiple dropbox records.");
						e.printStackTrace();

					} finally {
						cursor.close();
					}
				}
			}
		} else {
			MyLog.e("StoresTable: dbxDeleteMultipleRecords ", "Unable to delete records. dbxDatastore is null!");
		}
	}

	public static void dbxDeleteAllRecords(DbxDatastore dbxDatastore) {
		if (dbxDatastore != null) {
			DbxTable dbxActiveTable = dbxDatastore.getTable(TABLE_STORES);
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
					MyLog.e("StoresTable: dbxDeleteAllRecords ", "DbxException while deleteing all dropbox records.");
					e.printStackTrace();
				}
			}
		} else {
			MyLog.e("StoresTable: dbxDeleteAllRecords ", "Unable to delete records. dbxDatastore is null!");
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
			DbxTable dbxActiveTable = dbxDatastore.getTable(TABLE_STORES);

			if (dbxActiveTable != null) {
				String projection[] = { COL_STORE_ID, COL_STORE_DROPBOX_ID };
				String sortOrder = null;
				String dbxID;
				DbxRecord dbxRecord;
				ContentResolver cr = context.getContentResolver();
				Cursor cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
				if (cursor != null) {
					try {
						while (cursor.moveToNext()) {
							dbxID = cursor.getString(cursor.getColumnIndexOrThrow(COL_STORE_DROPBOX_ID));
							dbxRecord = dbxActiveTable.get(dbxID);
							if (dbxRecord != null) {
								setDbxRecordValues(dbxRecord, values);
							}
						}

						dbxDatastore.sync();
					} catch (DbxException e) {
						MyLog.e("StoresTable: dbxUpdateMultipleRecords ", "DbxException while trying update records.");
						e.printStackTrace();

					} finally {
						cursor.close();
					}
				}
			}
		} else {
			MyLog.e("StoresTable: dbxUpdateMultipleRecords ", "Unable to update records. dbxDatastore is null!");
		}
	}

	public static void dbxUpdateSingleRecord(Context context, DbxDatastore dbxDatastore, ContentValues values, Uri uri) {
		if (dbxDatastore != null) {
			DbxTable dbxActiveTable = dbxDatastore.getTable(TABLE_STORES);

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
							// the dbxStore has been deleted ...
							// but for some reason it has not been deleted from the sql database
							// so delete it now.
							sqlDeleteStoreAlreadyDeletedFromDropbox(context, dbxRecordID);
							// sync to hopefully capture other dropbox changes
							dbxDatastore.sync();
						}

					} catch (DbxException e) {
						MyLog.e("StoresTable: dbxUpdateSingleRecord ", "DbxException while trying update records.");
						e.printStackTrace();
					}
				}
			}
		} else {
			MyLog.e("StoresTable: dbxUpdateSingleRecord ", "Unable to update record. dbxDatastore is null!");
		}
	}

	private static void sqlDeleteStoreAlreadyDeletedFromDropbox(Context context, String dbxRecordID) {
		AListContentProvider.setSuppressDropboxChanges(true);
		DeleteStore(context, dbxRecordID);
		AListContentProvider.setSuppressDropboxChanges(false);
	}

	private static ContentValues setContentValues(Context context, Cursor cursor) {
		ContentValues newFieldValues = new ContentValues();
		if (cursor != null) {
			for (String col : PROJECTION_ALL) {
				if (col.equals(COL_STORE_ID) || col.equals(COL_STORE_DROPBOX_ID)) {
					// do nothing
				} else if (col.equals(COL_LIST_ID) || col.equals(COL_LAST_STORE_LOCATION_ID)) {
					long value = cursor.getLong(cursor.getColumnIndexOrThrow(col));
					newFieldValues.put(col, value);

				} else {
					String value = cursor.getString(cursor.getColumnIndexOrThrow(col));
					newFieldValues.put(col, value);
				}
			}
		}
		return newFieldValues;
	}

	private static ContentValues setContentValues(Context context, long StoreID) {
		ContentValues newFieldValues = null;
		Cursor cursor = getStore(context, StoreID);
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
			newFieldValues.put(COL_STORE_DROPBOX_ID, dbxID);

			if (dbxRecord.hasField(COL_STORE_NAME)) {
				String StoreName = dbxRecord.getString(COL_STORE_NAME);
				newFieldValues.put(COL_STORE_NAME, StoreName);
			}

			if (dbxRecord.hasField(COL_LIST_ID)) {
				long listID = dbxRecord.getLong(COL_LIST_ID);
				newFieldValues.put(COL_LIST_ID, listID);
			}

			if (dbxRecord.hasField(COL_STREET1)) {
				String string = dbxRecord.getString(COL_STREET1);
				newFieldValues.put(COL_STREET1, string);
			}

			if (dbxRecord.hasField(COL_STREET2)) {
				String string = dbxRecord.getString(COL_STREET2);
				newFieldValues.put(COL_STREET2, string);
			}

			if (dbxRecord.hasField(COL_CITY)) {
				String string = dbxRecord.getString(COL_CITY);
				newFieldValues.put(COL_CITY, string);
			}

			if (dbxRecord.hasField(COL_STATE)) {
				String string = dbxRecord.getString(COL_STATE);
				newFieldValues.put(COL_STATE, string);
			}

			if (dbxRecord.hasField(COL_ZIP)) {
				String string = dbxRecord.getString(COL_ZIP);
				newFieldValues.put(COL_ZIP, string);
			}

			if (dbxRecord.hasField(COL_GPS_LATITUDE)) {
				String string = dbxRecord.getString(COL_GPS_LATITUDE);
				newFieldValues.put(COL_GPS_LATITUDE, string);
			}

			if (dbxRecord.hasField(COL_GPS_LONGITUDE)) {
				String string = dbxRecord.getString(COL_GPS_LONGITUDE);
				newFieldValues.put(COL_GPS_LONGITUDE, string);
			}

			if (dbxRecord.hasField(COL_WEBSITE_URL)) {
				String string = dbxRecord.getString(COL_WEBSITE_URL);
				newFieldValues.put(COL_WEBSITE_URL, string);
			}

			if (dbxRecord.hasField(COL_PHONE_NUMBER)) {
				String string = dbxRecord.getString(COL_PHONE_NUMBER);
				newFieldValues.put(COL_PHONE_NUMBER, string);
			}

			if (dbxRecord.hasField(COL_LAST_STORE_LOCATION_ID)) {
				long lastStoreLocationID = dbxRecord.getLong(COL_LAST_STORE_LOCATION_ID);
				newFieldValues.put(COL_LAST_STORE_LOCATION_ID, lastStoreLocationID);
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

				if (key.equals(COL_STORE_NAME)) {
					String string = (String) me.getValue();
					if (string == null) {
						string = "";
					}
					dbxRecord.set(key, string);

				} else if (key.equals(COL_LIST_ID)) {
					long listID = (Long) me.getValue();
					dbxRecord.set(key, listID);

				} else if (key.equals(COL_STREET1)) {
					String string = (String) me.getValue();
					if (string == null) {
						string = "";
					}
					dbxRecord.set(key, string);

				} else if (key.equals(COL_STREET2)) {
					String string = (String) me.getValue();
					if (string == null) {
						string = "";
					}
					dbxRecord.set(key, string);

				} else if (key.equals(COL_CITY)) {
					String string = (String) me.getValue();
					if (string == null) {
						string = "";
					}
					dbxRecord.set(key, string);

				} else if (key.equals(COL_STATE)) {
					String string = (String) me.getValue();
					if (string == null) {
						string = "";
					}
					dbxRecord.set(key, string);

				} else if (key.equals(COL_ZIP)) {
					String string = (String) me.getValue();
					if (string == null) {
						string = "";
					}
					dbxRecord.set(key, string);

				} else if (key.equals(COL_GPS_LATITUDE)) {
					String string = (String) me.getValue();
					if (string == null) {
						string = "";
					}
					dbxRecord.set(key, string);

				} else if (key.equals(COL_GPS_LONGITUDE)) {
					String string = (String) me.getValue();
					if (string == null) {
						string = "";
					}
					dbxRecord.set(key, string);

				} else if (key.equals(COL_WEBSITE_URL)) {
					String string = (String) me.getValue();
					if (string == null) {
						string = "";
					}
					dbxRecord.set(key, string);

				} else if (key.equals(COL_PHONE_NUMBER)) {
					String string = (String) me.getValue();
					if (string == null) {
						string = "";
					}
					dbxRecord.set(key, string);

				} else if (key.equals(COL_LAST_STORE_LOCATION_ID)) {
					long lastStoreLocationID = (Long) me.getValue();
					dbxRecord.set(key, lastStoreLocationID);

				} else if (key.equals(COL_STORE_DROPBOX_ID)) {
					// do nothing

				} else {
					MyLog.e("StoresTable: setDbxRecordValues ", "Unknown column name:" + key);
				}
			}
		}
	}

	/*	public static void replaceSqlRecordsWithDbxRecords(Context context, DbxDatastore dbxDatastore) {
	MAY NEED TO CHECK IF SQL RECORD ALREADY EXISTS
			if (dbxDatastore != null) {
				DbxTable dbxActiveTable = dbxDatastore.getTable(TABLE_STORES);
				if (dbxActiveTable != null) {
					try {
						DbxTable.QueryResult allRecords = dbxActiveTable.query();
						Iterator<DbxRecord> itr = allRecords.iterator();
						while (itr.hasNext()) {
							DbxRecord dbxRecord = itr.next();
							CreateStore(context, dbxRecord);
						}

					} catch (DbxException e) {
						MyLog.e("StoresTable: replaceSqlRecordsWithDbxRecords ",
								"DbxException while replacing all sql records.");
						e.printStackTrace();
					}
				}

			} else {
				MyLog.e("StoresTable: replaceSqlRecordsWithDbxRecords ",
						"Unable to replace sql records. dbxDatastore is null!");
			}
		}*/

	public static void validateSqlRecords(Context context, DbxTable dbxTable) {
		if (dbxTable != null) {

			// Iterate thru the SQL table records and verify if the SQL record exists in the Dbx table.
			// If not ... delete the SQL table record
			Cursor allDbxStoresCursor = getAllDbxStoresCursor(context);
			String dbxRecordID = "";
			long sqlRecordID = -1;
			DbxRecord dbxRecord = null;
			if (allDbxStoresCursor != null && allDbxStoresCursor.getCount() > 0) {
				while (allDbxStoresCursor.moveToNext()) {

					try {
						dbxRecordID = allDbxStoresCursor.getString(allDbxStoresCursor
								.getColumnIndexOrThrow(COL_STORE_DROPBOX_ID));
						dbxRecord = dbxTable.get(dbxRecordID);
						if (dbxRecord == null) {
							// the SQL table record does not exist in the Dbx table ... so delete it.
							sqlRecordID = allDbxStoresCursor.getLong(allDbxStoresCursor
									.getColumnIndexOrThrow(COL_STORE_ID));
							DeleteStore(context, sqlRecordID);
						}
					} catch (DbxException e) {
						MyLog.e("StoresTable: validateSqlRecords ", "DbxException while iterating thru SQL table.");
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
					Cursor StoreCursor = getStoreFromDropboxID(context, dbxRecordID);
					if (StoreCursor != null && StoreCursor.getCount() > 0) {
						// update the existing record
						UpdateStore(context, dbxRecordID, dbxRecord);
					} else {
						// create a new record
						CreateStore(context, dbxRecord);
					}
					if (StoreCursor != null) {
						StoreCursor.close();
					}
				}
			} catch (DbxException e) {
				MyLog.e("StoresTable: validateSqlRecords ", "DbxException while iterating thru DbxTable.");
				e.printStackTrace();
			}

			if (allDbxStoresCursor != null) {
				allDbxStoresCursor.close();
			}
		}

	}

	private static Cursor getAllDbxStoresCursor(Context context) {
		Cursor cursor = null;

		Uri uri = CONTENT_URI;
		String[] projection = new String[] { COL_STORE_ID, COL_STORE_DROPBOX_ID };

		String selection = COL_STORE_DROPBOX_ID + " != '' OR " + COL_STORE_DROPBOX_ID + " NOT NULL";
		String selectionArgs[] = null;

		ContentResolver cr = context.getContentResolver();
		try {
			cursor = cr.query(uri, projection, selection, selectionArgs, SORT_ORDER_STORE_NAME);
		} catch (Exception e) {
			MyLog.e("Exception error  in getAllDbxStoresCursor. ", "");
			e.printStackTrace();
		}
		return cursor;
	}

}
