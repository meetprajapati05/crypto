package com.example.majorproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.amulyakhare.textdrawable.TextDrawable;
import com.bumptech.glide.Glide;
import com.example.majorproject.Adapters.PostRecyclerAdapter;
import com.example.majorproject.Models.PostModel;
import com.example.majorproject.databinding.ActivityAddCommunityBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.bson.Document;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.RealmResultTask;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;
import io.realm.mongodb.mongo.iterable.MongoCursor;
import io.realm.mongodb.mongo.result.InsertOneResult;

public class AddCommunity extends AppCompatActivity {
    ActivityAddCommunityBinding binding;
    private static final int PICK_IMG = 1;
    Uri imageUri;
    App app;
    User user;
    MongoClient mongoClient;
    MongoDatabase mongoDatabase;
    String user_email;
    String user_name;
    String user_image;
    FirebaseStorage storage;
    String imgPostUploadLink;

    ArrayList<PostModel> data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddCommunityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SharedPreferences preferences = getSharedPreferences("MajorProject", MODE_PRIVATE);
        user_email = preferences.getString("email", null);

        data = new ArrayList<>();

        Realm.init(AddCommunity.this);

        app = new App(new AppConfiguration.Builder(getString(R.string.MONGO_APP_ID)).build());
        user = app.currentUser();
        assert user != null;
        mongoClient = user.getMongoClient(getString(R.string.MONGO_DB_SERVICE_NAME));
        mongoDatabase = mongoClient.getDatabase(getString(R.string.MONGO_DATABASE_NAME));

        MongoCollection<Document> collection = mongoDatabase.getCollection(getString(R.string.MONGO_DB_USER_COLLECTION));



