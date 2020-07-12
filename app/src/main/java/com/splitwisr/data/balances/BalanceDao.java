package com.splitwisr.data.balances;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface BalanceDao {
    @Query("SELECT * FROM balance")
    LiveData<List<Balance>> getAll();

    @Query("SELECT * FROM balance WHERE a_email=:a_email AND b_email=:b_email")
    List<Balance> get(String a_email, String b_email);

    @Insert
    void insertAll(Balance... balances);

    @Delete
    void delete(Balance balance);

    @Update
    void update(Balance balance);
}
