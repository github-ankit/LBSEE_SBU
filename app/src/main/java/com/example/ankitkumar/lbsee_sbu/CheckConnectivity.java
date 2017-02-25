package com.example.ankitkumar.lbsee_sbu;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import static android.content.Context.CONNECTIVITY_SERVICE;

public class CheckConnectivity extends Activity{
    ConnectivityManager connectivityManager;
    NetworkInfo wifiInfo, mobileInfo;



    /**
     * Check for <code>TYPE_WIFI</code> and <code>TYPE_MOBILE</code> connection using <code>isConnected()</code>
     * Checks for generic Exceptions and writes them to logcat as <code>CheckConnectivity Exception</code>.
     * Make sure AndroidManifest.xml has appropriate permissions.
     * @param con Application context
     * @return Boolean
     */
    public boolean checkNow(Context con) {
        // get Connectivity Manager object to check connection
        ConnectivityManager connec
                =(ConnectivityManager)con.getSystemService(CONNECTIVITY_SERVICE);

        // Check for network connections
        if ( connec.getNetworkInfo(0).getState() ==
                android.net.NetworkInfo.State.CONNECTED ||
                connec.getNetworkInfo(0).getState() ==
                        android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() ==
                        android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTED ) {
            //Toast.makeText(this, " Connected ", Toast.LENGTH_LONG).show();
            return true;
        }else if (
                connec.getNetworkInfo(0).getState() ==
                        android.net.NetworkInfo.State.DISCONNECTED ||
                        connec.getNetworkInfo(1).getState() ==
                                android.net.NetworkInfo.State.DISCONNECTED  ) {
           // Toast.makeText(this, " Not Connected ", Toast.LENGTH_LONG).show();
            return false;
        }
        return false;
    }

}

