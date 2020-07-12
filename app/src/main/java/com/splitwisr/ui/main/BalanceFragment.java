package com.splitwisr.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

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
        binding.recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), LinearLayout.VERTICAL));

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);

        // TODO: Delete
//        mainViewModel.insertUser(new User("brian@mail.com", "Brian", "Norman"));
//        mainViewModel.insertUser(new User("joey@mail.com", "Joey", "Ho"));
//        mainViewModel.insertUser(new User("aidan@mail.com", "Aidan", "Wood"));
//        mainViewModel.insertBalance(new Balance("brian@mail.com", "joey@mail.com", 88));
//        mainViewModel.insertBalance(new Balance("aidan@mail.com", "brian@mail.com", 32));

        mainViewModel.getAllBalances().observe(getViewLifecycleOwner(), balances -> {
            if (balances != null || balances.size() > 0) {
                balancesAdapter.setData(balances);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}