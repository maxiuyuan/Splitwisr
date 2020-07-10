package com.splitwisr.data.balances;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.splitwisr.data.AppDatabase;

import java.util.List;

public class BalanceRepository {

    private BalanceDao balanceDao;
    private LiveData<List<Balance>> allBalances;

    // TODO: Not good practice to be coupled to application like this
    public BalanceRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        balanceDao = db.balanceDao();
        allBalances = balanceDao.getAll();
    }

    public LiveData<List<Balance>> getAllBalances() { return allBalances; }

    public void insert(final Balance balance) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            balanceDao.insertAll(balance);
        });
    }
}
