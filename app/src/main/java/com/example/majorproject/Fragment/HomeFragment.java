package com.example.majorproject.Fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.gsonparserfactory.GsonParserFactory;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.example.majorproject.Adapters.MarketAdapter;
import com.example.majorproject.Adapters.NewsHomeAdapter;
import com.example.majorproject.Adapters.WatchlistHomeAdapter;
import com.example.majorproject.AddCommunity;
import com.example.majorproject.CurrencyConverter;
import com.example.majorproject.Models.CryptoDataModel;
import com.example.majorproject.Models.NewsModel;
import com.example.majorproject.News;
import com.example.majorproject.R;
import com.example.majorproject.SearchCurrency;
import com.example.majorproject.Wallet;
import com.example.majorproject.databinding.FragmentHomeBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import org.bson.Document;
import org.json.JSONArray;
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

public class HomeFragment extends Fragment {

    FragmentHomeBinding binding;
    private double backPressedTime;
    ArrayList<CryptoDataModel> data;
    ArrayList<CryptoDataModel> trendinData;
    ArrayList<NewsModel>  newsData;
    String _id;
    App app;
    String coins;
    MongoClient client;
    MongoDatabase database;
    public HomeFragment() {
        // Required empty public constructor
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false);

        data = new ArrayList<>();
        trendinData = new ArrayList<>();
        newsData = new ArrayList<>();

        // Init realm and app and clicnt and database
        Realm.init(getContext());
        app = new App(new AppConfiguration.Builder(getContext().getString(R.string.MONGO_APP_ID)).build());
        client = app.currentUser().getMongoClient(getContext().getString(R.string.MONGO_DB_SERVICE_NAME));
        database = client.getDatabase(getContext().getString(R.string.MONGO_DATABASE_NAME));

        setGlobleMarketCap(getContext());

        setBalance(getContext());

        setWatchlistRecycler(getContext());

        setTrending(getContext());

        setNews(getContext());

        //set maerquee text
        binding.txtGlobleMarketCap.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        binding.txtGlobleMarketCap.setSelected(true);

