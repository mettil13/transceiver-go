package app_mobili.transceiver_go;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.preference.CheckBoxPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.room.Database;
import androidx.room.Room;
import androidx.sqlite.db.SimpleSQLiteQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FragmentLayerSelector extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.layer_selector, rootKey);
        PreferenceCategory category = (PreferenceCategory) findPreference("available_maps");
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
                String[] dbList = getContext().databaseList();
                for (String s : dbList) {
                    if (!s.endsWith("-wal") && !s.endsWith("-shm")) {
                        Preference pref = new CheckBoxPreference(getContext(), attr);
                        pref.setKey(s);
                        pref.setTitle(s);
                        category.addPreference(pref);
                    }
                }
            }).start();

        }


        Preference deleteDBs = findPreference("delete_maps");
        deleteDBs.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(@NonNull Preference preference) {
                new Thread(() -> {
                    String[] dbList = getContext().databaseList();
                    List<String> toDeleteList = new ArrayList<>();
                    for (String s : dbList) {
                        if (!s.endsWith("-wal") && !s.endsWith("-shm") && !PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(s, true)) {
                            Log.println(Log.ASSERT, "", "I should delete " + s);
                            toDeleteList.add(s);
                        }
                    }

                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {

                            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                            builder.setTitle(R.string.delete_maps_dialog_title)
                                .setMessage(getResources().getString(R.string.delete_maps_dialog_text) + "\n" + toDeleteList.toString())
                                .setPositiveButton(R.string.yes, (dialog, which) -> {
                                    for (String s : toDeleteList) {
                                        SquareDatabase db = Room.databaseBuilder(getActivity(), SquareDatabase.class, s).addMigrations(SquareDatabase.migration).build();
                                        DatabaseImportExportUtil.deleteDatabase(db, getContext());
                                    }
                                    onCreatePreferences(null, null);
                                })
                                .setNegativeButton(R.string.no, (dialog, which) -> {
                                    dialog.dismiss();
                                })
                                .show();

                        }
                    });


                }).start();
                return true;
            }
        });
    }
}