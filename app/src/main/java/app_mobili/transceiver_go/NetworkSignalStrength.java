package app_mobili.transceiver_go;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;


public class NetworkSignalStrength extends Sensor{
    private Context context;
    private TelephonyManager telephonyManager;

    public NetworkSignalStrength(Context context) {
        this.context = context;
        telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    }

    public void startMonitoringSignalStrength() {
        // Register the PhoneStateListener to start receiving signal strength updates
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }

    public void stopMonitoringSignalStrength() {
        // Unregister the PhoneStateListener to stop receiving signal strength updates
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
    }

    private final PhoneStateListener phoneStateListener = new PhoneStateListener() {
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            // Get the signal strength values for LTE and UMTS networks
            int lteSignalStrength = signalStrength.getLevel(); // For LTE
            int umtsSignalStrength = signalStrength.getGsmSignalStrength(); // For UMTS


        }
    };
}
}
