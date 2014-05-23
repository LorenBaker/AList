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

public class LocationsTable {

	// LOCATIONs data table
	// Version 4 changes
	public static final String TABLE_LOCATIONS = "tblLocations";
	public static final String COL_LOCATION_ID = "_id";
	public static final String COL_LOCATION_NAME = "locationName";

	public static String DEFAULT_LOCATION = "[No LOCATION]";

	public static final String[] PROJECTION_ALL = { COL_LOCATION_ID, COL_LOCATION_NAME };

	public static final String CONTENT_PATH = TABLE_LOCATIONS;
	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + "vnd.lbconsulting."
			+ TABLE_LOCATIONS;
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + "vnd.lbconsulting."
			+ TABLE_LOCATIONS;
	public static final Uri CONTENT_URI = Uri.parse("content://" + AListContentProvider.AUTHORITY + "/" + CONTENT_PATH);

	public static final String SORT_ORDER_LOCATION = COL_LOCATION_NAME + " ASC";

	// Database creation SQL statements
	private static final String DATATABLE_CREATE = "create table "
			+ TABLE_LOCATIONS
			+ " ("
			+ COL_LOCATION_ID + " integer primary key autoincrement, "
			+ COL_LOCATION_NAME + " text collate nocase "
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
	@SuppressWarnings("resource")
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
						ContentResolver cr = context.getContentResolver();
						Uri uri = CONTENT_URI;
						ContentValues values = new ContentValues();
						values.put(COL_LOCATION_NAME, locationName);
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

	/*	public static int DeleteAllLocation(Context context) {
			int numberOfDeletedRecords = -1;
			Uri uri = CONTENT_URI;
			String where = COL_LOCATION_ID + " > 1";
			String selectionArgs[] = null;
			ContentResolver cr = context.getContentResolver();
			numberOfDeletedRecords = cr.delete(uri, where, selectionArgs);

			return numberOfDeletedRecords;
		}*/

}
