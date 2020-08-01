package com.splitwisr.ui.receipts;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import com.abdeveloper.library.MultiSelectModel;
import com.google.firebase.auth.FirebaseAuth;
import com.splitwisr.data.balances.Balance;
import com.splitwisr.data.balances.BalanceRepository;
import com.splitwisr.data.users.User;
import com.splitwisr.data.users.UserRepository;
import com.splitwisr.ui.camera.CameraClass;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ReceiptsViewModel extends AndroidViewModel {
    private UserRepository userRepository;
    private BalanceRepository balanceRepository;

    public boolean camera = false;

    public static CameraClass cameraClass = new CameraClass();

    public List<User> users;
    public ArrayList<ReceiptsViewObject> receiptItems = new ArrayList<>();
    public File outFile;

    public ReceiptsViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application);
        balanceRepository = new BalanceRepository(application, getCurrentUserEmail());
        updateUserList();
    }

    public void addReceiptItem(Double itemCost, String itemName) {
        receiptItems.add(new ReceiptsViewObject(itemName, itemCost.toString()));
    }

    public void addUserToReceipt(int receiptIndex, ArrayList<Integer> indexes){
        receiptItems.get(receiptIndex).splitWith = indexes
                .stream()
                .map(i-> users.get(i))
                .collect(Collectors.toList());
    }

    public void removeReceiptItem(int index) {
        receiptItems.remove(index);
    }

    public String getCurrentUserEmail(){
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        return mAuth.getCurrentUser().getEmail();
    }

    public List<String> getUserNames() {
        return users
                .stream()
                .map(user -> user.firstName + " " + user.lastName)
                .collect(Collectors.toList());
    }

    public void updateSelectedUsers(ArrayList<Integer> selectedNameIndexes) {
        users = selectedNameIndexes
                .stream()
                .map(i->users.get(i))
                .collect(Collectors.toList());
        users.size();

    }

    public void updateUserList(){
        users = userRepository.getUserList();
    }

    public boolean submit(List<User> users, List<ReceiptsViewObject> receiptItems) {
        // Pass in as argument to copy them so we can safely reset even while running on a thread
        if (receiptItems.size() == 0){
            return false;
        }

        for (ReceiptsViewObject receiptsViewObject: receiptItems){
            // If empty split with everyone
            if (receiptsViewObject.splitWith == null
                    || receiptsViewObject.splitWith.size() == 0){
                receiptsViewObject.splitWith = users;
            }

            int divider = receiptsViewObject.splitWith.size();
            double remainingBill = Double.parseDouble(receiptsViewObject.itemCost);



            while (divider > 0){
                String payer = getCurrentUserEmail();
                String split_payee = receiptsViewObject.splitWith.get(divider-1).email;

                double amount = Math.round((remainingBill/divider)*100.0)/100.0;
                divider--;

                remainingBill -= amount;
                if (split_payee.equals(payer)) continue;

                if (payer.compareTo(split_payee) > 0) {
                    String temp = payer;
                    payer = split_payee;
                    split_payee = temp;
                    amount = -amount;
                }

                Balance oldBalance = balanceRepository.get(payer,split_payee);

                double totalBalance = oldBalance== null? amount : amount + oldBalance.totalOwing;
                balanceRepository.upsert(totalBalance, payer, split_payee);
            }
        }
        userRepository.insertAll(users);

        return true;

    }

    public List<MultiSelectModel> getModelList(List<String> users) {
        return IntStream
                .range(0, users.size())
                .mapToObj(i -> new MultiSelectModel(i, users.get(i)))
                .collect(Collectors.toList());
    }

    public List<ReceiptsViewObject> getReceipts() {
        return  receiptItems;
    }

    public List<User> getUsers() {
        return  users;
    }

    public void reset() {
        users = null;
        receiptItems = new ArrayList<>();
    }


}
