package com.example.debtspace.models;

import com.example.debtspace.config.AppConfig;

import java.util.Map;

public class HistoryItem implements Comparable<HistoryItem> {

    private String debt;
    private String name;
    private String username;
    private String date;
    private String comment;

    public HistoryItem(Map<String, Object> map, String username) {
        debt = (String) map.get(AppConfig.DEBT_KEY);
        name = (String) map.get(AppConfig.NAME_KEY);
        comment = (String) map.get(AppConfig.COMMENT_KEY);
        date = (String) map.get(AppConfig.DATE_KEY);
        this.username = username;
    }

    public HistoryItem(String debt, String comment, String date) {
        this.debt = debt;
        this.comment = comment;
        this.date = date;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setDebt(String debt) {
        this.debt = debt;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void  setDate(String date) {
        this.date = date;
    }

    public String getDebt() {
        return debt;
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public String getComment() {
        return comment;
    }

    @Override
    public int compareTo(HistoryItem o) {
        return o.date.compareTo(this.date);
    }
}

