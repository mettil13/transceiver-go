package app_mobili.transceiver_go;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class FragmentGameMap extends FragmentMainMap {

    private Latitude myLocationLatitude;
    private Longitude myLocationLongitude;
    private Latitude oldLocationLatitude;
    private Longitude oldLocationLongitude;
    private float myLocationBearing;
    private ImageView myAvatarSkin;
    private ImageView myAvatarClothes;
    private ImageView myAvatarHat;
    private Latitude avatarLatitude;
    private Longitude avatarLongitude;
    private GameManager gameInstance;
    private ImageView gameMarker;
    private Latitude gameMarkerLatitude;
    private Longitude gameMarkerLongitude;

    public FragmentGameMap() {
        // Required empty public constructor
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        gameInstance = new GameManager();
        // creates the my position avatar
        myAvatarSkin = new ImageView(requireContext());
        myAvatarClothes = new ImageView(requireContext());
        myAvatarHat = new ImageView(requireContext());
        setUpMyAvatar(myAvatarSkin, myAvatarClothes, myAvatarHat);

        //creates the target marker
        gameMarker = new ImageView(requireContext());
        setUpGameMarker(gameMarker);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        super.onMapReady(googleMap);
        CameraPosition position = map.getCameraPosition();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(position.target).tilt(90).zoom(position.zoom).build()));

        orientationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myLocationLatitude != null && myLocationLongitude != null) {
                    map.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(new LatLng(myLocationLatitude.getValue(), myLocationLongitude.getValue())).tilt(getMaximumTilt(19)).zoom(19).bearing(myLocationBearing).build()));
                }
            }
        });

        if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    808);
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    809);
            // ask permissions and return, if we don't have 'em we do nothing
            return;
        }

        if (requireActivity() instanceof MainActivity) {
            ((MainActivity) requireActivity()).startListenForCoordinates(new CoordinateListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    Log.println(Log.ASSERT, "", "Game location updated:" + new Date() + " " + location.getLatitude() + " " + location.getLongitude());
                    if (myLocationLatitude == null || myLocationLongitude == null) { // if a previous location does not exist, make the old location equal to the current one
                        oldLocationLatitude = new Latitude(location.getLatitude());
                        oldLocationLongitude = new Longitude(location.getLongitude());
                        // if it's the first time that the location is taken (so no myLocationLatitude and Longitude exist), move the game marker to the player position.
                        // placing the marker near the player masks some delays in the first update of its position (caused by the faulty Google method map.getProjection().toScreenLocation())
                        moveGameMarker(oldLocationLatitude, oldLocationLongitude);
                    } else {
                        oldLocationLatitude = new Latitude(myLocationLatitude.getValue());
                        oldLocationLongitude = new Longitude(myLocationLongitude.getValue());
                    }

                    myLocationLatitude = new Latitude(location.getLatitude());
                    myLocationLongitude = new Longitude(location.getLongitude());
                    myLocationBearing = location.getBearing();

                    ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
                    valueAnimator.setDuration(1000); // duration 1 second
                    valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                    valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            try {
                                float v = animation.getAnimatedFraction();

                                avatarLatitude = new Latitude((myLocationLatitude.getValue() - oldLocationLatitude.getValue()) * v + oldLocationLatitude.getValue());

                                // Take the shortest path across the 180th meridian.
                                double lngDelta = myLocationLongitude.getValue() - oldLocationLongitude.getValue();
                                if (Math.abs(lngDelta) > 180) {
                                    lngDelta -= Math.signum(lngDelta) * 360;
                                }
                                avatarLongitude = new Longitude(lngDelta * v + oldLocationLongitude.getValue());


                            } catch (Exception e) {
                                avatarLatitude = new Latitude(myLocationLatitude.getValue());
                                avatarLongitude = new Longitude(myLocationLongitude.getValue());
                            } finally {
                                moveMyAvatar(avatarLatitude, avatarLongitude);
                                myAvatarSkin.setVisibility(View.VISIBLE);
                                myAvatarClothes.setVisibility(View.VISIBLE);
                                myAvatarHat.setVisibility(View.VISIBLE);
                                ((AnimatedVectorDrawable) myAvatarSkin.getDrawable()).start();
                                ((AnimatedVectorDrawable) myAvatarClothes.getDrawable()).start();

                                orientationButton.callOnClick();
                            }
                        }
                    });

                    valueAnimator.start();
                }

                // without this execution on api 24 would result in a crash
                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

            });
        }


    }

    @Override
    public void onCameraMove() {
        super.onCameraMove();
        if (avatarLatitude != null && avatarLongitude != null) {
            moveMyAvatar(avatarLatitude, avatarLongitude);
        }
        if (gameMarkerLatitude != null && gameMarkerLongitude != null) {
            moveGameMarker(gameMarkerLatitude, gameMarkerLongitude);
        }
    }

    @Override
    public void onCameraIdle() {
        super.onCameraIdle();
        CameraPosition pos = map.getCameraPosition();
        if (pos.tilt != getMaximumTilt(pos.zoom)) {
            map.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(pos.target).tilt(getMaximumTilt(pos.zoom)).bearing(pos.bearing).zoom(pos.zoom).build()));
        }

        updateGameInstance();
    }

    private void setUpMyAvatar(ImageView avatarSkin, ImageView avatarClothes, ImageView avatarHat) {
        LorenzoHelper.buildLorenzoWalkFromPreferences(requireContext(), avatarSkin, avatarClothes, avatarHat);
        ((android.widget.FrameLayout) requireView().findViewById(R.id.fragment_main_map_layout)).addView(avatarSkin);
        ((android.widget.FrameLayout) requireView().findViewById(R.id.fragment_main_map_layout)).addView(avatarClothes);
        ((android.widget.FrameLayout) requireView().findViewById(R.id.fragment_main_map_layout)).addView(avatarHat);

        // resize the images
        ViewGroup.LayoutParams skinParams = avatarSkin.getLayoutParams();
        skinParams.height = 250; // pixels
        skinParams.width = 250;
        avatarSkin.setLayoutParams(skinParams);
        ViewGroup.LayoutParams clothesParams = avatarClothes.getLayoutParams();
        clothesParams.height = 250; // pixels
        clothesParams.width = 250;
        avatarClothes.setLayoutParams(clothesParams);
        ViewGroup.LayoutParams hatParams = avatarHat.getLayoutParams();
        hatParams.height = 250; // pixels
        hatParams.width = 250;
        avatarHat.setLayoutParams(hatParams);

        // hides the avatar until it needs to be shown
        avatarSkin.setVisibility(View.INVISIBLE);
        avatarClothes.setVisibility(View.INVISIBLE);
        avatarHat.setVisibility(View.INVISIBLE);
    }

    private void moveMyAvatar(Latitude newLatitude, Longitude newLongitude) {
        Point myPositionOnScreen = map.getProjection().toScreenLocation(new LatLng(newLatitude.getValue(), newLongitude.getValue()));
        myAvatarSkin.setTranslationX(myPositionOnScreen.x - myAvatarSkin.getLayoutParams().width / 2f);
        myAvatarSkin.setTranslationY(myPositionOnScreen.y - myAvatarSkin.getLayoutParams().height);
        myAvatarClothes.setTranslationX(myPositionOnScreen.x - myAvatarClothes.getLayoutParams().width / 2f);
        myAvatarClothes.setTranslationY(myPositionOnScreen.y - myAvatarClothes.getLayoutParams().height);
        myAvatarHat.setTranslationX(myPositionOnScreen.x - myAvatarHat.getLayoutParams().width / 2f);
        myAvatarHat.setTranslationY(myPositionOnScreen.y - myAvatarHat.getLayoutParams().height - (myAvatarSkin.getLayoutParams().height * 0.1f));
    }

    private void setUpGameMarker(ImageView gameMarker) {
        gameMarker.setImageResource(R.drawable.marker);
        ((android.widget.FrameLayout) requireView().findViewById(R.id.fragment_main_map_layout)).addView(gameMarker);

        // resize the images
        ViewGroup.LayoutParams sizeParams = gameMarker.getLayoutParams();
        sizeParams.height = 200; // pixels
        sizeParams.width = 200;

        gameMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                map.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(new LatLng(gameMarkerLatitude.getValue(), gameMarkerLongitude.getValue())).zoom(map.getCameraPosition().zoom).bearing(map.getCameraPosition().bearing).tilt(map.getCameraPosition().tilt).build()));

                String typeOfData = PreferenceManager.getDefaultSharedPreferences(requireContext()).getString("type_of_data", "None");
                Square playerSquare = new Square(myLocationLongitude.getValue(), myLocationLatitude.getValue());
                if (playerSquare.getLatitude().getValue() == gameInstance.getCurrentTarget().getLatitude().getValue() && playerSquare.getLongitude().getValue() == gameInstance.getCurrentTarget().getLongitude().getValue()) {
                    createGameMeasurementDialog(typeOfData);
                } else {
                    createGameTooFarDialog(typeOfData);
                }


            }
        });

        // hides the marker until it needs to be shown
        gameMarker.setVisibility(View.INVISIBLE);
    }

    private void moveGameMarker(Latitude newLatitude, Longitude newLongitude) {
        Point myPositionOnScreen = map.getProjection().toScreenLocation(new LatLng(newLatitude.getValue(), newLongitude.getValue()));
        if (checkForPositionOnScreenOverflow(newLatitude, newLongitude, myPositionOnScreen)) {
            return; // if an overflow has occurred, do not update the position of the target
        }

        if (myPositionOnScreen.x < gameMarker.getLayoutParams().width / 2) {
            myPositionOnScreen.x = gameMarker.getLayoutParams().width / 2;
        }
        if (myPositionOnScreen.y < gameMarker.getLayoutParams().height) {
            myPositionOnScreen.y = gameMarker.getLayoutParams().height;
        }

        View view = getView(); // in case moveGameMarker is called before the map view is fully loaded
        if (view != null) {
            if (myPositionOnScreen.x > view.getMeasuredWidth() - gameMarker.getLayoutParams().width / 2) {
                myPositionOnScreen.x = view.getMeasuredWidth() - gameMarker.getLayoutParams().width / 2;
            }
            if (myPositionOnScreen.y > view.getMeasuredHeight()) {
                myPositionOnScreen.y = view.getMeasuredHeight();
            }
        }

        gameMarker.setTranslationX(myPositionOnScreen.x - gameMarker.getLayoutParams().width / 2f);
        gameMarker.setTranslationY(myPositionOnScreen.y - gameMarker.getLayoutParams().height);
    }

    //returns true if there is an overflow, false otherwise
    private boolean checkForPositionOnScreenOverflow(Latitude targetLatitude, Longitude targetLongitude, Point targetPositionOnScreen) {
        int precision = 1; // Higher precisions increase false positives
        LatLng targetPosition = new LatLng(targetLatitude.getValue(), targetLongitude.getValue());
        LatLng roundedTargetPosition = new LatLng(BigDecimal.valueOf(targetPosition.latitude).setScale(precision, RoundingMode.HALF_UP).doubleValue(), BigDecimal.valueOf(targetPosition.longitude).setScale(precision, RoundingMode.HALF_UP).doubleValue());

        LatLng convertedTargetPosition = map.getProjection().fromScreenLocation(targetPositionOnScreen);
        if (convertedTargetPosition != null) { //this warning is not true: convertedTargetPosition becomes null when an overflow occurs!
            LatLng roundedConvertedTargetPosition = new LatLng(BigDecimal.valueOf(convertedTargetPosition.latitude).setScale(precision, RoundingMode.HALF_UP).doubleValue(), BigDecimal.valueOf(convertedTargetPosition.longitude).setScale(precision, RoundingMode.HALF_UP).doubleValue());

            return (roundedConvertedTargetPosition.longitude != roundedTargetPosition.longitude || roundedConvertedTargetPosition.latitude != roundedTargetPosition.latitude);
        } else {
            return true;
        }


    }

    protected void updateGameInstance() {

        new Thread(() -> {
            try {
                Thread.sleep(500); // awaits for half a second in order to let the squares fully render
            } catch (InterruptedException ignored) {
            }

            Context context = getContext();
            if (context != null) {
                String typeOfData = PreferenceManager.getDefaultSharedPreferences(context).getString("type_of_data", "None");
                Square currentTarget = gameInstance.getCurrentTarget();

                if (currentTarget == null) {
                    currentTarget = gameInstance.generateNewTarget(lastDrawnSquares, typeOfData); // if lastDrawnSquares is null, then also currentTarget is set to null
                }

                Square finalCurrentTarget = currentTarget;
                requireActivity().runOnUiThread(new Runnable() {
                    public void run() {

                        Context context = getContext();
                        if(context != null){
                            if (finalCurrentTarget == null) {
                                gameMarker.setVisibility(View.INVISIBLE);
                            } else {
                                finalCurrentTarget.drawTile(map, ContextCompat.getColor(context, R.color.game_target_border), ContextCompat.getColor(context, R.color.game_target_filler));
                                gameMarkerLatitude = finalCurrentTarget.getLatitude();
                                gameMarkerLongitude = finalCurrentTarget.getLongitude();
                                gameMarker.setVisibility(View.VISIBLE);
                                moveGameMarker(gameMarkerLatitude, gameMarkerLongitude);
                            }
                        }

                    }
                });
            }

        }).start();

    }

    // code from https://gist.github.com/bharris47/5057910
    public static float getMaximumTilt(float zoom) {
        // for tilt values, see:
        // https://developers.google.com/maps/documentation/android/reference/com/google/android/gms/maps/model/CameraPosition.Builder?hl=fr

        float tilt = 30.0f;

        if (zoom > 15.5f) {
            tilt = 67.5f;
        } else if (zoom >= 14.0f) {
            tilt = (((zoom - 14.0f) / 1.5f) * (67.5f - 45.0f)) + 45.0f;
        } else if (zoom >= 10.0f) {
            tilt = (((zoom - 10.0f) / 4.0f) * (45.0f - 30.0f)) + 30.0f;
        }

        return tilt;
    }

    private void createGameTooFarDialog(String typeOfData) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(getResources().getString(R.string.too_far_title)).setMessage(getResources().getString(R.string.too_far_text))
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.generate_new_target, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        gameInstance.generateNewTarget(lastDrawnSquares, typeOfData);
                        onCameraIdle();
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void createGameMeasurementDialog(String typeOfData) {
        List<String> selectedItems = new ArrayList<>();  // selected items

        String[] entries = Arrays.stream(getResources().getStringArray(R.array.type_of_data_entries)).skip(1).toArray(size -> new String[size]); // skip the first element of the array, which is "None"
        int indexOfMandatoryType = Arrays.asList(getResources().getStringArray(R.array.type_of_data_values)).indexOf(typeOfData) - 1; // skip the first element of the array, which is "None"

        boolean[] checkedItems = new boolean[entries.length];
        Arrays.fill(checkedItems, false);
        if (indexOfMandatoryType >= 0) { // if indexOfMandatoryType is < 0 it means that typeOfData is not an element of type_of_data_values
            checkedItems[indexOfMandatoryType] = true;
            selectedItems.add(getResources().getStringArray(R.array.type_of_data_values)[indexOfMandatoryType + 1]);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle(getResources().getString(R.string.measurements_and_reward_title))
                .setMultiChoiceItems(entries, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int index, boolean isChecked) {
                        String clickedItem = getResources().getStringArray(R.array.type_of_data_values)[index + 1]; // skip the first element of the array, which is "None"
                        if (isChecked) {
                            selectedItems.add(clickedItem);
                        } else {
                            selectedItems.remove(clickedItem);
                        }
                    }
                }).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if (selectedItems.contains(getResources().getStringArray(R.array.type_of_data_values)[indexOfMandatoryType + 1])) {
                            Activity currentActivity = requireActivity();
                            if (currentActivity instanceof MainActivity) {
                                MeasurementSingleton measurementSingleton = MeasurementSingleton.create(requireContext(), new CoordinateListener() {
                                    // without this execution on api 24 would result in a crash
                                    @Override
                                    public void onStatusChanged(String provider, int status, Bundle extras) {
                                    }
                                });
                                int coinsReward = 0;
                                if (selectedItems.contains("Noise")) {
                                    measurementSingleton.takeNoiseMeasurement((MainActivity) currentActivity);
                                    coinsReward += getResources().getInteger(R.integer.square_single_measurement_reward);
                                }
                                if (selectedItems.contains("Network")) {
                                    measurementSingleton.takeNetworkMeasurement((MainActivity) currentActivity);
                                    coinsReward += getResources().getInteger(R.integer.square_single_measurement_reward);
                                }
                                if (selectedItems.contains("Wi-fi")) {
                                    measurementSingleton.takeWifiMeasurement((MainActivity) currentActivity);
                                    coinsReward += getResources().getInteger(R.integer.square_single_measurement_reward);
                                }
                                addCoins(coinsReward);
                                Toast.makeText(requireContext(), getResources().getText(R.string.you_earned) + " " + coinsReward + " " + getResources().getString(R.string.coins), Toast.LENGTH_LONG).show();
                                gameInstance.generateNewTarget(lastDrawnSquares, typeOfData);
                                onCameraIdle();
                            }

                        } else {
                            Toast.makeText(requireContext(), getResources().getText(R.string.cannot_give_reward) + " " + getResources().getStringArray(R.array.type_of_data_entries)[indexOfMandatoryType + 1], Toast.LENGTH_LONG).show();
                        }
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                }).show();
    }

    private void addCoins(int coins) {
        int oldCoins = PreferenceManager.getDefaultSharedPreferences(requireContext()).getInt("coins", 0);
        PreferenceManager.getDefaultSharedPreferences(requireContext()).edit().putInt("coins", oldCoins + coins).apply();
    }
}