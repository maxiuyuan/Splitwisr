package com.splitwisr.ui.receipts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.splitwisr.data.balances.Balance;
import com.splitwisr.data.users.User;
import com.splitwisr.databinding.ReceiptFragmentBinding;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ReceiptFragment extends Fragment {
    private ReceiptsViewModel receiptsViewModel;
    private ReceiptFragmentBinding binding;

    StringBuilder usersSplittingItemText = new StringBuilder();
    StringBuilder receiptContentsText = new StringBuilder();

    private List<String> usersToSplitItem = new ArrayList<>();
    private List<String> userNamesToSplitItem = new ArrayList<>();
    private HashMap<String,String> userNames = new HashMap<>();
    private HashMap<String, Double> amountsOwed = new HashMap<>();
    private String selectedUser = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = ReceiptFragmentBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        receiptsViewModel = new ViewModelProvider(requireActivity()).get(ReceiptsViewModel.class);
        List<User> users = receiptsViewModel.getUserList();

        if (users != null) {
            for (int x = 0; x < users.size(); x++) {
                String userName = users.get(x).firstName + " " + users.get(x).lastName;
                userNames.put(userName, users.get(x).email);
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_spinner_dropdown_item,
                    userNames.keySet().toArray(new String[0])
            );
            binding.userDropDown.setAdapter(adapter);
        } else {
            String[] s = new String[]{"no users"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_spinner_dropdown_item,
                    s
            );
            binding.userDropDown.setAdapter(adapter);
        }

        binding.userDropDown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                selectedUser = (String) parent.getItemAtPosition(pos);
            }


            public void onNothingSelected(AdapterView<?> parent) {
                selectedUser = "";
            }
        });

        binding.addUserToItemButton.setOnClickListener(v -> {
            if (!selectedUser.equals("") && !usersToSplitItem.contains(selectedUser)) {
                usersToSplitItem.add(userNames.get(selectedUser));
                userNamesToSplitItem.add(selectedUser);
                addUsersSplittingItemText(selectedUser);
            }
        });

        binding.addItemButton.setOnClickListener(v -> {
            double tempItemCost = 0d;
            String tempItemName;

            if (!binding.itemCostText.getText().toString().equals("")) {
                tempItemCost = Double.parseDouble(binding.itemCostText.getText().toString());
            }
            tempItemName = binding.itemNameText.getText().toString();

            if (tempItemCost > 0d && usersToSplitItem.size() > 0) {
                double costPerUser = round(tempItemCost/usersToSplitItem.size());
                StringBuilder s = new StringBuilder("\n" + tempItemName + " - " + tempItemCost + " ");
                for (int x = 0; x < userNamesToSplitItem.size(); x++) {
                    String user = usersToSplitItem.get(x);
                    String userName = userNamesToSplitItem.get(x);
                    if (!user.equals(receiptsViewModel.getCurrentUserEmail())) {
                        if (!amountsOwed.containsKey(user)) {
                            amountsOwed.put(user, 0d);
                        }
                        amountsOwed.put(user, amountsOwed.get(user) + costPerUser);
                    }
                    s.append(userName);
                    if (x != userNamesToSplitItem.size() - 1) s.append(", ");
                }
                addReceiptContentsText(s.toString());
                usersToSplitItem.clear();
                userNamesToSplitItem.clear();
                resetUsersSplittingItemText();
            }
        });

        binding.submitButton.setOnClickListener(v -> {
            for (String splitUser : amountsOwed.keySet()) {
                double amountOwed = amountsOwed.get(splitUser);
                if (!splitUser.equals(receiptsViewModel.getCurrentUserEmail())) {
                    Balance balance = receiptsViewModel.get(receiptsViewModel.getCurrentUserEmail(), splitUser);
                    Balance b = null;
                    if (balance != null) {
                        b = balance;
                    } else {
                        receiptsViewModel.insertBalance(new Balance(receiptsViewModel.getCurrentUserEmail(), splitUser, 0d));
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
            resetReceiptContentsText();
        });

        resetReceiptContentsText();
        resetUsersSplittingItemText();

        return view;
    }

    // Rounding function for rounding to 2 decimal places
    public static double round(double value) {
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public void resetUsersSplittingItemText() {
        usersSplittingItemText = new StringBuilder("Users to Split Item:");
        binding.usersSplittingItem.setText(usersSplittingItemText.toString());
    }

    public void resetReceiptContentsText() {
        receiptContentsText = new StringBuilder("Receipt Contents:");
        binding.receiptContents.setText(receiptContentsText.toString());
    }

    public void addUsersSplittingItemText(String user) {
        usersSplittingItemText.append("\n").append(user);
        binding.usersSplittingItem.setText(usersSplittingItemText.toString());
    }

    public void addReceiptContentsText(String s) {
        receiptContentsText.append(s);
        binding.receiptContents.setText(receiptContentsText.toString());
    }
}
