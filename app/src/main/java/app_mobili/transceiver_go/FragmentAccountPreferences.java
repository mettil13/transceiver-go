package app_mobili.transceiver_go;

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
            DatabaseImportExportUtil.changeDatabaseName(requireContext(),old_name, new_name);
        }
    }
}