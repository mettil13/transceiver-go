package app_mobili.transceiver_go;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GameMeasurementDialog extends DialogFragment {
    String typeOfData;
    GameManager gameManager;
    FragmentGameMap gameFragment;
    MeasurementSingleton measurementSingleton;

    GameMeasurementDialog(String typeOfData, GameManager gameManager, FragmentGameMap gameFragment, CoordinateListener coordinateListener) {
        this.typeOfData = typeOfData;
        this.gameManager = gameManager;
        this.gameFragment = gameFragment;
        this.measurementSingleton = MeasurementSingleton.create(getContext(), coordinateListener);
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        List<String> selectedItems = new ArrayList<>();  // selected items

        // skip the first element of the array, which is "None"
        String[] entries = Arrays.stream(getResources().getStringArray(R.array.type_of_data_entries)).skip(1).toArray(size -> new String[size]);

        int indexOfMandatoryType = Arrays.asList(getResources().getStringArray(R.array.type_of_data_values)).indexOf(typeOfData) - 1; // skip the first element of the array, which is "None"

        boolean[] checkedItems = new boolean[entries.length];
        Arrays.fill(checkedItems, false);
        if (indexOfMandatoryType >= 0) {
            checkedItems[indexOfMandatoryType] = true;
            selectedItems.add(getResources().getStringArray(R.array.type_of_data_values)[indexOfMandatoryType + 1]);
        }

        builder.setTitle(getResources().getString(R.string.measurements_and_reward_title))
                .setMultiChoiceItems(entries, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int index, boolean isChecked) {
                String clickedItem = getResources().getStringArray(R.array.type_of_data_values)[index + 1]; // skip the first element of the array, which is "None"
                if (isChecked) {
                    selectedItems.add(clickedItem);
                } else {
                    selectedItems.remove(clickedItem);
                }
            }
        }).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                if (selectedItems.contains(getResources().getStringArray(R.array.type_of_data_values)[indexOfMandatoryType + 1])) {
                    Log.println(Log.ASSERT, "", "" + selectedItems);
                    Activity currentActivity = getActivity();
                    if (currentActivity instanceof MainActivity) {
                        int coinsReward = 0;
                        if (selectedItems.contains("Noise")) {
                            measurementSingleton.takeNoiseMeasurement((MainActivity) currentActivity);
                            coinsReward += getResources().getInteger(R.integer.square_single_measurement_reward);
                        }
                        if (selectedItems.contains("Network")) {
                            measurementSingleton.takeNetworkMeasurement((MainActivity) currentActivity);
                            coinsReward += getResources().getInteger(R.integer.square_single_measurement_reward);
                        }
                        if (selectedItems.contains("Wi-fi")) {
                            measurementSingleton.takeWifiMeasurement((MainActivity) currentActivity);
                            coinsReward += getResources().getInteger(R.integer.square_single_measurement_reward);
                        }
                        addCoins(coinsReward);
                        Toast.makeText(getContext(), getResources().getText(R.string.you_earned) + " " + coinsReward + " " + getResources().getString(R.string.coins), Toast.LENGTH_LONG).show();
                        gameFragment.onCameraIdle();
                    }

                } else {
                    Toast.makeText(getContext(), getResources().getText(R.string.cannot_give_prize) + " " + getResources().getStringArray(R.array.type_of_data_entries)[indexOfMandatoryType + 1], Toast.LENGTH_LONG).show();
                }
            }
        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

            }
        });
        return builder.create();
    }

    private void addCoins (int coins){
        int oldCoins = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt("coins", 0);
        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putInt("coins", oldCoins + coins).apply();
    }
}
