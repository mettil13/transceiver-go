package app_mobili.transceiver_go;

import android.util.Pair;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface SquareDAO {
    @Insert
    void addSquare(Square square);
    @Update
    void updateSquare(Square square);
    @Delete
    void deleteSquare(Square square);
    @Query("select * from square")
    List<Square> getAllSquares();
    @Query("select * from square where `X,Y`==:coordinates ")
    Square getSquare(Pair<Float,Float> coordinates);

    // only returns the square if it exists, otherwise square should be added manually
    @Query("SELECT * FROM square WHERE (`Length` == :l) ORDER BY ABS(`X,Y` - :x) + ABS(`X,Y` - :y) LIMIT 1")
    Square returnClosestSquare(float x, float y, int l);
}
