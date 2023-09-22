package app_mobili.transceiver_go;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;
import androidx.room.Room;

import app_mobili.transceiver_go.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private boolean isAddSelected = false;

    SquareDatabase squaredb, secondb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this); // Use getContext() in a Fragment or this in an Activity

        //int lastMeasurements = sharedPreferences.getInt("num_kept_measurements",0);
        //Log.println(Log.ASSERT,"lastmes", lastMeasurements+"");


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

        this.deleteDatabase("second");
        // setting the database
        squaredb = Room.databaseBuilder(this, SquareDatabase.class, "squaredb").addMigrations(SquareDatabase.migration).build();

        //secondb = Room.databaseBuilder(this, SquareDatabase.class, "second").addMigrations(SquareDatabase.migration).build();

        new Thread(() -> {
            Square s1 = new Square(50, 50, 5);
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
            squaredb.getSquareDAO().upsertSquare(new Square(11.4,44.500, 0.001));
            squaredb.getSquareDAO().upsertSquare(new Square(11.4, 44.501, 0.001));
            squaredb.getSquareDAO().upsertSquare(new Square(11.393, 44.472, 0.001));
            squaredb.getSquareDAO().upsertSquare(new Square(11.393, 44.473, 0.001));
            squaredb.getSquareDAO().upsertSquare(new Square(11.394963, 44.473466, 0.001));
            s1 = squaredb.getSquareDAO().getYourSquare(52, 52, 5);

            System.out.println(s1.toString());


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
        binding.newMeasurementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
            }
        });
        binding.newWiFiMeasurementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        binding.newInternetConnectionMeasurementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        binding.newNoiseMeasurementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        // on long click
        binding.newMeasurementButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Toast toast = Toast.makeText(view.getContext(), R.string.new_measurement, Toast.LENGTH_SHORT);
                toast.show();
                return true;
            }
        });
        binding.newWiFiMeasurementButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Toast toast = Toast.makeText(view.getContext(), R.string.new_wifi_measurement, Toast.LENGTH_SHORT);
                toast.show();
                return true;
            }
        });
        binding.newInternetConnectionMeasurementButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Toast toast = Toast.makeText(view.getContext(), R.string.new_internet_connection_measurement, Toast.LENGTH_SHORT);
                toast.show();
                return true;
            }
        });
        binding.newNoiseMeasurementButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Toast toast = Toast.makeText(view.getContext(), R.string.new_noise_measurement, Toast.LENGTH_SHORT);
                toast.show();
                return true;
            }
        });
    }

}