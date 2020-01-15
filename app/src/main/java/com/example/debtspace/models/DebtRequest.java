package com.example.debtspace.models;

import android.os.Bundle;

import com.example.debtspace.config.AppConfig;

import java.util.Map;

public class DebtRequest extends Notification {

    private String debt;
    private String debtDate;

    public DebtRequest() {}

    public DebtRequest(String id) {
        this.id = id;
    }

    public DebtRequest(Bundle bundle) {
        this.id = bundle.getString(AppConfig.ID_KEY);
        this.name = bundle.getString(AppConfig.NAME_KEY);
        this.username = bundle.getString(AppConfig.USERNAME_KEY);
        this.debt = bundle.getString(AppConfig.DEBT_KEY);
        this.date = bundle.getString(AppConfig.DATE_KEY);
        this.debtDate = bundle.getString(AppConfig.DEBT_DATE_KEY);
    }

    public DebtRequest(String id, Map<String, Object> data) {
        this.id = id;
        this.name = (String) data.get(AppConfig.NAME_KEY);
        this.username = (String) data.get(AppConfig.USERNAME_KEY);
        this.debt = (String) data.get(AppConfig.DEBT_KEY);
        this.date = (String) data.get(AppConfig.DATE_KEY);
        this.debtDate = (String) data.get(AppConfig.DEBT_DATE_KEY);
    }

    public DebtRequest(String id, User user, Map<String, Object> data) {
        this.id = id;
        this.name = user.getFirstName() + " " + user.getLastName();
        this.username = user.getUsername();
        this.debt = (String) data.get(AppConfig.DEBT_KEY);
        this.date = (String) data.get(AppConfig.DATE_KEY);
        this.debtDate = (String) data.get(AppConfig.DEBT_DATE_KEY);
    }

    public void setDebt(String debt) {
        this.debt = debt;
    }

    public String getDebt() {
        return debt;
    }

    public void setDebtDate(String debtDate) {
        this.debtDate = debtDate;
    }

    public String getDebtDate() {
        return debtDate;
    }
}
