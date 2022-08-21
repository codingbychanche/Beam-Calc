package berthold.beamcalc;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;

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
 */
public class InfoActivity extends AppCompatActivity {

    // Info
    private static String tag;
    private String currentVersion = "-";

    // Filesystem
    private BufferedReader bufferedReader;

    // Html
    private StringBuilder htmlSite;
    private WebView webView;
    private TextView updateInfoView;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        // UI
        final Handler handler = new Handler();
        webView = (WebView) findViewById(R.id.browser);
        updateInfoView = findViewById(R.id.info_new_version_available);
        progress = (ProgressBar) findViewById(R.id.html_load_progress);

        // @rem:Get current locale (determine language from Androids settings@@
        //final Locale current=getResources().getConfiguration().locale;
        final String current = getResources().getConfiguration().locale.getLanguage();
        Log.v("LOCALE", "Country:" + current);

        // @rem: Shows how to retrieve the version- name tag from the 'build.gradle'- file@@
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            currentVersion = pInfo.versionName;
            getSupportActionBar().setSubtitle("Version:" + currentVersion);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            getSupportActionBar().setSubtitle("Version: -");
        }
        //@@

        //
        // If an active network is connected to this device, get current version of this apps
        // published variant and check if the current version is installed.
        //
        progress.setVisibility(View.VISIBLE);

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {

                //
                // Active network available?
                //
                //
                // The following statement is true, when network is available, no ,matter if it is switched on or off!!!
                // This statement is noo good if one wants to check if a network connection is possible....
                if (CheckForNetwork.isNetworkAvailable(getApplicationContext())) {
                    final String latestVersionInGooglePlay = getAppVersionfromGooglePlay(getApplicationContext());

                    if (latestVersionInGooglePlay != "-") {
                        if (latestVersionInGooglePlay.equals(currentVersion)) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    //updateInfoView.setText(getResources().getText(R.string.version_info_is_latest_version));
                                    updateInfoView.setText(HtmlCompat.fromHtml(getResources().getText(R.string.version_info_ok) + "", 0));
                                }
                            });

                            // OK, could connect to network, could connect to google plays store listing of the app, get version.
                        } else {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    updateInfoView.setText(HtmlCompat.fromHtml(getResources().getText(R.string.version_info_update_available) + latestVersionInGooglePlay, 0));
                                }
                            });
                        }

                    } else
                        // Network was available but, could not retrieve version info from google plays store listing of this app.
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                updateInfoView.setText(HtmlCompat.fromHtml(getResources().getText(R.string.no_version_info_available) + "", 0));
                            }
                        });
                } else
                    // No network connection available or network disabled on device.
                    handler.post(new Runnable() {
                    @Override
                    public void run() {
                        updateInfoView.setText(HtmlCompat.fromHtml(getResources().getText(R.string.no_network) + "", 0));
                    }
                });
            }
        });
        t.start();

        Thread t2=new Thread(new Runnable() {
            @Override
            public void run() {
                //
                // Load HTML
                //
                try {
                    htmlSite = new StringBuilder();

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

                } catch (IOException io) {
                    Log.v("Info", io.toString());
                }

                // Wait a vew millisec's to enable the main UI thread
                // to react.
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                }

                // Show
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        progress.setVisibility(View.GONE);
                        webView.loadData(htmlSite.toString(), "text/html", null);
                    }
                });
            }
        });
        t2.start();
    }

    /**
     * Returns the version from the app's Google Play store listing...
     *
     * @param c
     * @return A String containing the version tag or, if errors: - char.
     */
    public String getAppVersionfromGooglePlay(Context c) {
        String latest;

        VersionChecker vc = new VersionChecker();

        try {
            latest = vc.execute().get();
        } catch (Exception e) {
           Log.v(tag,e.toString());
           latest="-";
        }
        return latest;
    }
}
