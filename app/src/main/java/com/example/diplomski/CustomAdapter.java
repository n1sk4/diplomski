package com.example.diplomski;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder> {

    private Context context;
    private ArrayList storeID, storeName, storeBarcode;

    int position;

    CustomAdapter(Context context,
                  ArrayList store_id,
                  ArrayList store_name,
                  ArrayList store_barcode){

        this.context = context;
        this.storeID = store_id;
        this.storeName = store_name;
        this.storeBarcode = store_barcode;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.my_row, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        holder.storeName_textView.setText(String.valueOf(storeName.get(position)));

        holder.mainLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent intent = new Intent(context, com.example.diplomski.UpdateActivity.class);
                intent.putExtra("id", String.valueOf(storeID.get(position)));
                intent.putExtra("name", String.valueOf(storeName.get(position)));
                intent.putExtra("barcode", String.valueOf(storeBarcode.get(position)));
                context.startActivity(intent);
                return true;
            }
        });

        holder.mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, com.example.diplomski.BarcodeActivity.class);
                intent.putExtra("id", String.valueOf(storeID.get(position)));
                intent.putExtra("name", String.valueOf(storeName.get(position)));
                intent.putExtra("barcode", String.valueOf(storeBarcode.get(position)));
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return storeID.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView storeName_textView;
        ImageView storeLogo_ImageView;
        LinearLayout mainLayout;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            storeName_textView = itemView.findViewById(R.id.store_name_txt);
            storeLogo_ImageView = itemView.findViewById(R.id.logo_imageView);
            mainLayout = itemView.findViewById(R.id.mainLayout);
        }
    }
}