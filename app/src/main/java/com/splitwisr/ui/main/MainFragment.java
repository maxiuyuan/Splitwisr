package com.splitwisr.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.splitwisr.data.balances.Balance;
import com.splitwisr.data.users.User;
import com.splitwisr.databinding.MainFragmentBinding;

public class MainFragment extends Fragment {

    private MainViewModel mainViewModel;
    private MainFragmentBinding binding;

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = MainFragmentBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        mainViewModel.getAllUsers().observe(getViewLifecycleOwner(), users -> {
            if (users.size() > 0) {
                binding.message.setText(users.get(users.size() - 1).firstName + "\n");
            } else {
                mainViewModel.insertUser(new User("fake@gmail.com", "Fake"));
            }
        });
        mainViewModel.getAllBalances().observe(getViewLifecycleOwner(), balances -> {
            if (balances.size() > 0) {
                Balance balance = balances.get(0);
                binding.balances.setText(balance.aEmail + " owes " + balance.bEmail + " $" + balance.totalOwing);
            } else {
                mainViewModel.insertBalance(new Balance("aEmail", "bEmail", 100));
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}