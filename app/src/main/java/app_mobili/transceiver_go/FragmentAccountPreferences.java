package app_mobili.transceiver_go;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

public class FragmentAccountPreferences extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.account_preferences, rootKey);
    }
}