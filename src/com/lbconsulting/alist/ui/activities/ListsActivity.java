package com.lbconsulting.alist.ui.activities;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.dropbox.sync.android.DbxAccount;
import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxDatastore;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxFields;
import com.dropbox.sync.android.DbxRecord;
import com.dropbox.sync.android.DbxTable;
import com.lbconsulting.alist.R;
import com.lbconsulting.alist.adapters.ListsPagerAdapter;
import com.lbconsulting.alist.classes.AListEvents.NewListCreated;
import com.lbconsulting.alist.classes.DynamicListView;
import com.lbconsulting.alist.classes.ListSettings;
import com.lbconsulting.alist.database.AListContentProvider;
import com.lbconsulting.alist.database.GroupsTable;
import com.lbconsulting.alist.database.ItemsTable;
import com.lbconsulting.alist.database.ListsTable;
import com.lbconsulting.alist.database.LocationsTable;
import com.lbconsulting.alist.database.StoresTable;
import com.lbconsulting.alist.dialogs.ListsDialogFragment;
import com.lbconsulting.alist.ui.fragments.MasterListFragment;
import com.lbconsulting.alist.utilities.AListUtilities;
import com.lbconsulting.alist.utilities.MyLog;

import de.greenrobot.event.EventBus;

public class ListsActivity extends FragmentActivity implements DbxDatastore.SyncStatusListener {

	private Button mLinkButton;
	private DbxAccountManager mAccountManager = null;
	private DbxAccount mAccount = null;
	private static DbxDatastore mDbxDatastore = null;

	private ListsPagerAdapter mListsPagerAdapter;
	private ViewPager mPager;

	private MasterListFragment mMasterListFragment;
	private Boolean mTwoFragmentLayout = false;

	private String FILENAME = "AListStoreSubmission.txt";

	private long NO_ACTIVE_LIST_ID = 0;
	private long mActiveListID = NO_ACTIVE_LIST_ID;
	private int mActiveListPosition = 0;
	private long mActiveItemID;
	// private long mActiveStoreID = -1;

	private ListSettings mListSettings;

	private Cursor mAllListsCursor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MyLog.i("Lists_ACTIVITY", "onCreate()");
		setContentView(R.layout.activity_lists_pager);

		AListContentProvider.setContext(this);
		EventBus.getDefault().register(this);

		SharedPreferences storedStates = getSharedPreferences("AList", MODE_PRIVATE);
		mActiveListID = storedStates.getLong("ActiveListID", -1);
		mActiveListPosition = storedStates.getInt("ActiveListPosition", -1);
		mActiveItemID = storedStates.getLong("ActiveItemID", -1);

		// check to see if we're in a horizontal orientation
		View frag_masterList_placeholder = this.findViewById(R.id.frag_masterList_placeholder);
		mTwoFragmentLayout = frag_masterList_placeholder != null
				&& frag_masterList_placeholder.getVisibility() == View.VISIBLE;

		mAllListsCursor = ListsTable.getAllLists(this);
		mListSettings = new ListSettings(this, mActiveListID);
		DynamicListView.setManualSort(mListSettings.isManualSort());

