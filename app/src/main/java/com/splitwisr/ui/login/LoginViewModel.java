package com.splitwisr.ui.login;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.google.firebase.auth.FirebaseAuth;
import com.splitwisr.data.balances.Balance;
import com.splitwisr.data.balances.BalanceRepository;
import com.splitwisr.data.users.User;
import com.splitwisr.data.users.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

public class LoginViewModel extends AndroidViewModel {
    private LiveData<List<Balance>> allBalances;
    UserRepository userRepository;

    public LoginViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application);
    }

    public void upsertUser(String email, String firstName, String lastName){
        upsertUser(new User(email,firstName, lastName));
    }

    public void upsertUser(String email){
        upsertUser(new User(email,null,null));
    }

    private void upsertUser(User user){
        userRepository.upsert(user);
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
}
