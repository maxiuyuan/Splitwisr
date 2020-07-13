package com.splitwisr.ui.receipts;


import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.splitwisr.data.balances.Balance;
import com.splitwisr.data.balances.BalanceRepository;
import com.splitwisr.data.users.User;
import com.splitwisr.data.users.UserRepository;

import java.util.List;

public class ReceiptsViewModel extends AndroidViewModel {
    private UserRepository userRepository;
    private BalanceRepository balanceRepository;

    public ReceiptsViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application);
        balanceRepository = new BalanceRepository(application);
    }

    public List<User> getUserList() { return userRepository.getUserList(); }

    public void update(double d, String a, String b) { balanceRepository.update(d, a, b); }

    public List<Balance> get(final String s1, final String s2) { return balanceRepository.get(s1, s2); }

    public void insertBalance(Balance balance) {
        balanceRepository.insert(balance);
    }

    public String getCurrentUserEmail(){
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        return mAuth.getCurrentUser().getEmail();
    }
}
