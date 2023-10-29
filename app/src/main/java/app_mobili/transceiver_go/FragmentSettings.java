package app_mobili.transceiver_go;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import java.util.Objects;

public class FragmentSettings extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private ActivityResultLauncher<Intent> importLauncher;

    private ActivityResultLauncher<String> requestPermissionLauncher;

    public FragmentSettings() {
        // Required empty public constructor
    }

    @SuppressLint("ApplySharedPref")
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        prefs.registerOnSharedPreferenceChangeListener(this);

        // needed for the importing process
        importLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // no permission -> abort operation
                    if (result == null) return;
                    int resultCode = result.getResultCode();
                    Intent data = result.getData();
                    DatabaseImportExportUtil.handleImportFileResult(getActivity(), resultCode, data);
                }
        );

        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            Toast toast = new Toast(requireContext());
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
            if (isGranted) {
                toast.setText(R.string.permission_granted_bg_measurement);
            } else {
                toast.setText(R.string.permission_denied);
            }
            // set automatic measurements to false so the user is forced to enable it again
            sharedPreferences.edit().putBoolean("automatic_measurements", false).apply();
            toast.show();
        });

        Preference shareButton = findPreference("pref_share_button");
        Objects.requireNonNull(shareButton).setOnPreferenceClickListener(preference -> {
            DatabaseImportExportUtil.shareDatabase(getContext(), getActivity());
            return true;
        });

        Preference importButton = findPreference("pref_import_button");
        Objects.requireNonNull(importButton).setOnPreferenceClickListener(preference -> {
            // Check for permission at runtime
            if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                // Request permission
                ActivityCompat.requestPermissions(requireActivity(),
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        420);
            } else {
                importLauncher.launch(DatabaseImportExportUtil.importFileToDatabaseDirectory());
            }
            return true;
        });

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {

        Log.d("Luizo", "preference changed:" + s);
        switch (s) {
            case "automatic_measurements":
                automaticMeasurements(sharedPreferences);
                break;
            case "measure_lte_umps":
            case "measure_noise":
            case "measure_wifi":
                restartService();
                break;
            case "measure_interval":
                // handling invalid case and restarting service
                if(sharedPreferences.getInt("measure_interval", 1) == 0){
                    sharedPreferences.edit().putInt("measure_interval", 1).apply();
                }
                restartService();
                break;

            default:
                break;
        }
    }

    // handles requesting permissions and starting/stopping the service
    private void automaticMeasurements(SharedPreferences sharedPreferences) {
        boolean automaticMeasurements = sharedPreferences.getBoolean("automatic_measurements", false);

        // if enabled, ask permissions, or if they're granted start the service
        if (automaticMeasurements) {
            boolean foreground_permission = true;
            boolean notification_permission = true;

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                foreground_permission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.FOREGROUND_SERVICE) == PackageManager.PERMISSION_GRANTED;
                if (!foreground_permission) requestPermissionLauncher.launch(Manifest.permission.FOREGROUND_SERVICE);
            }
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notification_permission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED;
                if (!notification_permission) requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);

            }

            if (foreground_permission && notification_permission) {
                // Start service
                Intent serviceIntent = new Intent(requireContext(), MeasurementService.class);
                requireActivity().startService(serviceIntent);
            }
        }
        // otherwise stop
        else {
            // Stop service
            Intent serviceIntent = new Intent(requireContext(), MeasurementService.class);
            requireContext().stopService(serviceIntent);
        }
    }

    public void restartService(){
        // Stop service
        Intent serviceIntent = new Intent(requireContext(), MeasurementService.class);
        requireContext().stopService(serviceIntent);
        // Start service
        serviceIntent = new Intent(requireContext(), MeasurementService.class);
        requireActivity().startService(serviceIntent);
    }
}