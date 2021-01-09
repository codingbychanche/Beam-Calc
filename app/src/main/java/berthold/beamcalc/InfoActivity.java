package berthold.beamcalc;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;

/**
 * Shows app info...
 *
 */
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
        final TextView versionNameTagView;
        versionNameTagView=(TextView)findViewById(R.id.version_tag);
        webView=(WebView)findViewById(R.id.browser);
        progress=(ProgressBar)findViewById(R.id.html_load_progress);

        // @rem:Get current locale (determine language from Androids settings@@
        //final Locale current=getResources().getConfiguration().locale;
        final String  current=getResources().getConfiguration().locale.getLanguage();
        Log.v("LOCALE","Country:"+current);

        // @rem: Shows how to retrieve the version- name tag from the 'build.gradle'- file@@
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            versionNameTagView.setText("Version:"+version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            versionNameTagView.setText("-");
        }
        //@@

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
