package com.splitwisr.ui.receipts;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
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
import com.splitwisr.ui.camera.CameraActivity;
import com.splitwisr.ui.camera.CameraClass;

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

        binding.cameraButton.setOnClickListener(v -> {
            try {
                throw new Exception("fuck you");
            }
            catch (Exception e) {

            }
            Long tsLong = System.currentTimeMillis()/1000;
            String ts = tsLong.toString() + ".jpg";
            ((MainActivity)getActivity()).hasCamera = true;
            receiptsViewModel.camera = true;
//            ((MainActivity)getActivity()).savedReceiptItems = receiptsViewModel.receiptItems;
            ((MainActivity)getActivity()).savedUsers = receiptsViewModel.users;
            dispatchTakePictureIntent();
        });

        binding.detectButton.setOnClickListener(v -> {
            List<String> newItemNames = receiptsViewModel.cameraClass.getItemNames();
            List<Double> newItemCosts = receiptsViewModel.cameraClass.getItemCosts();
            if (newItemNames != null && newItemCosts != null) {
                for (int x = 0; x < newItemNames.size(); x++) {
                    receiptsViewModel.addReceiptItem(newItemCosts.get(x), newItemNames.get(x));
                }
                receiptsAdapater.setData(receiptsViewModel.getReceipts());
            } else {
                System.out.println("YEAH THESE ARE NULL XDDDDDDDDDDDDDDDDDDDDDDd");
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == getActivity().RESULT_OK) {
            System.out.println("AHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHh");
            receiptsViewModel.cameraClass.detectTextFromReceipt(this.getContext(), Uri.fromFile(receiptsViewModel.outFile));
            //if (cameraClass.detectTextFromReceipt(this.getContext(), imageUri)) {
            //}
        } else {
            System.out.println("Problem while taking image");
        }
    }


//    private void deleteImageUri() {
//        if (receiptsViewModel.outFile.exists()) {
//            if (receiptsViewModel.outFile.delete()) {
//                System.out.println("file Deleted :" + imageUri.toString());
//            } else {
//                System.out.println("file not Deleted :" + imageUri.toString());
//            }
//        }
//    }



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
            System.out.println("hi");

            if (((MainActivity)getActivity()).savedUsers == null) {
                System.out.println("kill yourself 8=============================================================================D");
            } else {
                System.out.println(("KJLSDFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFf"));
            }
//            receiptsViewModel.receiptItems = ((MainActivity)getActivity()).savedReceiptItems;
            receiptsViewModel.users = ((MainActivity)getActivity()).savedUsers;
            System.out.println(("Users: " + Integer.toString(receiptsViewModel.users.size()) + " items: " + Integer.toString(receiptsViewModel.receiptItems.size())));
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (!receiptsViewModel.camera) {
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

