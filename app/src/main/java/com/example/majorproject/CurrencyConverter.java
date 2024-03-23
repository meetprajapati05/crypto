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
import com.example.majorproject.Models.ConverterModel;
import com.example.majorproject.databinding.ActivityCurrencyConverterBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CurrencyConverter extends AppCompatActivity {

    ActivityCurrencyConverterBinding binding;
    String convert_from_value, convert_to_value, conversion_value;
    String[] currency_county = {
                "AED: United Arab Emirates Dirham (United Arab Emirates)",
                "AFN: Afghan Afghani (Afghanistan)",
                "ALL: Albanian Lek (Albania)",
                "AMD: Armenian Dram (Armenia)",
                "ANG: Netherlands Antillean Guilder (Netherlands Antilles)",
                "AOA: Angolan Kwanza (Angola)",
                "ARS: Argentine Peso (Argentina)",
                "AUD: Australian Dollar (Australia)",
                "AWG: Aruban Florin (Aruba)",
                "AZN: Azerbaijani Manat (Azerbaijan)",
                "BAM: Bosnia and Herzegovina Convertible Mark (Bosnia and Herzegovina)",
                "BBD: Barbadian Dollar (Barbados)",
                "BDT: Bangladeshi Taka (Bangladesh)",
                "BGN: Bulgarian Lev (Bulgaria)",
                "BHD: Bahraini Dinar (Bahrain)",
                "BIF: Burundian Franc (Burundi)",
                "BMD: Bermudian Dollar (Bermuda)",
                "BND: Brunei Dollar (Brunei)",
                "BOB: Bolivian Boliviano (Bolivia)",
                "BRL: Brazilian Real (Brazil)",
                "BSD: Bahamian Dollar (Bahamas)",
                "BTN: Bhutanese Ngultrum (Bhutan)",
                "BWP: Botswana Pula (Botswana)",
                "BYN: Belarusian Ruble (Belarus)",
                "BZD: Belize Dollar (Belize)",
                "CAD: Canadian Dollar (Canada)",
                "CDF: Congolese Franc (Democratic Republic of the Congo)",
                "CHF: Swiss Franc (Switzerland)",
                "CLP: Chilean Peso (Chile)",
                "CNY: Chinese Yuan (China)",
                "COP: Colombian Peso (Colombia)",
                "CRC: Costa Rican Colón (Costa Rica)",
                "CUP: Cuban Peso (Cuba)",
                "CVE: Cape Verdean Escudo (Cape Verde)",
                "CZK: Czech Koruna (Czech Republic)",
                "DJF: Djiboutian Franc (Djibouti)",
                "DKK: Danish Krone (Denmark)",
                "DOP: Dominican Peso (Dominican Republic)",
                "DZD: Algerian Dinar (Algeria)",
                "EGP: Egyptian Pound (Egypt)",
                "ERN: Eritrean Nakfa (Eritrea)",
                "ETB: Ethiopian Birr (Ethiopia)",
                "EUR: Euro (Eurozone)",
                "FJD: Fijian Dollar (Fiji)",
                "FKP: Falkland Islands Pound (Falkland Islands)",
                "FOK: Faroese Króna (Faroe Islands)",
                "GBP: British Pound Sterling (United Kingdom)",
                "GEL: Georgian Lari (Georgia)",
                "GGP: Guernsey Pound (Guernsey)",
                "GHS: Ghanaian Cedi (Ghana)",
                "GIP: Gibraltar Pound (Gibraltar)",
                "GMD: Gambian Dalasi (Gambia)",
                "GNF: Guinean Franc (Guinea)",
                "GTQ: Guatemalan Quetzal (Guatemala)",
                "GYD: Guyanese Dollar (Guyana)",
                "HKD: Hong Kong Dollar (Hong Kong)",
                "HNL: Honduran Lempira (Honduras)",
                "HRK: Croatian Kuna (Croatia)",
                "HTG: Haitian Gourde (Haiti)",
                "HUF: Hungarian Forint (Hungary)",
                "IDR: Indonesian Rupiah (Indonesia)",
                "ILS: Israeli New Shekel (Israel)",
                "IMP: Isle of Man Pound (Isle of Man)",
                "INR: Indian Rupee (India)",
                "IQD: Iraqi Dinar (Iraq)",
                "IRR: Iranian Rial (Iran)",
                "ISK: Icelandic Króna (Iceland)",
                "JEP: Jersey Pound (Jersey)",
                "JMD: Jamaican Dollar (Jamaica)",
                "JOD: Jordanian Dinar (Jordan)",
                "JPY: Japanese Yen (Japan)",
                "KES: Kenyan Shilling (Kenya)",
                "KGS: Kyrgyzstani Som (Kyrgyzstan)",
                "KHR: Cambodian Riel (Cambodia)",
                "KID: Kiribati Dollar (Kiribati)",
                "KMF: Comorian Franc (Comoros)",
                "KRW: South Korean Won (South Korea)",
                "KWD: Kuwaiti Dinar (Kuwait)",
                "KYD: Cayman Islands Dollar (Cayman Islands)",
                "KZT: Kazakhstani Tenge (Kazakhstan)",
                "LAK: Lao Kip (Laos)",
                "LBP: Lebanese Pound (Lebanon)",
                "LKR: Sri Lankan Rupee (Sri Lanka)",
                "LRD: Liberian Dollar (Liberia)",
                "LSL: Lesotho Loti (Lesotho)",
                "LYD: Libyan Dinar (Libya)",
                "MAD: Moroccan Dirham (Morocco)",
                "MDL: Moldovan Leu (Moldova)",
                "MGA: Malagasy Ariary (Madagascar)",
                "MKD: Macedonian Denar (North Macedonia)",
                "MMK: Burmese Kyat (Myanmar)",
                "MNT: Mongolian Tugrik (Mongolia)",
                "MOP: Macanese Pataca (Macau)",
                "MRU: Mauritanian Ouguiya (Mauritania)",
                "MUR: Mauritian Rupee (Mauritius)",
                "MVR: Maldivian Rufiyaa (Maldives)",
                "MWK: Malawian Kwacha (Malawi)",
                "MXN: Mexican Peso (Mexico)",
                "MYR: Malaysian Ringgit (Malaysia)",
                "MZN: Mozambican Metical (Mozambique)",
                "NAD: Namibian Dollar (Namibia)",
                "NGN: Nigerian Naira (Nigeria)",
                "NIO: Nicaraguan Córdoba (Nicaragua)",
                "NOK: Norwegian Krone (Norway)",
                "NPR: Nepalese Rupee (Nepal)",
                "NZD: New Zealand Dollar (New Zealand)",
                "OMR: Omani Rial (Oman)",
                "PAB: Panamanian Balboa (Panama)",
                "PEN: Peruvian Nuevo Sol (Peru)",
                "PGK: Papua New Guinean Kina (Papua New Guinea)",
                "PHP: Philippine Peso (Philippines)",
                "PKR: Pakistani Rupee (Pakistan)",
                "PLN: Polish Złoty (Poland)",
                "PYG: Paraguayan Guarani (Paraguay)",
                "QAR: Qatari Riyal (Qatar)",
                "RON: Romanian Leu (Romania)",
                "RSD: Serbian Dinar (Serbia)",
                "RUB: Russian Ruble (Russia)",
                "RWF: Rwandan Franc (Rwanda)",
                "SAR: Saudi Riyal (Saudi Arabia)",
                "SBD: Solomon Islands Dollar (Solomon Islands)",
                "SCR: Seychellois Rupee (Seychelles)",
                "SDG: Sudanese Pound (Sudan)",
                "SEK: Swedish Krona (Sweden)",
                "SGD: Singapore Dollar (Singapore)",
                "SHP: Saint Helena Pound (Saint Helena)",
                "SLE: Sierra Leonean Leone (Sierra Leone)",
                "SLL: Sierra Leonean Leone (Sierra Leone)",
                "SOS: Somali Shilling (Somalia)",
                "SRD: Surinamese Dollar (Suriname)",
                "SSP: South Sudanese Pound (South Sudan)",
                "STN: São Tomé and Príncipe Dobra (São Tomé and Príncipe)",
                "SYP: Syrian Pound (Syria)",
                "SZL: Swazi Lilangeni (Eswatini)",
                "THB: Thai Baht (Thailand)",
                "TJS: Tajikistani Somoni (Tajikistan)",
                "TMT: Turkmenistani Manat (Turkmenistan)",
                "TND: Tunisian Dinar (Tunisia)",
                "TOP: Tongan Pa'anga (Tonga)",
                "TRY: Turkish Lira (Turkey)",
                "TTD: Trinidad and Tobago Dollar (Trinidad and Tobago)",
                "TVD: Tuvaluan Dollar (Tuvalu)",
                "TWD: New Taiwan Dollar (Taiwan)",
                "TZS: Tanzanian Shilling (Tanzania)",
                "UAH: Ukrainian Hryvnia (Ukraine)",
                "UGX: Ugandan Shilling (Uganda)",
                "USD: United States Dollar (United States)",
                "UYU: Uruguayan Peso (Uruguay)",
                "UZS: Uzbekistani Som (Uzbekistan)",
                "VES: Venezuelan Bolívar (Venezuela)",
                "VND: Vietnamese Đồng (Vietnam)",
                "VUV: Vanuatu Vatu (Vanuatu)",
                "WST: Samoan Tala (Samoa)",
                "XAF: Central African CFA Franc (Central African Republic)",
                "XCD: East Caribbean Dollar (Organisation of Eastern Caribbean States)",
                "XDR: Special Drawing Rights (International Monetary Fund)",
                "XOF: West African CFA Franc (West African Economic and Monetary Union)",
                "XPF: CFP Franc (French Polynesia)",
                "YER: Yemeni Rial (Yemen)",
                "ZAR: South African Rand (South Africa)",
                "ZMW: Zambian Kwacha (Zambia)",
                "ZWL: Zimbabwean Dollar (Zimbabwe)"
};
    String[] currency = {"AED",
            "AFN",
            "ALL",
            "AMD",
            "ANG",
            "AOA",
            "ARS",
            "AUD",
            "AWG",
            "AZN",
            "BAM",
            "BBD",
            "BDT",
            "BGN",
            "BHD",
            "BIF",
            "BMD",
            "BND",
            "BOB",
            "BRL",
            "BSD",
            "BTN",
            "BWP",
            "BYN",
            "BZD",
            "CAD",
            "CDF",
            "CHF",
            "CLP",
            "CNY",
            "COP",
            "CRC",
            "CUP",
            "CVE",
            "CZK",
            "DJF",
            "DKK",
            "DOP",
            "DZD",
            "EGP",
            "ERN",
            "ETB",
            "EUR",
            "FJD",
            "FKP",
            "FOK",
            "GBP",
            "GEL",
            "GGP",
            "GHS",
            "GIP",
            "GMD",
            "GNF",
            "GTQ",
            "GYD",
            "HKD",
            "HNL",
            "HRK",
            "HTG",
            "HUF",
            "IDR",
            "ILS",
            "IMP",
            "INR",
            "IQD",
            "IRR",
            "ISK",
            "JEP",
            "JMD",
            "JOD",
            "JPY",
            "KES",
            "KGS",
            "KHR",
            "KID",
            "KMF",
            "KRW",
            "KWD",
            "KYD",
            "KZT",
            "LAK",
            "LBP",
            "LKR",
            "LRD",
            "LSL",
            "LYD",
            "MAD",
            "MDL",
            "MGA",
            "MKD",
            "MMK",
            "MNT",
            "MOP",
            "MRU",
            "MUR",
            "MVR",
            "MWK",
            "MXN",
            "MYR",
            "MZN",
            "NAD",
            "NGN",
            "NIO",
            "NOK",
            "NPR",
            "NZD",
            "OMR",
            "PAB",
            "PEN",
            "PGK",
            "PHP",
            "PKR",
            "PLN",
            "PYG",
            "QAR",
            "RON",
            "RSD",
            "RUB",
            "RWF",
            "SAR",
            "SBD",
            "SCR",
            "SDG",
            "SEK",
            "SGD",
            "SHP",
            "SLE",
            "SLL",
            "SOS",
            "SRD",
            "SSP",
            "STN",
            "SYP",
            "SZL",
            "THB",
            "TJS",
            "TMT",
            "TND",
            "TOP",
            "TRY",
            "TTD",
            "TVD",
            "TWD",
            "TZS",
            "UAH",
            "UGX",
            "USD",
            "UYU",
            "UZS",
            "VES",
            "VND",
            "VUV",
            "WST",
            "XAF",
            "XCD",
            "XDR",
            "XOF",
            "XPF",
            "YER",
            "ZAR",
            "ZMW",
            "ZWL"
    };
    ArrayList<ConverterModel> currency_data;
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

        Log.e("position","Currency:"+ currency.length + "\nCountry:"+currency_county.length);

        currency_data = new ArrayList<>();
        for(int i=0;i<currency_county.length;i++) {
            ConverterModel model = new ConverterModel();
            model.setContryWithCurrency(currency_county[i]);
            model.setCurrencyType(currency[i]);
            currency_data.add(model);
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

                ArrayAdapter<String> adapter = new ArrayAdapter<>(CurrencyConverter.this, android.R.layout.simple_list_item_1, currency_county);
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

                        String convert_from = null;

                        binding.convertFromDropdownMenu.setText(adapter.getItem(position));
                        for(int i = 0 ; i<currency_data.size(); i++) {
                            if(currency_data.get(i).getContryWithCurrency().equals(adapter.getItem(position).toString())) {
                                convert_from = currency_data.get(i).getCurrencyType();
                                Log.e("ConvertTo", convert_from);
                            }
                        }
                        convert_from_value = convert_from;
                        from_dialog.dismiss();
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

                ArrayAdapter<String> adapter = new ArrayAdapter<>(CurrencyConverter.this, android.R.layout.simple_list_item_1,currency_county);
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
                        String convert_to = null;
                        for(int i = 0 ; i<currency_data.size(); i++) {
                            if(currency_data.get(i).getContryWithCurrency().equals(adapter.getItem(position).toString())){
                                convert_to = currency_data.get(i).getCurrencyType();
                                Log.e("ConvertTo", convert_to);
                            }
                        }

                        convert_to_value = convert_to;

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