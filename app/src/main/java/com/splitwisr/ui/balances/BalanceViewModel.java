package com.splitwisr.ui.balances;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.splitwisr.data.balances.Balance;
import com.splitwisr.data.balances.BalanceRepository;

import java.util.List;

public class BalanceViewModel extends AndroidViewModel {
    private BalanceRepository balanceRepository;
    private LiveData<List<Balance>> allBalances;

    public BalanceViewModel(@NonNull Application application) {
        super(application);
        balanceRepository = new BalanceRepository(application);
        allBalances = balanceRepository.getAllBalances();
    }


    List<Balance> getBalanceList() { return  balanceRepository.getBalanceList(); }
    LiveData<List<Balance>> getAllBalances() { return allBalances; }
    public List<Balance> get(final String s1, final String s2) { return balanceRepository.get(s1,s2); }

    public void update(double d, String a, String b) { balanceRepository.update(d,a,b);}
    public void update(Balance b) { balanceRepository.update(b); }
    public void insertBalance(Balance balance) {
        balanceRepository.insert(balance);
    }
    public void newBalance(String a, String b, double c) { balanceRepository.insert(new Balance(a,b,c));}
}