package ubwins.ubcomputerscience.netanalyzer;

/**
 * Created by Gautam on 6/16/16.
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/*Understanding of SQLite for Android -  http://developer.android.com/training/basics/data-storage/databases.html */

public class DBHandler extends SQLiteOpenHelper
{
    private static  String dbName="mainTuple";
    private static int version=1;
    static final String TAG = "[NetAnalyzer-DEBUG]";

    private static final String schema = "CREATE TABLE cellRecords (LAT DATA, LONG DATA, LOCALITY DATA, CITY DATA, STATE DATA, COUNTRY DATA, NETWORK_PROVIDER DATA, TIMESTAMP DATA, NETWORK_RSSI DATA, NETWORK_STATE DATA)";

    public DBHandler(Context context)
    {
        super(context, dbName, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(schema);
        Log.v(TAG, "CREATING DB " + dbName + "WITH TABLE cellRecords");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int obsolete, int latest)
    {
        //logic understood from - http://stackoverflow.com/questions/3675032/drop-existing-table-in-sqlite-when-if-exists-operator-is-not-supported
        db.execSQL("DROP TABLE IF EXISTS cellRecords");
        onCreate(db);
    }

}