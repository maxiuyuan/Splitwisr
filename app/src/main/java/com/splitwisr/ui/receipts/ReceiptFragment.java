package com.splitwisr.ui.receipts;

import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
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
import androidx.recyclerview.widget.RecyclerView;
import com.abdeveloper.library.MultiSelectDialog;
import com.abdeveloper.library.MultiSelectModel;
import com.splitwisr.MainActivity;
import com.splitwisr.R;
import com.splitwisr.databinding.ReceiptFragmentBinding;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

class ReceiptUpdateThread implements Runnable {
    @Override
    public void run() {
        ReceiptFragment.receiptsAdapater.notifyDataSetChanged();
        ReceiptFragment.binding.emptyStateImage.setVisibility(
                (ReceiptFragment.receiptsViewModel.getReceipts().isEmpty())? View.VISIBLE : View.GONE);
    }
}

public class ReceiptFragment extends Fragment {

    static ReceiptsViewModel receiptsViewModel;
    static ReceiptFragmentBinding binding;
    static ReceiptsAdapater receiptsAdapater;

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
        if (((MainActivity)getActivity()).receiptState == MainActivity.ReceiptStates.BASE_STATE) {
            receiptsViewModel.updatePersonsList();
        }

        View view = binding.getRoot();

        binding.addItemButton.setOnClickListener(v -> {
            if (binding.itemCostText.getText().toString().isEmpty()
                    || binding.itemNameText.getText().toString().isEmpty()) {
                return;
            }
            double itemCost = receiptsViewModel.round(Double.parseDouble(binding.itemCostText.getText().toString()));
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
            ((MainActivity)getActivity()).receiptState = MainActivity.ReceiptStates.RETURNING_FROM_CAMERA;
            System.out.println("RETURNING FROM CAMERA ================");
            dispatchTakePictureIntent();
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
                            receiptsViewModel.addPersonToReceipt(i, ids);
                            receiptsAdapater.setData(receiptsViewModel.getReceipts());
                        },
                        ()->{})
        );

        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.addItemDecoration(new BottomMarginDecoration());
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
                Log.e("ReceiptFragment", "Exception thrown cant create file" + e.getMessage());
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
                receiptsViewModel.addReceiptItem(ReceiptsViewModel.round(newItemCosts.get(x)), newItemNames.get(x));
            }
            receiptsAdapater.setData(receiptsViewModel.getReceipts());
            getActivity().runOnUiThread(new ReceiptUpdateThread());
        }
        // delete stored image as we no longer need it
        deleteImageUri();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == getActivity().RESULT_OK) {
            receiptsViewModel.cameraClass.detectTextFromReceipt(this.getContext(), Uri.fromFile(receiptsViewModel.outFile), this);
        } else {
            Log.e("ReceiptFragment", "Problem while taking image");
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
        if (((MainActivity)getActivity()).receiptState == MainActivity.ReceiptStates.BASE_STATE) {
            splitUsersDialog(
                    (ids, selectedNames, dataString) -> {
                        receiptsViewModel.updateSelectedPersons(ids);
                    },
                    this::navigateToBalancesFragment);
        } else {
            System.out.println("VIEW NOT BASE STATE ================");
            if (((MainActivity)getActivity()).receiptState == MainActivity.ReceiptStates.RETURNING_FROM_CAMERA) {
                ((MainActivity)getActivity()).receiptState = MainActivity.ReceiptStates.RETURNING_FROM_MAIN;
                System.out.println("VIEW RETURNING FROM CAMERA");
            } else if (((MainActivity)getActivity()).receiptState == MainActivity.ReceiptStates.RETURNING_FROM_MAIN) {
                ((MainActivity)getActivity()).receiptState = MainActivity.ReceiptStates.RETURNED_PAST_VIEW;
                System.out.println("VIEW RETURNING FROM MAIN");
            } else if (((MainActivity)getActivity()).receiptState == MainActivity.ReceiptStates.RETURNED_PAST_PAUSE) {
                ((MainActivity)getActivity()).receiptState = MainActivity.ReceiptStates.BASE_STATE;
                System.out.println("VIEW PAST PAUSE");
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        receiptsAdapater.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (((MainActivity)getActivity()).receiptState == MainActivity.ReceiptStates.BASE_STATE) {
            System.out.println("RESETTING");
            receiptsViewModel.reset();
        } else if (((MainActivity)getActivity()).receiptState == MainActivity.ReceiptStates.RETURNING_FROM_MAIN) {
            ((MainActivity)getActivity()).receiptState = MainActivity.ReceiptStates.RETURNED_PAST_PAUSE;
        } else if (((((MainActivity)getActivity()).receiptState == MainActivity.ReceiptStates.RETURNED_PAST_VIEW))) {
            ((MainActivity)getActivity()).receiptState = MainActivity.ReceiptStates.BASE_STATE;
        }
    }

    private void splitUsersDialog(
            MultiSelectModelOnSelected onSelected,
            MultiSelectModelOnCancel onCancel ){

        List<MultiSelectModel> modelList = receiptsViewModel.getModelList(receiptsViewModel.getNames());
        MultiSelectDialog multiSelectDialog = new MultiSelectDialog()
                .title("Select Users/Groups")
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

    class BottomMarginDecoration extends RecyclerView.ItemDecoration {

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            // only for the last one
            if (parent.getChildAdapterPosition(view) == parent.getAdapter().getItemCount() - 1) {
                outRect.bottom = 400;
            }
        }
    }

}

