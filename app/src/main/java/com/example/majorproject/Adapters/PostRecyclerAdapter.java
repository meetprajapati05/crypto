package com.example.majorproject.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.majorproject.CommunityPostComment;
import com.example.majorproject.Models.PostModel;
import com.example.majorproject.R;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;
import io.realm.mongodb.mongo.result.UpdateResult;

public class PostRecyclerAdapter extends RecyclerView.Adapter<PostRecyclerAdapter.ViewHolder> {

    Context context;
    ArrayList<PostModel> data;
    App app;
    MongoClient client;
    MongoDatabase database;

    public PostRecyclerAdapter(Context context, ArrayList<PostModel> data) {
        this.context = context;
        this.data = data;

        Realm.init(context);
        app = new App(new AppConfiguration.Builder(context.getString(R.string.MONGO_APP_ID)).build());
        client = app.currentUser().getMongoClient(context.getString(R.string.MONGO_DB_SERVICE_NAME));
        database = client.getDatabase(context.getString(R.string.MONGO_DATABASE_NAME));
    }

    @NonNull
    @Override
    public PostRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_community_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostRecyclerAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        if(data.get(position).getUser_image_link()!=null){
            Glide.with(context)
                    .load(data.get(position).getUser_image_link())
                    .into(holder.userImg);
        }else{
            holder.userImg.setImageDrawable(data.get(position).getUser_image_drawable());
        }

        holder.userName.setText(data.get(position).getUser_name());

