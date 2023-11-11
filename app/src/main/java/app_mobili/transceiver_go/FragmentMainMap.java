package app_mobili.transceiver_go;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;
import androidx.room.Room;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.VisibleRegion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

public class FragmentMainMap extends Fragment implements OnMapReadyCallback, GoogleMap.OnCameraMoveStartedListener, GoogleMap.OnCameraMoveListener, GoogleMap.OnCameraIdleListener {
    protected GoogleMap map;
    protected FloatingActionButton orientationButton;
    protected HeatmapTileProvider heatmapProvider;
    protected FragmentLayerSelector layerSelector; // contains the names of the data to display
    protected static int minClusterDimensionInPixel = 15;

    public List<Square> lastDrawnSquares; // contains the last cluster of squares drawn on the map

    public FragmentMainMap() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main_map, container, false);

        FloatingActionButton layerButton = view.findViewById(R.id.layerButton);
        layerSelector = new FragmentLayerSelector();
        layerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getParentFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setReorderingAllowed(true);
                fragmentTransaction.replace(R.id.fragmentContainer, layerSelector).addToBackStack("");
                fragmentTransaction.commit();
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mainMap)).getMapAsync(this);

    }

    @Override
    public void onResume() {
        super.onResume();
        Activity currentActivity = getActivity();
        if (currentActivity instanceof MainActivity) {
            ((MainActivity) currentActivity).refreshMaps();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        map.setOnCameraMoveStartedListener(this);
        map.setOnCameraMoveListener(this);
        map.setOnCameraIdleListener(this);

        // disables the camera tilting
        map.getUiSettings().setTiltGesturesEnabled(false);

        // makes squares clickable
        map.setOnPolygonClickListener(new GoogleMap.OnPolygonClickListener() {
            public void onPolygonClick(Polygon polygon) {
                Toast.makeText(getContext(), polygon.getTag().toString(), Toast.LENGTH_SHORT).show();
            }
        });

        // hides default compass button and creates another one with the same use (just an aesthetic thing)
        View defaultOrientationButton = requireView().findViewById((int) 5); // "5" is the id of google maps compass button
        // Change the visibility of compass button
        if (defaultOrientationButton != null) {
            defaultOrientationButton.setVisibility(View.GONE);
        }

        orientationButton = requireView().findViewById(R.id.orientationButton);
        orientationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (defaultOrientationButton != null) {
                    defaultOrientationButton.callOnClick();
                }
            }
        });
        float bearing = map.getCameraPosition().bearing;
        ObjectAnimator.ofFloat(orientationButton, "rotation", bearing - 45).setDuration(0).start(); // the icon has a 45° initial rotation
    }

    @Override
    public void onCameraMoveStarted(int i) {

    }

    @Override
    public void onCameraMove() {
        float bearing = map.getCameraPosition().bearing;
        ObjectAnimator.ofFloat(orientationButton, "rotation", bearing - 45).setDuration(0).start(); // the icon has a 45° initial rotation
    }

    @Override
    public void onCameraIdle() {
        map.clear();

        VisibleRegion viewPort = map.getProjection().getVisibleRegion();
        Longitude topLeftX = new Longitude(Math.min(viewPort.farLeft.longitude, Math.min(viewPort.farRight.longitude, Math.min(viewPort.nearLeft.longitude, viewPort.nearRight.longitude))));
        Latitude topLeftY = new Latitude(Math.max(viewPort.farLeft.latitude, Math.max(viewPort.farRight.latitude, Math.max(viewPort.nearLeft.latitude, viewPort.nearRight.latitude))));
        Longitude bottomRightX = new Longitude(Math.max(viewPort.farLeft.longitude, Math.max(viewPort.farRight.longitude, Math.max(viewPort.nearLeft.longitude, viewPort.nearRight.longitude))));
        Latitude bottomRightY = new Latitude(Math.min(viewPort.farLeft.latitude, Math.min(viewPort.farRight.latitude, Math.min(viewPort.nearLeft.latitude, viewPort.nearRight.latitude))));

        Longitude cameraX = new Longitude(map.getCameraPosition().target.longitude);
        Latitude cameraY = new Latitude(map.getCameraPosition().target.latitude);

        // expands the viewport area adding a bit of margin to let the camera move around a little bit without having to wait for the new areas being drawn
        topLeftX.subtract(topLeftX.getCounterClockwiseDistance(cameraX));
        bottomRightX.add(cameraX.getCounterClockwiseDistance(bottomRightX));
        topLeftY.add(topLeftY.getDistance(cameraY));
        bottomRightY.subtract(bottomRightY.getDistance(cameraY));

        // The heatmap uses too many resources when the user zooms in, while the grid uses too many resources when the user zooms out.
        // So the heatmap is used with low zoom levels and the grid with higher ones.
        if (map.getCameraPosition().zoom < 16) { // I really don't like those hardcoded constants, but it seems to be the only way possible
            retrieveSquaresAndDrawHeatmap(topLeftX, topLeftY, bottomRightX, bottomRightY);
        } else {
            int maxNumberOfSquares = 15; // limits the number of drawn squares (from the target of the camera to the side of the drawn area) to use less resources
            if (topLeftX.getCounterClockwiseDistance(cameraX) > Square.SIDE_LENGTH * maxNumberOfSquares) {
                topLeftX.setValue(cameraX.getValue() - Square.SIDE_LENGTH * maxNumberOfSquares);
            }
            if (cameraX.getCounterClockwiseDistance(bottomRightX) > Square.SIDE_LENGTH * maxNumberOfSquares) {
                bottomRightX.setValue(cameraX.getValue() + Square.SIDE_LENGTH * maxNumberOfSquares);
            }
            if (topLeftY.getDistance(cameraY) > Square.SIDE_LENGTH * maxNumberOfSquares) {
                topLeftY.setValue(cameraY.getValue() + Square.SIDE_LENGTH * maxNumberOfSquares);
            }
            if (bottomRightY.getDistance(cameraY) > Square.SIDE_LENGTH * maxNumberOfSquares) {
                bottomRightY.setValue(cameraY.getValue() - Square.SIDE_LENGTH * maxNumberOfSquares);
            }

            retrieveAndDrawSquares(topLeftX, topLeftY, bottomRightX, bottomRightY);
        }
    }

    protected void retrieveAndDrawSquares(Longitude topLeftX, Latitude topLeftY, Longitude bottomRightX, Latitude bottomRightY) {
        Longitude negativeLeft = truncateLongitudeToWesternHemisphere(topLeftX, bottomRightX) != null ? truncateLongitudeToWesternHemisphere(topLeftX, bottomRightX)[0] : null;
        Longitude negativeRight = truncateLongitudeToWesternHemisphere(topLeftX, bottomRightX) != null ? truncateLongitudeToWesternHemisphere(topLeftX, bottomRightX)[1] : null;
        Longitude positiveLeft = truncateLongitudeToEasternHemisphere(topLeftX, bottomRightX) != null ? truncateLongitudeToEasternHemisphere(topLeftX, bottomRightX)[0] : null;
        Longitude positiveRight = truncateLongitudeToEasternHemisphere(topLeftX, bottomRightX) != null ? truncateLongitudeToEasternHemisphere(topLeftX, bottomRightX)[1] : null;

        String typeOfData = PreferenceManager.getDefaultSharedPreferences(requireContext()).getString("type_of_data", "None");

        new Thread(() -> {
            lastDrawnSquares = new ArrayList<>();
            if (positiveLeft != null && positiveRight != null) {
                Map<String, Square> easternSquaresWithData = retrieveEasternSquares(getActiveMapNames(), positiveLeft, topLeftY, positiveRight, bottomRightY);
                drawGridOnOneHemisphere(easternSquaresWithData, positiveLeft, topLeftY, positiveRight, bottomRightY, typeOfData);
            }
            if (negativeLeft != null && negativeRight != null) {
                Map<String, Square> westernSquaresWithData = retrieveWesternSquares(getActiveMapNames(), negativeLeft, topLeftY, negativeRight, bottomRightY);
                drawGridOnOneHemisphere(westernSquaresWithData, negativeLeft, topLeftY, negativeRight, bottomRightY, typeOfData);
            }

        }).start();

    }

    protected void retrieveSquaresAndDrawHeatmap(Longitude topLeftX, Latitude topLeftY, Longitude bottomRightX, Latitude bottomRightY) {
        Longitude negativeLeft = truncateLongitudeToWesternHemisphere(topLeftX, bottomRightX) != null ? truncateLongitudeToWesternHemisphere(topLeftX, bottomRightX)[0] : null;
        Longitude negativeRight = truncateLongitudeToWesternHemisphere(topLeftX, bottomRightX) != null ? truncateLongitudeToWesternHemisphere(topLeftX, bottomRightX)[1] : null;
        Longitude positiveLeft = truncateLongitudeToEasternHemisphere(topLeftX, bottomRightX) != null ? truncateLongitudeToEasternHemisphere(topLeftX, bottomRightX)[0] : null;
        Longitude positiveRight = truncateLongitudeToEasternHemisphere(topLeftX, bottomRightX) != null ? truncateLongitudeToEasternHemisphere(topLeftX, bottomRightX)[1] : null;

        String typeOfData = PreferenceManager.getDefaultSharedPreferences(requireContext()).getString("type_of_data", "None");

        new Thread(() -> {
            Map<String, Square> squaresWithData = new HashMap<>();
            if (positiveLeft != null && positiveRight != null) {
                squaresWithData.putAll(retrieveEasternSquares(getActiveMapNames(), positiveLeft, topLeftY, positiveRight, bottomRightY));
            }
            if (negativeLeft != null && negativeRight != null) {
                squaresWithData.putAll(retrieveWesternSquares(getActiveMapNames(), negativeLeft, topLeftY, negativeRight, bottomRightY));
            }

            List<LatLng> heatmapPointsLow = new ArrayList<>();
            List<LatLng> heatmapPointsMedium = new ArrayList<>();
            List<LatLng> heatmapPointsHigh = new ArrayList<>();
            squaresWithData.forEach((index, square) -> {
                switch (typeOfData) {
                    case "Noise":
                        if (square.getNoise() >= 0) {
                            if (square.getNoise() <= requireContext().getResources().getInteger(R.integer.noise_low_upper_bound)) {
                                heatmapPointsLow.add(new LatLng(square.latitude.getValue(), square.longitude.getValue()));
                            } else if (square.getNoise() <= requireContext().getResources().getInteger(R.integer.noise_medium_upper_bound)) {
                                heatmapPointsLow.add(new LatLng(square.latitude.getValue(), square.longitude.getValue()));
                                heatmapPointsMedium.add(new LatLng(square.latitude.getValue(), square.longitude.getValue()));
                            } else {
                                heatmapPointsLow.add(new LatLng(square.latitude.getValue(), square.longitude.getValue()));
                                heatmapPointsMedium.add(new LatLng(square.latitude.getValue(), square.longitude.getValue()));
                                heatmapPointsHigh.add(new LatLng(square.latitude.getValue(), square.longitude.getValue()));
                            }
                        }
                        break;
                    case "Network":
                        if (square.getNetwork() >= 0) {
                            if (square.getNetwork() <= requireContext().getResources().getInteger(R.integer.network_low_upper_bound)) {
                                heatmapPointsLow.add(new LatLng(square.latitude.getValue(), square.longitude.getValue()));
                            } else if (square.getNetwork() <= requireContext().getResources().getInteger(R.integer.network_medium_upper_bound)) {
                                heatmapPointsLow.add(new LatLng(square.latitude.getValue(), square.longitude.getValue()));
                                heatmapPointsMedium.add(new LatLng(square.latitude.getValue(), square.longitude.getValue()));
                            } else {
                                heatmapPointsLow.add(new LatLng(square.latitude.getValue(), square.longitude.getValue()));
                                heatmapPointsMedium.add(new LatLng(square.latitude.getValue(), square.longitude.getValue()));
                                heatmapPointsHigh.add(new LatLng(square.latitude.getValue(), square.longitude.getValue()));
                            }
                        }
                        break;
                    case "Wi-fi":
                        if (square.getWifi() >= 0) {
                            if (square.getWifi() <= requireContext().getResources().getInteger(R.integer.wifi_low_upper_bound)) {
                                heatmapPointsLow.add(new LatLng(square.latitude.getValue(), square.longitude.getValue()));
                            } else if (square.getWifi() <= requireContext().getResources().getInteger(R.integer.wifi_medium_upper_bound)) {
                                heatmapPointsLow.add(new LatLng(square.latitude.getValue(), square.longitude.getValue()));
                                heatmapPointsMedium.add(new LatLng(square.latitude.getValue(), square.longitude.getValue()));
                            } else {
                                heatmapPointsLow.add(new LatLng(square.latitude.getValue(), square.longitude.getValue()));
                                heatmapPointsMedium.add(new LatLng(square.latitude.getValue(), square.longitude.getValue()));
                                heatmapPointsHigh.add(new LatLng(square.latitude.getValue(), square.longitude.getValue()));
                            }
                        }
                        break;
                }
            });

            Gradient gradientLow = new Gradient(new int[]{ContextCompat.getColor(requireContext(), R.color.low_intensity_border)}, new float[]{0.50f}, 3);
            Gradient gradientMedium = new Gradient(new int[]{ContextCompat.getColor(requireContext(), R.color.medium_intensity_border)}, new float[]{0.50f}, 3);
            Gradient gradientHigh = new Gradient(new int[]{ContextCompat.getColor(requireContext(), R.color.high_intensity_border)}, new float[]{0.50f}, 3);


            requireActivity().runOnUiThread(new Runnable() {
                public void run() {
                    if (!heatmapPointsLow.isEmpty()) {
                        heatmapProvider = new HeatmapTileProvider.Builder().data(heatmapPointsLow).build();
                        heatmapProvider.setRadius(calculateProperHeatmapRadiusBasedOnZoom(map.getCameraPosition().zoom)); // HeatmapTileProvider.Builder().radius() accepts only values between 0 and 50, provider.setRadius() instead accepts every value
                        heatmapProvider.setMaxIntensity(1);
                        heatmapProvider.setGradient(gradientLow);
                        map.addTileOverlay(new TileOverlayOptions().tileProvider(heatmapProvider));
                    }

                    if (!heatmapPointsMedium.isEmpty()) {
                        heatmapProvider = new HeatmapTileProvider.Builder().data(heatmapPointsMedium).build();
                        heatmapProvider.setRadius(calculateProperHeatmapRadiusBasedOnZoom(map.getCameraPosition().zoom)); // HeatmapTileProvider.Builder().radius() accepts only values between 0 and 50, provider.setRadius() instead accepts every value
                        heatmapProvider.setMaxIntensity(1);
                        heatmapProvider.setGradient(gradientMedium);
                        map.addTileOverlay(new TileOverlayOptions().tileProvider(heatmapProvider));

                    }

                    if (!heatmapPointsHigh.isEmpty()) {
                        heatmapProvider = new HeatmapTileProvider.Builder().data(heatmapPointsHigh).build();
                        heatmapProvider.setRadius(calculateProperHeatmapRadiusBasedOnZoom(map.getCameraPosition().zoom)); // HeatmapTileProvider.Builder().radius() accepts only values between 0 and 50, provider.setRadius() instead accepts every value
                        heatmapProvider.setMaxIntensity(1);
                        heatmapProvider.setGradient(gradientHigh);
                        map.addTileOverlay(new TileOverlayOptions().tileProvider(heatmapProvider));

                    }

                }
            });


        }).start();


    }

    // returns an array containing the boundaries of the area between leftLng and rightLng but truncated to the eastern hemisphere.
    // the first element [0] represents the left boundary, the second [1] represents the right one.
    // the array is null if the entire area is outside the eastern hemisphere.
    protected Longitude[] truncateLongitudeToEasternHemisphere(Longitude leftLng, Longitude rightLng) {
        Longitude positiveLeft;
        Longitude positiveRight;

        if (leftLng.getValue() < 0 && rightLng.getValue() < 0) {
            return null;
        } else if (leftLng.getValue() < 0 && rightLng.getValue() > 0) {
            positiveLeft = new Longitude(0);
            positiveRight = new Longitude(rightLng.getValue());
            return new Longitude[]{positiveLeft, positiveRight};
        } else if (leftLng.getValue() > 0 && rightLng.getValue() < 0) {
            positiveLeft = new Longitude(leftLng.getValue());
            positiveRight = new Longitude(180);
            return new Longitude[]{positiveLeft, positiveRight};
        } else {  // leftLng > 0 && rightLng > 0
            positiveLeft = new Longitude(leftLng.getValue());
            positiveRight = new Longitude(rightLng.getValue());
            return new Longitude[]{positiveLeft, positiveRight};
        }
    }

    // returns an array containing the boundaries of the area between leftLng and rightLng but truncated to the western hemisphere.
    // the first element [0] represents the left boundary, the second [1] represents the right one.
    // the array is null if the entire area is outside the western hemisphere.
    protected Longitude[] truncateLongitudeToWesternHemisphere(Longitude leftLng, Longitude rightLng) {
        Longitude negativeLeft;
        Longitude negativeRight;

        if (leftLng.getValue() < 0 && rightLng.getValue() < 0) {
            negativeLeft = new Longitude(leftLng.getValue());
            negativeRight = new Longitude(rightLng.getValue());
            return new Longitude[]{negativeLeft, negativeRight};
        } else if (leftLng.getValue() < 0 && rightLng.getValue() > 0) {
            negativeLeft = new Longitude(leftLng.getValue());
            negativeRight = new Longitude(0);
            return new Longitude[]{negativeLeft, negativeRight};
        } else if (leftLng.getValue() > 0 && rightLng.getValue() < 0) {
            negativeLeft = new Longitude(-180);
            negativeRight = new Longitude(rightLng.getValue());
            return new Longitude[]{negativeLeft, negativeRight};
        } else {  // leftLng > 0 && RightLng > 0
            return null;
        }
    }

    protected Polygon drawSquareOfType(String typeOfData, Square squareToDraw) {
        switch (typeOfData) {
            case "Noise":
                return squareToDraw.drawNoiseTile(map, requireContext());
            case "Network":
                return squareToDraw.drawNetworkTile(map, requireContext());
            case "Wi-fi":
                return squareToDraw.drawWifiTile(map, requireContext());
            default:
                return squareToDraw.drawEmptyTile(map, requireContext());
        }
    }

    protected void drawGridOnOneHemisphere(Map<String, Square> squaresWithData, Longitude topLeftX, Latitude topLeftY, Longitude bottomRightX, Latitude bottomRightY, String typeOfData) { // BEWARE: it doesn't behave correctly if the drawing area extends over two hemispheres
        // creates a bidimensional array to store all the square that are going to be drawn
        //    y  x
        Square[][] squaresToDraw = new Square[(int) ((topLeftY.getValue() - bottomRightY.getValue()) / Square.SIDE_LENGTH) + 1][(int) ((bottomRightX.getValue() - topLeftX.getValue()) / Square.SIDE_LENGTH) + 1];
        for (int i = 0; i < squaresToDraw.length; i++) { // y
            for (int j = 0; j < squaresToDraw[i].length; j++) { // x
                // creates a square with no measurements
                squaresToDraw[i][j] = new Square(topLeftX.getValue() + Square.SIDE_LENGTH * j, bottomRightY.getValue() + Square.SIDE_LENGTH * i);
                // if a square with some measurement exists, it replaces the empty square
                if (squaresWithData.containsKey(squaresToDraw[i][j].getSquareId())) {
                    squaresToDraw[i][j] = squaresWithData.get(squaresToDraw[i][j].getSquareId());
                }

                Square tileToDraw = squaresToDraw[i][j];
                requireActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        Polygon drawnSquare = drawSquareOfType(typeOfData, tileToDraw);
                        if(getValueOfType(typeOfData, tileToDraw) >= 0){
                            drawnSquare.setClickable(true);
                            drawnSquare.setTag(getLabelBasedOnType(typeOfData) + ": " + getValueOfType(typeOfData, tileToDraw));
                        }
                    }
                });

            }
        }

        for (Square[] array : squaresToDraw) { // adds all the squares to lastDrawnSquares
            lastDrawnSquares.addAll(Arrays.asList(array));
        }
    }

    private int calculateProperHeatmapRadiusBasedOnZoom(float zoom) {
        Point p1 = map.getProjection().toScreenLocation(map.getCameraPosition().target);
        Point p2 = map.getProjection().toScreenLocation(new LatLng(new Latitude(map.getCameraPosition().target.latitude).add(Square.SIDE_LENGTH).getValue(), new Longitude(map.getCameraPosition().target.longitude).add(Square.SIDE_LENGTH).getValue()));
        int distanceInPixel = (int) (Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2)) / 2);
        if (distanceInPixel > minClusterDimensionInPixel && distanceInPixel <= 500) {
            return distanceInPixel;
        } else if (distanceInPixel > 500) { // NEVER use the heatmap with this zoom level or higher: an OutOfMemoryError may occur
            return 500;
        } else {
            return minClusterDimensionInPixel;
        }
    }

    protected List<String> getActiveMapNames() {
        List<String> ret = new ArrayList<>();
        String[] dbList = requireContext().databaseList();
        for (String s : dbList) {
            if (PreferenceManager.getDefaultSharedPreferences(requireContext()).getBoolean(s, false)) {
                ret.add(s);
            }
        }

        return ret;
    }

    protected Map<String, Square> retrieveEasternSquares(List<String> mapNames, Longitude topLeftX, Latitude topLeftY, Longitude bottomRightX, Latitude bottomRightY) {
        Map<String, Square> easternSquaresWithData = new HashMap<>();
        Map<String, Integer> numberOfNoise = new HashMap<>();
        Map<String, Integer> numberOfNetwork = new HashMap<>();
        Map<String, Integer> numberOfWifi = new HashMap<>();
        // gets the squares from all the active databases
        for (String name : mapNames) {
            SquareDatabase squaredb = Room.databaseBuilder(requireContext(), SquareDatabase.class, name).addMigrations(SquareDatabase.migration).build();
            List<Square> list = squaredb.getSquareDAO().getAllSquaresInPositiveHemisphereRange(topLeftX.getValue(), topLeftY.getValue(), bottomRightX.getValue(), bottomRightY.getValue());
            list.forEach(square -> {
                if (easternSquaresWithData.containsKey(square.getSquareId())) {

                    Square oldSquare = easternSquaresWithData.get(square.getSquareId());
                    if (oldSquare.getNoise() >= 0 && square.getNoise() >= 0) {
                        oldSquare.setNoise(oldSquare.getNoise() + square.getNoise());
                        numberOfNoise.put(square.getSquareId(), numberOfNoise.get(square.getSquareId()) + 1);
                    }
                    if (oldSquare.getNetwork() >= 0 && square.getNetwork() >= 0) {
                        oldSquare.setNetwork(oldSquare.getNetwork() + square.getNetwork());
                        numberOfNetwork.put(square.getSquareId(), numberOfNetwork.get(square.getSquareId()) + 1);
                    }
                    if (oldSquare.getWifi() >= 0 && square.getWifi() >= 0) {
                        oldSquare.setWifi(oldSquare.getWifi() + square.getWifi());
                        numberOfWifi.put(square.getSquareId(), numberOfWifi.get(square.getSquareId()) + 1);
                    }

                } else {
                    easternSquaresWithData.put(square.getSquareId(), square);
                    numberOfNoise.put(square.getSquareId(), 1);
                    numberOfNetwork.put(square.getSquareId(), 1);
                    numberOfWifi.put(square.getSquareId(), 1);
                }
            });
            squaredb.close();
        }

        // divides the values to make average values
        easternSquaresWithData.forEach((id, square) -> {
            square.setNoise(square.getNoise() / numberOfNoise.get(id));
            square.setNetwork(square.getNetwork() / numberOfNetwork.get(id));
            square.setWifi(square.getWifi() / numberOfWifi.get(id));
        });

        return easternSquaresWithData;
    }

    protected Map<String, Square> retrieveWesternSquares(List<String> mapNames, Longitude topLeftX, Latitude topLeftY, Longitude bottomRightX, Latitude bottomRightY) {
        Map<String, Square> westernSquaresWithData = new HashMap<>();
        Map<String, Integer> numberOfNoise = new HashMap<>();
        Map<String, Integer> numberOfNetwork = new HashMap<>();
        Map<String, Integer> numberOfWifi = new HashMap<>();
        // gets the squares from all the active databases
        for (String name : mapNames) {
            SquareDatabase squaredb = Room.databaseBuilder(requireContext(), SquareDatabase.class, name).addMigrations(SquareDatabase.migration).build();
            List<Square> list = squaredb.getSquareDAO().getAllSquaresInNegativeHemisphereRange(topLeftX.getValue(), topLeftY.getValue(), bottomRightX.getValue(), bottomRightY.getValue());
            list.forEach(square -> {
                if (westernSquaresWithData.containsKey(square.getSquareId())) {

                    Square oldSquare = westernSquaresWithData.get(square.getSquareId());
                    if (oldSquare.getNoise() >= 0 && square.getNoise() >= 0) {
                        oldSquare.setNoise(oldSquare.getNoise() + square.getNoise());
                        numberOfNoise.put(square.getSquareId(), numberOfNoise.get(square.getSquareId()) + 1);
                    }
                    if (oldSquare.getNetwork() >= 0 && square.getNetwork() >= 0) {
                        oldSquare.setNetwork(oldSquare.getNetwork() + square.getNetwork());
                        numberOfNetwork.put(square.getSquareId(), numberOfNetwork.get(square.getSquareId()) + 1);
                    }
                    if (oldSquare.getWifi() >= 0 && square.getWifi() >= 0) {
                        oldSquare.setWifi(oldSquare.getWifi() + square.getWifi());
                        numberOfWifi.put(square.getSquareId(), numberOfWifi.get(square.getSquareId()) + 1);
                    }

                } else {
                    westernSquaresWithData.put(square.getSquareId(), square);
                    numberOfNoise.put(square.getSquareId(), 1);
                    numberOfNetwork.put(square.getSquareId(), 1);
                    numberOfWifi.put(square.getSquareId(), 1);
                }
            });
            squaredb.close();
        }

        // divides the values to make average values
        westernSquaresWithData.forEach((id, square) -> {
            square.setNoise(square.getNoise() / numberOfNoise.get(id));
            square.setNetwork(square.getNetwork() / numberOfNetwork.get(id));
            square.setWifi(square.getWifi() / numberOfWifi.get(id));
        });

        return westernSquaresWithData;
    }

    private float getValueOfType(String typeOfData, Square squareToDraw) {
        switch (typeOfData) {
            case "Noise":
                return squareToDraw.getNoise();
            case "Network":
                return squareToDraw.getNetwork();
            case "Wi-fi":
                return squareToDraw.getWifi();
            default:
                return -1;
        }
    }

    private String getLabelBasedOnType(String typeOfData) {
        switch (typeOfData) {
            case "Noise":
                return getResources().getString(R.string.noise);
            case "Network":
                return getResources().getString(R.string.network);
            case "Wi-fi":
                return getResources().getString(R.string.wifi);
            default:
                return getResources().getString(R.string.none);
        }
    }

}