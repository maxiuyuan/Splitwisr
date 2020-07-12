package com.splitwisr.ui.main;

import android.app.Activity;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class ReceiptFragment extends Fragment {

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
    private MainViewModel mainViewModel;

    private ReceiptFragmentBinding binding;

    private String currentUserEmail = "userA@gamil.com";

    private double currentReceiptAmount = 0d;
    private HashSet<String> selectedUsers = new HashSet<>();

    private HashMap<String,String> userNames = new HashMap<>();
    private String selectedUser;

    public static ReceiptFragment newInstance() {return new ReceiptFragment();}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = ReceiptFragmentBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        receiptAmount = (EditText)view.findViewById(R.id.receiptAmount);
        userDropDown = (Spinner)view.findViewById(R.id.userDropDown);
        submitReceipt = (Button)view.findViewById(R.id.receiptButton);
        addUser = (Button)view.findViewById(R.id.addUserButton);

        mainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        List<User> users = mainViewModel.getAllUsers().getValue();
        for (int x = 0; x < users.size(); x++) {
            String userName = users.get(x).firstName + " " + users.get(x).lastName;
            userNames.put(userName, users.get(x).email);
        }

        // CURRENT USER, CURRENTLY HARDCODED
        selectedUsers.add(currentUserEmail);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getContext(), android.R.layout.simple_spinner_dropdown_item, (String[])userNames.keySet().toArray());
        userDropDown.setAdapter(adapter);

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
                    selectedUsers.add(selectedUser);
                }
            }
        });

        submitReceipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentReceiptAmount > 0d) {
                    int numUsers = selectedUsers.size() + 1;
                    double amountPerUser = currentReceiptAmount/numUsers;

                    for (String splitUser : selectedUsers) {
                        if (!splitUser.equals(currentUserEmail)) {
                            Balance b = mainViewModel.get(currentUserEmail, splitUser).getValue().get(0);
                            if (b.aEmail.equals(currentUserEmail)) {
                                b.totalOwing += amountPerUser;
                            } else {
                                b.totalOwing -= amountPerUser;
                            }

                            mainViewModel.update(b);
                        }
                    }
                }
            }
        });

        return view;
    }
}