package app_mobili.transceiver_go;

public class Longitude {
    protected double lng;

    Longitude(double longitude){
        lng = toValidLongitude(longitude);
    }

    public Longitude add(Longitude otherLongitude){
        lng = toValidLongitude(lng + otherLongitude.getValue());
        return this;
    }

    public Longitude add(double otherNumber){
        lng = toValidLongitude(lng + otherNumber);
        return this;
    }

    public Longitude subtract(Longitude otherLongitude){
        lng = toValidLongitude(lng - otherLongitude.getValue());
        return this;
    }

    public Longitude subtract(double otherNumber){
        lng = toValidLongitude(lng - otherNumber);
        return this;
    }

    // returns distance between this longitude and otherLongitude in the counter-clockwise direction
    public double getDistance(Longitude otherLongitude){
        if(lng < otherLongitude.getValue()){
            return Math.abs(otherLongitude.getValue() - lng);
        }
        else {
            return 180 - lng + 180 + otherLongitude.getValue();
        }
    }

    public double getValue() {
        return lng;
    }

    public void setValue(double longitude) {
        lng = toValidLongitude(longitude);
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
