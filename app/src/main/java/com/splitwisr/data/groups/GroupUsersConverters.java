package com.splitwisr.data.groups;

import androidx.room.TypeConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GroupUsersConverters {
    @TypeConverter
    public static String fromUserEmailsList(List<String> usersEmails) {
        StringBuilder string = new StringBuilder();
        for (String email : usersEmails) {
            string.append(email).append(",");
        }
        return string.toString();
    }

    @TypeConverter
    public static List<String> toUserEmailsList(String concatenatedEmails) {
        return new ArrayList<>(Arrays.asList(concatenatedEmails.split(",")));
    }
}
