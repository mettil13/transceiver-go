package app_mobili.transceiver_go;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import java.util.Objects;

public class FragmentSettings extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private ActivityResultLauncher<Intent> importLauncher;

    private ActivityResultLauncher<String> requestPermissionLauncher;

    private ActivityResultLauncher<String> requestPermissionForImport;

    private SharedPreferences prefs;

    public FragmentSettings() {
        // Required empty public constructor
    }

    @SuppressLint("ApplySharedPref")
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
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

        requestPermissionForImport = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                importLauncher.launch(DatabaseImportExportUtil.importFileToDatabaseDirectory());
            }
        });


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
            boolean write_external_permission = ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED;

            if(!write_external_permission) {
                requestPermissionForImport.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }

            if (write_external_permission /*|| read_phone_permission*/) {
                importLauncher.launch(DatabaseImportExportUtil.importFileToDatabaseDirectory());
            }
            return true;
        });

        Preference toCalibrationButton = findPreference("to_calibration_button");
        Objects.requireNonNull(toCalibrationButton).setOnPreferenceClickListener(preference -> {
            Fragment micCalibration = new FragmentMicCalibration();
            replaceFragment(R.id.fragmentContainer, micCalibration);
            return true;
        });

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {

        switch (s) {
            case "automatic_measurements":
                automaticMeasurements(sharedPreferences);
                break;
            case "measure_lte_umps":
            case "measure_noise":
            case "measure_wifi":
            case "measure_interval":
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
            boolean background_permission = true;

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                foreground_permission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.FOREGROUND_SERVICE) == PackageManager.PERMISSION_GRANTED;
                if (!foreground_permission) requestPermissionLauncher.launch(Manifest.permission.FOREGROUND_SERVICE);
            }
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notification_permission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
                if (!notification_permission) requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);

            }
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                background_permission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;
                if (!background_permission) requestPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION);

            }


            if (foreground_permission && notification_permission && background_permission) {
                PowerManager powerManager = (PowerManager) requireActivity().getSystemService(Context.POWER_SERVICE);
                    if (!powerManager.isIgnoringBatteryOptimizations( requireContext().getPackageName())) {
                        // App is not exempt from battery optimization; show a pop-up to request exemption.
                        showBatteryOptimizationDialog();
                    }
                }
                // Start service
                Intent serviceIntent = new Intent(requireContext(), MeasurementService.class);
                requireActivity().startService(serviceIntent);
            }
        // otherwise stop
        else {
            // Stop service
            Intent serviceIntent = new Intent(requireContext(), MeasurementService.class);
            requireContext().stopService(serviceIntent);
        }
    }

    private void restartService(){
        // Stop service
        Intent serviceIntent = new Intent(requireContext(), MeasurementService.class);
        requireContext().stopService(serviceIntent);
        // Start service
        serviceIntent = new Intent(requireContext(), MeasurementService.class);
        requireActivity().startService(serviceIntent);
    }

    // containerId is always the same, but function may need to handle different containerIds in the future
    // hence why we left the warning be
    private void replaceFragment(int containerId, Fragment newFragment) {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setReorderingAllowed(true);
        fragmentTransaction.replace(containerId, newFragment).addToBackStack("");
        fragmentTransaction.commit();
    }
    // Function to show a pop-up dialog to request battery optimization exemption
    private void showBatteryOptimizationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.battery_msg_title)
                .setMessage(R.string.battery_msg_text)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    // Open battery optimization settings
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                    startActivity(intent);
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    // User canceled the request
                    dialog.dismiss();
                })
                .show();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        prefs.unregisterOnSharedPreferenceChangeListener(this);
    }
}