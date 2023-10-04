package app_mobili.transceiver_go;

import android.animation.ObjectAnimator;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentGameMap#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentGameMap extends FragmentMainMap implements GoogleMap.OnMyLocationClickListener {

    public FragmentGameMap() {
        // Required empty public constructor
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        super.onMapReady(googleMap);
        CameraPosition position = map.getCameraPosition();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(position.target).tilt(90).zoom(position.zoom).build()));

        View locationButton = getView().findViewById((int) 5);
        orientationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (locationButton != null) {
                    locationButton.callOnClick();
                    CameraPosition position = map.getCameraPosition();
                    map.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(position.target).tilt(90).zoom(position.zoom).build()));
                }
            }
        });

        // LUIZO TI PREGO FA QUALCOSA, QUESTO MOSTRO L'HA GENERATO INTELLIJ
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        map.setOnMyLocationClickListener(this);
        map.setMyLocationEnabled(true);
        //map.getMyLocation();

    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(getContext(), "Current location:\n" + location, Toast.LENGTH_LONG).show();

    }

    @Override
    public void onCameraIdle() {
        CameraPosition pos = map.getCameraPosition();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(pos.target).tilt(90).bearing(pos.bearing).zoom(pos.zoom).build()));
    }
}