package com.splitwisr.ui.contacts;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.splitwisr.data.balances.Balance;
import com.splitwisr.data.balances.BalanceRepository;
import com.splitwisr.data.users.User;
import com.splitwisr.data.users.UserRepository;

import java.util.List;

public class ContactsViewModel extends AndroidViewModel {
    private UserRepository userRepository;
    private BalanceRepository balanceRepository;

    public ContactsViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application);
        balanceRepository = new BalanceRepository(application);
    }

    LiveData<List<User>> getAllUsers() {
        return userRepository.getAllUsers();
    }

    void newBalance(String newEmail) {
        String aEmail;
        String bEmail;
        if (newEmail.compareTo(getCurrentUserEmail()) < 0) {
            aEmail = newEmail;
            bEmail = getCurrentUserEmail();
        } else {
            aEmail = getCurrentUserEmail();
            bEmail = newEmail;
        }

        balanceRepository.insert(new Balance(aEmail, bEmail, 0d));
    }

    public void insertUser(User user) {
        userRepository.insert(user);
    }

    public String getCurrentUserEmail(){
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        return mAuth.getCurrentUser().getEmail();
    }
}
