package com.example.majorproject.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.majorproject.Models.HistoryModel;
import com.example.majorproject.R;

import java.util.ArrayList;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    Context context;
    ArrayList<HistoryModel> data;

    public HistoryAdapter(Context context, ArrayList<HistoryModel> data) {
        this.context = context;
        this.data = data;
    }

    @NonNull
    @Override
    public HistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_history, parent , false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull HistoryAdapter.ViewHolder holder, int position) {
        holder.CurrencyName.setText(data.get(position).getCoin_name());
        holder.UserBalance.setText(String.format("%.2f",data.get(position).getUser_balance())+"$");
        holder.DateTime.setText(data.get(position).getAction_date_and_time());

        if(data.get(position).getAction().equals("Sell")){
            holder.ActionMoney.setText("+"+String.format("%.2f",data.get(position).getMoney_flow())+"$");
            holder.ActionMoney.setTextColor(Color.GREEN);
        }else{
            holder.ActionMoney.setText("-"+String.format("%.2f",data.get(position).getMoney_flow())+"$");
            holder.ActionMoney.setTextColor(Color.RED);
        }

        holder.Leverage.setText(data.get(position).getPurchase_leverage_in()+"x");
        holder.Quantity.setText(String.valueOf(data.get(position).getCoin_quntity()));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView CurrencyName,UserBalance,DateTime,ActionMoney,Leverage,Quantity;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            CurrencyName = itemView.findViewById(R.id.historyCurrencyName);
            UserBalance = itemView.findViewById(R.id.historyUserBalance);
            DateTime = itemView.findViewById(R.id.historyDateTime);
            ActionMoney = itemView.findViewById(R.id.historyActionMoney);
            Leverage = itemView.findViewById(R.id.historyLeverage);
            Quantity = itemView.findViewById(R.id.historyQuntity);
        }
    }
}
