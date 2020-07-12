package com.splitwisr.ui.main;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.splitwisr.R;
import com.splitwisr.data.balances.Balance;
import com.splitwisr.data.users.User;
import com.splitwisr.databinding.ReceiptFragmentBinding;
import com.splitwisr.ui.balances.BalanceViewModel;
import com.splitwisr.ui.contacts.ContactsViewModel;

import org.w3c.dom.Text;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

    public class ItemCostOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (!itemCost.getText().toString().equals("")) {
                currentItemCost = Double.parseDouble(itemCost.getText().toString());
            }
        }
    }

    public class ItemNameOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            currentItemName = itemName.getText().toString();
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
            if (currentItemCost > 0d && usersToSplitItem.size() > 0) {
                double costPerUser = round(currentItemCost/usersToSplitItem.size());
                StringBuilder s = new StringBuilder("\n" + currentItemName + " - " + Double.toString(currentItemCost) + " ");
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
                    List<Balance> balances = balanceViewModel.get(currentUserEmail, splitUser);
                    Balance b = null;
                    if (balances.size() > 0) {
                        b = balances.get(0);
                    } else {
                        balanceViewModel.insertBalance(new Balance(currentUserEmail, splitUser, 0d));
                    }
                    if (b.aEmail.equals(currentUserEmail)) {
                        b.totalOwing += amountOwed;
                    } else {
                        b.totalOwing -= amountOwed;
                    }
                    balanceViewModel.update(b.totalOwing, b.aEmail, b.bEmail);
                }
            }
            amountsOwed.clear();
            resetReceiptContentsText();
        }
    }

    public void resetUsersSplittingItemText() {
        usersSplittingItemText = new StringBuilder("Users to Split Item:");
        usersSplittingItem.setText(usersSplittingItemText.toString());
    }

    public void resetReceiptContentsText() {
        receiptContentsText = new StringBuilder("Receipt Contents:");
        receiptContents.setText(receiptContentsText.toString());
    }

    public void addUsersSplittingItemText(String user) {
        usersSplittingItemText.append("\n" + user);
        usersSplittingItem.setText(usersSplittingItemText.toString());
    }

    public void addReceiptContentsText(String s) {
        receiptContentsText.append(s);
        receiptContents.setText(receiptContentsText.toString());
    }

    private ContactsViewModel userViewModel;
    private BalanceViewModel balanceViewModel;

    private EditText itemCost;
    private EditText itemName;
    private Spinner userDropDown;
    private Button submitReceipt;
    private Button addUserToItem;
    private Button addItemToReceipt;
    private TextView usersSplittingItem;
    private TextView receiptContents;

    StringBuilder usersSplittingItemText = new StringBuilder();
    StringBuilder receiptContentsText = new StringBuilder();

    private ReceiptFragmentBinding binding;

    private String currentUserEmail = "userA@gmail.com";

    private double currentItemCost= 0d;
    private String currentItemName = "";

    private List<String> usersToSplitItem = new ArrayList<>();
    private List<String> userNamesToSplitItem = new ArrayList<>();

    private HashMap<String,String> userNames = new HashMap<>();
    private HashMap<String, Double> amountsOwed = new HashMap<>();

    private String selectedUser = "";

    private static boolean dummyDataAdded = false;

    public static ReceiptFragment newInstance() {return new ReceiptFragment();}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        userViewModel = new ViewModelProvider(requireActivity()).get(ContactsViewModel.class);
        balanceViewModel = new ViewModelProvider(requireActivity()).get(BalanceViewModel.class);
        if (!dummyDataAdded) {
            userViewModel.insertUser(new User("userA@gmail.com", "user", "A"));
            userViewModel.insertUser(new User("userB@gmail.com", "user", "B"));
            userViewModel.insertUser(new User("userC@gmail.com", "user", "C"));
            balanceViewModel.insertBalance(new Balance("userA@gmail.com", "userB@gmail.com", 55d));
            balanceViewModel.insertBalance(new Balance("userA@gmail.com", "userC@gmail.com", 55d));
            balanceViewModel.insertBalance(new Balance("userB@gmail.com", "userC@gmail.com", 55d));
            dummyDataAdded = true;
        }
        binding = ReceiptFragmentBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        userDropDown = (Spinner)view.findViewById(R.id.UserDropDown);
        submitReceipt = (Button)view.findViewById(R.id.SubmitReceiptButton);
        addUserToItem = (Button)view.findViewById(R.id.AddUserToItemButton);
        addItemToReceipt = (Button)view.findViewById(R.id.AddItemButton);
        usersSplittingItem = (TextView)view.findViewById(R.id.UsersSplittingItem);
        itemCost = (EditText)view.findViewById(R.id.ItemCost);
        itemName = (EditText)view.findViewById(R.id.ItemName);
        receiptContents = (TextView)view.findViewById(R.id.ReceiptContents);

        List<User> users = userViewModel.getUserList();

        if (users != null) {
            for (int x = 0; x < users.size(); x++) {
                String userName = users.get(x).firstName + " " + users.get(x).lastName;
                userNames.put(userName, users.get(x).email);
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getContext(), android.R.layout.simple_spinner_dropdown_item, userNames.keySet().toArray(new String[userNames.keySet().size()]));
            userDropDown.setAdapter(adapter);
        } else {
            String[] s = new String[]{"no users"};
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getContext(), android.R.layout.simple_spinner_dropdown_item, s);
            userDropDown.setAdapter(adapter);
        }
        
        itemCost.setOnClickListener(new ItemCostOnClickListener());
        userDropDown.setOnItemSelectedListener(new UserDropDownActivity());
        addUserToItem.setOnClickListener(new AddUserButtonOnClickListener());
        addItemToReceipt.setOnClickListener(new AddItemToReceiptButtonOnClickListener());
        submitReceipt.setOnClickListener(new SubmitReceiptButtonOnClickListener());
        itemName.setOnClickListener(new ItemNameOnClickListener());

        resetReceiptContentsText();
        resetUsersSplittingItemText();

        return view;
    }
}