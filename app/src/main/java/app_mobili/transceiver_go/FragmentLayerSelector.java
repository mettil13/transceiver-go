package app_mobili.transceiver_go;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

public class FragmentLayerSelector extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.layer_selector, rootKey);
    }
}