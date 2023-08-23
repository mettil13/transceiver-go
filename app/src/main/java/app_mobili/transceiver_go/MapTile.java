package app_mobili.transceiver_go;

import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

public class MapTile {
    protected double latitude;
    protected double longitude;
    protected double sideLength;

    public MapTile(double latitude, double longitude, double sideLength) {
                this.latitude = toValidLatitude(latitude);
        this.longitude = toValidLongitude(longitude);
        this.sideLength = sideLength;
    }

    public Polygon drawTile(GoogleMap googleMap, int strokeColor, int fillColor) {
        LatLng upLeft = new LatLng(toValidLatitude(latitude - sideLength / 2), toValidLongitude(longitude + sideLength / 2));
        LatLng downLeft = new LatLng(toValidLatitude(latitude + sideLength / 2), toValidLongitude(longitude + sideLength / 2));
        LatLng downRight = new LatLng(toValidLatitude(latitude + sideLength / 2), toValidLongitude(longitude - sideLength / 2));
        LatLng upRight = new LatLng(toValidLatitude(latitude - sideLength / 2), toValidLongitude(longitude - sideLength / 2));
        Polygon tile = googleMap.addPolygon(new PolygonOptions().add(upLeft, downLeft, downRight, upRight));
        tile.setStrokeColor(strokeColor);
        tile.setFillColor(fillColor);

        //Log.println(Log.ASSERT, "", /*upLeft +*/ " " + (latitude - sideLength / 2) + " " + (longitude + sideLength / 2) + " " + latitude + " " + longitude + " " + sideLength);
        return tile;
    }

    protected double toValidLatitude(double latitude) {
        if (latitude > 89.99) { // check if latitude is a valid number, 90 degrees glitches the map, so we use 89.99 instead
            latitude = 89.99;
        } else if (latitude < -89.99) {
            latitude = -89.99;
        }

        return latitude;
    }

    protected double toValidLongitude(double longitude) {
        while (longitude > 180) { // check if longitude is a valid number, if it's bigger than 180 it gets converted into negative longitude
            longitude = -180 + (longitude - 180);
        }

        while (longitude < -180) {
            longitude = 180 + (longitude + 180);
        }

        return longitude;
    }

}
