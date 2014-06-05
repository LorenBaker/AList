package com.lbconsulting.alist.database;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.dropbox.sync.android.DbxDatastore;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxRecord;
import com.lbconsulting.alist.utilities.AListUtilities;
import com.lbconsulting.alist.utilities.MyLog;

public class AListContentProvider extends ContentProvider {

	// AList database
	private AListDatabaseHelper database = null;

	// Dropbox database
	// private static DbxAccountManager mAccountManager = null;
	// private static DbxAccount mAccount = null;

	private boolean mListsSyncedToDropbox;

	private static DbxDatastore mDbxDatastore = null;
	private static Context mContext = null;
	private static boolean mSuppressChangeNotification = false;
	private static boolean mSuppressDropboxChanges = false;
	private static boolean mLastSuppressDropboxChangeRequest = false;

	public static void setDbxDatastore(DbxDatastore dbxDatastore) {
		mDbxDatastore = dbxDatastore;
		// update mSuppressDropboxChanges with the updated mDbxDatastore
		setSuppressDropboxChanges(mLastSuppressDropboxChangeRequest);
	}

	public static DbxDatastore getDbxDatastore() {
		return mDbxDatastore;
	}

	public static void setContext(Context context) {
		mContext = context;
	}

	public static void setSuppressChangeNotification(boolean supressChanges) {
		mSuppressChangeNotification = supressChanges;
	}

	public static void setSuppressDropboxChanges(boolean suppressDropboxChanges) {
		// TODO: remove line: suppressDropboxChanges = false
		// suppressDropboxChanges = false;

		mLastSuppressDropboxChangeRequest = suppressDropboxChanges;
		// if free version always suppress dropbox changes
		mSuppressDropboxChanges = suppressDropboxChanges || AListUtilities.isFreeVersion() || mDbxDatastore == null;
		// mSuppressDropboxChanges = suppressDropboxChanges || AListUtilities.isFreeVersion();
	}

	// UriMatcher switch constants
	private static final int ITEMS_MULTI_ROWS = 10;
	private static final int ITEMS_SINGLE_ROW = 11;

	private static final int LIST_MULTI_ROWS = 20;
	private static final int LIST_SINGLE_ROW = 21;

	private static final int GROUPS_MULTI_ROWS = 30;
	private static final int GROUPS_SINGLE_ROW = 31;

	private static final int STORES_MULTI_ROWS = 40;
	private static final int STORES_SINGLE_ROW = 41;

	private static final int LOCATIONS_MULTI_ROWS = 50;
	private static final int LOCATIONS_SINGLE_ROW = 51;

	private static final int BRIDGE_MULTI_ROWS = 60;
	private static final int BRIDGE_SINGLE_ROW = 61;

	private static final int ITEMS_WITH_GROUPS = 70;
	private static final int ITEMS_WITH_LOCATIONS = 71;
	private static final int GROUPS_WITH_LOCATIONS = 72;

	public static final String AUTHORITY = "com.lbconsulting.alist.contentprovider";

	private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		sURIMatcher.addURI(AUTHORITY, ItemsTable.CONTENT_PATH, ITEMS_MULTI_ROWS);
		sURIMatcher.addURI(AUTHORITY, ItemsTable.CONTENT_PATH + "/#", ITEMS_SINGLE_ROW);

		sURIMatcher.addURI(AUTHORITY, ListsTable.CONTENT_PATH, LIST_MULTI_ROWS);
		sURIMatcher.addURI(AUTHORITY, ListsTable.CONTENT_PATH + "/#", LIST_SINGLE_ROW);

		sURIMatcher.addURI(AUTHORITY, GroupsTable.CONTENT_PATH, GROUPS_MULTI_ROWS);
		sURIMatcher.addURI(AUTHORITY, GroupsTable.CONTENT_PATH + "/#", GROUPS_SINGLE_ROW);

		sURIMatcher.addURI(AUTHORITY, StoresTable.CONTENT_PATH, STORES_MULTI_ROWS);
		sURIMatcher.addURI(AUTHORITY, StoresTable.CONTENT_PATH + "/#", STORES_SINGLE_ROW);

		sURIMatcher.addURI(AUTHORITY, LocationsTable.CONTENT_PATH, LOCATIONS_MULTI_ROWS);
		sURIMatcher.addURI(AUTHORITY, LocationsTable.CONTENT_PATH + "/#", LOCATIONS_SINGLE_ROW);

		sURIMatcher.addURI(AUTHORITY, BridgeTable.CONTENT_PATH, BRIDGE_MULTI_ROWS);
		sURIMatcher.addURI(AUTHORITY, BridgeTable.CONTENT_PATH + "/#", BRIDGE_SINGLE_ROW);

