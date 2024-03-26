package com.example.majorproject.Fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.bumptech.glide.Glide;
import com.example.majorproject.R;
import com.example.majorproject.databinding.FragmentMarketDetailDetailsBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

import okhttp3.OkHttpClient;

public class MarketDetailDetailsFragment extends Fragment {

    FragmentMarketDetailDetailsBinding binding;
    String coin_id;
    String type;

    double coin_current_price;

    public MarketDetailDetailsFragment(String coin_id,String type) {
        // Required empty public constructor
        this.coin_id = coin_id;
        this.type = type;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentMarketDetailDetailsBinding.inflate(inflater, container, false);

        //call setCryptoDetail function
        setCryptoDetail(getContext());

        binding.etDetailCoin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!charSequence.toString().isEmpty()){
                    double totalCurrencyConvert = coin_current_price *  Double.parseDouble(binding.etDetailCoin.getText().toString());
                    binding.etDetailCurrency.setText(totalCurrencyConvert+"");
                }else {
                    binding.etDetailCurrency.setText("0");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        return binding.getRoot();
    }

    private void setCryptoDetail(Context context) {
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
                            Double coin_change_percentage_24h = objMarketData.getDouble("price_change_percentage_24h");

                            //Set image link with circular Imageview using glide
                            Glide.with(context)
                                    .load(coin_img_url)
                                    .into(binding.imgMarketDetailCrypto);

                            //Set coin name
                            binding.txtMarketDetailName.setText(coin_name);

                            //Set Type
                            //binding.txtMarketDetailCryptoType.setText("usd");

                            //Set Coin symbol
                            binding.txtMarketDetailCryptoSymbol.setText(coin_symbol);

                            //Set last 24h change price percentage and check the inDeVal is negative or positive then change Text color
                            if(coin_change_percentage_24h>=0.0){
                                binding.txtMarketDetailCryptoInDeVal.setTextColor(Color.GREEN);
                            }else{
                                binding.txtMarketDetailCryptoInDeVal.setTextColor(Color.RED);
                            }
                            binding.txtMarketDetailCryptoInDeVal.setText(String.format("%.2f",coin_change_percentage_24h)+"%");

                            //Set value
                            if(type.equals("inr")){
                                binding.txtMarketDetailCryptoValue.setText("₹" + String.format("%.2f", coin_current_price));
                            }else if (type.equals("btc")){
                                binding.txtMarketDetailCryptoValue.setText("₿" + String.format("%.2f", coin_current_price));
                            }else if (type.equals("eth")){
                                binding.txtMarketDetailCryptoValue.setText("Ξ" + String.format("%.2f", coin_current_price));
                            }else if (type.equals( "eru")){
                                binding.txtMarketDetailCryptoValue.setText("€" + String.format("%.2f", coin_current_price));
                            }else{
                                binding.txtMarketDetailCryptoValue.setText("$" + String.format("%.2f", coin_current_price));
                            }

                            //set converter coin detail
                            binding.txtDetailCoinName.setText(coin_name);
                            binding.txtDetailCoinSymbol.setText(coin_symbol);

                            Glide.with(context)
                                    .load(coin_img_url)
                                    .into(binding.imgDetailConverter);


                            //set converter curency
                            if(type.equals("inr")){
                                binding.imgDetailConverterCurrency.setImageDrawable(context.getResources().getDrawable(R.drawable.image_currency_ruppes,null));
                                binding.txtDetailCurrencyName.setText("Indian Rupees");
                                binding.txtDetailCurrencySymbol.setText("inr");
                            }else if(type.equals("btc")){
                                binding.imgDetailConverterCurrency.setImageDrawable(context.getResources().getDrawable(R.drawable.image_currency_bitcoin,null));
                                binding.txtDetailCurrencyName.setText("Bitcoin");
                                binding.txtDetailCurrencySymbol.setText("btc");
                            }else if(type.equals("eth")){
                                binding.imgDetailConverterCurrency.setImageDrawable(context.getResources().getDrawable(R.drawable.image_currency_etheream,null));
                                binding.txtDetailCurrencyName.setText("Ethereum");
                                binding.txtDetailCurrencySymbol.setText("eth");
                            }else if(type.equals("eur")){
                                binding.imgDetailConverterCurrency.setImageDrawable(context.getResources().getDrawable(R.drawable.image_currency_euro,null));
                                binding.txtDetailCurrencyName.setText("European Union Euro");
                                binding.txtDetailCurrencySymbol.setText("eur");
                            }else {
                                binding.imgDetailConverterCurrency.setImageDrawable(context.getResources().getDrawable(R.drawable.image_currency_dollar,null));
                                binding.txtDetailCurrencyName.setText("United States dollar");
                                binding.txtDetailCurrencySymbol.setText("usd");
                            }

                            binding.etDetailCurrency.setText(""+coin_current_price);


                            //set description
                            JSONObject objDesc = response.getJSONObject("description");

                            binding.txtMarketDetailDescription.setText(Html.fromHtml(objDesc.getString("en")));

                            //set all time high
                            JSONObject objAth = objMarketData.getJSONObject("ath");

                            if(type.equals("inr")){
                                binding.textAth.setText(objAth.getString("inr")+"₹");
                            }else if(type.equals("btc")){
                                binding.textAth.setText(objAth.getString("btc")+"₿");
                            }else if(type.equals("eth")){
                                binding.textAth.setText(objAth.getString("eth")+"Ξ");
                            }else if(type.equals("eur")){
                                binding.textAth.setText(objAth.getString("eur")+"€");
                            }else {
                                binding.textAth.setText(objAth.getString("usd")+"$");
                            }

                            //set price change 24h
                            JSONObject objHigh = objMarketData.getJSONObject("high_24h");
                            JSONObject objLow = objMarketData.getJSONObject("low_24h");

                            if(type.equals("inr")){
                                binding.textLast24hRange.setText(objLow.getString("inr")+"₹ - "+objHigh.getString("inr")+"₹");
                            }else if(type.equals("btc")){
                                binding.textLast24hRange.setText(objLow.getString("btc")+"₿ - "+objHigh.getString("btc")+"₿");
                            }else if(type.equals("eth")){
                                binding.textLast24hRange.setText(objLow.getString("eth")+"Ξ - "+objHigh.getString("eth")+"Ξ");
                            }else if(type.equals("eur")){
                                binding.textLast24hRange.setText(objLow.getString("eur")+"€ - "+objHigh.getString("eur")+"€");
                            }else {
                                binding.textLast24hRange.setText(objLow.getString("usd")+"$ - "+ objHigh.getString("usd")+"$");
                            }

                            //set volume
                            JSONObject objVol = objMarketData.getJSONObject("total_volume");

                            if(type.equals("inr")){
                                binding.textVolume.setText(objVol.getString("inr")+"₹");
                            }else if(type.equals("btc")){
                                binding.textVolume.setText(objVol.getString("btc")+"₿");
                            }else if(type.equals("eth")){
                                binding.textVolume.setText(objVol.getString("eth")+"Ξ");
                            }else if(type.equals("eur")){
                                binding.textVolume.setText(objVol.getString("eur")+"€");
                            }else {
                                binding.textVolume.setText(objVol.getString("usd")+"$");
                            }

                            //set All time low
                            JSONObject objAtl = objMarketData.getJSONObject("atl");

                            if(type.equals("inr")){
                                binding.textAtl.setText(objAtl.getString("inr")+"₹");
                            }else if(type.equals("btc")){
                                binding.textAtl.setText(objAtl.getString("btc")+"₿");
                            }else if(type.equals("eth")){
                                binding.textAtl.setText(objAtl.getString("eth")+"Ξ");
                            }else if(type.equals("eur")){
                                binding.textAtl.setText(objAtl.getString("eur")+"€");
                            }else {
                                binding.textAtl.setText(objAtl.getString("usd")+"$");
                            }

                            //set price live data descriptions
                            SpannableStringBuilder builder = new SpannableStringBuilder();

                            builder.append("The live ");
                            builder.append(coin_name);
                            builder.append(" price today is ");
                            builder.append(String.valueOf(coin_current_price));
                            builder.append(" ").append(type.toUpperCase());
                            builder.append(". We update our ");
                            builder.append(coin_symbol.toUpperCase(Locale.ROOT)).append(" to ").append(type.toUpperCase());
                            builder.append(" price in one minute. ").append(coin_name).append(" is ");

                            double inDeVal = objMarketData.getDouble("price_change_percentage_24h");

                            SpannableString inDeValString = new SpannableString(String.format("%.2f", inDeVal )+"%");

                            if(inDeVal>=0) {
                                inDeValString.setSpan(new ForegroundColorSpan(Color.GREEN), 0, inDeValString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                //marketCapChangeString.setSpan(new StyleSpan(Typeface.BOLD), 0, marketCapChangeString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            }else{
                                inDeValString.setSpan(new ForegroundColorSpan(Color.RED), 0, inDeValString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                //marketCapChangeString.setSpan(new StyleSpan(Typeface.BOLD), 0, marketCapChangeString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            }
                            builder.append(inDeValString);

                            builder.append(" in the last 24 hours. The current market cap ranking is #").append(response.getString("market_cap_rank"));

                            binding.txtMarketDetailPriceLive.setText(builder);

                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }

                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.e("ErrBuyPageApi",anError.getErrorBody().toString());
                    }
                });
    }

}