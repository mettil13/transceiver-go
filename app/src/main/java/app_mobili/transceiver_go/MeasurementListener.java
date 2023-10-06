package app_mobili.transceiver_go;

import android.content.Context;
import android.widget.Toast;

import androidx.room.Room;

public class MeasurementListener implements NoiseStrength.RecordingListener {

    SquareDatabase squaredb;
    Context context;

    final static double SIDE_LENGTH = 0.001;

    double longitude;
    double latitude;

    MeasurementListener(Context context){
        this.context = context;
        longitude = 0;
        latitude = 0;
    }

    public void updateCoordinates(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    @Override
    public void onRecordingFinished(int noise) {
        new Thread(() -> {
            squaredb = Room.databaseBuilder(context, SquareDatabase.class, "squaredb").build();
            Square square = new Square(longitude,latitude);
            // returns the square we're in, if it exists
            Square squareInDb = squaredb.getSquareDAO().getSquare(square.getCoordinates());

            // if such database exists, copy everything in the square used to update
            // information, if not update the new one
            if(squareInDb != null) square = squareInDb;

            // actual update
            square.updateNoise(noise);

            // update the database with updated square
            squaredb.getSquareDAO().upsertSquare(square);
            squaredb.close();
            // TODO: Update map view to reflect new measurement
        }).start();

        Toast toast = Toast.makeText(context, R.string.new_noise_measurement, Toast.LENGTH_SHORT);
        toast.show();
    }
}
