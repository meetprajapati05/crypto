package com.example.majorproject.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.majorproject.Models.HistoryModel;
import com.example.majorproject.R;
import com.example.majorproject.SellPage;

import java.util.ArrayList;

public class HoldingAdapter extends RecyclerView.Adapter<HoldingAdapter.ViewHolder> {

    Context context;
    ArrayList<HistoryModel> data;
    double currentPrice;

    public HoldingAdapter(Context context, ArrayList<HistoryModel> data,double currentPrice) {
        this.context = context;
        this.data = data;
        this.currentPrice = currentPrice;
    }

    @NonNull
    @Override
    public HoldingAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_market_detail_holding, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull HoldingAdapter.ViewHolder holder, int position) {
        holder.CurrencyName.setText(data.get(position).getCoin_name());
        holder.UserBalance.setText(String.format("%.2f",data.get(position).getAction_time_coin_value() / data.get(position).getPurchase_leverage_in())+"$");
        holder.DateTime.setText(data.get(position).getAction_date_and_time());

        double changePercentage = ((currentPrice - data.get(position).getAction_time_coin_value()) / data.get(position).getAction_time_coin_value() ) * 100;

        if(changePercentage>=0){
            holder.ActionMoney.setText("+"+String.format("%.2f",changePercentage)+"%");
            holder.ActionMoney.setTextColor(Color.GREEN);
        }else{
            holder.ActionMoney.setText(changePercentage+"%");
            holder.ActionMoney.setTextColor(Color.RED);
        }

        holder.Leverage.setText(data.get(position).getPurchase_leverage_in()+"x");
        holder.Quantity.setText(String.valueOf(data.get(position).getCoin_quntity()));

        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent iSell = new Intent(context, SellPage.class);
                iSell.putExtra("id", data.get(position).getCoin_id());
                iSell.putExtra("purchase_date_and_time", data.get(position).getAction_date_and_time());
                context.startActivity(iSell);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView CurrencyName,UserBalance,DateTime,ActionMoney,Leverage,Quantity;
        LinearLayout card;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            CurrencyName = itemView.findViewById(R.id.holdingCoinName);
            UserBalance = itemView.findViewById(R.id.holdingPrice);
            DateTime = itemView.findViewById(R.id.holdingDate);
            ActionMoney = itemView.findViewById(R.id.holdingChange);
            Leverage = itemView.findViewById(R.id.holdingLeverage);
            Quantity = itemView.findViewById(R.id.holdingQuantity);
            card = itemView.findViewById(R.id.layoutHoldingClick);
        }
    }
}
