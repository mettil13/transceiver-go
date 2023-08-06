package app_mobili.transceiver_go;

import androidx.appcompat.app.AppCompatActivity;

// imports for sensor usages :)
import android.content.Intent;
import android.widget.Button;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    NetworkSignalStrength networkSignalStrength;
    WifiSignalStrength wifiSignalStrength;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); //loads main layout

        networkSignalStrength = new NetworkSignalStrength(this);
        wifiSignalStrength = new WifiSignalStrength(this);


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

        // Class starts to listen to LTE & UMTS signal
        networkSignalStrength.startMonitoringSignalStrength();

        TextView signalView = findViewById(R.id.SignalView);
        signalView.setText(getString(R.string.click)); // click the button!

        Button buttonLTE = findViewById(R.id.buttonLTE);
        buttonLTE.setOnClickListener(v -> signalView.setText(
                "LTE: " + networkSignalStrength.getLteSignalStrength() +
                "\nUMTS: " + networkSignalStrength.getUmtsSignalStrength() + "\n")
        );

        TextView wifiView = findViewById(R.id.WifiView);
        signalView.setText(getString(R.string.click)); // click the button!

        Button buttonWifi = findViewById(R.id.buttonWifi);
        buttonWifi.setOnClickListener(v -> wifiView.setText(
                "Signal Strength (dBm): " + wifiSignalStrength.getSignalStrength() +
                        "\nSignal Level: " + wifiSignalStrength.getSignalLevel() + "/5\n")
        );

        TextView noiseView = findViewById(R.id.noiseView);
        Button buttonNoise = findViewById(R.id.noiseButton);
        NoiseStrength noiseStrength = new NoiseStrength(this);

        buttonNoise.setOnClickListener(v -> {

            noiseView.setText(
                            "Noise level: " + (int) noiseStrength.getNoiseLevel() +
                                    "/100\n");
        }
        );


    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        networkSignalStrength.stopMonitoringSignalStrength();
        super.onDestroy();
    }
}