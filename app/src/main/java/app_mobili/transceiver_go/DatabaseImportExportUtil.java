package app_mobili.transceiver_go;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;



public class DatabaseImportExportUtil {
    private static final String TAG = "DatabaseImportExportUtil";
    private static final int REQUEST_CODE_IMPORT_DB = 420;
    private static final int REQUEST_CODE_EXPORT_DB = 69;
    private static final String databaseDirectoryPath = "/data/user/0/app_mobili.transceiver_go/databases/";
    private static final String DATABASE_NAME = "squaredb";

    /* -------------------------------------------------------------------------- */
    /*                                Share Utils                                 */
    /* -------------------------------------------------------------------------- */
    // Export the database sharing it to some application chosen by the user
    public static void shareDatabase(Activity activity, String fileName) {
        String currentDBPath = activity.getDatabasePath(DATABASE_NAME).getPath();

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

        shareIntent.putExtra(Intent.EXTRA_TITLE, fileName);

        // Grant read permission to the receiving app
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // Start the activity to share the file
        activity.startActivityForResult(Intent.createChooser(shareIntent, "Share Database"), REQUEST_CODE_EXPORT_DB);
    }

    /* -------------------------------------------------------------------------- */
    /*                                Import Utils                                */
    /* -------------------------------------------------------------------------- */
    // Import a file to the app's internal storage, specifically to the database directory
    public static Intent importFileToDatabaseDirectory() {
        // Create an intent to open the document picker
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        // room databases aren't straight up sqlite databases, you can't import them by
        // specifying the file type
        intent.setType("*/*"); // Specify the MIME type of files you want to allow for import

        // Start the document picker activity
        return intent;
    }

    // Handle the result of the document picker activity
    public static void handleImportFileResult(Activity activity, int resultCode, @Nullable Intent data) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedUri = data.getData();
            if (selectedUri != null) {
                importSelectedFile(activity, selectedUri, "temp");
            }
        }
    }

    // Import the selected file to the app's internal storage, specifically to the database directory
    private static void importSelectedFile(Activity activity, Uri selectedUri, String dbname) {
        try {
            // Get the path of the Room database directory

            Log.println(Log.ASSERT,"Luizo",databaseDirectoryPath);

            File destinationFile = new File(databaseDirectoryPath, dbname);

            Log.println(Log.ASSERT,"Luizo",destinationFile.getPath());

            FileInputStream input = new FileInputStream(activity.getContentResolver().openFileDescriptor(selectedUri, "r").getFileDescriptor());
            FileOutputStream output = new FileOutputStream(destinationFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = input.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }

            input.close();
            output.close();

            Log.d(TAG, "File imported successfully to the database directory.");
        } catch (IOException e) {
            Log.e(TAG, "Error importing file: " + e.getMessage());
        }
    }

}
