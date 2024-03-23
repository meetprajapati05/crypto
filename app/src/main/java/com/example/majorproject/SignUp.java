package com.example.majorproject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.majorproject.databinding.ActivitySignUpBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import org.bson.Document;
import org.bson.conversions.Bson;

import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.User;
import io.realm.mongodb.auth.GoogleAuthType;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;
import io.realm.mongodb.mongo.result.InsertOneResult;

public class SignUp extends AppCompatActivity {
    ActivitySignUpBinding binding;
    boolean showPass = false;
    boolean showCpass = false;

    GoogleSignInOptions gso;

    GoogleSignInClient googleSignInClient;

    //MongoDB
    App app;
    MongoClient mongoClient;
    MongoDatabase mongoDatabase;

    //This variable is store If the email is exist in MongoAuth that store true else false. that are set in line no:181 - 193
    boolean isNotEmailExist;

    int RC_SING_IN = 1000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestServerAuthCode(getString(R.string.SERVER_CLIENT_ID)).requestEmail().requestProfile().build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        //MongoDB
        Realm.init(this);
        app = new App(new AppConfiguration.Builder(getString(R.string.MONGO_APP_ID)).build());
        checkShowPass();

    }

    private void checkShowPass() {
        binding.btnRegisterPassEye.setOnClickListener(view -> {
            if(showPass){
                binding.etRegisterPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                binding.btnRegisterPassEye.setImageResource(R.drawable.eye_open);
                showPass = false;
            }else {
                binding.etRegisterPass.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                binding.btnRegisterPassEye.setImageResource(R.drawable.eye_close);
                showPass = true;
            }
            binding.etRegisterPass.setSelection(binding.etRegisterPass.length());
        });

        binding.btnRegisterCpassEye.setOnClickListener(view -> {
            if(showCpass){
                binding.etRegisterCpass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                binding.btnRegisterCpassEye.setImageResource(R.drawable.eye_open);
                showCpass = false;
            }else {
                binding.etRegisterCpass.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                binding.btnRegisterCpassEye.setImageResource(R.drawable.eye_close);
                showCpass = true;
            }
            binding.etRegisterCpass.setSelection(binding.etRegisterCpass.length());
        });
    }

    @SuppressLint("SetTextI18n")
    public boolean checkValidation(String name, String email, String pass, String cpass)
    {
        if(name.isEmpty()){
            binding.progressSignUpNext.setVisibility(View.INVISIBLE);
            binding.errRegisterName.setText("Please! enter name");
            binding.errRegisterName.setVisibility(View.VISIBLE);
            return false;
        }
        else if(!name.matches("^[a-zA-Z ]*$")){
            binding.progressSignUpNext.setVisibility(View.INVISIBLE);
            binding.errRegisterName.setText("Name should be character only!");
            binding.errRegisterName.setVisibility(View.VISIBLE);
            return false;
        }
        else {
            binding.errRegisterName.setVisibility(View.INVISIBLE);
        }

        if(email.isEmpty()){
            binding.progressSignUpNext.setVisibility(View.INVISIBLE);
            binding.errRegisterEmail.setText("Please! enter email");
            binding.errRegisterEmail.setVisibility(View.VISIBLE);
            return false;
        }
        else if(!email.endsWith("@gmail.com")){
            binding.progressSignUpNext.setVisibility(View.INVISIBLE);
            binding.errRegisterEmail.setText("Invalid email");
            binding.errRegisterEmail.setVisibility(View.VISIBLE);
            return false;
        }
        else{
            binding.errRegisterEmail.setVisibility(View.INVISIBLE);
        }

       if(pass.length() < 8){
           binding.progressSignUpNext.setVisibility(View.INVISIBLE);
            binding.errRegisterPass.setText("Please! enter minimum 8 character password");
            binding.errRegisterPass.setVisibility(View.VISIBLE);
            return false;
        }
        else{
            binding.errRegisterPass.setVisibility(View.INVISIBLE);
        }

        if(!pass.equals(cpass)){
            binding.progressSignUpNext.setVisibility(View.INVISIBLE);
            binding.errRegisterCpass.setText("Password & Confirm Password are not match");
            binding.errRegisterCpass.setVisibility(View.VISIBLE);
            return false;
        }
        else{
            binding.errRegisterCpass.setVisibility(View.INVISIBLE);
        }

        return true;
    }
    public void btnRegisterSingIn(View view) {
        startActivity(new Intent(getApplicationContext(), SingIn.class));
    }

    public void btnRegisterNext(View view) {
        binding.btnRegisterNext.setVisibility(View.INVISIBLE);
        binding.progressSignUpNext.setVisibility(View.VISIBLE);

        boolean isValidate = checkValidation(binding.etRegisterName.getText().toString(), binding.etRegisterEmail.getText().toString(), binding.etRegisterPass.getText().toString(), binding.etRegisterCpass.getText().toString());

        if(isValidate) {

            Credentials credentials = Credentials.emailPassword("emailaddresscheck1@gmail.com","EmailAddressVerify");
            app.loginAsync(credentials, new App.Callback<User>() {
                @Override
                public void onResult(App.Result<User> result) {

                    if(result.isSuccess()){
                        mongoClient = app.currentUser().getMongoClient(getString(R.string.MONGO_DB_SERVICE_NAME));
                        mongoDatabase = mongoClient.getDatabase(getString(R.string.MONGO_DATABASE_NAME));
                        MongoCollection<Document> collection = mongoDatabase.getCollection(getString(R.string.MONGO_DB_USER_COLLECTION));

                        new LogoutTask().execute();

                        Document  filterQuery = new Document().append("email", binding.etRegisterEmail.getText().toString());
                        collection.findOne(filterQuery).getAsync(new App.Callback<Document>() {
                            @Override
                            public void onResult(App.Result<Document> result) {
                                if(result.get() != null){
                                    binding.progressSignUpNext.setVisibility(View.INVISIBLE);
                                    binding.btnRegisterNext.setVisibility(View.VISIBLE);
                                    binding.errRegisterEmail.setText("This email is already taken");
                                    binding.errRegisterEmail.setVisibility(View.VISIBLE);
                                }
                                else{
                                    binding.errRegisterEmail.setVisibility(View.INVISIBLE);
                                    binding.progressSignUpNext.setVisibility(View.INVISIBLE);
                                    binding.btnRegisterNext.setVisibility(View.VISIBLE);

                                    //Pass intent to mobile register page
                                    Intent intent = new Intent(SignUp.this, MobileNumber.class);
                                    intent.putExtra("name", binding.etRegisterName.getText().toString());
                                    intent.putExtra("email", binding.etRegisterEmail.getText().toString());
                                    intent.putExtra("password", binding.etRegisterPass.getText().toString());
                                    startActivity(intent);
                                }
                            }
                        });
                    }else{
                        binding.progressSignUpNext.setVisibility(View.INVISIBLE);
                        binding.btnRegisterNext.setVisibility(View.VISIBLE);
                        Log.e("EreVerifyLogin",result.getError().toString());
                    }
                }
            });

        }else{
            binding.progressSignUpNext.setVisibility(View.INVISIBLE);
            binding.btnRegisterNext.setVisibility(View.VISIBLE);
        }
    }

    public void btnRegisterGoogle(View view) {
        googleSignUp();
    }

    private void googleSignUp() {
        Intent iGoogle = googleSignInClient.getSignInIntent();
        startActivityForResult(iGoogle, RC_SING_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        binding.btnRegisterGoogle.setVisibility(View.INVISIBLE);
        binding.progressRegisterGoogle.setVisibility(View.VISIBLE);
        if(requestCode == RC_SING_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignUpResult(task);
        }
    }

    private void handleSignUpResult(Task<GoogleSignInAccount> task) {
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            setGoogleAuthInMongoDB(account);
        } catch (ApiException e) {
            Log.e("SignUpApiException",e.getMessage());
        }
    }

    private void setGoogleAuthInMongoDB(GoogleSignInAccount account) {
        String authCode = account.getServerAuthCode();

        Credentials googleCredential = Credentials.google(authCode, GoogleAuthType.AUTH_CODE);

        app.loginAsync(googleCredential, new App.Callback<User>() {
            @Override
            public void onResult(App.Result<User> result) {
                if(result.isSuccess()){
                    //get User
                    User user = app.currentUser();

                    mongoClient = user.getMongoClient(getString(R.string.MONGO_DB_SERVICE_NAME));
                    mongoDatabase = mongoClient.getDatabase(getString(R.string.MONGO_DATABASE_NAME));
                    MongoCollection<Document> collection = mongoDatabase.getCollection(getString(R.string.MONGO_DB_USER_COLLECTION));

                    Bson filter = new Document("email", user.getProfile().getEmail()).append("provider", user.getProviderType().name());
                    collection.findOne(filter).getAsync(new App.Callback<Document>() {
                        @Override
                        public void onResult(App.Result<Document> result) {
                            if(result.isSuccess()){
                                if(result.get()!=null){
                                    binding.btnRegisterGoogle.setVisibility(View.VISIBLE);
                                    binding.progressRegisterGoogle.setVisibility(View.INVISIBLE);

                                    Toast.makeText(SignUp.this, "Google SignUp Successfully ", Toast.LENGTH_SHORT).show();

                                    //Pass next activity
                                    Intent iHome = new Intent(SignUp.this, HomePage.class);
                                    iHome.putExtra("user_id", user.getId());
                                    iHome.putExtra("email", user.getProfile().getEmail());
                                    startActivity(iHome);
                                    finishAffinity();
                                }else{
                                    binding.btnRegisterGoogle.setVisibility(View.VISIBLE);
                                    binding.progressRegisterGoogle.setVisibility(View.INVISIBLE);
                                    addGoogleUserInDatabase(app.currentUser());
                                }
                            }else{
                                binding.btnRegisterGoogle.setVisibility(View.VISIBLE);
                                binding.progressRegisterGoogle.setVisibility(View.INVISIBLE);
                                Log.e("GoogleDataCheckErr", result.getError().toString());
                            }
                        }
                    });
                }else{
                    binding.btnRegisterGoogle.setVisibility(View.VISIBLE);
                    binding.progressRegisterGoogle.setVisibility(View.INVISIBLE);
                    Log.e("ErrAddGoogleData", result.getError().getErrorMessage());
                }
            }
        });
    }
    public void addGoogleUserInDatabase(User user){
        mongoClient = user.getMongoClient(getString(R.string.MONGO_DB_SERVICE_NAME));
        mongoDatabase = mongoClient.getDatabase(getString(R.string.MONGO_DATABASE_NAME));
        MongoCollection<Document> collection = mongoDatabase.getCollection(getString(R.string.MONGO_DB_USER_COLLECTION));

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        assert account != null;
        Document data = new Document("user_id", user.getId())
                .append("name", user.getProfile().getFirstName()+ " " + user.getProfile().getLastName())
                .append("email", user.getProfile().getEmail())
                .append("phone_no", null)
                .append("password", null)
                .append("provider", user.getProviderType().name())
                .append("img_url",  account.getPhotoUrl() != null ? account.getPhotoUrl().toString() : null)
                .append("balance", 10000.0)
                .append("user_block", false);

        collection.insertOne(data).getAsync(new App.Callback<InsertOneResult>() {
            @Override
            public void onResult(App.Result<InsertOneResult> result) {
                binding.btnRegisterGoogle.setVisibility(View.VISIBLE);
                binding.progressRegisterGoogle.setVisibility(View.INVISIBLE);
                if (result.isSuccess()) {
                    Intent intent = new Intent(SignUp.this, HomePage.class);
                    intent.putExtra("user_id", user.getId());
                    intent.putExtra("email",user.getProfile().getEmail());
                    startActivity(intent);
                    finishAffinity();
                } else {
                    Log.e("SignUpDatabaseErr", result.getError().toString());
                }
            }
        });
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
                Log.i("SignUpLogoutSuccess", "Logout successful");
            } else {
                // Handle failure or notify the user
                Log.i("SignUpLogoutFailed", "Logout successful");
            }
        }
    }
}