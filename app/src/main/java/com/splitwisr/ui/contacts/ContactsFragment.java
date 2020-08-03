package com.splitwisr.ui.contacts;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
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

import com.abdeveloper.library.MultiSelectDialog;
import com.splitwisr.R;
import com.splitwisr.data.users.User;
import com.splitwisr.databinding.ContactsFragmentBinding;

import java.util.ArrayList;

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

        binding.addUserButton.setOnClickListener(v -> {
            String newEmail = binding.email.getText().toString();
            String newFirstName = binding.firstName.getText().toString();
            String newLastName = binding.lastName.getText().toString();

            if (!newEmail.isEmpty() && !newFirstName.isEmpty() && !newLastName.isEmpty()) {
                User newUser = new User(newEmail, newFirstName, newLastName);
                viewModel.insertUser(newUser);
                hideKeyboard(view);
                binding.email.setText("");
                binding.firstName.setText("");
                binding.lastName.setText("");
            }
        });

        binding.addGroupButton.setOnClickListener(v -> {
            String newGroupName = binding.groupName.getText().toString();

            if (!newGroupName.isEmpty()) {
                MultiSelectDialog multiSelectDialog = new MultiSelectDialog()
                        .title("Add Members to " + newGroupName)
                        .titleSize(18)
                        .positiveText("Done")
                        .negativeText("Cancel")
                        .setMinSelectionLimit(2)
                        .multiSelectList(viewModel.getUserMultiSelectList())
                        .onSubmit(new MultiSelectDialog.SubmitCallbackListener() {
                            @Override
                            public void onSelected(ArrayList<Integer> selectedIds, ArrayList<String> selectedNames, String dataString) {
                                viewModel.insertGroup(newGroupName, selectedIds);
                            }

                            @Override
                            public void onCancel() {
                                Log.d("ContactsFragment","Dialog cancelled");
                            }
                        });
                multiSelectDialog.show(getParentFragmentManager(), "multiSelectDialog");
                hideKeyboard(view);
                binding.groupName.setText("");
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

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
