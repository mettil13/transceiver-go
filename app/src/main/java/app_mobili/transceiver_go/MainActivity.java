package app_mobili.transceiver_go;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;
import androidx.room.Room;


import app_mobili.transceiver_go.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements NoiseStrength.RecordingListener {

    private boolean isAddSelected = false;

    // stuff for coordinates
    CoordinateListener coordinateListener;
    LocationManager lm;
    double longitude;
    double latitude;

    //stuff for measurement
    NoiseStrength noiseStrength;
    MeasurementListener measurementListener;
    NetworkSignalStrength networkSignalStrength;
    WifiSignalStrength wifiSignalStrength;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // retrieving preferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this); // Use getContext() in a Fragment or this in an Activity

        int lastMeasurements = sharedPreferences.getInt("num_kept_measurements", 0);

        boolean automatic_measurements = sharedPreferences.getBoolean("automatic_measurements", false);

        int measure_interval = sharedPreferences.getInt("measure_interval", 10);

        boolean get_auto_wifi = sharedPreferences.getBoolean("measure_wifi", false);
        boolean get_auto_network = sharedPreferences.getBoolean("measure_lte_umps", false);
        boolean get_auto_noise = sharedPreferences.getBoolean("measure_noise", false);

        Log.d("Luizo", "automatic_measurements: " + automatic_measurements + "\n measure_interval: " + measure_interval + "\n get_auto_wifi: " + get_auto_wifi + "\n get_auto_network: " + get_auto_network + "\n get_auto_noise: " + get_auto_noise);


        // coordinate setup
        coordinateListener = new CoordinateListener();
        startListenForCoordinates(coordinateListener);

        // measurements setup
        noiseStrength = new NoiseStrength(this);
        measurementListener = new MeasurementListener(this);
        noiseStrength.setRecordingListener(measurementListener);

        networkSignalStrength = new NetworkSignalStrength(this);
        wifiSignalStrength = new WifiSignalStrength(this);

        // fragment setup
        Fragment mainMap = new FragmentMainMap();
        Fragment gameMap = new FragmentGameMap();
        Fragment somethingElse = new FragmentSomethingElse();
        Fragment account = new FragmentAccountSettings();
        Fragment settings = new FragmentSettings();

        ActivityMainBinding binding;
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        // TODO move these checks in order to happen when activating automatic measurements
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            // Request the FOREGROUND_SERVICE permission at runtime
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.FOREGROUND_SERVICE}, 444);
            }
        }
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    333);
        }


        // service setup
        Intent serviceIntent = new Intent(this, MeasurementService.class);
        startService(serviceIntent);

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
                case R.id.accountSettingsButton:
                    replaceFragment(R.id.fragmentContainer, account);
                    break;
                case R.id.settingsButton:
                    replaceFragment(R.id.fragmentContainer, settings);
                    break;
            }
            return true;
        });
        replaceFragment(R.id.fragmentContainer, mainMap);


        setUpMeasurementButtons(binding);


        new Thread(() -> {
            this.deleteDatabase("squaredb");
            // setting the database
            SquareDatabase squaredb = Room.databaseBuilder(this, SquareDatabase.class, "squaredb").addMigrations(SquareDatabase.migration).build();

            //Square s1;
            {
                Square square = new Square(1, 1);
                square.setNoise(100);
                squaredb.getSquareDAO().upsertSquare(square);
            }
            {
                Square square = new Square(0, 0);
                square.setNoise(5);
                squaredb.getSquareDAO().upsertSquare(square);
            }
            {
                Square square = new Square(10, 10);
                square.setNoise(100);
                squaredb.getSquareDAO().upsertSquare(square);
            }
            {
                Square square = new Square(10, 5);
                square.setNoise(100);
                squaredb.getSquareDAO().upsertSquare(square);
            }
            {
                Square square = new Square(10, 15);
                square.setNoise(10);
                squaredb.getSquareDAO().upsertSquare(square);
            }
            {
                Square square = new Square(20, 0);
                square.setNoise(10);
                squaredb.getSquareDAO().upsertSquare(square);
            }
            {
                Square square = new Square(1, -10);
                square.setNoise(10);
                squaredb.getSquareDAO().upsertSquare(square);
            }
            {
                Square square = new Square(-15, 5);
                square.setNoise(100);
                squaredb.getSquareDAO().upsertSquare(square);
            }
            {
                Square square = new Square(-5, -5);
                square.setNoise(100);
                squaredb.getSquareDAO().upsertSquare(square);
            }
            {
                Square square = new Square(-5, 10);
                square.setNoise(100);
                squaredb.getSquareDAO().upsertSquare(square);
            }
            {
                Square square = new Square(11.4, 44.500);
                square.setNoise(100);
                squaredb.getSquareDAO().upsertSquare(square);
            }
            {
                Square square = new Square(11.4, 44.501);
                square.setNoise(50);
                squaredb.getSquareDAO().upsertSquare(square);
            }
            {
                Square square = new Square(11.393, 44.472);
                square.setNoise(50);
                squaredb.getSquareDAO().upsertSquare(square);
            }
            {
                Square square = new Square(11.393, 44.473);
                square.setNoise(10);
                squaredb.getSquareDAO().upsertSquare(square);
            }
            {
                Square square = new Square(11.394963, 44.473466);
                square.setNoise(100);
                squaredb.getSquareDAO().upsertSquare(square);
            }
            squaredb.close();

        }).start();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, MeasurementService.class));
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
            // update current coordinates
            longitude = coordinateListener.getLongitude();
            latitude = coordinateListener.getLatitude();
            measurementListener.updateCoordinates(longitude, latitude);

            // get wifi signal
            int wifi = wifiSignalStrength.getSignalLevel();
            measurementListener.updateWifiMeasurement(wifi);

            // notify the user
            Toast toast = Toast.makeText(view.getContext(), R.string.taken_wifi_measurement, Toast.LENGTH_SHORT);
            toast.show();
        });
        binding.newInternetConnectionMeasurementButton.setOnClickListener(view -> {
            networkSignalStrength.startMonitoringSignalStrength();

            longitude = coordinateListener.getLongitude();
            latitude = coordinateListener.getLatitude();
            measurementListener.updateCoordinates(longitude, latitude);

            int umts = networkSignalStrength.getUmtsSignalStrength();
            int lte = networkSignalStrength.getLteSignalStrength();

            networkSignalStrength.stopMonitoringSignalStrength();

            measurementListener.updateNetworkMeasurement(umts, lte);

            Toast toast = Toast.makeText(view.getContext(), R.string.taken_internet_connection_measurement, Toast.LENGTH_SHORT);
            toast.show();

        });
        binding.newNoiseMeasurementButton.setOnClickListener(view -> {
            longitude = coordinateListener.getLongitude();
            latitude = coordinateListener.getLatitude();
            measurementListener.updateCoordinates(longitude, latitude);
            noiseStrength.startRecording();
            // when recording is finished, onRecordingFinished over "MeasurementListener" gets called
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

    public void startListenForCoordinates(CoordinateListener coordinateListener) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    808);
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    809);
            return;
        }

        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(true);

        String provider = lm.getBestProvider(criteria, true);
        if (provider != null) {
            lm.requestLocationUpdates(provider, 3000L, 20L, coordinateListener);
        }
    }
}