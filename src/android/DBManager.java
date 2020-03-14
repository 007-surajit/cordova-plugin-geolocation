package org.apache.cordova.geolocation;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;


public class DBManager {

	private DatabaseHelper dbHelper;
	
	private Context context;
	
	private SQLiteDatabase database;
	
	public static final String TAG = DBManager.class.toString();

	public DBManager(Context c) {
		context = c;
	}
	
	public DBManager open() throws SQLException {
		dbHelper = DatabaseHelper.getInstance(context);
		database = dbHelper.getWritableDatabase();
		
		return this;
	}

	public void close() {
		dbHelper.close();
	}
	
	public long insertLocationData(ContentValues contentValue) {
		return database.insert(DatabaseHelper.LOCATION, null, contentValue);
	}

	public void delete(long _id) {
		database.delete(DatabaseHelper.LOCATION, DatabaseHelper._ID + "=" + _id, null);
	}

}
