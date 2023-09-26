package app_mobili.transceiver_go;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;
import androidx.room.Room;

import app_mobili.transceiver_go.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements NoiseStrength.RecordingListener {

    private boolean isAddSelected = false;
    SquareDatabase squaredb;

    // stuff for coordinates
    CoordinateListener coordinateListener;
    LocationManager lm;
    double longitude;
    double latitude;

    //stuff for measurement
    NoiseStrength noiseStrength;
    NoiseListener noiseListener;
    NetworkSignalStrength networkSignalStrength;
    WifiSignalStrength wifiSignalStrength;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // retrieving preferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this); // Use getContext() in a Fragment or this in an Activity

        int lastMeasurements = sharedPreferences.getInt("num_kept_measurements",0);
        Log.println(Log.ASSERT,"luizo", lastMeasurements+"");

        // coordinate setup
        coordinateListener = new CoordinateListener();
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        startListenForCoordinates();

        // measurements setup
        noiseStrength = new NoiseStrength(this);
        noiseListener = new NoiseListener(this);
        noiseStrength.setRecordingListener(noiseListener);

        networkSignalStrength = new NetworkSignalStrength(this);
        wifiSignalStrength = new WifiSignalStrength(this);


        // fragment setup
        Fragment mainMap = new FragmentMainMap();
        Fragment gameMap = new FragmentGameMap();
        Fragment somethingElse = new FragmentSomethingElse();
        Fragment settings = new FragmentSettings();

        ActivityMainBinding binding;
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.mapButton:
                    replaceFragment(R.id.fragmentContainer, mainMap);
                    break;
                case R.id.gameButton:
                    replaceFragment(R.id.fragmentContainer, gameMap);
                    break;
                case R.id.somethingElseButton:
                    replaceFragment(R.id.fragmentContainer, somethingElse);
                    break;
                case R.id.settingsButton:
                    replaceFragment(R.id.fragmentContainer, settings);
            }
            return true;
        });
        replaceFragment(R.id.fragmentContainer, mainMap);

        setUpMeasurementButtons(binding);

        this.deleteDatabase("squaredb");
        // setting the database
        squaredb = Room.databaseBuilder(this, SquareDatabase.class, "squaredb").addMigrations(SquareDatabase.migration).build();

        //secondb = Room.databaseBuilder(this, SquareDatabase.class, "second").addMigrations(SquareDatabase.migration).build();

        new Thread(() -> {
            //Square s1;
            //secondb.getSquareDAO().upsertSquare(s1);
            squaredb.getSquareDAO().upsertSquare(new Square(1, 1, 0.001));
            squaredb.getSquareDAO().upsertSquare(new Square(0, 0, 0.001));
            squaredb.getSquareDAO().upsertSquare(new Square(10, 10, 0.001));
            squaredb.getSquareDAO().upsertSquare(new Square(10, 5, 0.001));
            squaredb.getSquareDAO().upsertSquare(new Square(10, 15, 0.001));
            squaredb.getSquareDAO().upsertSquare(new Square(20, 0, 0.001));
            squaredb.getSquareDAO().upsertSquare(new Square(1, -10, 0.001));
            squaredb.getSquareDAO().upsertSquare(new Square(-15, 5, 0.001));
            squaredb.getSquareDAO().upsertSquare(new Square(-5, -5, 0.001));
            squaredb.getSquareDAO().upsertSquare(new Square(11.4, 44.500, 0.001));
            squaredb.getSquareDAO().upsertSquare(new Square(11.4, 44.501, 0.001));
            squaredb.getSquareDAO().upsertSquare(new Square(11.393, 44.472, 0.001));
            squaredb.getSquareDAO().upsertSquare(new Square(11.393, 44.473, 0.001));
            squaredb.getSquareDAO().upsertSquare(new Square(11.394963, 44.473466, 0.001));
            //s1 = squaredb.getSquareDAO().getYourSquare(52, 52, 5);

            //System.out.println(s1.toString());


        }).start();

    }


    private void replaceFragment(int containerId, Fragment newFragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setReorderingAllowed(true);
        fragmentTransaction.replace(containerId, newFragment);
        fragmentTransaction.commit();
    }

    private void setUpMeasurementButtons(ActivityMainBinding binding) {
        // animations initialization
        Animation rotateOpen = AnimationUtils.loadAnimation(this, R.anim.rotate_open_animation);
        Animation rotateClose = AnimationUtils.loadAnimation(this, R.anim.rotate_close_animation);
        Animation fromBottomShow = AnimationUtils.loadAnimation(this, R.anim.from_bottom_show_animation);
        Animation toBottomHide = AnimationUtils.loadAnimation(this, R.anim.to_bottom_hide_animation);

        // on click
        binding.newMeasurementButton.setOnClickListener(view -> {
            isAddSelected = !isAddSelected;
            if (isAddSelected) {
                // animations
                binding.newMeasurementButton.startAnimation(rotateOpen);
                binding.newWiFiMeasurementButton.startAnimation(fromBottomShow);
                binding.newInternetConnectionMeasurementButton.startAnimation(fromBottomShow);
                binding.newNoiseMeasurementButton.startAnimation(fromBottomShow);
                // clickable
                binding.newWiFiMeasurementButton.setClickable(true);
                binding.newInternetConnectionMeasurementButton.setClickable(true);
                binding.newNoiseMeasurementButton.setClickable(true);
                // long clickable
                binding.newWiFiMeasurementButton.setLongClickable(true);
                binding.newInternetConnectionMeasurementButton.setLongClickable(true);
                binding.newNoiseMeasurementButton.setLongClickable(true);
            } else {
                binding.newMeasurementButton.startAnimation(rotateClose);
                binding.newWiFiMeasurementButton.startAnimation(toBottomHide);
                binding.newInternetConnectionMeasurementButton.startAnimation(toBottomHide);
                binding.newNoiseMeasurementButton.startAnimation(toBottomHide);

                binding.newWiFiMeasurementButton.setClickable(false);
                binding.newInternetConnectionMeasurementButton.setClickable(false);
                binding.newNoiseMeasurementButton.setClickable(false);

                binding.newWiFiMeasurementButton.setLongClickable(false);
                binding.newInternetConnectionMeasurementButton.setLongClickable(false);
                binding.newNoiseMeasurementButton.setLongClickable(false);
            }
        });
        binding.newWiFiMeasurementButton.setOnClickListener(view -> {
            longitude = coordinateListener.getLongitude();
            latitude = coordinateListener.getLatitude();

            int wifi = wifiSignalStrength.getSignalLevel();

            new Thread(() -> {
                Square square = new Square(longitude,latitude, 0.001);
                // returns the square we're in, if it exists
                Square squareInDb = squaredb.getSquareDAO().getSquare(square.getCoordinates());

                // if such database exists, copy everything in the square used to update
                // information, if not update the new one
                if(squareInDb != null) square = squareInDb;

                // actual update
                square.updateWifi(wifi);

                // update the database with updated square
                squaredb.getSquareDAO().upsertSquare(square);



                // TODO: Update map view to reflect new measurement
            }).start();
            Toast toast = Toast.makeText(view.getContext(), R.string.new_wifi_measurement, Toast.LENGTH_SHORT);
            toast.show();
        });
        binding.newInternetConnectionMeasurementButton.setOnClickListener(view -> {
            networkSignalStrength.startMonitoringSignalStrength();

            longitude = coordinateListener.getLongitude();
            latitude = coordinateListener.getLatitude();
            int umts = networkSignalStrength.getUmtsSignalStrength();
            int lte = networkSignalStrength.getLteSignalStrength();

            networkSignalStrength.stopMonitoringSignalStrength();
            // if umts has unused value
            if (umts == 99 || umts == android.telephony.CellInfo.UNAVAILABLE) {
                // save LTE measurement
                new Thread(() -> {
                    Square square = new Square(longitude,latitude, 0.001);
                    // returns the square we're in, if it exists
                    Square squareInDb = squaredb.getSquareDAO().getSquare(square.getCoordinates());

                    // if such database exists, copy everything in the square used to update
                    // information, if not update the new one
                    if(squareInDb != null) square = squareInDb;

                    // actual update
                    square.updateNetwork(lte);

                    // update the database with updated square
                    squaredb.getSquareDAO().upsertSquare(square);

                    // TODO: Update map view to reflect new measurement
                }).start();
            }
            else {
                // save UMTS measurement
                new Thread(() -> {
                    Square square = new Square(longitude,latitude, 0.001);
                    // returns the square we're in, if it exists
                    Square squareInDb = squaredb.getSquareDAO().getSquare(square.getCoordinates());

                    // if such database exists, copy everything in the square used to update
                    // information, if not update the new one
                    if(squareInDb != null) square = squareInDb;

                    // actual update
                    square.updateNetwork(umts);

                    // update the database with updated square
                    squaredb.getSquareDAO().upsertSquare(square);

                    // TODO: Update map view to reflect new measurement
                }).start();

            }
            Toast toast = Toast.makeText(view.getContext(), R.string.new_internet_connection_measurement, Toast.LENGTH_SHORT);
            toast.show();

        });
        binding.newNoiseMeasurementButton.setOnClickListener(view -> {
            longitude = coordinateListener.getLongitude();
            latitude = coordinateListener.getLatitude();
            noiseListener.updateCoordinates(longitude,latitude);
            noiseStrength.startRecording();
            // when recording is finished, onRecordingFinished over "NoiseListener" gets called
            // operations of db updates are done there
        });

        // on long click
        binding.newMeasurementButton.setOnLongClickListener(view -> {
            Toast toast = Toast.makeText(view.getContext(), R.string.new_measurement, Toast.LENGTH_SHORT);
            toast.show();
            return true;
        });
        binding.newWiFiMeasurementButton.setOnLongClickListener(view -> {
            Toast toast = Toast.makeText(view.getContext(), R.string.new_wifi_measurement, Toast.LENGTH_SHORT);
            toast.show();
            return true;
        });
        binding.newInternetConnectionMeasurementButton.setOnLongClickListener(view -> {
            Toast toast = Toast.makeText(view.getContext(), R.string.new_internet_connection_measurement, Toast.LENGTH_SHORT);
            toast.show();
            return true;
        });
        binding.newNoiseMeasurementButton.setOnLongClickListener(view -> {
            Toast toast = Toast.makeText(view.getContext(), R.string.new_noise_measurement, Toast.LENGTH_SHORT);
            toast.show();
            return true;
        });
    }


    public void startListenForCoordinates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    808);
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    809);
            return;
        }
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000L, 10F, coordinateListener);
    }
}