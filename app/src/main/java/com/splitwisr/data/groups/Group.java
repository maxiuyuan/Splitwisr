package com.splitwisr.data.groups;

import android.app.Person;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.splitwisr.data.persons.Persons;

import java.util.List;

@Entity
public class Group extends Persons {
    @PrimaryKey
    @NonNull
    public String name;

    public List<String> userEmails;

    public Group(@NonNull String name, List<String> userEmails) {
        this.name = name;
        this.userEmails = userEmails;
    }

    public String getName() {
        return name;
    }

    public String getShortName() {
        return name;
    }
}
