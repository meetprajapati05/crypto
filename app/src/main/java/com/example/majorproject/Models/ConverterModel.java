package com.example.majorproject.Models;

public class ConverterModel {
    String ContryWithCurrency;
    String CurrencyType;

    public ConverterModel(String contryWithCurrency, String currencyType) {
        ContryWithCurrency = contryWithCurrency;
        CurrencyType = currencyType;
    }

    public ConverterModel() {
    }

    public String getContryWithCurrency() {
        return ContryWithCurrency;
    }

    public void setContryWithCurrency(String contryWithCurrency) {
        ContryWithCurrency = contryWithCurrency;
    }

    public String getCurrencyType() {
        return CurrencyType;
    }

    public void setCurrencyType(String currencyType) {
        CurrencyType = currencyType;
    }
}
