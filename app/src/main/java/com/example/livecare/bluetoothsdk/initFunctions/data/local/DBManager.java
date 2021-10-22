package com.example.livecare.bluetoothsdk.initFunctions.data.local;


import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.example.livecare.bluetoothsdk.initFunctions.data.model.DataResultModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
            contentValue.put(DatabaseHelper.COLUMN_DATA, new Gson().toJson(dataResultModel.getData()));
            contentValue.put(DatabaseHelper.COLUMN_BT_MAC, dataResultModel.getBTMac());
            contentValue.put(DatabaseHelper.COLUMN_BT_NAME, dataResultModel.getBTName());
            contentValue.put(DatabaseHelper.COLUMN_USER_CREATED_AT, dataResultModel.getCreatedAt());
            database.insert(DatabaseHelper.TABLE_NAME, null, contentValue);
        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public List<DataResultModel> dataResultModel() throws Resources.NotFoundException, NullPointerException {
        List<DataResultModel> dataResultModels = new ArrayList<>();
        Gson gson = new Gson();
        try (Cursor cursor = database.rawQuery("SELECT * FROM "
                        + DatabaseHelper.TABLE_NAME,
                null)) {

            if (cursor.getCount() > 0) {
                if (cursor.moveToFirst()) {
                    while (!cursor.isAfterLast()) {
                        DataResultModel dataResultModel = new DataResultModel();
                        dataResultModel.setId(cursor.getLong(cursor.getColumnIndex(DatabaseHelper._ID)));
                        dataResultModel.setType(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_TYPE)));
                        dataResultModel.setBTMac(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_BT_MAC)));
                        dataResultModel.setBTName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_BT_NAME)));
                        String json = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_DATA));
                        Type type = new TypeToken<Map<String, String>>(){}.getType();
                        Map<String, Object> map = gson.fromJson(json, type);
                        dataResultModel.setData(map);
                        dataResultModel.setCreatedAt(cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_CREATED_AT)));
                        dataResultModels.add(dataResultModel);
                        cursor.moveToNext();
                    }

                }
                return dataResultModels;
            } else {
                Log.d("TAG", "dataResultModel: no user found");
                //throw new Resources.NotFoundException("User with id " + userId + " does not exists");
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            throw e;
        }
        return null;
    }

    public void delete() {
        database.delete(DatabaseHelper.TABLE_NAME, null, null);
    }

}
