package com.lbconsulting.alist.ui.activities;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.dropbox.sync.android.DbxDatastore;
import com.lbconsulting.alist.R;
import com.lbconsulting.alist.adapters.ManageItemsPagerAdapter;
import com.lbconsulting.alist.classes.AListEvents.ListTargetSelected;
import com.lbconsulting.alist.classes.AListEvents.ManageItemsActiveGroupChanged;
import com.lbconsulting.alist.classes.AListEvents.ManageItemsTabPostionChange;
import com.lbconsulting.alist.classes.ListSettings;
import com.lbconsulting.alist.database.AListContentProvider;
import com.lbconsulting.alist.database.ItemsTable;
import com.lbconsulting.alist.database.ListsTable;
import com.lbconsulting.alist.dialogs.GroupsDialogFragment;
import com.lbconsulting.alist.dialogs.MoveCheckedItemsDialogFragment;
import com.lbconsulting.alist.utilities.MyLog;

import de.greenrobot.event.EventBus;

public class ManageItemsActivity extends FragmentActivity implements DbxDatastore.SyncStatusListener {

	private DbxDatastore mDbxDatastore = null;

	private ManageItemsPagerAdapter mCheckItemsPagerAdapter;
	private ViewPager mPager;

	private long mActiveListID = -1;
	private int mActiveListPosition = -1;
	private long mActiveGroupID = -1;
	private ListSettings mListSettings;
	private Cursor mAllListsCursor;

	private long mSelectedListID = -1;

	private int mActiveTabPosition = -1;

