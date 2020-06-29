package com.splitwisr.ui.main;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.splitwisr.data.User;
import com.splitwisr.data.UserRepository;

import java.util.List;

public class MainViewModel extends AndroidViewModel {

    private UserRepository userRepository;

    private LiveData<List<User>> allUsers;

    public MainViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application);
        allUsers = userRepository.getAllUsers();
    }

    LiveData<List<User>> getAllUsers() {
        return allUsers;
    }

    public void insert(User user) {
        userRepository.insert(user);
    }
}