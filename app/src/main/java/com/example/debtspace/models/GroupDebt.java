package com.example.debtspace.models;

import com.example.debtspace.config.AppConfig;

import java.util.List;
import java.util.Map;

public class GroupDebt extends Debt {

    private String id;
    private String name;
    private List<String> members;

    public GroupDebt() {}

    @SuppressWarnings("unchecked")
    public GroupDebt(Map<String, Object> map) {
        this.name = (String) map.get(AppConfig.NAME_KEY);
        this.debt = (String) map.get(AppConfig.DEBT_KEY);
        this.date = (String) map.get(AppConfig.DATE_KEY);
        this.members = (List<String>) map.get(AppConfig.MEMBERS_KEY);
    }

    public GroupDebt(String id, String name, String debt, String date, List<String> members) {
        this.id = id;
        this.name = name;
        this.debt = debt;
        this.date = date;
        this.members = members;
    }

    public GroupDebt(String id) {
        this.id = id;
    }

    public void setID(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDebt() {
        return debt;
    }

    public List<String> getMembers() {
        return members;
    }
}
