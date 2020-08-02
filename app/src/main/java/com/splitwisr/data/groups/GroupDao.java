package com.splitwisr.data.groups;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

@Dao
public abstract class GroupDao {
    @Query("SELECT * FROM `group`")
    abstract LiveData<List<Group>> allGroups();

    @Query("SELECT userEmails FROM `group` WHERE name= :groupName")
    abstract List<String> getEmailsForGroup(String groupName);
}
