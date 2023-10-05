package app_mobili.transceiver_go;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;

import androidx.preference.CheckBoxPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.room.Database;
import androidx.room.Room;
import androidx.sqlite.db.SimpleSQLiteQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import org.xmlpull.v1.XmlPullParser;

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
        if(count != 0){

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


    }
}