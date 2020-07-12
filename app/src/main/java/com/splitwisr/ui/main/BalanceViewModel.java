package com.splitwisr.ui.main;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.splitwisr.data.balances.Balance;
import com.splitwisr.data.balances.BalanceRepository;
import com.splitwisr.data.users.UserRepository;

import java.util.List;

public class BalanceViewModel extends AndroidViewModel {
    private UserRepository userRepository;
    private BalanceRepository balanceRepository;
    private LiveData<List<Balance>> allBalances;

    public BalanceViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application);
        balanceRepository = new BalanceRepository(application);
        allBalances = balanceRepository.getAllBalances();
    }

    LiveData<List<Balance>> getAllBalances() { return allBalances; }

    public void insertBalance(Balance balance) {
        balanceRepository.insert(balance);
    }
}