package com.lbconsulting.alist.database;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

public class LocationsTable {

	// LOCATIONs data table
	// Version 4 changes
	public static final String TABLE_LOCATIONS = "tblLocations";
	public static final String COL_LOCATION_ID = "_id";
	public static final String COL_LOCATION_DROPBOX_ID = "locationDropboxID";
	public static final String COL_LOCATION_NAME = "locationName";
	public static final String COL_LOCATION_NUMBER = "locationNumber";

	public static String DEFAULT_LOCATION = "[No LOCATION]";

	public static final String[] PROJECTION_ALL = { COL_LOCATION_ID, COL_LOCATION_DROPBOX_ID, COL_LOCATION_NAME,
			COL_LOCATION_NUMBER };

	public static final String CONTENT_PATH = TABLE_LOCATIONS;
	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + "vnd.lbconsulting."
			+ TABLE_LOCATIONS;
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + "vnd.lbconsulting."
			+ TABLE_LOCATIONS;
	public static final Uri CONTENT_URI = Uri.parse("content://" + AListContentProvider.AUTHORITY + "/" + CONTENT_PATH);

	public static final String SORT_ORDER_LOCATION = COL_LOCATION_NUMBER + " ASC, " + COL_LOCATION_NAME + " ASC";

	// Database creation SQL statements
	private static final String DATATABLE_CREATE = "create table "
			+ TABLE_LOCATIONS
			+ " ("
			+ COL_LOCATION_ID + " integer primary key autoincrement, "
			+ COL_LOCATION_DROPBOX_ID + " text, "
			+ COL_LOCATION_NAME + " text collate nocase, "
			+ COL_LOCATION_NUMBER + " integer"
			+ ");";

	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(DATATABLE_CREATE);
		MyLog.i("LocationsTable", "onCreate: " + TABLE_LOCATIONS + " created.");
		ArrayList<String> sqlStatements = new ArrayList<String>();
		String insertProjection = "insert into "
				+ TABLE_LOCATIONS
				+ " ("
				+ COL_LOCATION_ID + ", "
				+ COL_LOCATION_NAME + ") VALUES ";

