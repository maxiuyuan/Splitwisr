package com.splitwisr.ui.importcontacts;

public class ImportContactViewObject {
    String givenName;
    String lastName;
    String email;
    public boolean isChecked = true;

    public ImportContactViewObject(String givenName, String lastName, String email) {
        this.givenName = givenName;
        this.lastName = lastName;
        this.email = email;
    }
}

