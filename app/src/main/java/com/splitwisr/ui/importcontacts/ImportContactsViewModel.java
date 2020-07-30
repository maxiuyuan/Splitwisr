package com.splitwisr.ui.importcontacts;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModel;

import com.splitwisr.data.users.User;
import com.splitwisr.data.users.UserRepository;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class ImportContactsViewModel extends AndroidViewModel {
    public ArrayList<ImportContactViewObject> contacts = new ArrayList<>();
    private UserRepository userRepository;

    public ImportContactsViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application);
    }

    public void toggleContact(int index, boolean isChecked){
        contacts.get(index).isChecked = isChecked;
    }

    public void submit(){
    List<User> users = contacts.stream()
            .filter(contact-> contact.isChecked)
            .map(contact-> new User(contact.email, contact.givenName, contact.lastName))
            .collect(toList());
    new Thread(() -> userRepository.insertAll(users)).start();
    }
}