        // Calculate the time difference in milliseconds
        long timeDifference = System.currentTimeMillis() - Long.parseLong(data.get(position).getPost_date_time());

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
        } else if (timeDifference < DateUtils.DAY_IN_MILLIS) {
            // Less than a day
            long hoursAgo = timeDifference / DateUtils.HOUR_IN_MILLIS;
            formattedTime = String.format("%d d", hoursAgo);
        }        else {
            // More than a day, format as "dd-MMM"
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
            formattedTime = sdf.format(new Date(Long.parseLong(data.get(position).getPost_date_time())));
        }

        // Use formattedTime as needed, for example, set it to a TextView
        holder.postTiming.setText(formattedTime);

        holder.postDescription.setText(data.get(position).getPost_description());

        if(data.get(position).getPost_image()!=null){
            Glide.with(context)
                    .load(data.get(position).getPost_image())
                    .into(holder.postImage);
            holder.cardPostImageBack.setVisibility(View.VISIBLE);
        }else{
            holder.cardPostImageBack.setVisibility(View.GONE);
        }

        SharedPreferences preferences = context.getSharedPreferences("MajorProject",Context.MODE_PRIVATE);
        String email = preferences.getString("email",null);

        MongoCollection<Document> findUserLike = database.getCollection(context.getString(R.string.MONGO_DB_COMMUNITY_COLLECTION));
        findUserLike.find(new Document("_id", new ObjectId(data.get(position).get_id())))
                .first()
                .getAsync(new App.Callback<Document>() {
                    @Override
                    public void onResult(App.Result<Document> result) {
                        if (result.get() != null) {
                            List<Document> likes = result.get().getList("post_like", Document.class);

                            boolean userLiked = false;

                            if(likes!=null) {
                                for (Document like : likes) {
                                    if (like.getString("user_email").equals(email)) {
                                        userLiked = true;
                                        break;
                                    }
                                }

                                if(userLiked){
                                    holder.imgLike.setImageResource(R.drawable.icon_liked_community_post);
                                    holder.imgLike.setTag("like");
                                }else{
                                    holder.imgLike.setImageResource(R.drawable.icon_unlike_community_post);
                                    holder.imgLike.setTag("unlike");
                                }

                                holder.txtLikeCount.setTextColor(userLiked ? Color.RED : Color.BLACK);
                                holder.txtLikeCount.setText(String.valueOf(likes.size()));
                            }else {
                                holder.txtLikeCount.setText("0");
                            }
                        }
                    }
                });


        holder.btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MongoCollection<Document> collection = database.getCollection(context.getString(R.string.MONGO_DB_COMMUNITY_COLLECTION));
                String userId = String.valueOf(app.currentUser().getId());
                String postId = data.get(position).get_id();

                // Use a ToggleButton for like/unlike

                Document likeDocument =  new Document("user_id", userId)
                        .append("user_email", email);

                Document updateDocument = new Document("post_like", likeDocument);

                if (holder.imgLike.getTag().equals("like")) {
                    holder.imgLike.setTag("unlike");
                    holder.imgLike.setImageResource(R.drawable.icon_unlike_community_post);
                    holder.txtLikeCount.setTextColor(Color.BLACK);
                    // If already liked, remove from the post_like array
                    collection.updateOne(new Document("_id", new ObjectId(postId)), new Document("$pull", updateDocument)).getAsync(new App.Callback<UpdateResult>() {
                        @Override
                        public void onResult(App.Result<UpdateResult> result) {
                            if(!result.isSuccess()){
                                Log.e("ErrUnlikeData", result.getError().toString());
                            }
                        }
                    });
                    int likeCount = Integer.parseInt(holder.txtLikeCount.getText().toString());
                    holder.txtLikeCount.setText(String.valueOf(likeCount - 1));
                } else {
                    holder.imgLike.setTag("like");
                    holder.imgLike.setImageResource(R.drawable.icon_liked_community_post);
                    holder.txtLikeCount.setTextColor(Color.RED);
                    collection.updateOne(new Document("_id", new ObjectId(postId)), new Document("$push", updateDocument)).getAsync(new App.Callback<UpdateResult>() {
                        @Override
                        public void onResult(App.Result<UpdateResult> result) {
                            if (!result.isSuccess()) {
                                Log.e("ErrLikeData", result.getError().toString());
                            }
                        }
                    });
                    int likeCount = Integer.parseInt(holder.txtLikeCount.getText().toString());
                   holder.txtLikeCount.setText(String.valueOf(likeCount + 1));
                }

            }
        });

        holder.btnComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, CommunityPostComment.class);
                intent.putExtra("post_id", data.get(position).get_id());
                context.startActivity(intent);
            }
        });

        holder.btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message;
                if (data.get(position).getPost_image() != null) {
                    String imageUrl = data.get(position).getPost_image();
                    message = "Coin Galaxy\nPost : " + data.get(position).getPost_description() + "\nCheck post image : " + imageUrl + "." ;
                }
                else{
                    message = "Coin Galaxy\nPost : " + data.get(position).getPost_description() + ".";
                }
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");

                // Set the message text
                shareIntent.putExtra(Intent.EXTRA_TEXT, message);

                // Optionally, you can set a subject
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "CoinGalaxy Post");

                // Create a chooser to let the user choose how to share
                Intent chooserIntent = Intent.createChooser(shareIntent, "Share Via");

                if (shareIntent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(chooserIntent);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView userImg, postImage;
        TextView userName, postTiming, postDescription;
        CardView cardPostImageBack;
        RelativeLayout btnLike,btnComment, btnShare;
        ImageView imgLike;
        TextView txtLikeCount;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userImg = itemView.findViewById(R.id.imgCommunityUser);
            userName = itemView.findViewById(R.id.txtCommunityUserName);
            postTiming = itemView.findViewById(R.id.txtCommunityTiming);
            postDescription = itemView.findViewById(R.id.txtCommunityDescription);
            cardPostImageBack = itemView.findViewById(R.id.cardCommunityPostImage);
            postImage = itemView.findViewById(R.id.imgCommunityPost);
            btnLike = itemView.findViewById(R.id.btnCommunityLike);
            imgLike = itemView.findViewById(R.id.checkCommunityLike);
            txtLikeCount = itemView.findViewById(R.id.txtCommunityLikeCounting);
            btnComment = itemView.findViewById(R.id.btnCommunityComments);
            btnShare = itemView.findViewById(R.id.btnCommunityShare);
        }
    }
}
