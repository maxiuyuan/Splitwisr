package com.splitwisr.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.splitwisr.data.balances.Balance;
import com.splitwisr.data.balances.BalanceDao;
import com.splitwisr.data.groups.Group;
import com.splitwisr.data.groups.GroupDao;
import com.splitwisr.data.groups.GroupUsersConverters;
import com.splitwisr.data.users.User;
import com.splitwisr.data.users.UserDao;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {User.class, Balance.class, Group.class}, version = 1, exportSchema = false)
@TypeConverters({GroupUsersConverters.class})
public abstract class AppDatabase extends RoomDatabase {

    public abstract UserDao userDao();
    public abstract GroupDao groupDao();
    public abstract BalanceDao balanceDao();

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room
                            .databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "splitwisr_db").allowMainThreadQueries()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
