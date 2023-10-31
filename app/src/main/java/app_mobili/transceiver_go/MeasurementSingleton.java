package app_mobili.transceiver_go;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.PreferenceManager;
import androidx.room.Room;

public class MeasurementSingleton implements NoiseStrength.RecordingListener {
    private static MeasurementSingleton measurementSingleton;
    private final Context context;
    private final CoordinateListener coordinateListener;
    private double longitude;
    private double latitude;
    //stuff for measurement
    private final NoiseStrength noiseStrength;
    private final NetworkSignalStrength networkSignalStrength;
    private final WifiSignalStrength wifiSignalStrength;

    private MeasurementSingleton(Context context, CoordinateListener coordinateListener) {
        this.context = context;
        // coordinate setup
        this.coordinateListener = coordinateListener;

        // measurements setup
        noiseStrength = new NoiseStrength(context);
        noiseStrength.setRecordingListener(this);

        networkSignalStrength = new NetworkSignalStrength(context);

        wifiSignalStrength = new WifiSignalStrength(context);


    }

    // creates the singleton and assigns it
    public static MeasurementSingleton create(Context context, CoordinateListener coordinateListener) {

        if (measurementSingleton == null) {
            measurementSingleton = new MeasurementSingleton(context, coordinateListener);
        }
        return measurementSingleton;
    }

    public void takeWifiMeasurement(MainActivity activity) {
        // update current coordinates
        longitude = coordinateListener.getLongitude();
        latitude = coordinateListener.getLatitude();


        int wifi = wifiSignalStrength.getSignalLevel();
        updateWifiMeasurement(wifi);

        if(activity != null) {
            activity.refreshMaps();
        }

        Toast toast = Toast.makeText(context, R.string.taken_wifi_measurement, Toast.LENGTH_SHORT);
        toast.show();
    }

    private void updateWifiMeasurement(int wifi) {
        new Thread(() -> {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

            String dbname = sharedPreferences.getString("account_name", "squaredb");
            SquareDatabase squaredb = Room.databaseBuilder(context, SquareDatabase.class, dbname).build();

            Square square = new Square(longitude, latitude);
            // returns the square we're in, if it exists
            Square squareInDb = squaredb.getSquareDAO().getSquare(square.getCoordinates());

            // if such database exists, copy everything in the square used to update
            // information, if not update the new one
            if (squareInDb != null) square = squareInDb;

            // actual update
            square.updateWifi(wifi);

            // update the database with updated square
            squaredb.getSquareDAO().upsertSquare(square);

            squaredb.close();

        }).start();
    }

    public void takeNoiseMeasurement(MainActivity activity) {
        //update coordinates
        longitude = coordinateListener.getLongitude();
        latitude = coordinateListener.getLatitude();

        //keep thresholds synced
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        noiseStrength.setClapAmplitude(sharedPreferences.getInt("clap_value", 100));
        noiseStrength.setSilenceAmplitude(sharedPreferences.getInt("silence_value", 0));

        noiseStrength.startRecording();
        // when recording is finished, onRecordingFinished (just below) gets called
        // operations of db updates are done there

        // handler to update the map after 3.5 seconds of measurement started
        // needed here because we cannot pass an activity on callback
        // Create a handler
        Handler handler = new Handler();

        int delayInMillis = 3500;

        // Create a runnable to be executed after the delay
        Runnable updateMap = () -> {
            if(activity != null) {
                activity.refreshMaps();
                Log.d("Refresh", "refresh function called");
            }
        };
        handler.postDelayed(updateMap, delayInMillis);
    }

    // updateNoiseMeasurement equivalent
    @Override
    public void onRecordingFinished(int noise) {
        new Thread(() -> {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

            String dbname = sharedPreferences.getString("account_name", "squaredb");

            SquareDatabase squaredb = Room.databaseBuilder(context, SquareDatabase.class, dbname).build();
            Square square = new Square(longitude, latitude);
            // returns the square we're in, if it exists
            Square squareInDb = squaredb.getSquareDAO().getSquare(square.getCoordinates());

            // if such database exists, copy everything in the square used to update
            // information, if not update the new one
            if (squareInDb != null) square = squareInDb;

            // actual update
            square.updateNoise(noise);

            // update the database with updated square
            squaredb.getSquareDAO().upsertSquare(square);
            squaredb.close();
            // TODO: Update map view to reflect new measurement
        }).start();

        Toast toast = Toast.makeText(context, R.string.taken_noise_measurement, Toast.LENGTH_SHORT);
        toast.show();
    }


    public void takeNetworkMeasurement(MainActivity activity){
        networkSignalStrength.startMonitoringSignalStrength();

        longitude = coordinateListener.getLongitude();
        latitude = coordinateListener.getLatitude();

        int umts = networkSignalStrength.getUmtsSignalStrength();
        int lte = networkSignalStrength.getLteSignalStrength();

        networkSignalStrength.stopMonitoringSignalStrength();

        updateNetworkMeasurement(umts, lte);

        if(activity != null) {
            activity.refreshMaps();
        }

        // notify the user
        Toast toast = Toast.makeText(context, R.string.taken_internet_connection_measurement, Toast.LENGTH_SHORT);
        toast.show();
    }

    private void updateNetworkMeasurement(int umts, int lte) {
        // save measurement
        new Thread(() -> {
            int updatedValue;
            if (umts == 99 || umts == android.telephony.CellInfo.UNAVAILABLE) {
                updatedValue = lte;
            } else updatedValue = umts;

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

            String dbname = sharedPreferences.getString("account_name", "squaredb");
            SquareDatabase squaredb = Room.databaseBuilder(context, SquareDatabase.class, dbname).build();

            Square square = new Square(longitude, latitude);
            // returns the square we're in, if it exists
            Square squareInDb = squaredb.getSquareDAO().getSquare(square.getCoordinates());

            // if such database exists, copy everything in the square used to update
            // information, if not update the new one
            if (squareInDb != null) square = squareInDb;

            // actual update
            square.updateNetwork(updatedValue);

            // update the database with updated square
            squaredb.getSquareDAO().upsertSquare(square);

            squaredb.close();
            // TODO: Update map view to reflect new measurement
        }).start();
    }

}
