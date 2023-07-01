package app_mobili.transceiver_go;

import androidx.appcompat.app.AppCompatActivity;

// imports for sensor usages :)
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.wifi.WifiInfo;
import android.os.Handler;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    //variable for the list of sensors
    SensorManager smm;
    List<Sensor> sensor;
    ListView lv;

    private TextView wifiSignalTextView;
    private WifiManager wifiManager;

    private Handler handler;
    private Runnable updateSignalRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); //loads main layout

        // getting the list of sensors :)
        smm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        lv = (ListView) findViewById (R.id.listView1);
        sensor = smm.getSensorList(Sensor.TYPE_ALL);
        lv.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,  sensor));

        // button that switches view to SensorView :D
        Button myButton = findViewById(R.id.toSensorView);
        myButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SensorActivity.class);
            startActivity(intent);
        });

        Button soundButton = findViewById(R.id.toSoundView);
        soundButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SoundActivity.class);
            startActivity(intent);
        });

        wifiSignalTextView = findViewById(R.id.wifiSignalTextView);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // RSSI doesn't work as expected on my OnePlus Nord, so i'll try using an handler
        handler = new Handler();

        // Create a periodic Runnable to update the Wi-Fi signal strength
        updateSignalRunnable = new Runnable() {
            @Override
            public void run() {
                updateWifiSignal();
                handler.postDelayed(this, 500); // Repeat every 0.5 second (adjust as needed)
            }
        };

    }

    @Override
    protected void onResume() {
        super.onResume();
        startUpdatingSignal();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopUpdatingSignal();
    }

    private void startUpdatingSignal() {
        // Start updating the Wi-Fi signal strength
        handler.postDelayed(updateSignalRunnable, 1000); // Start after 1 second (adjust as needed)
    }

    private void stopUpdatingSignal() {
        // Stop updating the Wi-Fi signal strength
        handler.removeCallbacks(updateSignalRunnable);
    }

    private void updateWifiSignal() {
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
    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Unregister the BroadcastReceiver
        // unregisterReceiver(wifiReceiver);
    }
}