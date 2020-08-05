package com.splitwisr.ui.receipts;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.splitwisr.R;
import com.splitwisr.data.persons.Persons;
import com.splitwisr.data.users.User;
import com.splitwisr.ui.receipts.ReceiptsViewObject;

import java.util.Collections;
import java.util.List;


public class ReceiptsAdapater extends RecyclerView.Adapter<ReceiptsAdapater.ReceiptsViewHolder> {

    private List<ReceiptsViewObject> receipts = Collections.emptyList();
    private DeleteItemCallBack deleteItemCallBack;
    private SplitCallBack splitCallBack;

    public ReceiptsAdapater(DeleteItemCallBack deleteItemCallBack, SplitCallBack splitCallBack) {
        this.splitCallBack = splitCallBack;
        this.deleteItemCallBack = deleteItemCallBack;
    }

    public static class ReceiptsViewHolder extends RecyclerView.ViewHolder {
        public MaterialCardView receiptsItemView;
        public ReceiptsViewHolder(@NonNull MaterialCardView itemView) {
            super(itemView);
            receiptsItemView = itemView;
        }
    }

    @NonNull
    @Override
    public ReceiptsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MaterialCardView receiptsItemView = (MaterialCardView) LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.receipt_item_view, parent, false);

        return new ReceiptsViewHolder(receiptsItemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ReceiptsViewHolder holder, int position) {
        ReceiptsViewObject receiptsViewObject = receipts.get(position);

        TextView deleteButton = holder.receiptsItemView.findViewById(R.id.delete_item);
        deleteButton.setOnClickListener(v-> this.deleteItemCallBack.callback(position));
        Button splitButton = holder.receiptsItemView.findViewById(R.id.split_button);
        splitButton.setOnClickListener(v->this.splitCallBack.callback(position));

        TextView itemName = holder.receiptsItemView.findViewById(R.id.item_name);
        TextView itemCost = holder.receiptsItemView.findViewById(R.id.item_cost);
        TextView splitWith = holder.receiptsItemView.findViewById(R.id.split_with);

        itemName.setText(receiptsViewObject.itemName);
        itemCost.setText("$ ".concat(receiptsViewObject.itemCost));

        StringBuilder splitWithSB = new StringBuilder();
        if (receiptsViewObject.splitWith != null){
            for (int x = 0; x < receiptsViewObject.splitWith.size(); x++){
                String s = x < receiptsViewObject.splitWith.size()-1 ? receiptsViewObject.splitWith.get(x).getShortName().concat(", ") : receiptsViewObject.splitWith.get(x).getShortName();
                splitWithSB.append(s);
            }
            splitWith.setText(splitWithSB.toString());
        }
    }

    @Override
    public int getItemCount() {
        return receipts.size();
    }

    public void setData(List<ReceiptsViewObject> newReceipts) {
        receipts = newReceipts;
        notifyDataSetChanged();
    }
}