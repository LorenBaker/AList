package com.lbconsulting.alist.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lbconsulting.alist.R;
import com.lbconsulting.alist.classes.AListEvents.ApplyPresetColors;
import com.lbconsulting.alist.classes.DynamicListView;
import com.lbconsulting.alist.classes.ListSettings;
import com.lbconsulting.alist.database.BridgeTable;
import com.lbconsulting.alist.database.GroupsTable;
import com.lbconsulting.alist.database.ItemsTable;
import com.lbconsulting.alist.database.LocationsTable;
import com.lbconsulting.alist.utilities.MyLog;

import de.greenrobot.event.EventBus;

public class ItemsCursorAdaptor extends CursorAdapter implements DynamicListView.SwappableListAdapter {

	private ListSettings mListSettings;
	private final int INVALID_ID = -1;
	// private BroadcastReceiver mListColorsChanged;

	private Context mContext1;

	public ItemsCursorAdaptor(Context context1, Cursor c, int flags, ListSettings listSettings) {
		super(context1, c, flags);
		this.mListSettings = listSettings;
		this.mContext1 = context1;
		MyLog.i("ItemsCursorAdaptor", "ItemsCursorAdaptor constructor.");
		EventBus.getDefault().register(this);

		/*		mListColorsChanged = new BroadcastReceiver() {

					@Override
					public void onReceive(Context context2, Intent intent) {
						mListSettings.RefreshListSettings();
					}
				};*/
		// Register to receive messages.
		/*		String key = String.valueOf(mListSettings.getListID())
						+ ListColorsPreviewFragment.APPLY_PRESET_COLORS_BROADCAST_KEY;
				LocalBroadcastManager.getInstance(context1).registerReceiver(mListColorsChanged, new IntentFilter(key));*/
	}

	public void onEvent(ApplyPresetColors event) {
		if (mListSettings.getListID() == event.getListID()) {
			mListSettings.RefreshListSettings();
		}
	}

	@Override
	protected void finalize() throws Throwable {
		EventBus.getDefault().unregister(this);
		// LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mListColorsChanged);
		super.finalize();
	}

	private boolean ShowGroupSeparator(TextView tv, Cursor listCursor) {
		boolean result = false;
		long currentGroupID = listCursor.getLong(listCursor.getColumnIndexOrThrow(ItemsTable.COL_GROUP_ID));
		long previousGroupID = -1;
		if (listCursor.moveToPrevious()) {
			previousGroupID = listCursor.getLong(listCursor.getColumnIndexOrThrow(ItemsTable.COL_GROUP_ID));
			listCursor.moveToNext();
			if (currentGroupID == previousGroupID) {
				tv.setVisibility(View.GONE);
				result = false;
			} else {
				tv.setVisibility(View.VISIBLE);
				result = true;
			}
		} else {
			tv.setVisibility(View.VISIBLE);
			listCursor.moveToFirst();
			result = true;
		}
		return result;
	}

