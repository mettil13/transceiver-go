package app_mobili.transceiver_go;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

public class DatabaseExportUtil {
    private static final String TAG = "DatabaseExportUtil";

    private static final int REQUEST_CODE_EXPORT_DB = 69;

    // Replace with your Room database name
    private static final String DATABASE_NAME = "squaredb";

    public static Intent exportDatabaseIntent() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/x-sqlite3");
        intent.putExtra(Intent.EXTRA_TITLE, DATABASE_NAME+".db");
        return intent;
    }

    public static void onActivityResult(int resultCode, @Nullable Intent data, @NonNull Activity activity, @NonNull ContentResolver contentResolver) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            Uri exportUri = data.getData();

            if (exportUri != null) {
                try {
                    DocumentFile documentFile = DocumentFile.fromSingleUri(activity, exportUri);

                    if (documentFile != null && documentFile.exists()) {
                        String currentDBPath = activity.getDatabasePath(DATABASE_NAME).getPath();
                        Log.println(Log.ASSERT,"Luizo", currentDBPath);
                        FileInputStream input = new FileInputStream(currentDBPath);
                        OutputStream output = contentResolver.openOutputStream(exportUri);

                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = input.read(buffer)) > 0) {
                            output.write(buffer, 0, length);
                        }

                        input.close();
                        output.close();

                        Log.d(TAG, "Database exported successfully.");
                    } else {
                        Log.e(TAG, "DocumentFile not found or does not exist.");
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error exporting database: " + e.getMessage());
                }
            }
        }
    }
    public static void shareDatabase(Activity activity) {
        String currentDBPath = activity.getDatabasePath(DATABASE_NAME).getPath();
        Log.println(Log.ASSERT,"Luizo",currentDBPath);

        // Create a content URI using FileProvider
        Uri contentUri = FileProvider.getUriForFile(
                activity,
                "app_mobili.transceiver_go.fileprovider", // Use your FileProvider authority
                new File(currentDBPath)
        );

        // Create an intent to share the file
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/x-sqlite3");
        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);

        // Grant read permission to the receiving app
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // Start the activity to share the file
        activity.startActivityForResult(Intent.createChooser(shareIntent, "Share Database"), REQUEST_CODE_EXPORT_DB);
    }
}
