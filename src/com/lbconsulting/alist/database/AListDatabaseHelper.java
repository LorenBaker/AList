package com.lbconsulting.alist.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.lbconsulting.alist.utilities.MyLog;

public class AListDatabaseHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "AList.db";
	private static final int DATABASE_VERSION = 4;

	private static SQLiteDatabase dBase;

	public AListDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		AListDatabaseHelper.dBase = database;

		MyLog.i("AListDatabaseHelper", "onCreate");
		ListsTable.onCreate(database);
		GroupsTable.onCreate(database);
		StoresTable.onCreate(database);
		ItemsTable.onCreate(database);
		LocationsTable.onCreate(database);
		BridgeTable.onCreate(database);
	}

	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		MyLog.i("AListDatabaseHelper", "onUpgrade");
		ItemsTable.onUpgrade(database, oldVersion, newVersion);
		ListsTable.onUpgrade(database, oldVersion, newVersion);
		GroupsTable.onUpgrade(database, oldVersion, newVersion);
		StoresTable.onUpgrade(database, oldVersion, newVersion);
		LocationsTable.onUpgrade(database, oldVersion, newVersion);
		BridgeTable.onUpgrade(database, oldVersion, newVersion);
	}

	public static SQLiteDatabase getDatabase() {
		return dBase;
	}

}
