package com.splitwisr.data.groups;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.List;

@Entity
public class Group {
    @PrimaryKey
    @NonNull
    public String name;

    public List<String> userEmails;

    public Group(@NonNull String name, List<String> userEmails) {
        this.name = name;
        this.userEmails = userEmails;
    }
}
