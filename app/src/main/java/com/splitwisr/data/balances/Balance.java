package com.splitwisr.data.balances;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

@Entity(primaryKeys = {"a_email", "b_email"})
public class Balance {
    @NonNull
    @ColumnInfo(name = "a_email")
    public String aEmail;

    @NonNull
    @ColumnInfo(name = "b_email")
    public String bEmail;

    @NonNull
    @ColumnInfo(name = "total_owing")
    public Integer totalOwing;  // Amount that bEmail owes aEmail

    public Balance(@NonNull String aEmail, @NonNull String bEmail, @NonNull Integer totalOwing) {
        this.aEmail = aEmail;
        this.bEmail = bEmail;
        this.totalOwing = totalOwing;
    }
}
