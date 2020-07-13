package com.splitwisr.ui.receipts;

import android.app.Activity;
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

import com.codepath.asynchttpclient.AsyncHttpClient;
import com.codepath.asynchttpclient.RequestParams;
import com.codepath.asynchttpclient.callback.TextHttpResponseHandler;
import com.splitwisr.data.balances.Balance;
import com.splitwisr.data.users.User;
import com.splitwisr.databinding.ReceiptFragmentBinding;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Headers;

public class ReceiptFragment extends Fragment {

    // Rounding function for rounding to 2 decimal places
    public static double round(double value) {
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    // User dropdown interaction class
    public class UserDropDownActivity extends Activity implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int pos, long id) {
            selectedUser = (String)parent.getItemAtPosition(pos);
        }

        public void onNothingSelected(AdapterView<?> parent) {
            selectedUser = "";
        }
    }

    public class AddUserButtonOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (!selectedUser.equals("") && !usersToSplitItem.contains(selectedUser)) {
                usersToSplitItem.add(userNames.get(selectedUser));
                userNamesToSplitItem.add(selectedUser);
                addUsersSplittingItemText(selectedUser);
            }
        }
    }

    public class AddItemToReceiptButtonOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            double tempItemCost = 0d;
            String tempItemName;

            if (!binding.ItemCost.getText().toString().equals("")) tempItemCost = Double.parseDouble(binding.ItemCost.getText().toString());
            tempItemName = binding.ItemName.getText().toString();

            if (tempItemCost > 0d && usersToSplitItem.size() > 0) {
                double costPerUser = round(tempItemCost/usersToSplitItem.size());
                StringBuilder s = new StringBuilder("\n" + tempItemName + " - " + Double.toString(tempItemCost) + " ");
                for (int x = 0; x < userNamesToSplitItem.size(); x++) {
                    String user = usersToSplitItem.get(x);
                    String userName = userNamesToSplitItem.get(x);
                    if (!user.equals(currentUserEmail)) {
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
        }
    }

    public class SubmitReceiptButtonOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            for (String splitUser : amountsOwed.keySet()) {
                double amountOwed = amountsOwed.get(splitUser);
                if (!splitUser.equals(currentUserEmail)) {
                    List<Balance> balances = receiptsViewModel.get(currentUserEmail, splitUser);
                    Balance b = null;
                    if (balances.size() > 0) {
                        b = balances.get(0);
                    } else {
                        receiptsViewModel.insertBalance(new Balance(currentUserEmail, splitUser, 0d));
                    }
                    if (b.aEmail.equals(currentUserEmail)) {
                        b.totalOwing += amountOwed;
                    } else {
                        b.totalOwing -= amountOwed;
                    }
                    receiptsViewModel.update(b.totalOwing, b.aEmail, b.bEmail);

                    RequestParams params = new RequestParams();
                    params.put("payer", b.aEmail);
                    params.put("payee", b.bEmail);
                    params.put("balance", b.totalOwing.toString());
                    asyncClient.post(basePath, params, "", new TextHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Headers headers, String response) {
                                    // Do nothing
                                }

                                @Override
                                public void onFailure(int statusCode, Headers headers, String errorResponse, Throwable t) {
                                    // Do nothing
                                }
                            }
                    );
                }
            }
            amountsOwed.clear();
            resetReceiptContentsText();
        }
    }

    public void resetUsersSplittingItemText() {
        usersSplittingItemText = new StringBuilder("Users to Split Item:");
        binding.UsersSplittingItem.setText(usersSplittingItemText.toString());
    }

    public void resetReceiptContentsText() {
        receiptContentsText = new StringBuilder("Receipt Contents:");
        binding.ReceiptContents.setText(receiptContentsText.toString());
    }

    public void addUsersSplittingItemText(String user) {
        usersSplittingItemText.append("\n").append(user);
        binding.UsersSplittingItem.setText(usersSplittingItemText.toString());
    }

    public void addReceiptContentsText(String s) {
        receiptContentsText.append(s);
        binding.ReceiptContents.setText(receiptContentsText.toString());
    }

    private ReceiptsViewModel receiptsViewModel;

    StringBuilder usersSplittingItemText = new StringBuilder();
    StringBuilder receiptContentsText = new StringBuilder();

    private ReceiptFragmentBinding binding;

    private String currentUserEmail;

    private List<String> usersToSplitItem = new ArrayList<>();
    private List<String> userNamesToSplitItem = new ArrayList<>();

    private HashMap<String,String> userNames = new HashMap<>();
    private HashMap<String, Double> amountsOwed = new HashMap<>();

    private String selectedUser = "";

    private static AsyncHttpClient asyncClient;
    private static String basePath = "https://ece452project.herokuapp.com/write";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        receiptsViewModel = new ViewModelProvider(requireActivity()).get(ReceiptsViewModel.class);
        currentUserEmail = receiptsViewModel.getCurrentUserEmail();

        binding = ReceiptFragmentBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        List<User> users = receiptsViewModel.getUserList();

        if (users != null) {
            for (int x = 0; x < users.size(); x++) {
                String userName = users.get(x).firstName + " " + users.get(x).lastName;
                userNames.put(userName, users.get(x).email);
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, userNames.keySet().toArray(new String[0]));
            binding.UserDropDown.setAdapter(adapter);
        } else {
            String[] s = new String[]{"no users"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, s);
            binding.UserDropDown.setAdapter(adapter);
        }

        binding.UserDropDown.setOnItemSelectedListener(new UserDropDownActivity());
        binding.AddUserToItemButton.setOnClickListener(new AddUserButtonOnClickListener());
        binding.AddItemButton.setOnClickListener(new AddItemToReceiptButtonOnClickListener());
        binding.SubmitReceiptButton.setOnClickListener(new SubmitReceiptButtonOnClickListener());

        resetReceiptContentsText();
        resetUsersSplittingItemText();

        asyncClient = new AsyncHttpClient();

        return view;
    }
}
