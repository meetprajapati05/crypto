package com.example.majorproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.majorproject.databinding.ActivityEmailCheckBinding;

import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;

public class EmailCheck extends AppCompatActivity {
    ActivityEmailCheckBinding binding;
    App app;

    String APP_ID = "application-0-dbkcj";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEmailCheckBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Realm.init(this);
        app = new App(new AppConfiguration.Builder(APP_ID).build());

        binding.btnEmailVerifyNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.btnEmailVerifyNext.setVisibility(View.INVISIBLE);
                binding.progressEmailVerify.setVisibility(View.VISIBLE);
                boolean isValid = checkEmailEditText(binding.etEmail.getText().toString());
                if(isValid) {
                    app.getEmailPassword().sendResetPasswordEmailAsync(binding.etEmail.getText().toString(), new App.Callback<Void>() {
                        @Override
                        public void onResult(App.Result<Void> result) {
                            if (result.isSuccess()) {
                                binding.btnEmailVerifyNext.setVisibility(View.VISIBLE);
                                binding.progressEmailVerify.setVisibility(View.INVISIBLE);

                                //PASS EMAIL Vai SharedPreferences
                                SharedPreferences preferences = getSharedPreferences("EmailVerify", MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString("email", binding.etEmail.getText().toString());
                                editor.apply();

                                Toast.makeText(EmailCheck.this, "Send reset password email on this Email Account", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(EmailCheck.this, SingIn.class));
                            } else {
                                binding.btnEmailVerifyNext.setVisibility(View.VISIBLE);
                                binding.progressEmailVerify.setVisibility(View.INVISIBLE);
                                binding.errEmailVerify.setText(result.getError().getErrorMessage());
                                binding.errEmailVerify.setVisibility(View.VISIBLE);
                                Log.e("ResetPassEmail",result.getError().toString());
                            }
                        }
                    });
                }
            }
        });



        binding.etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.errEmailVerify.setVisibility(View.INVISIBLE);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private boolean checkEmailEditText(String email) {
        if(email.isEmpty()){
            binding.errEmailVerify.setText("Please enter email address");
            binding.errEmailVerify.setVisibility(View.VISIBLE);
            return false;
        }
        else if(!email.endsWith("@gmail.com")){
            binding.errEmailVerify.setText("Invalid! Email format");
            binding.errEmailVerify.setVisibility(View.VISIBLE);
            return false;
        }else{
            binding.errEmailVerify.setVisibility(View.INVISIBLE);
        }
        return true;
    }
}