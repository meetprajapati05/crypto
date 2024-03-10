package com.example.majorproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.majorproject.databinding.ActivityPasswordResetSiteBinding;

import org.bson.Document;

import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;
import io.realm.mongodb.mongo.result.UpdateResult;

public class PasswordResetSite extends AppCompatActivity {

    ActivityPasswordResetSiteBinding binding;

    boolean showPass = false;

    boolean showCPass = false;

    App app;
    Intent appLinkIntent;
    String appLinkAction;
    Uri data;
    String token,tokenId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPasswordResetSiteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Set Password & Confirm Password Reset Eye Button
        binding.btnResetPassEye.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(showPass){
                    binding.etResetPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    binding.btnResetPassEye.setImageResource(R.drawable.eye_close);
                    showPass = true;
                }else{
                    binding.etResetPass.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    binding.btnResetPassEye.setImageResource(R.drawable.eye_open);
                    showPass = false;
                }
                binding.etResetPass.setSelection(binding.etResetPass.length());
            }
        });

        binding.btnResetCpassEye.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(showPass){
                    binding.etResetCpass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    binding.btnResetCpassEye.setImageResource(R.drawable.eye_close);
                    showPass = true;
                }else{
                    binding.etResetCpass.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    binding.btnResetCpassEye.setImageResource(R.drawable.eye_open);
                    showPass = false;
                }
                binding.etResetCpass.setSelection(binding.etResetCpass.length());
            }
        });

        //Get Token Passing with using Intent
        // ATTENTION: This was auto-generated to handle app links.
        appLinkIntent = getIntent();
        appLinkAction = appLinkIntent.getAction();
        data = appLinkIntent.getData();

        if(data!=null){
            token = data.getQueryParameter("token");
            tokenId = data.getQueryParameter("tokenId");
        }

        binding.btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(data!=null){
                    binding.btnResetPassword.setVisibility(View.INVISIBLE);
                    binding.progressPasswordResetSite.setVisibility(View.VISIBLE);
                    boolean isValid = checkPasswordValidation(binding.etResetPass.getText().toString(), binding.etResetCpass.getText().toString());

                    if(isValid){
                        app = new App(new AppConfiguration.Builder(getString(R.string.MONGO_APP_ID)).build());

                        app.getEmailPassword().resetPasswordAsync(token, tokenId, binding.etResetPass.getText().toString(), new App.Callback<Void>() {
                            @Override
                            public void onResult(App.Result<Void> result) {
                                if(result.isSuccess()){

                                    //get Email for the SharedPreferences
                                    SharedPreferences getEmail = getSharedPreferences("EmailVerify", MODE_PRIVATE);
                                    String email = getEmail.getString("email",null);

                                    Credentials credentials = Credentials.emailPassword("emailaddresscheck1@gmail.com","EmailAddressVerify");

                                    app.loginAsync(credentials, new App.Callback<User>() {
                                        @Override
                                        public void onResult(App.Result<User> result) {
                                            if(result.isSuccess()){
                                                MongoClient mongoClient = app.currentUser().getMongoClient(getString(R.string.MONGO_DB_SERVICE_NAME));
                                                MongoDatabase mongoDatabase = mongoClient.getDatabase(getString(R.string.MONGO_DATABASE_NAME));
                                                MongoCollection<Document> collection = mongoDatabase.getCollection(getString(R.string.MONGO_DB_USER_COLLECTION));

                                                if(email!=null){
                                                    Document  filterQuery = new Document().append("email", email);
                                                    collection.findOne(filterQuery).getAsync(new App.Callback<Document>() {
                                                        @Override
                                                        public void onResult(App.Result<Document> result) {
                                                            if(result.isSuccess()) {
                                                                if (result.get() != null) {
                                                                    Document filter = new Document("email",email).append("provider","EMAIL_PASSWORD");
                                                                    Document update = new Document("$set", new Document("password", binding.etResetPass.getText().toString()));
                                                                    
                                                                    collection.updateOne(filter, update).getAsync(new App.Callback<UpdateResult>() {
                                                                        @Override
                                                                        public void onResult(App.Result<UpdateResult> result) {
                                                                            if(result.isSuccess()){
                                                                                binding.btnResetPassword.setVisibility(View.VISIBLE);
                                                                                binding.progressPasswordResetSite.setVisibility(View.INVISIBLE);

                                                                                new LogoutTask().execute();

                                                                                //Remove email form SharedPreferences
                                                                                SharedPreferences preferences = getSharedPreferences("EmailVerify", MODE_PRIVATE);
                                                                                SharedPreferences.Editor editor = preferences.edit();
                                                                                editor.putString("email", null);
                                                                                editor.apply();

                                                                                app.loginAsync(Credentials.emailPassword(email, binding.etResetCpass.getText().toString()), new App.Callback<User>() {
                                                                                    @Override
                                                                                    public void onResult(App.Result<User> result) {
                                                                                        if(result.isSuccess()){
                                                                                            Toast.makeText(PasswordResetSite.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                                                                                            finishAffinity();
                                                                                        }
                                                                                    }
                                                                                });
                                                                            }else{
                                                                                binding.btnResetPassword.setVisibility(View.VISIBLE);
                                                                                binding.progressPasswordResetSite.setVisibility(View.INVISIBLE);
                                                                                Log.e("ErrUpdatePassword",  result.getError().toString());
                                                                            }
                                                                        }
                                                                    });

                                                                } else {
                                                                    binding.btnResetPassword.setVisibility(View.VISIBLE);
                                                                    binding.progressPasswordResetSite.setVisibility(View.INVISIBLE);
                                                                    Log.e("ErrFindEmailData",  email + " Data is missing on database");
                                                                }
                                                            }else{
                                                                binding.btnResetPassword.setVisibility(View.VISIBLE);
                                                                binding.progressPasswordResetSite.setVisibility(View.INVISIBLE);
                                                                Log.e("ErrFindEmailOnPasswordResetSite",result.getError().toString());
                                                            }
                                                        }
                                                    });
                                                }

                                            }else{
                                                binding.btnResetPassword.setVisibility(View.VISIBLE);
                                                binding.progressPasswordResetSite.setVisibility(View.INVISIBLE);
                                                Log.e("ErrCheckEmailUserLoginInPassReset", result.getError().toString());
                                            }
                                        }
                                    });

                                }else{
                                    binding.btnResetPassword.setVisibility(View.VISIBLE);
                                    binding.progressPasswordResetSite.setVisibility(View.INVISIBLE);
                                    Log.e("ResetPassword", result.getError().toString());
                                    Toast.makeText(PasswordResetSite.this, "Token expired! Send Email again", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                    else {
                        binding.btnResetPassword.setVisibility(View.VISIBLE);
                        binding.progressPasswordResetSite.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });
    }
    private boolean checkPasswordValidation(String pass, String cpass) {
        if(pass.isEmpty()){
            binding.errResetPass.setText("Please enter password");
            binding.errResetPass.setVisibility(View.VISIBLE);
            return false;
        }
        else if(pass.length() < 8){
            binding.errResetPass.setText("Please! enter minimum 8 character password");
            binding.errResetPass.setVisibility(View.VISIBLE);
            return false;
        }
        else{
            binding.errResetPass.setVisibility(View.INVISIBLE);
        }

        if(!cpass.equals(pass)){
            binding.errResetCPass.setText("Password & Confirm Password are not match");
            binding.errResetCPass.setVisibility(View.VISIBLE);
            return false;
        }else{
            binding.errResetCPass.setVisibility(View.INVISIBLE);
        }
        return true;
    }

    private class LogoutTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                // Move your logout logic here
                app.currentUser().logOut();
                return true; // Indicates success
            } catch (Exception e) {
                e.printStackTrace();
                return false; // Indicates failure
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            // This method is called on the UI thread after doInBackground finishes
            if (success) {
                // Handle UI updates or post-logout actions here
                Toast.makeText(PasswordResetSite.this, "Logout successful", Toast.LENGTH_SHORT).show();
            } else {
                // Handle failure or notify the user
                Toast.makeText(PasswordResetSite.this, "Logout failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

}