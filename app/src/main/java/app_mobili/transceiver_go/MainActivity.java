package app_mobili.transceiver_go;

import androidx.appcompat.app.AppCompatActivity;

// imports for sensor usages :)
import android.content.Intent;
import android.widget.Button;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    NetworkSignalStrength networkSignalStrength;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); //loads main layout

        networkSignalStrength = new NetworkSignalStrength(this);


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