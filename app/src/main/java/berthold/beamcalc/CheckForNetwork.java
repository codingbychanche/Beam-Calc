package berthold.beamcalc;

/*
 * CheckForNetwork.java
 *
 * Created by Berthold Fritz
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License:
 * https://creativecommons.org/licenses/by-nc-sa/4.0/
 *
 * Last modified 03.03.22 08:45
 */
import android.content.Context;
import android.net.ConnectivityManager;

//@rem: Shows how to check if there is an active network connected to this device@@
/**
 * Checks whether there is an active network connected or not.
 *
 * @returns True if a network is connected, false if not....
 */
public class CheckForNetwork {
    // ToDo Check if this works......
    public static boolean isNetworkAvailable(final Context context) {
        return ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo() != null;
    }
}
//@@
