package com.splitwisr.ui.receipts;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import com.splitwisr.data.balances.Balance;
import com.splitwisr.data.users.User;
import com.splitwisr.databinding.ReceiptFragmentBinding;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ReceiptFragment extends Fragment {
    // Receipt is now broken down into map of ID to item, item class is below
    // ID is just an integer that identifies that item, starts at 0 for the first item
    class ReceiptItem {
        public String name;
        public Double cost;
        public List<String> usersSplitting;

        ReceiptItem(String s, Double d) {
            name = s;
            cost = d;
            usersSplitting = new ArrayList<>();
        }
    }
    private ReceiptsViewModel receiptsViewModel;
    private ReceiptFragmentBinding binding;

    // Maps full name to email
    private HashMap<String,String> userNames = new HashMap<>();

    // Maps email to amount owed to currentUser for this receipt, is only used at submit stage
    private HashMap<String, Double> amountsOwed = new HashMap<>();

    // List of textviews that follow the "ADD USER" buttons per item in the UI
    // When a new set of users is selected for an item, this text view updates
    private List<TextView> usersSplittingItemViews = new ArrayList<>();

    // Maps item ID to item object
    private HashMap<Integer, ReceiptItem> receiptItems = new HashMap<>();

    // Array of strings that denotes the users "in the receipt"
    // Currently is the entire user DB but should eventually become a subset
    // This array is used in the ADD USERS pop-up per item
    private String[] selectableUsers;

    // Params for the buttons in the horizontal linear layout per item
    private final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

    // Current item ID, will increment every time a new item is added
    private int itemId = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = ReceiptFragmentBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        receiptsViewModel = new ViewModelProvider(requireActivity()).get(ReceiptsViewModel.class);
        List<User> users = receiptsViewModel.getUserList();

        // TODO -> make only a subset of users "in the receipt"
        // Generate the userNames map and selectable users array
        for (int x = 0; x < users.size(); x++) {
            String userName = users.get(x).firstName + " " + users.get(x).lastName;
            userNames.put(userName, users.get(x).email);
        }
        selectableUsers = userNames.keySet().toArray(new String[0]);

        binding.addItemButton.setOnClickListener(v -> {
            double tempItemCost = 0d;
            String tempItemName;

            // Check if the itemcost field is currently filled out, if its empty do nothing
            if (!binding.itemCostText.getText().toString().equals("")) {
                tempItemCost = Double.parseDouble(binding.itemCostText.getText().toString());
            }
            tempItemName = binding.itemNameText.getText().toString();

            if (tempItemCost > 0d) {
                // Create new receipt item and add to the map
                receiptItems.put(itemId, new ReceiptItem(tempItemName, tempItemCost));

                // Create new horizontal linear layout for this item
                LinearLayout ll = new LinearLayout(this.getContext());
                ll.setOrientation(LinearLayout.HORIZONTAL);

                // Add item name + item cost textview to linear layout
                TextView itemView = new TextView(this.getContext());
                itemView.setText(tempItemName + " " + tempItemCost);
                ll.addView(itemView);

                // Create add users button and add to linear layout
                Button addUsers = new Button(this.getContext());
                addUsers.setId(itemId);
                addUsers.setLayoutParams(params);
                addUsers.setText("add users");
                addUsers.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectUsers(v.getId());
                    }
                });
                ll.addView(addUsers);

                // Create textview that will show the users currently selected for the item
                TextView userView = new TextView(this.getContext());
                userView.setText("All");
                usersSplittingItemViews.add(userView);
                ll.addView(userView);

                // Add horizontal linearlayout to the vertical linearlayout
                binding.receiptLinearLayout.addView(ll);
                itemId++;
            }
        });

        binding.submitButton.setOnClickListener(v -> {
            // Iterate through all the receipt items
            for (ReceiptItem item : receiptItems.values()) {
                // If the item has no assigned users, then split among all users
                if(item.usersSplitting.size() == 0) {
                    item.usersSplitting = new ArrayList<>(userNames.values());
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
            if (!amountsOwed.isEmpty()){
                amountsOwed.clear();
                //resetReceiptContentsText();
                NavController navController = NavHostFragment.findNavController(this);
                navController.navigate(R.id.destination_balance_fragment);
            }
        });
        return view;
    }

    void selectUsers (int id) {
        // Construct a new popup dialogue that will prompt the user to select the users
        // with which they want to split this item

        boolean[] selectedUsers = new boolean[selectableUsers.length];
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder.setTitle("users splitting").setMultiChoiceItems(selectableUsers, selectedUsers, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                selectedUsers[which] = isChecked;
            }
        });
        builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                addUsersToItem(id, selectedUsers);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    void addUsersToItem(int id, boolean[] selectedUsers) {
        // Based on what the user chose in the popup, update the ReceiptItem object
        // and update the textview that displays the users splitting for this item
        if (receiptItems.containsKey(id)) {
            ReceiptItem item = receiptItems.get(id);
            StringBuilder names = new StringBuilder();
            item.usersSplitting.clear();
            boolean first = true;
            for (int x = 0; x < selectedUsers.length; x++) {
                if (selectedUsers[x]) {
                    item.usersSplitting.add(userNames.get(selectableUsers[x]));
                    if (first) {
                        first = false;
                    } else {
                        names.append(", ");
                    }
                    names.append(selectableUsers[x].split(" ")[0]);
                }
            }
            usersSplittingItemViews.get(id).setText(names.toString());
        }
    }

    // Rounding function for rounding to 2 decimal places
    public static double round(double value) {
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

}
