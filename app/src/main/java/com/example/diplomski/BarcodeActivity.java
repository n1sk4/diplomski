package com.example.diplomski;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class BarcodeActivity extends AppCompatActivity {

    ImageView barcode_imageView;

    String id, name, barcode;

    boolean barcodeExists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode);

        findViews();

        getIntentData();

        ActionBar ab = getSupportActionBar();
        if(name != null){
            ab.setTitle(name + " barcode");
        }

        generateBarcodeImage();
    }

    private void getIntentData(){
        if(getIntent().hasExtra("id") && getIntent().hasExtra("name")){
            //Getting Data From Intent
            id = getIntent().getStringExtra("id");
            name = getIntent().getStringExtra("name");
            if(getIntent().hasExtra("barcode")){
                barcode = getIntent().getStringExtra("barcode");
                barcodeExists = true;
            }
            else{
                Toast.makeText(this, "Barcode doesn't exist for " + name + " store", Toast.LENGTH_SHORT).show();
                barcodeExists = false;
            }
        }else{
            Toast.makeText(this, "No data", Toast.LENGTH_SHORT).show();
        }
    }

    private void generateBarcodeImage(){
        MultiFormatWriter writer = new MultiFormatWriter();
        if(barcodeExists){
            try {
                int width, height;
                width = 750;
                height = 1000;
                BitMatrix matrix = writer.encode(barcode, BarcodeFormat.CODE_128, width, height);
                BarcodeEncoder encoder = new BarcodeEncoder();
                Bitmap bitmap = encoder.createBitmap(matrix);
                barcode_imageView.setImageBitmap(bitmap);
            } catch (WriterException e) {
                e.printStackTrace();
            }
        }else {
            //TODO Do something
        }
    }

    private void findViews(){
        barcode_imageView = findViewById(R.id.barcode_Barcode_ImageView);
    }
}