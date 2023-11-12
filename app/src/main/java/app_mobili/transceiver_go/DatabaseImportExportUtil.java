package app_mobili.transceiver_go;

import android.app.Activity;

import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;
import androidx.preference.PreferenceManager;
import androidx.room.Room;

import java.io.File;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;


public class DatabaseImportExportUtil {
    private static final String TAG = "DatabaseImportExportUtil";
    private static final int REQUEST_CODE_EXPORT_DB = 69;



    /* -------------------------------------------------------------------------- */
    /*                                Share Utils                                 */
    /* -------------------------------------------------------------------------- */
    // Export the database sharing it to some application chosen by the user
    // DATABASE MUST BE CLOSE WHEN EXPORTING, OTHERWISE IT WILL BE EMPTY
    public static void shareDatabase(Context context, Activity activity) {
        new Thread(() -> {

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            String dbname = sharedPreferences.getString("account_name", "squaredb");

            String currentDBPath = activity.getDatabasePath(dbname).getPath();

            // Create a content URI using FileProvider
            Uri contentUri = FileProvider.getUriForFile(
                    activity,
                    "app_mobili.transceiver_go.fileprovider", // Use your FileProvider authority
                    new File(currentDBPath)
            );

            // Create an intent to share the file
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/octet-stream");
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);

            shareIntent.putExtra(Intent.EXTRA_TITLE, dbname);

            // Grant read permission to the receiving app
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);


            SquareDatabase squaredb = Room.databaseBuilder(context, SquareDatabase.class, dbname).build();

            // close the db
            if (squaredb.isOpen()) {
                squaredb.close();
            }

            // Start the activity to share the file
            activity.startActivityForResult(Intent.createChooser(shareIntent, "Share Database"), REQUEST_CODE_EXPORT_DB);
        }).start();

    }

    public static void changeDatabaseName(Context context, String oldName, String newName) {
        try {
            // Close the old database
            SquareDatabase oldDatabase = Room.databaseBuilder(context, SquareDatabase.class, oldName).build();
            oldDatabase.close();

            // Get the path to the old and new database files
            File oldDbFile = context.getDatabasePath(oldName);
            File newDbFile = context.getDatabasePath(newName);

            Log.d("dbName", "old: " + oldDbFile.toString());
            Log.d("dbName", "new: " + newDbFile.toString());

            // Rename the old database file to the new name
            if (oldDbFile.renameTo(newDbFile)) {
                // Reopen the database with the new name
                SquareDatabase newDatabase = Room.databaseBuilder(context, SquareDatabase.class, newName).build();
                newDatabase.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        // specifying the file type sqlite-3, they need the BIN type
        intent.setType("application/octet-stream"); // Specifying the MIME type

        // Start the document picker activity
        return intent;
    }

    // Handle the result of the document picker activity
    public static void handleImportFileResult(Activity activity, int resultCode, @Nullable Intent data) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedUri = data.getData();
            if (selectedUri != null) {
                assert DocumentFile.fromSingleUri(activity, selectedUri) != null;
                // asserted, but the warning is still present for some reason
                String fileName = DocumentFile.fromSingleUri(activity, selectedUri).getName();
                importSelectedFile(activity, selectedUri, fileName);
            }
        }
    }

    private static void importSelectedFile(Activity activity, Uri selectedUri, String dbname) {
        try {
            final String databaseDirectoryPath = activity.getDatabasePath(dbname).getParent();

            // Get the path of the Room database directory
            File destinationFile = new File(databaseDirectoryPath, dbname);

            Log.d(TAG, destinationFile.toString());
            // Check if a file with the same name already exists
            if (destinationFile.exists()) {
                // Delete the existing file
                boolean isDeleted = destinationFile.delete();
                if (isDeleted) Log.d(TAG, "Deleted the existing file with the same name.");
            } else {
                Log.d(TAG, "couldn't delete the file, the file is already present and i should update it");
            }

            // Copy the contents of the selected file to the destination file
            InputStream inputStream = activity.getContentResolver().openInputStream(selectedUri);
            OutputStream outputStream;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                outputStream = Files.newOutputStream(destinationFile.toPath());
            }
            // To address warning: we need to use this for api level 24 and 25
            else outputStream = new FileOutputStream(destinationFile);
            byte[] buffer = new byte[1024];
            int length;
            while (true) {
                assert inputStream != null;
                if (!((length = inputStream.read(buffer)) > 0)) break;
                outputStream.write(buffer, 0, length);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();

        } catch (IOException e) {
            Log.e(TAG, "Error importing file: " + e.getMessage());
        }

        // trying to load the database and test
        new Thread(() -> {
            try {
                Looper.prepare();
                // try to build the database to see if it's compatible
                SquareDatabase imported = Room.databaseBuilder(activity, SquareDatabase.class, dbname).build();
                Square e = new Square(69, 420);
                imported.getSquareDAO().upsertSquare(e);

                // if an exception was not thrown, we're good
                Log.d(TAG, "File imported successfully to the database directory.");
                Toast toast = Toast.makeText(activity, R.string.imported, Toast.LENGTH_SHORT);
                toast.show();

            } catch (IllegalStateException e) {
                Log.e(TAG, "Error checking db: " + e.getMessage());

                final String databaseDirectoryPath = activity.getDatabasePath(dbname).getParent();
                File destinationFile = new File(databaseDirectoryPath, dbname);

                boolean isDeleted = destinationFile.delete();
                if (isDeleted) {
                    Toast toast = Toast.makeText(activity, R.string.not_a_db, Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    Log.d(TAG, "couldn't delete the file, the file was probably not a db");

                }
            }
        }).start();
    }


    /* -------------------------------------------------------------------------- */
    /*                                Delete Utils                                */
    /* -------------------------------------------------------------------------- */

    // deletes the database given in input, requires a context
    // returns true if successfully deleted, false otherwise
    public static boolean deleteDatabase(SquareDatabase db, Context context) {

        // Close the database if it's open
        db.close();

        // Get the database file
        File dbFile = context.getDatabasePath(db.getOpenHelper().getDatabaseName());

        // Check if the database file exists and delete it
        if (dbFile.exists()) {
            if (dbFile.delete()) {
                Log.d("DatabaseDeletion", "Database deleted successfully");
                return true;
            } else {
                Log.e("DatabaseDeletion", "Failed to delete database");
                return false;
            }
        } else {
            Log.d("DatabaseDeletion", "Database file does not exist");
            return false;
        }
    }
}
