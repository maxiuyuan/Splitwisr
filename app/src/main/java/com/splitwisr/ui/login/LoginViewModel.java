package com.splitwisr.ui.login;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.splitwisr.data.users.User;
import com.splitwisr.data.users.UserRepository;

public class LoginViewModel extends AndroidViewModel {
    UserRepository userRepository;

    public LoginViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application);
    }

    public void upsertUser(String email, String firstName, String lastName){
        upsertUser(new User(email,firstName, lastName));
    }

    private void upsertUser(User user){
        userRepository.upsert(user);
    }
}
