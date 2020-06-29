package com.splitwisr.data;

import android.app.Application;
import androidx.lifecycle.LiveData;
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

    public LiveData<List<User>> getAllUsers() {
        return allUsers;
    }

    public void insert(final User user) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            userDao.insertAll(user);
        });
    }
}
