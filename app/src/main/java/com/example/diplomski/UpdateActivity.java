package com.example.diplomski;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;

public class UpdateActivity extends AppCompatActivity {

    EditText name_input;
    Button update_button, deleteButton, updateLogo_button;
    ImageView logo_imageView;

    String id, name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        name_input = findViewById(R.id.name_input_up);
        update_button = findViewById(R.id.update_button);
        deleteButton = findViewById(R.id.delete_button);
        updateLogo_button = findViewById(R.id.update_logo);
        logo_imageView = findViewById(R.id.addLogo_imageView);

        getAndSetIntentData();

        ActionBar ab = getSupportActionBar();
        if(name != null){
            ab.setTitle(name);
        }

        update_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                com.example.diplomski.StoreNamesDB myDB = new com.example.diplomski.StoreNamesDB(UpdateActivity.this);
                name = name_input.getText().toString().trim();
                myDB.updateData(id, name);
                ActionBar ab = getSupportActionBar();
                if(name != null){
                    assert ab != null;
                    ab.setTitle(name);
                }
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDialog();
            }
        });

        updateLogo_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectAndPlaceLogo();
            }
        });
    }

    void getAndSetIntentData(){
        if(getIntent().hasExtra("id") && getIntent().hasExtra("name")){
            //Getting Data From Intent
            id = getIntent().getStringExtra("id");
            name = getIntent().getStringExtra("name");
            //Setting Data
            name_input.setText(name);
        }else{
            Toast.makeText(this, "No data", Toast.LENGTH_SHORT).show();
        }
    }

    void confirmDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete " + name + "?");
        builder.setMessage("Are you sure you want to delete " + name + "?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                com.example.diplomski.StoreNamesDB myDB = new com.example.diplomski.StoreNamesDB(UpdateActivity.this);
                myDB.deleteOneRow(id);
                myDB.updateData(id, name);
                Intent intent = new Intent(UpdateActivity.this, com.example.diplomski.MainActivity.class);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.create().show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.update_item_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void selectAndPlaceLogo(){
        CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).start(UpdateActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK){
                Uri resultUri = result.getUri();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                    logo_imageView.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}