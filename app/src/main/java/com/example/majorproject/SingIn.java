package com.example.majorproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.majorproject.databinding.ActivitySingInBinding;
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
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;
import io.realm.mongodb.mongo.result.InsertOneResult;

public class SingIn extends AppCompatActivity {
    ActivitySingInBinding binding;

    private boolean showPass = false;

    //Google Auth Variable
    GoogleSignInOptions gso;
    GoogleSignInClient googleSignInClient;
    int RC_SING_IN = 1000; // Request Code for google startActivityForResult

    //MongoDB Variable
    App app;
    MongoClient mongoClient;
    MongoDatabase mongoDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySingInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Google SignIn Class obj creation
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestServerAuthCode(getString(R.string.SERVER_CLIENT_ID)).requestEmail().requestProfile().build();
        googleSignInClient = GoogleSignIn.getClient(this,gso);


        //MongoDB Class obj and initlize MongoDB Realm
        Realm.init(this);
        app = new App(new AppConfiguration.Builder(getString(R.string.MONGO_APP_ID)).build());

        //Password Eye image setup code
        binding.btnLoginEye.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View view) {
                if(showPass){
                    binding.etLoginPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    binding.btnLoginEye.setImageResource(R.drawable.eye_open);
                    showPass = false;
                }else {
                    binding.etLoginPass.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    binding.btnLoginEye.setImageResource(R.drawable.eye_close);
                    showPass = true;
                }
                binding.etLoginPass.setSelection(binding.etLoginPass.length());
            }
        });

        //Button LoginSignUp clicked code
        binding.btnLoginSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), SignUp.class));
            }
        });

        // Edittext reclick then remove error
        binding.etLoginPass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.errLoginPass.setVisibility(View.INVISIBLE);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        binding.etLoginEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.errLoginPass.setVisibility(View.INVISIBLE);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    //Google auth start after calling this first Method
    private void singIn(){
        binding.btnLoginGoogle.setVisibility(View.VISIBLE);
        binding.progressLoginGoogleSingIn.setVisibility(View.INVISIBLE);
        Intent iGoogleAuth = googleSignInClient.getSignInIntent();
        startActivityForResult(iGoogleAuth, RC_SING_IN);
    }

    //Google Auth get Intent result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SING_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    //Google Auth Intent result handle
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        binding.btnLoginGoogle.setVisibility(View.INVISIBLE);
        binding.progressLoginGoogleSingIn.setVisibility(View.VISIBLE);
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            setGoogleAuthWithMongoDB(account);

        } catch (com.google.android.gms.common.api.ApiException e) {
            Log.e("handleSignInResultException", e.getMessage());
        }
    }

    //Google auth with MongoDb
    private void setGoogleAuthWithMongoDB(GoogleSignInAccount account) {

        String authCode = account.getServerAuthCode();
        Credentials googleCredentials = Credentials.google(authCode);

            app.loginAsync(googleCredentials, new App.Callback<User>() {
                @Override
                public void onResult(App.Result<User> result) {
                    if(result.isSuccess()){
                        binding.btnLoginGoogle.setVisibility(View.VISIBLE);
                        binding.progressLoginGoogleSingIn.setVisibility(View.INVISIBLE);
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
                                        binding.btnLoginGoogle.setVisibility(View.VISIBLE);
                                        binding.progressLoginGoogleSingIn.setVisibility(View.INVISIBLE);

                                        Toast.makeText(SingIn.this, "Google SignUp Successfully ", Toast.LENGTH_SHORT).show();

                                        //Pass next activity
                                        Intent iHome = new Intent(SingIn.this, HomePage.class);
                                        iHome.putExtra("user_id", user.getId());
                                        iHome.putExtra("email",user.getProfile().getEmail());
                                        startActivity(iHome);
                                        finishAffinity();
                                    }else{
                                        binding.btnLoginGoogle.setVisibility(View.VISIBLE);
                                        binding.progressLoginGoogleSingIn.setVisibility(View.INVISIBLE);
                                        setGoogleUserDetailInMongoDbCollection(app.currentUser());
                                    }
                                }else{
                                    binding.btnLoginGoogle.setVisibility(View.VISIBLE);
                                    binding.progressLoginGoogleSingIn.setVisibility(View.INVISIBLE);
                                    Log.e("GoogleDataCheckErr", result.getError().toString());
                                }
                            }
                        });

                    }else{
                        binding.btnLoginGoogle.setVisibility(View.VISIBLE);
                        binding.progressLoginGoogleSingIn.setVisibility(View.INVISIBLE);
                        Toast.makeText(SingIn.this, result.getError().getErrorMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("GoogleErr", result.getError().toString());
                    }
                }
            });
    }

    //Button LoginGoogle click event
    public void btnLoginGoogle(View view) {
        binding.btnLoginGoogle.setVisibility(View.INVISIBLE);
        binding.progressLoginGoogleSingIn.setVisibility(View.VISIBLE);
        singIn();
    }

    //Email password SignIn Method
    private void emailPasswordSingIn(){

        Credentials credentials = Credentials.emailPassword(binding.etLoginEmail.getText().toString() , binding.etLoginPass.getText().toString());

        app.loginAsync(credentials, new App.Callback<User>() {
            @Override
            public void onResult(App.Result<User> result) {
                if(result.isSuccess()){
                    binding.btnLoginSignIn.setVisibility(View.VISIBLE);
                    binding.progressLoginSignIn.setVisibility(View.INVISIBLE);

                    //get user
                    User user = app.currentUser();
                    Toast.makeText(SingIn.this, "Sign In Successfully", Toast.LENGTH_SHORT).show();

                    //Pass to home page
                    Intent iSignIn = new Intent(SingIn.this, HomePage.class);
                    iSignIn.putExtra("user_id", user.getId());
                    iSignIn.putExtra("email", binding.etLoginEmail.getText().toString());
                    iSignIn.putExtra("signIn",true);
                    iSignIn.putExtra("pass",binding.etLoginPass.getText().toString());
                    startActivity(iSignIn);
                    finishAffinity();

                }else{
                    binding.btnLoginSignIn.setVisibility(View.VISIBLE);
                    binding.progressLoginSignIn.setVisibility(View.INVISIBLE);
                    binding.errLoginPass.setText(result.getError().getErrorMessage());
                    binding.errLoginPass.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    //Google SignUp User detail store in MongoDB User Collection
    public void setGoogleUserDetailInMongoDbCollection(User user){
        mongoClient = user.getMongoClient(getString(R.string.MONGO_DB_SERVICE_NAME));
        mongoDatabase = mongoClient.getDatabase(getString(R.string.MONGO_DATABASE_NAME));
        MongoCollection<Document> collection = mongoDatabase.getCollection(getString(R.string.MONGO_DB_USER_COLLECTION));

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        assert account != null;
        Document userData = new Document("user_id", user.getId());
        userData.append("name", user.getProfile().getFirstName() + " " + user.getProfile().getLastName());
        userData.append("email", user.getProfile().getEmail());
        userData.append("phone_no", null);
        userData.append("password", null);
        userData.append("provider", user.getProviderType().name());
        userData.append("img_url",  account.getPhotoUrl() != null ? account.getPhotoUrl().toString() : null);
        userData.append("balance", 10000.0);

        collection.insertOne(userData).getAsync(new App.Callback<InsertOneResult>() {
            @Override
            public void onResult(App.Result<InsertOneResult> result) {
                binding.btnLoginGoogle.setVisibility(View.VISIBLE);
                binding.progressLoginGoogleSingIn.setVisibility(View.INVISIBLE);
                if (result.isSuccess()) {
                    Toast.makeText(SingIn.this, "Google SignUp Successfully ", Toast.LENGTH_SHORT).show();
                    Intent iGoogleSignUp = new Intent(SingIn.this, HomePage.class);
                    iGoogleSignUp.putExtra("user_id", user.getId());
                    iGoogleSignUp.putExtra("email", user.getProfile().getEmail());
                    startActivity(iGoogleSignUp);
                    finishAffinity();
                } else {
                    Log.e("SignInDatabaseErr", result.getError().toString());
                }
            }
        });
    }

    //Button LoginSignIn click event
    public void btnLoginSingIn(View view) {
        boolean isValid;
        isValid = checkValidation(binding.etLoginEmail.getText().toString(), binding.etLoginPass.getText().toString());
        binding.btnLoginSignIn.setVisibility(View.INVISIBLE);
        binding.progressLoginSignIn.setVisibility(View.VISIBLE);
        if(isValid) {
            emailPasswordSingIn();
        }
        else {
            binding.btnLoginSignIn.setVisibility(View.VISIBLE);
            binding.progressLoginSignIn.setVisibility(View.INVISIBLE);
        }
    }

    //Check validation Method. That calling in btnLoginSignIn click event
    private boolean checkValidation(String email, String password){

        if(email.isEmpty())
        {
            binding.errLoginEmail.setText("Please enter email");
            binding.errLoginEmail.setVisibility(View.VISIBLE);
            return false;
        }
        else{
            binding.errLoginEmail.setVisibility(View.INVISIBLE);
        }

        if(password.isEmpty())
        {
            binding.errLoginPass.setText("Password can be minimum 8 digit");
            binding.errLoginPass.setVisibility(View.VISIBLE);
            return false;
        }
        else{
            binding.errLoginPass.setVisibility(View.INVISIBLE);
        }
        return  true;
    }

    //Button btnLoginForgotPass click event
    public void btnLoginForgotPass(View view) {
        startActivity(new Intent(SingIn.this, EmailCheck.class));
    }
}