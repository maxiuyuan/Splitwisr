package com.splitwisr.ui.camera;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;

import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.splitwisr.R;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class CameraActivity extends AppCompatActivity {

    private Button captureImageButton;
    private Button detectTextButton;
    private ImageView imageView;
    private ListView receiptListView;
    private File outFile;
    private Uri imageUri;
    private String mCameraFileName;
    private List<String> receiptItems = new ArrayList<>();

    static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        captureImageButton = findViewById(R.id.capture_image);
        detectTextButton = findViewById(R.id.detect_text);
        imageView = findViewById(R.id.image_view);
        receiptListView = findViewById(R.id.receipt_list_view);

        captureImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        detectTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detectTextFromReceipt();
            }
        });
    }

    private void dispatchTakePictureIntent() {

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        Date date = new Date();
        DateFormat df = new SimpleDateFormat("-mm-ss");

        String newPicFile = df.format(date) + ".jpg";
        outFile = new File(getApplication().getApplicationContext().getFilesDir(), newPicFile);
        mCameraFileName = outFile.toString();
        imageUri = Uri.fromFile(outFile);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(outFile));

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            if (data != null) {
                //imageView.setImageURI(null);
                //imageView.setImageURI(imageUri);
                try {
                    System.out.println(imageUri.toString());
                    Bitmap bitmap = android.provider.MediaStore.Images.Media
                            .getBitmap(this.getContentResolver(), imageUri);
                    imageView.setImageBitmap(bitmap);
                }
                catch (Exception e) {
                    Toast.makeText(CameraActivity.this, "ERROR: " + e.getMessage(), Toast.LENGTH_SHORT);
                }

                imageView.setVisibility(View.VISIBLE);
            }
            if (imageUri == null && mCameraFileName != null) {
                imageUri = Uri.fromFile(new File(mCameraFileName));
                imageView.setImageURI(null);
                imageView.setImageURI(imageUri);
                imageView.setVisibility(View.VISIBLE);
            }
            File file = new File(mCameraFileName);
            if (!file.exists()) {
                file.mkdir();
            }
        }
    }

    private void deleteImageUri() {
        if (outFile.exists()) {
            if (outFile.delete()) {
                System.out.println("file Deleted :" + imageUri.toString());
            } else {
                System.out.println("file not Deleted :" + imageUri.toString());
            }
        }
    }

    private void detectTextFromReceipt() {
        try {
            FirebaseVisionImage image = FirebaseVisionImage.fromFilePath(getApplication().getApplicationContext(), imageUri);
            FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
            Task<FirebaseVisionText> result =
                    detector.processImage(image)
                            .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                                @Override
                                public void onSuccess(FirebaseVisionText firebaseVisionText) {
                                    displayTextFromImage(firebaseVisionText);
                                }
                            })
                            .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(CameraActivity.this, "ERROR: " + e.getMessage(), Toast.LENGTH_SHORT);
                                            Log.d("Error ", e.getMessage());
                                        }
                                    });
        }
        catch (Exception e){
            Toast.makeText(CameraActivity.this, "ERROR: " + e.getMessage(), Toast.LENGTH_SHORT);
        }
        deleteImageUri();
    }

    private void displayTextFromImage(FirebaseVisionText firebaseVisionText) {
        List<FirebaseVisionText.TextBlock> textBlockList = firebaseVisionText.getTextBlocks();
        if (textBlockList.isEmpty()) {
            Toast.makeText(CameraActivity.this, "No text was detected", Toast.LENGTH_SHORT);
        }
        else {
            for (FirebaseVisionText.TextBlock block : firebaseVisionText.getTextBlocks()) {
                receiptItems.add(block.getText());
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_list_item_1,
                    receiptItems
            );
            receiptListView.setAdapter(adapter);
        }
    }


}