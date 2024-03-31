package com.example.majorproject;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.majorproject.databinding.ActivityVerifyEmailPasswordBinding;

import org.bson.Document;
import org.bson.types.ObjectId;

import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;

public class VerifyEmailPassword extends AppCompatActivity {

    ActivityVerifyEmailPasswordBinding binding;
    String userObjId;
    boolean showPass = false;
    App app;
    MongoClient mongoClient;
    MongoDatabase mongoDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVerifyEmailPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userObjId = getIntent().getStringExtra("_id");

        binding.btnLoginEye.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(showPass){
                    binding.etLoginPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    binding.btnLoginEye.setImageResource(R.drawable.eye_close);
                    showPass = true;
                }else{
                    binding.etLoginPass.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    binding.btnLoginEye.setImageResource(R.drawable.eye_open);
                    showPass = false;
                }
                binding.etLoginPass.setSelection(binding.etLoginPass.length());
            }
        });

        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        Realm.init(this);
        app = new App(new AppConfiguration.Builder(getString(R.string.MONGO_APP_ID)).build());

        mongoClient = app.currentUser().getMongoClient(getString(R.string.MONGO_DB_SERVICE_NAME));
        mongoDatabase = mongoClient.getDatabase(getString(R.string.MONGO_DATABASE_NAME));

        binding.btnEmailPasswordVerifyNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.btnEmailPasswordVerifyNext.setVisibility(View.GONE);
                binding.progressEmailPasswordVerify.setVisibility(View.VISIBLE);

                String email = binding.etEmail.getText().toString();
                String password = binding.etLoginPass.getText().toString();

                boolean isValid = checkValidation(email, password);

                if(isValid) {
                    MongoCollection<Document> collection = mongoDatabase.getCollection(getString(R.string.MONGO_DB_USER_COLLECTION));

                    collection.findOne(new Document("_id", new ObjectId(userObjId))).getAsync(new App.Callback<Document>() {
                        @Override
                        public void onResult(App.Result<Document> result) {
                            if(result.isSuccess()){
                                if(result.get()!=null){
                                    if(result.get().getString("email").equals(email) && result.get().getString("password").equals(password)) {
                                        binding.errEmailPasswordVerifyPassword.setVisibility(View.INVISIBLE);

                                        app.getEmailPassword().sendResetPasswordEmailAsync(binding.etEmail.getText().toString(), new App.Callback<Void>() {
                                            @Override
                                            public void onResult(App.Result<Void> result) {
                                                if (result.isSuccess()) {
                                                    binding.btnEmailPasswordVerifyNext.setVisibility(View.VISIBLE);
                                                    binding.progressEmailPasswordVerify.setVisibility(View.INVISIBLE);

                                                    //Remove email form SharedPreferences
                                                    SharedPreferences preferences = getSharedPreferences("EmailVerify", MODE_PRIVATE);
                                                    SharedPreferences.Editor editor = preferences.edit();
                                                    editor.putString("email", email);
                                                    editor.apply();

                                                    Toast.makeText(VerifyEmailPassword.this, "Send reset password email on this Email Account", Toast.LENGTH_SHORT).show();
                                                    onBackPressed();
                                                    finish();
                                                } else {
                                                    binding.btnEmailPasswordVerifyNext.setVisibility(View.VISIBLE);
                                                    binding.progressEmailPasswordVerify.setVisibility(View.INVISIBLE);
                                                    Log.e("ErrChangePasswordEmaillVerifySend", result.getError().toString());
                                                }
                                            }
                                        });
                                    }else{
                                        binding.btnEmailPasswordVerifyNext.setVisibility(View.VISIBLE);
                                        binding.progressEmailPasswordVerify.setVisibility(View.INVISIBLE);
                                        binding.errEmailPasswordVerifyPassword.setText("Invalid email & password!");
                                        binding.errEmailPasswordVerifyPassword.setVisibility(View.VISIBLE);
                                    }
                                }
                            }else{
                                binding.btnEmailPasswordVerifyNext.setVisibility(View.VISIBLE);
                                binding.progressEmailPasswordVerify.setVisibility(View.INVISIBLE);
                                Log.e("ErrVerifyEmailPassword",result.getError().toString());
                            }
                        }
                    });
                }
            }
        });

    }

    private boolean checkValidation(String email, String password){
        if(email.isEmpty()){
            binding.errEmailPasswordVerifyEmail.setText("Requaierd!");
            binding.errEmailPasswordVerifyEmail.setVisibility(View.VISIBLE);
            binding.btnEmailPasswordVerifyNext.setVisibility(View.VISIBLE);
            binding.progressEmailPasswordVerify.setVisibility(View.INVISIBLE);
            return false;
        }else{
            binding.errEmailPasswordVerifyEmail.setVisibility(View.INVISIBLE);
        }

        if(!email.endsWith("@gmail.com")){
            binding.errEmailPasswordVerifyEmail.setText("Invalid email formate!");
            binding.errEmailPasswordVerifyEmail.setVisibility(View.VISIBLE);
            binding.btnEmailPasswordVerifyNext.setVisibility(View.VISIBLE);
            binding.progressEmailPasswordVerify.setVisibility(View.INVISIBLE);
            return false;
        }else{
            binding.errEmailPasswordVerifyEmail.setVisibility(View.INVISIBLE);
        }

        if(password.isEmpty()){
            binding.errEmailPasswordVerifyPassword.setText("Requaierd!");
            binding.errEmailPasswordVerifyPassword.setVisibility(View.VISIBLE);
            binding.btnEmailPasswordVerifyNext.setVisibility(View.VISIBLE);
            binding.progressEmailPasswordVerify.setVisibility(View.INVISIBLE);
            return false;
        }else{
            binding.errEmailPasswordVerifyPassword.setVisibility(View.INVISIBLE);
        }
        return true;
    }

}