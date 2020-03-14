package org.apache.cordova.geolocation;

import java.io.File;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

	private static DatabaseHelper sInstance;
	private Context myContext;
	// Table Name
	public static final String LOCATION = "Locations";

	// Table columns
	public static final String _ID = "_id";
	public static final String USER_ID = "user_id";
	public static final String LATITUDE = "latitude";	
	public static final String LONGITUDE = "longitude";
	public static final String TIMESTAMP = "timestamp";
	public static final String ADDRESS = "address";
	public static final String PUNCHOUT = "punchout";

	// Database Information
	static final String DB_NAME = "netram.db";
	
	// static final String DB_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Netram-DB/";

	// database version
	static final int DB_VERSION = 1;
	
	private SQLiteDatabase database;

	// Creating table query
	private static final String CREATE_TABLE = "create table if not exists " + LOCATION + "(" + USER_ID + " TEXT NOT NULL, " + LATITUDE + " TEXT, " + LONGITUDE + " TEXT, " + TIMESTAMP + " TEXT, " + ADDRESS + " TEXT, " + PUNCHOUT + " TEXT);";

	 public static synchronized DatabaseHelper getInstance(Context context) {

	    // Use the application context, which will ensure that you 
	    // don't accidentally leak an Activity's context.
	    // See this article for more information: http://bit.ly/6LRzfx
	    if (sInstance == null) {
		  sInstance = new DatabaseHelper(context.getApplicationContext());
	    }
	    return sInstance;
	  }
	 
	 @Override
	 public void onOpen(SQLiteDatabase db) {
		 super.onOpen(db);
		 final DatabaseHelper dbhelper = new DatabaseHelper(myContext);
	     if (!db.isReadOnly()) {
	         // Enable foreign key constraints
	         db.execSQL("PRAGMA foreign_keys=ON;"); 
	               //(OR)
	         db.setForeignKeyConstraintsEnabled (true);
	     }
	 }
	 
	 public SQLiteDatabase getWritableDatabase()
		{
		    database = SQLiteDatabase.openDatabase(myContext.getDatabasePath(DB_NAME).getAbsolutePath(), null,
		            SQLiteDatabase.OPEN_READWRITE);
		    return database;
		}
	 
	 
	 @Override
	 public void onConfigure(SQLiteDatabase db){
	     db.setForeignKeyConstraintsEnabled(true);
	 }
	 
	 /**
	   * Constructor should be private to prevent direct instantiation.
	   * make call to static method "getInstance()" instead.
	   */
	 
	 private  DatabaseHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
		this.myContext = context;
		Log.e("Path",context.getDatabasePath(DB_NAME).getAbsolutePath());
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + LOCATION);
		onCreate(db);
	}
}