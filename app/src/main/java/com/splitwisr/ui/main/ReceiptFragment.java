package com.splitwisr.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.splitwisr.R;
import com.splitwisr.databinding.ReceiptFragmentBinding;

import java.util.ArrayList;
import java.util.List;

public class ReceiptFragment extends Fragment {

    private EditText receiptAmount;
    private EditText userList;
    private Button submitReceipt;

    private ReceiptFragmentBinding binding;

    private double currentReceiptAmount = 0d;
    private List<String> currentUsers = new ArrayList<String>();

    public static ReceiptFragment newInstance() {return new ReceiptFragment();}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = ReceiptFragmentBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        receiptAmount = (EditText)view.findViewById(R.id.receiptAmount);
        userList = (EditText)view.findViewById(R.id.userList);
        submitReceipt = (Button)view.findViewById(R.id.receiptButton);

        receiptAmount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentReceiptAmount = Double.parseDouble(receiptAmount.getText().toString());
            }
        });

        userList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] s = userList.getText().toString().split(System.getProperty("line.separator"));
                currentUsers.clear();
                for (String user : s) {
                    currentUsers.add(user);
                }
            }
        });

        submitReceipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentReceiptAmount > 0d) {
                    int numUsers = currentUsers.size() + 1;
                    double amountPerUser = currentReceiptAmount/numUsers;

                }
            }
        });

        return view;
    }
}