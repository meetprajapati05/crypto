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

public class WatchlistHomeAdapter extends RecyclerView.Adapter<WatchlistHomeAdapter.ViewHolder> {
    Context context;
    ArrayList<CryptoDataModel> cryptoData;
    int lastPostion = -1;

    public WatchlistHomeAdapter(Context context, ArrayList<CryptoDataModel> cryptoData) {
        this.context = context;
        this.cryptoData = cryptoData;

    }

    @NonNull
    @Override
    public WatchlistHomeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_home_watchlist,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WatchlistHomeAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Glide.with(context)
                .load(cryptoData.get(position).getImage())
                .into(holder.imageView);

        holder.name.setText(cryptoData.get(position).getName());
        holder.symbol.setText(cryptoData.get(position).getSymbol());
        holder.price.setText(String.format("%.2f",cryptoData.get(position).getCurrent_price())+"$");
        double inDeVal = cryptoData.get(position).getPrice_change_percentage_24h();

        holder.indeval.setText(String.format("%.2f",cryptoData.get(position).getPrice_change_percentage_24h()));
        if(inDeVal>=0){
            holder.indeval.setTextColor(Color.GREEN);
        }else {
            holder.indeval.setTextColor(Color.RED);
        }
        setAnimation(holder.itemView,position);
        //click any crypto detail or goto next page with data
        holder.cardClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MarketDetail.class);
                intent.putExtra("name",cryptoData.get(position).getName());
                intent.putExtra("symbol",cryptoData.get(position).getSymbol());
                intent.putExtra("type",cryptoData.get(position).getType());
                intent.putExtra("id",cryptoData.get(position).getId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cryptoData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView name,price,symbol,indeval;
        CardView cardClick;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.topGainImage);
            name = itemView.findViewById(R.id.topGainName);
            price = itemView.findViewById(R.id.topGainPrice);
            symbol = itemView.findViewById(R.id.topGainSymbol);
            indeval = itemView.findViewById(R.id.topGainInDeVal);
            cardClick = itemView.findViewById(R.id.topGainCard);
        }
    }

    public void setAnimation(View viewType,int position){
        if(position>lastPostion){
            Animation animation = AnimationUtils.loadAnimation(context,android.R.anim.fade_in);
            viewType.startAnimation(animation);
            lastPostion = position;
        }
    }
}