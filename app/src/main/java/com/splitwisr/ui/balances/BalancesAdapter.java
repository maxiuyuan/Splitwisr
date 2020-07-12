package com.splitwisr.ui.balances;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.splitwisr.R;
import com.splitwisr.data.balances.Balance;

import java.util.Collections;
import java.util.List;

public class BalancesAdapter extends RecyclerView.Adapter<BalancesAdapter.BalanceViewHolder> {
    private List<Balance> balances = Collections.emptyList();

    public static class BalanceViewHolder extends RecyclerView.ViewHolder {
        public ConstraintLayout balanceView;
        public BalanceViewHolder(@NonNull ConstraintLayout itemView) {
            super(itemView);
            balanceView = itemView;
        }
    }

    @NonNull
    @Override
    public BalanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ConstraintLayout balanceView = (ConstraintLayout) LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.balance_view, parent, false);

        return new BalanceViewHolder(balanceView);
    }

    @Override
    public void onBindViewHolder(@NonNull BalanceViewHolder holder, int position) {
        Balance balance = balances.get(position);
        TextView userText = holder.balanceView.findViewById(R.id.user_text);
        TextView balanceText = holder.balanceView.findViewById(R.id.balance_text);
        userText.setText(balance.bEmail);
        balanceText.setText("$" + balance.totalOwing);
    }

    @Override
    public int getItemCount() {
        return balances.size();
    }

    public void setData(List<Balance> newBalances) {
        balances = newBalances;
        notifyDataSetChanged();
    }
}
