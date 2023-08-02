package app_mobili.transceiver_go;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;

public class WifiSignalStrength extends Sensor{
    private final WifiManager wifiManager;
    private final Handler handler;
    private final Runnable updateSignalRunnable;
    int signalStrength = 1; // Default value (dBm)
    int signalLevel = -1; // Default value


    WifiSignalStrength(Context context){
        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // RSSI doesn't work as expected on my OnePlus Nord, so i'll try using an handler
        // and updating the signal manually every x seconds
        handler = new Handler();

        // is it alright for this to be in the constructor?
        updateSignalRunnable = new Runnable() {
            @Override
            public void run() {
                updateWifiSignal();
                handler.postDelayed(this, 500); // Repeat every 0.5 seconds
            }
        };
    }

    public void startUpdatingSignal() {
        // Start updating the Wi-Fi signal strength
        handler.postDelayed(updateSignalRunnable, 1000); // Start after 1 second
    }


    public void stopUpdatingSignal() {
        // Stop updating the Wi-Fi signal strength
        handler.removeCallbacks(updateSignalRunnable);
    }

    private void updateWifiSignal() {
        // Check if Wi-Fi is enabled
        if (wifiManager.isWifiEnabled()) {
            // Get the Wi-Fi connection info
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();

            // Get the signal strength in dBm
            signalStrength = wifiInfo.getRssi();

            // Get the signal level as a human-readable string
            signalLevel = WifiManager.calculateSignalLevel(signalStrength, 5);
        } else {
            signalStrength = 1; // dBm
            signalLevel = -1;
        }
    }

    // Should i handle a special response when in default value?
    public int getSignalStrength() {
        return signalStrength;
    }

    public int getSignalLevel() {
        return signalLevel;
    }
}