        binding.btnHomeAddMoney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), Wallet.class).putExtra("_id", _id));
            }
        });

        binding.btnHomeMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.homeDrawerLayout.open();
            }
        });

        binding.navigationHome.bringToFront();
        binding.navigationHome.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                int ItemId = item.getItemId();

                if (ItemId==R.id.drawerOptHome){
                    binding.homeDrawerLayout.close();
                }
                if(ItemId== R.id.drawerOptCommunity){
                    startActivity(new Intent(getActivity(), AddCommunity.class));
                }
                if(ItemId== R.id.drawerOptNews){
                    startActivity(new Intent(getActivity(), News.class));
                }
                if(ItemId== R.id.drawerOptConverter){
                    startActivity(new Intent(getActivity(), CurrencyConverter.class));
                }
                if(ItemId == R.id.drawerOptCalander){
                    startActivity(new Intent(getActivity(), SearchCurrency.class).putExtra("passTo", "Calander"));
                }

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(ItemId!=R.id.drawerOptHome){
                            binding.homeDrawerLayout.close();
                        }
                    }
                },1000);

                return false;
            }
        });

        //Button News See all click event
        binding.btnHomeNewsSeeAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.btnHomeNewsSeeAll.setTextColor(getContext().getColor(R.color.light_green));
                startActivity(new Intent(getContext(), News.class));
                binding.btnHomeNewsSeeAll.setTextColor(Color.GREEN);
            }
        });

        //Button view all market
        binding.btnHomeTrendingSeeAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottomNavView);

                bottomNavigationView.setSelectedItemId(R.id.bottomOptMarket);
            }
        });

        //Handel backpress method on this and onBackPress drawer is open then first close the drawer and second backpress fragment close
        backPressed(requireActivity());

        return  binding.getRoot();
    }

    private void setBalance(Context context){

        MongoCollection<Document> collection = database.getCollection(context.getString(R.string.MONGO_DB_USER_COLLECTION));

        collection.findOne(new Document("user_id", app.currentUser().getId().toString())).getAsync(new App.Callback<Document>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResult(App.Result<Document> result) {
                if(result.isSuccess()){
                    if(result.get()!=null){

                        _id = result.get().getObjectId("_id").toString();

                        double CurrentBalance = result.get().getDouble("balance");
                        int balanceInInt = (int) CurrentBalance;
                        double blancePointZero = CurrentBalance - balanceInInt;
                        double pointBalance = blancePointZero * 100;
                        int pointVal = (int) pointBalance;

                        binding.txtHomeBalance.setText("$"+ balanceInInt);
                        binding.txtHomeBalancePoint.setText("."+pointVal);

                        //set user name field
                        binding.txtHomeName.setText(result.get().getString("name"));
                    }
                }else{
                    Log.e("ErrHomeFragBalance", result.getError().toString());
                }
            }
        });

    }

    private void backPressed(Activity context){
        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if(binding.homeDrawerLayout.isOpen()){
                    binding.homeDrawerLayout.close();
                }else {
                    if (backPressedTime + 2000 > System.currentTimeMillis()) {
                        setEnabled(false);
                        context.onBackPressed();
                    } else {
                        Toast.makeText(context, "Press again to exit", Toast.LENGTH_SHORT).show();
                    }
                    backPressedTime = System.currentTimeMillis();
                }
            }
        });
    }

    public void filterApiWithWatchlistData(String coins, Context context){
        OkHttpClient okHttpClient = new OkHttpClient()
                .newBuilder().build();

        AndroidNetworking.initialize(context,okHttpClient);

        AndroidNetworking.setParserFactory(new GsonParserFactory());

        AndroidNetworking.get("https://api.coingecko.com/api/v3/coins/markets?x_cg_demo_api_key=&vs_currency=usd&ids=&sparkline=false&locale=en")
                .addQueryParameter("x_cg_demo_api_key", context.getString(R.string.COINGECKO_API_KEY))
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
                                data.add(model);

                            } catch (JSONException e) {
                                binding.progressHomeWatchlist.setVisibility(View.GONE);
                                Log.e("ErrHomeApiCalling", e.getMessage().toString());
                            }
                        }

                        //set layout in recycerView
                        LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
                        binding.recyclerHomeWatchlist.setLayoutManager(layoutManager);

                        WatchlistHomeAdapter adapter = new WatchlistHomeAdapter(getContext(), data);
                        binding.progressHomeWatchlist.setVisibility(View.GONE);
                        binding.recyclerHomeWatchlist.setAdapter(adapter);
                    }

                    @Override
                    public void onError(ANError anError) {
                        binding.progressHomeWatchlist.setVisibility(View.GONE);
                        Log.e("ErrWatchlistApi", anError.getErrorBody());
                    }
                });
    }

    public void setWatchlistRecycler(Context context) {

        binding.progressHomeWatchlist.setVisibility(View.VISIBLE);

        MongoCollection<Document> collection = database.getCollection(context.getString(R.string.MONGO_DB_USER_COLLECTION));


        Document document = new Document("user_id", app.currentUser().getId().toString());
        collection.find(document)
                .first()
                .getAsync(new App.Callback<Document>() {
                    @Override
                    public void onResult(App.Result<Document> result) {
                        if (result.isSuccess()) {
                            if (result.get() != null) {
                                data.clear();
                                List<Document> watchlist = result.get().getList("watchlist", Document.class);
                                if (watchlist != null) {
                                    binding.msgHomeWatchlistEmpty.setVisibility(View.GONE);
                                    for (Document watchlistItem : watchlist) {
                                        String coin_name = watchlistItem.getString("coin_name");
                                        String coin_symbol = watchlistItem.getString("coin_symbol");
                                        String id = watchlistItem.getString("coin_id");
                                        //Filter api and get data that are added in watchlist
                                        if (coins != null) {
                                            coins = coins + "," + id;
                                        } else {
                                            coins = id;
                                        }
                                    }
                                    if (coins != null) {
                                        filterApiWithWatchlistData(coins, context);
                                    }else {
                                        binding.progressHomeWatchlist.setVisibility(View.GONE);
                                        binding.msgHomeWatchlistEmpty.setVisibility(View.VISIBLE);
                                    }
                                }else {
                                    binding.progressHomeWatchlist.setVisibility(View.GONE);
                                    binding.msgHomeWatchlistEmpty.setVisibility(View.VISIBLE);
                                }
                            }
                        } else {
                            binding.progressHomeWatchlist.setVisibility(View.GONE);
                            Log.e("ErrWatchlistDatabase", result.getError().toString());
                        }
                    }
                });
    }

    private void setTrending(Context context){
        binding.progressHomeTreanding.setVisibility(View.VISIBLE);

        AndroidNetworking.initialize(context);

        OkHttpClient okHttpClient = new OkHttpClient()
                .newBuilder().build();

        AndroidNetworking.initialize(context,okHttpClient);

        AndroidNetworking.setParserFactory(new GsonParserFactory());

        AndroidNetworking.get("https://api.coingecko.com/api/v3/search/trending?x_cg_demo_api_key=")
                .addQueryParameter("x_cg_demo_api_key", context.getString(R.string.COINGECKO_API_KEY))
                .setTag("Trending")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray coins = response.getJSONArray("coins");
                            for (int i=0;i<coins.length();i++){
                                JSONObject arrayObj = coins.getJSONObject(i);

                                JSONObject objItem = arrayObj.getJSONObject("item");
                                CryptoDataModel model = new CryptoDataModel();
                                model.setName(objItem.getString("name"));
                                model.setSymbol(objItem.getString("symbol"));
                                model.setImage(objItem.getString("large"));

                                JSONObject dataObj = objItem.getJSONObject("data");

                                try {
                                    String price = dataObj.getString("price").toString().replaceAll("[^0-9.]", "");
                                    model.setCurrent_price(Double.parseDouble(price));
                                }catch (NumberFormatException e){
                                    model.setCurrent_price(0.001);
                                }

                                JSONObject price_change_percentage_24hObj = dataObj.getJSONObject("price_change_percentage_24h");

                                model.setPrice_change_percentage_24h(price_change_percentage_24hObj.getDouble("usd"));

                                model.setType("usd");
                                model.setId(objItem.getString("id"));
                                trendinData.add(model);
                            }
                            LinearLayoutManager trendingLayout = new LinearLayoutManager(context){
                                @Override
                                public boolean canScrollVertically() {
                                    return false;
                                }
                            };
                            binding.recyclerHomeTreanding.setLayoutManager(trendingLayout);
                            binding.progressHomeTreanding.setVisibility(View.GONE);
                            MarketAdapter adapter = new MarketAdapter(context, trendinData);
                            binding.recyclerHomeTreanding.setAdapter(adapter);

                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        binding.progressHomeTreanding.setVisibility(View.GONE);
                        Log.e("ErrApiTrending", anError.getErrorDetail().toString());
                    }
                });

    }

    private void setNews(Context context){
        binding.progressHomeNews.setVisibility(View.VISIBLE);

        data.clear();

        String api ="https://min-api.cryptocompare.com/data/v2/news/?lang=EN";

        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, api,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject object=new JSONObject(response);
                            JSONArray array = object.getJSONArray("Data");

                            for(int i = 0 ; i < array.length() ; i++){
                                if(i<10) {
                                    JSONObject simpleobject = array.getJSONObject(i);

                                    NewsModel singlemodel = new NewsModel();

                                    singlemodel.setTitle(simpleobject.getString("title"));
                                    singlemodel.setCategories(simpleobject.getString("categories"));
                                    singlemodel.setImageurl(simpleobject.getString("imageurl"));
                                    singlemodel.setTag(simpleobject.getString("tags"));
                                    singlemodel.setPublish_on(simpleobject.getString("published_on"));

                                    singlemodel.setUrl(simpleobject.getString("url"));

                                    JSONObject sourceInfo = simpleobject.getJSONObject("source_info");
                                    singlemodel.setSource_name(sourceInfo.getString("name"));

                                    newsData.add(singlemodel);
                                }
                            }
                            LinearLayoutManager newsLayout = new LinearLayoutManager(context){
                                @Override
                                public boolean canScrollVertically() {
                                    return false;
                                }
                            };
                            binding.recyclerHomeNews.setLayoutManager(newsLayout);

                            binding.recyclerHomeNews.setHasFixedSize(true);
                            binding.progressHomeNews.setVisibility(View.GONE);
                            binding.recyclerHomeNews.setAdapter(new NewsHomeAdapter(context,newsData));
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("api1", "onResponce:" + e.getMessage());

                            binding.progressHomeNews.setVisibility(View.GONE);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("api2","onError:" + error.getLocalizedMessage());
                binding.progressHomeNews.setVisibility(View.GONE);
            }
        });
        queue.add(stringRequest);
    }

    private void setGlobleMarketCap(Context context){

        AndroidNetworking.initialize(context);

        OkHttpClient okHttpClient = new OkHttpClient()
                .newBuilder().build();

        AndroidNetworking.initialize(context,okHttpClient);

        AndroidNetworking.setParserFactory(new GsonParserFactory());

        AndroidNetworking.get("https://api.coingecko.com/api/v3/global?x_cg_demo_api_key=")
                .addQueryParameter("x_cg_demo_api_key", context.getString(R.string.COINGECKO_API_KEY))
                .setTag("Trending")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject dataObj = response.getJSONObject("data");

                            String coins = dataObj.getString("active_cryptocurrencies");
                            String exchange = dataObj.getString("markets");

                            JSONObject marketCapObj = dataObj.getJSONObject("total_market_cap");
                            double marketCap = marketCapObj.getDouble("usd");

                            JSONObject volumeObj = dataObj.getJSONObject("total_volume");
                            String volume = volumeObj.getString("usd");


                            JSONObject domainceObj = dataObj.getJSONObject("market_cap_percentage");
                            String domainceBtc = domainceObj.getString("btc");
                            String domainceEth = domainceObj.getString("eth");
                            String domainceUsdt = domainceObj.getString("usdt");
                            String domainceBnb = domainceObj.getString("bnb");
                            String domainceSol = domainceObj.getString("sol");
                            String domainceXrp = domainceObj.getString("xrp");
                            String domainceSteth = domainceObj.getString("steth");
                            String domainceUsdc = domainceObj.getString("usdc");
                            String domainceDoge = domainceObj.getString("doge");
                            String domainceAda = domainceObj.getString("ada");

                            double marketCapChange = dataObj.getDouble("market_cap_change_percentage_24h_usd");

                            SpannableStringBuilder builder = new SpannableStringBuilder();

                            builder.append("Coins: ");
                            SpannableString coinString = new SpannableString(coins);
                            coinString.setSpan(new ForegroundColorSpan(Color.BLACK), 0, coinString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            coinString.setSpan(new StyleSpan(Typeface.BOLD), 0, coinString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            builder.append(coinString);

                            builder.append("   Exchange: ");
                            SpannableString exchangeString = new SpannableString(exchange);
                            exchangeString.setSpan(new ForegroundColorSpan(Color.BLACK), 0, coinString.length()-1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            exchangeString.setSpan(new StyleSpan(Typeface.BOLD), 0, coinString.length()-1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            builder.append(exchangeString);

                            builder.append("   Market Cap: ");

                            // Append market cap with black color and bold style
                            SpannableString marketCapString = new SpannableString(String.format("%.2f", marketCap) + "$");
                            marketCapString.setSpan(new ForegroundColorSpan(Color.BLACK), 0, marketCapString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            marketCapString.setSpan(new StyleSpan(Typeface.BOLD), 0, marketCapString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            builder.append(marketCapString);

                            // Append Market Cap Change 24h
                            builder.append("   Market Cap Change 24h(%): ");
                            SpannableString marketCapChangeString = new SpannableString(String.format("%.2f", marketCapChange) + "%");

                            if(marketCapChange>=0) {
                                marketCapChangeString.setSpan(new ForegroundColorSpan(Color.GREEN), 0, marketCapChangeString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                //marketCapChangeString.setSpan(new StyleSpan(Typeface.BOLD), 0, marketCapChangeString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            }else{
                                marketCapChangeString.setSpan(new ForegroundColorSpan(Color.RED), 0, marketCapChangeString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                //marketCapChangeString.setSpan(new StyleSpan(Typeface.BOLD), 0, marketCapChangeString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            }
                            builder.append(marketCapChangeString);

                            // Append 24h Volume
                            builder.append("   24h Volume: ");
                            SpannableString volumeString = new SpannableString(String.format("%.2f", Double.parseDouble(volume))+"$");
                            volumeString.setSpan(new ForegroundColorSpan(Color.BLACK), 0, volumeString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            volumeString.setSpan(new StyleSpan(Typeface.BOLD), 0, volumeString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            builder.append(volumeString);

                            // Append Dominance
                            builder.append("   Dominance: ");
                            String[] currencies = {"BTC", "ETH", "USDT", "BNB", "SOL"};
                            String[] dominanceValues = {domainceBtc, domainceEth, domainceUsdt, domainceBnb, domainceSol};
                            for (int i = 0; i < currencies.length; i++) {
                                SpannableString currenciesString = new SpannableString(currencies[i]);
                                currenciesString.setSpan(new ForegroundColorSpan(Color.BLACK), 0, currenciesString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                currenciesString.setSpan(new StyleSpan(Typeface.BOLD), 0, currenciesString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                builder.append(currenciesString + " ");

                                SpannableString dominanceString = new SpannableString(String.format("%.2f", Double.parseDouble(dominanceValues[i])) + "% ");
                                if(Double.parseDouble(dominanceValues[i])>=0) {
                                    dominanceString.setSpan(new ForegroundColorSpan(Color.GREEN), 0, dominanceString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                }else{
                                    dominanceString.setSpan(new ForegroundColorSpan(Color.RED), 0, dominanceString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                }
                                builder.append(dominanceString);
                            }

                            // Set the text with different colors and styles
                            binding.txtGlobleMarketCap.setText(builder);

                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }

                    }
                    @Override
                    public void onError(ANError anError) {

                    }
                });
    }
}