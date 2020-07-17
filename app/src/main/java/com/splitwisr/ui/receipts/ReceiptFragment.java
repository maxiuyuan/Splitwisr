package com.splitwisr.ui.receipts;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.abdeveloper.library.MultiSelectDialog;
import com.abdeveloper.library.MultiSelectModel;
import com.splitwisr.R;
import com.splitwisr.databinding.ReceiptFragmentBinding;
import com.splitwisr.ui.camera.CameraActivity;

import java.util.ArrayList;
import java.util.List;

public class ReceiptFragment extends Fragment {

    private ReceiptsViewModel receiptsViewModel;
    private ReceiptFragmentBinding binding;
    private ReceiptsAdapater receiptsAdapater;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = ReceiptFragmentBinding.inflate(inflater, container, false);
        receiptsViewModel = new ViewModelProvider(requireActivity()).get(ReceiptsViewModel.class);
        receiptsViewModel.updateUserList();

        View view = binding.getRoot();

        binding.addItemButton.setOnClickListener(v -> {
            if (binding.itemCostText.getText().toString().isEmpty()
                    || binding.itemNameText.getText().toString().isEmpty()) {
                return;
            }
            double itemCost = Double.parseDouble(binding.itemCostText.getText().toString());
            if (itemCost <= 0d){
                return;
            }
            String itemName = binding.itemNameText.getText().toString();
            receiptsViewModel.addReceiptItem(itemCost, itemName);

            binding.emptyStateImage.setVisibility(
                    (receiptsViewModel.getReceipts().isEmpty())? View.VISIBLE : View.GONE);
            binding.itemCostText.getText().clear();
            binding.itemNameText.getText().clear();
            receiptsAdapater.setData(receiptsViewModel.getReceipts());
        });

        binding.submitButton.setOnClickListener(v -> {
            if(receiptsViewModel.submit(receiptsViewModel.getUsers(), receiptsViewModel.getReceipts())){
                navigateToBalancesFragment();
            }
        });

        receiptsAdapater = new ReceiptsAdapater(
                i-> {
                    receiptsViewModel.removeReceiptItem(i);
                    binding.emptyStateImage.setVisibility(
                            (receiptsViewModel.getReceipts().isEmpty())? View.VISIBLE : View.GONE);
                    receiptsAdapater.setData(receiptsViewModel.getReceipts());
                },
                i -> splitUsersDialog(
                        (ids, selectedNames, dataString)-> {
                            receiptsViewModel.addUserToReceipt(i, ids);
                            receiptsAdapater.setData(receiptsViewModel.getReceipts());
                        },
                        ()->{})
        );

        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(receiptsAdapater);
        binding.recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), LinearLayout.VERTICAL));

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.emptyStateImage.setVisibility(
                (receiptsViewModel.getReceipts().isEmpty())? View.VISIBLE : View.GONE);
        receiptsAdapater.setData(receiptsViewModel.getReceipts());
        splitUsersDialog(
                (ids, selectedNames, dataString)->{
                    receiptsViewModel.updateSelectedUsers(ids);
                },
                this::navigateToBalancesFragment);
    }

    @Override
    public void onStop() {
        super.onStop();
        receiptsViewModel.reset();
    }

    private void splitUsersDialog(
            MultiSelectModelOnSelected onSelected,
            MultiSelectModelOnCancel onCancel ){

        List<MultiSelectModel> modelList = receiptsViewModel.getModelList(receiptsViewModel.getUserNames());
        MultiSelectDialog multiSelectDialog = new MultiSelectDialog()
                .title("Select Users")
                .titleSize(25)
                .positiveText("Done")
                .negativeText("Cancel")
                .setMinSelectionLimit(1)
                .multiSelectList(new ArrayList<>(modelList)) // the multi select model list with ids and name
                .onSubmit(new MultiSelectDialog.SubmitCallbackListener() {
                    @Override
                    public void onSelected(ArrayList<Integer> selectedIds, ArrayList<String> selectedNames, String dataString) {
                        onSelected.callback(selectedIds, selectedNames, dataString);
                    }
                    @Override
                    public void onCancel() {
                        onCancel.callback();
                    }
                });
        multiSelectDialog.show(getActivity().getSupportFragmentManager(), "multiSelectDialog");
        multiSelectDialog.setShowsDialog(true);
    }

    private void navigateToBalancesFragment() {
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.destination_balance_fragment);
    }

    private interface MultiSelectModelOnSelected{
        void callback(ArrayList<Integer> selectedIds, ArrayList<String> selectedNames, String dataString);
    }
    private interface MultiSelectModelOnCancel{
        void callback();
    }
}

