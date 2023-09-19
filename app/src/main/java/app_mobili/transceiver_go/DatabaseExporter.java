package app_mobili.transceiver_go;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class DatabaseExporter {
    static int REQUEST_MANAGE_PERMISSION_CODE = 987;

    // the function to export the database backup locally
    // automatically requests writing permissions
    public static boolean saveDatabase(Context context, String databaseName) {
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.MANAGE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return DatabaseExporter.exportDatabase(context, databaseName);
        } else {
            // Request storage permission from the user
            Log.println(Log.ASSERT, "click", context+" asking permissions");

            ActivityCompat.requestPermissions(
                    (Activity) context,
                    new String[]{Manifest.permission.MANAGE_EXTERNAL_STORAGE},
                    REQUEST_MANAGE_PERMISSION_CODE);
            return false;
        }
    }

    // function to directly share the DB
    public static void shareDatabase(Context context, String databaseName) {
        // Get the path to the current database file
        File currentDB = context.getDatabasePath(databaseName);

        if (currentDB.exists()) {
            Uri databaseUri = Uri.fromFile(currentDB);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("*/*"); // Set the MIME type to all types of files
            shareIntent.putExtra(Intent.EXTRA_STREAM, databaseUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // You can set a title for the sharing dialog
            shareIntent.putExtra(Intent.EXTRA_TITLE, "Share your Database");

            // Start the activity to share the file
            try {
                context.startActivity(Intent.createChooser(shareIntent, "Share Database"));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(context, "No suitable app to open the file", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "Database file not found", Toast.LENGTH_SHORT).show();
        }
    }

    private static boolean exportDatabase(Context context, String databaseName) {
        try {
            // Path to your app's database file
            String currentDBPath = context.getDatabasePath(databaseName).getAbsolutePath();

            // Path to the desired backup location
            // We should implement a method that lets the user decide the name of the DB, i'd advice to make the user
            // set an username in the options that will be used as the child name
            File backupDBFile = new File(Environment.getExternalStorageDirectory(), "backup_database.db");

            // Copy the database file
            copyFile(new FileInputStream(currentDBPath), new FileOutputStream(backupDBFile));

            Toast.makeText(context, "Database saved!", Toast.LENGTH_SHORT).show();
            Log.d("Database Export", "Database exported to " + backupDBFile.getAbsolutePath());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void copyFile(FileInputStream source, FileOutputStream destination) throws IOException {
        FileChannel sourceChannel = source.getChannel();
        FileChannel destinationChannel = destination.getChannel();
        sourceChannel.transferTo(0, sourceChannel.size(), destinationChannel);
        sourceChannel.close();
        destinationChannel.close();
    }
}

