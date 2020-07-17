package com.splitwisr.ui.balances;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.google.firebase.auth.FirebaseAuth;
import com.splitwisr.data.balances.Balance;
import com.splitwisr.data.balances.BalanceRepository;

import java.util.List;
import java.util.stream.Collectors;

public class BalanceViewModel extends AndroidViewModel {
    private LiveData<List<Balance>> allBalances;
    private final BalanceRepository balanceRepository;

    public BalanceViewModel(@NonNull Application application) {
        super(application);
        balanceRepository = new BalanceRepository(application, getCurrentUserEmail());
        allBalances = balanceRepository.getAllBalances();
    }

    LiveData<List<Balance>> getAllBalances() {
        // This is a just-in-case security feature for extraneous situations :)
        return Transformations.map(allBalances, balances -> balances
                .stream()
                .filter(balance -> getCurrentUserEmail().equals(balance.aEmail) || getCurrentUserEmail().equals(balance.bEmail))
                .collect(Collectors.toList()));
    }

    public String getCurrentUserEmail(){
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        return mAuth.getCurrentUser().getEmail();
    }

    public void refreshBalances() {
        balanceRepository.getLatestBalances();
    }
}
