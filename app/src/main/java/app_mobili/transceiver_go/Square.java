package app_mobili.transceiver_go;

import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

@Entity(tableName = "Square")
public class Square {
    @PrimaryKey()
    @NonNull
    @ColumnInfo(name="SquareID")
    // X,Y,L coordinates
    String coordinates;

    //square centre coordinates can't be non integer numbers so i'll keep 'em this way
    @ColumnInfo(name="X")
    protected int latitude;
    @ColumnInfo(name="Y")
    protected int longitude;
    @NonNull
    @ColumnInfo(name="Length")
    protected int sideLength;
    @ColumnInfo(name="Network Signal Strength")
    protected int network;
    @ColumnInfo(name="Wifi Signal Strength")
    protected int wifi;
    @ColumnInfo(name="Noise Strength")
    protected int noise;
    protected int noiseAverageCounter = 1;
    protected int wifiAverageCounter = 1;
    protected int networkAverageCounter = 1;

    // Empty constructor for room
    public Square(){

    }

    // X,Y coordinates 0 = X , 1 = Y
    // L values are measured in Meters and have to be integers
    public Square(double latitude, double longitude, int sideLength){
        // first we make sure that the provided coordinates are valid
        latitude = toValidLatitude(latitude);
        longitude = toValidLongitude(longitude);
        // coordinates provided will get rounded to nearest square coordinates
        Pair<Integer,Integer> block = new Pair<>(Math.round((float)latitude / sideLength),Math.round((float)longitude / sideLength)); // now latitude and longitude can be casted to float because they are real earth coordinates
        // then to find the X,Y coordinates of square (M,N) we multiply by the requested length l
        // to mathematically get the center of the square we want
        this.latitude = block.first * sideLength;
        this.longitude = block.second * sideLength;
        this.sideLength = sideLength;

        // i'm sorry this primary key has to be a string with this format, but this is the best
        // solution for readability than a meaningless integer id.
        coordinates =this.latitude +"."+this.longitude +"/"+this.sideLength;

        network = -1;
        wifi = -1;
        noise = -1;
    }

    public Polygon drawTile(GoogleMap googleMap, int strokeColor, int fillColor) {
        LatLng upLeft = new LatLng(toValidLatitude(latitude - (double)sideLength / 2), toValidLongitude(longitude + (double)sideLength / 2));
        LatLng downLeft = new LatLng(toValidLatitude(latitude + (double)sideLength / 2), toValidLongitude(longitude + (double)sideLength / 2));
        LatLng downRight = new LatLng(toValidLatitude(latitude + (double)sideLength / 2), toValidLongitude(longitude - (double)sideLength / 2));
        LatLng upRight = new LatLng(toValidLatitude(latitude - (double)sideLength / 2), toValidLongitude(longitude - (double)sideLength / 2));
        Polygon tile = googleMap.addPolygon(new PolygonOptions().add(upLeft, downLeft, downRight, upRight));
        tile.setStrokeColor(strokeColor);
        tile.setFillColor(fillColor);
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

    // NOTE: Set functions reset the average counters of a square
    // for the interested value, eg. setNetwork resets the networkAverageCounter
    public void setNetwork(int network) {
        networkAverageCounter= 1;
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

    public void updateNetwork(int network) {
        // (currentAverage * numberOfElements) + newNumber) / (numberOfElements + 1);
        //        numberOfElements++;
        this.network = (this.network*networkAverageCounter + network)/ ++networkAverageCounter;
    }

    public void updateWifi(int wifi) {
        this.wifi = (this.wifi*wifiAverageCounter + wifi)/ ++wifiAverageCounter;
    }

    public void updateNoise(int noise) {
        this.noise = (this.noise*noiseAverageCounter + noise)/ ++noiseAverageCounter;
    }

    @Override
    public String toString() {
        return "Square{" +
                "coordinates='" + coordinates + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", length=" + sideLength +
                ", network=" + network +
                ", wifi=" + wifi +
                ", noise=" + noise +
                '}';
    }
}
