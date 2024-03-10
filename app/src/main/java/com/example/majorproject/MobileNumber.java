package com.example.majorproject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.majorproject.databinding.ActivityMobileNumberBinding;

import io.realm.mongodb.App;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoDatabase;


public class MobileNumber extends AppCompatActivity {
    ActivityMobileNumberBinding binding;
    String name, email, password, phone_no, country_name_code;

    String phoneNO;
    boolean editNo;

    App app;
    MongoDatabase mongoDatabase;
    MongoClient mongoClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMobileNumberBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Initialize values get data for both SingUp & VerifyOtp
        name = getIntent().getStringExtra("name");
        email = getIntent().getStringExtra("email");
        password = getIntent().getStringExtra("password");


        //get data from VerifyOtp
        country_name_code = getIntent().getStringExtra("country_short_name");
        phone_no = getIntent().getStringExtra("phone_no");

        binding.countryCodePicker.registerCarrierNumberEditText(binding.etMobileNumber);

        //Check mobile number valid nor note
        checkMobileNumber();

        //if get return to the VerifyOtp then this method is called
        setEtMobileNoAndCountryCodePicker();
    }

    public void setEtMobileNoAndCountryCodePicker() {
        if(phone_no != null && country_name_code != null){
            binding.countryCodePicker.setCountryForNameCode(country_name_code);
            binding.etMobileNumber.setText(phone_no);
        }
    }


    public void checkMobileNumber(){
        binding.etMobileNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.countryCodePicker.setPhoneNumberValidityChangeListener(isValidNumber -> {
                    if(isValidNumber){
                        binding.imgPhoneCheck.setImageResource(R.drawable.baseline_check_24);
                        binding.imgPhoneCheck.setVisibility(View.VISIBLE);
                    }
                    else {
                        binding.imgPhoneCheck.setImageResource(R.drawable.baseline_close_24);
                        binding.imgPhoneCheck.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    @SuppressLint({"ResourceType", "SetTextI18n"})
    public void btnGetOTP(View view) {
        binding.mobileNumberProgress.setVisibility(View.VISIBLE);
        if(binding.etMobileNumber.getText().toString().isEmpty()) {
            binding.mobileNumberProgress.setVisibility(View.INVISIBLE);
            binding.errMobileNumber.setText("Required");
            binding.errMobileNumber.setVisibility(View.VISIBLE);
        }
        else{
            binding.countryCodePicker.setPhoneNumberValidityChangeListener(isValidNumber -> {
                if(isValidNumber){
                    if(!name.isEmpty() && !email.isEmpty() && !password.isEmpty()) {
                        binding.mobileNumberProgress.setVisibility(View.INVISIBLE);
                        binding.errMobileNumber.setVisibility(View.INVISIBLE);
                        Intent intent = new Intent(MobileNumber.this, VerifyOtp.class);
                        intent.putExtra("country_short_name", binding.countryCodePicker.getSelectedCountryNameCode());
                        intent.putExtra("country_code", binding.countryCodePicker.getSelectedCountryCodeWithPlus());
                        intent.putExtra("phone_no", binding.etMobileNumber.getText().toString());
                        intent.putExtra("name", name);
                        intent.putExtra("email", email);
                        intent.putExtra("password", password);
                        startActivity(intent);
                    }
                }
                else {
                    binding.errMobileNumber.setText("Invalid Number!");
                    binding.errMobileNumber.setVisibility(View.VISIBLE);
                    binding.mobileNumberProgress.setVisibility(View.INVISIBLE);
                }
            });
        }
        binding.etMobileNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.toString().isEmpty()){
                    binding.errMobileNumber.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }
}