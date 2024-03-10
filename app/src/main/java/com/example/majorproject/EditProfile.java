package com.example.majorproject;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.amulyakhare.textdrawable.TextDrawable;
import com.bumptech.glide.Glide;
import com.example.majorproject.databinding.ActivityEditProfileBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.ByteArrayOutputStream;

import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;
import io.realm.mongodb.mongo.result.UpdateResult;

public class EditProfile extends AppCompatActivity {

    private static final int CAMERA_REQUEST_ID = 1000;
    private static final int GALARY_REQUEST_ID = 1001;

    boolean getOtp = false;

    ActivityEditProfileBinding binding;
    String userObjId;
    App app;
    MongoClient client;
    MongoDatabase database;
    String user_name, user_email, user_phone, user_img;
    Bitmap imageBitmap;
    Uri imageUri;
    boolean isGoogleAuth = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userObjId = getIntent().getStringExtra("_id");

        Realm.init(this);
        app = new App(new AppConfiguration.Builder(getString(R.string.MONGO_APP_ID)).build());
        client = app.currentUser().getMongoClient(getString(R.string.MONGO_DB_SERVICE_NAME));
        database = client.getDatabase(getString(R.string.MONGO_DATABASE_NAME));

        setProfileDetail();

        binding.toolbarEditProfile.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        binding.btnEditProfileUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.btnEditProfileUpdate.setVisibility(View.GONE);
                binding.progressUpdateProfileButton.setVisibility(View.VISIBLE);

