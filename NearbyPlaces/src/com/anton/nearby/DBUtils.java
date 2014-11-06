package com.anton.nearby;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Klasa za upravljanje bazom podataka
 * 
 * @author anton
 *
 */
public class DBUtils {
	public static final String DATABASE_NAME = "placesDatabase";
	public static final String TABLE_NAME = "placesTable";

	private DBUtils() {

	}

	private static SQLiteDatabase getDatabase(Context context) {
		SQLiteDatabase database = context.openOrCreateDatabase(DATABASE_NAME,
				Context.MODE_PRIVATE, null);

		// AKO TABLA NE POSTOJI, NAPRAVITI ĆU NOVU
		String createTable = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME
				+ " ('_id' INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ "name TEXT NOT NULL," + "address CHAR(50) NOT NULL,"
				+ "geoLocation TEXT NOT NULL,image TEXT NOT NULL)";
		database.execSQL(createTable);

		return database;
	}

	public static Cursor getCursor(Context context, String sqlQuery) {
		return getDatabase(context).rawQuery(sqlQuery, null);

	}

	public static void updateTable(Context context,ContentValues cv,String where){
		SQLiteDatabase database = getDatabase(context);
				database.update(TABLE_NAME, cv,where, null);
		database.close();
		
	}
	
	public static void execQuery(Context context,String sqlQuery){
		SQLiteDatabase database = getDatabase(context);
		database.execSQL(sqlQuery);
		database.close();
		
	}

}
