package app_mobili.transceiver_go;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import androidx.annotation.Nullable;
import java.io.FileInputStream;
import java.io.IOException;

public class DatabaseImportUtil {
    private static final int REQUEST_CODE_IMPORT_DB = 123;
    private static final String TAG = "DatabaseImportUtil";

    public static void openDocumentPicker(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/x-sqlite3"); // Specify the MIME type for SQLite databases

        activity.startActivityForResult(intent, REQUEST_CODE_IMPORT_DB);
    }
    
    //todo: check requestCode, or make the fragment check it before calling, it needs to know
    // which result he's dealing with, import on export
    public static void handleDocumentPickerResult(Activity activity, int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_IMPORT_DB && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedUri = data.getData();
            if (selectedUri != null) {
                importDatabase(activity, selectedUri);
            }
        }
    }

    public static void importDatabase(Activity activity, Uri selectedUri) {
        try {
            ContentResolver contentResolver = activity.getContentResolver();
            ParcelFileDescriptor pfd = contentResolver.openFileDescriptor(selectedUri, "r");

            if (pfd != null) {
                FileInputStream input = new FileInputStream(pfd.getFileDescriptor());

                // Your import logic here
                // Read from the input stream and insert data into your Room database

                input.close();
                pfd.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error importing database: " + e.getMessage());
        }
    }
}

