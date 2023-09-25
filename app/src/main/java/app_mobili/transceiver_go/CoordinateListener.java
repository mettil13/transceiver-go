package app_mobili.transceiver_go;

import android.location.Location;
import android.location.LocationListener;

import androidx.annotation.NonNull;

public class CoordinateListener implements LocationListener {
    Longitude longitude;
    Latitude latitude;

    CoordinateListener() {
        longitude = new Longitude(0);
        latitude = new Latitude(0);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        longitude.setValue(location.getLongitude());
        latitude.setValue(location.getLatitude());
    }

    public double getLongitude() {
        return longitude.getValue();
    }

    public double getLatitude() {
        return latitude.getValue();
    }
}
