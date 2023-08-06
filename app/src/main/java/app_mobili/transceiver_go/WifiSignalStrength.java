package app_mobili.transceiver_go;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;

public class WifiSignalStrength extends Sensor{
    private final WifiManager wifiManager;
    private WifiInfo wifiInfo;
    int signalStrength = 1; // Default value (dBm)
    int signalLevel = -1; // Default value

    // initialize and request information, ez! :>
    WifiSignalStrength(Context context) {
        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (wifiManager.isWifiEnabled()) {
            // Get the Wi-Fi connection info
            wifiInfo = wifiManager.getConnectionInfo();
        }
    }
    public int getSignalStrength() {
        if (wifiManager.isWifiEnabled()) {
            // Get the Wi-Fi connection info
            wifiInfo = wifiManager.getConnectionInfo();
            signalStrength = wifiInfo.getRssi();
            return signalStrength;
        }
        // Wifi is disabled, return invalid value
        else return 1;
    }

    public int getSignalLevel() {
        if (wifiManager.isWifiEnabled()) {
            // Get the Wi-Fi connection info
            wifiInfo = wifiManager.getConnectionInfo();
            signalStrength = wifiInfo.getRssi();
            // Calculate signal level with android standards
            signalLevel = WifiManager.calculateSignalLevel(signalStrength, 5);
            return signalLevel;
        }
        // Wifi is disabled, return invalid value
        else return -1;
    }
}
