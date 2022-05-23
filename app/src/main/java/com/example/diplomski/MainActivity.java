package com.example.diplomski;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    FloatingActionButton add_fab;
    RecyclerView recyclerView;
    TextView noData_textView;
    ImageView noData_imageView;
    View fabCircle_View;

    StoresDB myDB;
    ArrayList<String> store_id, store_name, store_barcode;
    byte[] store_logo_blob;
    ArrayList<Bitmap> store_logo;

    CustomAdapter customAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViews();

        myDB = new StoresDB(MainActivity.this);
        store_id = new ArrayList<>();
        store_name = new ArrayList<>();
        store_barcode = new ArrayList<>();
        store_logo = new ArrayList<>();

        storeDataInArrays();

        customAdapter = new CustomAdapter(MainActivity.this,
                store_id, store_name, store_barcode, store_logo);
        recyclerView.setAdapter(customAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));

        add_fab.setOnClickListener(v -> {
            animateFABAndStartActivity();
        });

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelperCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.delete_all){
            confirmDeleteAllDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    private void confirmDeleteAllDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete All?");
        builder.setMessage("Are you sure you want to delete all Data?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            StoresDB myDB = new StoresDB(MainActivity.this);
            myDB.deleteAllData();
            recreate();
        });
        builder.setNegativeButton("No", (dialog, which) -> {

        });
        builder.create().show();
    }

    ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.START
                    | ItemTouchHelper.END, 0) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView,
                              @NonNull RecyclerView.ViewHolder viewHolder,
                              @NonNull RecyclerView.ViewHolder target) {
            int fromPosition = viewHolder.getAdapterPosition();
            int toPosition = target.getAdapterPosition();

            Collections.swap(store_id, fromPosition, toPosition);

            Objects.requireNonNull(recyclerView.getAdapter()).
                    notifyItemMoved(fromPosition, toPosition);

            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

        }
    };

    private void findViews(){
        recyclerView = findViewById(R.id.recyclerView);
        add_fab = findViewById(R.id.add_button);
        noData_textView = findViewById(R.id.no_data);
        noData_imageView = findViewById(R.id.no_data_imageView);
        fabCircle_View = findViewById(R.id.fabCircle_Main_View);
    }

    private void storeDataInArrays(){
        Cursor cursor = myDB.readAllData();
        if(cursor.getCount() == 0){
            noData_imageView.setVisibility(View.VISIBLE);
            noData_textView.setVisibility(View.VISIBLE);
        }else{
            while (cursor.moveToNext()){
                store_id.add(cursor.getString(0));
                store_name.add(cursor.getString(1));
                store_barcode.add(cursor.getString(2));
                store_logo_blob = cursor.getBlob(3);
                if(store_logo_blob == null){
                    store_logo.add(null);
                }else {
                    store_logo.add(BitmapFactory.decodeByteArray(store_logo_blob, 0,
                            store_logo_blob.length));
                }
            }

            noData_imageView.setVisibility(View.GONE);
            noData_textView.setVisibility(View.GONE);
        }
    }

    private void startAddNameActivity(){
        Intent intent = new Intent(MainActivity.this, AddNameActivity.class);
        startActivity(intent);
    }

    private void animateFABAndStartActivity(){
        Animation animation = AnimationUtils.loadAnimation(MainActivity.this,
                R.anim.main_layout_fab_circle_animation);
        animation.setFillAfter(true);
        fabCircle_View.startAnimation(animation);
        fabCircle_View.postDelayed(new Runnable() {
            @Override
            public void run() {
                startAddNameActivity();
            }
        }, 300);
    }
}