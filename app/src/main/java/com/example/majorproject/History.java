package com.example.majorproject;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.majorproject.Adapters.HistoryAdapter;
import com.example.majorproject.Models.HistoryModel;
import com.example.majorproject.databinding.ActivityHistoryBinding;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;

public class History extends AppCompatActivity {

    ActivityHistoryBinding binding;
    App app;
    MongoClient client;
    MongoDatabase database;

    ArrayList<HistoryModel> data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        data = new ArrayList<>();

        Realm.init(History.this);
        app = new App(new AppConfiguration.Builder(getString(R.string.MONGO_APP_ID)).build());
        client = app.currentUser().getMongoClient(getString(R.string.MONGO_DB_SERVICE_NAME));
        database = client.getDatabase(getString(R.string.MONGO_DATABASE_NAME));

        String userObjId = getIntent().getStringExtra("_id");

        setHistory(userObjId);

        binding.toolbarHistory.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void setHistory(String userObjId) {
        binding.progressHistory.setVisibility(View.VISIBLE);
        MongoCollection<Document> collection = database.getCollection(getString(R.string.MONGO_DB_USER_COLLECTION));

        collection.findOne(new Document("_id", new ObjectId(userObjId))).getAsync(new App.Callback<Document>() {
            @Override
            public void onResult(App.Result<Document> result) {
                if(result.get()!=null){
                    List<Document> historys = result.get().getList("history", Document.class);
                    for(Document history : historys){
                        HistoryModel model = new HistoryModel();
                        model.setAction(history.getString("action"));
                        model.setAction_date_and_time(history.getString("action_date_and_time"));
                        model.setAction_time_coin_value(history.getDouble("action_time_coin_value"));
                        model.setCoin_id(history.getString("coin_id"));
                        model.setCoin_symbol(history.getString("coin_symbol"));
                        model.setCoin_name(history.getString("coin_name"));
                        model.setCoin_quntity(history.getInteger("coin_quntity"));
                        model.setMoney_flow(history.getDouble("money_flow"));
                        model.setUser_balance(history.getDouble("user_balance"));
                        model.setPurchase_leverage_in(history.getInteger("purchase_leverage_in-x"));

                        data.add(model);
                    }
                    binding.layoutEmptyHistory.setVisibility(View.GONE);
                    binding.progressHistory.setVisibility(View.GONE);
                    HistoryAdapter adapter = new HistoryAdapter(History.this, data);
                    binding.recyclerHistory.setAdapter(adapter);
                }else{
                    binding.layoutEmptyHistory.setVisibility(View.VISIBLE);
                    binding.lottieEmptyHistory.playAnimation();
                    binding.progressHistory.setVisibility(View.GONE);
                }
            }
        });
    }
}