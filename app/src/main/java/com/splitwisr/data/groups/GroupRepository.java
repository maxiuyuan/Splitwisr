package com.splitwisr.data.groups;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.splitwisr.data.AppDatabase;

import java.util.List;

public class GroupRepository {
    private GroupDao groupDao;
    private LiveData<List<Group>> allGroups;

    // TODO: Not good practice to be coupled to application like this
    public GroupRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        groupDao = db.groupDao();
        allGroups = groupDao.allGroups();
    }

    public LiveData<List<Group>> getAllGroups() { return allGroups; }
}