		sURIMatcher.addURI(AUTHORITY, ItemsTable.CONTENT_PATH_ITEMS_WITH_GROUPS, ITEMS_WITH_GROUPS);
		sURIMatcher.addURI(AUTHORITY, ItemsTable.CONTENT_PATH_ITEMS_WITH_LOCATIONS, ITEMS_WITH_LOCATIONS);
		sURIMatcher.addURI(AUTHORITY, GroupsTable.CONTENT_PATH_GROUPS_WITH_LOCATIONS, GROUPS_WITH_LOCATIONS);

	}

	@Override
	public boolean onCreate() {
		MyLog.i("AListContentProvider", "onCreate()");
		// Construct the underlying database
		// Defer opening the database until you need to perform
		// a query or other transaction.
		database = new AListDatabaseHelper(getContext());

		// open the dbxDatastore if there are any lists synced to dropbox
		SharedPreferences storedStates = getContext().getSharedPreferences("AList", Context.MODE_PRIVATE);
		mListsSyncedToDropbox = storedStates.getBoolean("ListsSyncedToDropbox", false);

		/*		if (mListsSyncedToDropbox) {
					mAccountManager = DbxAccountManager.getInstance(getContext(), AListUtilities.APP_KEY,
							AListUtilities.APP_SECRET);
					if (mAccountManager.hasLinkedAccount()) {
						mAccount = mAccountManager.getLinkedAccount();
						try {
							setDbxDatastore(DbxDatastore.openDefault(mAccount));

						} catch (DbxException e) {
							MyLog.e("AListContentProvider",
									"setupDropbox(): DbxException while trying to openDefault datastore.");
							e.printStackTrace();
						}
					}
				}*/
		return true;
	}

	public static void onDatastoreStatusChange(DbxDatastore store) {
		mDbxDatastore = store;
		if (store.getSyncStatus().hasIncoming) {
			// Handle the updated data
			try {
				Map<String, Set<DbxRecord>> changes = mDbxDatastore.sync();

				setSuppressDropboxChanges(true);
				for (Map.Entry<String, Set<DbxRecord>> table : changes.entrySet()) {
					String tableName = table.getKey();

					if (tableName.equals(ItemsTable.TABLE_ITEMS)) {
						Set<?> recordSet = table.getValue();
						Iterator<?> itr = recordSet.iterator();
						while (itr.hasNext()) {
							DbxRecord dbxRecord = (DbxRecord) itr.next();
							String dbxRecordID = dbxRecord.getId();

							if (dbxRecordID != null && !dbxRecordID.isEmpty()) {
								if (dbxRecord.isDeleted()) {
									ItemsTable.DeleteItem(mContext, dbxRecordID);
								} else {
									// record is either a new or revised record
									// try and get the SQLite record
									Cursor itemCursor = ItemsTable.getItemFromDropboxID(mContext, dbxRecordID);
									if (itemCursor != null && itemCursor.getCount() > 0) {
										// update the existing record
										ItemsTable.UpdateItem(mContext, dbxRecordID, dbxRecord);
									} else {
										// create a new record
										ItemsTable.CreateItem(mContext, dbxRecord);
									}
									if (itemCursor != null) {
										itemCursor.close();
									}
								}
							}
						}

					} else if (tableName.equals(ListsTable.TABLE_LISTS)) {
						Set<?> recordSet = table.getValue();
						Iterator<?> itr = recordSet.iterator();
						while (itr.hasNext()) {
							DbxRecord dbxRecord = (DbxRecord) itr.next();
							String dbxRecordID = dbxRecord.getId();

							if (dbxRecordID != null && !dbxRecordID.isEmpty()) {
								if (dbxRecord.isDeleted()) {
									ListsTable.DeleteList(mContext, dbxRecordID);
								} else {
									// record is either a new or revised record
									// try and get the SQLite record
									Cursor itemCursor = ListsTable.getListFromDropboxID(mContext, dbxRecordID);
									if (itemCursor != null && itemCursor.getCount() > 0) {
										// update the existing record
										ListsTable.UpdateList(mContext, dbxRecordID, dbxRecord);
									} else {
										// create a new record
										ListsTable.CreateNewList(mContext, dbxRecord);
									}
									if (itemCursor != null) {
										itemCursor.close();
									}
								}
							}
						}
					} else if (tableName.equals(GroupsTable.TABLE_GROUPS)) {
						Set<?> recordSet = table.getValue();
						Iterator<?> itr = recordSet.iterator();
						while (itr.hasNext()) {
							DbxRecord dbxRecord = (DbxRecord) itr.next();
							String dbxRecordID = dbxRecord.getId();

							if (dbxRecordID != null && !dbxRecordID.isEmpty()) {
								if (dbxRecord.isDeleted()) {
									GroupsTable.DeleteGroup(mContext, dbxRecordID);
								} else {
									// record is either a new or revised record
									// try and get the SQLite record
									Cursor itemCursor = GroupsTable.getGroupFromDropboxID(mContext, dbxRecordID);
									if (itemCursor != null && itemCursor.getCount() > 0) {
										// update the existing record
										GroupsTable.UpdateGroup(mContext, dbxRecordID, dbxRecord);
									} else {
										// create a new record
										GroupsTable.CreateGroup(mContext, dbxRecord);
									}
									if (itemCursor != null) {
										itemCursor.close();
									}
								}
							}
						}
					} else if (tableName.equals(LocationsTable.TABLE_LOCATIONS)) {
						Set<?> recordSet = table.getValue();
						Iterator<?> itr = recordSet.iterator();
						while (itr.hasNext()) {
							DbxRecord dbxRecord = (DbxRecord) itr.next();
							String dbxRecordID = dbxRecord.getId();

							if (dbxRecordID != null && !dbxRecordID.isEmpty()) {
								if (dbxRecord.isDeleted()) {
									LocationsTable.DeleteLocation(mContext, dbxRecordID);
								} else {
									// record is either a new or revised record
									// try and get the SQLite record
									Cursor itemCursor = LocationsTable.getLocationFromDropboxID(mContext, dbxRecordID);
									if (itemCursor != null && itemCursor.getCount() > 0) {
										// update the existing record
										LocationsTable.UpdateLocation(mContext, dbxRecordID, dbxRecord);
									} else {
										// create a new record
										LocationsTable.CreateLocation(mContext, dbxRecord);
									}
									if (itemCursor != null) {
										itemCursor.close();
									}
								}
							}
						}
					} else if (tableName.equals(StoresTable.TABLE_STORES)) {
						Set<?> recordSet = table.getValue();
						Iterator<?> itr = recordSet.iterator();
						while (itr.hasNext()) {
							DbxRecord dbxRecord = (DbxRecord) itr.next();
							String dbxRecordID = dbxRecord.getId();

							if (dbxRecordID != null && !dbxRecordID.isEmpty()) {
								if (dbxRecord.isDeleted()) {
									StoresTable.DeleteStore(mContext, dbxRecordID);
								} else {
									// record is either a new or revised record
									// try and get the SQLite record
									Cursor itemCursor = StoresTable.getStoreFromDropboxID(mContext, dbxRecordID);
									if (itemCursor != null && itemCursor.getCount() > 0) {
										// update the existing record
										StoresTable.UpdateStore(mContext, dbxRecordID, dbxRecord);
									} else {
										// create a new record
										StoresTable.CreateStore(mContext, dbxRecord);
									}
									if (itemCursor != null) {
										itemCursor.close();
									}
								}
							}
						}
					} else if (tableName.equals(BridgeTable.TABLE_BRIDGE)) {
						Set<?> recordSet = table.getValue();
						Iterator<?> itr = recordSet.iterator();
						while (itr.hasNext()) {
							DbxRecord dbxRecord = (DbxRecord) itr.next();
							String dbxRecordID = dbxRecord.getId();

							if (dbxRecordID != null && !dbxRecordID.isEmpty()) {
								if (dbxRecord.isDeleted()) {
									BridgeTable.DeleteBridgeRow(mContext, dbxRecordID);
								} else {
									// record is either a new or revised record
									// try and get the SQLite record
									Cursor itemCursor = BridgeTable.getBridgeRowFromDropboxID(mContext, dbxRecordID);
									if (itemCursor != null && itemCursor.getCount() > 0) {
										// update the existing record
										BridgeTable.UpdateBridgeRow(mContext, dbxRecordID, dbxRecord);
									} else {
										// create a new record
										BridgeTable.CreateBridgeRow(mContext, dbxRecord);
									}
									if (itemCursor != null) {
										itemCursor.close();
									}
								}
							}
						}
					}

				}
			} catch (DbxException e) {
				MyLog.e("AListContentProvider: onDatastoreStatusChange ", "DbxException.");
				e.printStackTrace();
			} finally {
				setSuppressDropboxChanges(false);
			}
		}
	}

	/*A content provider is created when its hosting process is created, 
	 * and remains around for as long as the process does, so there is 
	 * no need to close the database -- it will get closed as part of the 
	 * kernel cleaning up the process's resources when the process is killed.
	 */

	@SuppressWarnings("resource")
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		String rowIDstring = null;
		int deleteCount = 0;

		// Open a WritableDatabase database to support the delete transaction
		SQLiteDatabase db = database.getWritableDatabase();

		int uriType = sURIMatcher.match(uri);
		switch (uriType) {
			case ITEMS_MULTI_ROWS:
				// To return the number of deleted items you must specify a where clause.
				// To delete all rows and return a value pass in "1".
				if (selection == null) {
					selection = "1";
				}

				if (!mSuppressDropboxChanges) {
					ItemsTable.dbxDeleteMultipleRecords(mContext, mDbxDatastore, uri, selection, selectionArgs);
				}

				// Perform the deletion
				deleteCount = db.delete(ItemsTable.TABLE_ITEMS, selection, selectionArgs);
				break;

			case ITEMS_SINGLE_ROW:
				// Limit deletion to a single row
				rowIDstring = uri.getLastPathSegment();
				selection = ItemsTable.COL_ITEM_ID + "=" + rowIDstring
						+ (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : "");

				if (!mSuppressDropboxChanges) {
					ItemsTable.dbxDeleteSingleRecord(mContext, mDbxDatastore, rowIDstring);
				}

				// Perform the deletion
				deleteCount = db.delete(ItemsTable.TABLE_ITEMS, selection, selectionArgs);
				break;

			case LIST_MULTI_ROWS:
				if (selection == null) {
					selection = "1";
				}

				if (!mSuppressDropboxChanges) {
					ListsTable.dbxDeleteMultipleRecords(mContext, mDbxDatastore, uri, selection, selectionArgs);
				}

				// Perform the deletion
				deleteCount = db.delete(ListsTable.TABLE_LISTS, selection, selectionArgs);
				break;

			case LIST_SINGLE_ROW:
				// Limit deletion to a single row
				rowIDstring = uri.getLastPathSegment();
				selection = ListsTable.COL_LIST_ID + "=" + rowIDstring
						+ (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : "");

				if (!mSuppressDropboxChanges) {
					ListsTable.dbxDeleteSingleRecord(mContext, mDbxDatastore, rowIDstring);
				}

				// Perform the deletion
				deleteCount = db.delete(ListsTable.TABLE_LISTS, selection, selectionArgs);
				break;

			case GROUPS_MULTI_ROWS:
				if (selection == null) {
					selection = "1";
				}

				if (!mSuppressDropboxChanges) {
					GroupsTable.dbxDeleteMultipleRecords(mContext, mDbxDatastore, uri, selection, selectionArgs);
				}

				// Perform the deletion
				deleteCount = db.delete(GroupsTable.TABLE_GROUPS, selection, selectionArgs);
				break;

			case GROUPS_SINGLE_ROW:
				rowIDstring = uri.getLastPathSegment();
				selection = GroupsTable.COL_GROUP_ID + "=" + rowIDstring
						+ (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : "");

				if (!mSuppressDropboxChanges) {
					GroupsTable.dbxDeleteSingleRecord(mContext, mDbxDatastore, rowIDstring);
				}

				// Perform the deletion
				deleteCount = db.delete(GroupsTable.TABLE_GROUPS, selection, selectionArgs);
				break;

			case STORES_MULTI_ROWS:
				if (selection == null) {
					selection = "1";
				}
				if (!mSuppressDropboxChanges) {
					StoresTable.dbxDeleteMultipleRecords(mContext, mDbxDatastore, uri, selection, selectionArgs);
				}

				// Perform the deletion
				deleteCount = db.delete(StoresTable.TABLE_STORES, selection, selectionArgs);
				break;

			case STORES_SINGLE_ROW:
				rowIDstring = uri.getLastPathSegment();
				selection = StoresTable.COL_STORE_ID + "=" + rowIDstring
						+ (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : "");

				if (!mSuppressDropboxChanges) {
					StoresTable.dbxDeleteSingleRecord(mContext, mDbxDatastore, rowIDstring);
				}

				// Perform the deletion
				deleteCount = db.delete(StoresTable.TABLE_STORES, selection, selectionArgs);
				break;

			case LOCATIONS_MULTI_ROWS:
				if (selection == null) {
					selection = "1";
				}

				if (!mSuppressDropboxChanges) {
					LocationsTable.dbxDeleteMultipleRecords(mContext, mDbxDatastore, uri, selection, selectionArgs);
				}

				// Perform the deletion
				deleteCount = db.delete(LocationsTable.TABLE_LOCATIONS, selection, selectionArgs);
				break;

			case LOCATIONS_SINGLE_ROW:
				rowIDstring = uri.getLastPathSegment();
				selection = LocationsTable.COL_LOCATION_ID + "=" + rowIDstring
						+ (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : "");

				if (!mSuppressDropboxChanges) {
					LocationsTable.dbxDeleteSingleRecord(mContext, mDbxDatastore, rowIDstring);
				}

				// Perform the deletion
				deleteCount = db.delete(LocationsTable.TABLE_LOCATIONS, selection, selectionArgs);
				break;

			case BRIDGE_MULTI_ROWS:
				if (selection == null) {
					selection = "1";
				}

				if (!mSuppressDropboxChanges) {
					BridgeTable.dbxDeleteMultipleRecords(mContext, mDbxDatastore, uri, selection, selectionArgs);
				}

				// Perform the deletion
				deleteCount = db.delete(BridgeTable.TABLE_BRIDGE, selection, selectionArgs);
				break;

			case BRIDGE_SINGLE_ROW:
				rowIDstring = uri.getLastPathSegment();
				selection = BridgeTable.COL_STORE_ID + "=" + rowIDstring
						+ (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : "");

				if (!mSuppressDropboxChanges) {
					BridgeTable.dbxDeleteSingleRecord(mContext, mDbxDatastore, rowIDstring);
				}

				// Perform the deletion
				deleteCount = db.delete(BridgeTable.TABLE_BRIDGE, selection, selectionArgs);
				break;

			default:
				throw new IllegalArgumentException("Method delete: Unknown URI: " + uri);
		}
		if (!mSuppressChangeNotification) {
			// Notify and observers of the change in the database.
			getContext().getContentResolver().notifyChange(uri, null);

			getContext().getContentResolver().notifyChange(ItemsTable.CONTENT_URI_ITEMS_WITH_GROUPS, null);
			getContext().getContentResolver().notifyChange(ItemsTable.CONTENT_URI_ITEMS_WITH_LOCATIONS, null);
			getContext().getContentResolver().notifyChange(GroupsTable.CONTENT_URI_GROUPS_WITH_LOCATIONS, null);
		}
		return deleteCount;
	}

	@Override
	public String getType(Uri uri) {
		int uriType = sURIMatcher.match(uri);
		switch (uriType) {
			case ITEMS_MULTI_ROWS:
				return ItemsTable.CONTENT_TYPE;
			case ITEMS_SINGLE_ROW:
				return ItemsTable.CONTENT_ITEM_TYPE;

			case LIST_MULTI_ROWS:
				return ListsTable.CONTENT_TYPE;
			case LIST_SINGLE_ROW:
				return ListsTable.CONTENT_ITEM_TYPE;

			case GROUPS_MULTI_ROWS:
				return GroupsTable.CONTENT_TYPE;
			case GROUPS_SINGLE_ROW:
				return GroupsTable.CONTENT_ITEM_TYPE;

			case STORES_MULTI_ROWS:
				return StoresTable.CONTENT_TYPE;
			case STORES_SINGLE_ROW:
				return StoresTable.CONTENT_ITEM_TYPE;

			case LOCATIONS_MULTI_ROWS:
				return LocationsTable.CONTENT_TYPE;
			case LOCATIONS_SINGLE_ROW:
				return LocationsTable.CONTENT_ITEM_TYPE;

			case BRIDGE_MULTI_ROWS:
				return BridgeTable.CONTENT_TYPE;
			case BRIDGE_SINGLE_ROW:
				return BridgeTable.CONTENT_ITEM_TYPE;

			default:
				throw new IllegalArgumentException("Method getType. Unknown URI: " + uri);
		}
	}

	@SuppressWarnings("resource")
	@Override
	public Uri insert(Uri uri, ContentValues values) {

		SQLiteDatabase db = null;
		long newRowID = 0;
		String nullColumnHack = null;

		// Open a WritableDatabase database to support the insert transaction
		db = database.getWritableDatabase();

		int uriType = sURIMatcher.match(uri);
		switch (uriType) {
			case ITEMS_MULTI_ROWS:
				values.put(ItemsTable.COL_DATE_TIME_LAST_USED, Calendar.getInstance().getTimeInMillis());
				newRowID = db.insertOrThrow(ItemsTable.TABLE_ITEMS, nullColumnHack, values);
				if (newRowID > 0) {
					// Construct and return the URI of the newly inserted row.
					Uri newRowUri = ContentUris.withAppendedId(ItemsTable.CONTENT_URI, newRowID);

					if (!mSuppressDropboxChanges) {
						try {
							ItemsTable.dbxInsert(mContext, mDbxDatastore, newRowID, values);
						} catch (DbxException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

					if (!mSuppressChangeNotification) {
						// Notify and observers of the change in the database.
						getContext().getContentResolver().notifyChange(ItemsTable.CONTENT_URI, null);

						getContext().getContentResolver().notifyChange(ItemsTable.CONTENT_URI_ITEMS_WITH_GROUPS, null);
						getContext().getContentResolver().notifyChange(ItemsTable.CONTENT_URI_ITEMS_WITH_LOCATIONS,
								null);
						getContext().getContentResolver().notifyChange(GroupsTable.CONTENT_URI_GROUPS_WITH_LOCATIONS,
								null);
					}
					return newRowUri;
				}
				return null;

			case ITEMS_SINGLE_ROW:
				throw new IllegalArgumentException(
						"Method insert: Cannon insert a new row with a single row URI. Illegal URI: " + uri);

			case LIST_MULTI_ROWS:
				newRowID = db.insertOrThrow(ListsTable.TABLE_LISTS, nullColumnHack, values);
				if (newRowID > 0) {
					// Construct and return the URI of the newly inserted row.
					Uri newRowUri = ContentUris.withAppendedId(ListsTable.CONTENT_URI, newRowID);

					try {
						if (!mSuppressDropboxChanges) {
							ListsTable.dbxInsert(mContext, mDbxDatastore, newRowID, values);
						}
					} catch (DbxException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					if (!mSuppressChangeNotification) {
						// Notify and observers of the change in the database.
						getContext().getContentResolver().notifyChange(ListsTable.CONTENT_URI, null);

						getContext().getContentResolver().notifyChange(ItemsTable.CONTENT_URI_ITEMS_WITH_GROUPS, null);
						getContext().getContentResolver().notifyChange(ItemsTable.CONTENT_URI_ITEMS_WITH_LOCATIONS,
								null);
						getContext().getContentResolver().notifyChange(GroupsTable.CONTENT_URI_GROUPS_WITH_LOCATIONS,
								null);
					}
					return newRowUri;
				}
				return null;

			case LIST_SINGLE_ROW:
				throw new IllegalArgumentException(
						"Method insert: Cannot insert a new row with a single row URI. Illegal URI: " + uri);

			case GROUPS_MULTI_ROWS:
				newRowID = db.insertOrThrow(GroupsTable.TABLE_GROUPS, nullColumnHack, values);
				if (newRowID > 0) {
					// Construct and return the URI of the newly inserted row.
					Uri newRowUri = ContentUris.withAppendedId(GroupsTable.CONTENT_URI, newRowID);

					if (!mSuppressDropboxChanges) {
						GroupsTable.dbxInsert(mContext, mDbxDatastore, newRowID, values);
					}

					if (!mSuppressChangeNotification) {
						// Notify and observers of the change in the database.
						getContext().getContentResolver().notifyChange(GroupsTable.CONTENT_URI, null);

						getContext().getContentResolver().notifyChange(ItemsTable.CONTENT_URI_ITEMS_WITH_GROUPS, null);
						getContext().getContentResolver().notifyChange(ItemsTable.CONTENT_URI_ITEMS_WITH_LOCATIONS,
								null);
						getContext().getContentResolver().notifyChange(GroupsTable.CONTENT_URI_GROUPS_WITH_LOCATIONS,
								null);
					}
					return newRowUri;
				}
				return null;

			case GROUPS_SINGLE_ROW:
				throw new IllegalArgumentException(
						"Method insert: Cannot insert a new row with a single row URI. Illegal URI: " + uri);

			case STORES_MULTI_ROWS:
				newRowID = db.insertOrThrow(StoresTable.TABLE_STORES, nullColumnHack, values);
				if (newRowID > 0) {
					// Construct and return the URI of the newly inserted row.
					Uri newRowUri = ContentUris.withAppendedId(StoresTable.CONTENT_URI, newRowID);

					if (!mSuppressDropboxChanges) {
						StoresTable.dbxInsert(mContext, mDbxDatastore, newRowID, values);
					}

					if (!mSuppressChangeNotification) {
						// Notify and observers of the change in the database.
						getContext().getContentResolver().notifyChange(StoresTable.CONTENT_URI, null);
					}
					return newRowUri;
				}
				return null;

			case STORES_SINGLE_ROW:
				throw new IllegalArgumentException(
						"Method insert: Cannot insert a new row with a single row URI. Illegal URI: " + uri);

			case LOCATIONS_MULTI_ROWS:
				newRowID = db.insertOrThrow(LocationsTable.TABLE_LOCATIONS, nullColumnHack, values);
				if (newRowID > 0) {
					// Construct and return the URI of the newly inserted row.
					Uri newRowUri = ContentUris.withAppendedId(LocationsTable.CONTENT_URI, newRowID);

					if (!mSuppressDropboxChanges) {
						LocationsTable.dbxInsert(mContext, mDbxDatastore, newRowID, values);
					}
					if (!mSuppressChangeNotification) {
						// Notify and observers of the change in the database.
						getContext().getContentResolver().notifyChange(LocationsTable.CONTENT_URI, null);

						getContext().getContentResolver().notifyChange(ItemsTable.CONTENT_URI_ITEMS_WITH_GROUPS, null);
						getContext().getContentResolver().notifyChange(ItemsTable.CONTENT_URI_ITEMS_WITH_LOCATIONS,
								null);
						getContext().getContentResolver().notifyChange(GroupsTable.CONTENT_URI_GROUPS_WITH_LOCATIONS,
								null);
					}
					return newRowUri;
				}
				return null;

			case LOCATIONS_SINGLE_ROW:
				throw new IllegalArgumentException(
						"Method insert: Cannot insert a new row with a single row URI. Illegal URI: " + uri);

			case BRIDGE_MULTI_ROWS:
				newRowID = db.insertOrThrow(BridgeTable.TABLE_BRIDGE, nullColumnHack, values);
				if (newRowID > 0) {
					// Construct and return the URI of the newly inserted row.
					Uri newRowUri = ContentUris.withAppendedId(BridgeTable.CONTENT_URI, newRowID);

					if (!mSuppressDropboxChanges) {
						BridgeTable.dbxInsert(mContext, mDbxDatastore, newRowID, values);
					}

					if (!mSuppressChangeNotification) {
						// Notify and observers of the change in the database.
						getContext().getContentResolver().notifyChange(BridgeTable.CONTENT_URI, null);

						getContext().getContentResolver().notifyChange(ItemsTable.CONTENT_URI_ITEMS_WITH_GROUPS, null);
						getContext().getContentResolver().notifyChange(ItemsTable.CONTENT_URI_ITEMS_WITH_LOCATIONS,
								null);
						getContext().getContentResolver().notifyChange(GroupsTable.CONTENT_URI_GROUPS_WITH_LOCATIONS,
								null);
					}
					return newRowUri;
				}
				return null;

			case BRIDGE_SINGLE_ROW:
				throw new IllegalArgumentException(
						"Method insert: Cannot insert a new row with a single row URI. Illegal URI: " + uri);

			default:
				throw new IllegalArgumentException("Method insert: Unknown URI: " + uri);
		}
	}

	@SuppressWarnings("resource")
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

		// Using SQLiteQueryBuilder
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

		int uriType = sURIMatcher.match(uri);
		switch (uriType) {
			case ITEMS_MULTI_ROWS:
				queryBuilder.setTables(ItemsTable.TABLE_ITEMS);
				checkItemsColumnNames(projection);
				break;

			case ITEMS_SINGLE_ROW:
				queryBuilder.setTables(ItemsTable.TABLE_ITEMS);
				checkItemsColumnNames(projection);
				queryBuilder.appendWhere(ItemsTable.COL_ITEM_ID + "=" + uri.getLastPathSegment());
				break;

			case LIST_MULTI_ROWS:
				queryBuilder.setTables(ListsTable.TABLE_LISTS);
				checkListsColumnNames(projection);
				break;

			case LIST_SINGLE_ROW:
				queryBuilder.setTables(ListsTable.TABLE_LISTS);
				checkListsColumnNames(projection);
				queryBuilder.appendWhere(ListsTable.COL_LIST_ID + "=" + uri.getLastPathSegment());
				break;

			case GROUPS_MULTI_ROWS:
				queryBuilder.setTables(GroupsTable.TABLE_GROUPS);
				checkGroupsColumnNames(projection);
				break;

			case GROUPS_SINGLE_ROW:
				queryBuilder.setTables(GroupsTable.TABLE_GROUPS);
				checkGroupsColumnNames(projection);
				queryBuilder.appendWhere(GroupsTable.COL_GROUP_ID + "=" + uri.getLastPathSegment());
				break;

			case STORES_MULTI_ROWS:
				queryBuilder.setTables(StoresTable.TABLE_STORES);
				checkStoresColumnNames(projection);
				break;

			case STORES_SINGLE_ROW:
				queryBuilder.setTables(StoresTable.TABLE_STORES);
				checkStoresColumnNames(projection);
				queryBuilder.appendWhere(StoresTable.COL_STORE_ID + "=" + uri.getLastPathSegment());
				break;

			case LOCATIONS_MULTI_ROWS:
				queryBuilder.setTables(LocationsTable.TABLE_LOCATIONS);
				checkLocationsColumnNames(projection);
				break;

			case LOCATIONS_SINGLE_ROW:
				queryBuilder.setTables(LocationsTable.TABLE_LOCATIONS);
				checkLocationsColumnNames(projection);
				queryBuilder.appendWhere(LocationsTable.COL_LOCATION_ID + "=" + uri.getLastPathSegment());
				break;

			case BRIDGE_MULTI_ROWS:
				queryBuilder.setTables(BridgeTable.TABLE_BRIDGE);
				// checkBridgeColumnNames(projection);
				break;

			case BRIDGE_SINGLE_ROW:
				queryBuilder.setTables(BridgeTable.TABLE_BRIDGE);
				checkBridgeColumnNames(projection);
				queryBuilder.appendWhere(BridgeTable.COL_BRIDGE_ID + "=" + uri.getLastPathSegment());
				break;

			case ITEMS_WITH_GROUPS:
				/*SELECT tblItems._id, tblItems.itemName, tblItems.groupID, tblGroups.groupName 
				FROM tblItems 
				JOIN tblGroups ON tblItems.groupID = tblGroups._id
				WHERE tblItems.listID=3
				ORDER BY groupName ASC, itemName ASC*/

				String tables = ItemsTable.TABLE_ITEMS +
						" JOIN " + GroupsTable.TABLE_GROUPS + " ON "
						+ ItemsTable.TABLE_ITEMS + "." + ItemsTable.COL_GROUP_ID + " = "
						+ GroupsTable.TABLE_GROUPS + "." + GroupsTable.COL_GROUP_ID;
				queryBuilder.setTables(tables);

				break;

			case ITEMS_WITH_LOCATIONS:
				/*SELECT tblItems._id,tblItems.itemName, tblItems.itemNote, tblItems.groupID, tblBridge.locationID, tblLocations.locationName
				FROM tblItems
				JOIN tblBridge ON tblItems.groupID = tblBridge.groupID
				JOIN tblLocations ON tblBridge.locationID = tblLocations._id
				WHERE tblItems.listID=2 AND tblBridge.storeID =5
				ORDER BY   tblLocations.locationName, tblItems.itemName*/

				projection = ItemsTable.PROJECTION_WITH_LOCATION_NAME;

				tables = ItemsTable.TABLE_ITEMS
						+ " JOIN " + BridgeTable.TABLE_BRIDGE + " ON "
						+ ItemsTable.TABLE_ITEMS + "." + ItemsTable.COL_GROUP_ID + " = "
						+ BridgeTable.TABLE_BRIDGE + "." + BridgeTable.COL_GROUP_ID

						+ " JOIN " + LocationsTable.TABLE_LOCATIONS + " ON "
						+ BridgeTable.TABLE_BRIDGE + "." + BridgeTable.COL_LOCATION_ID + " = "
						+ LocationsTable.TABLE_LOCATIONS + "." + LocationsTable.COL_LOCATION_ID;

				queryBuilder.setTables(tables);

				sortOrder = LocationsTable.SORT_ORDER_LOCATION + ", " + ItemsTable.SORT_ORDER_ITEM_NAME;

				break;

			case GROUPS_WITH_LOCATIONS:

				/*SELECT tblGroups._id, tblGroups.groupName,tblBridge.locationID, tblLocations.locationName
				
				FROM tblGroups
				JOIN tblBridge ON tblGroups._id= tblBridge.groupID
				JOIN tblLocations ON tblLocations._id =  tblBridge.locationID
				
				WHERE tblGroups.listID = 3 AND tblBridge.storeID=2
				ORDER BY   tblLocations.locationName, tblGroups.groupName*/

				tables = GroupsTable.TABLE_GROUPS
						+ " JOIN " + BridgeTable.TABLE_BRIDGE + " ON "
						+ GroupsTable.TABLE_GROUPS + "." + GroupsTable.COL_GROUP_ID + " = "
						+ BridgeTable.TABLE_BRIDGE + "." + BridgeTable.COL_GROUP_ID

						+ " JOIN " + LocationsTable.TABLE_LOCATIONS + " ON "
						+ LocationsTable.TABLE_LOCATIONS + "." + LocationsTable.COL_LOCATION_ID + " = "
						+ BridgeTable.TABLE_BRIDGE + "." + BridgeTable.COL_LOCATION_ID;

				queryBuilder.setTables(tables);
				break;

			default:
				throw new IllegalArgumentException("Method query. Unknown URI: " + uri);
		}

		// Execute the query on the database
		SQLiteDatabase db = null;
		try {
			db = database.getWritableDatabase();
		} catch (SQLiteException ex) {
			db = database.getReadableDatabase();
		}

		if (null != db) {
			String groupBy = null;
			String having = null;
			Cursor cursor = null;
			try {
				cursor = queryBuilder.query(db, projection, selection, selectionArgs, groupBy, having, sortOrder);
			} catch (Exception e) {
				MyLog.e("Exception error in AListContentProvider:query. ", e.toString());
			}

			if (null != cursor) {
				cursor.setNotificationUri(getContext().getContentResolver(), uri);
			}
			return cursor;
		}
		return null;
	}

	@SuppressWarnings("resource")
	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		String rowIDstring = null;
		int updateCount = 0;

		// Open a WritableDatabase database to support the update transaction
		SQLiteDatabase db = database.getWritableDatabase();

		int uriType = sURIMatcher.match(uri);
		switch (uriType) {
			case ITEMS_MULTI_ROWS:
				if (values.containsKey(ItemsTable.COL_SELECTED)) {
					// update time last used if item is being selected
					int selectedValue = values.getAsInteger(ItemsTable.COL_SELECTED);
					if (selectedValue == 1) {
						values.put(ItemsTable.COL_DATE_TIME_LAST_USED, Calendar.getInstance().getTimeInMillis());
					}
				}

				// Perform the update
				if (!mSuppressDropboxChanges) {
					ItemsTable.dbxUpdateMultipleRecords(mContext, mDbxDatastore, values, uri, selection,
							selectionArgs);
				}
				updateCount = db.update(ItemsTable.TABLE_ITEMS, values, selection, selectionArgs);
				break;

			case ITEMS_SINGLE_ROW:
				// Limit update to a single row
				rowIDstring = uri.getLastPathSegment();
				selection = ItemsTable.COL_ITEM_ID + "=" + rowIDstring
						+ (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : "");

				if (values.containsKey(ItemsTable.COL_SELECTED)) {
					// update time last used if item is being selected
					int selectedValue = values.getAsInteger(ItemsTable.COL_SELECTED);
					if (selectedValue == 1) {
						values.put(ItemsTable.COL_DATE_TIME_LAST_USED, Calendar.getInstance().getTimeInMillis());
					}
				}
				// Perform the update
				if (!mSuppressDropboxChanges) {
					ItemsTable.dbxUpdateSingleRecord(mContext, mDbxDatastore, values, uri);
				}
				updateCount = db.update(ItemsTable.TABLE_ITEMS, values, selection, selectionArgs);

				break;

			case LIST_MULTI_ROWS:
				// Perform the update
				if (!mSuppressDropboxChanges) {
					ListsTable.dbxUpdateMultipleRecords(mContext, mDbxDatastore, values, uri, selection, selectionArgs);
				}
				updateCount = db.update(ListsTable.TABLE_LISTS, values, selection, selectionArgs);
				break;

			case LIST_SINGLE_ROW:
				// Limit deletion to a single row
				rowIDstring = uri.getLastPathSegment();
				selection = ListsTable.COL_LIST_ID + "=" + rowIDstring
						+ (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : "");
				// Perform the update
				if (!mSuppressDropboxChanges) {
					ListsTable.dbxUpdateSingleRecord(mContext, mDbxDatastore, values, uri);
				}
				updateCount = db.update(ListsTable.TABLE_LISTS, values, selection, selectionArgs);
				break;

			case GROUPS_MULTI_ROWS:
				// Perform the update
				if (!mSuppressDropboxChanges) {
					GroupsTable.dbxUpdateMultipleRecords(mContext, mDbxDatastore, values, uri, selection,
							selectionArgs);
				}
				updateCount = db.update(GroupsTable.TABLE_GROUPS, values, selection, selectionArgs);
				break;

			case GROUPS_SINGLE_ROW:
				// Limit deletion to a single row
				rowIDstring = uri.getLastPathSegment();
				selection = GroupsTable.COL_GROUP_ID + "=" + rowIDstring
						+ (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : "");
				// Perform the update
				if (!mSuppressDropboxChanges) {
					GroupsTable.dbxUpdateSingleRecord(mContext, mDbxDatastore, values, uri);
				}
				updateCount = db.update(GroupsTable.TABLE_GROUPS, values, selection, selectionArgs);
				break;

			case STORES_MULTI_ROWS:
				// Perform the update
				if (!mSuppressDropboxChanges) {
					StoresTable.dbxUpdateMultipleRecords(mContext, mDbxDatastore, values, uri, selection,
							selectionArgs);
				}
				updateCount = db.update(StoresTable.TABLE_STORES, values, selection, selectionArgs);
				break;

			case STORES_SINGLE_ROW:
				// Limit deletion to a single row
				rowIDstring = uri.getLastPathSegment();
				selection = StoresTable.COL_STORE_ID + "=" + rowIDstring
						+ (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : "");
				// Perform the update
				if (!mSuppressDropboxChanges) {
					StoresTable.dbxUpdateSingleRecord(mContext, mDbxDatastore, values, uri);
				}
				updateCount = db.update(StoresTable.TABLE_STORES, values, selection, selectionArgs);
				break;

			case LOCATIONS_MULTI_ROWS:
				// Perform the update
				if (!mSuppressDropboxChanges) {
					LocationsTable.dbxUpdateMultipleRecords(mContext, mDbxDatastore, values, uri, selection,
							selectionArgs);
				}
				updateCount = db.update(LocationsTable.TABLE_LOCATIONS, values, selection, selectionArgs);
				break;

			case LOCATIONS_SINGLE_ROW:
				// Limit deletion to a single row
				rowIDstring = uri.getLastPathSegment();
				selection = LocationsTable.COL_LOCATION_ID + "=" + rowIDstring
						+ (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : "");
				// Perform the update
				if (!mSuppressDropboxChanges) {
					LocationsTable.dbxUpdateSingleRecord(mContext, mDbxDatastore, values, uri);
				}
				updateCount = db.update(LocationsTable.TABLE_LOCATIONS, values, selection, selectionArgs);
				break;

			case BRIDGE_MULTI_ROWS:
				// Perform the update
				if (!mSuppressDropboxChanges) {
					BridgeTable.dbxUpdateMultipleRecords(mContext, mDbxDatastore, values, uri, selection,
							selectionArgs);
				}
				updateCount = db.update(BridgeTable.TABLE_BRIDGE, values, selection, selectionArgs);
				break;

			case BRIDGE_SINGLE_ROW:
				// Limit deletion to a single row
				rowIDstring = uri.getLastPathSegment();
				selection = BridgeTable.COL_BRIDGE_ID + "=" + rowIDstring
						+ (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : "");
				// Perform the update
				if (!mSuppressDropboxChanges) {
					BridgeTable.dbxUpdateSingleRecord(mContext, mDbxDatastore, values, uri);
				}
				updateCount = db.update(BridgeTable.TABLE_BRIDGE, values, selection, selectionArgs);
				break;

			default:
				throw new IllegalArgumentException("Method update: Unknown URI: " + uri);
		}
		if (!mSuppressChangeNotification) {
			// Notify any observers of the change in the database.
			getContext().getContentResolver().notifyChange(uri, null);

			getContext().getContentResolver().notifyChange(ItemsTable.CONTENT_URI_ITEMS_WITH_GROUPS, null);
			getContext().getContentResolver().notifyChange(ItemsTable.CONTENT_URI_ITEMS_WITH_LOCATIONS, null);
			getContext().getContentResolver().notifyChange(GroupsTable.CONTENT_URI_GROUPS_WITH_LOCATIONS, null);
		}
		return updateCount;
	}

	/* From: http://stackoverflow.com/questions/14090695/how-to-use-join-query-in-cursorloader-when-its-constructor-does-not-support-it
	 
	The Uri does not point to any table. It points to whatever you feel like pointing it to.

	Let's pretend that your two tables are Customer and Order. One customer may have many orders. 
	You want to execute a query to get all outstanding orders... but you want to join in some customer-related columns 
	that you will need, such as the customer's name.

	Let's further pretend that you already have 
	content://your.authority.goes.here/customer and content://your.authority.goes.here/order 
	defined to purely query those tables.

	You have two choices:

	(1)	Add the join of the customer's display name on your /order Uri. 
		Having another available column probably will not break any existing consumers 
		of the provider (though testing is always a good idea). This is what ContactsContract does -- 
		it joins in some base columns, like the contact's name, on pretty much all queries of all tables.
		
	(2)	Create content://your.authority.goes.here/orderWithCust 
		that does the same basic query as /order does, but contains your join. 
		In this case, you could have insert(), update(), and delete() throw some sort of RuntimeException, 
		to remind you that you should not be modifying data using /orderWithCust as a Uri.

	In the end, designing a ContentProvider Uri system is similar to designing a REST Web service's URL system. 
	In both cases, the join has to be done on the provider/server side, and so you may need to break 
	the one-table-to-one-URL baseline to offer up some useful joins.*/

	private void checkItemsColumnNames(String[] projection) {
		// Check if the caller has requested a column that does not exist
		if (projection != null) {
			HashSet<String> requstedColumns = new HashSet<String>(Arrays.asList(projection));
			HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(ItemsTable.PROJECTION_ALL));

			// Check if all columns which are requested are available
			if (!availableColumns.containsAll(requstedColumns)) {
				throw new IllegalArgumentException(
						"Method checkItemsColumnNames: Unknown MasterListItemsTable column name!");
			}
		}
	}

	private void checkListsColumnNames(String[] projection) {
		// Check if the caller has requested a column that does not exist
		if (projection != null) {
			HashSet<String> requstedColumns = new HashSet<String>(Arrays.asList(projection));
			HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(ListsTable.PROJECTION_ALL));

			// Check if all columns which are requested are available
			if (!availableColumns.containsAll(requstedColumns)) {
				throw new IllegalArgumentException(
						"Method checkListsColumnNames: Unknown ListTitlesTable column name!");
			}
		}
	}

	private void checkGroupsColumnNames(String[] projection) {
		// Check if the caller has requested a column that does not exist
		if (projection != null) {
			HashSet<String> requstedColumns = new HashSet<String>(Arrays.asList(projection));
			HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(GroupsTable.PROJECTION_ALL));

			// Check if all columns which are requested are available
			if (!availableColumns.containsAll(requstedColumns)) {
				throw new IllegalArgumentException(
						"Method checkListsColumnNames: Unknown ListTitlesTable column name!");
			}
		}
	}

	private void checkStoresColumnNames(String[] projection) {
		// Check if the caller has requested a column that does not exist
		if (projection != null) {
			HashSet<String> requstedColumns = new HashSet<String>(Arrays.asList(projection));
			HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(StoresTable.PROJECTION_ALL));

			// Check if all columns which are requested are available
			if (!availableColumns.containsAll(requstedColumns)) {
				throw new IllegalArgumentException(
						"Method checkListsColumnNames: Unknown ListTitlesTable column name!");
			}
		}
	}

	private void checkLocationsColumnNames(String[] projection) {
		// Check if the caller has requested a column that does not exist
		if (projection != null) {
			HashSet<String> requstedColumns = new HashSet<String>(Arrays.asList(projection));
			HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(LocationsTable.PROJECTION_ALL));

			// Check if all columns which are requested are available
			if (!availableColumns.containsAll(requstedColumns)) {
				throw new IllegalArgumentException(
						"Method checkListsColumnNames: Unknown ListTitlesTable column name!");
			}
		}
	}

	private void checkBridgeColumnNames(String[] projection) {
		// Check if the caller has requested a column that does not exist
		if (projection != null) {
			HashSet<String> requstedColumns = new HashSet<String>(Arrays.asList(projection));
			HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(BridgeTable.PROJECTION_ALL));

			// Check if all columns which are requested are available
			if (!availableColumns.containsAll(requstedColumns)) {
				throw new IllegalArgumentException(
						"Method checkListsColumnNames: Unknown ListTitlesTable column name!");
			}
		}
	}

	/**
	 * A test package can call this to get a handle to the database underlying AListContentProvider, so it can insert test data into the database. The
	 * test case class is responsible for instantiating the provider in a test context; {@link android.test.ProviderTestCase2} does this during the
	 * call to setUp()
	 * 
	 * @return a handle to the database helper object for the provider's data.
	 */
	public AListDatabaseHelper getOpenHelperForTest() {
		return database;
	}

}