	private boolean ShowLocationSeparator(TextView tv, Cursor listCursor) {
		boolean result = false;
		long currentLocationID = listCursor.getLong(listCursor.getColumnIndexOrThrow(BridgeTable.COL_LOCATION_ID));
		long previousLocationID = -1;
		if (listCursor.moveToPrevious()) {
			previousLocationID = listCursor.getLong(listCursor.getColumnIndexOrThrow(BridgeTable.COL_LOCATION_ID));
			listCursor.moveToNext();
			if (currentLocationID == previousLocationID) {
				tv.setVisibility(View.GONE);
				result = false;
			} else {
				tv.setVisibility(View.VISIBLE);
				result = true;
			}
		} else {
			tv.setVisibility(View.VISIBLE);
			listCursor.moveToFirst();
			result = true;
		}
		return result;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		if (cursor != null) {
			if (isVisible(cursor)) {
				view.setVisibility(View.VISIBLE);

				/*				View parent = (View) view.getParent();
								if (parent != null) {
									// if (parent.getFirstVisiblePosition() == cursor.getPosition()) {
									String text = cursor.getString(cursor.getColumnIndexOrThrow(LocationsTable.COL_LOCATION_NAME));
									text = text + " ";
									// }
								}*/

				TextView tvListItemSeparator = (TextView) view.findViewById(R.id.tvListItemSeparator);
				if (tvListItemSeparator != null) {
					// if (isVisible(cursor)) {
					if (mListSettings.getShowGroupsInListsFragment()) {
						if (ShowGroupSeparator(tvListItemSeparator, cursor)) {
							try {
								tvListItemSeparator.setText(cursor.getString(cursor
										.getColumnIndexOrThrow(GroupsTable.COL_GROUP_NAME)));
								tvListItemSeparator
										.setBackgroundColor(this.mListSettings.getSeparatorBackgroundColor());
								tvListItemSeparator.setTextColor(this.mListSettings.getSeparatorTextColor());

							} catch (IllegalArgumentException e) {
								MyLog.e("IllegalArgumentException error in ItemsCursorAdaptor:bindView: getShowGroupsInListsFragment: list:"
										+ mListSettings.getListTitle(), e.toString());
							}
						}
					} else if (mListSettings.getShowStores()) {
						if (ShowLocationSeparator(tvListItemSeparator, cursor)) {
							try {
								tvListItemSeparator.setText(cursor.getString(cursor
										.getColumnIndexOrThrow(LocationsTable.COL_LOCATION_NAME)));
								tvListItemSeparator
										.setBackgroundColor(this.mListSettings.getSeparatorBackgroundColor());
								tvListItemSeparator.setTextColor(this.mListSettings.getSeparatorTextColor());
							} catch (IllegalArgumentException e) {
								MyLog.e("IllegalArgumentException error in ItemsCursorAdaptor:bindView: getShowStores: list:"
										+ mListSettings.getListTitle(), e.toString());
							}
						}
					} else {
						tvListItemSeparator.setVisibility(View.GONE);
					}
					/*				} else {
										tvListItemSeparator.setVisibility(View.INVISIBLE);
									}*/
				}

				int isStruckOut = cursor.getInt(cursor.getColumnIndexOrThrow(ItemsTable.COL_STRUCK_OUT));
				// int isStruckOut = cursor.getInt(cursor.getColumnIndex(ItemsTable.COL_STRUCK_OUT));
				TextView tvListItemName = (TextView) view.findViewById(R.id.tvListItemName);
				if (tvListItemName != null) {
					// if (isVisible(cursor)) {
					tvListItemName.setText(cursor.getString(cursor.getColumnIndexOrThrow(ItemsTable.COL_ITEM_NAME)));

					if (isStruckOut > 0) {
						// item has been struck out
						tvListItemName.setTypeface(null, Typeface.ITALIC);
						tvListItemName.setTextColor(this.mListSettings.getItemStrikeoutTextColor());
						tvListItemName.setPaintFlags(tvListItemName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
					} else {
						// item is NOT struck out
						tvListItemName.setTypeface(null, Typeface.NORMAL);
						tvListItemName.setTextColor(this.mListSettings.getItemNormalTextColor());
						tvListItemName.setPaintFlags(tvListItemName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
					}
					/*				} else {
										tvListItemName.setVisibility(View.INVISIBLE);
									}*/
				}

				TextView tvListItemNote = (TextView) view.findViewById(R.id.tvListItemNote);
				if (tvListItemNote != null) {
					// if (isVisible(cursor)) {
					// if a note exists ... then show it
					String note = cursor.getString(cursor.getColumnIndexOrThrow(ItemsTable.COL_ITEM_NOTE));
					if (note != null && !note.isEmpty()) {
						StringBuilder sb = new StringBuilder();
						sb.append("(");
						sb.append(cursor.getString(cursor.getColumnIndexOrThrow(ItemsTable.COL_ITEM_NOTE)));
						sb.append(")");
						tvListItemNote.setText(sb.toString());
						if (isStruckOut > 0) {
							// item has been struck out
							tvListItemNote.setTypeface(null, Typeface.ITALIC);
							tvListItemNote.setTextColor(this.mListSettings.getItemStrikeoutTextColor());
							tvListItemNote.setPaintFlags(tvListItemNote.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
						} else {
							// item is NOT struck out
							tvListItemNote.setTypeface(null, Typeface.NORMAL);
							tvListItemNote.setTextColor(this.mListSettings.getItemNormalTextColor());
							tvListItemNote.setPaintFlags(tvListItemNote.getPaintFlags()
									& (~Paint.STRIKE_THRU_TEXT_FLAG));
						}
						tvListItemNote.setVisibility(View.VISIBLE);
					} else {
						// no note exists ...
						tvListItemNote.setVisibility(View.GONE);
					}
					/*} else {
						tvListItemNote.setVisibility(View.INVISIBLE);
					}*/
				}
			} else {
				view.setVisibility(View.INVISIBLE);
			}
		}
	}

	private boolean isVisible(Cursor itemCursor) {
		boolean result = false;
		int switchValue = itemCursor.getInt(itemCursor.getColumnIndexOrThrow(ItemsTable.COL_MANUAL_SORT_SWITCH));
		if (switchValue > 0) {
			result = true;
		}
		return result;
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.row_list_items, parent, false);
		return view;
	}

	@SuppressWarnings("resource")
	@Override
	public long getItemId(int position) {
		long itemID = INVALID_ID;
		Cursor cursor = getCursor();
		if (cursor != null) {
			if (position > -1 && position < cursor.getCount()) {
				int currentCursorPosition = cursor.getPosition();
				cursor.moveToPosition(position);
				itemID = cursor.getLong(cursor.getColumnIndexOrThrow(ItemsTable.COL_ITEM_ID));
				cursor.moveToPosition(currentCursorPosition);
			}
			// cursor.close(); DON'T CLOSE THE CURSOR
		}
		return itemID;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public void swap(long mobileItemID, long switchItemID, long previousSwitchItemID) {
		ItemsTable.SwapManualSortOrder(mContext1, mobileItemID, switchItemID, previousSwitchItemID);
	}

}
