package app_mobili.transceiver_go;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.Date;

@Entity(tableName = "Square")
public class Square {
    @Ignore
    public final static double SIDE_LENGTH = 0.001;

    @PrimaryKey()
    @NonNull
    @ColumnInfo(name = "SquareID")
    // X,Y,L coordinates
    String coordinates = "0|0"; // initialized, but should always be overwritten

    @ColumnInfo(name = "X")
    protected Longitude longitude;
    @ColumnInfo(name = "Y")
    protected Latitude latitude;
    @ColumnInfo(name = "Network Signal Strength")
    protected float network;
    @ColumnInfo(name = "Wifi Signal Strength")
    protected float wifi;
    @ColumnInfo(name = "Noise Strength")
    protected float noise;
    protected long lastNetworkMeasurement = -1;
    protected long lastWifiMeasurement = -1;
    protected long lastNoiseMeasurement = -1;

    // Empty constructor for room
    public Square() {
    }


    @Ignore
    public Square(double longitude, double latitude) {
        // first we make sure that the provided coordinates are valid
        this.latitude = new Latitude(latitude);
        this.longitude = new Longitude(longitude);
        // First, calculate the block indices (rounded to nearest) for latitude and longitude
        int blockLatitude = (int) Math.round(this.latitude.getValue() / SIDE_LENGTH);
        int blockLongitude = (int) Math.round(this.longitude.getValue() / SIDE_LENGTH);

        // Then, calculate the center of the square using the block indices and side length
        //                      v block number  v side length
        double centerLatitude = blockLatitude * SIDE_LENGTH;
        double centerLongitude = blockLongitude * SIDE_LENGTH;

        // Set the calculated center and other attributes
        this.latitude.setValue(centerLatitude);
        this.longitude.setValue(centerLongitude);

        // i'm sorry this primary key has to be a string with this format, but this is the best
        // solution for readability than a meaningless integer id.
        coordinates = this.latitude.getValue() + "|" + this.longitude.getValue();

        network = -1;
        wifi = -1;
        noise = -1;
    }

    protected Polygon drawTile(GoogleMap googleMap, int strokeColor, int fillColor) {
        // calculate the 4 corners of the tile:
        // latitude - SIDE_LENGTH / 2 , longitude + SIDE_LENGTH / 2
        LatLng upLeft = new LatLng(new Latitude(latitude.getValue()).subtract(SIDE_LENGTH / 2).getValue(), new Longitude(longitude.getValue()).add(SIDE_LENGTH / 2).getValue());
        // latitude + SIDE_LENGTH / 2 , longitude + SIDE_LENGTH / 2
        LatLng downLeft = new LatLng(new Latitude(latitude.getValue()).add(SIDE_LENGTH / 2).getValue(), new Longitude(longitude.getValue()).add(SIDE_LENGTH / 2).getValue());
        // latitude + SIDE_LENGTH / 2 , longitude - SIDE_LENGTH / 2
        LatLng downRight = new LatLng(new Latitude(latitude.getValue()).add(SIDE_LENGTH / 2).getValue(), new Longitude(longitude.getValue()).subtract(SIDE_LENGTH / 2).getValue());
        // latitude - SIDE_LENGTH / 2 , longitude - SIDE_LENGTH / 2
        LatLng upRight = new LatLng(new Latitude(latitude.getValue()).subtract(SIDE_LENGTH / 2).getValue(), new Longitude(longitude.getValue()).subtract(SIDE_LENGTH / 2).getValue());

        Polygon tile = googleMap.addPolygon(new PolygonOptions().add(upLeft, downLeft, downRight, upRight));
        tile.setStrokeColor(strokeColor);
        tile.setFillColor(fillColor);
        return tile;
    }

    public String getSquareId() {
        return coordinates;
    }

    public Polygon drawNoiseTile(GoogleMap googleMap, Context context) {
        if (noise < 0) {
            return drawTile(googleMap, ContextCompat.getColor(context, R.color.no_data_intensity_border), ContextCompat.getColor(context, R.color.no_data_intensity_filler));
        } else if (noise <= context.getResources().getInteger(R.integer.noise_low_upper_bound)) {
            return drawTile(googleMap, ContextCompat.getColor(context, R.color.low_intensity_border), ContextCompat.getColor(context, R.color.low_intensity_filler));
        } else if (noise <= context.getResources().getInteger(R.integer.noise_medium_upper_bound)) {
            return drawTile(googleMap, ContextCompat.getColor(context, R.color.medium_intensity_border), ContextCompat.getColor(context, R.color.medium_intensity_filler));
        } else {
            return drawTile(googleMap, ContextCompat.getColor(context, R.color.high_intensity_border), ContextCompat.getColor(context, R.color.high_intensity_filler));
        }
    }

    public Polygon drawWifiTile(GoogleMap googleMap, Context context) {
        if (wifi < 0) {
            return drawTile(googleMap, ContextCompat.getColor(context, R.color.no_data_intensity_border), ContextCompat.getColor(context, R.color.no_data_intensity_filler));
        } else if (wifi <= context.getResources().getInteger(R.integer.wifi_low_upper_bound)) {
            return drawTile(googleMap, ContextCompat.getColor(context, R.color.low_intensity_border), ContextCompat.getColor(context, R.color.low_intensity_filler));
        } else if (wifi <= context.getResources().getInteger(R.integer.wifi_medium_upper_bound)) {
            return drawTile(googleMap, ContextCompat.getColor(context, R.color.medium_intensity_border), ContextCompat.getColor(context, R.color.medium_intensity_filler));
        } else {
            return drawTile(googleMap, ContextCompat.getColor(context, R.color.high_intensity_border), ContextCompat.getColor(context, R.color.high_intensity_filler));
        }
    }