		mListsPagerAdapter = new ListsPagerAdapter(getSupportFragmentManager(), this);
		mPager = (ViewPager) findViewById(R.id.listsPager);
		mPager.setAdapter(mListsPagerAdapter);
		mPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageScrollStateChanged(int state) {
			}

			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			}

			@Override
			public void onPageSelected(int position) {
				// A list page has been selected
				SetActiveListID(position);
				DynamicListView.setManualSort(mListSettings.isManualSort());
				MyLog.d("Lists_ACTIVITY", "onPageSelected() - position = " + position + " ; listID = " + mActiveListID);

				if (mTwoFragmentLayout) {
					LoadMasterListFragment();
				}
			}
		});

		mLinkButton = (Button) findViewById(R.id.link_button);

		if (ListsTable.isAnyListSyncedToDropBox(this)) {
			// there is at least one list synced to dropbox
			// get the dbxDatastore from the AList Content Provider
			mDbxDatastore = AListContentProvider.getDbxDatastore();
			if (mDbxDatastore == null) {
				// the application has not been authenticated with dropbox
				// so do the "OAuth" dance
				mAccountManager = DbxAccountManager.getInstance(getApplicationContext(), AListUtilities.APP_KEY,
						AListUtilities.APP_SECRET);
				mPager.setVisibility(View.GONE);

				mLinkButton.setVisibility(View.VISIBLE);
				mLinkButton.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						mAccountManager.startLink(ListsActivity.this, AListUtilities.REQUEST_LINK_TO_DBX);
					}
				});
			} else {
				// the application has been authenticated with dropbox
				mDbxDatastore.addSyncStatusListener(this);
				mLinkButton.setVisibility(View.GONE);
				mPager.setVisibility(View.VISIBLE);
			}
		}
	}

	@Override
	public void onDatastoreStatusChange(DbxDatastore store) {
		if (store.getSyncStatus().hasIncoming) {
			// Handle the updated data
			try {
				Map<String, Set<DbxRecord>> changes = mDbxDatastore.sync();

				AListContentProvider.setSuppressDropboxChanges(true);
				for (Map.Entry<String, Set<DbxRecord>> table : changes.entrySet()) {
					String tableName = table.getKey();
					if (tableName.equals(ItemsTable.TABLE_ITEMS)) {
						Set<?> recordSet = table.getValue();
						Iterator<?> itr = recordSet.iterator();
						while (itr.hasNext()) {
							DbxRecord dbxRecord = (DbxRecord) itr.next();
							String dbxRecordID = dbxRecord.getId();

							if (!dbxRecordID.isEmpty()) {
								if (dbxRecord.isDeleted()) {
									ItemsTable.DeleteItem(this, dbxRecordID);
								} else {
									// record is either a new or revised record
									// try and get the SQLite record
									Cursor itemCursor = ItemsTable.getItemFromDropboxID(this, dbxRecordID);
									if (itemCursor != null && itemCursor.getCount() > 0) {
										// update the existing record
										ItemsTable.UpdateItem(this, dbxRecordID, dbxRecord);
									} else {
										// create a new record
										ItemsTable.CreateItem(this, dbxRecord);
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
				MyLog.e("MainActivity: onDatastoreStatusChange ", "DbxException.");
			} finally {
				AListContentProvider.setSuppressDropboxChanges(false);
			}
		}

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == AListUtilities.REQUEST_LINK_TO_DBX) {
			if (resultCode == Activity.RESULT_OK) {

				mAccount = mAccountManager.getLinkedAccount();
				String userid = mAccount.getUserId();
				String msg = "Dropbox user < " + userid + " > linked.";

				try {
					mDbxDatastore = DbxDatastore.openDefault(mAccount);
					AListContentProvider.setDbxDatastore(mDbxDatastore);
					mDbxDatastore.addSyncStatusListener(this);

				} catch (DbxException e) {
					MyLog.e("Lists_ACTIVITY", "onActivityResult(): DbxException while trying to openDefault datastore.");
					e.printStackTrace();
				}

				mLinkButton.setVisibility(View.GONE);
				// ... Now display your own UI using the linked account information.
				mPager.setVisibility(View.VISIBLE);

				if (mTwoFragmentLayout) {
					LoadMasterListFragment();
				}
				if (mActiveListID < 2) {
					CreatNewList();
				}

				MyLog.i("Lists_ACTIVITY", "onActivityResult(): " + msg);
				Toast.makeText(this, msg, Toast.LENGTH_LONG).show();

			} else {
				MyLog.e("Lists_ACTIVITY", "onActivityResult(): Dropbox link failed or was cancelled by the user.");
				// ... Link failed or was cancelled by the user.
			}
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	public void onEvent(NewListCreated event) {
		mActiveListID = event.getActiveListID();
		// restart activity to ensure that all lists are shown in
		// alphabetical order
		ReStartListsActivity();
	}

	private void SetActiveListID(int position) {
		if (mAllListsCursor != null) {
			try {
				mAllListsCursor.moveToPosition(position);
				mActiveListID = mAllListsCursor.getLong(mAllListsCursor.getColumnIndexOrThrow(ListsTable.COL_LIST_ID));
				mListSettings = new ListSettings(this, mActiveListID);
				mActiveListPosition = position;
			} catch (Exception e) {
				MyLog.d("Lists_ACTIVITY", "Exception in getlistID: " + e);
			}
		}
	}

	private void ReStartListsActivity() {
		mAllListsCursor = ListsTable.getAllLists(this);
		mActiveListPosition = AListUtilities.getCursorPositon(mAllListsCursor, mActiveListID);
		Intent intent = new Intent(this, ListsActivity.class);
		// prohibit the back button from displaying previous version of this ListPreferencesActivity
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	private void ReStartListsActivity(int position) {
		mAllListsCursor = ListsTable.getAllLists(this);
		if (mAllListsCursor.getCount() >= position + 1) {
			mActiveListPosition = position;
		} else {
			if (mAllListsCursor.getCount() > 0) {
				mActiveListPosition = 0;
			} else {
				// there are no lists in the ListsTable!
				mActiveListPosition = -1;
				mActiveListID = -1;
			}

		}
		if (mActiveListPosition > -1) {
			mActiveListID = AListUtilities.getIdByPosition(mAllListsCursor, mActiveListPosition);
		}
		Intent intent = new Intent(this, ListsActivity.class);
		// prohibit the back button from displaying previous version of this ListPreferencesActivity
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	private void StartMasterListActivity() {
		Intent masterListActivityIntent = new Intent(this, MasterListActivity.class);
		// masterListActivityIntent.putExtra("ActiveListID", mActiveListID);
		startActivity(masterListActivityIntent);
	}

	private void StartManageLocationsActivity() {
		Intent intent = new Intent(this, ManageLocationsActivity.class);
		intent.putExtra("ActiveListID", mActiveListID);
		startActivity(intent);
	}

	private void StartListPreferencesActivity() {
		Intent intent = new Intent(this, ListPreferencesActivity.class);
		startActivity(intent);
	}

	private void StartAboutActivity() {
		Intent intent = new Intent(this, AboutActivity.class);
		startActivity(intent);
	}

	private void LoadMasterListFragment() {
		mMasterListFragment = (MasterListFragment) this.getSupportFragmentManager().findFragmentByTag(
				"MasterListFragment");
		if (mMasterListFragment == null) {
			// create MasterListFragment
			mMasterListFragment = MasterListFragment.newInstance(mActiveListID);

			MyLog.i("Lists_ACTIVITY", "LoadMasterListFragment. New MasterListFragment created. ListID = "
					+ mActiveListID);
			// add the fragment to the Activity
			this.getSupportFragmentManager().beginTransaction()
					.add(R.id.frag_masterList_placeholder, mMasterListFragment, "MasterListFragment")
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
					.commit();
			MyLog.i("Lists_ACTIVITY", "LoadMasterListFragment. MasterListFragment ADD. ListID = " + mActiveListID);
		} else {
			// MasterListFragment exists ... so replace it
			mMasterListFragment = MasterListFragment.newInstance(mActiveListID);

			MyLog.i("Lists_ACTIVITY", "LoadMasterListFragment. New MasterListFragment created. ListID = "
					+ mActiveListID);
			// add the fragment to the Activity
			this.getSupportFragmentManager().beginTransaction()
					.replace(R.id.frag_masterList_placeholder, mMasterListFragment, "MasterListFragment")
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
					.commit();
			MyLog.i("Lists_ACTIVITY", "LoadMasterListFragment. MasterListFragment REPLACE. ListID = " + mActiveListID);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		MyLog.i("Lists_ACTIVITY", "onSaveInstanceState");
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		MyLog.i("Lists_ACTIVITY", "onRestoreInstanceState");
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onStart() {
		MyLog.i("Lists_ACTIVITY", "onStart");
		super.onStart();
	}

	@Override
	protected void onRestart() {
		MyLog.i("Lists_ACTIVITY", "onRestart");
		super.onRestart();
	}

	@Override
	protected void onResume() {
		MyLog.i("Lists_ACTIVITY", "onResume()");

		if (ListsTable.isAnyListSyncedToDropBox(this)) {
			mDbxDatastore = AListContentProvider.getDbxDatastore();
			if (mDbxDatastore != null) {
				if (!mDbxDatastore.isOpen()) {
					mAccountManager = DbxAccountManager.getInstance(getApplicationContext(), AListUtilities.APP_KEY,
							AListUtilities.APP_SECRET);
					if (mAccountManager.hasLinkedAccount()) {
						mAccount = mAccountManager.getLinkedAccount();
						try {
							mDbxDatastore = DbxDatastore.openDefault(mAccount);
							AListContentProvider.setDbxDatastore(mDbxDatastore);
						} catch (DbxException e) {
							MyLog.e("Lists_ACTIVITY", "onResume(): DbxException while opening dbxDatastore");
							e.printStackTrace();
						}
					} else {
						MyLog.e("Lists_ACTIVITY", "onResume(): there is no linked account!");
					}
				}
				new ValidateSqlTables().execute();
			}
		}

		if (mTwoFragmentLayout) {
			LoadMasterListFragment();
		}

		if (mActiveListID < 2) {
			CreatNewList();
		} else {
			mPager.setCurrentItem(mActiveListPosition);
		}

		super.onResume();
	}

	private void DeleteAllDropboxTables() {
		ListsTable.dbxDeleteAllRecords(mDbxDatastore);
		ItemsTable.dbxDeleteAllRecords(mDbxDatastore);
	}

	private class ValidateSqlTables extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			// TODO Show sync progress
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			AListContentProvider.setSuppressDropboxChanges(true);

			// check to see if there are any lists that are being synced for the first time
			Cursor firstTimeSyncLists = ListsTable.getFirstTimeSyncLists(ListsActivity.this);
			if (firstTimeSyncLists != null) {

				while (firstTimeSyncLists.moveToNext()) {

					String listTitle = firstTimeSyncLists.getString(firstTimeSyncLists
							.getColumnIndexOrThrow(ListsTable.COL_LIST_TITLE));
					long listID = firstTimeSyncLists.getLong(firstTimeSyncLists
							.getColumnIndexOrThrow(ListsTable.COL_LIST_ID));

					DbxFields queryParams = new DbxFields().set(ListsTable.COL_LIST_TITLE, listTitle);
					DbxTable dbxListsTable = mDbxDatastore.getTable(ListsTable.TABLE_LISTS);
					if (dbxListsTable != null) {
						// the lists table was found in the dropbox database
						DbxTable.QueryResult results;
						try {
							results = dbxListsTable.query(queryParams);

							if (results != null && results.count() > 0) {
								// found the list in dropbox ... so delete it from the SQLite database
								// get the dropbox ID and " is list preference synced" to carry over to
								// the new SQLite list
								int isListPreferencesSyncedToDropbox = firstTimeSyncLists.getInt(firstTimeSyncLists
										.getColumnIndexOrThrow(ListsTable.COL_IS_LIST_PREF_SYNCED_TO_DROPBOX));
								DbxRecord firstResult = results.iterator().next();

								// delete the SQLite list
								ListsTable.DeleteList(ListsActivity.this, listID);

								firstResult
										.set(ListsTable.COL_IS_SYNCED_TO_DROPBOX, 1)
										.set(ListsTable.COL_IS_FIRST_TIME_SYNC, isListPreferencesSyncedToDropbox)
										.set(ListsTable.COL_IS_FIRST_TIME_SYNC, 0);
							} else {
								// The first time sync list does not exist in the dropbox database.

								// clear the first time sync list flag
								ContentValues cv = new ContentValues();
								cv.put(ListsTable.COL_IS_FIRST_TIME_SYNC, 0);
								ListsTable.UpdateListsTableFieldValues(ListsActivity.this, listID, cv);

								// Insert SQLite first time sync list in to dropbox
								ListsTable.dbxInsert(ListsActivity.this, mDbxDatastore, listID);
								ItemsTable.dbxInsertAllItems(ListsActivity.this, mDbxDatastore, listID);
								// TODO: insert other SQLite tables
							}
						} catch (DbxException e) {
							MyLog.e("Lists_ACTIVITY", "DbxException in ValidateSqlTables, doInBackground.");
							e.printStackTrace();
						}
					} else {
						// The lists table does NOT exist in the dropbox database
						// So the first time sync list does not exist in the dropbox database
						// TODO: place all SQLite first time sync list in to dropbox
						try {
							ListsTable.dbxInsert(ListsActivity.this, mDbxDatastore, listID);
						} catch (DbxException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}

			if (firstTimeSyncLists != null) {
				firstTimeSyncLists.close();
			}

			// TODO: add all tables to the table names list
			String tableNames[] = { ListsTable.TABLE_LISTS, ItemsTable.TABLE_ITEMS };

			for (String tableName : tableNames) {

				if (tableName.equals(ListsTable.TABLE_LISTS)) {
					DbxTable dbxActiveTable = mDbxDatastore.getTable(ListsTable.TABLE_LISTS);
					ListsTable.validateSqlRecords(ListsActivity.this, dbxActiveTable);

				} else if (tableName.equals(ItemsTable.TABLE_ITEMS)) {
					DbxTable dbxActiveTable = mDbxDatastore.getTable(ItemsTable.TABLE_ITEMS);
					ItemsTable.validateSqlRecords(ListsActivity.this, dbxActiveTable);
				}
			}
			AListContentProvider.setSuppressDropboxChanges(false);
			return null;

		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
		}

	}

	@Override
	protected void onPause() {
		MyLog.i("Lists_ACTIVITY", "onPause");

		// save activity state
		SharedPreferences preferences = getSharedPreferences("AList", MODE_PRIVATE);
		SharedPreferences.Editor applicationStates = preferences.edit();
		applicationStates.putLong("ActiveListID", mActiveListID);
		applicationStates.putLong("ActiveItemID", mActiveItemID);
		applicationStates.putInt("ActiveListPosition", mActiveListPosition);
		if (ListsTable.isAnyListSyncedToDropBox(this)) {
			applicationStates.putBoolean("ListsSyncedToDropbox", true);
		} else {
			applicationStates.putBoolean("ListsSyncedToDropbox", false);
		}
		applicationStates.commit();

		if (mDbxDatastore != null) {
			if (mDbxDatastore.getSyncStatus().hasOutgoing) {
				Toast.makeText(this, "Dropbox datastore has OUT_GOING", Toast.LENGTH_LONG).show();
				MyLog.i("MainActivity: onPause()", "Dropbox datastore has outgoing");
			}
			if (mDbxDatastore.getSyncStatus().hasIncoming) {
				Toast.makeText(this, "Dropbox datastore has IN_COMMING", Toast.LENGTH_LONG).show();
				MyLog.i("MainActivity: onPause()", "Dropbox datastore has incomming");
			}
			if (mDbxDatastore.getSyncStatus().isDownloading) {
				Toast.makeText(this, "Dropbox datastore is DOWN_LOADING", Toast.LENGTH_LONG).show();
				MyLog.i("MainActivity: onPause()", "Dropbox datastore is downloading");
			}
			if (mDbxDatastore.getSyncStatus().isUploading) {
				Toast.makeText(this, "Dropbox datastore is UP_LOADING", Toast.LENGTH_LONG).show();
				MyLog.i("MainActivity: onPause()", "Dropbox datastore is uploading");
			}

			mDbxDatastore.removeSyncStatusListener(this);
			// mDbxDatastore.close();
		}
		super.onPause();
	}

	@Override
	protected void onStop() {
		MyLog.i("Lists_ACTIVITY", "onStop");
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		MyLog.i("Lists_ACTIVITY", "onDestroy");
		if (mAllListsCursor != null) {
			mAllListsCursor.close();
		}

		AListContentProvider.setContext(null);
		EventBus.getDefault().unregister(this);
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MyLog.i("Lists_ACTIVITY", "onCreateOptionsMenu");
		getMenuInflater().inflate(R.menu.lists_activity, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		// handle item selection
		switch (item.getItemId()) {
			case R.id.action_removeStruckOffItems:
				ItemsTable.UnStrikeAndDeselectAllStruckOutItems(this, mActiveListID,
						mListSettings.getDeleteNoteUponDeselectingItem());

				return true;

			case R.id.action_addItem:
				StartMasterListActivity();
				return true;

			case R.id.action_DeleteAllDropboxTables:
				DeleteAllDropboxTables();
				return true;

			case R.id.action_newList:
				CreatNewList();
				return true;

			case R.id.action_clearList:
				ItemsTable
						.DeselectAllItemsInList(this, mActiveListID, mListSettings.getDeleteNoteUponDeselectingItem());
				return true;

			case R.id.action_emailList:
				EmailList();
				// Toast.makeText(this, "\"" + item.getTitle() + "\"" + " is under construction.",
				// Toast.LENGTH_SHORT).show();
				return true;

			case R.id.action_editListTitle:
				EditListTitle();
				return true;

			case R.id.action_deleteList:
				DeleteList();
				return true;

			case R.id.action_manageLocations:
				StartManageLocationsActivity();
				return true;

			case R.id.action_uploadStoreLocations:
				Toast.makeText(this, "\"" + item.getTitle() + "\"" + " is under construction.",
						Toast.LENGTH_SHORT).show();
				// UploadStoreLocations();
				return true;

			case R.id.action_refreshStoreLocations:
				Toast.makeText(this, "\"" + item.getTitle() + "\"" + " is under construction.",
						Toast.LENGTH_SHORT).show();
				// RefreshStoreLocations();
				return true;

			case R.id.action_uploadListItems:
				Toast.makeText(this, "\"" + item.getTitle() + "\"" + " is under construction.",
						Toast.LENGTH_SHORT).show();
				// UploadListItems();
				return true;

			case R.id.action_refreshListItems:
				Toast.makeText(this, "\"" + item.getTitle() + "\"" + " is under construction.", Toast.LENGTH_SHORT)
						.show();
				// RefreshListItems();
				return true;

			case R.id.action_Preferences:
				StartListPreferencesActivity();
				return true;

			case R.id.action_about:
				StartAboutActivity();
				return true;

			default:
				return super.onMenuItemSelected(featureId, item);
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (menu != null) {
			boolean showingStore = mListSettings.getShowStores();
			MenuItem action_cloudServices = menu.findItem(R.id.action_cloudServices);
			if (action_cloudServices != null) {
				action_cloudServices.setVisible(showingStore);
			}
		}
		return true;
	}

	private void EmailList() {
		Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

		String listName = ListsTable.getListTitle(this, mActiveListID);
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "AList: " + listName);

		emailIntent.setType("plain/text");
		StringBuilder sb = getList(mActiveListID, mListSettings.getListSortOrder());

		emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, sb.toString());

		startActivity(Intent.createChooser(emailIntent, "Send your email using:"));

	}

	private StringBuilder getList(long listID, int listSortOrder) {
		String sortOrder = "";
		Cursor listCursor = null;
		StringBuilder sb = null;

		try {
			switch (listSortOrder) {
				case AListUtilities.LIST_SORT_ALPHABETICAL:
					sortOrder = ItemsTable.SORT_ORDER_ITEM_NAME;
					listCursor = ItemsTable.getAllSelectedItems(this, mActiveListID, true, sortOrder);
					sb = getListAsString(listCursor);
					break;

				case AListUtilities.LIST_SORT_BY_GROUP:
					listCursor = ItemsTable.getAllSelectedItemsWithGroups(this, mActiveListID, true);
					sb = getListAsStringWithGroups(listCursor);
					break;

				case AListUtilities.LIST_SORT_MANUAL:
					sortOrder = ItemsTable.SORT_ORDER_MANUAL;
					listCursor = ItemsTable.getAllSelectedItems(this, mActiveListID, true, sortOrder);
					sb = getListAsString(listCursor);
					break;

				case AListUtilities.LIST_SORT_BY_STORE_LOCATION:
					mListSettings.RefreshListSettings();
					long selectedStoreID = mListSettings.getActiveStoreID();
					String storeName = StoresTable.getStoreDisplayName(this, selectedStoreID);
					listCursor = ItemsTable
							.getAllSelectedItemsWithLocations(this, mActiveListID, selectedStoreID, true);
					sb = getListAsStringWithLocations(listCursor, storeName);
					break;

				default:
					sortOrder = ItemsTable.SORT_ORDER_ITEM_NAME;
					listCursor = ItemsTable.getAllSelectedItems(this, mActiveListID, true, sortOrder);
					sb = getListAsString(listCursor);
					break;
			}

		} catch (SQLiteException e) {
			MyLog.e("Lists_ACTIVITY: getList SQLiteException: ", e.toString());
			return null;

		} catch (IllegalArgumentException e) {
			MyLog.e("Lists_ACTIVITY: getList IllegalArgumentException: ", e.toString());
			return null;

		} finally {
			if (listCursor != null) {
				listCursor.close();
			}
		}
		if (listCursor != null) {
			listCursor.close();
		}
		return sb;
	}

	private StringBuilder getListAsStringWithLocations(Cursor listCursor, String storeName) {
		StringBuilder sb = new StringBuilder();
		if (listCursor != null) {

			sb.append("List sorted for:").append(System.getProperty("line.separator"));
			sb.append(storeName).append(System.getProperty("line.separator"));
			sb.append(System.getProperty("line.separator"));

			String locationName = "";
			String previousLocationName = "";
			String itemName = "";

			listCursor.moveToFirst();
			locationName = listCursor.getString(listCursor.getColumnIndexOrThrow(LocationsTable.COL_LOCATION_NAME));
			sb.append(locationName).append(System.getProperty("line.separator"));
			previousLocationName = locationName;
			itemName = listCursor.getString(listCursor.getColumnIndexOrThrow(ItemsTable.COL_ITEM_NAME));
			sb.append("   ").append(itemName).append(System.getProperty("line.separator"));

			while (listCursor.moveToNext()) {
				locationName = listCursor.getString(listCursor.getColumnIndexOrThrow(LocationsTable.COL_LOCATION_NAME));
				if (!locationName.equals(previousLocationName)) {
					sb.append(System.getProperty("line.separator"));
					sb.append(locationName).append(System.getProperty("line.separator"));
					previousLocationName = locationName;
				}
				itemName = listCursor.getString(listCursor.getColumnIndexOrThrow(ItemsTable.COL_ITEM_NAME));
				sb.append("   ").append(itemName).append(System.getProperty("line.separator"));
			}
		}
		return sb;
	}

	private StringBuilder getListAsStringWithGroups(Cursor listCursor) {
		StringBuilder sb = new StringBuilder();
		if (listCursor != null) {

			String groupTitle = "";
			String previousGroupTitle = "";
			String itemName = "";

			listCursor.moveToFirst();
			groupTitle = listCursor.getString(listCursor.getColumnIndexOrThrow(GroupsTable.COL_GROUP_NAME));
			sb.append(groupTitle).append(System.getProperty("line.separator"));
			previousGroupTitle = groupTitle;
			itemName = listCursor.getString(listCursor.getColumnIndexOrThrow(ItemsTable.COL_ITEM_NAME));
			sb.append("   ").append(itemName).append(System.getProperty("line.separator"));

			while (listCursor.moveToNext()) {
				groupTitle = listCursor.getString(listCursor.getColumnIndexOrThrow(GroupsTable.COL_GROUP_NAME));
				if (!groupTitle.equals(previousGroupTitle)) {
					sb.append(System.getProperty("line.separator"));
					sb.append(groupTitle).append(System.getProperty("line.separator"));
					previousGroupTitle = groupTitle;
				}
				itemName = listCursor.getString(listCursor.getColumnIndexOrThrow(ItemsTable.COL_ITEM_NAME));
				sb.append("   ").append(itemName).append(System.getProperty("line.separator"));
			}
		}
		return sb;
	}

	private StringBuilder getListAsString(Cursor listCursor) {
		StringBuilder sb = new StringBuilder();
		if (listCursor != null) {
			listCursor.moveToPosition(-1);
			String itemName = "";
			while (listCursor.moveToNext()) {
				itemName = listCursor.getString(listCursor.getColumnIndexOrThrow(ItemsTable.COL_ITEM_NAME));
				sb.append(itemName).append(System.getProperty("line.separator"));
			}
		}
		return sb;
	}

	private void DeleteList() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		// set title
		builder.setTitle("Delete List");

		String msg = "Permanently delete " + "\"" + mListSettings.getListTitle() + "\" ?";
		// set dialog message
		builder
				.setMessage(msg)
				.setCancelable(false)
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int id) {
						ListsTable.DeleteList(ListsActivity.this, mActiveListID);
						ReStartListsActivity(mActiveListPosition);
						finish();
					}
				})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int id) {
						// if this button is clicked, just close
						// the dialog box and do nothing
						dialog.cancel();
					}
				});

		// create alert dialog
		AlertDialog alertDialog = builder.create();

		// show it
		alertDialog.show();
	}

	private void CreatNewList() {
		MyLog.i("Lists_ACTIVITY", "CreatNewList");
		FragmentManager fm = this.getSupportFragmentManager();
		// Remove any currently showing dialog
		Fragment prev = fm.findFragmentByTag("dialog_lists_table_update");
		if (prev != null) {
			FragmentTransaction ft = fm.beginTransaction();
			ft.remove(prev);
			ft.commit();
		}

		ListsDialogFragment editListTitleDialog = ListsDialogFragment.newInstance(mActiveListID,
				ListsDialogFragment.NEW_LIST);
		editListTitleDialog.show(fm, "dialog_lists_table_update");
	}

	private void EditListTitle() {

		FragmentManager fm = this.getSupportFragmentManager();
		// Remove any currently showing dialog
		Fragment prev = fm.findFragmentByTag("dialog_lists_table_update");
		if (prev != null) {
			FragmentTransaction ft = fm.beginTransaction();
			ft.remove(prev);
			ft.commit();
		}

		ListsDialogFragment editListTitleDialog = ListsDialogFragment
				.newInstance(mActiveListID, ListsDialogFragment.EDIT_LIST_TITLE);
		editListTitleDialog.show(fm, "dialog_lists_table_update");
	}

	/*private void UploadStoreLocations() {

		Cursor storeCursor = StoresTable.getStore(this, mActiveStoreID);
		Cursor groupLocationsCursor = GroupsTable.getCursorAllGroupsInListIncludeLocations(this, mActiveListID,
				mActiveStoreID);
		Cursor listCursor = ListsTable.getList(this, mActiveListID);

		StoreDataSubmission storeData = new StoreDataSubmission(this, "Loren", "Baker", listCursor, storeCursor,
				groupLocationsCursor);
		String xmlString = storeData.getXml();
		// write xmlString to file to disk
		ReadWriteFile.Write(FILENAME, xmlString);

		if (storeCursor != null) {
			storeCursor.close();
		}

		if (groupLocationsCursor != null) {
			groupLocationsCursor.close();
		}

		if (listCursor != null) {
			listCursor.close();
		}

		// read file back from disk
		String result = ReadWriteFile.Read(FILENAME);

		if (result.length() > 0) {
			if (result.equals(xmlString)) {
				MyLog.i("Lists_ACTIVITY", "UploadStoreLocations: xmlString and result are equal");
				ReadWriteFile.sendEmail(this, FILENAME);
			} else {
				MyLog.e("Lists_ACTIVITY", "UploadStoreLocations: xmlString and result are NOT equal");
				ReadWriteFile.sendEmail(this, FILENAME);
			}
		} else {
			MyLog.e("Lists_ACTIVITY", "UploadStoreLocations:File lenght is ZERO");
		}
	}*/

	/*private void RefreshStoreLocations() {
		StoreGoupLocations groupLocations = null;
		File file = ReadWriteFile.getFile(FILENAME);
		if (ReadWriteFile.isExternalStorageReadable()) {

			InputStream in = null;
			try {
				in = new BufferedInputStream(new FileInputStream(file));
				groupLocations = StoreDataParser.parse(in);
				if (groupLocations != null) {
					UpdateStoreGroupLocations(groupLocations);
				}

			} catch (XmlPullParserException e) {
				MyLog.e("Lists_ACTIVITY", "XmlPullParserException in UpdateStoreLocations.");
				e.printStackTrace();

			} catch (FileNotFoundException e) {
				MyLog.e("Lists_ACTIVITY", "FileNotFoundException in UpdateStoreLocations.");
				e.printStackTrace();

			} catch (IOException e) {
				MyLog.e("Lists_ACTIVITY", "IOException in UpdateStoreLocations.");
				e.printStackTrace();

			} finally { // Will execute despite any exception
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						MyLog.e("Lists_ACTIVITY", "IOException in finally-UpdateStoreLocations.");
						e.printStackTrace();
					}
				}
			}
		}
	}*/

	/*private void UploadListItems() {
		Cursor listItems = ItemsTable.getAllItemsWithGroups(this, mActiveListID);
		Cursor listCursor = ListsTable.getList(this, mActiveListID);

		ItemsSubmission itemData = new ItemsSubmission(this, "Loren", "Baker", listCursor, listItems);
		String xmlString = itemData.getXml();
		// write xmlString to file to disk
		ReadWriteFile.Write(FILENAME, xmlString);

		if (listItems != null) {
			listItems.close();
		}

		if (listCursor != null) {
			listCursor.close();
		}

		// read file back from disk
		String result = ReadWriteFile.Read(FILENAME);

		if (result.length() > 0) {
			if (result.equals(xmlString)) {
				MyLog.i("Lists_ACTIVITY", "UploadStoreLocations: xmlString and result are equal");
				ReadWriteFile.sendEmail(this, FILENAME);
			} else {
				MyLog.e("Lists_ACTIVITY", "UploadStoreLocations: xmlString and result are NOT equal");
				ReadWriteFile.sendEmail(this, FILENAME);
			}
		} else {
			MyLog.e("Lists_ACTIVITY", "UploadStoreLocations:File lenght is ZERO");
		}
	}*/

	/*private void UpdateStoreGroupLocations(StoreGoupLocations storeGroupLocations) {
		long listID = storeGroupLocations.getLocalListID();
		long storeID = storeGroupLocations.getLocalStoreID();
		ArrayList<GroupLocation> groupLocations = storeGroupLocations.getGroupLocations();
		for (GroupLocation groupLocation : groupLocations) {
			long groupID = GroupsTable.CreateNewGroup(this, listID, groupLocation.getGroupName());
			long loctionID = LocationsTable.CreateNewLocation(this, groupLocation.getStoreLocation());
			if (listID > 1 && storeID > 1 && groupID > 1 && loctionID > 1) {
				long bridgeRowID = BridgeTable.CreateNewBridgeRow(this, listID, storeID, groupID, loctionID);
				if (bridgeRowID < 1) {
					MyLog.e("Lists_ACTIVITY",
							"ERROR:UpdateStoreGroupLocations. Failed to create new, or update existing, Bridge row!");
				}
			} else {
				MyLog.e("Lists_ACTIVITY",
						"ERROR:UpdateStoreGroupLocations. Invlaid row. One of the row parameters is less than 1!");
			}
		}
	}*/

}
