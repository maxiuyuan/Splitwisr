package com.splitwisr.ui.contacts;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.abdeveloper.library.MultiSelectModel;
import com.splitwisr.data.users.User;
import com.splitwisr.data.users.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    public ArrayList<MultiSelectModel> getUserMultiSelectList() {
        List<User> users = userRepository.getUserList();
        return IntStream
                .range(0, users.size())
                .mapToObj(i -> new MultiSelectModel(i, users.get(i).firstName + " " + users.get(i).lastName))
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
