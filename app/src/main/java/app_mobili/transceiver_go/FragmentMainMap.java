package app_mobili.transceiver_go;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.VisibleRegion;

import java.util.Collections;
import java.util.List;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentMainMap#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentMainMap extends Fragment implements OnMapReadyCallback, GoogleMap.OnCameraMoveStartedListener {
    protected GoogleMap map;

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
    }

    @Override
    public void onCameraMoveStarted(int i) {
        map.clear();
        VisibleRegion viewPort = map.getProjection().getVisibleRegion();
        retrieveAndDrawSquares(viewPort.farLeft.longitude, viewPort.farLeft.latitude, viewPort.nearRight.longitude, viewPort.nearRight.latitude, 5);
        /*
        double viewPortRange;
        if (viewPort.farLeft.longitude > 90 && viewPort.farRight.longitude < -90) { // in case one border has negative longitude and the other one has positive longitude and they are both near 180
            viewPortRange = (180 - viewPort.farLeft.longitude) + (180 + viewPort.farRight.longitude);
        } else {
            viewPortRange = viewPort.farLeft.longitude - viewPort.farRight.longitude;
        }
        viewPortRange = Math.abs(viewPortRange);
        Square tile = new Square(map.getCameraPosition().target.latitude, map.getCameraPosition().target.longitude, (int) viewPortRange);
        tile.drawTile(map, 0xff000000, 0x66000000);
         */
    }

    protected void retrieveAndDrawSquares(double topLeftX, double topLeftY, double bottomRightX, double bottomRightY, double l) {
        double negativeLeft;
        double negativeRight;
        double positiveLeft;
        double positiveRight;

        if (topLeftX < 0 && bottomRightX < 0) {
            positiveLeft = -1; // invalid value
            positiveRight = -1;
            negativeLeft = topLeftX;
            negativeRight = bottomRightX;
        } else if (topLeftX < 0 && bottomRightX > 0) {
            negativeLeft = topLeftX;
            negativeRight = 0;
            positiveLeft = 0;
            positiveRight = bottomRightX;
        } else if (topLeftX > 0 && bottomRightX < 0) {
            negativeLeft = -180;
            negativeRight = bottomRightX;
            positiveLeft = topLeftX;
            positiveRight = 180;
        } else {  // topLeftX > 0 && bottomRightX > 0
            negativeRight = 1; // invalid value
            negativeLeft = 1;
            positiveLeft = topLeftX;
            positiveRight = bottomRightX;
        }


        new Thread(() -> {
            SquareDatabase squaredb = Room.databaseBuilder(getActivity(), SquareDatabase.class, "squaredb").addMigrations(SquareDatabase.migration).build();
            List<Square> positiveSquares = squaredb.getSquareDAO().getAllSquaresInPositiveEmisphereRange(positiveLeft, topLeftY, positiveRight, bottomRightY, l);
            List<Square> negativeSquares = squaredb.getSquareDAO().getAllSquaresInNegativeEmisphereRange(negativeLeft, topLeftY, negativeRight, bottomRightY, l);

            List<Square> squaresWithData = positiveSquares;
            squaresWithData.addAll(negativeSquares);

            squaresWithData.sort(new Square.LongitudeComparator());
            squaresWithData.sort(new Square.LatitudeComparator());

            Log.println(Log.ASSERT, "", squaresWithData.toString());
            // lat = y, lng = x

            for (double i = topLeftX; i <= bottomRightX; i = i + l) {
                for (double j = bottomRightY; j <= topLeftY; j = j + l) {
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
}