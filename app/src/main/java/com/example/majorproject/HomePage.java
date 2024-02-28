package com.example.majorproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SharedMemory;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.majorproject.Fragment.HomeFragment;
import com.example.majorproject.Fragment.MarketFragment;
import com.example.majorproject.Fragment.PortfolioFragment;
import com.example.majorproject.Fragment.WatchlistFragment;
import com.example.majorproject.databinding.ActivityHomePageBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.bson.Document;

import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;
import io.realm.mongodb.mongo.options.UpdateOptions;
import io.realm.mongodb.mongo.result.UpdateResult;

public class HomePage extends AppCompatActivity {

    ActivityHomePageBinding binding;
    String user_email;
    Boolean signIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomePageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        user_email = getIntent().getStringExtra("email");
        signIn = getIntent().getBooleanExtra("signIn",false);

        if(user_email != null){
            SharedPreferences setUserId = getSharedPreferences("MajorProject", MODE_PRIVATE);
            SharedPreferences.Editor editor = setUserId.edit();
            editor.putString("email", user_email);
            editor.apply();
        }

        //Check that email with this user id or not
        if(signIn) {
            Realm.init(this);
            User user = new App(new AppConfiguration.Builder(getString(R.string.MONGO_APP_ID)).build()).currentUser();

            MongoClient client = user.getMongoClient(getString(R.string.MONGO_DB_SERVICE_NAME));
            MongoDatabase database = client.getDatabase(getString(R.string.MONGO_DATABASE_NAME));
            MongoCollection<Document> collection = database.getCollection(getString(R.string.MONGO_DB_USER_COLLECTION));


            Document filter = new Document().append("email",user_email).append("provider","EMAIL_PASSWORD");
            Document update = new Document("$set",new Document("user_id",user.getId().toString()));

            collection.updateOne(filter,update).getAsync(new App.Callback<UpdateResult>() {
                @Override
                public void onResult(App.Result<UpdateResult> result) {
                    if(result.isSuccess()){
                    }else{
                        Log.e("ErrUpdateUserId",result.getError().toString());
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
               } else {
                   setFragment(new MarketFragment   ());
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
}