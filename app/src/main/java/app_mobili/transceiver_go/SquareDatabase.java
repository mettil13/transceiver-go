package app_mobili.transceiver_go;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;

@Database(
        entities={Square.class},
        version = 2)
@TypeConverters({ConvertersForSquareDB.class})
public abstract class SquareDatabase extends RoomDatabase {
    public abstract SquareDAO getSquareDAO();

    static final Migration migration = new SquareMigration(1,2);
}
