package com.splitwisr.ui.contacts;

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

import com.splitwisr.data.balances.Balance;
import com.splitwisr.data.users.User;
import com.splitwisr.databinding.ContactsFragmentBinding;

public class ContactsFragment extends Fragment {
    private ContactsFragmentBinding binding;
    private ContactsViewModel viewModel;
    private UsersAdapter usersAdapter = new UsersAdapter();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ContactsFragmentBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        binding.userRecycler.setHasFixedSize(true);
        binding.userRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.userRecycler.setAdapter(usersAdapter);
        binding.userRecycler.addItemDecoration(new DividerItemDecoration(requireContext(), LinearLayout.VERTICAL));

        binding.submit.setOnClickListener(v -> {
            // TODO: Data validation
            String email = binding.email.getText().toString();
            String first = binding.firstName.getText().toString();
            String last = binding.lastName.getText().toString();
            User newUser =  new User(email, first, last);
            viewModel.insertUser(newUser);

            // TODO: can't hardcode this!
            String myEmail = viewModel.getCurrentUserEmail();
            String aEmail;
            String bEmail;
            if (email.compareTo(myEmail) < 0) {
                aEmail = email;
                bEmail = myEmail;
            } else {
                aEmail = myEmail;
                bEmail = email;
            }

            viewModel.newBalance(new Balance(aEmail, bEmail, 0));
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = ViewModelProviders.of(this).get(ContactsViewModel.class);

        // TODO: Why do these only show up when I click the editText ...
        viewModel.getAllUsers().observe(getViewLifecycleOwner(), users -> {
            if (users != null && users.size() > 0) {
                usersAdapter.setData(users);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
