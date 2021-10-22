package com.example.livecare.bluetoothsdk.initFunctions.data.local;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Table Name
    static final String TABLE_NAME = "COUNTRIES";

    // Table columns
    static final String _ID = "_id";
    static final String COLUMN_TYPE = "type";
    public static final String COLUMN_BT_MAC = "mac";
    static final String COLUMN_BT_NAME = "name";
    static final String COLUMN_USER_CREATED_AT = "created_at";


    // Database Information
    private static final String DB_NAME = "bluetoothsdk.result.DB";
    // database version
    private static final int DB_VERSION = 1;

    // Creating table query
    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS "
            + TABLE_NAME + "("
            + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_TYPE + " TEXT NOT NULL, "
            + COLUMN_BT_MAC + " TEXT NOT NULL, "
            + COLUMN_BT_NAME + " TEXT NOT NULL, "
            + COLUMN_USER_CREATED_AT + " LONG" +
            ");";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
