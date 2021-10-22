package com.example.livecare.bluetoothsdk.initFunctions.data.local;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.livecare.bluetoothsdk.initFunctions.data.model.DataResultModel;
import com.google.gson.Gson;
import java.util.Calendar;

public class DbHelper extends SQLiteOpenHelper {

    private static final String USER_TABLE_NAME = "results";
    private static final String USER_COLUMN_RESULT_ID = "id";
    private static final String USER_COLUMN_TYPE = "type";
    private static final String USER_COLUMN_BT_MAC = "BTMac";
    private static final String USER_COLUMN_BT_NAME = "BTName";
    private static final String USER_COLUMN_BT_DATA = "data";
    private static final String USER_COLUMN_USER_CREATED_AT = "created_at";
    private static final String USER_COLUMN_USER_UPDATED_AT = "updated_at";

    // Database Information
    private static final String DB_NAME = "bluetoothsdk.result.DB";
    // database version
    private static final int DB_VERSION = 1;

    public DbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        tableCreateStatements(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + USER_TABLE_NAME);
        onCreate(db);
    }

    private void tableCreateStatements(SQLiteDatabase db) {
        try {
            db.execSQL(
                    "CREATE TABLE IF NOT EXISTS "
                            + USER_TABLE_NAME + "("
                            + USER_COLUMN_RESULT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                            + USER_COLUMN_TYPE + " VARCHAR(20), "
                            + USER_COLUMN_BT_MAC + " VARCHAR(50), "
                            + USER_COLUMN_BT_NAME + " VARCHAR(50), "
                            + USER_COLUMN_USER_CREATED_AT + " LONG " + getCurrentTimeStamp() + ", "
                            + USER_COLUMN_USER_UPDATED_AT + " LONG " + getCurrentTimeStamp() + ")"
            );

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected DataResultModel dataResultModel(Long userId) throws Resources.NotFoundException, NullPointerException {
        Cursor cursor = null;
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            cursor = db.rawQuery(
                    "SELECT * FROM " + USER_TABLE_NAME,
                    new String[]{userId + ""});

            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                DataResultModel dataResultModel = new DataResultModel();
                dataResultModel.setId(cursor.getLong(cursor.getColumnIndex(USER_COLUMN_RESULT_ID)));
                dataResultModel.setType(cursor.getString(cursor.getColumnIndex(USER_COLUMN_TYPE)));
                dataResultModel.setBTMac(cursor.getString(cursor.getColumnIndex(USER_COLUMN_BT_MAC)));
                dataResultModel.setBTName(cursor.getString(cursor.getColumnIndex(USER_COLUMN_BT_NAME)));
                //dataResultModel.setData(new Gson().fromJson(cursor.getString(cursor.getColumnIndex(USER_COLUMN_BT_DATA))); todo
                dataResultModel.setCreatedAt(cursor.getLong(cursor.getColumnIndex(USER_COLUMN_USER_CREATED_AT)));
                return dataResultModel;
            } else {
                throw new Resources.NotFoundException("User with id " + userId + " does not exists");
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (cursor != null)
                cursor.close();
        }
    }

    public Long insertResult(DataResultModel dataResultModel) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(USER_COLUMN_TYPE, dataResultModel.getType());
            contentValues.put(USER_COLUMN_BT_MAC, dataResultModel.getBTMac());
            contentValues.put(USER_COLUMN_BT_NAME, dataResultModel.getBTName());
            contentValues.put(USER_COLUMN_BT_DATA, new Gson().toJson(dataResultModel.getData()));
            return db.insert(USER_TABLE_NAME, null, contentValues);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private Long getCurrentTimeStamp() {
        return Calendar.getInstance().getTime().getTime();
    }
}
