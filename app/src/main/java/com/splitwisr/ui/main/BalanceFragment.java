package com.splitwisr.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.splitwisr.data.balances.Balance;
import com.splitwisr.databinding.BalanceFragmentBinding;

public class BalanceFragment extends Fragment {

    private MainViewModel mainViewModel;
    private BalanceFragmentBinding binding;

    private BalancesAdapter balancesAdapter = new BalancesAdapter();

    public static BalanceFragment newInstance() {
        return new BalanceFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = BalanceFragmentBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Setup recyclerView
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(balancesAdapter);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        mainViewModel.getAllBalances().observe(getViewLifecycleOwner(), balances -> {
            if (balances != null || balances.size() > 0) {
                balancesAdapter.setData(balances);
            } else {
                mainViewModel.insertBalance(new Balance("userA@gamil.com", "userB@gmail.com", 55));
                mainViewModel.insertBalance(new Balance("fake@gamil.com", "faker@gmail.com", 60));
                mainViewModel.insertBalance(new Balance("bozo@hotmail.com", "coolGuy@gmail.com", 4000));
                mainViewModel.insertBalance(new Balance("John@gamil.com", "Doe@gmail.com", 30));
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}