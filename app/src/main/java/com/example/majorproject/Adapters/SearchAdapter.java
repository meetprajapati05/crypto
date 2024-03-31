package com.example.majorproject.Adapters;

import android.content.Context;
import android.content.Intent;
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
import com.example.majorproject.CalanderHistory;
import com.example.majorproject.MarketDetail;
import com.example.majorproject.Models.CryptoDataModel;
import com.example.majorproject.R;

import java.util.ArrayList;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {
    Context context;
    ArrayList<CryptoDataModel> data;
    String previousActivity;

    int lastPostition = -1;


    public SearchAdapter(Context context, ArrayList<CryptoDataModel> data, String previousActivity) {
        this.context = context;
        this.data = data;
        this.previousActivity = previousActivity;
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

        //Card click to move next page
        holder.searchCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(previousActivity==null) {
                    Intent intent = new Intent(context, MarketDetail.class);
                    intent.putExtra("name", data.get(position).getName());
                    intent.putExtra("symbol", data.get(position).getSymbol());
                    intent.putExtra("type", data.get(position).getType());
                    intent.putExtra("id", data.get(position).getId());
                    context.startActivity(intent);
                }else{
                    Intent intent = new Intent(context, CalanderHistory.class);
                    intent.putExtra("id", data.get(position).getId());
                    context.startActivity(intent);
                }
            }
        });

        setAnimation(holder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView name,symbol,rank;
        CardView searchCard;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageCryptoSearch);
            name = itemView.findViewById(R.id.txtCryptoNameSearch);
            symbol = itemView.findViewById(R.id.txtCryptoSymbolSearch);
            rank = itemView.findViewById(R.id.txtRankSearch);
            searchCard = itemView.findViewById(R.id.searchLayoutCard);
        }
    }

    public void setAnimation(View viewType,int position){
        if(position>lastPostition){
            Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
            viewType.startAnimation(animation);
            lastPostition=position;
        }
    }
}
