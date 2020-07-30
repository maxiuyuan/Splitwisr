package com.splitwisr.ui.importcontacts;

import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

public class ImportContactsViewModel extends ViewModel {
    public ArrayList<ImportContactViewObject> contacts = new ArrayList<>();

    public void toggleContact(int index, boolean isChecked){
        contacts.get(index).isChecked = isChecked;
    }

    public void submit(){

    }
}