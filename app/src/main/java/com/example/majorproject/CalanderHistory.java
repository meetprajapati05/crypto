package com.example.majorproject;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.majorproject.databinding.ActivityCalanderHistoryBinding;

import java.util.Calendar;

public class CalanderHistory extends AppCompatActivity {

    ActivityCalanderHistoryBinding binding;

    String id;
    String date;
    DatePickerDialog datePickerDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCalanderHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        id=getIntent().getStringExtra("id");

        // initialising the calendar
        final Calendar calendar = Calendar.getInstance();

        final int day = calendar.get(Calendar.DAY_OF_MONTH);
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);

        // initialising the datepickerdialog
        datePickerDialog = new DatePickerDialog(CalanderHistory.this);

        // click on edittext to set the value
        binding.idEdtDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog = new DatePickerDialog(CalanderHistory.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(android.widget.DatePicker view, int year, int month, int dayOfMonth) {
                        // adding the selected date in the edittext
                        binding.idEdtDate.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                        date = dayOfMonth + "-" + (month + 1) + "-" + year;
                    }
                }, year, month, day);
                datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
                datePickerDialog.show();
            }
        });
        binding.getdetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BottomSheetFragment bottomSheetFragment=new BottomSheetFragment(date);
                bottomSheetFragment.show(getSupportFragmentManager(),bottomSheetFragment.getTag());
            }

        });
    }
}