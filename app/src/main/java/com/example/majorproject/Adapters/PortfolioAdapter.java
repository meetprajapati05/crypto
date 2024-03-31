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
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.majorproject.MarketDetail;
import com.example.majorproject.Models.CryptoDataModel;
import com.example.majorproject.Models.PortfolioModel;
import com.example.majorproject.R;
import com.example.majorproject.SellPage;

import java.util.ArrayList;

public class PortfolioAdapter extends RecyclerView.Adapter<PortfolioAdapter.ViewHolder> {
    Context context;
    ArrayList<CryptoDataModel> cryptoDataModels;
    ArrayList<PortfolioModel> portfolioModels;
    int lastPosition=-1;

    public PortfolioAdapter(Context context, ArrayList<CryptoDataModel> cryptoDataModels, ArrayList<PortfolioModel> portfolioModels) {
        this.context = context;
        this.cryptoDataModels = cryptoDataModels;
        this.portfolioModels = portfolioModels;
    }

    @NonNull
    @Override
    public PortfolioAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.portfolio_recycler_layout, parent , false);
        return new ViewHolder(view);
    }

    @SuppressLint("RecyclerView")
    @Override
    public void onBindViewHolder(@NonNull PortfolioAdapter.ViewHolder holder, int position) {
        for(int i =0 ; i<cryptoDataModels.size(); i++) {
            if (cryptoDataModels.get(i).getId().equals(portfolioModels.get(position).getCoin_id())) {
                Glide.with(context)
                        .load(cryptoDataModels.get(i).getImage())
                        .into(holder.coin_img);

                holder.txtCoinName.setText(cryptoDataModels.get(i).getName());

                holder.txtCoinSymbol.setText(cryptoDataModels.get(i).getSymbol());

                holder.txtCoinRealPrice.setText(String.format("%.2f", cryptoDataModels.get(i).getCurrent_price()));

                holder.txtCoinInDeVal.setText(String.format("%.2f", cryptoDataModels.get(i).getPrice_change_percentage_24h()));
                if (cryptoDataModels.get(i).getPrice_change_percentage_24h() >= 0) {
                    holder.txtCoinInDeVal.setTextColor(Color.GREEN);
                } else {
                    holder.txtCoinInDeVal.setTextColor(Color.RED);
                }

                holder.txtQuantity.setText(String.valueOf(portfolioModels.get(position).getCoin_quantity()));

                holder.txtLeverage.setText(String.format("%dx", portfolioModels.get(position).getPurchase_leverage_in()));

                holder.txtPurchaseVal.setText(String.valueOf(String.format("%.2f",portfolioModels.get(position).getPurchase_value())));
            }
        }

        holder.upperClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent iDetail = new Intent(context, MarketDetail.class);
                iDetail.putExtra("name", portfolioModels.get(position).getCoin_name());
                iDetail.putExtra("symbol", portfolioModels.get(position).getCoin_symbol());
                iDetail.putExtra("type", "usd");
                iDetail.putExtra("id", portfolioModels.get(position).getCoin_id());
                iDetail.putExtra("purchase_date_and_time", portfolioModels.get(position).getPurchase_date_and_time());
                context.startActivity(iDetail);
            }
        });

        holder.bottomClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent iSell = new Intent(context, SellPage.class);
                iSell.putExtra("id",portfolioModels.get(position).getCoin_id());
                iSell.putExtra("purchase_date_and_time", portfolioModels.get(position).getPurchase_date_and_time());
                context.startActivity(iSell);
            }
        });

        setAnimation(holder.itemView, position);

    }

    @Override
    public int getItemCount() {
        return portfolioModels.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView coin_img;
        TextView txtCoinName, txtCoinPrice, txtCoinSymbol, txtCoinRealPrice, txtCoinInDeVal;
        TextView txtQuantity,txtLeverage,txtPurchaseVal;
        RelativeLayout bottomClick, upperClick;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            coin_img = itemView.findViewById(R.id.portfolioImages);
            txtCoinName = itemView.findViewById(R.id.portfolioCurrencyName);
            txtCoinPrice = itemView.findViewById(R.id.portfolioCurrencyPrice);
            txtCoinSymbol = itemView.findViewById(R.id.portfolioCurrencySymbol);
            txtCoinRealPrice = itemView.findViewById(R.id.portfolioCurrencyPrice);
            txtCoinInDeVal = itemView.findViewById(R.id.portfolioCurrencyInDeVal);
            txtQuantity = itemView.findViewById(R.id.portfolioCurrencyQuantity);
            txtLeverage = itemView.findViewById(R.id.portfolioLeverage);
            txtPurchaseVal = itemView.findViewById(R.id.portfolioHoldingPrice);

            bottomClick = itemView.findViewById(R.id.bottomDetailLayout);
            upperClick = itemView.findViewById(R.id.topDetailLayout);
        }
    }

    public  void  setAnimation(View viewType, int position){
        if(position>lastPosition){
            Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
            viewType.setAnimation(animation);
            lastPosition = position;
        }
    }
}
