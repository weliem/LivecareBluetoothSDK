package com.example.livecare.bluetoothsdk.initFunctions.data.local;


import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.example.livecare.bluetoothsdk.initFunctions.data.model.DataResultModel;

public class DBManager {
    private DatabaseHelper dbHelper;
    private Context context;
    private SQLiteDatabase database;

    public DBManager(Context c) {
        context = c;
    }

    public void open() throws SQLException {
        dbHelper = new DatabaseHelper(context);
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public void insert(DataResultModel dataResultModel) {
        try {
            ContentValues contentValue = new ContentValues();
            contentValue.put(DatabaseHelper.COLUMN_TYPE, dataResultModel.getType());
            contentValue.put(DatabaseHelper.COLUMN_BT_MAC, dataResultModel.getBTMac());
            contentValue.put(DatabaseHelper.COLUMN_BT_NAME, dataResultModel.getBTName());
            contentValue.put(DatabaseHelper.COLUMN_USER_CREATED_AT, dataResultModel.getCreatedAt());
            database.insert(DatabaseHelper.TABLE_NAME, null, contentValue);
        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public Cursor fetch() {
        String[] columns = new String[] {
                DatabaseHelper._ID,
                DatabaseHelper.COLUMN_TYPE,
                DatabaseHelper.COLUMN_BT_MAC };
        Cursor cursor = database.query(DatabaseHelper.TABLE_NAME, columns, null, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }
/*
    public DataResultModel getData(){
        Cursor  cursor = database.rawQuery("select * from table",null);
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                String name = cursor.getString(cursor.getColumnIndex(countyname));

                list.add(name);
                cursor.moveToNext();
            }
        }
    }*/

    public DataResultModel dataResultModel(Long userId) throws Resources.NotFoundException, NullPointerException {
        Cursor cursor = null;
        try {
            //SQLiteDatabase db = dbHelper.getReadableDatabase();
            cursor = database.rawQuery("SELECT * FROM "
                    + DatabaseHelper.TABLE_NAME,
                    null);

            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                DataResultModel dataResultModel = new DataResultModel();
                dataResultModel.setId(cursor.getLong(cursor.getColumnIndex(DatabaseHelper._ID)));
                dataResultModel.setType(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_TYPE)));
                dataResultModel.setBTMac(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_BT_MAC)));
                dataResultModel.setBTName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_BT_NAME)));
                //dataResultModel.setData(new Gson().fromJson(cursor.getString(cursor.getColumnIndex(USER_COLUMN_BT_DATA))); todo
                dataResultModel.setCreatedAt(cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_CREATED_AT)));
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

    public int update(long _id, String type) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.COLUMN_TYPE, type);
        int i = database.update(DatabaseHelper.TABLE_NAME, contentValues, DatabaseHelper._ID + " = " + _id, null);
        return i;
    }

    public void delete() {
        database.delete(DatabaseHelper.TABLE_NAME, null, null);
    }

}
