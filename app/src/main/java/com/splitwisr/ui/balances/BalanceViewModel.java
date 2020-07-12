package com.splitwisr.ui.balances;

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

    LiveData<List<User>> getAllUsers() {
        return allUsers;
    }

    List<User> getUserList() { return userRepository.getUserList(); }
    List<Balance> getBalanceList() { return  balanceRepository.getBalanceList(); }
    LiveData<List<Balance>> getAllBalances() { return allBalances; }
    List<Balance> get(final String s1, final String s2) { return balanceRepository.get(s1,s2); }

    public void update(double d, String a, String b) { balanceRepository.update(d,a,b);}
    public void update(Balance b) { balanceRepository.update(b); }
    public void insertUser(User user) {
        userRepository.insert(user);
    }
    public void insertBalance(Balance balance) {
        balanceRepository.insert(balance);
    }
}