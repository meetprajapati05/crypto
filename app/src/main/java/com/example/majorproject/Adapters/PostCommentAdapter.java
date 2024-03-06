package com.example.majorproject.Adapters;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.bumptech.glide.Glide;
import com.example.majorproject.Models.PostCommentModel;
import com.example.majorproject.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class PostCommentAdapter extends RecyclerView.Adapter<PostCommentAdapter.ViewHolder> {

    Context context;
    ArrayList<PostCommentModel> data;

    public PostCommentAdapter(Context context, ArrayList<PostCommentModel> data) {
        this.context = context;
        this.data = data;
    }

    @NonNull
    @Override
    public PostCommentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_comment, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PostCommentAdapter.ViewHolder holder, int position) {
        //set user image
        if (data.get(position).getUser_image() != null) {
            Glide.with(context)
                    .load(data.get(position).getUser_image())
                    .into(holder.imgProfile);
        } else {
            TextDrawable firstLatterImage = TextDrawable.builder()
                    .beginConfig()
                    .width(50)
                    .height(50)
                    .endConfig()
                    .buildRect(String.valueOf(data.get(position).getUser_name().toUpperCase().charAt(0)), context.getResources().getColor(R.color.light_green));
            holder.imgProfile.setImageDrawable(firstLatterImage);
        }

        //set user name
        holder.txtUserName.setText(data.get(position).getUser_name());

        //Set Comment time
        // Calculate the time difference in milliseconds
        long timeDifference = System.currentTimeMillis() - Long.parseLong(data.get(position).getComment_date_time());

        // Define the thresholds for different time units
        long minuteThreshold = DateUtils.MINUTE_IN_MILLIS;
        long hourThreshold = DateUtils.HOUR_IN_MILLIS;
        long dayThreshold = DateUtils.DAY_IN_MILLIS;
        // Format the timestamp based on the thresholds
        String formattedTime;

        if (timeDifference < minuteThreshold) {
            long secondAgo = timeDifference / DateUtils.SECOND_IN_MILLIS;
            formattedTime = String.format("%d s", secondAgo);
        } else if (timeDifference < hourThreshold) {
            // Less than an hour
            long minutesAgo = timeDifference / DateUtils.MINUTE_IN_MILLIS;
            formattedTime = String.format("%d m", minutesAgo);
        } else if (timeDifference < dayThreshold) {
            // Less than a day
            long hoursAgo = timeDifference / DateUtils.HOUR_IN_MILLIS;
            formattedTime = String.format("%d h", hoursAgo);
        }else if (timeDifference < DateUtils.DAY_IN_MILLIS) {
            // Less than a day
            long hoursAgo = timeDifference / DateUtils.HOUR_IN_MILLIS;
            formattedTime = String.format("%d d", hoursAgo);
        } else {
            // More than a day, format as "dd-MMM"
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
            formattedTime = sdf.format(new Date(Long.parseLong(data.get(position).getComment_date_time())));
        }

        // Use formattedTime as needed, for example, set it to a TextView
        holder.txtCommentTime.setText(formattedTime);

        //Set Text Comment
        holder.txtComment.setText(data.get(position).getComment());

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgProfile;
        TextView txtUserName, txtCommentTime, txtComment;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProfile = itemView.findViewById(R.id.imgCommentLayoutUser);
            txtUserName = itemView.findViewById(R.id.txtCommentLayoutUserName);
            txtCommentTime = itemView.findViewById(R.id.txtCommentLayoutTiming);
            txtComment = itemView.findViewById(R.id.txtCommentDescription);
        }
    }
}
