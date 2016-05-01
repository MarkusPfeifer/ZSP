package at.pfeifer.zsp;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {


    private String downloadUrlStrom;
    private String downloadUrlWasser;
    private String uploadUrlStrom;
    private String uploadUrlWasser;
    private EditText strom;
    private EditText wasser;
    private EditText datum;
    private String sdatum;
    private SharedPreferences sharedPrefStrom;
    private SharedPreferences sharedPrefWasser;
    private double oldValueWasser;
    private double oldValueStrom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        strom = (EditText) findViewById(R.id.editTextStrom);
        wasser = (EditText) findViewById(R.id.editTextWasser);
        datum = (EditText) findViewById(R.id.editTextDatum);

        sdatum = getDate();

        datum.setText(sdatum);

        sharedPrefStrom = getPreferences(Context.MODE_PRIVATE);
        sharedPrefWasser = getPreferences(Context.MODE_PRIVATE);
    }

    public String getDate()
    {
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        String date = df.format(new Date());

        return date;
    }

    public String getUrlDate(){

        DateFormat df = new SimpleDateFormat("yyyyMMdd");

        String date = df.format(new Date());

        return date;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onClickLoadStrom(View view) {

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {

            downloadUrlStrom = "http://192.168.0.111:821/getconsumption?typ=strom";
            new DownloadWebpageTaskStrom().execute(downloadUrlStrom);

        } else {

            Toast.makeText(MainActivity.this, "Error, no Network Connection", Toast.LENGTH_SHORT).show();
        }
    }

    public void onClickLoadWasser(View view) {

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {

            downloadUrlWasser = "http://192.168.0.111:821/getconsumption?typ=wasser";
            new DownloadWebpageTaskWasser().execute(downloadUrlWasser);

        } else {

            Toast.makeText(MainActivity.this, "Error, no Network Connection", Toast.LENGTH_SHORT).show();
        }
    }

    private class DownloadWebpageTaskStrom extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {

                return downloadUrl(urls[0]);

            } catch (IOException e) {

                return "Unable to retrieve web page. URL may be invalid.";
            }
        }
        // onPostExecute displays the results of the AsyncTask.

        protected void onPostExecuteStrom(String result) {

            strom.setText(result);
            oldValueStrom = Double.valueOf(result);
        }
    }

    private class DownloadWebpageTaskWasser extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {

                return downloadUrl(urls[0]);

            } catch (IOException e) {

                return "Unable to retrieve web page. URL may be invalid.";
            }
        }
        // onPostExecute displays the results of the AsyncTask.

        protected void onPostExecuteWasser(String result) {

            wasser.setText(result);
            oldValueWasser = Double.valueOf(result);
        }
    }

    private String downloadUrl(String myurl) throws IOException {
        InputStream is = null;
        // Only display the first 500 characters of the retrieved
        // web page content.
        int len = 500;

        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response  =conn.getResponseCode();
            is = conn.getInputStream();

            // Convert the InputStream into a string
            String contentAsString = readIt(is, len);
            return contentAsString;

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {

                is.close();
            }
        }
    }

    public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {

        Reader reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);

        String Buffer = String.valueOf(buffer);

        return Buffer;
    }

    public void onClickSaveStrom(View view){

        sdatum = getUrlDate();
        double valueStrom = Double.valueOf(strom.getText().toString());

        SharedPreferences.Editor editor = sharedPrefStrom.edit();
        editor.putString("Value", strom.getText().toString());
        editor.commit();

        if (oldValueStrom < valueStrom) {

            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {

                uploadUrlStrom = "http://192.168.0.111:821/consumption?typ=strom&date=" + sdatum + "&value=" + valueStrom;
                new UploadWebpageTaskStrom().execute(uploadUrlStrom);

            } else {

                Toast.makeText(MainActivity.this, "Error, no Network Connection", Toast.LENGTH_SHORT).show();
            }
        }
        else{

            Toast.makeText(MainActivity.this, "Error, alter Zählerstand größer als neuer", Toast.LENGTH_SHORT).show();
        }
    }

    public void onClickSaveWasser(View view) {

        sdatum = getUrlDate();
        double valueWasser = Double.valueOf(wasser.getText().toString());

        SharedPreferences.Editor editor = sharedPrefWasser.edit();
        editor.putString("Value", wasser.getText().toString());
        editor.commit();

        if (oldValueWasser < valueWasser){

            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {

                uploadUrlWasser = "http://192.168.0.111:821/consumption?typ=wasser&date=" + sdatum + "&value=" + valueWasser;
                new UploadWebpageTaskWasser().execute(uploadUrlWasser);

            } else {

                Toast.makeText(MainActivity.this, "Error, no Network Connection", Toast.LENGTH_SHORT).show();
            }

        }
        else{

            Toast.makeText(MainActivity.this, "Error, alter Zählerstand größer als neuer", Toast.LENGTH_SHORT).show();
        }
    }
    private class UploadWebpageTaskStrom extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {

                return uploadUrl(urls[0]);

            } catch (IOException e) {

                return "Unable to retrieve web page. URL may be invalid.";
            }
        }
        // onPostExecute displays the results of the AsyncTask.

        protected void onPostExecuteWasser(String result) {

        }
    }

    private class UploadWebpageTaskWasser extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {

                return uploadUrl(urls[0]);

            } catch (IOException e) {

                return "Unable to retrieve web page. URL may be invalid.";
            }
        }
        // onPostExecute displays the results of the AsyncTask.

        protected void onPostExecuteWasser(String result) {

        }
    }
    private String uploadUrl(String myurl) throws IOException {
        InputStream is = null;
        // Only display the first 500 characters of the retrieved
        // web page content.
        int len = 500;

        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response  =conn.getResponseCode();
            is = conn.getInputStream();

            // Convert the InputStream into a string
            String contentAsString = readIt(is, len);
            return contentAsString;

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {

                is.close();
            }
        }
    }
}