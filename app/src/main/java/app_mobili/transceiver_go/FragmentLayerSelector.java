package app_mobili.transceiver_go;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;

import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.room.Room;

import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.List;

public class FragmentLayerSelector extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.layer_selector, rootKey);
        PreferenceCategory category = findPreference("available_maps");
        //Preference pref = (Preference) findPreference("my_database");

        XmlPullParser parser = getResources().getXml(R.xml.layer_preference_list);
        try {
            parser.next();
            parser.nextTag();
        } catch (Exception e) {
            e.printStackTrace();
        }

        AttributeSet attr = Xml.asAttributeSet(parser);
        int count = attr.getAttributeCount();
        if (count != 0) {

            new Thread(() -> {
                String[] dbList = requireContext().databaseList();
                for (String s : dbList) {
                    if (!s.endsWith("-wal") && !s.endsWith("-shm")) {
                        Preference pref = new CheckBoxPreference(requireContext(), attr);
                        pref.setKey(s);
                        pref.setTitle(s);
                        assert category != null;
                        category.addPreference(pref);
                    }
                }
            }).start();

        }


        Preference deleteDBs = findPreference("delete_maps");
        assert deleteDBs != null;
        deleteDBs.setOnPreferenceClickListener(preference -> {
            new Thread(() -> {
                String[] dbList = requireContext().databaseList();
                List<String> toDeleteList = new ArrayList<>();
                for (String s : dbList) {
                    if (!s.endsWith("-wal") && !s.endsWith("-shm") && !PreferenceManager.getDefaultSharedPreferences(requireContext()).getBoolean(s, true)) {
                        Log.println(Log.ASSERT, "", "I should delete " + s);
                        toDeleteList.add(s);
                    }
                }

                requireActivity().runOnUiThread(() -> {

                    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                    builder.setTitle(R.string.delete_maps_dialog_title)
                        .setMessage(getResources().getString(R.string.delete_maps_dialog_text) + "\n" + toDeleteList)
                        .setPositiveButton(R.string.yes, (dialog, which) -> {
                            for (String s : toDeleteList) {
                                SquareDatabase db = Room.databaseBuilder(requireActivity(), SquareDatabase.class, s).addMigrations(SquareDatabase.migration).build();
                                DatabaseImportExportUtil.deleteDatabase(db, requireContext());
                            }
                            onCreatePreferences(null, null);
                        })
                        .setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss())
                        .show();

                });


            }).start();
            return true;
        });
    }
}