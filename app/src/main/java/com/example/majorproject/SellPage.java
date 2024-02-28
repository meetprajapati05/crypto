package com.example.majorproject;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.bumptech.glide.Glide;
import com.example.majorproject.databinding.ActivitySellPageBinding;

import org.bson.Document;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;
import io.realm.mongodb.mongo.iterable.FindIterable;
import io.realm.mongodb.mongo.result.UpdateResult;
import okhttp3.OkHttpClient;

public class SellPage extends AppCompatActivity {
    ActivitySellPageBinding binding;
    String coin_id;
    String coin_purchase_date_time;
    String user_email;
    Integer quantity;
    Integer leverage;
    App app;
    User user;
    MongoClient mongoClient;
    MongoDatabase mongoDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySellPageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        coin_id = getIntent().getStringExtra("id").toString();
        coin_purchase_date_time = getIntent().getStringExtra("purchase_date_and_time");

        SharedPreferences preferences = getSharedPreferences("MajorProject",MODE_PRIVATE);
        user_email = preferences.getString("email",null);

        //initlize realm and call that class
        Realm.init(this);

        app = new App(new AppConfiguration.Builder(getString(R.string.MONGO_APP_ID)).build());

        user = app.currentUser();
        mongoClient = user.getMongoClient(getString(R.string.MONGO_DB_SERVICE_NAME));
        mongoDatabase = mongoClient.getDatabase(getString(R.string.MONGO_DATABASE_NAME));


        //Set details
        setCryptoDetail();


        //set balance textview
        setBalance();



