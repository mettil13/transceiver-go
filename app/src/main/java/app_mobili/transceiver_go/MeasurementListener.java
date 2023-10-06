package app_mobili.transceiver_go;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.preference.PreferenceManager;
import androidx.room.Room;

public class MeasurementListener implements NoiseStrength.RecordingListener {
    Context context;
    double longitude;
    double latitude;

    MeasurementListener(Context context) {
        this.context = context;
        longitude = 0;
        latitude = 0;
    }

    public void updateCoordinates(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    // updateNoiseMeasurement basically lol
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

    public void updateWifiMeasurement(int wifi) {
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
            // TODO: Update map view to reflect new measurement
        }).start();
    }

    public void updateNetworkMeasurement(int umts, int lte) {

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
