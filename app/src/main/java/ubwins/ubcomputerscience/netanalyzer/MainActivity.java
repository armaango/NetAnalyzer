//netanalyzer v1.0

//CONTRIBUTORS
//agautam2@buffalo.edu, armaango@buffalo.edu
//UBWiNS@ Computer Science @ State University of New York at Buffalo

//DESCRIPTION
//An Android application that lets you monitor interesting cellular network
//statistics on your Android powered phone

package ubwins.ubcomputerscience.netanalyzer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;


public class MainActivity extends AppCompatActivity
{
    static final String TAG = "[NetAnalyzer-DEBUG]";

    @Override
    protected void onCreate(Bundle savedInstanceState)
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

        //Log.v(TAG,"IMEI " + IMEI);
        //Log.v(TAG,"Service " + service);
        //Log.v(TAG,"Model Make " + modelMake);
        //Log.v(TAG,"Android Version " + androidVersion);

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

            // Defined URL  where to send data
            URL url = new URL("http://mediahackerz.azurewebsites.net/ir/finalproject/FileStore.php");

            // Send POST data request

            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write( data );
            wr.flush();

            // Get the server response

            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line = null;

            // Read Server Response
            while((line = reader.readLine()) != null)
            {
                // Append server response in string
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
