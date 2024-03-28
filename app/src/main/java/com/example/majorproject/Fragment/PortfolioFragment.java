package com.example.majorproject.Fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.gsonparserfactory.GsonParserFactory;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.example.majorproject.Adapters.PortfolioAdapter;
import com.example.majorproject.Models.CryptoDataModel;
import com.example.majorproject.Models.PortfolioModel;
import com.example.majorproject.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;
import okhttp3.OkHttpClient;

public class PortfolioFragment extends Fragment {

    RecyclerView recyclerView;
    LottieAnimationView progressBar;
    RelativeLayout layoutEmptyPortfolio;
    AppCompatButton btnViewMarket;
    LottieAnimationView lottieEmptyPortfolioAnim;

    PortfolioAdapter adapter;
    SwipeRefreshLayout refresh;

    ArrayList<CryptoDataModel> cryptoData;
    ArrayList<PortfolioModel> portfolioData;
    String email;
    String coins;
    public PortfolioFragment() {
        // Required empty public constructor
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_portfolio, container, false);

        recyclerView = v.findViewById(R.id.portfolioRecycler);
        progressBar = v.findViewById(R.id.portfolioLoadingAnimation);
        refresh = v.findViewById(R.id.portfolioRefresh);
        cryptoData = new ArrayList<>();
        portfolioData = new ArrayList<>();
        layoutEmptyPortfolio = v.findViewById(R.id.layoutEmptyPortfolio);
        lottieEmptyPortfolioAnim = v.findViewById(R.id.lottieEmptyPortfolio);
        btnViewMarket = v.findViewById(R.id.btnPortfolioEmpty);

        SharedPreferences preferences = getContext().getSharedPreferences("MajorProject", Context.MODE_PRIVATE);
        email = preferences.getString("email",null);

        getPortfolioData(email,getContext());
        progressBar.setVisibility(View.VISIBLE);

        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                recyclerView.setAdapter(null);
                cryptoData.clear();
                portfolioData.clear();
                getPortfolioData(email, getContext());
                progressBar.setVisibility(View.VISIBLE);
                refresh.setRefreshing(false);
            }
        });

        //set button view
        btnViewMarket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottomNavView);
                bottomNavigationView.setSelectedItemId(R.id.bottomOptMarket);
            }
        });

        return v;
    }

    private void getPortfolioData(String email, Context context) {
        Realm.init(context);
        App app = new App(new AppConfiguration.Builder(context.getString(R.string.MONGO_APP_ID)).build());
        User user = app.currentUser();
        MongoClient mongoClient = user.getMongoClient(context.getString(R.string.MONGO_DB_SERVICE_NAME));
        MongoDatabase mongoDatabase = mongoClient.getDatabase(context.getString(R.string.MONGO_DATABASE_NAME));
        MongoCollection<Document> collection = mongoDatabase.getCollection(context.getString(R.string.MONGO_DB_USER_COLLECTION));

        Document filter = new Document("user_id", user.getId()).append("email",email);

        if(email!=null) {
            collection.find(filter).first().getAsync(new App.Callback<Document>() {
                @Override
                public void onResult(App.Result<Document> result) {
                    if (result.isSuccess()) {
                        if(result.get()!=null){
                            List<Document> portfolios = result.get().getList("portfolio",Document.class);
                            if(portfolios!=null){
                                layoutEmptyPortfolio.setVisibility(View.INVISIBLE);
                                for(Document portfolioItem : portfolios){
                                    PortfolioModel model = new PortfolioModel();
                                    model.setCoin_id(portfolioItem.getString("coin_id"));
                                    model.setCoin_name(portfolioItem.getString("coin_name"));
                                    model.setCoin_symbol(portfolioItem.getString("coin_symbol"));
                                    model.setPurchase_time_coin_price(portfolioItem.getDouble("purchase_time_coin_price"));
                                    model.setCoin_quantity(portfolioItem.getInteger("coin_quantity"));
                                    model.setPurchase_date_and_time(portfolioItem.getString("purchase_date_and_time"));
                                    model.setPurchase_value(portfolioItem.getDouble("purchase_value"));
                                    model.setPurchase_leverage_in(portfolioItem.getInteger("purchase_leverage_in"));
                                    model.setSet_profit(portfolioItem.getDouble("set_profit"));
                                    model.setStop_lose(portfolioItem.getDouble("stop_lose"));

                                    portfolioData.add(model);

                                    coins = coins + "," + portfolioItem.getString("coin_id");
                                }
                                if(coins!=null) {
                                    setRecycleView(coins, context);
                                }else{
                                    progressBar.setVisibility(View.INVISIBLE);
                                    layoutEmptyPortfolio.setVisibility(View.VISIBLE);
                                    lottieEmptyPortfolioAnim.playAnimation();
                                }
                            }else{
                                progressBar.setVisibility(View.INVISIBLE);
                                layoutEmptyPortfolio.setVisibility(View.VISIBLE);
                                lottieEmptyPortfolioAnim.playAnimation();
                            }
                        }
                    }else{
                        progressBar.setVisibility(View.INVISIBLE);
                        Log.e("ErrPortfolioFindUser",result.getError().toString());
                    }
                }
            });
        }
    }

    private void setRecycleView(String coins, Context context) {

        OkHttpClient okHttpClient = new OkHttpClient()
                .newBuilder().build();

        AndroidNetworking.initialize(context,okHttpClient);

        AndroidNetworking.setParserFactory(new GsonParserFactory());

        AndroidNetworking.get("https://api.coingecko.com/api/v3/coins/markets?x_cg_demo_api_key=&vs_currency=usd&ids=&sparkline=false&locale=en")
                .addQueryParameter("x_cg_demo_api_key",context.getString(R.string.COINGECKO_API_KEY))
                .addQueryParameter("ids",coins)
                .setTag("Watchlist")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONArray(new JSONArrayRequestListener() {
                    @Override
                    public void onResponse(JSONArray response) {
                        for (int i=0; i< response.length(); i++){
                            try {
                                JSONObject apiData = response.getJSONObject(i);

                                CryptoDataModel model = new CryptoDataModel();
                                model.setName(apiData.getString("name"));
                                model.setSymbol(apiData.getString("symbol"));
                                model.setImage(apiData.getString("image"));
                                model.setCurrent_price(apiData.getDouble("current_price"));
                                model.setPrice_change_percentage_24h(apiData.getDouble("price_change_percentage_24h"));
                                model.setType("usd");
                                model.setId(apiData.getString("id"));
                                cryptoData.add(model);

                            } catch (JSONException e) {
                                progressBar.setVisibility(View.INVISIBLE);
                                throw new RuntimeException(e);
                            }
                        }

                        adapter = new PortfolioAdapter(getContext(), cryptoData, portfolioData);
                        progressBar.setVisibility(View.INVISIBLE);
                        recyclerView.setAdapter(adapter);
                    }

                    @Override
                    public void onError(ANError anError) {
                        progressBar.setVisibility(View.INVISIBLE);
                        Log.e("ErrWatchlistApi", anError.getErrorBody());
                    }
                });
    }
}
