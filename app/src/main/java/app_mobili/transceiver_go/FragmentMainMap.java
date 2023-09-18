package app_mobili.transceiver_go;

import android.graphics.Point;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.room.Room;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.VisibleRegion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mainMap)).getMapAsync(this);

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        /*
        for (int i=0; i<180; i=i+2){
            for(int j=0; j<90; j=j+2){
                MapTile tile = new MapTile(j, i, 2);
                tile.drawTile(googleMap, 0xff000000,0x66000000);
            }
        }
        */
        map = googleMap;
        map.setOnCameraMoveStartedListener(this);
        map.setOnCameraMoveListener(this);
        map.setOnCameraIdleListener(this);
    }

    @Override
    public void onCameraMoveStarted(int i) {
        VisibleRegion viewPort = map.getProjection().getVisibleRegion();
        //retrieveAndDrawSquares(new Longitude(viewPort.farLeft.longitude), new Latitude(viewPort.farLeft.latitude), new Longitude(viewPort.nearRight.longitude), new Latitude(viewPort.nearRight.latitude), 5);
    }

    @Override
    public void onCameraMove() {

    }

    @Override
    public void onCameraIdle() {
        map.clear();
        Log.println(Log.ASSERT, "", "" + map.getCameraPosition().zoom);
        // The heatmap uses too many resources when the user zooms in, while the grid uses too many resources when the user zooms out.
        // So the heatmap is used with low zoom levels and the grid with higher ones.
        if (map.getCameraPosition().zoom < 16) { // I really don't like those hardcoded constants, but it seems to be the only way possible
            retrieveSquaresAndDrawHeatmap();
        } else {
            VisibleRegion viewPort = map.getProjection().getVisibleRegion();
            retrieveAndDrawSquares(new Longitude(viewPort.farLeft.longitude), new Latitude(viewPort.farLeft.latitude), new Longitude(viewPort.nearRight.longitude), new Latitude(viewPort.nearRight.latitude), ResourcesCompat.getFloat(getContext().getResources(), R.dimen.square_dimension));
        }
    }

    protected void retrieveAndDrawSquares(Longitude topLeftX, Latitude topLeftY, Longitude bottomRightX, Latitude bottomRightY, double l) {
        topLeftX.subtract(topLeftX.getDistance(bottomRightX) / 2);
        bottomRightX.add(topLeftX.getDistance(bottomRightX) / 2);
        topLeftY.add(topLeftY.getDistance(bottomRightY) / 2);
        bottomRightY.subtract(topLeftY.getDistance(bottomRightY) / 2);

        Longitude negativeLeft;
        Longitude negativeRight;
        Longitude positiveLeft;
        Longitude positiveRight;

        if (topLeftX.getValue() < 0 && bottomRightX.getValue() < 0) {
            positiveLeft = null;
            positiveRight = null;
            negativeLeft = topLeftX;
            negativeRight = bottomRightX;
        } else if (topLeftX.getValue() < 0 && bottomRightX.getValue() > 0) {
            negativeLeft = topLeftX;
            negativeRight = new Longitude(0);
            positiveLeft = new Longitude(0);
            positiveRight = bottomRightX;
        } else if (topLeftX.getValue() > 0 && bottomRightX.getValue() < 0) {
            negativeLeft = new Longitude(-180);
            negativeRight = bottomRightX;
            positiveLeft = topLeftX;
            positiveRight = new Longitude(180);
        } else {  // topLeftX > 0 && bottomRightX > 0
            negativeRight = null;
            negativeLeft = null;
            positiveLeft = topLeftX;
            positiveRight = bottomRightX;
        }


        new Thread(() -> {
            SquareDatabase squaredb = Room.databaseBuilder(getActivity(), SquareDatabase.class, "squaredb").addMigrations(SquareDatabase.migration).build();
            List<Square> squaresWithData = new ArrayList<>();
            if (positiveLeft != null && positiveRight != null) {
                squaresWithData.addAll(squaredb.getSquareDAO().getAllSquaresInPositiveEmisphereRange(positiveLeft.getValue(), topLeftY.getValue(), positiveRight.getValue(), bottomRightY.getValue(), l));
            }
            if (negativeLeft != null && negativeRight != null) {
                squaresWithData.addAll(squaredb.getSquareDAO().getAllSquaresInNegativeEmisphereRange(negativeLeft.getValue(), topLeftY.getValue(), negativeRight.getValue(), bottomRightY.getValue(), l));
            }

            squaresWithData.sort(new Square.LongitudeComparator());
            squaresWithData.sort(new Square.LatitudeComparator());

            //Log.println(Log.ASSERT, "", squaresWithData.toString());


            for (double i = topLeftX.getValue(); i <= bottomRightX.getValue(); i = i + l) {
                for (double j = bottomRightY.getValue(); j <= topLeftY.getValue(); j = j + l) {
                    Square square = new Square(j, i, l);
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            square.drawTile(map, 0xff000000, 0x66000000);
                        }
                    });
                }
            }

            squaresWithData.forEach((item) -> {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        item.drawTile(map, 0xff00ff00, 0x66ffff33);
                    }
                });
            });

        }).start();

    }

    protected void retrieveSquaresAndDrawHeatmap() {

        new Thread(() -> {
            SquareDatabase squaredb = Room.databaseBuilder(getActivity(), SquareDatabase.class, "squaredb").addMigrations(SquareDatabase.migration).build();
            List<Square> squaresWithData = new ArrayList<>();
            squaresWithData.addAll(squaredb.getSquareDAO().getAllSquares());


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

    private int calculateProperHeatmapRadiusBasedOnZoom(float zoom) {
        Log.println(Log.ASSERT, "", "" + zoom);
        Point p1 = map.getProjection().toScreenLocation(map.getCameraPosition().target);
        Point p2 = map.getProjection().toScreenLocation(new LatLng(map.getCameraPosition().target.latitude + ResourcesCompat.getFloat(getContext().getResources(), R.dimen.square_dimension), map.getCameraPosition().target.longitude));

        if (p1.y - p2.y > getContext().getResources().getInteger(R.integer.min_cluster_dimension_in_pixel) && p1.y - p2.y < 500) {
            return p1.y - p2.y;
        } else if (p1.y - p2.y > 500) { // NEVER use the heatmap with this zoom level or higher: an OutOfMemoryError may occur
            return 500;
        } else {
            return 15;
        }
    }
}