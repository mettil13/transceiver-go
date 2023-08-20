package app_mobili.transceiver_go;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

public class MapTile {
    protected double latitude;
    protected double longitude;
    protected double sideLength;

    public MapTile(double latitude, double longitude, double sideLength){
        this.latitude = latitude;
        this.longitude = longitude;
        this.sideLength = sideLength;
    }

    public Polygon drawTile(GoogleMap googleMap, int strokeColor, int fillColor){
        Polygon tile = googleMap.addPolygon(new PolygonOptions()
                .add(
                        new LatLng(latitude - sideLength/2, longitude + sideLength/2),
                        new LatLng(latitude + sideLength/2, longitude + sideLength/2),
                        new LatLng(latitude + sideLength/2, longitude - sideLength/2),
                        new LatLng(latitude - sideLength/2, longitude - sideLength/2)
                ));
        tile.setStrokeColor(strokeColor);
        tile.setFillColor(fillColor);
        return tile;
    }


}
