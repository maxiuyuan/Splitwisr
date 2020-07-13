package com.splitwisr.ui.balances;

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
    private BalanceViewModel viewModel;
    private BalanceFragmentBinding binding;
    private BalancesAdapter balancesAdapter = new BalancesAdapter();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = BalanceFragmentBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(balancesAdapter);
        binding.recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), LinearLayout.VERTICAL));

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = ViewModelProviders.of(this).get(BalanceViewModel.class);

        viewModel.getAllBalances().observe(getViewLifecycleOwner(), balances -> {
            if (balances != null && balances.size() > 0) {
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
