package com.example.majorproject;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.majorproject.databinding.ActivityCurrencyConverterBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CurrencyConverter extends AppCompatActivity {

    ActivityCurrencyConverterBinding binding;
    String convert_from_value, convert_to_value, conversion_value;
    String[] currency = {"AED", "AFN", "ALL", "AMD", "ANG", "AOA", "ARS", "AUD", "AWG", "AZN", "BAM", "BBD", "BDT", "BGN", "BHD", "BIF", "BMD", "BND", "BOB", "BRL", "BSD", "BTC", "BTN", "BWP", "BYN", "BYR", "BZD", "CAD", "CDF", "CHF", "CLF", "CLP", "CNY", "COP", "CRC", "CUC", "CUP", "CVE", "CZK", "DJF", "DKK", "DOP", "DZD", "EGP", "ERN", "ETB", "EUR", "FJD", "FKP", "GBP", "GEL", "GGP", "GHS", "GIP", "GMD", "GNF", "GTQ", "GYD", "HKD", "HNL", "HRK", "HTG", "HUF", "IDR", "ILS", "IMP", "INR", "IQD", "IRR", "ISK", "JEP", "JMD", "JOD", "JPY", "KES", "KGS", "KHR", "KMF", "KPW", "KRW", "KWD", "KYD", "KZT", "LAK", "LBP", "LKR", "LRD", "LSL", "LVL", "LYD", "MAD", "MDL", "MGA", "MKD", "MMK", "MNT", "MOP", "MRO", "MUR", "MVR", "MWK", "MXN", "MYR", "MZN", "NAD", "NGN", "NIO", "NOK", "NPR", "NZD", "OMR", "PAB", "PEN", "PGK", "PHP", "PKR", "PLN", "PYG", "QAR", "RON", "RSD", "RUB", "RWF", "SAR", "SBD", "SCR", "SDG", "SEK", "SGD", "SHP", "SLL", "SOS", "SRD", "STD", "SVC", "SYP", "SZL", "THB", "TJS", "TMT", "TND", "TOP", "TRY", "TTD", "TWD", "TZS", "UAH", "UGX", "USD", "UYU", "UZS", "VEF", "VND", "VUV", "WST", "XAF", "XAG", "XCD", "XDR", "XOF", "XPF", "YER", "ZAR", "ZMK", "ZMW", "ZWL"};
    ArrayList<String> arraylist;
    Dialog from_dialog, to_dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCurrencyConverterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbarCurrencyConverter.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        arraylist = new ArrayList<>();
        for(String i : currency) {
            arraylist.add(i);
        }

        binding.convertFromDropdownMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                from_dialog = new Dialog(CurrencyConverter.this);
                from_dialog.setContentView(R.layout.dailog_converter_select_currency);
                from_dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                from_dialog.show();

                EditText edittext = from_dialog.findViewById(R.id.edit_text);
                ListView listview = from_dialog.findViewById(R.id.list_view);

                ArrayAdapter<String> adapter = new ArrayAdapter<>(CurrencyConverter.this, android.R.layout.simple_list_item_1, arraylist);
                listview.setAdapter(adapter);

                edittext.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        adapter.getFilter().filter(s);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
                listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        binding.convertFromDropdownMenu.setText(adapter.getItem(position));
                        from_dialog.dismiss();
                        convert_from_value = adapter.getItem(position);
                    }
                });
            }
        });

        binding.convertToDropdownMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                to_dialog = new Dialog(CurrencyConverter.this);
                to_dialog.setContentView(R.layout.dailog_converter_select_currency);
                to_dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                to_dialog.show();

                EditText edittext = to_dialog.findViewById(R.id.edit_text);
                ListView listview = to_dialog.findViewById(R.id.list_view);

                ArrayAdapter<String> adapter = new ArrayAdapter<>(CurrencyConverter.this, android.R.layout.simple_list_item_1, arraylist);
                listview.setAdapter(adapter);

                edittext.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        adapter.getFilter().filter(s);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
                listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        binding.convertToDropdownMenu.setText(adapter.getItem(position));
                        to_dialog.dismiss();
                        convert_to_value = adapter.getItem(position);
                    }
                });
            }
        });

        binding.conversion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Double edit_amount_to_convert_value = Double.valueOf(binding.editAmountToConvertValue.getText().toString());
                    getConversionRate(convert_from_value, convert_to_value, edit_amount_to_convert_value);
                }
                catch(Exception e) {

                }
            }
        });
    }

    public String getConversionRate(String convert_from_value, String convert_to_value, Double edit_amount_to_convert_value) {
        RequestQueue requestqueue = Volley.newRequestQueue(this);
        String url = "https://v6.exchangerate-api.com/v6/e096aff25b90e1d2cdde200d/latest/"+convert_to_value;


        StringRequest stringrequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONObject jsonobject = null;
                try {
                    jsonobject = new JSONObject(response);

                    JSONObject conversion_rates = jsonobject.getJSONObject("conversion_rates");
                    double fromValueRate = conversion_rates.getDouble(convert_from_value);
                    double toValueRate = conversion_rates.getDouble(convert_to_value);

                    double convertedValue =Double.valueOf(binding.editAmountToConvertValue.getText().toString()) * fromValueRate;

                    binding.conversionRate.setText(String.valueOf(convertedValue));

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("ErrCalling",e.toString());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("ErrApi", error.toString());
            }
        });
        requestqueue.add(stringrequest);
        return null;
    }

}