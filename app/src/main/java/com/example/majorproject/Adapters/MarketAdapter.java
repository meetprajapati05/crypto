package com.example.majorproject.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.majorproject.MarketDetail;
import com.example.majorproject.Models.CryptoDataModel;
import com.example.majorproject.R;

import java.util.ArrayList;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MarketAdapter extends RecyclerView.Adapter<MarketAdapter.ViewHolder> {

    Context context;
    ArrayList<CryptoDataModel> data;
    int lastPosition = -1;

    public MarketAdapter(Context context, ArrayList<CryptoDataModel> data) {
        this.context = context;
        this.data = data;
    }

    @NonNull
    @Override
    public MarketAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.market_recycler_layout, parent , false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MarketAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        //Set animation on itemView
        setAnimation(holder.itemView , position);

        //Set Crypto Image
        Glide.with(context)
                .load(data.get(position).getImage())
                .into(holder.imgCrypto);

        //Set Crypto Name
        holder.txtName.setText(data.get(position).getName());

        //Set Crypto Price
        holder.txtPrice.setText(String.format("%.2f",data.get(position).getCurrent_price()));

        //Set Crypto Symbol
        holder.txtSymbol.setText(data.get(position).getSymbol());

        //Set image up down for in de val and  change in de val color and Set last 24h Increment Decrement Value
        if(data.get(position).getPrice_change_percentage_24h() >= 0){
            holder.txtInDeVal.setText(String.format("%.2f",data.get(position).getPrice_change_percentage_24h())+"%");
            holder.imgInDeVal.setImageResource(R.drawable.baseline_arrow_drop_up_24);
            holder.txtInDeVal.setTextColor(Color.GREEN);
        }else{
            holder.txtInDeVal.setText(String.format("%.2f",Math.abs(data.get(position).getPrice_change_percentage_24h()))+"%");
            holder.imgInDeVal.setImageResource(R.drawable.baseline_arrow_drop_down_24);
            holder.txtInDeVal.setTextColor(Color.RED);
        }

        //Set Price symbol
        if(Objects.equals(data.get(position).getType(), "inr")){
            holder.txtPriceSymbol.setText("\u20B9");
        }else if (Objects.equals(data.get(position).getType(), "btc")){
            holder.txtPriceSymbol.setText("₿");
        }else if (Objects.equals(data.get(position).getType(), "eth")){
            holder.txtPriceSymbol.setText("Ξ");
        }else if (Objects.equals(data.get(position).getType(), "eur")){
            holder.txtPriceSymbol.setText("€");
        }else{
            holder.txtPriceSymbol.setText("$");
        }

        //Card click to move next page
        holder.marketCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, MarketDetail.class);
                intent.putExtra("name",data.get(position).getName());
                intent.putExtra("symbol",data.get(position).getSymbol());
                intent.putExtra("type",data.get(position).getType());
                intent.putExtra("id",data.get(position).getId());
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView imgCrypto;
        ImageView imgInDeVal;
        TextView txtName, txtPrice, txtPriceSymbol, txtSymbol, txtInDeVal;
        CardView marketCard;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgCrypto = itemView.findViewById(R.id.imageCrypto);
            txtName = itemView.findViewById(R.id.txtCryptoName);
            txtPrice = itemView.findViewById(R.id.txtCryptoPrice);
            txtPriceSymbol = itemView.findViewById(R.id.txtCryptoPriceSymbol);
            txtSymbol = itemView.findViewById(R.id.txtCryptoSymbol);
            txtInDeVal = itemView.findViewById(R.id.txtCryptoInDeVal);
            imgInDeVal = itemView.findViewById(R.id.imgInDeVal);
            marketCard = itemView.findViewById(R.id.cardViewMarket);

        }
    }

    public void setAnimation(View viewType, int position){
        if(position>lastPosition){
            Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
            viewType.startAnimation(animation);
            lastPosition=position;
        }
    }

}
