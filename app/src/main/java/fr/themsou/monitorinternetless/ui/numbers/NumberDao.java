package fr.themsou.monitorinternetless.ui.numbers;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface NumberDao {
    @Query("SELECT * FROM number")
    List<Number> getAll();


    @Query("SELECT * FROM number WHERE owner LIKE :owner AND number LIKE :number LIMIT 1")
    Number findByName(String owner, String number);

    @Insert
    void insertAll(Number... users);

    @Delete
    void delete(Number user);
}
