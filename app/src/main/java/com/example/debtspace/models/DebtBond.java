package com.example.debtspace.models;

import com.example.debtspace.config.AppConfig;

import java.util.Map;

public class DebtBond {

    private String username;
    private String debt;
    private String date;

    public DebtBond() {}

    public DebtBond(String username, Map<String, Object> data) {
        this.username = username;
        this.debt = (String) data.get(AppConfig.DEBT_KEY);
        this.date = (String) data.get(AppConfig.DATE_KEY);
    }

    public DebtBond(String date, String debt) {
        this.date = date;
        this.debt = debt;
    }

    public String getDate() {
        return date;
    }

    public String getDebt() {
        return debt;
    }

    public String getUsername() {
        return username;
    }
}
