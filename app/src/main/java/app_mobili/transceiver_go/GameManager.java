package app_mobili.transceiver_go;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class GameManager {

    static int poolSize = 10; // size of pool of squares among which choose the target one
    Square currentTarget;
    String typeOfDataUsed = "None";

    GameManager(){

    }

    public Square getCurrentTarget(){
        return currentTarget;
    }

    public Square generateNewTarget(List<Square> loadedSquares, String typeOfData){
        if(loadedSquares == null || loadedSquares.size() == 0){
            return null;
        }

        Collections.shuffle(loadedSquares); // randomizes the order of loadedSquares, bringing more entropy to the generation

        switch (typeOfData) {
            case "Noise":
                loadedSquares.sort(new Square.NoiseComparator());
                break;
            case "Network":
                loadedSquares.sort(new Square.NetworkComparator());
                break;
            case "Wi-fi":
                loadedSquares.sort(new Square.WifiComparator());
                break;
            default:
                return null;
        }

        int bound = Math.min(poolSize, loadedSquares.size());
        Random rand = new Random();
        int indexOfTarget = rand.nextInt(bound); // generates a random number between 0 and bound - 1

        typeOfDataUsed = typeOfData;
        currentTarget = loadedSquares.get(indexOfTarget);
        return currentTarget;
    }

    public Boolean isTargetUpdated(List<Square> targetNewReads, String typeOfData){
        if(targetNewReads == null){
            return false;
        }
        if(!Objects.equals(typeOfData, typeOfDataUsed)){
            return true;
        }

        targetNewReads.add(currentTarget);
        for(int i = 1; i < targetNewReads.size(); i++){
            switch (typeOfData) {
                case "Noise":
                    if(targetNewReads.get(i).getLastNoiseMeasurement() != targetNewReads.get(i - 1).getLastNoiseMeasurement()){
                        return true;
                    }
                    break;
                case "Network":
                    if(targetNewReads.get(i).getLastNetworkMeasurement() != targetNewReads.get(i - 1).getLastNetworkMeasurement()){
                        return true;
                    }
                    break;
                case "Wi-fi":
                    if(targetNewReads.get(i).getLastWifiMeasurement() != targetNewReads.get(i - 1).getLastWifiMeasurement()){
                        return true;
                    }
                    break;
                default:
                    return false;
            }
        }
        return false;
    }

}
