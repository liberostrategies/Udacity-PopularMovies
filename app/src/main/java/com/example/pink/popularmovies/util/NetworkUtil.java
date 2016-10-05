package com.example.pink.popularmovies.util;

import android.content.Context;
import android.net.ConnectivityManager;

/**
 * Utility methods for network connnectivity.
 */

public class NetworkUtil {
    public static boolean isOnline(Context context) {

        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null &&
                cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }
}
