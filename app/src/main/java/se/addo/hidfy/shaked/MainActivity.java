package se.addo.hidfy.shaked;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private String mFileContent;
    private ListView listSongs;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private long curTime, lastUpdate;
    private float x, y, z, last_x, last_y, last_z;
    // only allow one update every 300 ms
    private final static long UPDATEPERIOD = 300;
    private static final int SHAKE_THRESHOLD = 800;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        listSongs = (ListView) findViewById(R.id.xmlLisView);
        DownloadData downloadData = new DownloadData();
        downloadData.execute("http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topsongs/limit=25/xml");
    }

    private void init() {
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        curTime = lastUpdate = (long) 0.0;
        x = y = z = last_x = last_y = last_z = (float) 0.0;
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        long curTime = System.currentTimeMillis();

        if ((curTime - lastUpdate) > UPDATEPERIOD) {
            long diffTime = (curTime - lastUpdate);
            lastUpdate = curTime;

            x = sensorEvent.values[0];
            y = sensorEvent.values[1];
            z = sensorEvent.values[2];

            float speed = Math.abs(x + y +z -last_x - last_y - last_z) / diffTime * 10000;

            if (speed > SHAKE_THRESHOLD) {
                ParseApplication parse = new ParseApplication(mFileContent);
                parse.process();

                ArrayAdapter arrayAdapter = new ArrayAdapter(MainActivity.this, R.layout.list_item, parse.getApplications());
                listSongs.setAdapter(arrayAdapter);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    private class DownloadData extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            mFileContent = downloadXMLFile(strings[0]);
            if (mFileContent == null) {
                Log.d("DownloadData", "Error downloading");
            }
            return mFileContent;
        }


        /*@Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d("DownloadData", "Result: " + result);
        }*/

        private String downloadXMLFile(String urlPath) {
            StringBuilder tempBuffer = new StringBuilder();
            try{
                URL url = new URL(urlPath);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                int response = connection.getResponseCode();
                Log.d("DownloadData", "The response code was " + response);
                InputStream inputStream = connection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

                int charRead;
                char [] inputBuffer = new char[500];
                while (true) {
                    charRead = inputStreamReader.read(inputBuffer);
                    if (charRead <= 0){
                        break;
                    }
                    tempBuffer.append(String.copyValueOf(inputBuffer, 0, charRead));
                }
                return tempBuffer.toString();
            }catch (IOException e) {
                Log.d("DownloadData", "IO Exception reading data: " + e.getMessage());
            }catch (SecurityException e) {
                Log.d("DownloadData", "Security exception. Needs permission? " + e.getMessage());
            }
            return null;

        }

    }
}
