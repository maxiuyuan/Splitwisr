package com.splitwisr.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class User {
    @PrimaryKey
    @NonNull
    public String user_id;

    @ColumnInfo(name = "first_name")
    public String firstName;

    @ColumnInfo(name = "last_name")
    public String lastName;

    @ColumnInfo(name = "phone")
    public String phone;

    @ColumnInfo(name = "email")
    public String email;

    @ColumnInfo(name = "password")
    public String password;

    public User(@NonNull String user_id, String firstName) {
        this.user_id = user_id;
        this.firstName = firstName;
    }
}
