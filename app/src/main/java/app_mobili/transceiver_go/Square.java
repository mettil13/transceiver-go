package app_mobili.transceiver_go;

import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Square")
public class Square {
    @PrimaryKey()
    @NonNull
    @ColumnInfo(name="SquareID")
    // X,Y,L coordinates
    String coordinates;

    //square centre coordinates can't be non integer numbers so i'll keep 'em this way
    @ColumnInfo(name="X")
    int x;
    @ColumnInfo(name="Y")
    int y;
    @NonNull
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


    public Square(){

    }
    // X,Y coordinates 0 = X , 1 = Y
    // L values are measured in Meters and have to be integers
    public Square(float x, float y, int l){
        // coordinates provided will get rounded to nearest square coordinates
        Pair<Integer,Integer> block = new Pair<>(Math.round(x/l),Math.round(y/l));
        // then to find the X,Y coordinates of square (M,N) we multiply by the requested length l
        // to mathematically get the center of the square we want
        this.x = block.first * l;
        this.y = block.second * l;
        length = l;

        // i'm sorry this primary key has to be a string with this format, but this is the best
        // solution for readability than a meaningless integer id.
        coordinates =this.x+"."+this.y+"/"+this.length;

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

    @Override
    public String toString() {
        return "Square{" +
                "coordinates='" + coordinates + '\'' +
                ", x=" + x +
                ", y=" + y +
                ", length=" + length +
                ", network=" + network +
                ", wifi=" + wifi +
                ", noise=" + noise +
                '}';
    }
}
