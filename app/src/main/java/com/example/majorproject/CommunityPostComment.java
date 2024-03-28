package com.example.majorproject;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.amulyakhare.textdrawable.TextDrawable;
import com.bumptech.glide.Glide;
import com.example.majorproject.Adapters.PostCommentAdapter;
import com.example.majorproject.Models.PostCommentModel;
import com.example.majorproject.Models.PostModel;
import com.example.majorproject.databinding.ActivityCommunityPostCommentBinding;

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
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;
import io.realm.mongodb.mongo.result.UpdateResult;

public class CommunityPostComment extends AppCompatActivity {

    String post_id;
    String user_email;
    String user_name;
    String user_image;
    App app;
    User user;
    MongoClient mongoClient;
    MongoDatabase mongoDatabase;
    PostModel model;
    ArrayList<PostCommentModel> data;

    ActivityCommunityPostCommentBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCommunityPostCommentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        post_id = getIntent().getStringExtra("post_id");

        model = new PostModel();

        SharedPreferences preferences = getSharedPreferences("MajorProject", MODE_PRIVATE);
        user_email = preferences.getString("email", null);

        Realm.init(CommunityPostComment.this);

        app = new App(new AppConfiguration.Builder(getString(R.string.MONGO_APP_ID)).build());

        user = app.currentUser();

        mongoClient = user.getMongoClient(getString(R.string.MONGO_DB_SERVICE_NAME));
        mongoDatabase = mongoClient.getDatabase(getString(R.string.MONGO_DATABASE_NAME));

        MongoCollection<Document> collection = mongoDatabase.getCollection(getString(R.string.MONGO_DB_COMMUNITY_COLLECTION));

        collection.find(new Document("_id", new ObjectId(post_id.toString()))).first().getAsync(new App.Callback<Document>() {
            @Override
            public void onResult(App.Result<Document> result) {
                if (result.get() != null) {
                    model.setPost_image(result.get().getString("post_id"));
                    model.setPost_description(result.get().getString("post_description"));
                    model.setPost_date_time(result.get().getString("post_date_time"));
                    model.setPost_block(result.get().getBoolean("post_block"));
                    model.setPost_image(result.get().getString("post_image"));

                    if (result.get().getString("user_image") != null) {
                        model.setUser_image_link(result.get().getString("user_image"));
                    } else {
                        TextDrawable firstLatterImage = TextDrawable.builder()
                                .beginConfig()
                                .width(50)
                                .height(50)
                                .endConfig()
                                .buildRect(String.valueOf(result.get().getString("user_name").toUpperCase().charAt(0)), getResources().getColor(R.color.light_green));
                        model.setUser_image_drawable(firstLatterImage);
                    }

                    model.setUser_email(result.get().getString("user_email"));
                    model.setUser_name(result.get().getString("user_name"));
                    model.setUser_id(result.get().getString("user_id"));

                    if(model.getUser_image_link()!=null){
                        Glide.with(CommunityPostComment.this)
                                .load(model.getUser_image_link())
                                .into(binding.imgCommentUser);
                    }else{
                        binding.imgCommentUser.setImageDrawable(model.getUser_image_drawable());
                    }

                    binding.txtCommentUserName.setText(model.getUser_name());

                    // Calculate the time difference in milliseconds
                    long timeDifference = System.currentTimeMillis() - Long.parseLong(model.getPost_date_time());

                    // Define the thresholds for different time units
                    long minuteThreshold = DateUtils.MINUTE_IN_MILLIS;
                    long hourThreshold = DateUtils.HOUR_IN_MILLIS;
                    long dayThreshold = DateUtils.DAY_IN_MILLIS;
                    // Format the timestamp based on the thresholds
                    String formattedTime;

                    if (timeDifference < minuteThreshold) {
                        long secondAgo = timeDifference / DateUtils.SECOND_IN_MILLIS;
                        formattedTime = String.format("%ds", secondAgo);
                    } else if (timeDifference < hourThreshold) {
                        // Less than an hour
                        long minutesAgo = timeDifference / DateUtils.MINUTE_IN_MILLIS;
                        formattedTime = String.format("%dm", minutesAgo);
                    } else if (timeDifference < dayThreshold) {
                        // Less than a day
                        long hoursAgo = timeDifference / DateUtils.HOUR_IN_MILLIS;
                        formattedTime = String.format("%dh", hoursAgo);
                    }else if (timeDifference < DateUtils.DAY_IN_MILLIS) {
                        // Less than a day
                        long hoursAgo = timeDifference / DateUtils.HOUR_IN_MILLIS;
                        formattedTime = String.format("%dd", hoursAgo);
                    } else {
                        // More than a day, format as "dd-MMM"
                        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
                        formattedTime = sdf.format(new Date(Long.parseLong(model.getPost_date_time())));
                    }

                    // Use formattedTime as needed, for example, set it to a TextView
                    binding.txtCommentTiming.setText(formattedTime);

                    binding.txtCommentDescription.setText(model.getPost_description());

                    if(model.getPost_image()!=null){
                        Glide.with(CommunityPostComment.this)
                                .load(model.getPost_image())
                                .into(binding.imgCommentPost);
                        binding.imgCard.setVisibility(View.VISIBLE);
                    }else{
                        binding.imgCard.setVisibility(View.GONE);
                    }
                }
            }

        });

        data = new ArrayList<>();
        //Set Comment
        setComment();

