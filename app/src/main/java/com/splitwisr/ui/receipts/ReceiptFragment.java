package com.splitwisr.ui.receipts;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.fonts.SystemFonts;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.splitwisr.MainActivity;
import com.splitwisr.R;
import com.splitwisr.databinding.ReceiptFragmentBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class ReceiptFragment extends Fragment {

    private ReceiptsViewModel receiptsViewModel;
    private ReceiptFragmentBinding binding;
    private ReceiptsAdapater receiptsAdapater;

    static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

    }

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

        // Button to take picture
        binding.cameraButton.setOnClickListener(v -> {
            Long tsLong = System.currentTimeMillis()/1000;
            String ts = tsLong.toString() + ".jpg";
            // Inform the main activity we are returning from camera so nav back to receipt frag
            ((MainActivity)getActivity()).hasCamera = true;
            // Save the user list because upon nav-ing back after the camera the user list resets and idk how to fix it so this is whats happening for now
            ((MainActivity)getActivity()).savedUsers = receiptsViewModel.getUsers();
          //  ((MainActivity)getActivity()).savedItems = receiptsViewModel.getReceipts();
            dispatchTakePictureIntent();
        });

        // Temp button to update receipts adapter because it doesn't update when its called in addScannedItems by the camera class
        binding.detectButton.setOnClickListener(v -> {
            receiptsAdapater.setData(receiptsViewModel.getReceipts());

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

    private void dispatchTakePictureIntent() {

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        Long tsLong = System.currentTimeMillis()/1000;
        String ts = tsLong.toString() + ".jpg";

        receiptsViewModel.outFile = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES), ts + ".jpg");

        if (!receiptsViewModel.outFile.exists()) {
            try {
                receiptsViewModel.outFile.createNewFile();
            } catch (Exception e) {
                System.out.println("Exception thrown cant create file" + e.getMessage());
            }
        }

        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(receiptsViewModel.outFile));

        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            this.startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    public void addScannedItems() {
        List<String> newItemNames = receiptsViewModel.cameraClass.getItemNames();
        List<Double> newItemCosts = receiptsViewModel.cameraClass.getItemCosts();
        if (newItemNames != null && newItemCosts != null) {
            for (int x = 0; x < newItemNames.size(); x++) {
                System.out.println("adding " + newItemNames.get(x) + " " + newItemNames.get(x));
                receiptsViewModel.addReceiptItem(newItemCosts.get(x), newItemNames.get(x));
            }
            // This call fails silently for some dumbass reason
            receiptsAdapater.setData(receiptsViewModel.getReceipts());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == getActivity().RESULT_OK) {
            receiptsViewModel.cameraClass.detectTextFromReceipt(this.getContext(), Uri.fromFile(receiptsViewModel.outFile), this);
        } else {
            System.out.println("Problem while taking image");
        }
    }


    private void deleteImageUri() {
        if (receiptsViewModel.outFile.exists()) {
            if (receiptsViewModel.outFile.delete()) {
                System.out.println("file Deleted :" + receiptsViewModel.outFile.toString());
            } else {
                System.out.println("file not Deleted :" + receiptsViewModel.outFile.toString());
            }
        }
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.emptyStateImage.setVisibility(
                (receiptsViewModel.getReceipts().isEmpty())? View.VISIBLE : View.GONE);
        receiptsAdapater.setData(receiptsViewModel.getReceipts());
        if (((MainActivity)getActivity()).hasCamera == false) {
            splitUsersDialog(
                    (ids, selectedNames, dataString) -> {
                        receiptsViewModel.updateSelectedUsers(ids);
                    },
                    this::navigateToBalancesFragment);
        } else {
            // I don't wanna have to use this but the user list resets if you don't run the contents of the above if
            // and idk how that works so we're saving it in the activity until someone else fixes it

            receiptsViewModel.users = ((MainActivity)getActivity()).savedUsers;
           // receiptsViewModel.receiptItems = ((MainActivity)getActivity()).savedItems;

            System.out.println("SAVED STATE: " + receiptsViewModel.users.size() + " " + receiptsViewModel.receiptItems.size());
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (!((MainActivity)getActivity()).hasCamera) {
            System.out.println("RESETTING");
            receiptsViewModel.reset();
        }
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

