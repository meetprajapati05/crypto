package com.example.majorproject;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.bumptech.glide.Glide;
import com.example.majorproject.databinding.ActivityBuyPageBinding;

import org.bson.Document;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;
import io.realm.mongodb.mongo.result.UpdateResult;
import okhttp3.OkHttpClient;

public class BuyPage extends AppCompatActivity {
    ActivityBuyPageBinding binding;
    String user_email;
    String coin_id;
    App app;
    User user;
    MongoClient mongoClient;
    MongoDatabase mongoDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBuyPageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        coin_id = getIntent().getStringExtra("id").toString();

        SharedPreferences preferences = getSharedPreferences("MajorProject",MODE_PRIVATE);
        user_email = preferences.getString("email",null);

        //Initlize realm database and get balance
        Realm.init(this);
        app = new App(new AppConfiguration.Builder(getString(R.string.MONGO_APP_ID)).build());

        //Initlize mongoClient and mongoDatabase
        user = app.currentUser();
        mongoClient = user.getMongoClient(getString(R.string.MONGO_DB_SERVICE_NAME));
        mongoDatabase = mongoClient.getDatabase(getString(R.string.MONGO_DATABASE_NAME));

        //Set Page details vai api and stored in variables and display it
        setCryptoDetail();

        //Get balance from realm and get to the balance variables and display it
        setBalance();

        //manage seekbard and leverage on limit price
        final List<Integer> steps = Arrays.asList(1, 5, 10, 20);
        binding.seekbarBuyPageLaverage.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int selectedValue = steps.get(i);
                binding.txtBuyPageSeekbarValue.setText(String.valueOf(selectedValue)+"x");

                //Get Coin value to textview and remove $ symbol and convert coin_val in Double formate
                Double coin_val = Double.valueOf(binding.txtBuyPageCryptoValue.getText().toString().replace("$",""));
                double quantity = Double.valueOf(binding.etBuyPageQuantity.getText().toString());
                Double countLevevrageOnValue = coin_val / selectedValue * quantity;


