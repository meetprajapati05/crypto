package com.example.majorproject.Fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
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
import com.example.majorproject.Adapters.MarketAdapter;
import com.example.majorproject.Models.CryptoDataModel;
import com.example.majorproject.R;
import com.example.majorproject.SearchCurrency;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.OkHttpClient;

public class MarketFragment extends Fragment {

    public MarketFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    RecyclerView recyclerView;

    LottieAnimationView progressBar,progressLoadMore;
    NestedScrollView scrollView;
    ArrayList<CryptoDataModel> data;
    AppCompatButton btnUSD,btnINR,btnBTC,btnETH,btnDOT;
    TextView title;
    ImageView btnSearch;
    int page = 1;
    MarketAdapter adapter;

    String type= "usd";

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_market, container, false);

        recyclerView = view.findViewById(R.id.marketRecycler);
        progressBar = view.findViewById(R.id.marketLoadingAnimation);
        scrollView = view.findViewById(R.id.marketNestedScrollView);
        progressLoadMore = view.findViewById(R.id.progressLoadMore);
        btnUSD = view.findViewById(R.id.btnMarketUSD);
        btnINR = view.findViewById(R.id.btnMarketINR);
        btnBTC = view.findViewById(R.id.btnMarketBTC);
        btnETH = view.findViewById(R.id.btnMarketETH);
        btnDOT = view.findViewById(R.id.btnMarketDOT);

        btnSearch = view.findViewById(R.id.btnSearch);
        title = view.findViewById(R.id.marketTitle);

        data = new ArrayList<>();

        //Set api data on create fragment
        progressBar.setVisibility(View.VISIBLE);
        setRecyclerViewVolley(page,type,getContext());

        //Load next page data on first page data over
        scrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(@NonNull NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if(scrollY == v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight()){
                    if(recyclerView.getAdapter()!=null) {
                        ++page;
                        progressLoadMore.setVisibility(View.VISIBLE);
                        setRecyclerViewVolley(page, type,getContext());
                    }
                }
            }
        });

        //Pass Activity when button search is called
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), SearchCurrency.class));
            }
        });

        //Set Button click event
        btnUSD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setButtonColorOnClicked(btnUSD);
                recyclerView.setAdapter(null);
                progressBar.setVisibility(View.VISIBLE);
                progressLoadMore.setVisibility(View.GONE);
                type="usd";
                data.clear();
                page=1;
                setRecyclerViewVolley(page,type,getContext());
            }
        });
        btnINR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setButtonColorOnClicked(btnINR);
                recyclerView.setAdapter(null);
                progressBar.setVisibility(View.VISIBLE);
                progressLoadMore.setVisibility(View.INVISIBLE);
                data.clear();
                page=1;
                type="inr";
                setRecyclerView(page,type,getContext());
            }
        });
        btnBTC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setButtonColorOnClicked(btnBTC);
                recyclerView.setAdapter(null);
                type="btc";
                progressBar.setVisibility(View.VISIBLE);
                progressLoadMore.setVisibility(View.INVISIBLE);
                data.clear();
                page=1;
                setRecyclerView(page,type,getContext());
            }
        });
        btnETH.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setButtonColorOnClicked(btnETH);
                recyclerView.setAdapter(null);
                type="eth";
                progressBar.setVisibility(View.VISIBLE);
                progressLoadMore.setVisibility(View.INVISIBLE);
                data.clear();
                page=1;
                setRecyclerView(page,type,getContext());
            }
        });
        btnDOT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setButtonColorOnClicked(btnDOT);
                recyclerView.setAdapter(null);
                type="eur";
                progressBar.setVisibility(View.VISIBLE);
                progressLoadMore.setVisibility(View.INVISIBLE);
                data.clear();
                page=1;
                setRecyclerView(page,type,getContext());
            }
        });


        return view;
    }

    private void setRecyclerView(int page, String type, Context context) {
        OkHttpClient okHttpClient = new OkHttpClient()
                .newBuilder().build();
        AndroidNetworking.initialize(context,okHttpClient);

        AndroidNetworking.setParserFactory(new GsonParserFactory());

        AndroidNetworking.get("https://api.coingecko.com/api/v3/coins/markets?x_cg_demo_api_key=&vs_currency=&order=market_cap_desc&per_page=&page=&sparkline=false")
                .addQueryParameter("vs_currency", type)
                .addQueryParameter("x_cg_demo_api_key",context.getString(R.string.COINGECKO_API_KEY))
                .addQueryParameter("per_page","100")
                .addQueryParameter("page", String.valueOf(page))
                .setTag("Market")
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
                                model.setType(type);
                                model.setId(apiData.getString("id"));

                                data.add(model);

                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        }

                        adapter = new MarketAdapter(context, data);
                        progressBar.setVisibility(View.INVISIBLE);
                        progressLoadMore.setVisibility(View.INVISIBLE);
                        recyclerView.setAdapter(adapter);
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.e("ErrMarketApi", anError.getErrorBody());
                    }
                });
    }

    private void setRecyclerViewVolley(int page, String type, Context context) {
        String apiurl = "https://api.coingecko.com/api/v3/coins/markets?x_cg_demo_api_key="+context.getString(R.string.COINGECKO_API_KEY)+"&vs_currency="+ type +"&order=market_cap_desc&per_page=100&page=" + page +"&sparkline=false";
        StringRequest request = new StringRequest(Request.Method.GET, apiurl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    for(int i =0;i< jsonArray.length();i++){
                        JSONObject apiData = jsonArray.getJSONObject(i);

                        CryptoDataModel model = new CryptoDataModel();
                        model.setName(apiData.getString("name"));
                        model.setSymbol(apiData.getString("symbol"));
                        model.setImage(apiData.getString("image"));
                        model.setCurrent_price(apiData.getDouble("current_price"));
                        model.setPrice_change_percentage_24h(apiData.getDouble("price_change_percentage_24h"));
                        model.setType(type);
                        model.setId(apiData.getString("id"));
                        data.add(model);
                    }
                    adapter = new MarketAdapter(context, data);
                    progressBar.setVisibility(View.INVISIBLE);
                    progressLoadMore.setVisibility(View.INVISIBLE);
                    recyclerView.setAdapter(adapter);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(request);
    }


    @SuppressLint("ResourceAsColor")
    public void setButtonColorOnClicked(Button clickedButton){
        //Set all button background unselected
        btnUSD.setBackgroundResource(R.drawable.market_unselected_button_backgroug);
        btnUSD.setTextColor(ContextCompat.getColor(getContext(),R.color.light_green));
        btnINR.setBackgroundResource(R.drawable.market_unselected_button_backgroug);
        btnINR.setTextColor(ContextCompat.getColor(getContext(),R.color.light_green));
        btnBTC.setBackgroundResource(R.drawable.market_unselected_button_backgroug);
        btnBTC.setTextColor(ContextCompat.getColor(getContext(),R.color.light_green));
        btnETH.setBackgroundResource(R.drawable.market_unselected_button_backgroug);
        btnETH.setTextColor(ContextCompat.getColor(getContext(),R.color.light_green));
        btnDOT.setBackgroundResource(R.drawable.market_unselected_button_backgroug);
        btnDOT.setTextColor(ContextCompat.getColor(getContext(),R.color.light_green));

        clickedButton.setBackgroundResource(R.drawable.market_selected_button_background);
        clickedButton.setTextColor(ContextCompat.getColor(getContext(),R.color.white));
    }


}