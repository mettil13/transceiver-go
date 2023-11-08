package app_mobili.transceiver_go;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;


public class FragmentAccountPreferences extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private String old_name;
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.account_preferences, rootKey);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        prefs.registerOnSharedPreferenceChangeListener(this);

        old_name = prefs.getString("account_name", "user");

    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        // if the user changed account name, update the database name
        if (s.equals("account_name")) {
            final String new_name = sharedPreferences.getString("account_name","user");
            String[] dbList = requireContext().databaseList();

            //checking if two databases will merge with this change
            for (String dbname : dbList) {
                if (!dbname.endsWith("-wal") && !dbname.endsWith("-shm")) {
                    if(dbname.equals(new_name) && !old_name.equals(new_name)) {
                        showMergeDialog();
                        return;
                    }
                }
            }

            DatabaseImportExportUtil.changeDatabaseName(requireContext(),old_name, new_name);
            old_name = new_name;
        }
    }
    private void showMergeDialog() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        final String new_name = prefs.getString("account_name","user");
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.merge_msg_title)
                .setMessage(R.string.merge_msg_text)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    // change the db name and proceed with merge action
                    DatabaseImportExportUtil.changeDatabaseName(requireContext(),old_name, new_name);
                    old_name = new_name;
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    // User canceled the request
                    prefs.edit().putString("account_name", old_name).apply();
                    dialog.dismiss();
                })
                .show();
    }
}