package com.example.majorproject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.majorproject.databinding.ActivityVerifyOtpBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import org.bson.Document;

import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;
import io.realm.mongodb.mongo.result.InsertOneResult;

public class VerifyOtp extends AppCompatActivity {
    ActivityVerifyOtpBinding binding;
    String phone_no;
    String country_code, country_short_name;
    String number;
    String userObjId;

    String name,email,password;

    FirebaseAuth auth;

    String verifyCode;

    PhoneAuthProvider.ForceResendingToken forceResendToken;
    int setTiming = 71000;

    Long setTime = 70l;
    int countDoenInterval = 1000;

    //MongoDb
    App app;
    MongoDatabase mongoDatabase;
    MongoClient mongoClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVerifyOtpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Initlize data
        country_code = getIntent().getStringExtra("country_code");
        phone_no = getIntent().getStringExtra("phone_no");
        number = country_code+phone_no;
        country_short_name = getIntent().getStringExtra("country_short_name");

        userObjId = getIntent().getStringExtra("_id");

        name = getIntent().getStringExtra("name");
        email = getIntent().getStringExtra("email");
        password = getIntent().getStringExtra("password");

        auth = FirebaseAuth.getInstance();

        //MongoDB
        Realm.init(this);
        app = new App(new AppConfiguration.Builder(getString(R.string.MONGO_APP_ID)).build());

        //set background and text at runtime for using that method
        changeEditTextOTP();

        //set number in textview that show number for sending otp
        setNumber();

        //set timing fand set to TextView & After time over that visible Resend Button TextView
        setTimer();

