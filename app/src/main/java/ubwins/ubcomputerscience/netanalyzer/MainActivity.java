//NetAnalyzer v1.0

//CONTRIBUTORS
//agautam2@buffalo.edu, armaango@buffalo.edu
//UBWiNS@ Computer Science @ State University of New York at Buffalo

//DESCRIPTION
//An Android application that lets you monitor interesting cellular network
//statistics on your Android powered phone

package ubwins.ubcomputerscience.netanalyzer;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.channels.FileChannel;


public class MainActivity extends AppCompatActivity
{
    static final String TAG = "[NetAnalyzer-DEBUG]";

    Button track;
    GPSTracker gps;
    DBStore dbStore;
    Context context;
    CellularDataRecorder cdr;
    Location location;

    //Exports SQLiteDB to CSV file in Phone Storage
    public void exportDatabase()
    {
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state))
        {
            Toast.makeText(this, "MEDIA MOUNT ERROR!", Toast.LENGTH_LONG).show();
        }
        else
        {
            File exportDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!exportDir.exists())
            {
                exportDir.mkdirs();
                Log.v(TAG, "Directory made");
            }

            File file = new File(exportDir, "GeoData.csv") ;
            PrintWriter printWriter = null;
            try
            {
                file.createNewFile();
                printWriter = new PrintWriter(new FileWriter(file));
                DBHandler dbHandler = new DBHandler(getApplicationContext());
                SQLiteDatabase sqLiteDatabase = dbHandler.getReadableDatabase();
                Cursor curCSV = sqLiteDatabase.rawQuery("select * from footRecords", null);
                printWriter.println("Latitude,Longitude,locality,city,state,country,NETWORK_PROVIDER");
                while(curCSV.moveToNext())
                {
                    Double latitude = curCSV.getDouble(curCSV.getColumnIndex("LAT"));
                    Double longitude = curCSV.getDouble(curCSV.getColumnIndex("LONG"));
                    String networkProvider = curCSV.getString(curCSV.getColumnIndex("NETWORK_PROVIDER"));
                    String locality = curCSV.getString(curCSV.getColumnIndex("LOCALITY"));
                    String city = curCSV.getString(curCSV.getColumnIndex("CITY"));
                    String stateName = curCSV.getString(curCSV.getColumnIndex("STATE"));
                    String country = curCSV.getString(curCSV.getColumnIndex("COUNTRY"));

                    String record = latitude + "," + longitude + "," + locality + "," + city + "," + stateName + "," + country + "," + networkProvider;
                    Log.v(TAG, "attempting to write to file");
                    printWriter.println(record);
                    Log.v(TAG, "data written to file");
                }
                curCSV.close();
                sqLiteDatabase.close();
            }

            catch(Exception exc)
            {
                exc.printStackTrace();
                Toast.makeText(this, "ERROR!", Toast.LENGTH_LONG).show();
            }
            finally
            {
                if(printWriter != null) printWriter.close();
            }

            //If there are no errors, return true.
            Toast.makeText(this, "DB Exported to CSV file!", Toast.LENGTH_LONG).show();
        }
    }

    private void deleteDB()
    {
        boolean result = this.deleteDatabase("mainTuple");
        if (result==true)
        {
            Toast.makeText(this, "DB Deleted!", Toast.LENGTH_LONG).show();
        }
    }
    private void exportDB()
    {
        File sd = Environment.getExternalStorageDirectory();
        File data = Environment.getDataDirectory();
        FileChannel source = null;
        FileChannel destination = null;
        String currentDBPath = "/data/" + "com.example.gpstracking" + "/databases/" + "mainTuple";
        String backupDBPath = "mainTuple";
        File currentDB = new File(data, currentDBPath);
        File backupDB = new File(sd, backupDBPath);
        if (currentDB.exists())
        {
            try
            {
                source = new FileInputStream(currentDB).getChannel();
                destination = new FileOutputStream(backupDB).getChannel();
                destination.transferFrom(source, 0, source.size());
                source.close();
                destination.close();
                Toast.makeText(this, "DB Exported!", Toast.LENGTH_LONG).show();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.v(TAG,"NetAnalyzer Service Started");

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        Boolean started = sharedPref.getBoolean("Started", false);

        if(started)
        {
            Button button = (Button) findViewById(R.id.button);
            button.setEnabled(false);
        }

        track = (Button) findViewById(R.id.button1);
        track.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg1)
            {
                deleteDB();
            }
        });

        track = (Button) findViewById(R.id.button2);
        track.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View arg1)
            {
                exportDB();
            }
        });

        track = (Button) findViewById(R.id.button3);
        track.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View arg1)
            {
                exportDatabase();
            }
        });

        ImageButton imageButton = (ImageButton) findViewById(R.id.btnShowLocation);

        imageButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                gps = new GPSTracker(MainActivity.this);
                if(!gps.canGetLocation)
                {
                    gps.showSettingsAlertForceGPS();
                }
                else
                {
                   location = gps.getLocationByNetwork();
                }
