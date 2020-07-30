package com.splitwisr.ui.importcontacts;

public class ImportContactViewObject {
    String givenName;
    String lastName;
    String email;
    boolean isChecked = false;

    public ImportContactViewObject(String givenName, String lastName, String email) {
        this.givenName = givenName;
        this.lastName = lastName;
        this.email = email;
    }

    public ImportContactViewObject(String givenName, String lastName, String email, boolean isChecked) {
        this.givenName = givenName;
        this.lastName = lastName;
        this.email = email;
        this.isChecked = isChecked;
    }
}
