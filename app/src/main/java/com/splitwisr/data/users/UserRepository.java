package com.splitwisr.data.users;

import android.app.Application;
import androidx.lifecycle.LiveData;

import com.splitwisr.data.AppDatabase;

import java.util.List;

public class UserRepository {

    private UserDao userDao;
    private LiveData<List<User>> allUsers;

    // TODO: Not good practice to be coupled to application like this
    public UserRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        userDao = db.userDao();
        allUsers = userDao.getAll();
    }

    public User getUserBlocking(String email) {
        return userDao.getUserBlocking(email);
    }

    public LiveData<List<User>> getAllUsers() {
        return allUsers;
    }

    public List<User> getUserList() { return userDao.getUserList(); }

    public void upsert(final User user) {
        AppDatabase.databaseWriteExecutor.execute(() -> userDao.upsert(user));
    }
}
