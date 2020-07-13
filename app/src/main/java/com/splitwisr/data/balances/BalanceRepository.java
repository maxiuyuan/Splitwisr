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

    public List<Balance> getBalanceList() { return balanceDao.getBalanceList(); }

    public void insert(Balance balance) {
        StringBuilder email_a = new StringBuilder();
        StringBuilder email_b = new StringBuilder();

        if (!(balance.aEmail.compareTo(balance.bEmail) < 0)) {
            String t = balance.aEmail;
            balance.aEmail = balance.bEmail;
            balance.bEmail = t;
        }

        AppDatabase.databaseWriteExecutor.execute(() -> {
            balanceDao.insertAll(balance);
        });
    }

    public void update(final double d, final String s1, final String s2) {
        StringBuilder email_a = new StringBuilder();
        StringBuilder email_b = new StringBuilder();

        if (s1.compareTo(s2) < 0) {
            email_a.append(s1);
            email_b.append(s2);
        } else {
            email_a.append(s2);
            email_b.append(s1);
        }
        AppDatabase.databaseWriteExecutor.execute(() -> {
            balanceDao.update(d, email_a.toString(), email_b.toString());
        });
    }

    public void update (final Balance b) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            balanceDao.update(b);
        });
    }

    public List<Balance> get(final String s1, final String s2) {
        StringBuilder email_a = new StringBuilder();
        StringBuilder email_b = new StringBuilder();

        if (s1.compareTo(s2) < 0) {
            email_a.append(s1);
            email_b.append(s2);
        } else {
            email_a.append(s2);
            email_b.append(s1);
        }
        return balanceDao.get(email_a.toString(), email_b.toString());
    }
}