                binding.etBuyPageLimitPrice.setText(countLevevrageOnValue.toString());

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //Calculate quntity on etQuantity has change
        binding.etBuyPageQuantity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.toString().isEmpty()){
                    int quantity = Integer.valueOf(charSequence.toString());
                    Double currentPrice = Double.valueOf(binding.txtBuyPageCryptoValue.getText().toString().replace("$",""));

                    int leverage = Integer.valueOf(binding.txtBuyPageSeekbarValue.getText().toString().replace("x",""));
                    double calculateLimitPrice = currentPrice / leverage  * quantity;
                    binding.etBuyPageLimitPrice.setText(String.valueOf(calculateLimitPrice));
                }else {
                    binding.etBuyPageLimitPrice.setText("0.0");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        //Set back button
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        //Set buy button
        binding.btnBuyPageBuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.btnBuyPageBuy.setVisibility(View.INVISIBLE);
                binding.btnBuyProgress.setVisibility(View.VISIBLE);
                //Check the user balance for buying coin
                Double balance = Double.valueOf(binding.txtByPageBalance.getText().toString().replace("$",""));
                Double purchase_value = Double.valueOf(binding.etBuyPageLimitPrice.getText().toString());
                if(balance >= purchase_value) {
                    setBuyData();
                }else{
                    binding.btnBuyPageBuy.setVisibility(View.VISIBLE);
                    binding.btnBuyProgress.setVisibility(View.INVISIBLE);

                    Dialog dialog = new Dialog(BuyPage.this);
                    dialog.setContentView(R.layout.dailog_inifficial_balance_layout);
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    dialog.getWindow().setBackgroundDrawable(null);


                    dialog.findViewById(R.id.dailogInsufficientButton).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.cancel();
                        }
                    });

                    dialog.show();

                }
            }
        });

    }

    private void setBuyData() {

        String coin_name = binding.txtBuyPageCryptoName.getText().toString();
        String coin_symbol = binding.txtBuyPageCryptoSymbol.getText().toString();
        int coin_quantity = Integer.parseInt(binding.etBuyPageQuantity.getText().toString());
        String date_time = getDateTime();
        double purchase_value = Double.valueOf(binding.etBuyPageLimitPrice.getText().toString());
        int leverage = Integer.parseInt(binding.txtBuyPageSeekbarValue.getText().toString().replace("x",""));
        double coin_val = Double.valueOf(binding.txtBuyPageCryptoValue.getText().toString().replace("$", ""));
        double set_profit = 0.0;
        double stop_loss = 0.0;
        if(!binding.etBuyPageSetProfit.getText().toString().isEmpty()){
            set_profit = Double.parseDouble(binding.etBuyPageSetProfit.getText().toString());
        }
        if(!binding.etBuyPageStopLoss.getText().toString().isEmpty()){
            stop_loss = Double.parseDouble(binding.etBuyPageStopLoss.getText().toString());
        }

        MongoCollection<Document> collection = mongoDatabase.getCollection(getString(R.string.MONGO_DB_USER_COLLECTION));

        Document filter = new Document("user_id",user.getId()).append("email",user_email);

        Document updateData = new Document("$push",new Document("portfolio",new Document("coin_id",coin_id)
                .append("coin_name",coin_name)
                .append("coin_symbol", coin_symbol)
                .append("purchase_time_coin_price", coin_val)
                .append("coin_quantity", coin_quantity)
                .append("purchase_date_and_time", date_time)
                .append("purchase_value", purchase_value)
                .append("purchase_leverage_in", leverage)
                .append("set_profit",set_profit)
                .append("stop_lose",stop_loss))
        );

        collection.updateOne(filter,updateData).getAsync(new App.Callback<UpdateResult>() {
            @Override
            public void onResult(App.Result<UpdateResult> result) {
                if (result.isSuccess()) {
                    updateBalance();
                }else {
                    binding.btnBuyPageBuy.setVisibility(View.VISIBLE);
                    binding.btnBuyProgress.setVisibility(View.INVISIBLE);
                    Log.e("ErrBuyPagePortfolio", result.getError().toString());
                }
            }
        });

    }

    private void updateBalance() {

        MongoCollection<Document> collection = mongoDatabase.getCollection(getString(R.string.MONGO_DB_USER_COLLECTION));

        collection.find(new Document("user_id", user.getId()).append("email", user_email)).first().getAsync(new App.Callback<Document>() {
            @Override
            public void onResult(App.Result<Document> result) {
                if(result.isSuccess()){
                    if(result.get()!=null){
                        Double balance = result.get().getDouble("balance");
                        double invest_value = Double.parseDouble(binding.etBuyPageLimitPrice.getText().toString());
                        double available_balance = balance - invest_value;

                        collection.updateOne(new Document("user_id", user.getId()).append("email", user_email), new Document("$set",new Document("balance",available_balance))).getAsync(new App.Callback<UpdateResult>() {
                            @Override
                            public void onResult(App.Result<UpdateResult> result) {
                                if(result.isSuccess()){
                                    setBuyHistoryData();
                                }else{
                                    binding.btnBuyPageBuy.setVisibility(View.VISIBLE);
                                    binding.btnBuyProgress.setVisibility(View.INVISIBLE);
                                     Log.e("ErrBuyPageUpdateBalance", result.getError().toString());
                                }
                            }
                        });
                    }
                }else{
                    Log.e("ErrBalanceUpdateFind", result.getError().toString());
                }
            }
        });

    }

    private void setBuyHistoryData(){

        String action = "Buy";
        String date_time = getDateTime();
        Double coin_value = Double.valueOf(binding.txtBuyPageCryptoValue.getText().toString().replace("$", ""));
        String coin_symbol = binding.txtBuyPageCryptoSymbol.getText().toString();
        String coin_name = binding.txtBuyPageCryptoName.getText().toString();
        int quntity = Integer.parseInt(binding.etBuyPageQuantity.getText().toString());
        Double invest = Double.valueOf(binding.etBuyPageLimitPrice.getText().toString());
        Double available_balance = Double.valueOf(binding.txtByPageBalance.getText().toString().replace("$","")) - Double.valueOf(binding.etBuyPageLimitPrice.getText().toString());
        int leverage = Integer.parseInt(binding.txtBuyPageSeekbarValue.getText().toString().replace("x",""));


        MongoCollection<Document> collection = mongoDatabase.getCollection(getString(R.string.MONGO_DB_USER_COLLECTION));

        Document filter = new Document("user_id", user.getId()).append("email",user_email);
        Document data = new Document("$push", new Document("history", new Document()
                .append("action",action)
                .append("action_date_and_time",date_time)
                .append("action_time_coin_value", coin_value)
                .append("coin_id", coin_id)
                .append("coin_symbol", coin_symbol)
                .append("coin_name", coin_name)
                .append("coin_quntity", quntity)
                .append("money_flow", invest)
                .append("user_balance", available_balance)
                .append("purchase_leverage_in-x", leverage)
        ));

        collection.updateOne(filter, data).getAsync(new App.Callback<UpdateResult>() {
            @Override
            public void onResult(App.Result<UpdateResult> result) {
                if(result.isSuccess()){
                    binding.btnBuyPageBuy.setVisibility(View.VISIBLE);
                    binding.btnBuyProgress.setVisibility(View.INVISIBLE);

                    Dialog dialogBuy = new Dialog(BuyPage.this);
                    dialogBuy.setContentView(R.layout.dailog_buy_successful);
                    dialogBuy.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    dialogBuy.getWindow().setBackgroundDrawable(null);

                    dialogBuy.findViewById(R.id.dailogBuySuccessfullButton).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialogBuy.cancel();
                            onRestart();
                        }
                    });

                    dialogBuy.show();
                }else{
                    binding.btnBuyPageBuy.setVisibility(View.VISIBLE);
                    binding.btnBuyProgress.setVisibility(View.INVISIBLE);
                    Log.e("ErrBuyPageHistory", result.getError().toString());
                }
            }
        });
    }

    private String getDateTime() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date currentDateTime = new Date();
        return  format.format(currentDateTime);
    }

    private void setBalance() {

        MongoCollection<Document> collection = mongoDatabase.getCollection(getString(R.string.MONGO_DB_USER_COLLECTION));

        Document filtered = new Document().append("user_id",user.getId()).append("email",user_email);
        collection.find(filtered)
                .first()
                .getAsync(new App.Callback<Document>() {
                    @Override
                    public void onResult(App.Result<Document> result) {
                        if(result.isSuccess()){
                            if(result.get()!=null){
                                Double balance = result.get().getDouble("balance");
                                //Set Balance
                                binding.txtByPageBalance.setText(String.format("%.2f",balance)+" $");
                            }
                        }else{
                            Log.e("ErrBuyPageBalance", result.getError().toString());
                        }
                    }
                });
    }

  /*  Panding work in BuyPage:;
   done->  User Balance update and remove;
   done-> History create and;
    Set Proper Insufficial Funds;
     Set Proper Buy Coin;
   done->  Set buy button loadin;*/

    private void setCryptoDetail() {
        AndroidNetworking.initialize(BuyPage.this);

        OkHttpClient client = new OkHttpClient().newBuilder().build();

        AndroidNetworking.initialize(BuyPage.this,client);

        AndroidNetworking.get("https://api.coingecko.com/api/v3/coins/{id}")
                .addPathParameter("id", coin_id)
                .setTag("BuyPageDetails")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @SuppressLint({"SetTextI18n", "DefaultLocale"})
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            String coin_name = response.getJSONObject("localization").getString("en");
                            String coin_symbol = response.getString("symbol");

                            JSONObject objImage = response.getJSONObject("image");
                            String coin_img_url = objImage.getString("large");

                            JSONObject objMarketData = response.getJSONObject("market_data");
                            JSONObject objCurrentPrice = objMarketData.getJSONObject("current_price");
                            Double coin_current_price = objCurrentPrice.getDouble("usd");
                            Double coin_change_percentage_24h = objMarketData.getDouble("price_change_percentage_24h");

                            //Set image link with circular Imageview using glide
                            Glide.with(BuyPage.this)
                                    .load(coin_img_url)
                                    .into(binding.imgBuyPageCrypto);

                            //Set coin name
                            binding.txtBuyPageCryptoName.setText(coin_name);

                            //Set Type
                            binding.txtBuyPageCryptoType.setText("usd");

                            //Set Coin symbol
                            binding.txtBuyPageCryptoSymbol.setText(coin_symbol);

                            //Set last 24h change price percentage and check the inDeVal is negative or positive then change Text color
                            if(coin_change_percentage_24h>=0.0){
                                binding.txtBuyPageCryptoInDeVal.setTextColor(Color.GREEN);
                            }else{
                                binding.txtBuyPageCryptoInDeVal.setTextColor(Color.RED);
                            }
                            binding.txtBuyPageCryptoInDeVal.setText(String.format("%.2f",coin_change_percentage_24h)+"%");

                            //Set value
                            binding.txtBuyPageCryptoValue.setText("$" + String.format("%.2f",coin_current_price));

                            //Set EditText to limit price and quantity
                            //set edittext limit price
                            binding.etBuyPageLimitPrice.setText(coin_current_price.toString());
                            //set edittext quentity
                            binding.etBuyPageQuantity.setText("1");
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }

                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.e("ErrBuyPageApi",anError.getErrorBody().toString());
                    }
                });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        //Set Page details vai api and stored in variables and display it
        setCryptoDetail();

        //Get balance from realm and get to the balance variables and display it
        setBalance();

        binding.etBuyPageSetProfit.setText("");
        binding.etBuyPageStopLoss.setText("");
    }
}