        //Set back button
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        //Change price to change quantity
        binding.etSellPageQuantity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.toString().isEmpty()){

                    try {

                        double coin_val = Double.parseDouble(binding.txtSellPageCryptoValue.getText().toString().replace("$", ""));
                        int leverage_val = Integer.parseInt(binding.txtLeverage.getText().toString().replace("x",""));
                        int quantity_val = Integer.parseInt(charSequence.toString());

                        double limitPrice = coin_val / leverage_val * quantity_val;

                        binding.etSellPageLimitPrice.setText("" + limitPrice);
                    }catch (NumberFormatException e){
                        e.printStackTrace();
                    }
                }else{
                    binding.etSellPageLimitPrice.setText("0.0");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        //sell on click sell button
        binding.btnSellPageSell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int BuyQuantity = Integer.parseInt(binding.txtSellPageQuantity.getText().toString());
                int sellQuantity = Integer.parseInt(binding.etSellPageQuantity.getText().toString());
                if(BuyQuantity==sellQuantity){
                    binding.btnSellPageSell.setVisibility(View.INVISIBLE);
                    binding.btnSellProgress.setVisibility(View.VISIBLE);
                    removePortfolio();
                } else if (BuyQuantity < sellQuantity) {
                    Dialog dialog = new Dialog(SellPage.this);
                    dialog.setContentView(R.layout.dailog_insuffician_coin);
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    dialog.getWindow().setBackgroundDrawable(null);
                    dialog.findViewById(R.id.dailogInsufficientButton).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.cancel();
                        }
                    });

                    dialog.show();
                } else{
                    binding.btnSellPageSell.setVisibility(View.INVISIBLE);
                    binding.btnSellProgress.setVisibility(View.VISIBLE);
                    setRemoveSellQuantity();
                }
            }
        });

    }

    private void setRemoveSellQuantity() {
        MongoCollection<Document> collection = mongoDatabase.getCollection(getString(R.string.MONGO_DB_USER_COLLECTION));

        Document filter = new Document(new Document("user_id", user.getId()).append("email",user_email).append("portfolio.coin_id",coin_id).append("portfolio.purchase_date_and_time", binding.txtSellPageDateTime.getText().toString()));

        collection.find(filter).first().getAsync(new App.Callback<Document>() {
            @Override
            public void onResult(App.Result<Document> result) {
                if (result.isSuccess()) {
                    Document document = result.get();

                    if (document != null) {
                        List<Document> portfolios = document.getList("portfolio", Document.class);

                        for (Document portfolioData : portfolios) {
                            if (coin_id.equals(portfolioData.get("coin_id", String.class))
                                    && binding.txtSellPageDateTime.getText().toString().equals(portfolioData.get("purchase_date_and_time", String.class))) {

                                int quantity = portfolioData.getInteger("coin_quantity");
                                int leverage = portfolioData.getInteger("purchase_leverage_in");
                                double purchase_val = portfolioData.getDouble("purchase_value").doubleValue();
                                double purchase_time_coin_price = portfolioData.getDouble("purchase_time_coin_price");

                                 double sellingOfOldValue = purchase_time_coin_price / leverage * quantity;

                                int selllQuantity = Integer.parseInt(binding.etSellPageQuantity.getText().toString());
                                double sellingOfNewValue = purchase_time_coin_price / leverage * selllQuantity;

                                double change_purchase_value = sellingOfOldValue - sellingOfNewValue;

                                Document filter = new Document("user_id", user.getId())
                                        .append("email", user_email)
                                        .append("portfolio.coin_id", coin_id)
                                        .append("portfolio.purchase_date_and_time", binding.txtSellPageDateTime.getText().toString());

                                // Create an update document to set the new quantity value
                                Document update = new Document("$set",
                                        new Document("portfolio.$.coin_quantity", quantity - selllQuantity)
                                                .append("portfolio.$.purchase_value", change_purchase_value));

                                collection.updateOne(filter,update).getAsync(new App.Callback<UpdateResult>() {
                                    @Override
                                    public void onResult(App.Result<UpdateResult> result) {
                                        if(result.isSuccess()){
                                            updateBalance();
                                        }else{
                                            binding.btnSellPageSell.setVisibility(View.VISIBLE);
                                            binding.btnSellProgress.setVisibility(View.INVISIBLE);
                                            Log.e("ErrUpdatePortfolio",result.getError().toString());
                                        }
                                    }
                                });
                            }
                        }
                        }
                }else{
                    binding.btnSellPageSell.setVisibility(View.VISIBLE);
                    binding.btnSellProgress.setVisibility(View.INVISIBLE);
                    Log.e("ErrGetPortfolio",result.getError().toString());
                }
            }
        });


    }

    private void removePortfolio() {
        MongoCollection<Document> collection = mongoDatabase.getCollection(getString(R.string.MONGO_DB_USER_COLLECTION));
        Document filterUser = new Document("user_id",user.getId()).append("email",user_email);
        Document deleteData = new Document("$pull", new Document("portfolio",new Document("coin_id",coin_id).append("purchase_date_and_time", binding.txtSellPageDateTime.getText().toString())));

        collection.updateOne(filterUser,deleteData).getAsync(new App.Callback<UpdateResult>() {
            @Override
            public void onResult(App.Result<UpdateResult> result) {
                if(result.isSuccess()){
                    updateBalance();
                }else{
                    binding.btnSellPageSell.setVisibility(View.VISIBLE);
                    binding.btnSellProgress.setVisibility(View.INVISIBLE);
                    Log.e("ErrSellPortfolioRemove", result.getError().toString());
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
                        double returned_value = Double.parseDouble(binding.etSellPageLimitPrice.getText().toString());
                        double available_balance = balance + returned_value;

                        collection.updateOne(new Document("user_id", user.getId()).append("email", user_email), new Document("$set",new Document("balance",available_balance))).getAsync(new App.Callback<UpdateResult>() {
                            @Override
                            public void onResult(App.Result<UpdateResult> result) {
                                if(result.isSuccess()){
                                    addDataOnHistory();
                                }else{
                                    binding.btnSellPageSell.setVisibility(View.VISIBLE);
                                    binding.btnSellProgress.setVisibility(View.INVISIBLE);
                                    Log.e("ErrBuyPageUpdateBalance", result.getError().toString());
                                }
                            }
                        });
                    }
                }else{
                    binding.btnSellPageSell.setVisibility(View.VISIBLE);
                    binding.btnSellProgress.setVisibility(View.INVISIBLE);
                    Log.e("ErrBalanceUpdateFind", result.getError().toString());
                }
            }
        });
    }

    private void addDataOnHistory() {
        String action = "Sell";
        String date_time = getDateTime();
        Double coin_value = Double.valueOf(binding.txtSellPageCryptoValue.getText().toString().replace("$", ""));
        String coin_symbol = binding.txtSellPageCryptoSymbol.getText().toString();
        String coin_name = binding.txtSellPageCryptoName.getText().toString();
        int quntity = Integer.parseInt(binding.etSellPageQuantity.getText().toString());
        Double invest = Double.valueOf(binding.etSellPageLimitPrice.getText().toString());
        Double available_balance = Double.valueOf(binding.txtSellPageBalance.getText().toString().replace("$","")) + Double.valueOf(binding.etSellPageLimitPrice.getText().toString());
        int leverage = Integer.parseInt(binding.txtLeverage.getText().toString().replace("x",""));


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
                    binding.btnSellPageSell.setVisibility(View.VISIBLE);
                    binding.btnSellProgress.setVisibility(View.INVISIBLE);
                    Dialog dialogSell = new Dialog(SellPage.this);
                    dialogSell.setContentView(R.layout.dailog_buy_successful);
                    dialogSell.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    dialogSell.getWindow().setBackgroundDrawable(null);

                    dialogSell.findViewById(R.id.dailogBuySuccessfullButton).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            onBackPressed();
                        }
                    });

                    dialogSell.show();
                }else{
                    binding.btnSellPageSell.setVisibility(View.VISIBLE);
                    binding.btnSellProgress.setVisibility(View.INVISIBLE);
                    Log.e("ErrBuyPageHistory", result.getError().toString());
                }
            }
        });

    }

    private  String  getDateTime(){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date currentDT = new Date();
        return format.format(currentDT);
    }

    private void setPriceLeverageQuantity(Double coin_price) {
        MongoCollection<Document> collection = mongoDatabase.getCollection(getString(R.string.MONGO_DB_USER_COLLECTION));

        collection.find(new Document("user_id", user.getId()).append("email", user_email))
                .first()
                .getAsync(new App.Callback<Document>() {
                    @Override
                    public void onResult(App.Result<Document> result) {
                        if(result.isSuccess()){
                            if(result.get().get("portfolio")!=null){

                                List<Document> portfolio = (List<Document>) result.get().get("portfolio");

                                List<Document> filteredPortfolio = portfolio.stream()
                                        .filter(item -> coin_id.equals(item.getString("coin_id")))
                                        .collect(Collectors.toList());

                                if(filteredPortfolio!=null){
                                    for(Document filterItem : filteredPortfolio){
                                            Double priceprice = filterItem.getDouble("purchase_value");
                                            quantity = filterItem.getInteger("coin_quantity");
                                            leverage = filterItem.getInteger("purchase_leverage_in");
                                            String dateTime = filterItem.getString("purchase_date_and_time");

                                            //Set Price edit textbox
                                            Double limitPrice = coin_price / leverage * quantity;

                                            binding.etSellPageLimitPrice.setText(limitPrice.toString());

                                            binding.etSellPageQuantity.setText(quantity.toString());
                                            binding.txtLeverage.setText(leverage + "x");
                                            binding.txtSellPageDateTime.setText(dateTime);
                                            binding.txtSellPageQuantity.setText(quantity.toString());
                                            break;
                                    }
                                }
                            }
                        }else{
                            Log.e("ErrSellPageGetData", result.getError().toString());
                        }
                    }
                });
    }

    private void setCryptoDetail() {
        AndroidNetworking.initialize(SellPage.this);

        OkHttpClient client = new OkHttpClient().newBuilder().build();

        AndroidNetworking.initialize(SellPage.this,client);

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
                            String coin_name = response.getString("name");
                            String coin_symbol = response.getString("symbol");

                            JSONObject objImage = response.getJSONObject("image");
                            String coin_img_url = objImage.getString("large");

                            JSONObject objMarketData = response.getJSONObject("market_data");
                            JSONObject objCurrentPrice = objMarketData.getJSONObject("current_price");
                            Double coin_current_price = objCurrentPrice.getDouble("usd");
                            Double coin_change_percentage_24h = objMarketData.getDouble("price_change_percentage_24h");

                          //Set image link with circular Imageview using glide
                            Glide.with(SellPage.this)
                                    .load(coin_img_url)
                                    .into(binding.imgSellPageCrypto);

                            //Set coin name
                            binding.txtSellPageCryptoName.setText(coin_name);

                            //Set Type
                            binding.txtSellPageCryptoType.setText("usd");

                            //Set Coin symbol
                            binding.txtSellPageCryptoSymbol.setText(coin_symbol);

                            //Set last 24h change price percentage and check the inDeVal is negative or positive then change Text color
                            if(coin_change_percentage_24h>=0){
                                binding.txtSellPageCryptoInDeVal.setTextColor(Color.GREEN);
                            }else{
                                binding.txtSellPageCryptoInDeVal.setTextColor(Color.RED);
                            }
                            binding.txtSellPageCryptoInDeVal.setText(String.format("%.2f",coin_change_percentage_24h));

                            //Set value
                            binding.txtSellPageCryptoValue.setText("$" + String.format("%.2f",coin_current_price));
                            binding.txtSellPageCurrentPrice.setText(coin_current_price.toString());


                            //Set mongo database filter data
                            if(coin_purchase_date_time!=null) {
                                setPriceLeverageQuantitySearchingByPurchaseDate(coin_current_price);
                            }else{
                                setPriceLeverageQuantity(coin_current_price);
                            }
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

    private void setPriceLeverageQuantitySearchingByPurchaseDate(Double coinCurrentPrice) {
        MongoCollection<Document> collection = mongoDatabase.getCollection(getString(R.string.MONGO_DB_USER_COLLECTION));

        collection.find(new Document("user_id", user.getId()).append("email", user_email))
                .first()
                .getAsync(new App.Callback<Document>() {
                    @Override
                    public void onResult(App.Result<Document> result) {
                        if(result.isSuccess()){
                            if(result.get().get("portfolio")!=null){

                                List<Document> portfolio = (List<Document>) result.get().get("portfolio");

                                List<Document> filteredPortfolio = portfolio.stream()
                                        .filter(item -> coin_id.equals(item.getString("coin_id")))
                                        .collect(Collectors.toList());

                                if(filteredPortfolio!=null){
                                    for(Document filterItem : filteredPortfolio){

                                        if(filterItem.getString("purchase_date_and_time").equals(coin_purchase_date_time) && filterItem.getString("coin_id").equals(coin_id) ) {
                                            Double priceprice = filterItem.getDouble("purchase_value");
                                            quantity = filterItem.getInteger("coin_quantity");
                                            leverage = filterItem.getInteger("purchase_leverage_in");
                                            String dateTime = filterItem.getString("purchase_date_and_time");

                                            //Set Price edit textbox
                                            Double limitPrice = coinCurrentPrice / leverage * quantity;

                                            binding.etSellPageLimitPrice.setText(limitPrice.toString());

                                            binding.etSellPageQuantity.setText(quantity.toString());
                                            binding.txtLeverage.setText(leverage + "x");
                                            binding.txtSellPageDateTime.setText(dateTime);
                                            binding.txtSellPageQuantity.setText(quantity.toString());
                                        }
                                    }
                                }
                            }
                        }else{
                            Log.e("ErrSellPageGetData", result.getError().toString());
                        }
                    }
                });
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
                                binding.txtSellPageBalance.setText(String.format("%.2f",balance)+" $");
                            }
                        }else{
                            Log.e("ErrBuyPageBalance", result.getError().toString());
                        }
                    }
                });
    }
}