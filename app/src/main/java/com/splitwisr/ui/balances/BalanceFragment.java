package com.splitwisr.ui.balances;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.splitwisr.databinding.BalanceFragmentBinding;

import java.util.List;
import java.util.stream.Collectors;

public class BalanceFragment extends Fragment {
    private BalanceViewModel viewModel;
    private BalanceFragmentBinding binding;
    private BalancesAdapter balancesAdapter = new BalancesAdapter(bEmail -> viewModel.settleBalance(bEmail));

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = BalanceFragmentBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        viewModel = ViewModelProviders.of(this).get(BalanceViewModel.class);

        binding.refreshView.setOnRefreshListener(() -> {
            viewModel.refreshBalances();
            if (binding.refreshView.isRefreshing()) {
                (new Handler()).postDelayed(this::removeRefresh, 1000);
            }
        });

        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(balancesAdapter);
        binding.recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), LinearLayout.VERTICAL));

        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                viewModel.setSearchFilter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                viewModel.setSearchFilter(newText);
                return false;
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel.getAllBalances().observe(getViewLifecycleOwner(), balanceViewObjects -> {
            List<BalanceViewObject> objects = balanceViewObjects
                    .stream()
                    .filter(obj -> obj.balance > 0.0)
                    .collect(Collectors.toList());
            balancesAdapter.setData(objects);
            binding.emptyStateImage.setVisibility(objects.isEmpty()? View.VISIBLE : View.GONE);
        });
    }

    void removeRefresh() {
        binding.refreshView.setRefreshing(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