    public Polygon drawNetworkTile(GoogleMap googleMap, Context context) {
        if (network < 0) {
            return drawTile(googleMap, ContextCompat.getColor(context, R.color.no_data_intensity_border), ContextCompat.getColor(context, R.color.no_data_intensity_filler));
        } else if (network <= context.getResources().getInteger(R.integer.network_low_upper_bound)) {
            return drawTile(googleMap, ContextCompat.getColor(context, R.color.low_intensity_border), ContextCompat.getColor(context, R.color.low_intensity_filler));
        } else if (network <= context.getResources().getInteger(R.integer.network_medium_upper_bound)) {
            return drawTile(googleMap, ContextCompat.getColor(context, R.color.medium_intensity_border), ContextCompat.getColor(context, R.color.medium_intensity_filler));
        } else {
            return drawTile(googleMap, ContextCompat.getColor(context, R.color.high_intensity_border), ContextCompat.getColor(context, R.color.high_intensity_filler));
        }
    }

    public Polygon drawEmptyTile(GoogleMap googleMap, Context context) {
        return drawTile(googleMap, ContextCompat.getColor(context, R.color.no_data_intensity_border), ContextCompat.getColor(context, R.color.no_data_intensity_filler));
    }

    public void updateNetwork(Context context, int network) {
        Log.println(Log.ASSERT, "", "network: " + network);
        float measurementWeight = convertMeasurementWeightToDecimal(PreferenceManager.getDefaultSharedPreferences(context).getInt("num_kept_measurements", 50));
        if (this.network < 0) {
            this.network = network;
        } else {
            // newMeasurement * measurementWeight + oldMeasurements * ( 1 - measurementWeight )
            this.network = ((float) network) * measurementWeight + this.network * (1f - measurementWeight);
        }
        lastNetworkMeasurement = System.currentTimeMillis();
    }

    public void updateWifi(Context context, int wifi) {
        float measurementWeight = convertMeasurementWeightToDecimal(PreferenceManager.getDefaultSharedPreferences(context).getInt("num_kept_measurements", 50));
        if (this.wifi < 0) {
            this.wifi = wifi;
        } else {
            // newMeasurement * measurementWeight + oldMeasurements * ( 1 - measurementWeight )
            this.wifi = ((float) wifi) * measurementWeight +  this.wifi * (1f - measurementWeight);
        }
        lastWifiMeasurement = System.currentTimeMillis();
    }

    public void updateNoise(Context context, int noise) {
        float measurementWeight = convertMeasurementWeightToDecimal(PreferenceManager.getDefaultSharedPreferences(context).getInt("num_kept_measurements", 50));
        if (this.noise < 0) {
            this.noise = noise;
        } else {
            // newMeasurement * measurementWeight + oldMeasurements * ( 1 - measurementWeight )
            this.noise = ((float) noise) * measurementWeight + ((float) this.noise) * (1f - measurementWeight);
        }
        lastNoiseMeasurement = System.currentTimeMillis();
    }

    private float convertMeasurementWeightToDecimal(int measurementWeight) {
        if (measurementWeight > 95) {
            measurementWeight = 95;
        } else if (measurementWeight < 5) {
            measurementWeight = 5;
        }
        return ((float) measurementWeight) / 100;
    }

    // SETTERS
    public void setNetwork(float network) {
        this.network = network;
    }

    public void setWifi(float wifi) {
        this.wifi = wifi;
    }

    public void setNoise(float noise) {
        this.noise = noise;
    }

    // GETTERS
    public float getNetwork() {
        return network;
    }

    public float getWifi() {
        return wifi;
    }

    public float getNoise() {
        return noise;
    }

    // returns Id of the square
    @NonNull
    public String getCoordinates() {
        return coordinates;
    }

    public Longitude getLongitude() {
        return longitude;
    }

    public Latitude getLatitude() {
        return latitude;
    }

    public long getLastNetworkMeasurement() {
        return lastNetworkMeasurement;
    }

    public long getLastWifiMeasurement() {
        return lastWifiMeasurement;
    }

    public long getLastNoiseMeasurement() {
        return lastNoiseMeasurement;
    }

    @NonNull
    @Override
    public String toString() {
        return "Square{" +
                "coordinates='" + coordinates + '\'' +
                ", latitude=" + latitude.getValue() +
                ", longitude=" + longitude.getValue() +
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

    public static class NetworkComparator implements java.util.Comparator<Square> {
        @Override
        public int compare(Square square1, Square square2) {
            return Double.compare(square1.getNetwork(), square2.getNetwork());
        }
    }

    public static class NoiseComparator implements java.util.Comparator<Square> {
        @Override
        public int compare(Square square1, Square square2) {
            return Double.compare(square1.getNoise(), square2.getNoise());
        }
    }

    public static class WifiComparator implements java.util.Comparator<Square> {
        @Override
        public int compare(Square square1, Square square2) {
            return Double.compare(square1.getWifi(), square2.getWifi());
        }
    }
}
