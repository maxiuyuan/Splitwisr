package com.splitwisr.ui.balances;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.google.firebase.auth.FirebaseAuth;
import com.splitwisr.data.MessagingService;
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
    public String searchQuery = "";
    MessagingService messagingService = new MessagingService();

    public BalanceViewModel(@NonNull Application application) {
        super(application);
        balanceRepository = new BalanceRepository(application, getCurrentUserEmail());
        userRepository = new UserRepository(application);
        allBalances = balanceRepository.getAllBalances();
    }

    LiveData<List<BalanceViewObject>> getAllBalances() {
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
            .filter(balanceViewObject ->
                    balanceViewObject.otherName.toLowerCase().contains(searchQuery.toLowerCase())
                    || balanceViewObject.otherEmail.toLowerCase().contains(searchQuery.toLowerCase()))
            .collect(Collectors.toList()));
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

    public void setSearchFilter(String query) {
        searchQuery = query;
        // Hacky way to restart the getAllBalances() call and filter again
        allBalances = balanceRepository.getAllBalances();
    }

    public void settleBalance(String bEmail){
        new Thread(() -> balanceRepository.upsert(0, getCurrentUserEmail(), bEmail)).start();
    }

    public List<BalanceViewObject> getNonZeroBalances(List<BalanceViewObject> balanceViewObjects){
        return  balanceViewObjects
                .stream()
                .filter(obj -> obj.balance != 0.0)
                .collect(Collectors.toList());
    }

    public void notifyUser(String bEmail) {
        messagingService.sendMessage(bEmail);
    }
}
