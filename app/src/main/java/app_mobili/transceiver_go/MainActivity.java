package app_mobili.transceiver_go;


import android.Manifest;
import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;
import androidx.room.Room;


import app_mobili.transceiver_go.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements NoiseStrength.RecordingListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private boolean isAddSelected = false;

    MeasurementSingleton measurementSingleton;

    CoordinateListener coordinateListener;

    TextView coinsNumber;

    Fragment mainMap;
    Fragment gameMap;
    Fragment account;
    Fragment settings;

    private ActivityResultLauncher<String> requestPermissionForCoordinates;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // fragment setup
        mainMap = new FragmentMainMap();
        gameMap = new FragmentGameMap();
        account = new FragmentAccountSettings();
        settings = new FragmentSettings();

        ActivityMainBinding binding;
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // coordinate setup
        coordinateListener = new CoordinateListener() {
            // without this execution on api 24 would result in a crash
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }
        };
        requestPermissionForCoordinates = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                startListenForCoordinates(coordinateListener);
            }
        });

        startListenForCoordinates(coordinateListener);

        // setting up measurement
        measurementSingleton = MeasurementSingleton.create(getApplicationContext(), coordinateListener);

        setUpMeasurementButtons(binding);

        //service setup at startup (if enabled)
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean automaticMeasurements = sharedPreferences.getBoolean("automatic_measurements", false);
        if (automaticMeasurements) {
            // service setup
            Intent serviceIntent = new Intent(this, MeasurementService.class);
            startService(serviceIntent);
        }


        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.mapButton:
                    replaceFragment(R.id.fragmentContainer, mainMap);
                    break;
                case R.id.gameButton:
                    replaceFragment(R.id.fragmentContainer, gameMap);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.coins, menu);

        View layout = menu.getItem(0).getActionView();
        assert layout != null;
        coinsNumber = layout.findViewById(R.id.coins_number);
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
        updateCoinsMenu();
        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals("coins")) {
            if (coinsNumber != null) { // if the menu has been initialized
                updateCoinsMenu();
            }
        }
    }

    private void updateCoinsMenu() {
        String value = String.valueOf(PreferenceManager.getDefaultSharedPreferences(this).getInt("coins", 0));
        coinsNumber.setText(value);
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
        binding.newWiFiMeasurementButton.setOnClickListener(view -> measurementSingleton.takeWifiMeasurement(this));
        binding.newInternetConnectionMeasurementButton.setOnClickListener(view -> measurementSingleton.takeNetworkMeasurement(this));
        binding.newNoiseMeasurementButton.setOnClickListener(view -> measurementSingleton.takeNoiseMeasurement(this));

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

    public void refreshMaps() {
        try {
            ((FragmentMainMap) mainMap).onCameraIdle();
        } catch (Exception ignored) {
        }

        try {
            ((FragmentGameMap) gameMap).onCameraIdle();
        } catch (Exception ignored) {
        }
    }

    public void startListenForCoordinates(CoordinateListener coordinateListener) {

        boolean location_permission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean coarse_permission = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (!location_permission || !coarse_permission) {
            if (!location_permission)
                requestPermissionForCoordinates.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            if (!coarse_permission)
                requestPermissionForCoordinates.launch(Manifest.permission.ACCESS_COARSE_LOCATION);
            return;
        }

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //set criteria to save battery during getCoordinates
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