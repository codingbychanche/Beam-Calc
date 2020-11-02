package berthold.beamcalc;

/*
 * InfoActivity.java
 *
 * Created by Berthold Fritz
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License:
 * https://creativecommons.org/licenses/by-nc-sa/4.0/
 *
 * Last modified 8/31/19 7:51 PM
 */


/**
 * Shows app info...
 *
 */

import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ProgressBar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;

public class InfoActivity extends AppCompatActivity {

    // Info
    private static String tag;

    // Filesystem
    private BufferedReader bufferedReader;

    // Html
    private StringBuilder htmlSite;
    private WebView webView;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        // UI
        final Handler handler=new Handler();
        webView=(WebView)findViewById(R.id.browser);
        progress=(ProgressBar)findViewById(R.id.html_load_progress);

        // @rem:Get current locale (determine language from Androids settings@@
        //final Locale current=getResources().getConfiguration().locale;
        final String  current=getResources().getConfiguration().locale.getLanguage();
        Log.v("LOCALE","Country:"+current);

        // Load html...
        progress.setVisibility(View.VISIBLE);

        Thread t=new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    htmlSite=new StringBuilder();

                    // @rem:Shows how to load data from androids 'assests'- folder@@

                    if (current.equals("de") || current.equals("en")) {
                        if (current.equals("de"))
                            bufferedReader = new BufferedReader(new InputStreamReader(getAssets().open("beamCalcInfoFile-de.html")));
                        if (current.equals("en"))
                            bufferedReader = new BufferedReader(new InputStreamReader(getAssets().open("beamCalcInfoFile-en.html")));
                    } else
                        bufferedReader = new BufferedReader(new InputStreamReader(getAssets().open("beamCalcInfoFile-en.html")));

                    String line;
                        while ((line = bufferedReader.readLine()) != null)
                            htmlSite.append(line);

                }catch (IOException io){
                    Log.v("Info",io.toString());
                }

                // Wait a vew millisec's to enable the main UI thread
                // to react.
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {}

                // Show
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        progress.setVisibility(View.GONE);
                        webView.loadData(htmlSite.toString(),"text/html",null);
                    }
                });
            }
        });
        t.start();
    }
}
