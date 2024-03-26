package com.example.majorproject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.majorproject.Fragment.MarketDetailChartFragment;
import com.example.majorproject.Fragment.MarketDetailDetailsFragment;
import com.example.majorproject.Fragment.MarketDetailInvestFragment;
import com.example.majorproject.databinding.ActivityMarketDetailBinding;

import org.bson.Document;

import java.util.List;

import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;
import io.realm.mongodb.mongo.result.UpdateResult;

public class MarketDetail extends AppCompatActivity {
    ActivityMarketDetailBinding binding;
    String name,symbol,type,coin_id,coin_purchase_date_and_time;

    App app;
    User user;
    MongoClient client;
    MongoDatabase database;
    MongoCollection<Document> collection;
    String email;
    boolean buy = false;
    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMarketDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Initlizing realm
        Realm.init(this);
        app = new App(new AppConfiguration.Builder(getString(R.string.MONGO_APP_ID)).build());
        user = app.currentUser();
        client = user.getMongoClient(getString(R.string.MONGO_DB_SERVICE_NAME));
        database = client.getDatabase(getString(R.string.MONGO_DATABASE_NAME));
        collection = database.getCollection(getString(R.string.MONGO_DB_USER_COLLECTION));

        name = getIntent().getStringExtra("name").toString();
        symbol = getIntent().getStringExtra("symbol").toString();
        type = getIntent().getStringExtra("type").toString();
        coin_id = getIntent().getStringExtra("id").toString();
        coin_purchase_date_and_time = getIntent().getStringExtra("purchase_date_and_time");


        binding.marketTitleSymbol.setText(symbol.toUpperCase());
        binding.marketTitleType.setText(type.toUpperCase());

        SharedPreferences emailPreferences = getSharedPreferences("MajorProject", MODE_PRIVATE);
        email = emailPreferences.getString("email",null);


        //Check data is enterd in watchlist or not if exist then check btnAddWatchlist
        SharedPreferences preferences = getSharedPreferences("Watchlist",MODE_PRIVATE);
        boolean isChecked = preferences.getBoolean(name, false);
        if(isChecked){
            binding.btnAddWatchlist.setChecked(true);
        }else {
            Document checkData = new Document().append("user_id",user.getId()).append("email",email).append("watchlist.coin_name", name);

            collection.find(checkData).first().getAsync(new App.Callback<Document>() {
                @Override
                public void onResult(App.Result<Document> result) {
                    if(result.isSuccess()) {
                        if (result.get() != null) {
                            SharedPreferences addIsChecked = getSharedPreferences("Watchlist",MODE_PRIVATE);
                            SharedPreferences.Editor editor = addIsChecked.edit();
                            editor.putBoolean(name,true);
                            editor.apply();
                            binding.btnAddWatchlist.setChecked(true);
                        }
                        else {
                            SharedPreferences addIsChecked = getSharedPreferences("Watchlist",MODE_PRIVATE);
                            SharedPreferences.Editor editor = addIsChecked.edit();
                            editor.putBoolean(name,false);
                            editor.apply();
                            binding.btnAddWatchlist.setChecked(false);
                        }
                    }else{
                        Log.e("ErrWatchlistFindData", result.getError().toString());
                    }
                }
            });
        }

        //set button sell invisible then coin not buy

        collection.findOne(new Document("user_id", user.getId()).append("email",email)).getAsync(new App.Callback<Document>() {
            @Override
            public void onResult(App.Result<Document> result) {
                if(result.get()!=null){
                    List<Document> porfolios = result.get().getList("portfolio", Document.class);
                    if(porfolios!=null) {
                        for (Document porfolio : porfolios) {
                            if(porfolio.getString("coin_id").toString().equals(coin_id)) {
                              buy = true;
                            }
                        }
                        if(!buy) {
                            binding.btnMarketDetailSell.setVisibility(View.GONE);
                            binding.btnMarketDeatilInvest.setVisibility(View.GONE);
                        }
                    }
                }
            }
        });

        //Hide buy sell button then that is not usd
        if(!type.equals("usd")){
            binding.btnMarketDetailBuy.setVisibility(View.GONE);
            binding.btnMarketDetailSell.setVisibility(View.GONE);

            binding.btnMarketDeatilInvest.setVisibility(View.GONE);
          /* TypedValue typedValue = new TypedValue();
            getTheme().resolveAttribute(androidx.appcompat.R.attr.actionBarSize, typedValue, true);
            int actionBarHeight = TypedValue.complexToDimensionPixelSize(typedValue.data, getResources().getDisplayMetrics());*/

            // Convert -65sp to pixels
            float scale = getResources().getDisplayMetrics().scaledDensity;
            int marginInPixels = (int) (-65 * scale + 0.5f);

            // Get existing layout params
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) binding.framMarketDEtailFragments.getLayoutParams();

            // Set bottom margin
            layoutParams.setMargins(layoutParams.leftMargin, layoutParams.topMargin, layoutParams.rightMargin, marginInPixels);

