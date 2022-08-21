package berthold.beamcalc;

import android.os.AsyncTask;
import android.os.StrictMode;
import android.util.Log;

import org.jsoup.Jsoup;
import java.io.IOException;

/**
 * Version checker.
 *
 * Retrieves the version of this app from the Google Play- Store.
 *
 * Source: https://stackoverflow.com/questions/34309564/how-to-get-app-market-version-information-from-google-play-store
 */
public class VersionChecker extends AsyncTask<String, String, String> {

    String newVersion;

    @Override
    protected String doInBackground(String... params) {

        try {
            newVersion = Jsoup.connect("https://play.google.com/store/apps/details?id=" + "berthold.beamcalc" + "&hl=de")
                    .timeout(30000)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .referrer("http://www.google.com")
                    .get()
                    .select("div.hAyfc:nth-child(4) > span:nth-child(2) > div:nth-child(1) > span:nth-child(1)")
                    .first()
                    .ownText();
        } catch (Exception e) {
           Log.v("VERSION_CHECKER:",e.toString());
           newVersion="-";
        }
        return newVersion;
    }
}