		// Default LOCATION
		sqlStatements.add(insertProjection + "(NULL, '" + DEFAULT_LOCATION + "')");
		AListUtilities.execMultipleSQL(database, sqlStatements);
	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {

		int upgradeToVersion = oldVersion + 1;
		switch (upgradeToVersion) {
		// fall through each case to upgrade to the newVersion
			case 2:
			case 3:
			case 4:
				// create new locations table
				database.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATIONS);
				onCreate(database);
				MyLog.i(TABLE_LOCATIONS, "New " + TABLE_LOCATIONS + " created.");
				break;

			default:
				// upgrade version not found!
				MyLog.e(TABLE_LOCATIONS, "Upgrade version " + newVersion + " not found!");
				database.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATIONS);
				onCreate(database);
				break;
		}
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Create Methods
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	public static long CreateNewLocation(Context context, String locationName) {
		long newlocationID = -1;

		// verify that the location does not already exist in the table

		Cursor cursor = getLocation(context, locationName);
		if (cursor != null && cursor.getCount() > 0) {
			// the item exists in the table ... so return its id
			cursor.moveToFirst();
			newlocationID = cursor.getLong(cursor.getColumnIndexOrThrow(COL_LOCATION_ID));
			cursor.close();
		} else {
			// Location does not exist in the table ... so add it
			if (locationName != null) {
				locationName = locationName.trim();
				if (!locationName.isEmpty()) {
					try {
						ContentValues values = new ContentValues();
						values.put(COL_LOCATION_NAME, locationName);

						// if locationName starts with "Aisle"
						// parse out any numbers that exist
						if (locationName.startsWith("Aisle")) {
							String locationNumber_string = "";
							Pattern p = Pattern.compile("(\\d+)");
							Matcher m = p.matcher(locationName);
							if (m.find()) {
								MatchResult mr = m.toMatchResult();
								locationNumber_string = mr.group(1);
							}
							if (!locationNumber_string.isEmpty()) {
								// numbers exist in itemName ...
								// so insert them into their field so that they will be sorted correctly
								int locationNumber = Integer.parseInt(locationNumber_string);
								values.put(COL_LOCATION_NUMBER, locationNumber);
							}
						}
						Uri uri = CONTENT_URI;
						ContentResolver cr = context.getContentResolver();
						Uri newListUri = cr.insert(uri, values);
						if (newListUri != null) {
							newlocationID = Long.parseLong(newListUri.getLastPathSegment());
						}
					} catch (Exception e) {
						MyLog.e("Exception error in CreateNewLocation. ", e.toString());
					}
				} else {
					MyLog.e("LocationsTable", "Error in CreateNewLocation; locationName is Empty!");
				}
			} else {
				MyLog.e("LocationsTable", "Error in CreateNewLocation; locationName is Null!");
			}
		}

		if (cursor != null) {
			cursor.close();
		}
		return newlocationID;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Read Methods
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	public static Cursor getLocation(Context context, long locationID) {
		Cursor cursor = null;
		if (locationID > 0) {
			Uri uri = Uri.withAppendedPath(CONTENT_URI, String.valueOf(locationID));
			String[] projection = PROJECTION_ALL;
			String selection = null;
			String selectionArgs[] = null;
			String sortOrder = null;
			ContentResolver cr = context.getContentResolver();
			try {
				cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
			} catch (Exception e) {
				MyLog.e("Exception error in LocationsTable: getLocation. ", e.toString());
			}
		}
		return cursor;
	}

	public static Cursor getLocation(Context context, String locationName) {
		Cursor cursor = null;

		Uri uri = CONTENT_URI;
		String[] projection = PROJECTION_ALL;
		String selection = COL_LOCATION_NAME + " = ?";
		String selectionArgs[] = new String[] { locationName };
		String sortOrder = null;
		ContentResolver cr = context.getContentResolver();
		try {
			cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
		} catch (Exception e) {
			MyLog.e("Exception error in LocationsTable: getLocation. ", e.toString());
		}
		return cursor;
	}

	public static String getLocationName(Context context, long locationID) {
		String locationName = "";
		Cursor cursor = getLocation(context, locationID);
		if (cursor != null) {
			cursor.moveToFirst();
			locationName = cursor.getString(cursor.getColumnIndexOrThrow(COL_LOCATION_NAME));
			cursor.close();
		}
		return locationName;
	}

	public static CursorLoader getAllLocationssInListIncludeDefault(Context context, String sortOrder) {
		CursorLoader cursorLoader = null;

		Uri uri = CONTENT_URI;
		String[] projection = PROJECTION_ALL;
		String selection = null;
		String selectionArgs[] = null;
		try {
			cursorLoader = new CursorLoader(context, uri, projection, selection, selectionArgs, sortOrder);
		} catch (Exception e) {
			MyLog.e("Exception error in GroupsTable: getAllGroupsInList. ", e.toString());
		}
		return cursorLoader;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Update Methods
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static int UpdateLocationName(Context context, long locationID, String locationName) {
		int numberOfUpdatedRecords = -1;
		// cannot update the default Location with ID=1
		if (locationID > 1) {
			Uri uri = CONTENT_URI;
			String selection = COL_LOCATION_ID + " = ?";
			String selectionArgs[] = new String[] { String.valueOf(locationID) };
			ContentResolver cr = context.getContentResolver();

			ContentValues values = new ContentValues();
			values.put(COL_LOCATION_NAME, locationName.trim());
			numberOfUpdatedRecords = cr.update(uri, values, selection, selectionArgs);
			if (numberOfUpdatedRecords != 1) {
				MyLog.e("LocationsTable: UpdateLocationName", "The number of records updated does not equal 1!");
			}
		}
		return numberOfUpdatedRecords;
	}

	public static int UpdateLocationFieldValues(Context context, long locationID, ContentValues newFieldValues) {
		int numberOfUpdatedRecords = -1;
		if (locationID > 1) {
			ContentResolver cr = context.getContentResolver();
			Uri defaultUri = Uri.withAppendedPath(CONTENT_URI, String.valueOf(locationID));
			String selection = null;
			String[] selectionArgs = null;
			numberOfUpdatedRecords = cr.update(defaultUri, newFieldValues, selection, selectionArgs);
		}
		return numberOfUpdatedRecords;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Delete Methods
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static int DeleteLocation(Context context, long locationID) {
		int numberOfDeletedRecords = -1;
		if (locationID > 1) {
			ContentResolver cr = context.getContentResolver();
			Uri uri = CONTENT_URI;
			String where = COL_LOCATION_ID + " = ?";
			String[] selectionArgs = { String.valueOf(locationID) };
			numberOfDeletedRecords = cr.delete(uri, where, selectionArgs);
		}
		// reset the locationID to the default value
		BridgeTable.ResetLocationID(context, locationID);

		return numberOfDeletedRecords;
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

	public static long CreateLocation(Context context, DbxRecord dbxRecord) {
		// a check to see if the Location is already in the database
		// was done prior to making this call ... so don't repeat it.
		long newLocationID = -1;

		ContentValues newFieldValues = setContentValues(dbxRecord);
		Uri uri = CONTENT_URI;
		ContentResolver cr = context.getContentResolver();
		Uri newLocationUri = cr.insert(uri, newFieldValues);
		if (newLocationUri != null) {
			newLocationID = Long.parseLong(newLocationUri.getLastPathSegment());
		}
		return newLocationID;
	}

	public static Cursor getLocationFromDropboxID(Context context, String dbxRecordID) {
		Uri uri = CONTENT_URI;
		String[] projection = PROJECTION_ALL;
		String selection = COL_LOCATION_DROPBOX_ID + " = '" + dbxRecordID + "'";
		String selectionArgs[] = null;
		String sortOrder = SORT_ORDER_LOCATION;

		ContentResolver cr = context.getContentResolver();
		Cursor cursor = null;
		try {
			cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
		} catch (Exception e) {
			MyLog.e("LocationsTable", "Exception error in getLocationFromDropboxID:");
			e.printStackTrace();
		}
		return cursor;
	}

	public static Uri getLocationUri(Context context, String dbxRecordID) {
		Uri LocationUri = null;
		Cursor cursor = getLocationFromDropboxID(context, dbxRecordID);
		if (cursor != null) {
			cursor.moveToFirst();
			long LocationID = cursor.getLong(cursor.getColumnIndexOrThrow(COL_LOCATION_ID));
			LocationUri = ContentUris.withAppendedId(LocationsTable.CONTENT_URI, LocationID);
			cursor.close();
		}
		return LocationUri;
	}

	public static String getDropboxID(Context context, long LocationID) {
		String dbxID = "";
		Cursor cursor = getLocation(context, LocationID);
		if (cursor != null) {
			if (cursor.getCount() > 0) {
				cursor.moveToFirst();
				dbxID = cursor.getString(cursor.getColumnIndexOrThrow(COL_LOCATION_DROPBOX_ID));
			}
			cursor.close();
		}

		return dbxID;
	}

	public static int UpdateLocation(Context context, String dbxRecordID, DbxRecord dbxRecord) {
		int numberOfUpdatedRecords = -1;
		ContentResolver cr = context.getContentResolver();
		Uri LocationUri = getLocationUri(context, dbxRecordID);
		ContentValues newFieldValues = setContentValues(dbxRecord);
		String selection = null;
		String[] selectionArgs = null;
		numberOfUpdatedRecords = cr.update(LocationUri, newFieldValues, selection, selectionArgs);

		return numberOfUpdatedRecords;
	}

	public static int DeleteLocation(Context context, String dbxRecordID) {
		int numberOfDeletedRecords = -1;

		Uri LocationUri = getLocationUri(context, dbxRecordID);
		ContentResolver cr = context.getContentResolver();
		String where = null;
		String[] selectionArgs = null;
		numberOfDeletedRecords = cr.delete(LocationUri, where, selectionArgs);

		return numberOfDeletedRecords;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Dropbox DataLocation Methods
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static void dbxInsert(Context context, DbxDatastore DbxDatastore, long LocationID) throws DbxException {
		ContentValues values = setContentValues(context, LocationID);
		DbxRecord dbxRecord = dbxInsert(context, DbxDatastore, LocationID, values);
		setDbxRecordValues(dbxRecord, values);
		DbxDatastore.sync();
	}

	/*	public static void dbxInsertAllLocations(Context context, DbxDatastore DbxDatastore, long listID) throws DbxException {
			Cursor LocationsCursor = getAllLocationsInListCursor(context, listID, null);
			if (LocationsCursor != null) {
				while (LocationsCursor.moveToNext()) {
					ContentValues values = setContentValues(context, LocationsCursor);
					long LocationID = LocationsCursor.getLong(LocationsCursor.getColumnIndexOrThrow(COL_LOCATION_ID));
					DbxRecord dbxRecord = dbxInsert(context, DbxDatastore, LocationID, values);
					setDbxRecordValues(dbxRecord, values);
				}
				DbxDatastore.sync();
				LocationsCursor.close();
			}
		}*/

	public static DbxRecord dbxInsert(Context context, DbxDatastore DbxDatastore, long locationID, ContentValues values) {

		DbxRecord newLocationRecord = null;
		if (DbxDatastore != null) {
			DbxTable dbxActiveTable = DbxDatastore.getTable(TABLE_LOCATIONS);

			if (dbxActiveTable != null) {

				Set<Entry<String, Object>> s = values.valueSet();
				Iterator<Entry<String, Object>> itr = s.iterator();
				while (itr.hasNext()) {
					Entry<String, Object> me = itr.next();
					String key = me.getKey().toString();

					if (key.equals(COL_LOCATION_NAME)) {
						String LocationName = (String) me.getValue();
						newLocationRecord = dbxActiveTable.insert()
								.set(key, LocationName)
								.set(COL_LOCATION_NUMBER, 1);

						// update the SQLite record with the dbxID
						AListContentProvider.setSuppressDropboxChanges(true);
						String dbxID = newLocationRecord.getId();
						ContentValues newFieldValues = new ContentValues();
						newFieldValues.put(COL_LOCATION_DROPBOX_ID, dbxID);
						UpdateLocationFieldValues(context, locationID, newFieldValues);

						MyLog.d("LocationsTable: dbxInsert ", key + ":" + LocationName);
						AListContentProvider.setSuppressDropboxChanges(false);

					} else if (key.equals(COL_LOCATION_NUMBER)) {
						int locationNumber = (Integer) me.getValue();
						if (newLocationRecord != null) {
							newLocationRecord.set(key, locationNumber);
							MyLog.d("LocationsTable: dbxInsert ", key + ":" + locationNumber);
						}
					}
				}
			}
		} else {
			MyLog.e("LocationsTable: dbxInsert ", "Unable to insert record. DbxDatastore is null!");
		}
		return newLocationRecord;

	}

	public static void dbxDeleteSingleRecord(Context context, DbxDatastore DbxDatastore, String locationIDstring) {
		if (DbxDatastore != null) {
			DbxTable dbxActiveTable = DbxDatastore.getTable(TABLE_LOCATIONS);

			String dbxRecordID = getDropboxID(context, Long.parseLong(locationIDstring));
			if (dbxRecordID != null && !dbxRecordID.isEmpty()) {
				try {
					DbxRecord dbxRecord = dbxActiveTable.get(dbxRecordID);
					if (dbxRecord != null) {
						dbxRecord.deleteRecord();
						DbxDatastore.sync();
					}
				} catch (DbxException e) {
					MyLog.e("LocationsTable: dbxDeleteSingleRecord ",
							"DbxException while trying delete a dropbox record.");
				}
			}
		} else {
			MyLog.e("LocationsTable: dbxDeleteSingleRecord ", "Unable to delete record. DbxDatastore is null!");
		}
	}

	public static void dbxDeleteMultipleRecords(Context context, DbxDatastore DbxDatastore, Uri uri, String selection,
			String[] selectionArgs) {

		if (DbxDatastore != null) {
			DbxTable dbxActiveTable = DbxDatastore.getTable(TABLE_LOCATIONS);

			if (dbxActiveTable != null) {
				String projection[] = { COL_LOCATION_ID, COL_LOCATION_DROPBOX_ID };
				String sortOrder = null;
				String dbxID;
				DbxRecord dbxRecord;
				ContentResolver cr = context.getContentResolver();
				Cursor cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
				if (cursor != null) {
					try {
						while (cursor.moveToNext()) {
							dbxID = cursor.getString(cursor.getColumnIndexOrThrow(COL_LOCATION_DROPBOX_ID));
							dbxRecord = dbxActiveTable.get(dbxID);
							if (dbxRecord != null) {
								dbxRecord.deleteRecord();
							}
						}

						DbxDatastore.sync();
					} catch (DbxException e) {
						MyLog.e("LocationsTable: dbxDeleteMultipleRecords ",
								"DbxException while trying to delete multiple dropbox records.");
						e.printStackTrace();

					} finally {
						cursor.close();
					}
				}
			}
		} else {
			MyLog.e("LocationsTable: dbxDeleteMultipleRecords ", "Unable to delete records. DbxDatastore is null!");
		}
	}

	public static void dbxDeleteAllRecords(DbxDatastore DbxDatastore) {
		if (DbxDatastore != null) {
			DbxTable dbxActiveTable = DbxDatastore.getTable(TABLE_LOCATIONS);
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
					MyLog.e("LocationsTable: dbxDeleteAllRecords ", "DbxException while deleteing all dropbox records.");
					e.printStackTrace();
				}
			}
		} else {
			MyLog.e("LocationsTable: dbxDeleteAllRecords ", "Unable to delete records. DbxDatastore is null!");
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
			DbxTable dbxActiveTable = DbxDatastore.getTable(TABLE_LOCATIONS);

			if (dbxActiveTable != null) {
				String projection[] = { COL_LOCATION_ID, COL_LOCATION_DROPBOX_ID };
				String sortOrder = null;
				String dbxID;
				DbxRecord dbxRecord;
				ContentResolver cr = context.getContentResolver();
				Cursor cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
				if (cursor != null) {
					try {
						while (cursor.moveToNext()) {
							dbxID = cursor.getString(cursor.getColumnIndexOrThrow(COL_LOCATION_DROPBOX_ID));
							dbxRecord = dbxActiveTable.get(dbxID);
							if (dbxRecord != null) {
								setDbxRecordValues(dbxRecord, values);
							}
						}

						DbxDatastore.sync();
					} catch (DbxException e) {
						MyLog.e("LocationsTable: dbxUpdateMultipleRecords ",
								"DbxException while trying update records.");
						e.printStackTrace();

					} finally {
						cursor.close();
					}
				}
			}
		} else {
			MyLog.e("LocationsTable: dbxUpdateMultipleRecords ", "Unable to update records. DbxDatastore is null!");
		}
	}

	public static void dbxUpdateSingleRecord(Context context, DbxDatastore DbxDatastore, ContentValues values, Uri uri) {
		if (DbxDatastore != null) {
			DbxTable dbxActiveTable = DbxDatastore.getTable(TABLE_LOCATIONS);

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
							// the dbxLocation has been deleted ...
							// but for some reason it has not been deleted from the sql database
							// so delete it now.
							sqlDeleteLocationAlreadyDeletedFromDropbox(context, dbxRecordID);
							// sync to hopefully capture other dropbox changes
							DbxDatastore.sync();
						}

					} catch (DbxException e) {
						MyLog.e("LocationsTable: dbxUpdateSingleRecord ", "DbxException while trying update records.");
						e.printStackTrace();
					}
				}
			}
		} else {
			MyLog.e("LocationsTable: dbxUpdateSingleRecord ", "Unable to update record. DbxDatastore is null!");
		}
	}

	private static void sqlDeleteLocationAlreadyDeletedFromDropbox(Context context, String dbxRecordID) {
		AListContentProvider.setSuppressDropboxChanges(true);
		DeleteLocation(context, dbxRecordID);
		AListContentProvider.setSuppressDropboxChanges(false);
	}

	private static ContentValues setContentValues(Context context, Cursor cursor) {
		ContentValues newFieldValues = new ContentValues();
		if (cursor != null) {
			for (String col : PROJECTION_ALL) {
				if (col.equals(COL_LOCATION_ID) || col.equals(COL_LOCATION_DROPBOX_ID)) {
					// do nothing
				} else if (col.equals(COL_LOCATION_NUMBER)) {
					int value = cursor.getInt(cursor.getColumnIndexOrThrow(col));
					newFieldValues.put(col, value);

				} else {
					String value = cursor.getString(cursor.getColumnIndexOrThrow(col));
					newFieldValues.put(col, value);
				}
			}
		}
		return newFieldValues;
	}

	/*						+ TABLE_LOCATIONS
		+ " ("
		+ COL_LOCATION_ID + " integer primary key autoincrement, "
		+ COL_LOCATION_DROPBOX_ID + " text, "
		+ COL_LOCATION_NAME + " text collate nocase, "
		+ COL_LOCATION_NUMBER + " integer"*/

	private static ContentValues setContentValues(Context context, long LocationID) {
		ContentValues newFieldValues = null;
		Cursor cursor = getLocation(context, LocationID);
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
			newFieldValues.put(COL_LOCATION_DROPBOX_ID, dbxID);

			if (dbxRecord.hasField(COL_LOCATION_NAME)) {
				String locationName = dbxRecord.getString(COL_LOCATION_NAME);
				newFieldValues.put(COL_LOCATION_NAME, locationName);
			}

			if (dbxRecord.hasField(COL_LOCATION_NUMBER)) {
				int locationNumber = (int) dbxRecord.getLong(COL_LOCATION_NUMBER);
				newFieldValues.put(COL_LOCATION_NUMBER, locationNumber);
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

				if (key.equals(COL_LOCATION_NAME)) {
					String string = (String) me.getValue();
					if (string == null) {
						string = "";
					}
					dbxRecord.set(key, string);

				} else if (key.equals(COL_LOCATION_NUMBER)) {
					int locationNumber = (Integer) me.getValue();
					dbxRecord.set(key, locationNumber);

				} else if (key.equals(COL_LOCATION_DROPBOX_ID)) {
					// do nothing

				} else {
					MyLog.e("LocationsTable: setDbxRecordValues ", "Unknown column name:" + key);
				}
			}
		}
	}

	/*	public static void replaceSqlRecordsWithDbxRecords(Context context, DbxDatastore DbxDatastore) {
	MAY NEED TO CHECK IF SQL RECORD ALREADY EXISTS
			if (DbxDatastore != null) {
				DbxTable dbxActiveTable = DbxDatastore.getTable(TABLE_LOCATIONS);
				if (dbxActiveTable != null) {
					try {
						DbxTable.QueryResult allRecords = dbxActiveTable.query();
						Iterator<DbxRecord> itr = allRecords.iterator();
						while (itr.hasNext()) {
							DbxRecord dbxRecord = itr.next();
							CreateLocation(context, dbxRecord);
						}

					} catch (DbxException e) {
						MyLog.e("LocationsTable: replaceSqlRecordsWithDbxRecords ",
								"DbxException while replacing all sql records.");
						e.printStackTrace();
					}
				}

			} else {
				MyLog.e("LocationsTable: replaceSqlRecordsWithDbxRecords ",
						"Unable to replace sql records. DbxDatastore is null!");
			}
		}*/

	public static void validateSqlRecords(Context context, DbxTable dbxTable) {
		if (dbxTable != null) {

			// Iterate thru the SQL table records and verify if the SQL record exists in the Dbx table.
			// If not ... delete the SQL table record
			Cursor allDbxLocationsCursor = getAllDbxLocationsCursor(context);
			String dbxRecordID = "";
			long sqlRecordID = -1;
			DbxRecord dbxRecord = null;
			if (allDbxLocationsCursor != null && allDbxLocationsCursor.getCount() > 0) {
				while (allDbxLocationsCursor.moveToNext()) {

					try {
						dbxRecordID = allDbxLocationsCursor.getString(allDbxLocationsCursor
								.getColumnIndexOrThrow(COL_LOCATION_DROPBOX_ID));
						dbxRecord = dbxTable.get(dbxRecordID);
						if (dbxRecord == null) {
							// the SQL table record does not exist in the Dbx table ... so delete it.
							sqlRecordID = allDbxLocationsCursor.getLong(allDbxLocationsCursor
									.getColumnIndexOrThrow(COL_LOCATION_ID));
							DeleteLocation(context, sqlRecordID);
						}
					} catch (DbxException e) {
						MyLog.e("LocationsTable: validateSqlRecords ", "DbxException while iterating thru SQL table.");
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
					Cursor LocationCursor = getLocationFromDropboxID(context, dbxRecordID);
					if (LocationCursor != null && LocationCursor.getCount() > 0) {
						// update the existing record
						UpdateLocation(context, dbxRecordID, dbxRecord);
					} else {
						// create a new record
						CreateLocation(context, dbxRecord);
					}
					if (LocationCursor != null) {
						LocationCursor.close();
					}
				}
			} catch (DbxException e) {
				MyLog.e("LocationsTable: validateSqlRecords ", "DbxException while iterating thru DbxTable.");
				e.printStackTrace();
			}

			if (allDbxLocationsCursor != null) {
				allDbxLocationsCursor.close();
			}
		}

	}

	private static Cursor getAllDbxLocationsCursor(Context context) {
		Cursor cursor = null;

		Uri uri = CONTENT_URI;
		String[] projection = new String[] { COL_LOCATION_ID, COL_LOCATION_DROPBOX_ID };

		String selection = COL_LOCATION_DROPBOX_ID + " != '' OR " + COL_LOCATION_DROPBOX_ID + " NOT NULL";
		String selectionArgs[] = null;

		ContentResolver cr = context.getContentResolver();
		try {
			cursor = cr.query(uri, projection, selection, selectionArgs, SORT_ORDER_LOCATION);
		} catch (Exception e) {
			MyLog.e("Exception error  in getAllDbxLocationsCursor. ", "");
			e.printStackTrace();
		}
		return cursor;
	}

}
