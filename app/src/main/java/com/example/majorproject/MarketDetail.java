package com.example.majorproject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.majorproject.databinding.ActivityMarketDetailBinding;

import org.bson.Document;

import java.util.Collections;

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

        showChart(symbol,type,"Light");


    }

    public void showChart(String symbol,String type,String theme){
        String chartUrl = "https://s.tradingview.com/widgetembed/?frameElementId=tradingview_76d87&symbol="+ symbol + type +"&interval=D&hidesidetoolbar=1&hidetoptoolbar=1&symboledit=1&saveimage=1&toolbarbg=FFFFFF&studies=[]&hideideas=1&theme="+ theme +"&style=1&timezone=Etc%2FUTC&studies_overrides={}&overrides={}&enabled_features=[]&disabled_features=[]&locale=en&utm_source=coinmarketcap.com&utm_medium=widget&utm_campaign=chart";
        WebSettings webSettings = binding.marketDetailChart.getSettings();
        webSettings.setJavaScriptEnabled(true);
        binding.marketDetailChart.clearFormData();
        binding.marketDetailChart.loadUrl(chartUrl);
        binding.marketDetailChart.setWebContentsDebuggingEnabled(true);
        binding.marketDetailChart.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                view.loadUrl(request.toString());
                return super.shouldOverrideUrlLoading(view, request);
            }
        });

    }
}