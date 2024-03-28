package com.example.majorproject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.majorproject.databinding.ActivityWalletBinding;
import com.razorpay.Checkout;
import com.razorpay.PaymentData;
import com.razorpay.PaymentResultWithDataListener;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;
import io.realm.mongodb.mongo.result.UpdateResult;

public class Wallet extends AppCompatActivity implements PaymentResultWithDataListener {

    ActivityWalletBinding binding;

    String userObjId;
    App app;
    MongoClient client;
    MongoDatabase database;
    String email;
    int payableVal = 0;
    int payableDollar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWalletBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userObjId = getIntent().getStringExtra("_id");

        //initlizing Realm and realmDatabase
        Realm.init(this);
        app = new App(new AppConfiguration.Builder(getString(R.string.MONGO_APP_ID)).build());

        client = app.currentUser().getMongoClient(getString(R.string.MONGO_DB_SERVICE_NAME));
        database = client.getDatabase(getString(R.string.MONGO_DATABASE_NAME));

        //get string that login the user
        SharedPreferences preferences = getSharedPreferences("MajorProject", MODE_PRIVATE);
        email = preferences.getString("email",null);

        binding.toolbarWallet.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        setBalanceAndInvest();

        //set add button
        binding.btnWalletAddMoney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.layoutWalletPaymentsButton.setVisibility(View.VISIBLE);
            }
        });

        //set value button
        binding.btnWallet10R.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setButtonColorOnClicked(binding.btnWallet10R);
                payableVal = 10;
                payableDollar = 100;
                binding.txtWalletPayPrice.setText(payableVal+"₹");
                binding.txtWalletPayPrice.setVisibility(View.VISIBLE);
                binding.btnWalletPay.setVisibility(View.VISIBLE);
            }
        });

        binding.btnWallet50R.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setButtonColorOnClicked(binding.btnWallet50R);
                payableVal = 50;
                payableDollar = 500;
                binding.txtWalletPayPrice.setText(payableVal+"₹");
                binding.txtWalletPayPrice.setVisibility(View.VISIBLE);
                binding.btnWalletPay.setVisibility(View.VISIBLE);
            }
        });

        binding.btnWalletPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Checkout.preload(getApplicationContext());

                Checkout checkout = new Checkout();
                checkout.setKeyID(getString(R.string.RAZORPAY_API_ID));

                MongoCollection<Document> collection = database.getCollection(getString(R.string.MONGO_DB_USER_COLLECTION));

                collection.findOne(new Document("_id", new ObjectId(userObjId))).getAsync(new App.Callback<Document>() {
                    @SuppressLint({"SetTextI18n"})
                    @Override
                    public void onResult(App.Result<Document> result) {
                        if(result.get()!=null){
                            String user_id = result.get().getString("user_id");
                            String user_name = result.get().getString("name");
                            String user_email = result.get().getString("email");
                            String phone_no = result.get().getString("phone_no");
                            String img_link = result.get().getString("img_url");

                            checkout.setImage(R.drawable.app_logo);

                            /**
                             * Reference to current activity
                             */
                            final Activity activity = Wallet.this;

                            /**
                             * Pass your payment options to the Razorpay Checkout as a JSONObject
                             */

                            int amount = Math.round(Float.parseFloat(""+payableVal) * 100);

                            try {
                                JSONObject options = new JSONObject();
                                options.put("name","Coin Galaxy");
                                options.put("description", "Add money to wallet.");
                                options.put("theme.color", getColor(R.color.light_green));
                                options.put("amount", amount);
                                options.put("prefill.contact", phone_no);
                                options.put("prefill.email", email);

                                checkout.open(activity, options);

                            } catch(Exception e) {
                                Log.e("ErrPayment", "Error in starting Razorpay Checkout", e);
                            }

                        }
                    }
                });
            }
        });

    }

    private void setBalanceAndInvest() {
        //set current balance invest value and return value
        MongoCollection<Document> collection = database.getCollection(getString(R.string.MONGO_DB_USER_COLLECTION));

        collection.findOne(new Document("_id", new ObjectId(userObjId))).getAsync(new App.Callback<Document>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResult(App.Result<Document> result) {
                if(result.get()!=null){
                    double balance = result.get().getDouble("balance");

                    int balanceInInt = (int) balance;
                    double blancePointZero = balance - balanceInInt;
                    double pointBalance = blancePointZero * 100;
                    int pointVal = (int) pointBalance;

                    binding.txtWalletCurrentBalance.setText("$"+balanceInInt);
                    binding.txtWalletCurrentBalancePoint.setText("."+pointVal);

                    List<Document> portfolios = result.get().getList("portfolio", Document.class);
                    if(portfolios!=null){
                        double investVal = 0.0;
                        for(Document portfolio : portfolios){
                            investVal = investVal + portfolio.getDouble("purchase_value");
                        }
                        binding.txtWalletInvestBalance.setText(String.format("%.2f",investVal)+"$");
                    }
                    else {
                        binding.txtWalletInvestBalance.setText(0.00 + "$");
                    }

                    List<Document> historys = result.get().getList("history", Document.class);
                    if(historys!=null){
                        double reciveVal = 0.0;
                        for(Document history : historys){
                            if (history.getString("action").equals("Sell")) {
                                double sellPrice = history.getDouble("money_flow");
                                double sell_time_price = history.getDouble("purchase_time_price");
                                int leverage = history.getInteger("purchase_leverage_in-x");
                                int quantity = history.getInteger("coin_quntity");
                                double purchace =  (sell_time_price / leverage) * quantity;
                                double reciveSingleValue = sellPrice - purchace;
                                reciveVal = reciveVal + reciveSingleValue;
                            }
                        }
                        binding.txtWalletReciveBalance.setText(String.format("%.2f",reciveVal)+"$");
                    }
                    else{
                        binding.txtWalletReciveBalance.setText(0.00+"$");
                    }
                }
            }
        });

    }

    @SuppressLint("ResourceAsColor")
    public void setButtonColorOnClicked(Button clickedButton){
        //Set all button background unselected
        binding.btnWallet10R.setBackgroundResource(R.drawable.market_unselected_button_backgroug);
        binding.btnWallet10R.setTextColor(ContextCompat.getColor(this,R.color.light_green));
        binding.btnWallet50R.setBackgroundResource(R.drawable.market_unselected_button_backgroug);
        binding.btnWallet50R.setTextColor(ContextCompat.getColor(this,R.color.light_green));

        clickedButton.setBackgroundResource(R.drawable.market_selected_button_background);
        clickedButton.setTextColor(ContextCompat.getColor(this,R.color.white));
    }

    private void updateBalance(String s, PaymentData paymentData){
        MongoCollection<Document> collection = database.getCollection(getString(R.string.MONGO_DB_USER_COLLECTION));

        collection.find(new Document("_id", new ObjectId(userObjId))).first().getAsync(new App.Callback<Document>() {
            @Override
            public void onResult(App.Result<Document> result) {
                if(result.isSuccess()) {
                    if (result.get() != null) {
                        double balance = result.get().getDouble("balance");

                        double total_balance = balance + payableDollar;

                        String user_id = result.get().getString("user_id");
                        collection.updateOne(new Document("_id", new ObjectId(userObjId)), new Document("$set", new Document("balance", total_balance))).getAsync(new App.Callback<UpdateResult>() {
                            @Override
                            public void onResult(App.Result<UpdateResult> result) {
                                if(result.isSuccess()){
                                    addDataToMongoDb(s, paymentData);
                                }else{
                                    Log.e("ErrWalletUpdateBalance", result.getError().toString());
                                }
                            }
                        });
                    }
                }else {
                    Log.e("ErrWalletGetBalance", result.getError().toString());
                }
            }
        });
    }

    private void addDataToMongoDb(String s, PaymentData paymentData) {

            MongoCollection<Document> collection = database.getCollection(getString(R.string.MONGO_DB_USER_COLLECTION));

            Document filter = new Document("_id", new ObjectId(userObjId));

            Document data = new Document("$push", new Document("payment_transaction", new Document(
                    new Document("user_id", app.currentUser().getId())
                    .append("payment_id", paymentData.getPaymentId())
                    .append("payment_external_wallet", paymentData.getExternalWallet())
                    .append("signature", paymentData.getSignature())
                    .append("payable_mobile_no", paymentData.getUserContact())
                    .append("payable_email", paymentData.getUserEmail())
                    .append("payment_date_and_time",getDateTime())
                    .append("pay_money", payableVal)
                    .append("add_balance", payableDollar)
                    .append("status", "Success")
            )));

            collection.updateOne(filter,data).getAsync(new App.Callback<UpdateResult>() {
                @Override
                public void onResult(App.Result<UpdateResult> result) {
                    if(result.isSuccess()){
                        onRestart();
                    }else{
                        Log.e("ErrWalletSetPaymentTransaction", result.getError().toString());
                    }
                }
            });
    }

    private String getDateTime() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date currentDateTime = new Date();
        return  format.format(currentDateTime);
    }

    @Override
    public void onPaymentSuccess(String s, PaymentData paymentData) {
       updateBalance(s,paymentData);

    }

    @Override
    public void onPaymentError(int i, String s, PaymentData paymentData) {
        Log.e("ErrPaymentError", "Payment has canceled by "+ s);
        MongoCollection<Document> collection = database.getCollection(getString(R.string.MONGO_DB_USER_COLLECTION));

        Document filter = new Document("_id", new ObjectId(userObjId));

        Document data = new Document("$push", new Document("payment_transaction", new Document(
                new Document("user_id", app.currentUser().getId())
                        .append("payment_id", paymentData.getPaymentId())
                        .append("payment_external_wallet", paymentData.getExternalWallet())
                        .append("signature", paymentData.getSignature())
                        .append("payable_mobile_no", paymentData.getUserContact())
                        .append("payable_email", paymentData.getUserEmail())
                        .append("payment_date_and_time",getDateTime())
                        .append("pay_money", payableVal)
                        .append("add_balance", payableDollar)
                        .append("status", "Failed")
        )));

        collection.updateOne(filter,data).getAsync(new App.Callback<UpdateResult>() {
            @Override
            public void onResult(App.Result<UpdateResult> result) {
                if(result.isSuccess()){

                }else{
                    Log.e("ErrWalletSetPaymentTransactionFailed", result.getError().toString());
                }
            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        setBalanceAndInvest();
        binding.btnWalletPay.setVisibility(View.GONE);
        binding.txtWalletPayPrice.setVisibility(View.GONE);
        binding.layoutWalletPaymentsButton.setVisibility(View.GONE);
    }
}