        //send otp orbResend otp
        sendOTP(number,false);

    }

    private void sendOTP(String number, boolean isResend) {

        if(isResend){
            binding.btnVerifyOtpResendCode.setVisibility(View.INVISIBLE);
            binding.txtTimer.setVisibility(View.VISIBLE);
            setTimer();
        }

        isProcess(true);
        PhoneAuthOptions.Builder builder = new PhoneAuthOptions.Builder(auth)
                .setPhoneNumber(number)
                .setTimeout(setTime, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                        isProcess(false);
                        Toast.makeText(VerifyOtp.this, "Send otp successfully", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Log.e("GetOtpErr",e.getMessage());
                        isProcess(false);
                    }

                    @Override
                    public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(s, forceResendingToken);
                        isProcess(false);
                        verifyCode = s;
                        forceResendToken = forceResendingToken;
                    }

                });
        if(isResend){
            PhoneAuthProvider.verifyPhoneNumber(builder.setForceResendingToken(forceResendToken).build());
        }
        else {
            PhoneAuthProvider.verifyPhoneNumber(builder.build());
        }
    }

    private void isProcess(boolean b) {
        if(b){
            binding.verifyOtpProgress.setVisibility(View.VISIBLE);
            binding.btnVerifyOtpContinue.setVisibility(View.INVISIBLE);
        }else {
            binding.verifyOtpProgress.setVisibility(View.INVISIBLE);
        }
    }

    private void setTimer() {
        CountDownTimer timer = new CountDownTimer(setTiming,countDoenInterval) {
            @SuppressLint("DefaultLocale")
            @Override
            public void onTick(long mTimeLeftInMilies) {
                int minute = (int) ((mTimeLeftInMilies / 1000) / 60);
                int second = (int) ((mTimeLeftInMilies / 1000) % 60);

                binding.txtTimer.setText(String.format("%02d:%02d", minute, second));
            }

            @Override
            public void onFinish() {
                    binding.txtTimer.setVisibility(View.INVISIBLE);
                    binding.btnVerifyOtpResendCode.setVisibility(View.VISIBLE);
            }
        }.start();
    }

    private void setNumber() {
        binding.tvNumber.setText(number);
    }



    private void changeEditTextOTP() {
        binding.otpET1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.toString().isEmpty()){
                    binding.otpET1.setBackgroundResource(R.drawable.button_background);
                    binding.otpET1.setTextColor(Color.WHITE);
                    binding.otpET2.requestFocus();
                }
                else {
                    binding.otpET1.setBackgroundResource(R.drawable.edittext_background);
                    binding.otpET1.setTextColor(Color.BLACK);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.otpET2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.toString().isEmpty()){
                    binding.otpET2.setBackgroundResource(R.drawable.button_background);
                    binding.otpET2.setTextColor(Color.WHITE);
                    binding.otpET3.requestFocus();
                }else {
                    binding.otpET2.setBackgroundResource(R.drawable.edittext_background);
                    binding.otpET2.setTextColor(Color.BLACK);
                    binding.otpET1.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.otpET3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.toString().isEmpty()){
                    binding.otpET3.setBackgroundResource(R.drawable.button_background);
                    binding.otpET3.setTextColor(Color.WHITE);
                    binding.otpET4.requestFocus();
                }
                else {
                    binding.otpET3.setBackgroundResource(R.drawable.edittext_background);
                    binding.otpET3.setTextColor(Color.BLACK);
                    binding.otpET2.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.otpET4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.toString().isEmpty()){
                    binding.otpET4.setBackgroundResource(R.drawable.button_background);
                    binding.otpET4.setTextColor(Color.WHITE);
                    binding.otpET5.requestFocus();
                }
                else {
                    binding.otpET4.setBackgroundResource(R.drawable.edittext_background);
                    binding.otpET4.setTextColor(Color.BLACK);
                    binding.otpET3.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.otpET5.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.toString().isEmpty()){
                    binding.otpET5.setBackgroundResource(R.drawable.button_background);
                    binding.otpET5.setTextColor(Color.WHITE);
                    binding.otpET6.requestFocus();
                }
                else {
                    binding.otpET5.setBackgroundResource(R.drawable.edittext_background);
                    binding.otpET5.setTextColor(Color.BLACK);
                    binding.otpET4.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.otpET6.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.toString().isEmpty()){
                    binding.btnVerifyOtpContinue.setVisibility(View.INVISIBLE);
                    binding.otpET6.setBackgroundResource(R.drawable.edittext_background);
                    binding.otpET6.setTextColor(Color.BLACK);
                    binding.otpET5.requestFocus();
                }
                else {
                    binding.otpET6.setBackgroundResource(R.drawable.button_background);
                    binding.otpET6.setTextColor(Color.WHITE);
                    binding.btnVerifyOtpContinue.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    public void btnResendClick(View view) {
        sendOTP(number,true);
    }

    public void btnEditClicked(View view) {
        onBackPressed();
    }

    public void btnContinueClick(View view) {

        String enterdCode = binding.otpET1.getText().toString() +
                binding.otpET2.getText().toString() +
                binding.otpET3.getText().toString() +
                binding.otpET4.getText().toString() +
                binding.otpET5.getText().toString() +
                binding.otpET6.getText().toString();

        binding.verifyOtpProgress.setVisibility(View.VISIBLE);
        binding.btnVerifyOtpContinue.setVisibility(View.INVISIBLE);

        if(verifyCode!=null) {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verifyCode, enterdCode);

            auth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        if(userObjId==null) {
                            //set Intent method from home page
                            app.getEmailPassword().registerUserAsync(email, password, new App.Callback<Void>() {
                                @Override
                                public void onResult(App.Result<Void> result) {
                                    if (result.isSuccess()) {

                                        Credentials credentials = Credentials.emailPassword(email, password);
                                        app.loginAsync(credentials, new App.Callback<User>() {
                                            @Override
                                            public void onResult(App.Result<User> result) {
                                                if (result.isSuccess()) {
                                                    if (name != null && email != null && password != null & phone_no != null) {
                                                        // Retrieve the authenticated user
                                                        User user = app.currentUser();

                                                        if (user != null) {
                                                            // User is authenticated, proceed with MongoDB operations

                                                            mongoClient = app.currentUser().getMongoClient(getString(R.string.MONGO_DB_SERVICE_NAME));
                                                            mongoDatabase = mongoClient.getDatabase(getString(R.string.MONGO_DATABASE_NAME));
                                                            MongoCollection<Document> collection = mongoDatabase.getCollection(getString(R.string.MONGO_DB_USER_COLLECTION));


                                                            Document data = new Document("user_id", user.getId())
                                                                    .append("name", name)
                                                                    .append("email", email)
                                                                    .append("phone_no", phone_no)
                                                                    .append("password", password)
                                                                    .append("provider", user.getProviderType().name())
                                                                    .append("img_url", null)
                                                                    .append("balance", 10000.0)
                                                                    .append("user_block",false);

                                                            collection.insertOne(data).getAsync(new App.Callback<InsertOneResult>() {
                                                                @Override
                                                                public void onResult(App.Result<InsertOneResult> result) {
                                                                    if (result.isSuccess()) {

                                                                        // MongoDB insertion successful
                                                                        Toast.makeText(VerifyOtp.this, "Sign Up Successfully", Toast.LENGTH_SHORT).show();
                                                                        binding.verifyOtpProgress.setVisibility(View.INVISIBLE);
                                                                        Intent intent = new Intent(VerifyOtp.this, HomePage.class);
                                                                        intent.putExtra("user_id", user.getId());
                                                                        intent.putExtra("email", email);
                                                                        startActivity(intent);
                                                                        finishAffinity();
                                                                    } else {
                                                                        binding.verifyOtpProgress.setVisibility(View.INVISIBLE);
                                                                        binding.btnVerifyOtpContinue.setVisibility(View.VISIBLE);
                                                                        Log.e("VerifyOtpDatabaseErr", result.getError().toString());
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    } else {
                                                        binding.verifyOtpProgress.setVisibility(View.INVISIBLE);
                                                        binding.btnVerifyOtpContinue.setVisibility(View.VISIBLE);
                                                        Log.e("VerifyOtp:line_393", "Null value");
                                                    }
                                                } else {
                                                    binding.verifyOtpProgress.setVisibility(View.INVISIBLE);
                                                    binding.btnVerifyOtpContinue.setVisibility(View.VISIBLE);
                                                    Log.e("ErrLoginVerifyOtp", result.getError().toString());
                                                }
                                            }
                                        });

                                    } else {
                                        binding.errOtpVerify.setVisibility(View.INVISIBLE);
                                        binding.btnVerifyOtpContinue.setVisibility(View.VISIBLE);
                                        binding.verifyOtpProgress.setVisibility(View.INVISIBLE);
                                        Log.e("RegisterEmail", result.getError().toString());
                                    }
                                }
                            });
                        }else{
                            onBackPressed();
                        }
                    } else {
                        binding.errOtpVerify.setVisibility(View.INVISIBLE);
                        binding.errOtpVerify.setText("Invalid OTP!");
                        binding.errOtpVerify.setVisibility(View.VISIBLE);
                        binding.btnVerifyOtpContinue.setVisibility(View.VISIBLE);
                    }
                }
            });
        }else{
            binding.errOtpVerify.setVisibility(View.INVISIBLE);
            binding.btnVerifyOtpContinue.setVisibility(View.VISIBLE);
            Toast.makeText(this, "VerifyCode is null", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
            super.onBackPressed();

    }
}