                        if(imageUri!=null){
                            StorageReference reference = FirebaseStorage.getInstance().getReference();
                            String imgPath = "Profiles/" + user_name +"/" + System.currentTimeMillis() + ".jpg";

                            reference.child(imgPath).putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    reference.child(imgPath).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            if(!isGoogleAuth) {
                                                MongoCollection<Document> collection = database.getCollection(getString(R.string.MONGO_DB_USER_COLLECTION));
                                                Document filter = new Document("_id", new ObjectId(userObjId));
                                                Document data = new Document("$set", new Document("name", binding.etEditProfileName.getText().toString())
                                                        .append("email", binding.etEditProfileEmail.getText().toString())
                                                        .append("img_url", uri.toString()));
                                                collection.updateOne(filter, data).getAsync(new App.Callback<UpdateResult>() {
                                                    @Override
                                                    public void onResult(App.Result<UpdateResult> result) {
                                                        if (result.isSuccess()) {
                                                            binding.btnEditProfileUpdate.setVisibility(View.VISIBLE);
                                                            binding.progressUpdateProfileButton.setVisibility(View.GONE);
                                                            Intent intent = new Intent(EditProfile.this, HomePage.class);
                                                            intent.putExtra("priviousActivity", "EditProfile");
                                                            intent.putExtra("_id", userObjId);
                                                            intent.putExtra("email", binding.etEditProfileEmail.getText().toString());
                                                            startActivity(intent);
                                                            finish();
                                                        } else {
                                                            binding.btnEditProfileUpdate.setVisibility(View.VISIBLE);
                                                            binding.progressUpdateProfileButton.setVisibility(View.GONE);
                                                            Log.e("ErrLinkImageSetData", result.getError().toString());
                                                        }
                                                    }
                                                });
                                            }else{
                                                MongoCollection<Document> collection = database.getCollection(getString(R.string.MONGO_DB_USER_COLLECTION));
                                                Document filter = new Document("_id", new ObjectId(userObjId));
                                                Document data = new Document("$set", new Document("name", binding.etEditProfileName.getText().toString())
                                                        .append("img_url", uri.toString()));
                                                collection.updateOne(filter, data).getAsync(new App.Callback<UpdateResult>() {
                                                    @Override
                                                    public void onResult(App.Result<UpdateResult> result) {
                                                        if (result.isSuccess()) {
                                                            binding.btnEditProfileUpdate.setVisibility(View.VISIBLE);
                                                            binding.progressUpdateProfileButton.setVisibility(View.GONE);
                                                            Intent intent = new Intent(EditProfile.this, HomePage.class);
                                                            intent.putExtra("isGoogleAuth", true);
                                                            startActivity(intent);
                                                            finish();
                                                        } else {
                                                            binding.btnEditProfileUpdate.setVisibility(View.VISIBLE);
                                                            binding.progressUpdateProfileButton.setVisibility(View.GONE);
                                                            Log.e("ErrGoogleAuthLinkImageSetData", result.getError().toString());
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    binding.btnEditProfileUpdate.setVisibility(View.VISIBLE);
                                    binding.progressUpdateProfileButton.setVisibility(View.GONE);
                                    Log.e("ErrProfilePicUriUpload",e.getMessage().toString());
                                }
                            });
                        } else if (imageBitmap!=null) {
                            StorageReference reference = FirebaseStorage.getInstance().getReference();
                            String imgPath = "Profiles/" + user_name +"/" + System.currentTimeMillis() + ".jpg";

                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                            byte[] thumbnailData = baos.toByteArray();

                            reference.child(imgPath).putBytes(thumbnailData).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    reference.child(imgPath).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            if(!isGoogleAuth) {
                                                MongoCollection<Document> collection = database.getCollection(getString(R.string.MONGO_DB_USER_COLLECTION));
                                                Document filter = new Document("_id", new ObjectId(userObjId));
                                                Document data = new Document("$set", new Document("name", binding.etEditProfileName.getText().toString())
                                                        .append("email", binding.etEditProfileEmail.getText().toString())
                                                        .append("img_url", uri.toString()));
                                                collection.updateOne(filter, data).getAsync(new App.Callback<UpdateResult>() {
                                                    @Override
                                                    public void onResult(App.Result<UpdateResult> result) {
                                                        if (result.isSuccess()) {
                                                            binding.btnEditProfileUpdate.setVisibility(View.VISIBLE);
                                                            binding.progressUpdateProfileButton.setVisibility(View.GONE);
                                                            Intent intent = new Intent(EditProfile.this, HomePage.class);
                                                            intent.putExtra("priviousActivity", "EditProfile");
                                                            intent.putExtra("_id", userObjId);
                                                            intent.putExtra("email", binding.etEditProfileEmail.getText().toString());
                                                            startActivity(intent);
                                                            finish();
                                                        } else {
                                                            binding.btnEditProfileUpdate.setVisibility(View.VISIBLE);
                                                            binding.progressUpdateProfileButton.setVisibility(View.GONE);
                                                            Log.e("ErrBitmapImageSetData", result.getError().toString());
                                                        }
                                                    }
                                                });
                                            }else {
                                                MongoCollection<Document> collection = database.getCollection(getString(R.string.MONGO_DB_USER_COLLECTION));
                                                Document filter = new Document("_id", new ObjectId(userObjId));
                                                Document data = new Document("$set", new Document("name", binding.etEditProfileName.getText().toString())
                                                        .append("img_url", uri.toString()));
                                                collection.updateOne(filter, data).getAsync(new App.Callback<UpdateResult>() {
                                                    @Override
                                                    public void onResult(App.Result<UpdateResult> result) {
                                                        if (result.isSuccess()) {
                                                            binding.btnEditProfileUpdate.setVisibility(View.VISIBLE);
                                                            binding.progressUpdateProfileButton.setVisibility(View.GONE);
                                                            Intent intent = new Intent(EditProfile.this, HomePage.class);
                                                            intent.putExtra("isGoogleAuth", true);
                                                            startActivity(intent);
                                                            finish();
                                                        } else {
                                                            binding.btnEditProfileUpdate.setVisibility(View.VISIBLE);
                                                            binding.progressUpdateProfileButton.setVisibility(View.GONE);
                                                            Log.e("ErrGoogleAuthLinkImageSetData", result.getError().toString());
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    binding.btnEditProfileUpdate.setVisibility(View.VISIBLE);
                                    binding.progressUpdateProfileButton.setVisibility(View.GONE);
                                    Log.e("ErrProfilePicUriUpload",e.getMessage().toString());
                                }
                            });
                        }else {
                            if (!isGoogleAuth) {
                                MongoCollection<Document> collection = database.getCollection(getString(R.string.MONGO_DB_USER_COLLECTION));
                                Document filter = new Document("_id", new ObjectId(userObjId));
                                Document data = new Document("$set", new Document("name", binding.etEditProfileName.getText().toString())
                                        .append("email", binding.etEditProfileEmail.getText().toString())
                                        .append("img_url", null));
                                collection.updateOne(filter, data).getAsync(new App.Callback<UpdateResult>() {
                                    @Override
                                    public void onResult(App.Result<UpdateResult> result) {
                                        if (result.isSuccess()) {
                                            binding.btnEditProfileUpdate.setVisibility(View.VISIBLE);
                                            binding.progressUpdateProfileButton.setVisibility(View.GONE);
                                            Intent intent = new Intent(EditProfile.this, HomePage.class);
                                            intent.putExtra("priviousActivity", "EditProfile");
                                            intent.putExtra("_id", userObjId);
                                            intent.putExtra("email", binding.etEditProfileEmail.getText().toString());
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            binding.btnEditProfileUpdate.setVisibility(View.VISIBLE);
                                            binding.progressUpdateProfileButton.setVisibility(View.GONE);
                                            Log.e("ErrNullImageSetData", result.getError().toString());
                                        }
                                    }
                                });
                            }else{
                                MongoCollection<Document> collection = database.getCollection(getString(R.string.MONGO_DB_USER_COLLECTION));
                                Document filter = new Document("_id", new ObjectId(userObjId));
                                Document data = new Document("$set", new Document("name", binding.etEditProfileName.getText().toString())
                                        .append("img_url", null));
                                collection.updateOne(filter, data).getAsync(new App.Callback<UpdateResult>() {
                                    @Override
                                    public void onResult(App.Result<UpdateResult> result) {
                                        if (result.isSuccess()) {
                                            binding.btnEditProfileUpdate.setVisibility(View.VISIBLE);
                                            binding.progressUpdateProfileButton.setVisibility(View.GONE);
                                            Intent intent = new Intent(EditProfile.this, HomePage.class);
                                            intent.putExtra("isGoogleAuth", true);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            binding.btnEditProfileUpdate.setVisibility(View.VISIBLE);
                                            binding.progressUpdateProfileButton.setVisibility(View.GONE);
                                            Log.e("ErrGoogleAuthLinkImageSetData", result.getError().toString());
                                        }
                                    }
                                });
                            }

                        }

            }
        });

        binding.btnEditProfileEditImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(EditProfile.this);
                View v = LayoutInflater.from(EditProfile.this).inflate(R.layout.layout_bottomsheet_edit_profile_image, null);
                bottomSheetDialog.setContentView(v);
                bottomSheetDialog.show();

                @SuppressLint({"MissingInflatedId", "LocalSuppress"})
                ImageView btnRemoveImage = v.findViewById(R.id.btnRemoveImage);
                @SuppressLint({"MissingInflatedId", "LocalSuppress"})
                LinearLayout btnAddImageCam = v.findViewById(R.id.btnAddImageFromCam);
                @SuppressLint({"MissingInflatedId", "LocalSuppress"})
                LinearLayout btnAddImageGal = v.findViewById(R.id.btnAddImageFromGal);

                btnRemoveImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        TextDrawable firstLetterImage = TextDrawable.builder()
                                .beginConfig()
                                .width(120)
                                .height(120)
                                .endConfig()
                                .buildRect(String.valueOf(user_name.toUpperCase().charAt(0)), getResources().getColor(R.color.light_green));

                        binding.imgEditProfile.setImageDrawable(firstLetterImage);
                        imageBitmap=null;
                        imageUri = null;
                        bottomSheetDialog.dismiss();
                    }
                });

                btnAddImageCam.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (ContextCompat.checkSelfPermission(EditProfile.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(EditProfile.this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_ID);
                        } else {
                            Intent iCam = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityIfNeeded(iCam, CAMERA_REQUEST_ID);
                            bottomSheetDialog.dismiss();
                        }
                    }
                });

                btnAddImageGal.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);

                        startActivityIfNeeded(Intent.createChooser(intent, "Select Image : "), GALARY_REQUEST_ID);
                        bottomSheetDialog.dismiss();
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == CAMERA_REQUEST_ID) {
                // Permission already granted, you can proceed with camera access.
                imageBitmap = (Bitmap) data.getExtras().get("data");
                if (imageBitmap != null) {
                    binding.imgEditProfile.setImageBitmap(imageBitmap);
                }
                imageUri=null;
            }else if (requestCode == GALARY_REQUEST_ID) {
                // Handle gallery result
                imageUri = data.getData();
                if (imageUri != null) {
                    binding.imgEditProfile.setImageURI(imageUri);
                }
                imageBitmap=null;
            }
        }
    }

    private void setProfileDetail() {
        MongoCollection<Document> collection = database.getCollection(getString(R.string.MONGO_DB_USER_COLLECTION));

        collection.findOne(new Document("_id", new ObjectId(userObjId))).getAsync(new App.Callback<Document>() {
            @Override
            public void onResult(App.Result<Document> result) {
                if (result.isSuccess()) {
                    if (result.get() != null) {
                        user_img = result.get().getString("img_url");
                        user_name = result.get().getString("name");
                        user_email = result.get().getString("email");
                        user_phone = result.get().getString("phone_no");

                        if (user_img != null) {
                            Glide.with(EditProfile.this)
                                    .load(user_img)
                                    .into(binding.imgEditProfile);
                        } else {
                            TextDrawable firstLetterImage = TextDrawable.builder()
                                    .beginConfig()
                                    .width(120)
                                    .height(120)
                                    .endConfig()
                                    .buildRect(String.valueOf(user_name.toUpperCase().charAt(0)), getResources().getColor(R.color.light_green));

                            binding.imgEditProfile.setImageDrawable(firstLetterImage);
                        }

                        binding.txtEditProfileName.setText(user_name);
                        binding.etEditProfileName.setText(user_name);
                        binding.etEditProfileEmail.setText(user_email);
                        if(result.get().getString("provider").equals("GOOGLE")){
                            binding.etEditProfileEmail.setEnabled(false);
                            Toast.makeText(EditProfile.this, "Your login is by using Google then you can't change Email.", Toast.LENGTH_SHORT).show();
                            isGoogleAuth = true;
                        }else{
                            binding.etEditProfileEmail.setEnabled(true);
                            isGoogleAuth = false;
                        }
                    }
                } else {
                    Log.e("ErrEditProfileDb", result.getError().toString());
                }
            }
        });
    }

    public boolean isValidate(String name, String email, String phone) {
        int err = 0;
        if (name.isEmpty()) {
            binding.etEditProfileName.setError("Requierd");
            binding.etEditProfileName.setFocusable(true);
            err = err + 1;
            return false;
        } else {
            binding.etEditProfileName.setError("");
            binding.etEditProfileName.setFocusable(false);
        }
        if (name.matches("^[a-zA-Z]*$")) {
            binding.etEditProfileName.setError("Name should be alphabate only");
            binding.etEditProfileName.setFocusable(true);
            err = err + 1;
            return false;
        } else {
            binding.etEditProfileName.setError("");
            binding.etEditProfileName.setFocusable(false);
        }
        if (email.isEmpty()) {
            binding.etEditProfileEmail.setError("Requierd");
            binding.etEditProfileEmail.setFocusable(true);
            err = err + 1;
            return false;
        } else {
            binding.etEditProfileEmail.setError("");
            binding.etEditProfileEmail.setFocusable(false);
        }
        if (email.endsWith("@gmail.com")) {
            binding.etEditProfileEmail.setError("Invalid email formate!");
            binding.etEditProfileEmail.setFocusable(true);
            err = err + 1;
            return false;
        } else {
            binding.etEditProfileEmail.setError("");
            binding.etEditProfileEmail.setFocusable(false);
        }
        return true;
    }
}