package com.splitwisr.data.users;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

@Dao
public abstract class UserDao {
    @Query("SELECT * FROM user")
    abstract LiveData<List<User>> getAll();

    @Query("SELECT * FROM user WHERE email LIKE :email")
    abstract LiveData<User> getUser(String email);

    @Query("SELECT * FROM USER WHERE email LIKE :email")
    abstract User getUserBlocking(String email);

    @Query("SELECT * FROM user")
    abstract List<User> getUserList();

    @Query("SELECT * FROM user WHERE first_name LIKE :first AND last_name LIKE :last LIMIT 1")
    abstract LiveData<User> findByName(String first, String last);

    @Insert
    abstract void insertAll(User... users);

    @Insert
    abstract long insert(User user);

    @Delete
    abstract void delete(User user);

    @Update
    abstract void update(User user);

    @Query("SELECT EXISTS(SELECT * FROM user WHERE user.email = :email)")
    abstract Boolean userExists(String email);

    @Transaction
    public void upsert(User user) {
        if (userExists(user.email)) {
            update(user);
        } else {
            insert(user);
        }
    }
}
