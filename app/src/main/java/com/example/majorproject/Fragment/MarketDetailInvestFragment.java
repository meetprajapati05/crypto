package com.example.majorproject.Fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.example.majorproject.Adapters.HoldingAdapter;
import com.example.majorproject.Models.HistoryModel;
import com.example.majorproject.R;
import com.example.majorproject.databinding.FragmentMarketDetailInvestBinding;

import org.bson.Document;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;
import okhttp3.OkHttpClient;

public class MarketDetailInvestFragment extends Fragment {

    FragmentMarketDetailInvestBinding binding;
    String coin_id;
    String type;
    double coin_current_price;
    App app;
    MongoClient client;
    MongoDatabase database;
    ArrayList<HistoryModel> data;

    public MarketDetailInvestFragment(String coin_id,String type) {
        // Required empty public constructor
        this.coin_id = coin_id;
        this.type = type;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentMarketDetailInvestBinding.inflate(inflater, container, false);

        data = new ArrayList<>();

        Realm.init(getContext());
        app = new App(new AppConfiguration.Builder(getString(R.string.MONGO_APP_ID)).build());
        client = app.currentUser().getMongoClient(getString(R.string.MONGO_DB_SERVICE_NAME));
        database = client.getDatabase(getString(R.string.MONGO_DATABASE_NAME));

        setHolding(getContext());

        return binding.getRoot();
    }
    private void setHolding(Context context){

        binding.progressHolder.setVisibility(View.VISIBLE);
        AndroidNetworking.initialize(context);

        OkHttpClient client = new OkHttpClient().newBuilder().build();

        AndroidNetworking.initialize(context,client);

        AndroidNetworking.get("https://api.coingecko.com/api/v3/coins/{id}?x_cg_demo_api_key=")
                .addQueryParameter("x_cg_demo_api_key",getString(R.string.COINGECKO_API_KEY))
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
                            coin_current_price = objCurrentPrice.getDouble(type);

                            setHoldingRecycler(context, coin_current_price);

                        } catch (JSONException e) {

                            binding.progressHolder.setVisibility(View.GONE);
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    public void onError(ANError anError) {

                        binding.progressHolder.setVisibility(View.GONE);
                        Log.e("ErrBuyPageApi",anError.getErrorBody().toString());
                    }
                });
    }

    private void setHoldingRecycler(Context context, double coinCurrentPrice) {
        MongoCollection<Document> collection = database.getCollection(getString(R.string.MONGO_DB_USER_COLLECTION));

        SharedPreferences preferences = context.getSharedPreferences("MajorProject", Context.MODE_PRIVATE);
        String email = preferences.getString("email", null);
        collection.findOne(new Document("user_id", app.currentUser().getId()).append("email", email)).getAsync(new App.Callback<Document>() {
            @Override
            public void onResult(App.Result<Document> result) {
                if(result.get()!=null){
                    List<Document> porfolios = result.get().getList("portfolio", Document.class);
                    if(porfolios!=null) {
                        for (Document porfolio : porfolios) {
                            if(porfolio.getString("coin_id").toString().equals(coin_id)) {
                                HistoryModel model = new HistoryModel();
                                model.setAction_date_and_time(porfolio.getString("purchase_date_and_time"));
                                model.setAction_time_coin_value(porfolio.getDouble("purchase_time_coin_price"));
                                model.setCoin_id(porfolio.getString("coin_id"));
                                model.setCoin_name(porfolio.getString("coin_name"));
                                model.setCoin_quntity(porfolio.getInteger("coin_quantity"));
                                model.setMoney_flow(porfolio.getDouble("purchase_value"));
                                model.setPurchase_leverage_in(porfolio.getInteger("purchase_leverage_in"));

                                data.add(model);
                            }
                        }
                        if(data.size()!=0) {
                            binding.msgNoHolderTitle.setVisibility(View.GONE);
                            binding.progressHolder.setVisibility(View.GONE);
                            HoldingAdapter adapter = new HoldingAdapter(context, data, coinCurrentPrice);
                            binding.recyclerHolder.setAdapter(adapter);
                        }else{
                            binding.msgNoHolderTitle.setVisibility(View.VISIBLE);
                            binding.progressHolder.setVisibility(View.GONE);
                        }
                    }
                }
            }
        });
    }
}