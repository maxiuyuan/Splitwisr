package com.splitwisr.data.balances;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.splitwisr.data.users.User;

import java.util.List;

@Dao
public abstract class BalanceDao {
    @Query("SELECT * FROM balance")
    abstract LiveData<List<Balance>> getAll();

    @Query("SELECT * FROM balance")
    abstract List<Balance> getAllBlocking();

    @Query("SELECT * FROM balance WHERE a_email=:a_email AND b_email=:b_email")
    abstract Balance get(String a_email, String b_email);

    @Insert
    abstract void insertAll(Balance... balances);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract void insert(Balance balance);

    @Query("UPDATE balance SET total_owing=:totalOwing WHERE a_email=:a_email AND b_email=:b_email")
    abstract void update(double totalOwing, String a_email, String b_email);

    @Query("SELECT EXISTS(SELECT * FROM balance WHERE a_email = :aEmail AND b_email = :bEmail)")
    abstract Boolean balanceExists(String aEmail, String bEmail);

    @Transaction
    public void upsert( double totalOwing, String aEmail, String bEmail) {
        if (balanceExists(aEmail, bEmail)) {
            update(totalOwing, aEmail, bEmail);
        } else {
            insert(new Balance(aEmail, bEmail, totalOwing));
        }
    }
}
