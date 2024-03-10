package com.example.majorproject.Models;

public class HistoryModel {
    String action;
    String action_date_and_time;
    double action_time_coin_value;
    String coin_id;
    String coin_symbol;
    String coin_name;
    int coin_quntity;
    double money_flow;
    double user_balance;
    int purchase_leverage_in;

    public HistoryModel() {
    }

    public HistoryModel(String action, String action_date_and_time, double action_time_coin_value, String coin_id, String coin_symbol, String coin_name, int coin_quntity, double money_flow, double user_balance, int purchase_leverage_in) {
        this.action = action;
        this.action_date_and_time = action_date_and_time;
        this.action_time_coin_value = action_time_coin_value;
        this.coin_id = coin_id;
        this.coin_symbol = coin_symbol;
        this.coin_name = coin_name;
        this.coin_quntity = coin_quntity;
        this.money_flow = money_flow;
        this.user_balance = user_balance;
        this.purchase_leverage_in = purchase_leverage_in;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getAction_date_and_time() {
        return action_date_and_time;
    }

    public void setAction_date_and_time(String action_date_and_time) {
        this.action_date_and_time = action_date_and_time;
    }

    public double getAction_time_coin_value() {
        return action_time_coin_value;
    }

    public void setAction_time_coin_value(double action_time_coin_value) {
        this.action_time_coin_value = action_time_coin_value;
    }

    public String getCoin_id() {
        return coin_id;
    }

    public void setCoin_id(String coin_id) {
        this.coin_id = coin_id;
    }

    public String getCoin_symbol() {
        return coin_symbol;
    }

    public void setCoin_symbol(String coin_symbol) {
        this.coin_symbol = coin_symbol;
    }

    public String getCoin_name() {
        return coin_name;
    }

    public void setCoin_name(String coin_name) {
        this.coin_name = coin_name;
    }

    public int getCoin_quntity() {
        return coin_quntity;
    }

    public void setCoin_quntity(int coin_quntity) {
        this.coin_quntity = coin_quntity;
    }

    public double getMoney_flow() {
        return money_flow;
    }

    public void setMoney_flow(double money_flow) {
        this.money_flow = money_flow;
    }

    public double getUser_balance() {
        return user_balance;
    }

    public void setUser_balance(double user_balance) {
        this.user_balance = user_balance;
    }

    public int getPurchase_leverage_in() {
        return purchase_leverage_in;
    }

    public void setPurchase_leverage_in(int purchase_leverage_in) {
        this.purchase_leverage_in = purchase_leverage_in;
    }
}