        //refresh comments
        binding.refreshCommunityComment.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                setComment();
                binding.refreshCommunityComment.setRefreshing(false);
            }
        });

        //set edittext enable of typing
        binding.etAddCommuntiDescription.requestFocus();

        //Set User image of communti
        MongoCollection<Document> collection1 = mongoDatabase.getCollection(getString(R.string.MONGO_DB_USER_COLLECTION));



        collection1.find(new Document("user_id", user.getId()).append("email", user_email)).first().getAsync(new App.Callback<Document>() {
            @Override
            public void onResult(App.Result<Document> result) {
                if (result.isSuccess()) {
                    Document document = result.get();

                    if (document != null) {
                        user_image = document.getString("img_url");
                        user_name = document.getString("name");

                        if (user_image != null) {
                            // Load and display user image using Glide
                            Glide.with(CommunityPostComment.this)
                                    .load(user_image)
                                    .into(binding.imgAddCommentUserImage);
                        } else {
                            if (user_name != null) {
                                TextDrawable firstLetterImage = TextDrawable.builder()
                                        .beginConfig()
                                        .width(60)
                                        .height(60)
                                        .endConfig()
                                        .buildRect(String.valueOf(user_name.toUpperCase().charAt(0)), getResources().getColor(R.color.light_green));
                                binding.imgAddCommentUserImage.setImageDrawable(firstLetterImage);
                            }
                        }
                    } else {
                        Log.e("UserDataNull", "Document is null.");
                        // Handle the case where the document is null
                        // For example, show a default image or handle accordingly
                    }
                } else {
                    Exception error = result.getError();
                    if (error != null) {
                        Log.e("ErrGetUserImage", error.toString());
                    }
                }
            }
        });

        binding.etAddCommuntiDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.toString().isEmpty()){
                    binding.btnCommentReplay.setVisibility(View.VISIBLE);
                }else{
                    binding.btnCommentReplay.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.toolbarAddCommunity.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        binding.btnCommentReplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.btnCommentReplay.setVisibility(View.GONE);
                binding.progressAddComment.setVisibility(View.VISIBLE);
                MongoCollection<Document> replayCommentCollection = mongoDatabase.getCollection(getString(R.string.MONGO_DB_COMMUNITY_COLLECTION));

                Document filter = new Document("_id", new ObjectId(post_id));
                Document updateData = new Document("$push", new Document(
                        new Document("post_comment",
                                new Document("user_id", user.getId())
                                        .append("user_name", user_name)
                                        .append("user_email", user_email)
                                        .append("user_image", user_image)
                                        .append("comment_description", binding.etAddCommuntiDescription.getText().toString())
                                        .append("comment_date_time", String.valueOf(System.currentTimeMillis()))
                )));

                replayCommentCollection.updateOne(filter,updateData).getAsync(new App.Callback<UpdateResult>() {
                    @Override
                    public void onResult(App.Result<UpdateResult> result) {
                        if(result.isSuccess()){
                            setComment();
                            binding.etAddCommuntiDescription.setText("");
                            binding.etAddCommuntiDescription.clearFocus();
                            binding.btnCommentReplay.setVisibility(View.GONE);
                            binding.progressAddComment.setVisibility(View.GONE);
                        }else{
                            binding.btnCommentReplay.setVisibility(View.VISIBLE);
                            binding.progressAddComment.setVisibility(View.GONE);
                            Log.e("ErrPostComment", result.getError().toString());
                        }
                    }
                });
            }
        });

    }

    public void setComment(){
        data.clear();
        MongoCollection<Document> mongoCollection = mongoDatabase.getCollection(getString(R.string.MONGO_DB_COMMUNITY_COLLECTION));

        Document filter = new Document("_id", new ObjectId(post_id));

        mongoCollection.find(filter).first().getAsync(new App.Callback<Document>() {
            @Override
            public void onResult(App.Result<Document> result) {
                if(result.get()!=null){
                    List<Document> comments = result.get().getList("post_comment", Document.class);
                    if(comments != null) {
                        for (Document post : comments) {
                            PostCommentModel commentModel = new PostCommentModel();
                            commentModel.setUser_id(post.getString("user_id"));
                            commentModel.setUser_name(post.getString("user_name"));
                            commentModel.setUser_email(post.getString("user_email"));
                            commentModel.setUser_image(post.getString("user_image"));
                            commentModel.setComment(post.getString("comment_description"));
                            commentModel.setComment_date_time(post.getString("comment_date_time"));

                            data.add(commentModel);
                        }
                        for(int i=0;i<data.size();i++){
                            for (int j = 0;j<=i;j++){
                                PostCommentModel tempModel = new PostCommentModel();

                                if(Double.parseDouble(data.get(i).getComment_date_time()) > Double.parseDouble(data.get(j).getComment_date_time())){
                                    tempModel = data.get(i);
                                    data.set(i,data.get(j));
                                    data.set(j,tempModel);
                                }
                            }
                        }
                        binding.msgNoCommentYet.setVisibility(View.GONE);
                        binding.msgStartConversion.setVisibility(View.GONE);

                        LinearLayoutManager manager = new LinearLayoutManager(CommunityPostComment.this){
                            @Override
                            public boolean canScrollVertically() {
                                return false;
                            }
                        };
                        binding.commentRecycler.setLayoutManager(manager);
                        PostCommentAdapter adapter = new PostCommentAdapter(CommunityPostComment.this, data);
                        binding.commentRecycler.setAdapter(adapter);
                    }else{
                        binding.msgNoCommentYet.setVisibility(View.VISIBLE);
                        binding.msgStartConversion.setVisibility(View.VISIBLE);
                    }
                }else{
                    Log.e("ErrPostFind", "This " + post_id + "is not find");
                }
            }
        });
    }
}