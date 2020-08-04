package com.splitwisr.ui.contacts;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.splitwisr.R;

import java.util.ArrayList;
import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {
    private List<String> users = new ArrayList<>();

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        public ConstraintLayout userView;
        public UserViewHolder(@NonNull ConstraintLayout itemView) {
            super(itemView);
            userView = itemView;
        }
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ConstraintLayout userView = (ConstraintLayout) LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.user_view, parent, false);
        return new UserViewHolder(userView);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        String userName = users.get(position);
        TextView nameText = holder.userView.findViewById(R.id.name_text);
        nameText.setText(userName);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void setData(@NonNull List<String> newUsers) {
        users = newUsers;
        notifyDataSetChanged();
    }

    public void addData(@NonNull List<String> newGroups) {
        users.addAll(newGroups);
        notifyDataSetChanged();
    }
}
