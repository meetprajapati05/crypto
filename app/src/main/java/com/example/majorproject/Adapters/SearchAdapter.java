package com.example.majorproject.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.majorproject.Models.CryptoDataModel;
import com.example.majorproject.R;

import java.util.ArrayList;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {
    Context context;
    ArrayList<CryptoDataModel> data;

    int lastPostition = -1;


    public SearchAdapter(Context context, ArrayList<CryptoDataModel> data) {
        this.context = context;
        this.data = data;
    }

    @NonNull
    @Override
    public SearchAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.search_data_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchAdapter.ViewHolder holder, int position) {
        Glide.with(context)
                .load(data.get(position).getImage())
                .into(holder.imageView);

        holder.name.setText(data.get(position).getName());

        holder.symbol.setText(data.get(position).getSymbol());

        if(data.get(position).getMarket_cap_rank()!=null) {
            holder.rank.setText(data.get(position).getMarket_cap_rank());
        }

        setAnimation(holder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView name,symbol,rank;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageCryptoSearch);
            name = itemView.findViewById(R.id.txtCryptoNameSearch);
            symbol = itemView.findViewById(R.id.txtCryptoSymbolSearch);
            rank = itemView.findViewById(R.id.txtRankSearch);
        }
    }

    public void setAnimation(View viewType,int position){
        if(position>lastPostition){
            Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
            viewType.startAnimation(animation);
            lastPostition=position;
        }
    }
}
