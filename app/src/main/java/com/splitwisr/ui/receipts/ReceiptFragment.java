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

    StringBuilder usersSplittingItemText = new StringBuilder();
    StringBuilder receiptContentsText = new StringBuilder();

    private List<String> usersToSplitItem = new ArrayList<>();
    private List<String> userNamesToSplitItem = new ArrayList<>();
    private HashMap<String,String> userNames = new HashMap<>();
    private HashMap<String, Double> amountsOwed = new HashMap<>();
    private String selectedUser = "";

    private List<TextView> usersSplittingItemViews = new ArrayList<>();

    private HashMap<Integer, ReceiptItem> receiptItems = new HashMap<>();

    private String[] selectableUsers;

    private final String itemTextBaseId = "ItemText";
    private final String itemButtonBaseId = "ItemButton";

    private final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

    private int itemId = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = ReceiptFragmentBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        receiptsViewModel = new ViewModelProvider(requireActivity()).get(ReceiptsViewModel.class);
        List<User> users = receiptsViewModel.getUserList();

        for (int x = 0; x < users.size(); x++) {
            String userName = users.get(x).firstName + " " + users.get(x).lastName;
            userNames.put(userName, users.get(x).email);
        }
        selectableUsers = userNames.keySet().toArray(new String[0]);

        binding.addItemButton.setOnClickListener(v -> {
            double tempItemCost = 0d;
            String tempItemName;

            if (!binding.itemCostText.getText().toString().equals("")) {
                tempItemCost = Double.parseDouble(binding.itemCostText.getText().toString());
            }
            tempItemName = binding.itemNameText.getText().toString();

            if (tempItemCost > 0d) {
                receiptItems.put(itemId, new ReceiptItem(tempItemName, tempItemCost));
//                double costPerUser = round(tempItemCost/usersToSplitItem.size());
//                StringBuilder s = new StringBuilder("\n" + tempItemName + " - " + tempItemCost + " ");
//                for (int x = 0; x < userNamesToSplitItem.size(); x++) {
//                    String user = usersToSplitItem.get(x);
//                    String userName = userNamesToSplitItem.get(x);
//                    if (!user.equals(receiptsViewModel.getCurrentUserEmail())) {
//                        if (!amountsOwed.containsKey(user)) {
//                            amountsOwed.put(user, 0d);
//                        }
//                        amountsOwed.put(user, amountsOwed.get(user) + costPerUser);
//                    }
//                    s.append(userName);
//                    if (x != userNamesToSplitItem.size() - 1) s.append(", ");
//                }
//                addReceiptContentsText(s.toString());
//                usersToSplitItem.clear();
//                userNamesToSplitItem.clear();
//                resetUsersSplittingItemText();
                LinearLayout ll = new LinearLayout(this.getContext());
                ll.setOrientation(LinearLayout.HORIZONTAL);
                TextView itemView = new TextView(this.getContext());
                itemView.setText(tempItemName + " " + tempItemCost);
                ll.addView(itemView);
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
                TextView userView = new TextView(this.getContext());
                userView.setText("");
                usersSplittingItemViews.add(userView);
                ll.addView(userView);
                binding.receiptLinearLayout.addView(ll);
                itemId++;
            }
        });

        binding.submitButton.setOnClickListener(v -> {
            for (ReceiptItem item : receiptItems.values()) {
                
                double costPerUser = round(item.cost/item.usersSplitting.size());

            }
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