	private boolean isTAB_MoveORCullItemsSelected = true;
	private Menu mCheckItemsMenu;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		MyLog.i("ManageItems_ACTIVITY", "onCreate");
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_check_items_pager);

		EventBus.getDefault().register(this);

		SharedPreferences storedStates = getSharedPreferences("AList", MODE_PRIVATE);
		mActiveListID = storedStates.getLong("ActiveListID", -1);
		mActiveListPosition = storedStates.getInt("ActiveListPosition", -1);
		mActiveTabPosition = storedStates.getInt("ActiveTabPosition", -1);

		final ActionBar actionBar = getActionBar();
		actionBar.setTitle(R.string.action_bar_title_manage_items);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// add a tabs to the action bar.
		actionBar.addTab(actionBar.newTab()
				.setText(R.string.actionBar_tab_cull_or_move_items)
				.setTabListener(new TabListener() {

					@Override
					public void onTabReselected(Tab tab, android.app.FragmentTransaction ft) {
						// Do nothing
					}

					@Override
					public void onTabSelected(Tab tab, android.app.FragmentTransaction ft) {
						mActiveTabPosition = tab.getPosition();
						EventBus.getDefault().post(new ManageItemsTabPostionChange(mActiveListID, mActiveTabPosition));

						SharedPreferences preferences = getSharedPreferences("AList", MODE_PRIVATE);
						SharedPreferences.Editor applicationStates = preferences.edit();
						applicationStates.putInt("ActiveTabPosition", mActiveTabPosition);
						applicationStates.commit();

						onPrepareOptionsMenu(mCheckItemsMenu);
					}

					@Override
					public void onTabUnselected(Tab tab, android.app.FragmentTransaction ft) {
						// Do nothing
					}
				})
				);
		actionBar.addTab(actionBar.newTab()
				.setText(R.string.actionBar_tab_set_groups)
				.setTabListener(new TabListener() {

					@Override
					public void onTabReselected(Tab tab, android.app.FragmentTransaction ft) {
						// Do nothing
					}

					@Override
					public void onTabSelected(Tab tab, android.app.FragmentTransaction ft) {
						mActiveTabPosition = tab.getPosition();
						EventBus.getDefault().post(new ManageItemsTabPostionChange(mActiveListID, mActiveTabPosition));

						SharedPreferences preferences = getSharedPreferences("AList", MODE_PRIVATE);
						SharedPreferences.Editor applicationStates = preferences.edit();
						applicationStates.putInt("ActiveTabPosition", mActiveTabPosition);
						applicationStates.commit();

						onPrepareOptionsMenu(mCheckItemsMenu);
					}

					@Override
					public void onTabUnselected(Tab tab, android.app.FragmentTransaction ft) {
						// Do nothing
					}

				})
				);

		mAllListsCursor = ListsTable.getAllLists(this);
		mListSettings = new ListSettings(this, mActiveListID);
		mActiveGroupID = mListSettings.getManageItemsGroupID();

		mCheckItemsPagerAdapter = new ManageItemsPagerAdapter(getSupportFragmentManager(), this);
		mPager = (ViewPager) findViewById(R.id.checkItemsPager);
		mPager.setAdapter(mCheckItemsPagerAdapter);
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
				MyLog.d("ManageItems_ACTIVITY", "onPageSelected() - position = " + position + " ; listID = "
						+ mActiveListID);
			}
		});
	}

	public void onEvent(ManageItemsActiveGroupChanged event) {
		long listID = event.getListID();
		if (listID == mActiveListID) {
			mActiveGroupID = event.getActiveGroupID();
			mListSettings.RefreshListSettings();
		}
	}

	public void onEvent(ListTargetSelected event) {

		// the new list ID has been selected ...
		mSelectedListID = event.getSelectedListID();
		int numberOfItemsMoved = ItemsTable.MoveAllCheckedItemsInList(ManageItemsActivity.this,
				mActiveListID, mSelectedListID);

		AlertDialog.Builder builder = new AlertDialog.Builder(ManageItemsActivity.this);
		// set title
		Resources res = getResources();
		String numberOfCheckedItemsMoved = res.getQuantityString(R.plurals.numberOfCheckedItems,
				numberOfItemsMoved, numberOfItemsMoved);
		StringBuilder sb = new StringBuilder();
		sb.append("Successfully moved  ");
		sb.append(numberOfCheckedItemsMoved);
		sb.append(".");
		builder.setTitle(sb.toString());
		builder.setPositiveButton(R.string.btn_ok_text, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// close the dialog box and do nothing
				dialog.cancel();
			}
		});

		// create alert dialog
		AlertDialog alertDialog = builder.create();
		// show it
		alertDialog.show();
	}

	@Override
	protected void onStart() {
		MyLog.i("ManageItems_ACTIVITY", "onStart");
		super.onStart();
	}

	@Override
	protected void onRestart() {
		MyLog.i("ManageItems_ACTIVITY", "onRestart");
		super.onRestart();
	}

	@Override
	protected void onResume() {
		MyLog.i("ManageItems_ACTIVITY", "onResume");
		SharedPreferences storedStates = getSharedPreferences("AList", MODE_PRIVATE);
		mActiveListID = storedStates.getLong("ActiveListID", -1);
		mActiveListPosition = storedStates.getInt("ActiveListPosition", -1);
		mActiveTabPosition = storedStates.getInt("ActiveTabPosition", -1);

		AListContentProvider.setContext(this);
		if (mDbxDatastore == null) {
			mDbxDatastore = AListContentProvider.getDbxDatastore();
		}
		if (mDbxDatastore != null) {
			mDbxDatastore.addSyncStatusListener(this);
		}

		if (mActiveListPosition > -1) {
			mPager.setCurrentItem(mActiveListPosition);
		}

		getActionBar().setSelectedNavigationItem(mActiveTabPosition);
		EventBus.getDefault().post(new ManageItemsTabPostionChange(mActiveListID, mActiveTabPosition));

		super.onResume();
	}

	@Override
	protected void onPause() {
		MyLog.i("ManageItems_ACTIVITY", "onPause");
		SharedPreferences preferences = getSharedPreferences("AList", MODE_PRIVATE);
		SharedPreferences.Editor applicationStates = preferences.edit();
		applicationStates.putLong("ActiveListID", mActiveListID);
		applicationStates.putInt("ActiveListPosition", mActiveListPosition);
		applicationStates.putInt("ActiveTabPosition", mActiveTabPosition);

		applicationStates.commit();

		if (mDbxDatastore != null) {
			mDbxDatastore.removeSyncStatusListener(this);
		}
		super.onPause();
	}

	@Override
	protected void onStop() {
		MyLog.i("ManageItems_ACTIVITY", "onStop");
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.manage_items_activity, menu);
		mCheckItemsMenu = menu;
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		// handle item selection
		switch (item.getItemId()) {
			case R.id.action_deleteCheckedItems:
				DeleteCheckedItems();
				return true;

			case R.id.action_clearAllCheckedItems:
				ClearAllCheckedItems();
				return true;

			case R.id.action_moveCheckedItmes:
				MoveCheckedItems();
				// Toast.makeText(this, "\"" + item.getTitle() + "\"" + " is under construction.",
				// Toast.LENGTH_SHORT).show();
				return true;

			case R.id.action_checkUnused90:
				CheckUnused(90);
				return true;

			case R.id.action_checkUnused180:
				CheckUnused(180);
				return true;

			case R.id.action_checkUnused365:
				CheckUnused(365);
				return true;

			case R.id.action_editGroupName:
				// Toast.makeText(this, "\"" + item.getTitle() + "\"" + " is under construction.",
				// Toast.LENGTH_SHORT).show();
				EditGroupName();
				return true;

			case R.id.action_addNewGroup:
				// Toast.makeText(this, "\"" + item.getTitle() + "\"" + " is under construction.",
				// Toast.LENGTH_SHORT).show();
				AddNewGroup();
				return true;

			case R.id.action_deleteGroup:
				// Toast.makeText(this, "\"" + item.getTitle() + "\"" + " is under construction.",
				// Toast.LENGTH_SHORT).show();
				DeleteGroup();
				return true;

			case R.id.action_sortOrder:
				// ChangeSortOrder();
				Toast.makeText(this, "\"" + item.getTitle() + "\"" + " is under construction.", Toast.LENGTH_SHORT)
						.show();
				return true;

			default:
				return super.onMenuItemSelected(featureId, item);
		}
	}

	private void AddNewGroup() {
		FragmentManager fm = this.getSupportFragmentManager();
		// Remove any currently showing dialog
		Fragment prev = fm.findFragmentByTag("dialog_group_create_edit");
		if (prev != null) {
			FragmentTransaction ft = fm.beginTransaction();
			ft.remove(prev);
			ft.commit();
		}
		GroupsDialogFragment addNewGroupDialog = GroupsDialogFragment.newInstance(mActiveListID, mActiveGroupID,
				GroupsDialogFragment.NEW_GROUP);
		addNewGroupDialog.show(fm, "dialog_group_create_edit");
	}

	private void DeleteGroup() {
		FragmentManager fm = this.getSupportFragmentManager();
		// Remove any currently showing dialog
		Fragment prev = fm.findFragmentByTag("dialog_group_create_edit");
		if (prev != null) {
			FragmentTransaction ft = fm.beginTransaction();
			ft.remove(prev);
			ft.commit();
		}

		// SendGroupIDRequest();
		if (mActiveGroupID > 1) {
			// can't delete the default group
			GroupsDialogFragment deleteGroupDialog = GroupsDialogFragment.newInstance(mActiveListID, mActiveGroupID,
					GroupsDialogFragment.DELETE_GROUP);
			deleteGroupDialog.show(fm, "dialog_group_create_edit");
		}
	}

	private void EditGroupName() {
		FragmentManager fm = this.getSupportFragmentManager();
		// Remove any currently showing dialog
		Fragment prev = fm.findFragmentByTag("dialog_group_create_edit");
		if (prev != null) {
			FragmentTransaction ft = fm.beginTransaction();
			ft.remove(prev);
			ft.commit();
		}

		// SendGroupIDRequest();
		if (mActiveGroupID > 1) {
			// can't edit the default group
			GroupsDialogFragment editGroupNameDialog = GroupsDialogFragment.newInstance(mActiveListID, mActiveGroupID,
					GroupsDialogFragment.EDIT_GROUP_NAME);
			editGroupNameDialog.show(fm, "dialog_group_create_edit");
		}
	}

	private void SetActiveListID(int position) {
		if (mAllListsCursor != null) {
			try {
				mAllListsCursor.moveToPosition(position);
				mActiveListID = mAllListsCursor.getLong(mAllListsCursor.getColumnIndexOrThrow(ListsTable.COL_LIST_ID));
				mListSettings = new ListSettings(this, mActiveListID);
				mActiveListPosition = position;
			} catch (Exception e) {
				MyLog.d("ManageItems_ACTIVITY", "Exception in SetActiveListID: " + e);
			}
		}
	}

	private void DeleteCheckedItems() {
		int numberOfCheckedItems = ItemsTable.getNumberOfCheckedItmes(this, mActiveListID);

		if (numberOfCheckedItems > 0) {
			Resources res = getResources();
			String numberOfCheckedItemsFound = res.getQuantityString(R.plurals.numberOfCheckedItems,
					numberOfCheckedItems, numberOfCheckedItems);
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			// set title
			builder.setTitle(R.string.dialog_title_delete_all_checked_items);

			String msg = "Permanently delete " + numberOfCheckedItemsFound + "?";
			builder
					.setMessage(msg)
					.setCancelable(false)
					.setPositiveButton(R.string.btn_yes_text, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int id) {
							// delete all checked items
							ItemsTable.DeleteAllCheckedItemsInList(ManageItemsActivity.this, mActiveListID);
						}
					})
					.setNegativeButton(R.string.btn_no_text, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int id) {
							// close the dialog box and do nothing
							dialog.cancel();
						}
					});

			// create alert dialog
			AlertDialog alertDialog = builder.create();
			// show it
			alertDialog.show();
		} else {
			AlertDialog.Builder builder = new AlertDialog.Builder(ManageItemsActivity.this);
			// set title and message
			builder.setTitle("Unable to delete items.");
			builder.setMessage("No checked items available!");
			builder.setPositiveButton(R.string.btn_ok_text, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// close the dialog box and do nothing
					dialog.cancel();
				}
			});

			// create alert dialog
			AlertDialog alertDialog = builder.create();
			// show it
			alertDialog.show();

		}

	}

	private void ClearAllCheckedItems() {
		ItemsTable.UnCheckAllItemsInList(ManageItemsActivity.this, mActiveListID);
	}

	private void MoveCheckedItems() {
		int numberOfLists = ListsTable.getNumberOfLists(this);
		if (numberOfLists > 1) {
			FragmentManager fm = getSupportFragmentManager();
			Fragment prev = fm.findFragmentByTag("dialog_move_checked_items");
			if (prev != null) {
				FragmentTransaction ft = fm.beginTransaction();
				ft.remove(prev);
				ft.commit();
			}
			int numberOfCheckedItems = ItemsTable.getNumberOfCheckedItmes(this, mActiveListID);
			if (numberOfCheckedItems > 0) {
				MoveCheckedItemsDialogFragment moveCheckedItemsDialog =
						MoveCheckedItemsDialogFragment.newInstance(mActiveListID, numberOfCheckedItems);
				moveCheckedItemsDialog.show(fm, "dialog_move_checked_items");
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(ManageItemsActivity.this);
				// set title and message
				builder.setTitle("Unable to move items.");
				builder.setMessage("No checked items available!");
				builder.setPositiveButton(R.string.btn_ok_text, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// close the dialog box and do nothing
						dialog.cancel();
					}
				});

				// create alert dialog
				AlertDialog alertDialog = builder.create();
				// show it
				alertDialog.show();
			}
		} else {
			AlertDialog.Builder builder = new AlertDialog.Builder(ManageItemsActivity.this);
			// set title and message
			builder.setTitle("Unable to move items.");
			builder.setMessage("No target list available. There must be more than one list in the database before you can move items.");
			builder.setPositiveButton(R.string.btn_ok_text, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// close the dialog box and do nothing
					dialog.cancel();
				}
			});

			// create alert dialog
			AlertDialog alertDialog = builder.create();
			// show it
			alertDialog.show();
		}
	}

	private void CheckUnused(long numberOfDays) {
		ItemsTable.CheckItemsUnused(this, mActiveListID, numberOfDays);
	}

	@Override
	protected void onDestroy() {
		MyLog.i("ManageItems_ACTIVITY", "onDestroy");
		if (mAllListsCursor != null) {
			mAllListsCursor.close();
		}

		// AListContentProvider.setContext(null);
		EventBus.getDefault().unregister(this);
		super.onDestroy();
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		isTAB_MoveORCullItemsSelected = mActiveTabPosition == 0;
		if (menu != null) {
			MenuItem action_deleteCheckedItems = menu.findItem(R.id.action_deleteCheckedItems);
			MenuItem action_moveCheckedItmes = menu.findItem(R.id.action_moveCheckedItmes);
			MenuItem action_checkUnused90 = menu.findItem(R.id.action_checkUnused90);
			MenuItem action_checkUnused180 = menu.findItem(R.id.action_checkUnused180);
			MenuItem action_checkUnused365 = menu.findItem(R.id.action_checkUnused365);

			action_deleteCheckedItems.setVisible(isTAB_MoveORCullItemsSelected);
			action_moveCheckedItmes.setVisible(isTAB_MoveORCullItemsSelected);
			action_checkUnused90.setVisible(isTAB_MoveORCullItemsSelected);
			action_checkUnused180.setVisible(isTAB_MoveORCullItemsSelected);
			action_checkUnused365.setVisible(isTAB_MoveORCullItemsSelected);

			MenuItem action_editGroupName = menu.findItem(R.id.action_editGroupName);
			MenuItem action_addNewGroup = menu.findItem(R.id.action_addNewGroup);
			MenuItem action_deleteGroup = menu.findItem(R.id.action_deleteGroup);
			if (mListSettings.isGroupAdditonAllowed()) {
				action_editGroupName.setVisible(!isTAB_MoveORCullItemsSelected);
				action_addNewGroup.setVisible(!isTAB_MoveORCullItemsSelected);
				action_deleteGroup.setVisible(!isTAB_MoveORCullItemsSelected);
			} else {
				// not allowed to edit groups
				action_editGroupName.setVisible(false);
				action_addNewGroup.setVisible(!false);
				action_deleteGroup.setVisible(!false);
			}

		}
		return true;
	}

	@Override
	public void onDatastoreStatusChange(DbxDatastore store) {
		AListContentProvider.setDbxDatastore(store);
		if (store.getSyncStatus().hasIncoming) {
			AListContentProvider.onDatastoreStatusChange(store);
		}
	}
}
