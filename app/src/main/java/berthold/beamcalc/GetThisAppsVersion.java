package berthold.beamcalc;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * A simple version checker.
 *
 * Retrieves the version- tag from the app's manifest.
 */
public class GetThisAppsVersion {

    public static String thisVersion(Context context){
        // @rem: Shows how to retrieve the version- name tag from the 'build.gradle'- file@@
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionName;

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
           return ("-");
        }
        //@@
    }
}
