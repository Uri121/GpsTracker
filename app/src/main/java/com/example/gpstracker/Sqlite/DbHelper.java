package com.example.gpstracker.Sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.gpstracker.Model.SosPhoneContact;

import org.json.JSONException;

import java.util.ArrayList;

/**
 * Created by Uri Robinov on 20/7/2019.
 */

//a singleton data base class
public class DbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "sos.db";
    private static final int DATABASE_VERSION = 1;
    private Context mCxt;
    private static DbHelper mInstance = null;
    private SQLiteDatabase db;

    public static DbHelper getInstance(Context ctx) {

        if (mInstance == null) {
            mInstance = new DbHelper(ctx.getApplicationContext());
        }
        return mInstance;
    }
    private DbHelper(Context ctx) {
        super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
        this.mCxt = ctx;
    }

    //creates the sqlite database
    @Override
    public void onCreate(SQLiteDatabase db) {
        this.db = db;


        final String Sql_Create_Questions_Table = " CREATE TABLE "+
                SosDb.SosTable.TABLE_NAME + " ( " +
                SosDb.SosTable._ID+ " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                SosDb.SosTable.PHONENUMBER + " TEXT, " +
                SosDb.SosTable.NAME + " TEXT " +
                ")";

        db.execSQL(Sql_Create_Questions_Table);
    }

    //adding new order to the database
    public boolean AddContact(String name, String phone)  {

        db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put( SosDb.SosTable.NAME,name);
        contentValues.put( SosDb.SosTable.PHONENUMBER,phone);
        long rs = db.insert( SosDb.SosTable.TABLE_NAME,null,contentValues);
        if(rs == -1){
            return false;
        }
        else {
            return true;
        }
    }

    //return the list of orders from sqlite
    public ArrayList<SosPhoneContact> GetList() {
        ArrayList<SosPhoneContact> Contacts = new ArrayList<>();

        db = getReadableDatabase();

        Cursor cursor= db.rawQuery("SELECT * FROM " + SosDb.SosTable.TABLE_NAME,null);

        if (cursor.moveToFirst()){
            do {
                SosPhoneContact sosPhoneContact = new SosPhoneContact();
                sosPhoneContact.setName(cursor.getString(cursor.getColumnIndex(SosDb.SosTable.NAME)));
                sosPhoneContact.setPhone(cursor.getString(cursor.getColumnIndex(SosDb.SosTable.PHONENUMBER)));
                sosPhoneContact.setId(cursor.getString(cursor.getColumnIndex(SosDb.SosTable._ID)));

                Contacts.add(sosPhoneContact);
            }while (cursor.moveToNext());
        }
        cursor.close();
        return Contacts;
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS "+ SosDb.SosTable.TABLE_NAME);
        onCreate(db);
    }

    //deletes all the database orders
    public void CleanCart()
    {
        String query = String.format("DELETE FROM "+ SosDb.SosTable.TABLE_NAME);
        db.execSQL(query);
    }

    //on swipe deletes the item that was swiped
    public void DeleteItem(String id)
    {
        db.delete(SosDb.SosTable.TABLE_NAME, "id="+id,null);
    }
}