//                if(gps.canGetLocation())
//                {
//                    double latitude = gps.getLatitude();
//                    double longitude = gps.getLongitude();
//                    String countryCode = gps.getCountryCode();
//                    String adminArea = gps.getAdminArea();
//                    String locality = gps.getLocality();
//                    String throughFare = gps.getThroughFare();
//
//                    // \n is for new line
//                    Toast.makeText(getApplicationContext(), "You are at - " + throughFare + ", " + locality + ", " + adminArea + ", " + countryCode + "\n" +
//                            "Latitude: " + latitude + "\nLongitude: " + longitude,  Toast.LENGTH_LONG).show();
//                }
//                else
//                {
//
//
//                }

                final TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
                cdr = new CellularDataRecorder();
                Log.v(TAG, "Calling getLocalTimeStamp and getCellularInfo");
                String timeStamp = cdr.getLocalTimeStamp();
                String cellularInfo = cdr.getCellularInfo(telephonyManager);

                Log.v(TAG, "TIME STAMP: " + timeStamp);
                Log.v(TAG, "CELLULAR INFO: " + cellularInfo);
                dbStore = new DBStore(MainActivity.this);
                dbStore.insertIntoDB(location,timeStamp,cellularInfo);



            }
        });
    }


    private String getIMEI()
    {
        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getDeviceId();
    }

    private String getService()
    {
        TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        return manager.getNetworkOperatorName();
    }

    private String getModel()

    {
        return android.os.Build.MANUFACTURER+":"+android.os.Build.MODEL;
    }

    private String getOS()

    {
        return android.os.Build.VERSION.RELEASE;
    }

    private void enableStrictMode() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    public void onRegisterClicked(View view)
    {
        String text = "";
        BufferedReader reader=null;

        String IMEI = getIMEI();
        String service = getService();
        String modelMake = getModel();
        String androidVersion = getOS();

        try {
            enableStrictMode();
            String data = URLEncoder.encode("imei", "UTF-8")
                    + "=" + URLEncoder.encode(IMEI, "UTF-8");

            data += "&" + URLEncoder.encode("service", "UTF-8") + "="
                    + URLEncoder.encode(service, "UTF-8");

            data += "&" + URLEncoder.encode("model_make", "UTF-8")
                    + "=" + URLEncoder.encode(modelMake, "UTF-8");

            data += "&" + URLEncoder.encode("os_version", "UTF-8")
                    + "=" + URLEncoder.encode(androidVersion, "UTF-8");

            URL url = new URL("http://mediahackerz.azurewebsites.net/ir/finalproject/FileStore.php");

            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write( data );
            wr.flush();

            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line = null;

            while((line = reader.readLine()) != null)
            {
                sb.append(line + "\n");
            }

            text = sb.toString();
            Log.v(TAG,"Reply from server:");
            Log.v(TAG,text);
        }
        catch(UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        catch (IOException i)
        {
            i.printStackTrace();
        }
        finally
        {
            try
            {

                reader.close();
            }

            catch(Exception ex) {}
        }

    }

}
