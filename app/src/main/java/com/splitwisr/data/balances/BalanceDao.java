package com.splitwisr.data.balances;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface BalanceDao {
    @Query("SELECT * FROM balance")
    LiveData<List<Balance>> getAll();

    @Insert
    void insertAll(Balance... balances);

    @Delete
    void delete(Balance balance);
}
