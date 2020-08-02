package com.splitwisr.data.groups;

import androidx.room.TypeConverter;

import java.util.Arrays;
import java.util.Collections;
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
        List<String> emails = Collections.emptyList();
        emails.addAll(Arrays.asList(concatenatedEmails.split(",")));
        return emails;
    }
}
