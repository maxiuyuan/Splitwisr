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

import org.w3c.dom.Text;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.HashSet;
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
            currentItemCost = Double.parseDouble(itemCost.getText().toString());
        }
    }

    public class AddUserButtonOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (!selectedUser.equals("")) {
                selectedUsers.add(userNames.get(selectedUser));
                StringBuilder s = new StringBuilder();
                for (String str : selectedUsers.toArray(new String[selectedUsers.size()])) {
                    s.append(str + "\n");
                }
                //testText2.setText(s.toString());
            }
        }
    }

    public class SubmitReceiptButtonOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (currentItemCost > 0d) {
                int numUsers = selectedUsers.size();
                double amountPerUser = round(currentReceiptAmount/numUsers);
                for (String splitUser : selectedUsers) {
                    if (!splitUser.equals(currentUserEmail)) {
                        List<Balance> balances = mainViewModel.get(currentUserEmail, splitUser);
                        Balance b = null;
                        if (balances.size() > 0) {
                            b = balances.get(0);
//                                testText2.setText(Double.toString(b.totalOwing));
                        } else {
//                                testText2.setText("NO ENTRY");
                            if (currentUserEmail.compareTo(splitUser) < 0) {
                                b = new Balance(currentUserEmail, splitUser, 0d);
                            } else {
                                b = new Balance(splitUser, currentUserEmail, 0d);
                            }
                            mainViewModel.insertBalance(b);
                        }
                        if (b.aEmail.equals(currentUserEmail)) {
                            b.totalOwing += amountPerUser;
                        } else {
                            b.totalOwing -= amountPerUser;
                        }
                        mainViewModel.update(b.totalOwing, b.aEmail, b.bEmail);
                    }
                }
            }
        }
    }

    private MainViewModel mainViewModel;

    private EditText itemCost;
    private Spinner userDropDown;
    private Button submitReceipt;
    private Button addUserToItem;
    private Button addItemToReceipt;
    private TextView usersSplittingItem;
    private TextView receiptContents;

    private ReceiptFragmentBinding binding;

    private String currentUserEmail = "userA@gmail.com";

    private double currentItemCost= 0d;

    private HashSet<String> selectedUsers = new HashSet<>();
    private HashMap<String,String> userNames = new HashMap<>();
    private String selectedUser;


    public static ReceiptFragment newInstance() {return new ReceiptFragment();}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        if (true) {
            mainViewModel.insertUser(new User("userA@gmail.com", "user", "A"));
            mainViewModel.insertUser(new User("userB@gmail.com", "user", "B"));
            mainViewModel.insertUser(new User("userC@gmail.com", "user", "C"));
            mainViewModel.insertBalance(new Balance("userA@gmail.com", "userB@gmail.com", 55d));
            mainViewModel.insertBalance(new Balance("userA@gmail.com", "userC@gmail.com", 55d));
            mainViewModel.insertBalance(new Balance("userB@gmail.com", "userC@gmail.com", 55d));
        }
        binding = ReceiptFragmentBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        userDropDown = (Spinner)view.findViewById(R.id.UserDropDown);
        submitReceipt = (Button)view.findViewById(R.id.SubmitReceiptButton);
        addUserToItem = (Button)view.findViewById(R.id.AddUserToItemButton);
        addItemToReceipt = (Button)view.findViewById(R.id.AddItemButton);
        usersSplittingItem = (TextView)view.findViewById(R.id.UsersSplittingItem);
        itemCost = (EditText)view.findViewById(R.id.ItemCost);
        receiptContents = (TextView)view.findViewById(R.id.ReceiptContents);

        List<User> users = mainViewModel.getUserList();

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
        submitReceipt.setOnClickListener(new SubmitReceiptButtonOnClickListener());

        return view;
    }
}