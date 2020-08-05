package com.splitwisr.data.users;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.splitwisr.data.persons.Persons;

@Entity
public class User extends Persons {
    @PrimaryKey
    @NonNull
    public String email;

    @ColumnInfo(name = "first_name")
    public String firstName;

    @ColumnInfo(name = "last_name")
    public String lastName;

    public String phone;

    public String password;

    public User(@NonNull String email, @NonNull String firstName, @NonNull String lastName) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getName() {
        return firstName + " " + lastName;
    }

    public String getShortName() {
        return firstName;
    }
}
