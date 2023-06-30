package app_mobili.transceiver_go;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

        // Load the original image from the drawable resource
        Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.circle);

        // Define the desired width and height for the resized image
        int desiredWidth = 50;
        int desiredHeight = 50;

        // Create a new resized bitmap
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, desiredWidth, desiredHeight, false);

        // draw colored indicator
        ImageView imageView = new ImageView(this);
        // Set the resized bitmap to the ImageView
        imageView.setImageBitmap(resizedBitmap);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 50));
        imageView.setScaleType(ImageView.ScaleType.CENTER);

        // add colour indicator to the layout
        LinearLayout parentLayout = findViewById(R.id.circleLayout);
        parentLayout.addView(imageView);

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

            // this code is just to edit the image accordingly and print it on screen
            Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.circle);
            int desiredWidth = 50;
            int desiredHeight = 50;
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, desiredWidth, desiredHeight, false);
            ColorMatrix colorMatrix = new ColorMatrix();
            if(millibarsOfPressure > 720) // green
                colorMatrix.setRotate(1, 180); // Green
            else if(millibarsOfPressure > 360){ // yellow
                colorMatrix.set(new float[] {
                        1.8f, 0f, 0f, 0f, 0f,  // Red channel
                        1f, 0.6f, 0f, 0f, 0f,  // Green channel
                        0f, 0f, 0f, 0f, 0f,  // Blue channel
                        0f, 0f, 0f, 1f, 0f   // Alpha channel
                });
            }
            else
                colorMatrix.setRotate(0, 180); // Red

            ColorFilter colorFilter = new ColorMatrixColorFilter(colorMatrix);

            // Apply the color filter to the bitmap
            Paint paint = new Paint();
            paint.setColorFilter(colorFilter);

            // Create a canvas and draw the bitmap with the applied color filter
            Canvas canvas = new Canvas(resizedBitmap);
            canvas.drawBitmap(resizedBitmap, 0, 0, paint);

            ImageView imageView = new ImageView(this);
            LinearLayout parentLayout = findViewById(R.id.circleLayout);
            imageView.setImageBitmap(resizedBitmap);
            parentLayout.removeAllViewsInLayout();
            parentLayout.addView(imageView);


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
