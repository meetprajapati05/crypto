package com.example.majorproject;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import com.example.majorproject.databinding.ActivityNewsDetailsBinding;

public class NewsDetails extends AppCompatActivity {
    ActivityNewsDetailsBinding binding ;
    String url;
    String source_name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNewsDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        url = getIntent().getStringExtra("url");
        source_name = getIntent().getStringExtra("source_name");

        binding.txtTitleNewsDescription.setText(source_name.toString());

        setWebView();

        binding.toolbarNews.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    public void setWebView()
    {
        binding.webNews.setWebViewClient(new WebViewClient());
        binding.webNews.getSettings().setJavaScriptEnabled(true);
        binding.webNews.clearFormData();
        binding.webNews.loadUrl(url);
        binding.webNews.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                view.loadUrl(request.getUrl().toString());
                return super.shouldOverrideUrlLoading(view, request);
            }
        });

    }
}