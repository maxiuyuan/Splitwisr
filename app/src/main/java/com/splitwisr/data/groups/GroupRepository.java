package com.splitwisr.data.groups;

import android.app.Application;

import com.splitwisr.data.AppDatabase;

import java.util.List;

public class GroupRepository {
    private GroupDao groupDao;

    // TODO: Not good practice to be coupled to application like this
    public GroupRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        groupDao = db.groupDao();
    }

    public List<Group> getAllGroups() {
        return groupDao.allGroups();
    }

    public void insert(String groupName, List<String> userEmails) {
        AppDatabase.databaseWriteExecutor.execute(() -> groupDao.insert(new Group(groupName, userEmails)));
    }
}
