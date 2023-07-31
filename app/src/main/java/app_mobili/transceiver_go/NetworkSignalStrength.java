package app_mobili.transceiver_go;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

public class NetworkSignalStrength extends Sensor{
    private final TelephonyManager telephonyManager;
    private int lteSignalStrength = -1; // Default value when not available
    private int umtsSignalStrength = -1; // Default value when not available


    // constructor
    public NetworkSignalStrength(Context context) {
        telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    }

    // once we start monitoring the signal Strength, you can request
    // LTE & UMTS info with the methods get*SignalStrength below
    public void startMonitoringSignalStrength() {
        // Register the PhoneStateListener to start receiving signal strength updates
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }

    public void stopMonitoringSignalStrength() {
        // Unregister the PhoneStateListener to stop receiving signal strength updates
        // LISTEN_NONE is deprecated, but we have to use it since we support API levels of 24+
        // while this got deprecated in API 31
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
    }

    public int getLteSignalStrength() {
        return lteSignalStrength;
    }

    public int getUmtsSignalStrength() {
        return umtsSignalStrength;
    }

    // PhoneStateListener deprecated in API 33, but still gotta use it for our supported API levels
    private final PhoneStateListener phoneStateListener = new PhoneStateListener() {
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            // Get the signal strength values for LTE and UMTS networks
            lteSignalStrength = signalStrength.getLevel(); // For LTE, 0-4 scale
            umtsSignalStrength = signalStrength.getGsmSignalStrength(); // For UMTS
        }
    };
}
