package com.splitwisr.ui.contacts;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.splitwisr.R;
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
            String newEmail = binding.email.getText().toString();
            String newFirstName = binding.firstName.getText().toString();
            String newLastName = binding.lastName.getText().toString();

            if (!newEmail.isEmpty() && !newFirstName.isEmpty() && !newLastName.isEmpty()) {
                User newUser = new User(newEmail, newFirstName, newLastName);
                viewModel.insertUser(newUser);
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        });
        binding.importContactsFab.setOnClickListener(v->{
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.destination_import_contacts);

        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = ViewModelProviders.of(this).get(ContactsViewModel.class);

        viewModel.getAllUsers().observe(getViewLifecycleOwner(), users -> {
            if (users != null && users.size() > 1) {
                usersAdapter.setData(users);
            }
            binding.emptyStateImage.setVisibility(
                    (users == null || users.size() == 1) ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
