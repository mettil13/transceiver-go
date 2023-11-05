package app_mobili.transceiver_go;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.location.Criteria;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
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
    Fragment somethingElse;
    Fragment account;
    Fragment settings;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // fragment setup
        mainMap = new FragmentMainMap();
        gameMap = new FragmentGameMap();
        somethingElse = new FragmentSomethingElse();
        account = new FragmentAccountSettings();
        settings = new FragmentSettings();

        ActivityMainBinding binding;
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // coordinate setup
        coordinateListener = new CoordinateListener();
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
/*
            SquareDatabase aa = Room.databaseBuilder(this, SquareDatabase.class, "squaredb").addMigrations(SquareDatabase.migration).build();
            SquareDatabase bb = Room.databaseBuilder(this, SquareDatabase.class, "user").addMigrations(SquareDatabase.migration).build();
            bb.getSquareDAO().getSquare("a");

 */

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
        coinsNumber = layout.findViewById(R.id.coins_number);
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
        updateCoinsMenu();
        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals("coins")) {
            if(coinsNumber != null){ // if the menu has been initialized
                updateCoinsMenu();
            }
        }
    }

    private void updateCoinsMenu(){
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

        // check if i have permission
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    808);
            ActivityCompat.requestPermissions((Activity) this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    809);
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