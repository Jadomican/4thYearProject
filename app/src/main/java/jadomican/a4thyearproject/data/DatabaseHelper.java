package jadomican.a4thyearproject.data;

/*
 * Jason Domican
 * Final Year Project
 * Institute of Technology Tallaght
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Android has a base class for dealing with SQLite databases called SQLiteOpenHelper.
 * This is a derived class whose main purpose is to set up the database on first access.
 */
class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DBNAME = "mediapp.db";
    private static final int DBVERSION = 1;

    /**
     * Create a new SQLiteOpenHelper object for this database.
     *
     * @param context the application context
     */
    DatabaseHelper(Context context) {
        super(context, DBNAME, null, DBVERSION);
    }

    /**
     * Called when the database needs to be created
     *
     * @param db the database handle
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(UserDetailsContentContract.UserDetails.CREATE_SQLITE_TABLE);
    }

    /**
     * Called when the database needs to be updated
     * AWS does not support database updating as of the time of writing. Mandatory override
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