            // Apply the modified layout params
            binding.framMarketDEtailFragments.setLayoutParams(layoutParams);
        }

        //Back button
        binding.btnMarketDetailBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        //Buy button pass buy activity
        binding.btnMarketDetailBuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent iBuy = new Intent(getApplicationContext(),BuyPage.class);
                iBuy.putExtra("id",coin_id);
                startActivity(iBuy);
            }
        });

        //Sell button pass sell activity
        binding.btnMarketDetailSell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(coin_purchase_date_and_time!=null) {
                   ;Intent iSell = new Intent(getApplicationContext(), SellPage.class);
                    iSell.putExtra("id", coin_id);
                    iSell.putExtra("purchase_date_and_time", coin_purchase_date_and_time);
                    startActivity(iSell);
                }else{
                    Intent iSell = new Intent(getApplicationContext(), SellPage.class);
                    iSell.putExtra("id", coin_id);
                    startActivity(iSell);
                }
            }
        });

        binding.btnAddWatchlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(binding.btnAddWatchlist.isChecked()){
                    Document filter = new Document("user_id", user.getId()).append("email", email);

                    Document updateDocument = new Document("$push", new Document("watchlist",new Document().append("coin_name", name).append("coin_symbol", symbol).append("coin_id",coin_id)));
                    collection.updateOne(filter,updateDocument).getAsync(new App.Callback<UpdateResult>() {
                        @Override
                        public void onResult(App.Result<UpdateResult> result) {
                            if(result.isSuccess()){
                                if(result.get()!=null) {
                                    SharedPreferences addIsChecked = getSharedPreferences("Watchlist", MODE_PRIVATE);
                                    SharedPreferences.Editor editor = addIsChecked.edit();
                                    editor.putBoolean(name, true);
                                    editor.apply();
                                    Toast.makeText(MarketDetail.this, name + " is added in Watchlist", Toast.LENGTH_SHORT).show();
                                }
                            }else{
                                Log.e("ErrWatchlistInsertData", result.getError().toString());
                            }
                        }
                    });
                }else{
                    Document removeUpdateFilter = new Document().append("$pull", new Document("watchlist",new Document("coin_name",name).append("coin_symbol",symbol).append("coin_id",coin_id)));
                    collection.updateOne(new Document("user_id", user.getId()).append("email",email), removeUpdateFilter).getAsync(new App.Callback<UpdateResult>() {
                        @Override
                        public void onResult(App.Result<UpdateResult> result) {
                            if(result.isSuccess()){
                                SharedPreferences addIsChecked = getSharedPreferences("Watchlist",MODE_PRIVATE);
                                SharedPreferences.Editor editor = addIsChecked.edit();
                                editor.putBoolean(name,false);
                                editor.apply();
                                Toast.makeText(MarketDetail.this, name + "is removed from Watchlist", Toast.LENGTH_SHORT).show();
                            }else{
                                Log.e("ErrWatchlistDeleteData", result.getError().toString());
                            }
                        }
                    });
                }
            }
        });

        setFragment(new MarketDetailChartFragment(symbol, type));

        binding.btnMarketDetailChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setButtonColorOnClicked(binding.btnMarketDetailChart);
                setFragment(new MarketDetailChartFragment(symbol, type));
            }
        });

        binding.btnMarketDeatilDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setButtonColorOnClicked(binding.btnMarketDeatilDetails);
                setFragment(new MarketDetailDetailsFragment(coin_id, type));
            }
        });

        binding.btnMarketDeatilInvest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setButtonColorOnClicked(binding.btnMarketDeatilInvest);
                setFragment(new MarketDetailInvestFragment(coin_id, type));
            }
        });

    }

    @SuppressLint("ResourceAsColor")
    public void setButtonColorOnClicked(Button clickedButton){
        //Set all button background unselected
        binding.btnMarketDetailChart.setBackgroundResource(R.drawable.market_unselected_button_backgroug);
        binding.btnMarketDetailChart.setTextColor(ContextCompat.getColor(MarketDetail.this,R.color.light_green));
        binding.btnMarketDeatilDetails.setBackgroundResource(R.drawable.market_unselected_button_backgroug);
        binding.btnMarketDeatilDetails.setTextColor(ContextCompat.getColor(MarketDetail.this,R.color.light_green));
        binding.btnMarketDeatilInvest.setBackgroundResource(R.drawable.market_unselected_button_backgroug);
        binding.btnMarketDeatilInvest.setTextColor(ContextCompat.getColor(MarketDetail.this,R.color.light_green));

        clickedButton.setBackgroundResource(R.drawable.market_selected_button_background);
        clickedButton.setTextColor(ContextCompat.getColor(MarketDetail.this,R.color.white));
    }

    private void setFragment(Fragment fragment){
        FragmentManager fm =getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.framMarketDEtailFragments, fragment);
        ft.commit();
    }
}