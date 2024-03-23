package com.example.majorproject;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.majorproject.databinding.ActivityWalletBinding;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.List;

import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;

public class Wallet extends AppCompatActivity {

    ActivityWalletBinding binding;

    String userObjId;
    App app;
    MongoClient client;
    MongoDatabase database;
    String email;
    int payableVal;

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

                    binding.txtWalletCurrentBalance.setText("$"+String.valueOf(balanceInInt));
                    binding.txtWalletCurrentBalance.setText("."+pointVal);

                    List<Document> historys = result.get().getList("history", Document.class);
                    if(historys!=null){
                        double investVal = 0.0;
                        double reciveVal = 0.0;
                        for(Document history : historys){
                            if(history.getString("action").equals("Buy")){
                                investVal = investVal + history.getDouble("money_flow");
                            } else if (history.getString("action").equals("Sell")) {
                                reciveVal = reciveVal + history.getDouble("money_flow");
                            }
                        }
                        binding.txtWalletInvestBalance.setText(investVal+"$");
                        binding.txtWalletReciveBalance.setText(reciveVal+"$");
                    }
                    else{
                        binding.txtWalletInvestBalance.setText(0.0+"$");
                        binding.txtWalletReciveBalance.setText(0.0+"$");
                    }
                }
            }
        });


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
                binding.txtWalletPayPrice.setText(payableVal+"₹");
                binding.btnWalletPay.setVisibility(View.VISIBLE);
            }
        });

        binding.btnWallet50R.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setButtonColorOnClicked(binding.btnWallet10R);
                payableVal = 50;
                binding.txtWalletPayPrice.setText(payableVal+"₹");
                binding.btnWalletPay.setVisibility(View.VISIBLE);
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
}