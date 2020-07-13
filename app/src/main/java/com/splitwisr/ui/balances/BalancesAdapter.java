package com.splitwisr.ui.balances;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.splitwisr.R;

import java.util.Collections;
import java.util.List;

public class BalancesAdapter extends RecyclerView.Adapter<BalancesAdapter.BalanceViewHolder> {
    private List<BalanceViewObject> balances = Collections.emptyList();

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
        BalanceViewObject balanceViewObject = balances.get(position);
        TextView userText = holder.balanceView.findViewById(R.id.user_text);
        TextView balanceText = holder.balanceView.findViewById(R.id.balance_text);
        userText.setText(balanceViewObject.otherUser);
        balanceText.setText("$" + balanceViewObject.balance);
        if (balanceViewObject.balance == 0d) {
            // pass
        } else if (balanceViewObject.owesOtherUser && balanceViewObject.balance > 0) {
            balanceText.setTextColor(Color.GREEN);
        } else {
            balanceText.setTextColor(Color.RED);
        }
    }

    @Override
    public int getItemCount() {
        return balances.size();
    }

    public void setData(List<BalanceViewObject> newBalances) {
        balances = newBalances;
        notifyDataSetChanged();
    }
}
