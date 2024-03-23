package com.example.majorproject;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.majorproject.Adapters.NewsRescyclerAdapter;
import com.example.majorproject.Models.NewsModel;
import com.example.majorproject.databinding.ActivityNewsBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class News extends AppCompatActivity {

    ActivityNewsBinding binding;
    ArrayList<NewsModel> data;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNewsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        data = new ArrayList<>();

        setNews();

        binding.refreshNews.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                binding.recyclerNews.setAdapter(null);
                setNews();
                binding.refreshNews.setRefreshing(false);
            }
        });

        binding.toolbarNews.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }
    public void setNews(){
        binding.progressNews.setVisibility(View.VISIBLE);

        data.clear();

        String api ="https://min-api.cryptocompare.com/data/v2/news/?lang=EN";

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, api,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject object=new JSONObject(response);
                            JSONArray array = object.getJSONArray("Data");

                            for(int i = 0 ; i < array.length() ; i++){
                                JSONObject simpleobject=array.getJSONObject(i);

                                NewsModel singlemodel=new NewsModel();

                                singlemodel.setTitle(simpleobject.getString("title"));
                                singlemodel.setCategories(simpleobject.getString("categories"));
                                singlemodel.setImageurl(simpleobject.getString("imageurl"));
                                singlemodel.setTag(simpleobject.getString("tags"));
                                singlemodel.setPublish_on(simpleobject.getString("published_on"));

                                singlemodel.setUrl(simpleobject.getString("url"));

                                JSONObject sourceInfo = simpleobject.getJSONObject("source_info");
                                singlemodel.setSource_name(sourceInfo.getString("name"));

                                data.add(singlemodel);
                            }

                            binding.progressNews.setVisibility(View.GONE);
                            binding.recyclerNews.setAdapter(new NewsRescyclerAdapter(News.this,data));
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("api1", "onResponce:" + e.getMessage());

                            binding.progressNews.setVisibility(View.GONE);
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("api2","onError:" + error.getLocalizedMessage());
                binding.progressNews.setVisibility(View.GONE);
            }
        });
        queue.add(stringRequest);
    }
}