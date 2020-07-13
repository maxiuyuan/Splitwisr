package com.splitwisr.data.balances;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface BalanceDao {
    @Query("SELECT * FROM balance")
    LiveData<List<Balance>> getAll();

    @Query("SELECT * FROM balance WHERE a_email=:a_email AND b_email=:b_email")
    List<Balance> get(String a_email, String b_email);

    @Insert
    void insertAll(Balance... balances);

    @Query("UPDATE balance SET total_owing=:totalOwing WHERE a_email=:a_email AND b_email=:b_email")
    void update(double totalOwing, String a_email, String b_email);
}
