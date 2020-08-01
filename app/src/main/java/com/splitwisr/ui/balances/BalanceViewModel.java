package com.splitwisr.ui.balances;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.google.firebase.auth.FirebaseAuth;
import com.splitwisr.data.balances.Balance;
import com.splitwisr.data.balances.BalanceRepository;
import com.splitwisr.data.users.User;
import com.splitwisr.data.users.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

public class BalanceViewModel extends AndroidViewModel {
    private LiveData<List<Balance>> allBalances;
    private final BalanceRepository balanceRepository;
    private final UserRepository userRepository;
    private String searchQuery = "";
    private BalanceFilter filter = BalanceFilter.NONE;

    public BalanceViewModel(@NonNull Application application) {
        super(application);
        balanceRepository = new BalanceRepository(application, getCurrentUserEmail());
        userRepository = new UserRepository(application);
        allBalances = balanceRepository.getAllBalances();
    }

    LiveData<List<BalanceViewObject>> getBalances() {
        return Transformations.map(allBalances, balances ->
            balances
            .stream()
            .filter(balance -> getCurrentUserEmail().equals(balance.aEmail) || getCurrentUserEmail().equals(balance.bEmail))
            .map(balance -> {
                String otherUserEmail;
                boolean owesOtherUser;
                if (getCurrentUserEmail().equals(balance.aEmail)) {
                    otherUserEmail = balance.bEmail;
                    owesOtherUser = false;
                } else {
                    otherUserEmail = balance.aEmail;
                    owesOtherUser = true;
                }
                String otherUserName = getOtherName(otherUserEmail);
                return new BalanceViewObject(otherUserEmail, otherUserName, balance.totalOwing, owesOtherUser);
            })
            .filter(this::matchSearch)
            .filter(obj -> obj.balance != 0.0)
            .filter(obj -> filter == BalanceFilter.OWING ? isOwing(obj) : true)
            .filter(obj -> filter == BalanceFilter.OWED ? !isOwing(obj) : true)
            .collect(Collectors.toList()));
    }

    public Boolean matchSearch(BalanceViewObject obj) {
        return obj.otherName.toLowerCase().contains(searchQuery.toLowerCase())
                || obj.otherEmail.toLowerCase().contains(searchQuery.toLowerCase());
    }

    public Boolean isOwing(BalanceViewObject obj) {
        return (obj.owesOtherUser && obj.balance > 0) || (!obj.owesOtherUser && obj.balance < 0);
    }

    public String getCurrentUserEmail(){
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        return mAuth.getCurrentUser().getEmail();
    }

    public void refreshBalances() {
        balanceRepository.getLatestBalances();
    }

    public String getOtherName(String otherUserEmail) {
        User user = userRepository.getUserBlocking(otherUserEmail);
        if (user != null) {
            return ((user.firstName == null)?"" : user.firstName)
                    + " "
                    + ((user.lastName == null) ? "" : user.lastName);
        }
        return "";
    }

    public void settleBalance(String bEmail){
        new Thread(() -> balanceRepository.upsert(0, getCurrentUserEmail(), bEmail)).start();
    }

    public void setSearchFilter(String query) {
        searchQuery = query;
        // Hacky way to restart the getAllBalances() call and filter again
        allBalances = balanceRepository.getAllBalances();
    }

    public void setFilter(BalanceFilter newFilter) {
        filter = newFilter;
        // Hacky way to restart the getAllBalances() call and filter again
        allBalances = balanceRepository.getAllBalances();
    }
}
