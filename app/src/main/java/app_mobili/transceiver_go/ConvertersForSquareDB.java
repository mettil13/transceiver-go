package app_mobili.transceiver_go;

import androidx.room.TypeConverter;

public class ConvertersForSquareDB {
    @TypeConverter
    public static double longitudeToDouble(Longitude longitude){
        return longitude.getValue();
    }

    @TypeConverter
    public static Longitude doubleToLongitude(double longitude){
        return new Longitude(longitude);
    }

    @TypeConverter
    public static double latitudeToDouble(Latitude latitude){
        return latitude.getValue();
    }

    @TypeConverter
    public static Latitude doubleToLatitude(double latitude){
        return new Latitude(latitude);
    }
}
