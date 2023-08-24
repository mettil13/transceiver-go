package app_mobili.transceiver_go;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.VisibleRegion;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentMainMap#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentMainMap extends Fragment implements OnMapReadyCallback, GoogleMap.OnCameraMoveListener {
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
        map.setOnCameraMoveListener(this);
    }

    @Override
    public void onCameraMove() {
        map.clear();
        VisibleRegion viewPort = map.getProjection().getVisibleRegion();
        double viewPortRange;
        if(viewPort.farLeft.longitude > 90 && viewPort.farRight.longitude < -90){ // in case one border has negative longitude and the other one has positive longitude and they are both near 180
            viewPortRange = (180 - viewPort.farLeft.longitude) + (180 + viewPort.farRight.longitude);
        }
        else {
            viewPortRange = viewPort.farLeft.longitude - viewPort.farRight.longitude;
        }
        viewPortRange = Math.abs(viewPortRange);
        Square tile = new Square(map.getCameraPosition().target.latitude, map.getCameraPosition().target.longitude,  (int)viewPortRange);
        tile.drawTile(map, 0xff000000, 0x66000000);
    }
}