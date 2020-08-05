package com.splitwisr.ui.receipts;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import com.abdeveloper.library.MultiSelectModel;
import com.google.firebase.auth.FirebaseAuth;
import com.splitwisr.data.balances.Balance;
import com.splitwisr.data.balances.BalanceRepository;
import com.splitwisr.data.groups.Group;
import com.splitwisr.data.groups.GroupRepository;
import com.splitwisr.data.persons.Persons;
import com.splitwisr.data.users.User;
import com.splitwisr.data.users.UserRepository;
import com.splitwisr.ui.camera.CameraClass;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ReceiptsViewModel extends AndroidViewModel {
    private UserRepository userRepository;
    private BalanceRepository balanceRepository;
    private GroupRepository groupRepository;
    public static CameraClass cameraClass = new CameraClass();

    // this is gonna be public until the whole user list resetting on nav-ing back after camera thing is fixed
    private List<User> users;
    private List<Group> groups;
    private List<Persons> persons;
    private List<ReceiptsViewObject> receiptItems = new ArrayList<>();
    public File outFile;

    public ReceiptsViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application);
        balanceRepository = new BalanceRepository(application, getCurrentUserEmail());
        groupRepository = new GroupRepository(application);
        updatePersonsList();
    }

    public void addReceiptItem(Double itemCost, String itemName) {
        receiptItems.add(new ReceiptsViewObject(itemName, itemCost.toString()));
    }

    public void addUserToReceipt(int receiptIndex, ArrayList<Integer> indexes){
        receiptItems.get(receiptIndex).splitWith = indexes
                .stream()
                .map(i-> persons.get(i))
                .collect(Collectors.toList());
    }

    public void addPersonToReceipt(int receiptIndex, ArrayList<Integer> indexes){
        receiptItems.get(receiptIndex).splitWith = indexes
                .stream()
                .map(i->persons.get(i))
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

    public List<String> getNames() {
        return persons
                .stream()
                .map(person -> person.getName())
                .collect(Collectors.toList());
    }

    public void updateSelectedUsers(ArrayList<Integer> selectedNameIndexes) {
        users = selectedNameIndexes
                .stream()
                .map(i->users.get(i))
                .collect(Collectors.toList());
        users.size();
    }

    public void updateSelectedPersons(ArrayList<Integer> selectedNameIndexes) {
        persons = selectedNameIndexes
                .stream()
                .map(i->persons.get(i))
                .collect(Collectors.toList());
        persons.size();
        for (Persons p : persons) {
            users.clear();
            groups.clear();
            if (p instanceof User) {
                users.add((User)p);
            } else {
                groups.add((Group)p);
            }
        }
    }

    public void updateUserList(){
        users = userRepository.getUserList();
    }

    public void updateGroupList(){
        groups = groupRepository.getAllGroups();
    }

    public void updatePersonsList(){
        updateGroupList();
        updateUserList();
        persons = Stream.concat(users.stream(), groups.stream()).collect(Collectors.toList());
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
                receiptsViewObject.splitWith = persons;
            }

            HashSet<String> itemUsers = new HashSet<>();

            for (Persons p : receiptsViewObject.splitWith) {
                if (p instanceof User) {
                    itemUsers.add(((User)p).email);
                } else {
                    itemUsers.addAll(((Group)p).userEmails);
                }
            }

            String[] payee_emails = itemUsers.toArray(new String[0]);

            int divider = payee_emails.length;
            double remainingBill = Double.parseDouble(receiptsViewObject.itemCost);



            while (divider > 0){
                String payer = getCurrentUserEmail();
                String split_payee = payee_emails[divider-1];
                double amount = round(remainingBill / divider);
                divider--;

                remainingBill -= amount;
                if (split_payee.equals(payer)) continue;

                if (payer.compareTo(split_payee) > 0) {
                    String temp = payer;
                    payer = split_payee;
                    split_payee = temp;
                    amount = -amount;
                }

                Balance oldBalance = balanceRepository.get(payer, split_payee);

                double totalBalance = oldBalance == null ? amount : amount + oldBalance.totalOwing;
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
        return receiptItems;
    }

    public List<User> getUsers() {
        return  users;
    }

    public void reset() {
        updatePersonsList();
        receiptItems.clear();
    }
    // This stays
    public static double round(double value) {
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

}
