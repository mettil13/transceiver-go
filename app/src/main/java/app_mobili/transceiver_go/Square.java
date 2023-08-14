package app_mobili.transceiver_go;

import android.util.Pair;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Square")
public class Square {
    @PrimaryKey()
    @ColumnInfo(name="X,Y")
    // X,Y,L coordinates 0 = X , 1 = Y
    Pair<Float,Float> coordinates;
    @PrimaryKey()
    @ColumnInfo(name="Length")
    int length;
    @ColumnInfo(name="Network Signal Strength")
    int network;
    @ColumnInfo(name="Wifi Signal Strength")
    int wifi;
    @ColumnInfo(name="Noise Strength")
    int noise;
    int noiseAverageCounter = 1;
    int wifiAverageCounter = 1;
    int networkAverageCounter = 1;


    // X,Y,L coordinates 0 = X , 1 = Y, 2 = L (length)
    // L values are measured in Meters and have to be integers
    public Square(float x, float y, int l){
        // coordinates provided will get rounded to nearest square coordinates
        Pair<Integer,Integer> block = new Pair<>(Math.round(x/l),Math.round(y/l));

        // then to find the X,Y coordinates of square (M,N) we multiply by the requested length l
        // to mathematically get the center of the square we want
        coordinates = new Pair<>((float) block.first * l,(float) block.second * l);
        length = l;
        network = -1;
        wifi = -1;
        noise = -1;
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
}
