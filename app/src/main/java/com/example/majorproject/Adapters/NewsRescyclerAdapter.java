package com.example.majorproject.Adapters;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.majorproject.Models.NewsModel;
import com.example.majorproject.NewsDetails;
import com.example.majorproject.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class NewsRescyclerAdapter extends RecyclerView.Adapter<NewsRescyclerAdapter.ViewHolder> {
    Context context;
    ArrayList<NewsModel> data = new ArrayList<>();

    public NewsRescyclerAdapter(Context context, ArrayList<NewsModel> data) {
        this.context = context;
        this.data = data;
    }

    @NonNull
    @Override
    public NewsRescyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_news, parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull NewsRescyclerAdapter.ViewHolder holder, int position) {

        Glide.with(context)
                .load(data.get(position).getImageurl())
                .into(holder.imgNews);

        holder.txtNewsHeading.setText(data.get(position).getTitle());

        holder.txtNewsTag.setText(data.get(position).getCategories());

        //Set News publishing
       Long timeStemp = Long.valueOf(data.get(position).getPublish_on()) * 1000L;

       Date date = new Date(timeStemp);
       SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy h:mm a", Locale.getDefault());

        // Use formattedTime as needed, for example, set it to a TextView
        holder.txtNewsTiming.setText(format.format(date));

        holder.btnNewsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, NewsDetails.class);
                intent.putExtra("url", data.get(position).getUrl());
                intent.putExtra("source_name", data.get(position).getSource_name());
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgNews;
        TextView txtNewsHeading, txtNewsTag,txtNewsTiming;
        RelativeLayout btnNewsLayout;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgNews = itemView.findViewById(R.id.imgNews);
            txtNewsHeading = itemView.findViewById(R.id.txtNewsHeading);
            txtNewsTag = itemView.findViewById(R.id.txtNewsTags);
            txtNewsTiming = itemView.findViewById(R.id.txtNewsPublish);
            btnNewsLayout = itemView.findViewById(R.id.layoutNews);
        }
    }
}
