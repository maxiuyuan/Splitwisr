package com.splitwisr.ui.contacts;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.splitwisr.data.users.User;
import com.splitwisr.data.users.UserRepository;

import java.util.List;

public class ContactsViewModel extends AndroidViewModel {
    private UserRepository userRepository;

    public ContactsViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application);
    }

    LiveData<List<User>> getAllUsers() {
        return userRepository.getAllUsers();
    }

    public void insertUser(User user) {
        userRepository.upsert(user);
    }
}
