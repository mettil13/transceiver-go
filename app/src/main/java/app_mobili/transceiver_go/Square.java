package app_mobili.transceiver_go;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

@Entity(tableName = "Square")
public class Square {
    @PrimaryKey()
    @NonNull
    @ColumnInfo(name = "SquareID")
    // X,Y,L coordinates
    String coordinates = "0|0"; // initialized, but should always be overwritten

    @ColumnInfo(name = "X")
    protected Longitude longitude;
    @ColumnInfo(name = "Y")
    protected Latitude latitude;
    @ColumnInfo(name = "Length")
    protected double sideLength;
    @ColumnInfo(name = "Network Signal Strength")
    protected int network;
    @ColumnInfo(name = "Wifi Signal Strength")
    protected int wifi;
    @ColumnInfo(name = "Noise Strength")
    protected int noise;
    protected int noiseAverageCounter = 1;
    protected int wifiAverageCounter = 1;
    protected int networkAverageCounter = 1;

    // Empty constructor for room
    public Square() {
    }


    @Ignore
    public Square(double longitude, double latitude, double sideLength) {
        // first we make sure that the provided coordinates are valid
        this.latitude = new Latitude(latitude);
        this.longitude = new Longitude(longitude);
        // First, calculate the block indices (rounded to nearest) for latitude and longitude
        int blockLatitude = (int) Math.round(this.latitude.getValue() / sideLength);
        int blockLongitude = (int) Math.round(this.longitude.getValue() / sideLength);

        // Then, calculate the center of the square using the block indices and side length
        //                      v block number  v side length
        double centerLatitude = blockLatitude * sideLength;
        double centerLongitude = blockLongitude * sideLength;

        // Set the calculated center and other attributes
        this.latitude.setValue(centerLatitude);
        this.longitude.setValue(centerLongitude);
        this.sideLength = sideLength;

        // i'm sorry this primary key has to be a string with this format, but this is the best
        // solution for readability than a meaningless integer id.
        coordinates = this.latitude.getValue() + "|" + this.longitude.getValue();

        network = 1;
        wifi = 1;
        noise = 1;
    }

    public Polygon drawTile(GoogleMap googleMap, int strokeColor, int fillColor) {
        // calculate the 4 corners of the tile:
        // latitude - sideLength / 2 , longitude + sideLength / 2
        LatLng upLeft = new LatLng(new Latitude(latitude.getValue()).subtract(sideLength / 2).getValue(), new Longitude(longitude.getValue()).add(sideLength / 2).getValue());
        // latitude + sideLength / 2 , longitude + sideLength / 2
        LatLng downLeft = new LatLng(new Latitude(latitude.getValue()).add(sideLength / 2).getValue(), new Longitude(longitude.getValue()).add(sideLength / 2).getValue());
        // latitude + sideLength / 2 , longitude - sideLength / 2
        LatLng downRight = new LatLng(new Latitude(latitude.getValue()).add(sideLength / 2).getValue(), new Longitude(longitude.getValue()).subtract(sideLength / 2).getValue());
        // latitude - sideLength / 2 , longitude - sideLength / 2
        LatLng upRight = new LatLng(new Latitude(latitude.getValue()).subtract(sideLength / 2).getValue(), new Longitude(longitude.getValue()).subtract(sideLength / 2).getValue());

        Polygon tile = googleMap.addPolygon(new PolygonOptions().add(upLeft, downLeft, downRight, upRight));
        tile.setStrokeColor(strokeColor);
        tile.setFillColor(fillColor);
        return tile;
    }

    public void updateNetwork(int network) {
        // (currentAverage * numberOfElements) + newNumber) / (numberOfElements + 1);
        //        numberOfElements++;
        this.network = (this.network * networkAverageCounter + network) / ++networkAverageCounter;
    }

    public void updateWifi(int wifi) {
        this.wifi = (this.wifi * wifiAverageCounter + wifi) / ++wifiAverageCounter;
    }

    public void updateNoise(int noise) {
        this.noise = (this.noise * noiseAverageCounter + noise) / ++noiseAverageCounter;
    }


    // SETTERS
    // NOTE: Set functions reset the average counters of a square
    // for the interested value, eg. setNetwork resets the networkAverageCounter
    public void setNetwork(int network) {
        networkAverageCounter = 1;
        this.network = network;
    }

    public void setWifi(int wifi) {
        wifiAverageCounter = 1;
        this.wifi = wifi;
    }

    public void setNoise(int noise) {
        noiseAverageCounter = 1;
        this.noise = noise;
    }

    // GETTERS
    public int getNetwork() {
        return network;
    }

    public int getWifi() {
        return wifi;
    }

    public int getNoise() {
        return noise;
    }

    // returns Id of the square
    @NonNull
    public String getCoordinates() {
        return coordinates;
    }

    @NonNull
    @Override
    public String toString() {
        return "Square{" +
                "coordinates='" + coordinates + '\'' +
                ", latitude=" + latitude.getValue() +
                ", longitude=" + longitude.getValue() +
                ", length=" + sideLength +
                ", network=" + network +
                ", wifi=" + wifi +
                ", noise=" + noise +
                '}';
    }

    public static class LatitudeComparator implements java.util.Comparator<Square> {
        @Override
        public int compare(Square square1, Square square2) {
            return Double.compare(square1.latitude.getValue(), square2.latitude.getValue());
        }
    }

    public static class LongitudeComparator implements java.util.Comparator<Square> {
        @Override
        public int compare(Square square1, Square square2) {
            return Double.compare(square1.longitude.getValue(), square2.longitude.getValue());
        }
    }
}
