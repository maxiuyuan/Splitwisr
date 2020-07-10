package com.splitwisr.ui.main;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.splitwisr.data.Balance;
import com.splitwisr.data.BalanceRepository;
import com.splitwisr.data.User;
import com.splitwisr.data.UserRepository;

import java.util.List;

public class MainViewModel extends AndroidViewModel {

    private UserRepository userRepository;
    private BalanceRepository balanceRepository;

    private LiveData<List<User>> allUsers;
    private LiveData<List<Balance>> allBalances;

    public MainViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application);
        balanceRepository = new BalanceRepository(application);
        allUsers = userRepository.getAllUsers();
        allBalances = balanceRepository.getAllBalances();
    }

    LiveData<List<User>> getAllUsers() {
        return allUsers;
    }
    LiveData<List<Balance>> getAllBalances() { return allBalances; }

    public void insertUser(User user) {
        userRepository.insert(user);
    }
    public void insertBalance(Balance balance) {
        balanceRepository.insert(balance);
    }
}