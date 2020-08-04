package com.splitwisr.data.groups;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public abstract class GroupDao {
    @Query("SELECT * FROM `group`")
    abstract List<Group> allGroups();

    @Insert
    public abstract void insert(Group group);
}
