package com.splitwisr.ui.importcontacts;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProviders;
import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Rect;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.splitwisr.R;
import com.splitwisr.databinding.ImportContactsFragmentBinding;
import java.util.ArrayList;

public class ImportContacts extends Fragment {

    final int CONTACTS_READ_RESULT = 0;
    private ImportContactsViewModel mViewModel;
    private ImportContactsFragmentBinding binding;
    private ImportContactsAdapter importContactsAdapter = new ImportContactsAdapter((index,isChecked) ->{
        mViewModel.toggleContact(index,isChecked);
    } );
    private String[] nameProjection = new String[] {
            ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
            ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME,
            ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME
    };
    private String[] emailProjection = new String[] {
            ContactsContract.CommonDataKinds.Email.DATA,
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mViewModel = ViewModelProviders.of(this).get(ImportContactsViewModel.class);
        binding = ImportContactsFragmentBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.addItemDecoration(new BottomMarginDecoration());
        binding.recyclerView.setAdapter(importContactsAdapter);
        binding.saveFab.setOnClickListener(v->{
            mViewModel.submit();
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.destination_contacts_fragment);
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getContext().checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, CONTACTS_READ_RESULT);
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
                this.getContactList();

            } else {
                Toast.makeText(getActivity(),"Contact Permission is needed! :(",Toast.LENGTH_SHORT).show();

            }
        }
    }

    // Can't really move this to a viewModel because the cursors need activity
    private void getContactList() {
        ContentResolver cr = getActivity().getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
        if (cur == null){
            return;
        }
        ArrayList<ImportContactViewObject> contacts = new ArrayList<>();

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
            if (!email.equals("")){
                contacts.add(new ImportContactViewObject(givenName, familyName, email));
            }
        }
        cur.close();
        mViewModel.contacts = contacts;
        importContactsAdapter.setData(contacts);
    }
}

class BottomMarginDecoration extends RecyclerView.ItemDecoration {

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        // only for the last one
        if (parent.getChildAdapterPosition(view) == parent.getAdapter().getItemCount() - 1) {
            outRect.bottom = 100;
        }
    }
}