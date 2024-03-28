package com.example.majorproject.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.fragment.app.Fragment;

import com.example.majorproject.databinding.FragmentMarketDetailChartBinding;

public class MarketDetailChartFragment extends Fragment {

    FragmentMarketDetailChartBinding binding;
    String symbol,type;

    public MarketDetailChartFragment(String symbol, String type) {
        this.symbol = symbol;
        this.type = type;
    }

    public MarketDetailChartFragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentMarketDetailChartBinding.inflate(inflater, container, false);

        showChart(symbol, type);

        return binding.getRoot();
    }

    public void showChart(String symbol,String type){
        String chartUrl = "https://s.tradingview.com/widgetembed/?frameElementId=tradingview_76d87&symbol="+ symbol + type +"&interval=D&hidesidetoolbar=1&hidetoptoolbar=1&symboledit=1&saveimage=1&toolbarbg=FFFFFF&studies=[]&hideideas=1&theme=light&style=1&timezone=Etc%2FUTC&studies_overrides={}&overrides={}&enabled_features=[]&disabled_features=[]&locale=en&utm_source=coinmarketcap.com&utm_medium=widget&utm_campaign=chart";
        WebSettings webSettings = binding.marketDetailChart.getSettings();
        webSettings.setJavaScriptEnabled(true);
        binding.marketDetailChart.clearFormData();
        binding.marketDetailChart.loadUrl(chartUrl);
        binding.marketDetailChart.setWebContentsDebuggingEnabled(true);
        binding.marketDetailChart.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                view.loadUrl(request.toString());
                return super.shouldOverrideUrlLoading(view, request);
            }
        });

    }
}