package app_mobili.transceiver_go;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

public class FragmentMicCalibration extends Fragment implements NoiseStrength.RecordingListener {
    public FragmentMicCalibration() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_mic_calibration, container, false);

        //setting up buttons
        Button silenceButton = rootView.findViewById(R.id.silence_button);
        Button clapButton = rootView.findViewById(R.id.clap_button);


        // setting up noise class
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());

        NoiseStrength noiseStrength = new NoiseStrength(requireContext());
        noiseStrength.setRecordingListener(this);

        noiseStrength.setClapAmplitude(sharedPreferences.getInt("clap_value", 100));
        noiseStrength.setSilenceAmplitude(sharedPreferences.getInt("silence_value", 0));

        // Create a handler
        Handler handler = new Handler();

        // Define the delay in milliseconds (3.5 seconds = 3500 milliseconds)
        int delayMillis = 3500;

        // Create a runnable to be executed after the delay
        Runnable updateValues = new Runnable() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                sharedPreferences.edit().putInt("silence_value", noiseStrength.getSilenceAmplitude()).apply();
                sharedPreferences.edit().putInt("clap_value", noiseStrength.getClapAmplitude()).apply();

                TextView silenceThreshold = rootView.findViewById(R.id.silence_threshold);
                TextView clapThreshold = rootView.findViewById(R.id.clap_threshold);

                silenceThreshold.setText(noiseStrength.getSilenceAmplitude() + " dBm");
                clapThreshold.setText(noiseStrength.getClapAmplitude() + " dBm");
            }
        };

        // sync values when creating the app
        handler.post(updateValues);

        // buttons listeners
        silenceButton.setOnClickListener(v -> {
            noiseStrength.calibrateSilence();
            handler.postDelayed(updateValues, delayMillis);
        });

        clapButton.setOnClickListener(v -> {
            noiseStrength.calibrateClap();
            handler.postDelayed(updateValues, delayMillis);

        });

        // Inflate the layout for this fragment
        return rootView;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onRecordingFinished(int noise) {
        TextView noiseView = getActivity().findViewById(R.id.noiseInfo);
        noiseView.setText("noise: " + noise);
    }
}
