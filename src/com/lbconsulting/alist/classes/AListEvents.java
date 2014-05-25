package com.lbconsulting.alist.classes;

public class AListEvents {

	public static class ListTitleChanged {

		long mActiveListID;
		String mNewListTitle;

		public ListTitleChanged(long activeListID, String newListTitle) {
			mActiveListID = activeListID;
			mNewListTitle = newListTitle;
		}

		public long getActiveListID() {
			return mActiveListID;
		}

		public String getNewListTitle() {
			return mNewListTitle;
		}
	}

	public static class NewListCreated {

		long mActiveListID;

		public NewListCreated(long activeListID) {
			mActiveListID = activeListID;
		}

		public long getActiveListID() {
			return mActiveListID;
		}
	}

	public static class RestartListPreferencesActivity {

		public RestartListPreferencesActivity() {

		}

	}

	public static class ListTargetSelected {

		long mSelectedListTargetID;

		public ListTargetSelected(long selectedListTargetID) {
			mSelectedListTargetID = selectedListTargetID;
		}

		public long getSelectedListID() {
			return mSelectedListTargetID;
		}

	}

	public static class ManageItemsActiveGroupChanged {

		long mActiveGroupID;
		long mActiveListID;

		public ManageItemsActiveGroupChanged(long activeListID, long activeGroupID) {
			mActiveGroupID = activeGroupID;
		}

		public long getActiveGroupID() {
			return mActiveGroupID;
		}

		public long getListID() {
			return mActiveListID;
		}

	}

	public static class ManageItemsRefreshListSettings {

		public ManageItemsRefreshListSettings() {

		}

	}

	public static class ManageItemsTabPostionChange {

		int mTabPosition;
		long mListID;

		public ManageItemsTabPostionChange(long listID, int tabPosition) {
			mTabPosition = tabPosition;
			mListID = listID;
		}

		public int getTabPosition() {
			return mTabPosition;
		}

		public long getListID() {
			return mListID;
		}

	}

	public static class SetInitialColorPickerColor {

		int mInitialColorPickerColor;

		public SetInitialColorPickerColor(int initialColorPickerColor) {
			mInitialColorPickerColor = initialColorPickerColor;
		}

		public int getColorPickerColor() {
			return mInitialColorPickerColor;
		}
	}

	public static class SetPresetColors {

		long mListID;
		int mPresetcolorValue;

		public SetPresetColors(long listID, int presetcolorValue) {
			mListID = listID;
			mPresetcolorValue = presetcolorValue;
		}

		public int getPresetcolorValue() {
			return mPresetcolorValue;
		}

		public long getListID() {
			return mListID;
		}
	}

	public static class ColorPickerColorChange {

		long mListID;
		int mColorPickerColor;

		public ColorPickerColorChange(long listID, int colorPickerColor) {
			mListID = listID;
			mColorPickerColor = colorPickerColor;
		}

		public int getColorPickerColor() {
			return mColorPickerColor;
		}

		public long getListID() {
			return mListID;
		}
	}

	public static class ActiveColorPickerViewChanged {

		long mListID;
		int mColorPickerViewID;

		public ActiveColorPickerViewChanged(long listID, int colorPickerViewID) {
			mListID = listID;
			mColorPickerViewID = colorPickerViewID;
		}

		public int getColorPickerViewID() {

			return mColorPickerViewID;
		}

		public long getListID() {
			return mListID;
		}
	}

	public static class SetListSettingsColors {

		long mListID;

		public SetListSettingsColors(long listID) {
			mListID = listID;
		}

		public long getListID() {
			return mListID;
		}
	}

	public static class ApplyPresetColors {

		long mListID;

		public ApplyPresetColors(long listID) {
			mListID = listID;
		}

		public long getListID() {
			return mListID;
		}

	}

	public static class ActiveLocationChanged {

		long mActiveListID;
		long mActiveStoreID;
		long mActiveLocationID;

		public ActiveLocationChanged(long activeListID, long activeStoreID, long activeLocationID) {
			mActiveListID = activeListID;
			mActiveStoreID = activeStoreID;
			mActiveLocationID = activeLocationID;
		}

		public long getListID() {
			return mActiveListID;
		}

		public long getStoreID() {
			return mActiveStoreID;
		}

		public long getLocationID() {
			return mActiveLocationID;
		}

	}

	public static class RestartStoresActivity {

		long mActiveStoreID;

		public RestartStoresActivity(long activeStoreID) {
			mActiveStoreID = activeStoreID;
		}

		public long getStoreID() {
			return mActiveStoreID;
		}

	}

}
