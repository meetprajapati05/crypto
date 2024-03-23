package com.example.majorproject;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.majorproject.databinding.FragmentBottomSheetBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BottomSheetFragment extends BottomSheetDialogFragment {
    private FragmentBottomSheetBinding binding;
    private String id;
    private String dateEdt;
    public BottomSheetFragment(String date) {
        // Required empty public constructor
        dateEdt = date;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentBottomSheetBinding.inflate(inflater, container, false);
        id = requireActivity().getIntent().getStringExtra("id");
        Toast.makeText(getContext(),"Id:"+id+"\nDate:"+dateEdt,Toast.LENGTH_SHORT).show();
        new APICallTask().execute();

        binding.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), SearchCurrency.class).putExtra("passTo","calander"));
            }
        });
        return binding.getRoot();

    }
    @SuppressLint("StaticFieldLeak")
    class APICallTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            try{
                OkHttpClient client = new OkHttpClient();
                String apiUrl = "https://api.coingecko.com/api/v3/coins/" + id + "/history?date=" + dateEdt;
                Request request = new Request.Builder()
                    .url(apiUrl)
                    .build();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    return response.body().string();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            if (response != null) {
                try {
                    JSONObject coinData = new JSONObject(response);
                    updateUIFromCoinData(coinData);
                } catch (JSONException e) {
                    e.printStackTrace();
                    showError("Error parsing JSON response");
                }
            } else {
                showError("API request failed");
            }
        }
        @SuppressLint("SetTextI18n")
        private void updateUIFromCoinData(JSONObject coinData) throws JSONException {
            String id = coinData.getString("id");
            String name = coinData.getString("name");
            String symbol = coinData.getString("symbol");
            double currentPrice = coinData.getJSONObject("market_data").getJSONObject("current_price").getDouble("usd");
            double marketCap = coinData.getJSONObject("market_data").getJSONObject("market_cap").getDouble("usd");
            double totalVolume = coinData.getJSONObject("market_data").getJSONObject("total_volume").getDouble("usd");

            binding.txtCryptoId.setText( id.toString());
            binding.txtCryptoName.setText(name);
            binding.txtCryptoSymbol.setText(symbol);
            binding.txtCryptoPrice.setText(formatCurrency(currentPrice)+"$");
            binding.txtmarketcap.setText(formatCurrency(marketCap));
            binding.txtvolume.setText(formatCurrency(totalVolume));

            Glide.with(getContext())
                    .load(coinData.getJSONObject("image").getString("small"))
                    .into(binding.imageCrypto);
        }

        private String formatCurrency(double value) {
            // You can use NumberFormat or DecimalFormat for proper formatting
            // Example: NumberFormat.getCurrencyInstance().format(value)
            return String.format("%.2f", value);  // Example: Two decimal places
        }
        private void showError(String message) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        }

    }

}
