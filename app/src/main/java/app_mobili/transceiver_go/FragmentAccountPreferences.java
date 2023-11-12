package app_mobili.transceiver_go;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.room.Room;


public class FragmentAccountPreferences extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private Context context;
    private SharedPreferences prefs;
    private String old_name;
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.account_preferences, rootKey);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;

        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.registerOnSharedPreferenceChangeListener(this);

        old_name = prefs.getString("account_name", "user");
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        // if the user changed account name, update the database name
        if (s.equals("account_name")) {
            final String new_name = sharedPreferences.getString("account_name","user");
            if(!new_name.equals(old_name)){ // new_name and old_name are always initialized
                String[] dbList = context.databaseList();

                //checking if two databases will merge with this change
                for (String dbname : dbList) {
                    if (!dbname.endsWith("-wal") && !dbname.endsWith("-shm")) {
                        if(dbname.equals(new_name) && !old_name.equals(new_name)) {
                            showMergeDialog();
                            return;
                        }
                    }
                }

                DatabaseImportExportUtil.changeDatabaseName(context, old_name, new_name);
                old_name = new_name;
            }
        }
    }
    private void showMergeDialog() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final String new_name = prefs.getString("account_name","user");
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.merge_msg_title)
                .setMessage(R.string.merge_msg_text)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    // User canceled the request
                    prefs.edit().putString("account_name", old_name).apply();
                    onCreatePreferences(null, null);
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