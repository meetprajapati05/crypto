package com.example.majorproject.Models;

public class PortfolioModel {
    String coin_id;
    String coin_name;
    String coin_symbol;
    double purchase_time_coin_price;
    int coin_quantity;
    String purchase_date_and_time;
    double purchase_value;
    int purchase_leverage_in;
    double set_profit;
    double stop_lose;

    public PortfolioModel() {
    }

    public PortfolioModel(String coin_id, String coin_name, String coin_symbol, double purchase_time_coin_price, int coin_quantity, String purchase_date_and_time, double purchase_value, int purchase_leverage_in, double set_profit, double stop_lose) {
        this.coin_id = coin_id;
        this.coin_name = coin_name;
        this.coin_symbol = coin_symbol;
        this.purchase_time_coin_price = purchase_time_coin_price;
        this.coin_quantity = coin_quantity;
        this.purchase_date_and_time = purchase_date_and_time;
        this.purchase_value = purchase_value;
        this.purchase_leverage_in = purchase_leverage_in;
        this.set_profit = set_profit;
        this.stop_lose = stop_lose;
    }

    public String getCoin_id() {
        return coin_id;
    }

    public void setCoin_id(String coin_id) {
        this.coin_id = coin_id;
    }

    public String getCoin_name() {
        return coin_name;
    }

    public void setCoin_name(String coin_name) {
        this.coin_name = coin_name;
    }

    public String getCoin_symbol() {
        return coin_symbol;
    }

    public void setCoin_symbol(String coin_symbol) {
        this.coin_symbol = coin_symbol;
    }

    public double getPurchase_time_coin_price() {
        return purchase_time_coin_price;
    }

    public void setPurchase_time_coin_price(double purchase_time_coin_price) {
        this.purchase_time_coin_price = purchase_time_coin_price;
    }

    public int getCoin_quantity() {
        return coin_quantity;
    }

    public void setCoin_quantity(int coin_quantity) {
        this.coin_quantity = coin_quantity;
    }

    public String getPurchase_date_and_time() {
        return purchase_date_and_time;
    }

    public void setPurchase_date_and_time(String purchase_date_and_time) {
        this.purchase_date_and_time = purchase_date_and_time;
    }

    public double getPurchase_value() {
        return purchase_value;
    }

    public void setPurchase_value(double purchase_value) {
        this.purchase_value = purchase_value;
    }

    public int getPurchase_leverage_in() {
        return purchase_leverage_in;
    }

    public void setPurchase_leverage_in(int purchase_leverage_in) {
        this.purchase_leverage_in = purchase_leverage_in;
    }

    public double getSet_profit() {
        return set_profit;
    }

    public void setSet_profit(double set_profit) {
        this.set_profit = set_profit;
    }

    public double getStop_lose() {
        return stop_lose;
    }

    public void setStop_lose(double stop_lose) {
        this.stop_lose = stop_lose;
    }
}