        collection.find(new Document("user_id", user.getId()).append("email", user_email)).first().getAsync(new App.Callback<Document>() {
            @Override
            public void onResult(App.Result<Document> result) {
                if (result.isSuccess()) {
                    Document document = result.get();

                    if (document != null) {
                        user_image = document.getString("img_url");
                        user_name = document.getString("name");

                        if (user_image != null) {
                            // Load and display user image using Glide
                            Glide.with(AddCommunity.this)
                                    .load(user_image)
                                    .into(binding.imgAddCommunityUserImage);
                        } else {
                            if (user_name != null) {
                                TextDrawable firstLetterImage = TextDrawable.builder()
                                        .beginConfig()
                                        .width(60)
                                        .height(60)
                                        .endConfig()
                                        .buildRect(String.valueOf(user_name.toUpperCase().charAt(0)), getResources().getColor(R.color.light_green));
                                binding.imgAddCommunityUserImage.setImageDrawable(firstLetterImage);
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

        //Set recylcer
        setCommunityRecycler();

        //Set Refresh
        binding.refreshAddCommunity.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                setCommunityRecycler();
                binding.refreshAddCommunity.setRefreshing(false);
            }
        });
        binding.etAddCommuntiDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!charSequence.toString().isEmpty()) {
                    binding.txtTextCount.setText(String.valueOf(charSequence.length()));
                    binding.btnAddCommunityPost.setVisibility(View.VISIBLE);
                } else {
                    binding.txtTextCount.setText("0");
                    binding.btnAddCommunityPost.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.btnAddCommunityGalary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityIfNeeded(Intent.createChooser(intent, "Select Image"), PICK_IMG);
            }
        });


        binding.btnAddCommunityRemoveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.imgAddCommunityUploadFromGal.setImageBitmap(null);
                binding.cardAddCommuntiyGetImage.setVisibility(View.GONE);

                binding.btnAddCommunityGalary.setAlpha(1f);
                binding.btnAddCommunityGalary.setEnabled(true);
            }
        });

        binding.toolbarAddCommunity.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        binding.btnAddCommunityPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Set alpha on description and button and addImgButton
                binding.etAddCommuntiDescription.setAlpha(0.5f);
                binding.btnAddCommunityGalary.setAlpha(0.5f);

                //Set progress when data is uploading
                binding.btnAddCommunityPost.setVisibility(View.GONE);
                binding.progressAddCommunity.setVisibility(View.VISIBLE);

                //Set off enable widget on post is uploading
                binding.etAddCommuntiDescription.setEnabled(false);
                binding.btnAddCommunityGalary.setEnabled(false);
                if (imageUri != null) {
                    binding.imgAddCommunityUploadFromGal.setEnabled(false);
                    binding.btnAddCommunityRemoveImage.setEnabled(false);
                    binding.imgAddCommunityUploadFromGal.setAlpha(0.5f);
                    binding.btnAddCommunityRemoveImage.setAlpha(0.5f);

                    storage = FirebaseStorage.getInstance();
                    StorageReference reference = storage.getReference();

                    // Use a unique path within your Firebase Storage to store the image, for example:
                    String imagePath = "Posts/" + System.currentTimeMillis() + ".jpg";


                    reference.child(imagePath).putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            reference.child(imagePath).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    imgPostUploadLink = uri.toString();
                                    // Get the collection for "Community Posts"
                                    MongoCollection<Document> collection1 = mongoDatabase.getCollection(getString(R.string.MONGO_DB_COMMUNITY_COLLECTION));

                                    // Create a new document for the post data
                                    Document newPost = new Document("user_id", user.getId())
                                            .append("user_name", user_name)
                                            .append("user_email", user_email)
                                            .append("user_image", user_image)
                                            .append("post_image", imgPostUploadLink)
                                            .append("post_description", binding.etAddCommuntiDescription.getText().toString())
                                            .append("post_date_time", String.valueOf(System.currentTimeMillis()))
                                            .append("post_block", false);

                                    collection1.insertOne(newPost).getAsync(new App.Callback<InsertOneResult>() {
                                        @Override
                                        public void onResult(App.Result<InsertOneResult> result) {
                                            // Handle the result
                                            if (result.isSuccess()) {
                                                setCommunityRecycler();
                                                // Update successful
                                                binding.etAddCommuntiDescription.clearFocus();
                                                binding.etAddCommuntiDescription.setText("");
                                                binding.btnAddCommunityPost.setVisibility(View.GONE);
                                                imgPostUploadLink = null;
                                                imageUri = null;
                                                binding.cardAddCommuntiyGetImage.setVisibility(View.GONE);
                                                binding.progressAddCommunity.setVisibility(View.GONE);

                                                // Restore alpha values and enable UI elements
                                                binding.etAddCommuntiDescription.setAlpha(1f);
                                                binding.btnAddCommunityPost.setAlpha(1f);
                                                binding.btnAddCommunityGalary.setAlpha(1f);
                                                binding.imgAddCommunityUploadFromGal.setAlpha(1f);
                                                binding.btnAddCommunityRemoveImage.setAlpha(1f);

                                                binding.etAddCommuntiDescription.setEnabled(true);
                                                binding.btnAddCommunityPost.setEnabled(true);
                                                binding.btnAddCommunityGalary.setEnabled(true);
                                                binding.imgAddCommunityUploadFromGal.setEnabled(true);
                                                binding.btnAddCommunityGalary.setEnabled(true);
                                            } else {
                                                // Handle update failure
                                                Log.e("v", result.getError().toString());

                                                // Restore alpha values and enable UI elements in case of failure
                                                binding.etAddCommuntiDescription.setAlpha(1f);
                                                binding.btnAddCommunityPost.setAlpha(1f);
                                                binding.btnAddCommunityGalary.setAlpha(1f);
                                                binding.imgAddCommunityUploadFromGal.setAlpha(1f);
                                                binding.btnAddCommunityGalary.setAlpha(1f);
                                                binding.progressAddCommunity.setVisibility(View.GONE);

                                                binding.etAddCommuntiDescription.setEnabled(true);
                                                binding.btnAddCommunityPost.setEnabled(true);
                                                binding.btnAddCommunityGalary.setEnabled(true);
                                                binding.imgAddCommunityUploadFromGal.setEnabled(true);
                                                binding.btnAddCommunityGalary.setEnabled(true);
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    });
                }else {
                    //Set progress when data is uploading
                    binding.progressAddCommunity.setVisibility(View.VISIBLE);

                    MongoCollection<Document> collection1 = mongoDatabase.getCollection(getString(R.string.MONGO_DB_COMMUNITY_COLLECTION));
                    Document newPost = new Document("user_id", user.getId())
                            .append("user_name", user_name)
                            .append("user_email", user_email)
                            .append("user_image", user_image)
                            .append("post_image", imgPostUploadLink)
                            .append("post_description", binding.etAddCommuntiDescription.getText().toString())
                            .append("post_date_time",String.valueOf( System.currentTimeMillis()))
                            .append("post_block", false);
                    collection1.insertOne(newPost).getAsync(new App.Callback<InsertOneResult>() {
                        @Override
                        public void onResult(App.Result<InsertOneResult> result) {
                            if (result.isSuccess()) {
                                setCommunityRecycler();
                                binding.etAddCommuntiDescription.clearFocus();
                                binding.etAddCommuntiDescription.setText("");
                                binding.btnAddCommunityPost.setVisibility(View.GONE);
                                binding.progressAddCommunity.setVisibility(View.GONE);

                                binding.etAddCommuntiDescription.setAlpha(1f);
                                binding.btnAddCommunityPost.setAlpha(1f);
                                binding.btnAddCommunityGalary.setAlpha(1f);

                                binding.etAddCommuntiDescription.setEnabled(true);
                                binding.btnAddCommunityPost.setEnabled(true);
                                binding.btnAddCommunityGalary.setEnabled(true);
                            } else {
                                binding.etAddCommuntiDescription.setAlpha(1f);
                                binding.btnAddCommunityPost.setAlpha(1f);
                                binding.btnAddCommunityGalary.setAlpha(1f);

                                binding.progressAddCommunity.setVisibility(View.GONE);

                                binding.etAddCommuntiDescription.setEnabled(true);
                                binding.btnAddCommunityPost.setEnabled(true);
                                binding.btnAddCommunityGalary.setEnabled(true);
                                Log.e("ErrUploadPostOnMongo", result.getError().toString());
                            }
                        }
                    });
                    }
                }
        });
    }
    private String getDateTime() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date currentDateTime = new Date();
        return format.format(currentDateTime);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMG && resultCode == RESULT_OK) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                binding.imgAddCommunityUploadFromGal.setImageBitmap(bitmap);
                binding.cardAddCommuntiyGetImage.setVisibility(View.VISIBLE);

                binding.btnAddCommunityGalary.setAlpha(0.5f);
                binding.btnAddCommunityGalary.setEnabled(false);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }

    public void setCommunityRecycler() {
        data.clear();

        MongoCollection<Document> collection = mongoDatabase.getCollection(getString(R.string.MONGO_DB_COMMUNITY_COLLECTION));

        Document filter = new Document("post_block", false);

        RealmResultTask<MongoCursor<Document>> find = collection.find(filter).iterator();

        find.getAsync(task->{
            if(task.isSuccess()){
                MongoCursor<Document> posts = task.get();
                if(posts!=null) {
                    while (posts.hasNext()) {
                        Document document = posts.next();
                        if(document!=null) {
                            PostModel model = new PostModel();

                            model.set_id(document.getObjectId("_id").toString());
                            model.setUser_id(document.getString("user_id"));
                            model.setUser_name(document.getString("user_name"));
                            model.setUser_email(document.getString("user_email"));
                            if(document.getString("user_image")!=null) {
                                model.setUser_image_link(document.getString("user_image"));
                            }else{
                                TextDrawable firstLatterImage = TextDrawable.builder()
                                        .beginConfig()
                                        .width(50)
                                        .height(50)
                                        .endConfig()
                                        .buildRect(String.valueOf(document.getString("user_name").toUpperCase().charAt(0)),getResources().getColor(R.color.light_green));
                                model.setUser_image_drawable(firstLatterImage);
                            }

                            model.setPost_date_time(document.getString("post_date_time"));
                            model.setPost_description(document.getString("post_description"));
                            model.setPost_image(document.getString("post_image"));

                            data.add(model);
                        }else{
                            Log.e("postNull", "Posts is null");
                        }
                    }
                    for(int i=0;i<data.size();i++){
                        for (int j = 0;j<=i;j++){
                            PostModel tempModel = new PostModel();

                            if(Double.parseDouble(data.get(i).getPost_date_time()) > Double.parseDouble(data.get(j).getPost_date_time())){
                                tempModel = data.get(i);
                                data.set(i,data.get(j));
                                data.set(j,tempModel);
                            }
                        }
                    }
                    PostRecyclerAdapter adapter = new PostRecyclerAdapter(AddCommunity.this,data);
                    binding.addCommunityRecycler.setAdapter(adapter);
                }else{
                    Log.e("CollectionIsNull", "Collection is null");
                }
            }else{
                Log.e("ErrGetPostData",task.getError().toString());
            }
        });

    }
}