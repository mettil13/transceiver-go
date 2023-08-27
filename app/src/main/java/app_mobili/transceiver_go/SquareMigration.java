package app_mobili.transceiver_go;

import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class SquareMigration extends Migration {
    /**
     * Creates a new migration between {@code startVersion} and {@code endVersion}.
     *
     * @param startVersion The start version of the database.
     * @param endVersion   The end version of the database after this migration is applied.
     */
    public SquareMigration(int startVersion, int endVersion) {
        super(startVersion, endVersion);
    }
    @Override
    public void migrate(SupportSQLiteDatabase database) {
        // Drop the old table
        database.execSQL("DROP TABLE IF EXISTS `Square`");

        // Create the new table with the updated schema
        database.execSQL("CREATE TABLE IF NOT EXISTS `Square` " +
                "(`networkAverageCounter` INTEGER NOT NULL, " +
                "`Network Signal Strength` INTEGER NOT NULL, " +
                "`Noise Strength` INTEGER NOT NULL, " +
                "`Length` REAL NOT NULL, " +
                "`X` REAL NOT NULL, " +
                "`Y` REAL NOT NULL, " +
                "`noiseAverageCounter` INTEGER NOT NULL, " +
                "`wifiAverageCounter` INTEGER NOT NULL, " +
                "`SquareID` TEXT NOT NULL PRIMARY KEY, " +
                "`Wifi Signal Strength` INTEGER NOT NULL)");

        // Migrate data from the old table to the new table
        database.execSQL("INSERT INTO `Square` " +
                "(`networkAverageCounter`, " +
                "`Network Signal Strength`, " +
                "`Noise Strength`, " +
                "`Length`, " +
                "`X`, `Y`, " +
                "`noiseAverageCounter`, " +
                "`wifiAverageCounter`, " +
                "`SquareID`, " +
                "`Wifi Signal Strength`) " +
                "SELECT " +
                "`networkAverageCounter`, " +
                "`Network Signal Strength`, " +
                "`Noise Strength`, " +
                "`Length`, " +
                "`X`, `Y`, " +
                "`noiseAverageCounter`, " +
                "`wifiAverageCounter`, " +
                "CAST(`X` AS TEXT) || '|' || CAST(`Y` AS TEXT) || ':' || CAST(`Length` AS TEXT), " +
                "`Wifi Signal Strength` " +
                "FROM `Square`");
    }

}

