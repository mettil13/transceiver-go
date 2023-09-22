package app_mobili.transceiver_go;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class DatabaseImportUtil {
    private static final int REQUEST_CODE_IMPORT_DB = 420;
    private static final String TAG = "DatabaseImportUtil";

    public static void openDocumentPicker(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*"); // Specify the MIME type for SQLite databases

        activity.startActivityForResult(intent, REQUEST_CODE_IMPORT_DB);
    }

    public static void handleDocumentPickerResult(Activity activity, int requestCode, int resultCode, @Nullable Intent data) {
        Log.println(Log.ASSERT,"Luizo", "in handledocpicker");
        if (requestCode == REQUEST_CODE_IMPORT_DB && resultCode == Activity.RESULT_OK && data != null) {
            Log.println(Log.ASSERT,"Luizo", "if positive");
            Uri selectedUri = data.getData();
            if (selectedUri != null) {
                importDatabase(activity, selectedUri, "second");
            }
        }
    }

    public static void importDatabase(Activity activity, Uri selectedUri, String newDatabaseName) {
        try {
            ContentResolver contentResolver = activity.getContentResolver();
            ParcelFileDescriptor pfd = contentResolver.openFileDescriptor(selectedUri, "r");

            if (pfd != null) {
                FileInputStream input = new FileInputStream(pfd.getFileDescriptor());

                // Determine the target directory
                File targetDirectory = activity.getDatabasePath("").getParentFile(); // Use the parent directory of the default Room database directory

                // Ensure the target directory exists
                if (!targetDirectory.exists()) {
                    targetDirectory.mkdirs();
                }

                // Specify the target file path
                String targetFilePath = targetDirectory.getPath() + File.separator + newDatabaseName;

                Log.println(Log.ASSERT,"Luizo", targetFilePath);
                // Create a File object for the target file
                File targetFile = new File(targetFilePath);

                // Create an output stream for the target file
                FileOutputStream output = new FileOutputStream(targetFile);

                // Copy the data from the input stream to the output stream
                byte[] buffer = new byte[1024];
                int length;
                while ((length = input.read(buffer)) > 0) {
                    output.write(buffer, 0, length);
                }

                input.close();
                output.close();

                if (targetFile.exists()) {
                    Log.d(TAG, "Database imported and moved successfully.");
                } else {
                    Log.e(TAG, "Error moving the imported database.");
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error importing database: " + e.getMessage());
        }
    }
}

