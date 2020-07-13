package com.splitwisr.ui.balances;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.splitwisr.data.balances.Balance;
import com.splitwisr.data.balances.BalanceRepository;

import java.util.List;

public class BalanceViewModel extends AndroidViewModel {
    private LiveData<List<Balance>> allBalances;

    public BalanceViewModel(@NonNull Application application) {
        super(application);
        BalanceRepository balanceRepository = new BalanceRepository(application);
        allBalances = balanceRepository.getAllBalances();
    }

    LiveData<List<Balance>> getAllBalances() { return allBalances; }
}
