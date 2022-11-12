package com.bluetooth.pa2123.resus.ui;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String CPR_RECORD_TABLE = "CPR_RECORD_TABLE";
    public static final String COLUMN_CPR_COUNT = "CPR_COUNT";
    public static final String COLUMN_GOOD_CPR_COUNT = "GOOD_CPR_COUNT";
    public static final String COLUMN_CPR_RATE = "CPR_RATE";
    public static final String COLUMN_ID = "ID";
    public static final String COLUMN_CPR_TIMER = "CPR_TIMER";
    public static final String USER_RECORD_TABLE = "USER_RECORD_TABLE";
    public static final String COLUMN_USERNAME = "USER_USERNAME";
    public static final String COLUMN_PASSWORD = "USER_PASSWORD";
    public static final String COLUMN_CPR_COMPRESSION_FRACTION = "CPR_COMPRESSION_FRACTION";
    public static final String COLUMN_CPR_USERNAME = "CPR_USERNAME";
    public static final String COLUMN_CPR_DATE = "CPR_DATE";

    public DatabaseHelper(@Nullable Context context) {
        super(context, "records.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableStatement ="CREATE TABLE " + CPR_RECORD_TABLE + " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_CPR_USERNAME + " TEXT, " + COLUMN_CPR_RATE + " TEXT, " + COLUMN_CPR_COUNT + " INT, " + COLUMN_GOOD_CPR_COUNT + " INT, " + COLUMN_CPR_COMPRESSION_FRACTION + " FLOAT, " + COLUMN_CPR_DATE + " TEXT," + COLUMN_CPR_TIMER + " INT)";
        String createUserTable = "CREATE TABLE " + USER_RECORD_TABLE + " (" + COLUMN_USERNAME + " TEXT PRIMARY KEY, " + COLUMN_PASSWORD + " TEXT)";
        db.execSQL(createTableStatement);
        db.execSQL(createUserTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + USER_RECORD_TABLE);
    }

    public boolean addUser(usersRecords usersRecords) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_USERNAME, usersRecords.getUsername());
        cv.put(COLUMN_PASSWORD, usersRecords.getPassword());

        long insert = db.insert(USER_RECORD_TABLE, null, cv);
        if(insert == 1) {
            return false;
        } else {
            return true;
        }
    }

    public boolean checkUser(String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        String queryString = "SELECT * FROM " + USER_RECORD_TABLE + " WHERE " + COLUMN_USERNAME + " = ?";
        Cursor cursor = db.rawQuery(queryString, new String[] {username});

        if(cursor.moveToFirst()) {
            return true;
        } else {
            return false;
        }

    }

    public boolean checkUsernamePassword(usersRecords usersRecords) {
        SQLiteDatabase db = this.getWritableDatabase();
        String queryString = "SELECT * FROM " + USER_RECORD_TABLE + " WHERE " + COLUMN_USERNAME + " = ? AND " + COLUMN_PASSWORD + " = ?";
        Cursor cursor = db.rawQuery(queryString, new String[] {usersRecords.getUsername(), usersRecords.getPassword()});

        if(cursor.moveToFirst()) {
            return true;
        } else {
            return false;
        }

    }

    public boolean addOne(cprHistory cprHistory) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_CPR_COUNT, cprHistory.getCC());
        cv.put(COLUMN_CPR_RATE, cprHistory.getCR());
        cv.put(COLUMN_GOOD_CPR_COUNT, cprHistory.getGC());
        cv.put(COLUMN_CPR_TIMER, cprHistory.getTimer());
        cv.put(COLUMN_CPR_USERNAME, cprHistory.getUsername());
        cv.put(COLUMN_CPR_COMPRESSION_FRACTION,cprHistory.getCF());
        cv.put(COLUMN_CPR_DATE,cprHistory.getCurrentTime());


        long insert = db.insert(CPR_RECORD_TABLE, null, cv);
        if(insert == 1) {
            return false;
        } else {
            return true;
        }
    }

    public boolean deleteOne(cprHistory cprHistory) {
        SQLiteDatabase db = this.getWritableDatabase();
        String queryString = "DELETE FROM " + CPR_RECORD_TABLE + " WHERE " + COLUMN_ID + " = " + cprHistory.getId();
        Cursor cursor = db.rawQuery(queryString, null);

        if(cursor.moveToNext()) {
            return true;
        } else {
            return false;
        }
    }

    public List<cprHistory> getCPRHistory(String user) {
        List<cprHistory> returnList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        String queryString = "SELECT * FROM " + CPR_RECORD_TABLE + " WHERE " + COLUMN_CPR_USERNAME + " = ?";

        Cursor cursor = db.rawQuery(queryString, new String[] {user});

        if(cursor.moveToFirst()) {
            // loop through the cursor
            do {
                int cprID = cursor.getInt(0);
                String username = cursor.getString(1);
                float compRate = cursor.getFloat(2);
                int compCount = cursor.getInt(3);
                int goodComp = cursor.getInt(4);
                String compFrac = cursor.getString(5);
                String currentDate = cursor.getString(6);
                int timer = cursor.getInt(7);

                cprHistory newCpr = new cprHistory(cprID,username,compRate,compCount,goodComp,compFrac,currentDate,timer);
                returnList.add(newCpr);
            } while(cursor.moveToNext());
        } else {
            // failure. do not add anything to the list.
        }

        cursor.close();
        db.close();
        return returnList;
    }
}
