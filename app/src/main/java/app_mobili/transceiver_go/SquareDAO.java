package app_mobili.transceiver_go;

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
    @Query("select * from Square")
    List<Square> getAllSquares();

    // only returns the square if it exists, otherwise returns a null object reference, BEWARE!
    @Query("select * from Square where `X`==:x AND `Y`==:y AND `Length`==:l LIMIT 1")
    Square getSquare(int x, int y, int l);

    // only returns the square if it exists, otherwise returns a null object reference, BEWARE!
    @Query("SELECT * FROM Square WHERE (`Length` == :l) AND (ABS(`X` - :x) <= (:l/2)) AND (ABS(`Y` - :y) <= (:l/2)) ORDER BY ABS(`X` - :x) + ABS(`Y` - :y) LIMIT 1")
    Square getYourSquare(int x, int y, int l);

}
