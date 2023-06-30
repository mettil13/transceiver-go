package app_mobili.transceiver_go;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.widget.TextView;

public class WifiReceiver extends BroadcastReceiver {
    private TextView wifiSignalTextView;

    public WifiReceiver(TextView wifiSignalTextView) {
        this.wifiSignalTextView = wifiSignalTextView;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        System.out.println("call");

        if (WifiManager.RSSI_CHANGED_ACTION.equals(action) || WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

            // Check if Wi-Fi is enabled
            if (wifiManager.isWifiEnabled()) {
                // Get the Wi-Fi connection info
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();

                // Get the signal strength in dBm
                int signalStrength = wifiInfo.getRssi();

                // Get the signal level as a human-readable string
                int signalLevel = WifiManager.calculateSignalLevel(signalStrength, 5);

                // Update the Wi-Fi signal information in the TextView
                wifiSignalTextView.setText("Signal Level: " + signalLevel + "/5\n" + "Signal Strength: "+ signalStrength +" dBm");
            } else {
                wifiSignalTextView.setText("Wi-Fi is disabled");
            }
        }
    }
}
