package com.splitwisr.ui.importcontacts;

import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProviders;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.splitwisr.R;

import java.util.ArrayList;

public class ImportContacts extends Fragment {

    private static final String TAG = "IMPORT CONTACTS";
    private ImportContactsViewModel mViewModel;

    public static ImportContacts newInstance() {
        return new ImportContacts();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.import_contacts_fragment, container, false);
    }

    /*
     * The detail data row ID. To make a ListView work,
     * this column is required.
     */
    // The primary display name
    // The contact's _ID, to construct a content URI
    // The contact's LOOKUP_KEY, to construct a content URI
    // A permanent link to the contact
    @SuppressLint("InlinedApi")
    private static final String[] PROJECTION =
            {
                    ContactsContract.Data._ID,
            };

    // Perform a query to retrieve the contact's name parts
    private String[] nameProjection = new String[] {
            ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
            ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME,
            ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME
    };

    // Perform a query to retrieve the contact's name parts
    private String[] emailProjection = new String[] {
            ContactsContract.CommonDataKinds.Email.DATA,
    };

    final int CONTACTS_READ_RESULT = 0;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(ImportContactsViewModel.class);
        if (getContext().checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_CONTACTS}, CONTACTS_READ_RESULT);
        }
        if (getContext().checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            this.getContactList();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CONTACTS_READ_RESULT) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // permission was granted, yay! Do the
                // contacts-related task you need to do.
                this.getContactList();

            } else {

                // permission denied, boo! Disable the
                // functionality that depends on this permission.
            }
        }
    }

    private void getContactList() {
        ContentResolver cr = getActivity().getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
        if (cur == null){
            return;
        }
        ArrayList<String> givenNames = new ArrayList<>();
        ArrayList<String> familyNames = new ArrayList<>();
        ArrayList<String> emails = new ArrayList();

        while (cur.moveToNext()) {
            String id = cur.getString(
                    cur.getColumnIndex(ContactsContract.Contacts._ID));
            String email = "";
            String givenName = "";
            String familyName = "";

            Cursor nameCursor = getActivity().getContentResolver().query(
                    ContactsContract.Data.CONTENT_URI,
                    nameProjection,
                    ContactsContract.Data.MIMETYPE + " = '" +
                            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE + "' AND " +
                            ContactsContract.CommonDataKinds.StructuredName.CONTACT_ID
                            + " = ?", new String[] { id }, null);
            Cursor emailCur = getActivity().getContentResolver().query(
                    ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                    emailProjection,
                    ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", new String[]{id},
                    null);

            if (nameCursor != null && nameCursor.moveToFirst()){
                givenName = nameCursor.getString(nameCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME));
                familyName = nameCursor.getString(nameCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME));
                nameCursor.close();
            }
            if (emailCur != null && emailCur.moveToFirst()) {
                email = emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                emailCur.close();
            }
            givenNames.add(givenName);
            familyNames.add(familyName);
            emails.add(email);
            Log.e(TAG, givenName + " " + familyName + " " + email);
        }
        cur.close();
    }
}