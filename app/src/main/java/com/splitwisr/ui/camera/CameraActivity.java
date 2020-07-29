package com.splitwisr.ui.camera;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;

import android.os.Environment;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.HashMap;

import static android.app.Activity.RESULT_OK;

public class CameraActivity extends AppCompatActivity {

    private Button captureImageButton;
    private Button detectTextButton;
    private ImageView imageView;
    private ListView receiptListView;
    private File outFile;
    private Uri imageUri;
    private List<String> receiptItems = new ArrayList<>();
    static final float SCALING_FACTOR = 70f / 3f;
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

        Long tsLong = System.currentTimeMillis()/1000;
        String ts = tsLong.toString() + ".jpg";

        //String newPicFile = df.format(date) + ".jpg";

        outFile = new File(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES), ts);
        if (!outFile.exists()) {
            System.out.println("UWUUUUUUUUUUUUUUUUUU FILE DIDNT EXIST");
            try {
                outFile.createNewFile();
            } catch (Exception e) {
                System.out.println("UUUUUUUUUUUUUWUUUUUUUUUUUUUUUUu cant create file" + e.getMessage());
            }
        }
         imageUri = Uri.fromFile(outFile);
        System.out.println("HELLO I AM A FILE 8======================================================D " + imageUri.toString());
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(outFile));

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {
                System.out.println(imageUri.toString());
                Bitmap bitmap = android.provider.MediaStore.Images.Media
                        .getBitmap(this.getContentResolver(), imageUri);
                imageView.setImageBitmap(bitmap);
            }
            catch (Exception e) {
                System.out.print("UWUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUU EXCEPTION DID A BADEE " + e.getMessage());
                Toast.makeText(CameraActivity.this, "ERROR: " + e.getMessage(), Toast.LENGTH_SHORT);
            }

            imageView.setVisibility(View.VISIBLE);
        } else {
            System.out.println("UWUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUU result isnt okay");
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
            FirebaseVisionImage image = FirebaseVisionImage.fromFilePath(this, imageUri);
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
            deleteImageUri();
        }
        catch (Exception e){
            Toast.makeText(CameraActivity.this, "ERROR: " + e.getMessage(), Toast.LENGTH_SHORT);
        }
    }

    // used to filter unwanted lines
    private void addLineToReceiptItems(String line) {
        if (line.matches("(.*)\\$([0-9Oo]*[.])?[0-9Oo]+[ ]+.")) {
            receiptItems.add(line);
        }
        /*
        else {
            receiptItems.add(line + " FILTERED");
        }

         */
    }

    private void displayTextFromImage(FirebaseVisionText firebaseVisionText) {
        List<FirebaseVisionText.TextBlock> textBlockList = firebaseVisionText.getTextBlocks();

        if (textBlockList.isEmpty()) {
            Toast.makeText(CameraActivity.this, "No text was detected", Toast.LENGTH_SHORT);
        }
        else {
            ArrayList<FirebaseVisionText.Line> receiptLines = new ArrayList<>();

            // TODO remove failed attempt
            // match preliminary item to price
            //LinkedHashMap<Integer, StringBuilder> itemsMap = new LinkedHashMap<Integer, StringBuilder>();

            // for calculate scaling factor from min/max points, since receipt
            //    width is standard
            int leftBound = Integer.MAX_VALUE;
            int rightBound = Integer.MIN_VALUE;

            // find min and max x-values
            for (FirebaseVisionText.TextBlock block : textBlockList) {
                for (FirebaseVisionText.Line line : block.getLines()) {
                    Point[] lineCornerPoints = line.getCornerPoints();
                    if (leftBound > lineCornerPoints[0].x) {
                        leftBound = lineCornerPoints[0].x;
                    }
                    if (rightBound < lineCornerPoints[1].x) {
                        rightBound = lineCornerPoints[1].x;
                    }

                    // add receipt lines for sorting while we are at it
                    receiptLines.add(line);
                }
            }

            // calculate line height from width and scaling factor
            int lineHeight = (int) ((rightBound - leftBound) / SCALING_FACTOR);

            // sort by y-value then x-value
            Collections.sort(receiptLines, (line, t1) -> {
                int c;
                // allow for +/- 20% line height error
                int acceptableError = (int) (lineHeight * 0.2);

                // if y-values do not match
                if (line.getCornerPoints()[0].y - acceptableError > t1.getCornerPoints()[0].y ||
                        t1.getCornerPoints()[0].y > line.getCornerPoints()[0].y + acceptableError)
                    c =  line.getCornerPoints()[0].y < (t1.getCornerPoints()[0].y) ? -1 : 1;
                else
                    c = 0;

                // if y values did match, then sort by x-value
                if (c == 0)
                    c =  line.getCornerPoints()[0].x < (t1.getCornerPoints()[0].x) ? -1 : 1;
                return c;
            });

            // TODO REMOVE OLD STUFF
            /*
                for (FirebaseVisionText.Line line: block.getLines()) {
                    Point[] lineCornerPoints = line.getCornerPoints();


                    // round to nearest line
                    int rounded = (lineCornerPoints[0].y / lineHeight ) * lineHeight;

                    if (itemsMap.containsKey(rounded)) {
                        itemsMap.get(rounded).append(" ").append(line.getText()).append(lineCornerPoints[0].y);
                    }
                    else {
                        itemsMap.put(rounded, new StringBuilder(line.getText() + lineCornerPoints[0].y));
                    }


                }
            }


            for (int i : itemsMap.keySet()) {
                receiptItems.add(itemsMap.get(i).toString());
            }

             */

            // build each line and append to receiptItems
            StringBuilder curLine = new StringBuilder();
            int prevY = 0;
            for (FirebaseVisionText.Line line: receiptLines) {
                int curY = line.getCornerPoints()[0].y;
                int acceptableError = (int) (lineHeight * 0.5);

                // if we see a new line, add prev line and clear string builder
                if (curY > prevY + acceptableError) {
                    String prevLine = curLine.toString();
                    curLine.setLength(0);
                    curLine.append(line.getText());
                    addLineToReceiptItems(prevLine);
                }
                else {
                    curLine.append(" ").append(line.getText());
                }
                prevY = curY;
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