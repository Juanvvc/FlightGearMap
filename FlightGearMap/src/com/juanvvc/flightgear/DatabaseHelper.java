package com.juanvvc.flightgear;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static String DB_PATH = "/data/data/com.juanvvc.flightgear/databases/";
    private static String DB_NAME = "map.db";
    private SQLiteDatabase db = null;
    private Context context;

	public DatabaseHelper(Context context) {
		super(context, DB_NAME, null, 1);
		this.context = context;
	}
	
	/** Creates an empty database in the private directory and copy the database in assets */
	public void createDatabase() throws IOException {
		if (!checkDatabase()) {
			this.getReadableDatabase();
			copyDatabase();
		}
	}
	
	/** Checks if the database exists in the local filesystem */
	private boolean checkDatabase() {
		SQLiteDatabase checkDB = null;
		try {
			checkDB = SQLiteDatabase.openDatabase(DB_PATH + DB_NAME, null, SQLiteDatabase.OPEN_READONLY);
		} catch(SQLiteException e) {
			// database doesn't exist yet
		}
		
		if (checkDB != null) {
			checkDB.close();
		}
		
		return (checkDB != null);
	}
	
	private void copyDatabase() throws IOException {
		InputStream in = context.getAssets().open(DB_NAME);
		OutputStream out = new FileOutputStream(DB_PATH + DB_NAME);
		
		
		 //transfer bytes from the inputfile to the outputfile
		byte[] buffer = new byte[1024];
		int length;
		while ((length = in.read(buffer))>0){
			out.write(buffer, 0, length);
		}
		
		out.flush();
		out.close();
		in.close();
	}
	
	public void openDatabase() throws SQLException {
		this.db = SQLiteDatabase.openDatabase(DB_PATH + DB_NAME, null, SQLiteDatabase.OPEN_READONLY);
	}
	
	@Override
	public synchronized void close() {
		if (this.db != null) {
			this.db.close();
		}
		super.close();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}
	
	public Cursor getAirports(float lat, float lng) {
		if (db == null || !db.isOpen()) {
			return null;
		}
		return db.rawQuery(
				"select * from airport where lat>? and lat<? and lng>? and lng<?",
				new String[] {
						Float.valueOf(lat-1).toString(), Float.valueOf(lat+1).toString(),
						Float.valueOf(lng-1).toString(), Float.valueOf(lng+1).toString()});
	}
	
	public Cursor getNavaids(float lat, float lng) {
		if (db == null || !db.isOpen()) {
			return null;
		}
		return db.rawQuery(
				"select * from navaid where lat>? and lat<? and lng>? and lng<?",
				new String[] {
						Float.valueOf(lat-1).toString(), Float.valueOf(lat+1).toString(),
						Float.valueOf(lng-1).toString(), Float.valueOf(lng+1).toString()});
	}

}
