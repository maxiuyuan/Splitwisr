package com.splitwisr.ui.receipts;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.splitwisr.R;
import com.splitwisr.data.ReceiptItem;
import com.splitwisr.data.balances.Balance;
import com.splitwisr.data.users.User;
import com.splitwisr.databinding.ReceiptFragmentBinding;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReceiptFragment extends Fragment {
    // Receipt is now broken down into map of ID to item, item class is below
    // ID is just an integer that identifies that item, starts at 0 for the first item
    private ReceiptsViewModel receiptsViewModel;
    private ReceiptFragmentBinding binding;

    // List of textviews that follow the "ADD USER" buttons per item in the UI
    // When a new set of users is selected for an item, this text view updates

    private Map<Integer, TextView> usersSplittingItemViews = new HashMap<>();
    private Map<Integer, LinearLayout> itemLinearLayouts = new HashMap<>();


    // Current item ID, will increment every time a new item is added
    private int itemId = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = ReceiptFragmentBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // TODO -> make only a subset of users "in the receipt"

        binding.addItemButton.setOnClickListener(v -> {
            double tempItemCost = 0d;
            String tempItemName;

            // Check if the itemcost field is currently filled out, if its empty do nothing
            if (!binding.itemCostText.getText().toString().equals("")) {
                tempItemCost = Double.parseDouble(binding.itemCostText.getText().toString());
            }
            tempItemName = binding.itemNameText.getText().toString();

            binding.itemCostText.getText().clear();
            binding.itemNameText.getText().clear();

            if (tempItemCost > 0d) {
                addItem(tempItemName, tempItemCost);
            }
        });

        binding.submitButton.setOnClickListener(v -> {
            // Iterate through all the receipt items
            HashMap<String, Double> amountsOwed = new HashMap<>();
            for (ReceiptItem item : receiptsViewModel.getReceiptItemValueSet()) {
                // If the item has no assigned users, then split among all users
                if(item.usersSplitting.size() == 0) {
                    item.usersSplitting = new ArrayList<>(receiptsViewModel.getUserNamesValueSet());
                }

                double costPerUser = round(item.cost/item.usersSplitting.size());
                // For each user for this item, update the amount they owe to the user
                // in the amountsOwed map (local to just this receipt)
                for (String user : item.usersSplitting) {
                    if (!user.equals(receiptsViewModel.getCurrentUserEmail())) {
                        if (!amountsOwed.containsKey(user)) {
                            amountsOwed.put(user, 0d);
                        }
                        amountsOwed.put(user, amountsOwed.get(user) + costPerUser);
                    }
                }

            }
            // This is same as before, use the values from the amountsOwed list
            // To update the balances in the balances db and hit the endpoint
            for (String splitUser : amountsOwed.keySet()) {
                double amountOwed = amountsOwed.get(splitUser);
                if (!splitUser.equals(receiptsViewModel.getCurrentUserEmail())) {
                    Balance balance = receiptsViewModel.get(receiptsViewModel.getCurrentUserEmail(), splitUser);
                    Balance b = null;
                    if (balance != null) {
                        b = balance;
                    } else {
                        receiptsViewModel.insertBalance(new Balance(receiptsViewModel.getCurrentUserEmail(), splitUser, 0d));
                        b = receiptsViewModel.get(receiptsViewModel.getCurrentUserEmail(), splitUser);
                    }
                    if (b.aEmail.equals(receiptsViewModel.getCurrentUserEmail())) {
                        b.totalOwing += amountOwed;
                    } else {
                        b.totalOwing -= amountOwed;
                    }
                    receiptsViewModel.update(b.totalOwing, b.aEmail, b.bEmail);
                }
            }
            amountsOwed.clear();
            //resetReceiptContentsText();
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.destination_balance_fragment);
        });

        // Generate the userNames map and selectable users array
        receiptsViewModel = new ViewModelProvider(requireActivity()).get(ReceiptsViewModel.class);
        List<User> users = receiptsViewModel.getUserList();

        String[] allUsers = new String[users.size()];
        for (int x = 0; x < users.size(); x++) {
            allUsers[x] = users.get(x).firstName + " " + users.get(x).lastName;
        }

        boolean[] selectedUsers = new boolean[users.size()];
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder.setTitle("Users in this receipt").setMultiChoiceItems(allUsers, selectedUsers, (dialog, which, isChecked) -> selectedUsers[which] = isChecked);
        builder.setPositiveButton("Done", (dialog, which) -> {
            boolean first = true;
            HashMap<String,String> t = new HashMap<>();
            List<String> u = new ArrayList<>();
            for(int x = 0; x < selectedUsers.length; x++) {
                if (selectedUsers[x]) {
                    u.add(allUsers[x]);
                    t.put(allUsers[x], users.get(x).email);
                    first = false;
                }
            }
            if (first) {
                NavController navController = NavHostFragment.findNavController(getParentFragment());
                navController.navigate(R.id.destination_balance_fragment);
            } else {
                receiptsViewModel.setSelectableUsers(u.toArray(new String[0]));
                receiptsViewModel.setUserNames(t);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
            NavController navController = NavHostFragment.findNavController(getParentFragment());
            navController.navigate(R.id.destination_balance_fragment);
        });
        builder.show();
        return view;
    }

    void selectUsers (int id) {
        // Construct a new popup dialogue that will prompt the user to select the users
        // with which they want to split this item

        boolean[] selectedUsers = new boolean[receiptsViewModel.getSelectableUserSize()];
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder.setTitle("users splitting").setMultiChoiceItems(receiptsViewModel.getSelectableUsers(), selectedUsers, (dialog, which, isChecked) -> selectedUsers[which] = isChecked);
        builder.setPositiveButton("Done", (dialog, which) -> {
            addUsersToItem(id, selectedUsers);
            dialog.dismiss();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    void addUsersToItem(int id, boolean[] selectedUsers) {
        // Based on what the user chose in the popup, update the ReceiptItem object
        // and update the textview that displays the users splitting for this item
        if (receiptsViewModel.checkReceiptItemExists(id)) {
            ReceiptItem item = receiptsViewModel.getReceiptItem(id);
            StringBuilder names = new StringBuilder();
            item.usersSplitting.clear();
            boolean first = true;
            for (int x = 0; x < selectedUsers.length; x++) {
                if (selectedUsers[x]) {
                    item.usersSplitting.add(receiptsViewModel.getUserName(receiptsViewModel.getSelectableUser(x)));
                    if (first) {
                        first = false;
                    } else {
                        names.append(", ");
                    }
                    names.append(receiptsViewModel.getSelectableUser(x).split(" ")[0]);
                }
            }
            if (first) {
                usersSplittingItemViews.get(id).setText("All");
            } else {
                usersSplittingItemViews.get(id).setText(names.toString());
            }
        }
    }

    // Rounding function for rounding to 2 decimal places
    public static double round(double value) {
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    void addItem (String tempItemName, Double tempItemCost){
        // Create new receipt item and add to the map
        receiptsViewModel.addReceiptItem(itemId, new ReceiptItem(tempItemName, tempItemCost));
        // Create new horizontal linear layout for this item
        LinearLayout ll = new LinearLayout(this.getContext());
        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        ll.setWeightSum(10.0f);

        Button removeItem = new Button(this.getContext());
        removeItem.setId(itemId);
        removeItem.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
        removeItem.setText("X");
        removeItem.setOnClickListener(v -> {
            receiptsViewModel.removeReceiptItem(v.getId());
            binding.receiptLinearLayout.removeView(itemLinearLayouts.get(v.getId()));
        });
        ll.addView(removeItem);

        // Add item name + item cost textview to linear layout
        TextView itemView = new TextView(this.getContext());
        itemView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2.0f));
        itemView.setText(tempItemName);
        ll.addView(itemView);

        TextView priceView = new TextView(this.getContext());
        priceView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.5f));
        priceView.setText("$" + tempItemCost);
        ll.addView(priceView);

        // Create textview that will show the users currently selected for the item
        TextView userView = new TextView(this.getContext());
        userView.setText("All");
        userView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 3.8f));
        usersSplittingItemViews.put(itemId, userView);
        ll.addView(userView);


        // Create add users button and add to linear layout
        Button addUsers = new Button(this.getContext());
        addUsers.setId(itemId);
        addUsers.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.7f));
        addUsers.setText("split");
        addUsers.setOnClickListener(v -> selectUsers(v.getId()));
        ll.addView(addUsers);

        // Add horizontal linearlayout to the vertical linearlayout
        itemLinearLayouts.put(itemId, ll);
        binding.receiptLinearLayout.addView(ll);
        itemId++;
    }

}
