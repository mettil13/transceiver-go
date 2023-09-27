package app_mobili.transceiver_go;

import android.graphics.Point;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.room.Room;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.VisibleRegion;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentMainMap#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentMainMap extends Fragment implements OnMapReadyCallback, GoogleMap.OnCameraMoveStartedListener, GoogleMap.OnCameraMoveListener, GoogleMap.OnCameraIdleListener {
    protected GoogleMap map;
    protected HeatmapTileProvider heatmapProvider;

    protected static int minClusterDimensionInPixel = 15;

    public FragmentMainMap() {
        // Required empty public constructor
    }


    public static FragmentMainMap newInstance(String param1, String param2) {
        FragmentMainMap fragment = new FragmentMainMap();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main_map, container, false);
        FloatingActionButton button = (FloatingActionButton) view.findViewById(R.id.layerButton);
        Fragment layerSelector = new FragmentLayerSelector();
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
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
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        map.setOnCameraMoveStartedListener(this);
        map.setOnCameraMoveListener(this);
        map.setOnCameraIdleListener(this);
    }

    @Override
    public void onCameraMoveStarted(int i) {

    }

    @Override
    public void onCameraMove() {

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
        Longitude negativeRight = truncateLongitudeToWesternHemisphere(topLeftX, bottomRightX) != null ? truncateLongitudeToWesternHemisphere(topLeftX, bottomRightX)[0] : null;
        Longitude positiveLeft = truncateLongitudeToEasternHemisphere(topLeftX, bottomRightX) != null ? truncateLongitudeToEasternHemisphere(topLeftX, bottomRightX)[0] : null;
        Longitude positiveRight = truncateLongitudeToEasternHemisphere(topLeftX, bottomRightX) != null ? truncateLongitudeToEasternHemisphere(topLeftX, bottomRightX)[1] : null;


        new Thread(() -> {
            SquareDatabase squaredb = Room.databaseBuilder(getActivity(), SquareDatabase.class, "squaredb").addMigrations(SquareDatabase.migration).build();
            List<Square> easternSquaresWithData = new ArrayList<>();
            List<Square> westernSquaresWithData = new ArrayList<>();
            if (positiveLeft != null && positiveRight != null) {
                easternSquaresWithData.addAll(squaredb.getSquareDAO().getAllSquaresInPositiveEmisphereRange(positiveLeft.getValue(), topLeftY.getValue(), positiveRight.getValue(), bottomRightY.getValue()));
            }
            if (negativeLeft != null && negativeRight != null) {
                westernSquaresWithData.addAll(squaredb.getSquareDAO().getAllSquaresInNegativeEmisphereRange(negativeLeft.getValue(), topLeftY.getValue(), negativeRight.getValue(), bottomRightY.getValue()));
            }


            Log.println(Log.ASSERT, "", easternSquaresWithData.toString() + westernSquaresWithData.toString());

            /*
            if(positiveLeft != null && positiveRight != null){
                for (double lng = positiveLeft.getValue(); lng <= positiveRight.getValue(); lng = lng + Square.SIDE_LENGTH) {
                    for (double lat = bottomRightY.getValue(); lat <= topLeftY.getValue(); lat = lat + Square.SIDE_LENGTH) {
                        Square square;
                        List<Square> match = easternSquaresWithData.stream().filter(a -> Objects.equals(a.longitude.getValue(), lng) && Objects.equals(a.latitude.getValue(), lat)).collect(Collectors.toList()); // match contains all the squares (hopefully one or zero) with matching latitude and longitude
                        if(!match.isEmpty())  {
                            square = easternSquaresWithData.get(0);
                        } else{
                            square = new Square(lng, lat);
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                            //TODO: make squares of different colors
                                square.drawTile(map, 0xff000000, 0x66000000);
                            }
                        });
                    }
                }
            }
            //TODO: the same thing with the other hemisphere

             */


            for (double i = topLeftX.getValue(); i <= bottomRightX.getValue(); i = i + Square.SIDE_LENGTH) {
                for (double j = bottomRightY.getValue(); j <= topLeftY.getValue(); j = j + Square.SIDE_LENGTH) {
                    Square square = new Square(i, j);
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            square.drawTile(map, 0xff000000, 0x66000000);
                        }
                    });
                }
            }

            easternSquaresWithData.forEach((item) -> {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        item.drawTile(map, 0xff00ff00, 0x66ffff33);
                    }
                });
            });

            westernSquaresWithData.forEach((item) -> {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        item.drawTile(map, 0xff00ff00, 0x66ffff33);
                    }
                });
            });


        }).start();

    }

    protected void retrieveSquaresAndDrawHeatmap(Longitude topLeftX, Latitude topLeftY, Longitude bottomRightX, Latitude bottomRightY) {
        Longitude negativeLeft = truncateLongitudeToWesternHemisphere(topLeftX, bottomRightX) != null ? truncateLongitudeToWesternHemisphere(topLeftX, bottomRightX)[0] : null;
        Longitude negativeRight = truncateLongitudeToWesternHemisphere(topLeftX, bottomRightX) != null ? truncateLongitudeToWesternHemisphere(topLeftX, bottomRightX)[0] : null;
        Longitude positiveLeft = truncateLongitudeToEasternHemisphere(topLeftX, bottomRightX) != null ? truncateLongitudeToEasternHemisphere(topLeftX, bottomRightX)[0] : null;
        Longitude positiveRight = truncateLongitudeToEasternHemisphere(topLeftX, bottomRightX) != null ? truncateLongitudeToEasternHemisphere(topLeftX, bottomRightX)[1] : null;

        new Thread(() -> {
            SquareDatabase squaredb = Room.databaseBuilder(getActivity(), SquareDatabase.class, "squaredb").addMigrations(SquareDatabase.migration).build();
            List<Square> squaresWithData = new ArrayList<>();
            if (positiveLeft != null && positiveRight != null) {
                squaresWithData.addAll(squaredb.getSquareDAO().getAllSquaresInPositiveEmisphereRange(positiveLeft.getValue(), topLeftY.getValue(), positiveRight.getValue(), bottomRightY.getValue()));
            }
            if (negativeLeft != null && negativeRight != null) {
                squaresWithData.addAll(squaredb.getSquareDAO().getAllSquaresInNegativeEmisphereRange(negativeLeft.getValue(), topLeftY.getValue(), negativeRight.getValue(), bottomRightY.getValue()));
            }

            List<WeightedLatLng> heatmapPoints = new ArrayList<>();
            squaresWithData.forEach(square -> {
                heatmapPoints.add(new WeightedLatLng(new LatLng(square.latitude.getValue(), square.longitude.getValue()), 1));
            });

            if (!heatmapPoints.isEmpty()) {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        heatmapProvider = new HeatmapTileProvider.Builder().weightedData(heatmapPoints).build();
                        heatmapProvider.setRadius(calculateProperHeatmapRadiusBasedOnZoom(map.getCameraPosition().zoom)); // HeatmapTileProvider.Builder().radius() accepts only values between 0 and 50, provider.setRadius() instead accepts every value
                        // TODO: replace this value with the maximum intensity that a measurement can reach
                        heatmapProvider.setMaxIntensity(1);
                        TileOverlay overlay = map.addTileOverlay(new TileOverlayOptions().tileProvider(heatmapProvider));

                    }
                });
            }

        }).start();
    }

    // returns an array containing the boundaries of the area between leftLng and rightLng but truncated to the eastern hemisphere.
    // the first element [0] represents the left boundary, the second [1] represents the right one.
    // the array is null if the entire area is outside the eastern hemisphere.
    private Longitude[] truncateLongitudeToEasternHemisphere(Longitude leftLng, Longitude rightLng) {
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
    private Longitude[] truncateLongitudeToWesternHemisphere(Longitude leftLng, Longitude rightLng) {
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

    private int calculateProperHeatmapRadiusBasedOnZoom(float zoom) {
        //Log.println(Log.ASSERT, "", "" + zoom);
        Point p1 = map.getProjection().toScreenLocation(map.getCameraPosition().target);
        Point p2 = map.getProjection().toScreenLocation(new LatLng(map.getCameraPosition().target.latitude + Square.SIDE_LENGTH, map.getCameraPosition().target.longitude));

        if (p1.y - p2.y > minClusterDimensionInPixel && p1.y - p2.y < 500) {
            return p1.y - p2.y;
        } else if (p1.y - p2.y > 500) { // NEVER use the heatmap with this zoom level or higher: an OutOfMemoryError may occur
            return 500;
        } else {
            return minClusterDimensionInPixel;
        }
    }
}