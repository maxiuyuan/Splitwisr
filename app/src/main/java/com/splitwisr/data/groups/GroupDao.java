package com.splitwisr.data.groups;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public abstract class GroupDao {
    @Query("SELECT * FROM `group`")
    abstract LiveData<List<Group>> allGroups();

    @Insert
    public abstract void insert(Group group);
}
