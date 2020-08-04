package com.splitwisr.ui.camera;

import androidx.annotation.NonNull;

import android.content.Context;
import android.graphics.Point;
import android.net.Uri;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.splitwisr.ui.receipts.ReceiptFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CameraClass {
    static final float SCALING_FACTOR = 70f / 3f;
    private List<String> itemNames;
    private List<Double> itemCosts;

    public List<String> getItemNames() {
        return itemNames;
    }

    public List<Double> getItemCosts() {
        return itemCosts;
    }

    public boolean detectTextFromReceipt(Context c, Uri imageUri, ReceiptFragment r) {
        try {
            FirebaseVisionImage image = FirebaseVisionImage.fromFilePath(c, imageUri);
            FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
            Task<FirebaseVisionText> result =
                    detector.processImage(image)
                            .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                                @Override
                                public void onSuccess(FirebaseVisionText firebaseVisionText) {
                                    parseTextFromImage(firebaseVisionText, r);
                                    r.addScannedItems();
                                }
                            })
                            .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                        }
                                    });
            return true;
        }
        catch (Exception e){
            System.out.println("Exception throw while doing ml stuff" + e.getMessage());
            return false;
        }
    }

    private void parseTextFromImage(FirebaseVisionText firebaseVisionText, ReceiptFragment r) {
        List<FirebaseVisionText.TextBlock> textBlockList = firebaseVisionText.getTextBlocks();

        itemNames = new ArrayList<>();
        itemCosts = new ArrayList<>();

        if (textBlockList.isEmpty()) {
            Toast.makeText(r.getActivity(), "No text was detected", Toast.LENGTH_SHORT).show();
        }

        else {

            // intermediate list to sort lines by coordinate
            ArrayList<FirebaseVisionText.Line> receiptLines = new ArrayList<>();

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

        }
    }

    private void addLineToReceiptItems(String line) {
        if (line.matches("(.*)\\$([0-9Oo]*[.])?[0-9Oo]*+[ .*]?")) {
            String[] strs = line.split("\\$");
            // Split the line into the name and the price
            if (strs.length == 2) {
                itemNames.add(strs[0]);
                StringBuilder cost = new StringBuilder();
                for (int x = 0; x < strs[1].length(); x++) {
                    char c = strs[1].charAt(x);
                    if (c == ' ') break;
                    if (c == 'O' || c == 'o') c = '0';
                    cost.append(c);
                }
                itemCosts.add(Double.parseDouble(cost.toString()));
            }
        }
    }



}
