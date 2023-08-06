package com.example.BillBreak_downApp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SQLiteAdapter {
    private static final String DATABASE_NAME = "BILL_RESULT_DB";
    private static final String DATABASE_TABLE = "BILL_RESULT";
    public static final String ID = "ID";
    public static final String KEY_CONTENT = "Name";
    public static final String VALUE = "Amount";
    public static final String DATE_TIME = "DateTime";
    private static final int DATABASE_VERSION = 1;

    private static final String SCRIPT_CREATE_DATABASE =
            "create table " + DATABASE_TABLE + "(" +
                    ID + " integer PRIMARY KEY AUTOINCREMENT, " +
                    KEY_CONTENT + " text not null, " +
                    VALUE + " float, " +
                    DATE_TIME + " text)";

    private Context context;
    private SQLiteHelper sqLiteHelper;
    private SQLiteDatabase sqLiteDatabase;

    public SQLiteAdapter(Context c) {
        context = c;
    }

    private class SQLiteHelper extends SQLiteOpenHelper {
        public SQLiteHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL(SCRIPT_CREATE_DATABASE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(sqLiteDatabase);
        }
    }

    public SQLiteAdapter openToWrite() throws android.database.SQLException {
        sqLiteHelper = new SQLiteHelper(context);
        sqLiteDatabase = sqLiteHelper.getWritableDatabase();
        return this;
    }

    public SQLiteAdapter openToRead() throws android.database.SQLException {
        sqLiteHelper = new SQLiteHelper(context);
        sqLiteDatabase = sqLiteHelper.getReadableDatabase();
        return this;
    }

    public void clearData() {
        sqLiteDatabase.delete(DATABASE_TABLE, null, null);
    }

    public void close() {
        sqLiteHelper.close();
    }

    public void saveResult(String name, double amount) {
        ContentValues values = new ContentValues();
        values.put(KEY_CONTENT, name);
        values.put(VALUE, amount);
        values.put(DATE_TIME, getCurrentDateTime());
        sqLiteDatabase.insert(DATABASE_TABLE, null, values);
    }

    private String getCurrentDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    public Cursor getAllResults() {
        return sqLiteDatabase.query(DATABASE_TABLE, null, null, null,
                null, null, DATE_TIME + " DESC");
    }

    public Cursor getResultsByDate(String selectedDate) {
        String[] columns = {ID, KEY_CONTENT, VALUE, DATE_TIME};
        String startOfDay = selectedDate + " 00:00:00";
        String endOfDay = selectedDate + " 23:59:59";
        String selection = DATE_TIME + " BETWEEN ? AND ?";
        String[] selectionArgs = {startOfDay, endOfDay};
        return sqLiteDatabase.query(DATABASE_TABLE, columns, selection, selectionArgs,
                null, null, DATE_TIME + " DESC");
    }
}
