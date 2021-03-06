package com.example.diplomski;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.android.material.textfield.TextInputLayout;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;
import java.util.List;

public class AddBarcodeActivity extends AppCompatActivity {
    Button captureImage_button;
    Button next_button;
    Button generate_button;
    Button back_button;
    TextInputLayout barcodeNumber_editText;
    TextView barcode_textView;
    ImageView barcode_imageView;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch barcodeQR_switch;

    StoresDB myDB;

    String barcodeNumber;
    boolean switchSelection = false; //false = barcode; true = QR code

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_barcode);

        myDB = new StoresDB(AddBarcodeActivity.this);
        pref = getApplicationContext().
                getSharedPreferences("store_id", MODE_PRIVATE);
        editor = pref.edit();

        myDB = new StoresDB(AddBarcodeActivity.this);

        findViews();

        getIntentData();

        generate_button.setOnClickListener(v -> {
            generateBarcodeImage();
            storeBarcodeToDatabase();
            Toast.makeText(AddBarcodeActivity.this, "GENERATE SAVED TO DB", Toast.LENGTH_SHORT).show();
        });

        next_button.setOnClickListener(v -> {
            if(barcodeNumber_editText.getEditText().getText().length() <= 0){
                confirmNoBarcodeDialog();
            }
            else{
                if(myDB.getStoreBarcode(pref.getString("id", null)) == null){
                    storeBarcodeToDatabase();
                }
                startAddLogoActivity();
            }
        });

        back_button.setOnClickListener(v -> startAddNameActivity());

        barcodeQR_switch.setOnClickListener(v -> switchBarcodeStates());

        barcode_imageView.setOnClickListener(v -> selectAndPlaceBarcode());

        captureImage_button.setOnClickListener(v -> selectAndPlaceBarcode());
    }

    @Override
    public void onBackPressed() {
        startAddNameActivity();
    }

    private void generateBarcodeImage(){
        if(barcodeNumber_editText.getEditText().getText().toString().trim().length() <= 0) {
            barcodeNumber_editText.setError("Enter barcode manually field cannot be empty!");
            Toast.makeText(AddBarcodeActivity.this,
                    "This field can't be empty!", Toast.LENGTH_SHORT).show();
        }
        else if(barcodeNumber_editText.getEditText().getText().toString().trim().length() != 12
                && !switchSelection){
            Toast.makeText(AddBarcodeActivity.this,
                    "Barcode must be 12 numbers long!", Toast.LENGTH_SHORT).show();
        }
        else {
            MultiFormatWriter writer = new MultiFormatWriter();
            try {
                if (switchSelection) {
                    BitMatrix matrix = writer.encode(barcodeNumber_editText.getEditText().getText().toString()
                            .trim(), BarcodeFormat.QR_CODE, 350, 350);
                    BarcodeEncoder encoder = new BarcodeEncoder();
                    Bitmap bitmap = encoder.createBitmap(matrix);
                    barcode_imageView.setImageBitmap(bitmap);
                } else {
                    BitMatrix matrix = writer.encode(barcodeNumber_editText.getEditText().getText().toString()
                            .trim(), BarcodeFormat.CODE_128, 500, 350);
                    BarcodeEncoder encoder = new BarcodeEncoder();
                    Bitmap bitmap = encoder.createBitmap(matrix);
                    barcode_imageView.setImageBitmap(bitmap);
                }
                InputMethodManager manager = (InputMethodManager) getSystemService(
                        Context.INPUT_METHOD_SERVICE
                );
                manager.hideSoftInputFromWindow(barcode_textView.getApplicationWindowToken(), 0);
                if(switchSelection){
                    //TODO Save QR flag in database
                }else {
                    //TODO Save 1D barcode in database
                }
            } catch (WriterException e) {
                e.printStackTrace();
            }
        }
    }

    private void switchBarcodeStates(){
        switchSelection = !switchSelection;
        if(switchSelection){
            barcode_textView.setText("Generate QR code");
        }else{
            barcode_textView.setText("Generate Barcode");
        }
    }

    private void selectAndPlaceBarcode(){
        CropImage.activity().setGuidelines(CropImageView.Guidelines.ON)
                .start(AddBarcodeActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK){
                assert result != null;
                Uri resultUri = result.getUri();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                            this.getContentResolver(), resultUri);
                    getBarcodeFromImage(bitmap);
                    generateBarcodeImage();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void getBarcodeFromImage(Bitmap bitmap){
        TextRecognizer recognizer = new TextRecognizer.Builder(this).build();
        if(!recognizer.isOperational()){
            Toast.makeText(this, "Error occurred!", Toast.LENGTH_SHORT).show();
        }
        else{
            InputImage image = InputImage.fromBitmap(bitmap, 0);
            scanBarcodes(image);
        }
    }

    private void scanBarcodes(InputImage image){
        BarcodeScannerOptions options =
                new BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(
                                Barcode.FORMAT_ALL_FORMATS)
                        .build();

        BarcodeScanner scanner = BarcodeScanning.getClient();

        Task<List<Barcode>> result = scanner.process(image)
                .addOnSuccessListener(barcodes -> {
                    for(Barcode barcode : barcodes){
                        Rect bounds = barcode.getBoundingBox();
                        Point[] corners = barcode.getCornerPoints();

                        String rawValue = barcode.getRawValue();

                        String returnValue = barcode.getDisplayValue();
                        Toast.makeText(AddBarcodeActivity.this, rawValue,
                                Toast.LENGTH_SHORT).show();
                        barcodeNumber_editText.getEditText().setText(returnValue);
                    }
                }).addOnFailureListener(e -> Toast.makeText(AddBarcodeActivity.this,
                        "Barcode not recognized!", Toast.LENGTH_SHORT).show());
    }

    private void confirmNoBarcodeDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Barcode missing!");
        builder.setMessage("Are you sure you want to continue without "
                + "StoreName" + " store barcode?");
        builder.setPositiveButton("Yes", (dialog, which) -> startAddLogoActivity());
        builder.setNegativeButton("No", (dialog, which) -> {

        });
        builder.create().show();
    }

    private void startAddLogoActivity(){
        Intent intent = new Intent(AddBarcodeActivity.this, AddLogoActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void startAddNameActivity(){
        Intent intent = new Intent(AddBarcodeActivity.this, AddNameActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void findViews(){
        captureImage_button = findViewById(R.id.captureImage_AddBarcode_Button);
        next_button = findViewById(R.id.confirm_AddBarcode_Button);
        back_button = findViewById(R.id.back_AddBarcode_Button);
        barcodeNumber_editText = findViewById(R.id.barcodeInput_AddBarcode_editText);
        barcode_textView = findViewById(R.id.switchHint_AddBarcode_TextView);
        barcodeQR_switch = findViewById(R.id.barcodeQR_switch);
        generate_button = findViewById(R.id.generateBarcode_AddBarcode_Button);
        barcode_imageView = findViewById(R.id.barcodePreview_AddBarcode_ImageView);
    }

    private void getIntentData(){
        if(pref.getString("id", null) != null) {
            if (myDB.getStoreBarcode(pref.getString("id", null)) != null) {
                barcodeNumber_editText.getEditText().setText(myDB.getStoreBarcode(pref.getString("id", null)));
            }
        }
    }

    private void storeBarcodeToDatabase(){
        barcodeNumber = barcodeNumber_editText.getEditText().getText().toString().trim();
        if(pref.getString("id", null) == null) {
            Toast.makeText(AddBarcodeActivity.this,
                    "You are missing store name!", Toast.LENGTH_SHORT).show();
        }else if(barcodeNumber.isEmpty()) {
            barcodeNumber_editText.setError("Barcode is missing");
        }else if(barcodeNumber.equals("")){
            myDB.addStoreBarcode(pref.getString("id", null), null);
        }else{
            myDB.addStoreBarcode(pref.getString("id", null), barcodeNumber);
        }
    }
}