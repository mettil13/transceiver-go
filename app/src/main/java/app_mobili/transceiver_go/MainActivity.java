package app_mobili.transceiver_go;

import androidx.appcompat.app.AppCompatActivity;

// imports for sensor usages :)
import java.util.List;

import android.content.Context;
import android.content.Intent;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); //loads main layout

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
        super.onDestroy();
    }
}