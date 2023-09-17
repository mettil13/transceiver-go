package app_mobili.transceiver_go;

public class Latitude {
    protected double lat;

    Latitude(double latitude){
        lat = toValidLatitude(latitude);
    }

    public Latitude add(Latitude otherLatitude){
        lat = toValidLatitude(lat + otherLatitude.getValue());
        return this;
    }

    public Latitude add(double otherNumber){
        lat = toValidLatitude(lat + otherNumber);
        return this;
    }

    public Latitude subtract(Latitude otherLatitude){
        lat = toValidLatitude(lat - otherLatitude.getValue());
        return this;
    }

    public Latitude subtract(double otherNumber){
        lat = toValidLatitude(lat - otherNumber);
        return this;
    }

    // returns distance between this latitude and otherLatitude
    public double getDistance(Latitude otherLatitude){
        return Math.abs(lat - otherLatitude.getValue());
    }

    public double getValue() {
        return lat;
    }

    public void setValue(double latitude) {
        lat = toValidLatitude(latitude);
    }

    protected double toValidLatitude(double latitude) {
        if (latitude > 89.99) { // check if latitude is a valid number, 90 degrees glitches the map, so we use 89.99 instead
            latitude = 89.99;
        } else if (latitude < -89.99) {
            latitude = -89.99;
        }
        return latitude;
    }
}
