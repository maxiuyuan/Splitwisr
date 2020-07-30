package com.splitwisr.ui.importcontacts;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.splitwisr.R;

import java.util.Collections;
import java.util.List;

public class ImportContactsAdapter extends RecyclerView.Adapter<ImportContactsAdapter.ImportContactViewHolder> {
    private List<ImportContactViewObject> contacts = Collections.emptyList();

    public static class ImportContactViewHolder extends RecyclerView.ViewHolder {
        public MaterialCardView importContactsView;
        public ImportContactViewHolder(@NonNull MaterialCardView itemView) {
            super(itemView);
            importContactsView = itemView;
        }
    }

    @NonNull
    @Override
    public ImportContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MaterialCardView importContactsView = (MaterialCardView) LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.import_contacts_view, parent, false);

        return new ImportContactViewHolder(importContactsView);
    }

    @Override
    public void onBindViewHolder(@NonNull ImportContactViewHolder holder, int position) {
        ImportContactViewObject importContactViewObject = contacts.get(position);
        TextView givenName = holder.importContactsView.findViewById(R.id.givenName);
        givenName.setText(importContactViewObject.givenName);

        TextView familyName = holder.importContactsView.findViewById(R.id.familyName);
        familyName.setText(importContactViewObject.lastName);

        TextView email = holder.importContactsView.findViewById(R.id.email);
        email.setText(importContactViewObject.email);

        CheckBox checkBox = holder.importContactsView.findViewById(R.id.checkBox);
        checkBox.setChecked(importContactViewObject.isChecked);
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public void setData(List<ImportContactViewObject> newContacts) {
        contacts = newContacts;
        notifyDataSetChanged();
    }
}