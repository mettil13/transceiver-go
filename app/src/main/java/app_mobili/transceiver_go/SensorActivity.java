package app_mobili.transceiver_go;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;


public class SensorActivity extends Activity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor pressure;

    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensors);

        // Get an instance of the sensor service, and use that to get an instance of
        // a particular sensor.
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        pressure = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        TextView PressureView = findViewById(R.id.PressureView);
        if(pressure != null) {
            // formatting info to print :)

            String sensorInfo = "Name: " + pressure.getName() + "\n"
                    + "Type: " + pressure.getType() + "\n"
                    + "Vendor: " + pressure.getVendor() + "\n"
                    + "Version: " + pressure.getVersion();

            PressureView.setText(sensorInfo);
        }
        else
            PressureView.setText("no info retrieved :(");


    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        float millibarsOfPressure = event.values[0];
        TextView PressureView = findViewById(R.id.PressureView);
        if(pressure != null) {
            String sensorInfo = "Name: " + pressure.getName() + "\n"
                    + "Type: " + pressure.getType() + "\n"
                    + "Vendor: " + pressure.getVendor() + "\n"
                    + "Version: " + pressure.getVersion() + "\n"
                    + "Value: " + millibarsOfPressure;
            PressureView.setText(sensorInfo);
        }
        else
            PressureView.setText("no info retrieved :(");
    }

    @Override
    protected void onResume() {
        // Register a listener for the sensor.
        super.onResume();
        sensorManager.registerListener(this, pressure, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        // Be sure to unregister the sensor when the activity pauses.
        super.onPause();
        sensorManager.unregisterListener(this);
    }
}
