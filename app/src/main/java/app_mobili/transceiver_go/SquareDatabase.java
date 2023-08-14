package app_mobili.transceiver_go;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities={Square.class},version = 1)
public abstract class SquareDatabase extends RoomDatabase {
    public abstract SquareDAO getSquareDAO();
}
