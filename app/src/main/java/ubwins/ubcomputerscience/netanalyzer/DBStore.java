package ubwins.ubcomputerscience.netanalyzer;

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.util.Log;

/**
 * Created by Gautam on 6/17/16.
 */
public class DBStore
{
    static final String TAG = "[GPS-DEBUG]";
    private final Context mContext;
    GPSTracker gps;
    String locality;
    String adminArea;
    String countryCode;
    String throughFare;
    Double latitude;
    Double longitude;

    public DBStore(Context context)
    {
        this.mContext=context;
    }

    public void insertIntoDB(Location location, String timeStamp, String cellularInfo)
    {
                    ContentValues contentValues = new ContentValues();
                    DBHandler dbHandler = new DBHandler(mContext);
                    SQLiteDatabase sqLiteDatabase = dbHandler.getWritableDatabase();

                    latitude=location.getLatitude();
                    longitude=location.getLongitude();

                    gps = new GPSTracker(mContext);
                    locality=gps.getLocality();
                    adminArea=gps.getAdminArea();
                    countryCode=gps.getCountryCode();
                    throughFare=gps.getThroughFare();

                    Log.v(TAG, "before split: " + cellularInfo);
                    String[] splitter = cellularInfo.split("@");
                    Log.v(TAG, "splitter of zero: "+splitter[0]);
                    Log.v(TAG, "splitter of one: "+splitter[1]);
                    String networkType = splitter[0];
                    String splitter1[] = splitter[1].split("_");
                    String networkState = splitter1[0];
                    Log.v(TAG,"splitter1 of zero: " + splitter1[0]);
                    String networkRSSI = splitter1[1];

                    Log.v(TAG,"Trying to push to DB");
                    contentValues.put("LAT",latitude);
                    contentValues.put("LONG",longitude);
                    contentValues.put("NETWORK_PROVIDER", "NETWORK");
                    contentValues.put("LOCALITY",throughFare);
                    contentValues.put("CITY",locality);
                    contentValues.put("STATE",adminArea);
                    contentValues.put("COUNTRY",countryCode);
                    contentValues.put("TIMESTAMP",timeStamp);
                    contentValues.put("NETWORK_TYPE", networkType);
                    contentValues.put("NETWORK_STATE", networkState);
                    contentValues.put("NETWORK_RSSI", networkRSSI);

                    sqLiteDatabase.insert("cellRecords", null, contentValues);
                    sqLiteDatabase.close();
                    Log.v(TAG,"Push to DB Successful");
    }
}
