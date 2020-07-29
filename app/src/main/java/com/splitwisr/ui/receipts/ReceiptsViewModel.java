package com.splitwisr.ui.receipts;


import android.app.Application;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.splitwisr.data.ReceiptItem;
import com.splitwisr.data.balances.Balance;
import com.splitwisr.data.balances.BalanceRepository;
import com.splitwisr.data.users.User;
import com.splitwisr.data.users.UserRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class ReceiptsViewModel extends AndroidViewModel {
    private UserRepository userRepository;
    private BalanceRepository balanceRepository;

    // Maps full name to email
    private HashMap<String,String> userNames;

    public void setUserNames(HashMap<String,String> h) {
        userNames = h;
    }

    public String getUserName(String k) {
        if (userNames != null) {
            return userNames.get(k);
        } else {
            return "";
        }
    }

    public Collection<String> getUserNamesValueSet() {
        if (userNames != null) {
            return userNames.values();
        } else {
            return new ArrayList<>();
        }
    }

    // Maps item ID to item object
    private HashMap<Integer, ReceiptItem> receiptItems = new HashMap<>();

    public void addReceiptItem(Integer id, ReceiptItem r) {
        receiptItems.put(id, r);
    }

    public void removeReceiptItem(Integer id) {
        if (receiptItems.containsKey(id)) {
            receiptItems.remove(id);
        }
    }

    public boolean checkReceiptItemExists(Integer id) {
        return receiptItems.containsKey(id);
    }

    public ReceiptItem getReceiptItem(Integer id) {
        return receiptItems.get(id);
    }

    public Collection<ReceiptItem> getReceiptItemValueSet() {
        return receiptItems.values();
    }

    // Array of strings that denotes the users "in the receipt"
    // Currently is the entire user DB but should eventually become a subset
    // This array is used in the ADD USERS pop-up per item
    private String[] selectableUsers;

    public void setSelectableUsers(String[] s) {
        selectableUsers = s;
    }

    public String getSelectableUser(int index) {
        if (selectableUsers != null && index < selectableUsers.length) {
            return selectableUsers[index];
        } else {
            return "";
        }
    }

    public String[] getSelectableUsers() {
        return selectableUsers;
    }

    public int getSelectableUserSize() {
        if (selectableUsers != null) {
            return selectableUsers.length;
        } else {
            return 0;
        }
    }

    public ReceiptsViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application);
        balanceRepository = new BalanceRepository(application, getCurrentUserEmail());
    }

    public List<User> getUserList() { return userRepository.getUserList(); }

    public void update(double totalOwing, String aEmail, String bEmail) {
        balanceRepository.update(totalOwing, aEmail, bEmail);
    }

    public Balance get(final String s1, final String s2) {
        return balanceRepository.get(s1, s2);
    }

    public void insertBalance(Balance balance) {
        balanceRepository.insert(balance);
    }

    public String getCurrentUserEmail(){
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        return mAuth.getCurrentUser().getEmail();
    }
}
