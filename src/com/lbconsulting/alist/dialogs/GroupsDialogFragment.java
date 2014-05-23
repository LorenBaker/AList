package com.lbconsulting.alist.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.EditText;

import com.lbconsulting.alist.R;
import com.lbconsulting.alist.database.GroupsTable;
import com.lbconsulting.alist.utilities.MyLog;

public class GroupsDialogFragment extends DialogFragment {

	public static final int EDIT_GROUP_NAME = 30;
	public static final int NEW_GROUP = 40;
	public static final int DELETE_GROUP = 50;

	private Button btnApply;
	private Button btnCancel;
	private static EditText txtGroupName;

	private long mActiveListID;
	private long mActiveGroupID;
	private int mDialogType;

	public GroupsDialogFragment() {
		// Empty constructor required for DialogFragment
	}

	/**
	 * Creates a new instance of the GroupsDialogFragment
	 * 
	 * @param listID
	 * @param groupID
	 * @param dialogType
	 * @return GroupsDialogFragment
	 */
	public static GroupsDialogFragment newInstance(long listID, long groupID, int dialogType) {
		if (listID < 1) {
			if (dialogType != EDIT_GROUP_NAME) {
				MyLog.e("GroupsDialogFragment", "newInstance: invalid listID:" + listID);
			}
		}

		if (groupID < 1) {
			if (dialogType != NEW_GROUP) {
				MyLog.e("GroupsDialogFragment", "newInstance: invalid groupID:" + groupID);
			}
		}

		GroupsDialogFragment f = new GroupsDialogFragment();
		// Supply itemID input as an argument.
		Bundle args = new Bundle();
		args.putLong("ActiveListID", listID);
		args.putLong("ActiveGroupID", groupID);
		args.putInt("dialogType", dialogType);
		f.setArguments(args);
		return f;
	}

	@Override
	public void onAttach(Activity activity) {
		MyLog.i("GroupsDialogFragment", "onAttach");
		super.onAttach(activity);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// Store our mActiveGroupID
		outState.putLong("ActiveListID", this.mActiveListID);
		outState.putLong("ActiveGroupID", this.mActiveGroupID);
		outState.putLong("dialogType", this.mDialogType);
		super.onSaveInstanceState(outState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (savedInstanceState != null && savedInstanceState.containsKey("mActiveGroupID")) {
			mActiveListID = savedInstanceState.getLong("ActiveListID", 0);
			mActiveGroupID = savedInstanceState.getLong("ActiveGroupID", 0);
			mDialogType = savedInstanceState.getInt("dialogType", 0);
		} else {
			Bundle bundle = getArguments();
			if (bundle != null) {
				mActiveListID = bundle.getLong("ActiveListID", 0);
				mActiveGroupID = bundle.getLong("ActiveGroupID", 0);
				mDialogType = bundle.getInt("dialogType", 0);
			}
		}

		// inflate view
		View view = inflater.inflate(R.layout.dialog_group_create_edit, container);

		switch (mDialogType) {

			case EDIT_GROUP_NAME:
				MyLog.i("GroupsDialogFragment", "onCreateView: Edit Group Name");
				getDialog().setTitle("Edit Group Name?");
				break;

			case NEW_GROUP:
				MyLog.i("GroupsDialogFragment", "onCreateView: New Group");
				getDialog().setTitle("Create New Group?");
				break;

			case DELETE_GROUP:
				MyLog.i("GroupsDialogFragment", "onCreateView: Delete Group");
				getDialog().setTitle("Delete Group?");
				break;

			default:
				break;
		}

		if (view != null) {
			btnApply = (Button) view.findViewById(R.id.btnApply);
			btnApply.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					switch (mDialogType) {

						case EDIT_GROUP_NAME:
							ContentValues newFieldValues = new ContentValues();
							String newGroupName = txtGroupName.getText().toString();
							newGroupName = newGroupName.trim();
							if (!newGroupName.isEmpty()) {
								newFieldValues.put(GroupsTable.COL_GROUP_NAME, newGroupName);
								GroupsTable.UpdateGroupTableFieldValues(getActivity(), mActiveGroupID, newFieldValues);
							}
							break;

						case NEW_GROUP:
							newGroupName = txtGroupName.getText().toString();
							newGroupName = newGroupName.trim();
							if (!newGroupName.isEmpty()) {
								GroupsTable.CreateNewGroup(getActivity(), mActiveListID, newGroupName);
							}
							break;

						case DELETE_GROUP:
							GroupsTable.DeleteGroup(getActivity(), mActiveGroupID);
							break;

						default:
							break;
					}
					getDialog().dismiss();
				}
			});

			btnCancel = (Button) view.findViewById(R.id.btnCancel);
			btnCancel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					getDialog().dismiss();
				}
			});

			txtGroupName = (EditText) view.findViewById(R.id.txtGroupName);
			if (txtGroupName != null) {
				switch (mDialogType) {
					case EDIT_GROUP_NAME:
						// We're displaying the Edit Group Name dialog
						String groupName = GroupsTable.getGroupName(getActivity(), mActiveGroupID);
						txtGroupName.setText(groupName);
						break;

					case NEW_GROUP:
						// We're displaying the New Group dialog
						txtGroupName.setText("New Group");
						break;

					case DELETE_GROUP:
						// We're displaying the Delete Group dialog
						groupName = GroupsTable.getGroupName(getActivity(), mActiveGroupID);
						txtGroupName.setText(groupName);
						break;

					default:
						break;

				}

				if (txtGroupName.requestFocus()) {
					getDialog().getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
				}
			}
		}

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		MyLog.i("GroupsDialogFragment", "onActivityCreated");
		super.onActivityCreated(savedInstanceState);

		Bundle bundle = this.getArguments();
		if (bundle != null) {
			mActiveListID = bundle.getLong("ActiveListID", 0);
			mActiveGroupID = bundle.getLong("ActiveGroupID", 0);
			mDialogType = bundle.getInt("dialogType", 0);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		MyLog.i("GroupsDialogFragment", "onCreate");
		super.onCreate(savedInstanceState);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		MyLog.i("GroupsDialogFragment", "onCreateDialog");
		return super.onCreateDialog(savedInstanceState);
	}

	@Override
	public void onDestroyView() {
		MyLog.i("GroupsDialogFragment", "onDestroyView");
		super.onDestroyView();
	}

	@Override
	public void onDetach() {
		MyLog.i("GroupsDialogFragment", "onDetach");
		super.onDetach();
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		MyLog.i("GroupsDialogFragment", "onDismiss");
		super.onDismiss(dialog);
	}

	@Override
	public void onStart() {
		MyLog.i("GroupsDialogFragment", "onStart");
		super.onStart();
	}

	@Override
	public void onStop() {
		MyLog.i("GroupsDialogFragment", "onStop");
		super.onStop();
	}

	@Override
	public void onDestroy() {
		MyLog.i("GroupsDialogFragment", "onDestroy");
		super.onDestroy();
	}

}
