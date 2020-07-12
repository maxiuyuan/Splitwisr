package com.splitwisr.ui.main;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.splitwisr.R;
import com.splitwisr.data.balances.Balance;
import com.splitwisr.data.users.User;
import com.splitwisr.databinding.ReceiptFragmentBinding;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class ReceiptFragment extends Fragment {

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public class userDropDownActivity extends Activity implements AdapterView.OnItemSelectedListener {


        public void onItemSelected(AdapterView<?> parent, View view,
                                   int pos, long id) {
            // An item was selected. You can retrieve the selected item using
            // parent.getItemAtPosition(pos)
            selectedUser = (String)parent.getItemAtPosition(pos);
        }

        public void onNothingSelected(AdapterView<?> parent) {
            selectedUser = "";
        }
    }

    private EditText receiptAmount;
    private Spinner userDropDown;
    //private EditText userList;
    private Button submitReceipt;
    private Button addUser;
    private Button refreshBalances;
    private MainViewModel mainViewModel;
    private TextView testText;
    private TextView testText2;

    private ReceiptFragmentBinding binding;

    private String currentUserEmail = "userA@gmail.com";

    private double currentReceiptAmount = 0d;
    private HashSet<String> selectedUsers = new HashSet<>();

    private HashMap<String,String> userNames = new HashMap<>();
    private String selectedUser;

    public ReceiptFragment(MainViewModel m) { this.mainViewModel = m; }

    public static ReceiptFragment newInstance(MainViewModel m) {return new ReceiptFragment(m);}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        if (true) {
            mainViewModel.insertUser(new User("userA@gmail.com", "user A"));
            mainViewModel.insertUser(new User("userB@gmail.com", "user B"));
            mainViewModel.insertUser(new User("userC@gmail.com", "user C"));
            mainViewModel.insertBalance(new Balance("userA@gmail.com", "userB@gmail.com", 55d));
            mainViewModel.insertBalance(new Balance("userA@gmail.com", "userC@gmail.com", 55d));
            mainViewModel.insertBalance(new Balance("userB@gmail.com", "userC@gmail.com", 55d));
        }
        binding = ReceiptFragmentBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        receiptAmount = (EditText)view.findViewById(R.id.receiptAmount);
        userDropDown = (Spinner)view.findViewById(R.id.userDropDown);
        submitReceipt = (Button)view.findViewById(R.id.receiptButton);
        addUser = (Button)view.findViewById(R.id.addUserButton);
        refreshBalances = (Button)view.findViewById(R.id.refreshButton);
        testText = (TextView)view.findViewById(R.id.testText);
        testText2 = (TextView)view.findViewById(R.id.testText2);

        mainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);

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


        // CURRENT USER, CURRENTLY HARDCODED
        selectedUsers.add(currentUserEmail);



        receiptAmount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentReceiptAmount = Double.parseDouble(receiptAmount.getText().toString());
            }
        });

        userDropDown.setOnItemSelectedListener(new userDropDownActivity());

        addUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!selectedUser.equals("")) {
                    selectedUsers.add(userNames.get(selectedUser));
                    StringBuilder s = new StringBuilder();
                    for (String str : selectedUsers.toArray(new String[selectedUsers.size()])) {
                        s.append(str + "\n");
                    }
                    testText2.setText(s.toString());
                }
            }
        });

        refreshBalances.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringBuilder testString = new StringBuilder();
                List<Balance> bb = mainViewModel.getBalanceList();
                for (Balance b : bb )
                {
                    testString.append(b.aEmail + " " + b.bEmail + " " + b.totalOwing + "\n");
                }

                testText.setText(testString.toString());
            }
        });

        submitReceipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentReceiptAmount > 0d) {
                    int numUsers = selectedUsers.size();
                    double amountPerUser = round(currentReceiptAmount/numUsers,2);
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
                            //mainViewModel.update(b);

                        }
                    }
//                    StringBuilder testString = new StringBuilder();
//                    List<Balance> bb = mainViewModel.getBalanceList();
//                    for (Balance b : bb )
//                    {
//                        testString.append(b.aEmail + " " + b.bEmail + " " + b.totalOwing + "\n");
//                    }
//
//                    testText.setText(testString.toString());
                }
            }
        });

        return view;
    }
}