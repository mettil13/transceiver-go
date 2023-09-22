package app_mobili.transceiver_go;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;



public class FileUtil {
    private static final String TAG = "FileUtil";
    private static final int REQUEST_CODE_IMPORT_FILE = 69;

    // Import a file to the app's internal storage, specifically to the database directory
    public static Intent importFileToDatabaseDirectory() {
        // Create an intent to open the document picker
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.setType("*/*"); // Specify the MIME type of files you want to allow for import

        // Start the document picker activity
        return intent;
    }

    // Handle the result of the document picker activity
    public static void handleImportFileResult(Activity activity, int resultCode, @Nullable Intent data) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedUri = data.getData();
            if (selectedUri != null) {
                importSelectedFile(activity, selectedUri);
            }
        }
    }

    // Import the selected file to the app's internal storage, specifically to the database directory
    private static void importSelectedFile(Activity activity, Uri selectedUri) {
        try {
            // Get the path of the Room database directory
            String databaseDirectoryPath = activity.getDatabasePath("squaredb").getParent();

            Log.println(Log.ASSERT,"Luizo",databaseDirectoryPath);

            File destinationFile = new File(databaseDirectoryPath, "uuuh");

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

    // Export a file from app's internal storage to external /files storage of our package (kinda useless, the share function is better)
    public static boolean exportFile(Context context,Activity activity, String fileName) {
        try {
            String currentDBPath = activity.getDatabasePath(fileName).getPath();
            Log.println(Log.ASSERT,"Luizo",currentDBPath);
            File internalFile = new File(currentDBPath);
            File externalFile = new File(context.getExternalFilesDir(null), fileName);

            Log.println(Log.ASSERT,"Luizo",externalFile.getPath());
            FileInputStream input = new FileInputStream(internalFile);
            FileOutputStream output = new FileOutputStream(externalFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = input.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }

            input.close();
            output.close();

            Log.d(TAG, "File exported successfully to external storage.");
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Error exporting file: " + e.getMessage());
            return false;
        }
    }
}
