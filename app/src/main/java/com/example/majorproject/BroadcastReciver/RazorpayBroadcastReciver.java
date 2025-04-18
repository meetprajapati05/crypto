package com.example.majorproject.BroadcastReciver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RazorpayBroadcastReciver extends BroadcastReceiver {

    public static final String ACTION_PAYMENT_CONFIRMED = "com.razorpay.ACTION_PAYMENT_CONFIRMED";
    public static final String ACTION_OTP_RECEIVED = "com.razorpay.ACTION_OTP_RECEIVED";


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null) {
            switch (action) {
                case ACTION_PAYMENT_CONFIRMED:
                    // Handle payment confirmation
                    handlePaymentConfirmed(context, intent);
                    break;
                case ACTION_OTP_RECEIVED:
                    // Handle OTP received for payment verification
                    handleOTPReceived(context, intent);
                    break;
                default:
                    // Ignore unrecognized actions
                    break;
            }
        }

    }
    private void handlePaymentConfirmed(Context context, Intent intent) {
        // Extract payment details from the intent and process them
        String paymentId = intent.getStringExtra("paymentId");
        // Process the payment confirmation...
    }

    private void handleOTPReceived(Context context, Intent intent) {
        // Extract OTP message from the intent and initiate OTP verification
        String otpMessage = intent.getStringExtra("otpMessage");
        // Initiate OTP verification process...
    }
}
