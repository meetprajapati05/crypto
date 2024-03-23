package com.example.majorproject;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.majorproject.Fragment.HomeFragment;
import com.example.majorproject.Fragment.MarketFragment;
import com.example.majorproject.Fragment.PortfolioFragment;
import com.example.majorproject.Fragment.ProfileFragment;
import com.example.majorproject.Fragment.WatchlistFragment;
import com.example.majorproject.databinding.ActivityHomePageBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.bson.Document;
import org.bson.types.ObjectId;

import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;
import io.realm.mongodb.mongo.result.UpdateResult;

public class HomePage extends AppCompatActivity {

    ActivityHomePageBinding binding;
    String user_email;
    Boolean signIn;
    String userObjId;
    String priviousPage;
    App app;
    MongoCollection<Document> collection;

    boolean isEditGoogleAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomePageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Realm.init(this);
        app = new App(new AppConfiguration.Builder(getString(R.string.MONGO_APP_ID)).build());
        MongoClient client = app.currentUser().getMongoClient(getString(R.string.MONGO_DB_SERVICE_NAME));
        MongoDatabase database = client.getDatabase(getString(R.string.MONGO_DATABASE_NAME));
        collection = database.getCollection(getString(R.string.MONGO_DB_USER_COLLECTION));

        user_email = getIntent().getStringExtra("email");
        priviousPage = getIntent().getStringExtra("priviousActivity");
        userObjId = getIntent().getStringExtra("_id");
        signIn = getIntent().getBooleanExtra("signIn",false);

        isEditGoogleAuth = getIntent().getBooleanExtra("isGoogleAuth", false);

        if(user_email != null){
            SharedPreferences setUserId = getSharedPreferences("MajorProject", MODE_PRIVATE);
            SharedPreferences.Editor editor = setUserId.edit();
            editor.putString("email", user_email);
            editor.apply();
        }

        if(isEditGoogleAuth){
            setFragment(new ProfileFragment());
            binding.bottomNavView.setSelectedItemId(R.id.bottomOptProfile);
        }

        if(priviousPage!=null) {
            collection.findOne(new Document("_id", new ObjectId(userObjId))).getAsync(new App.Callback<Document>() {
                @Override
                public void onResult(App.Result<Document> result) {
                    if (result.isSuccess() && result.get() != null) {
                        String loginEmail = result.get().getString("email");
                        String loginPass = result.get().getString("password");

                        // Register and authenticate user
                        registerAndAuthenticateUser(loginEmail, loginPass);
                    } else {
                        // Handle the error or proceed based on your requirements
                        Log.e("ErrFindUser", result.getError().toString());
                    }
                }
            });
        }

        //set bottom navigation view  select item event
        binding.bottomNavView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                int itemId = item.getItemId();

               if(itemId == R.id.bottomOptHome){
                   setFragment(new HomeFragment());
               } else if (itemId==R.id.bottomOptWatchlist) {
                   setFragment(new WatchlistFragment());
               } else if (itemId==R.id.bottomOptPortfolio) {
                   setFragment(new PortfolioFragment());
               } else if (itemId==R.id.bottomOptProfile) {
                   setFragment(new ProfileFragment());
               } else {
                   setFragment(new MarketFragment());
               }
                return true;
            }
        });

        binding.bottomNavView.setSelectedItemId(R.id.bottomOptHome);
    }

    public void setFragment(Fragment fragment){
            FragmentManager fm =getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.frameLayout, fragment);
            ft.commit();
    }

    //Log out on background task
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
            } else {
                // Handle failure or notify the user
            }
        }
    }

    // Separate function for registering and authenticating the user
    private void registerAndAuthenticateUser(String loginEmail, String loginPass) {
        app.getEmailPassword().registerUserAsync(loginEmail, loginPass, new App.Callback<Void>() {
            @Override
            public void onResult(App.Result<Void> result) {
                if (result.isSuccess()) {
                    // Assign roles and authenticate user
                    assignRolesAndAuthenticate(loginEmail, loginPass);
                } else {
                    // Handle the error or proceed based on your requirements
                    Log.e("ErrRegisterUser", result.getError().toString());
                }
            }
        });
    }

    // Separate function for assigning roles and authenticating the user
    private void assignRolesAndAuthenticate(String loginEmail, String loginPass) {

        // Authenticate user
        Credentials credentials = Credentials.emailPassword(loginEmail, loginPass);
        app.loginAsync(credentials, new App.Callback<User>() {
            @Override
            public void onResult(App.Result<User> result) {
                if (result.isSuccess()) {
                    // Update user ID in the document
                    updateUserIdInDocument();
                } else {
                    // Handle the error or proceed based on your requirements
                    Log.e("ErrAuthUser", result.getError().toString());
                }
            }
        });
    }

    // Separate function for updating user ID in the document
    private void updateUserIdInDocument() {
        collection.updateOne(new Document("_id", new ObjectId(userObjId)),
                        new Document("$set", new Document("user_id", app.currentUser().getId().toString())))
                .getAsync(new App.Callback<UpdateResult>() {
                    @Override
                    public void onResult(App.Result<UpdateResult> result) {
                        if (result.isSuccess()) {
                            // Navigate to ProfileFragment
                            setFragment(new ProfileFragment());
                            binding.bottomNavView.setSelectedItemId(R.id.bottomOptProfile);
                        } else {
                            // Handle the error or proceed based on your requirements
                            Log.e("ErrUpdateUserId", result.getError().toString());
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        if(binding.bottomNavView.getSelectedItemId()!=R.id.bottomOptHome){
            binding.bottomNavView.setSelectedItemId(R.id.bottomOptHome);
        }else{
            super.onBackPressed();
        }
    }
}