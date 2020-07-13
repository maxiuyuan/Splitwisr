package com.splitwisr.ui.balances;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.splitwisr.R;
import com.splitwisr.databinding.BalanceFragmentBinding;

public class BalanceFragment extends Fragment {

    private BalanceViewModel viewModel;
    private BalanceFragmentBinding binding;

    private BalancesAdapter balancesAdapter = new BalancesAdapter();

    public static BalanceFragment newInstance() {
        return new BalanceFragment();
    }

    private Fragment here = this;

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

        binding.ReceiptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment
                        .findNavController(here)
                        .navigate(R.id.destination_receipt_fragment);
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = ViewModelProviders.of(this).get(BalanceViewModel.class);

        // TODO: Delete
//        viewModel.insertUser(new User("brian@mail.com", "Brian", "Norman"));
//        viewModel.insertUser(new User("joey@mail.com", "Joey", "Ho"));
//        viewModel.insertUser(new User("aidan@mail.com", "Aidan", "Wood"));
//        viewModel.insertBalance(new Balance("brian@mail.com", "joey@mail.com", 88));
//        viewModel.insertBalance(new Balance("aidan@mail.com", "brian@mail.com", 32));

        viewModel.getAllBalances().observe(getViewLifecycleOwner(), balances -> {
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