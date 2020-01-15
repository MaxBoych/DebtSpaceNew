package com.example.debtspace.models;

import com.example.debtspace.config.AppConfig;

import java.util.Map;

public class HistoryItem implements Comparable<HistoryItem> {

    private String id;
    private String debt;
    private String name;
    private String username;
    private String date;
    private String comment;

    public HistoryItem() {}

    public HistoryItem(String id, Map<String, Object> map) {
        this.id = id;
        debt = (String) map.get(AppConfig.DEBT_KEY);
        name = (String) map.get(AppConfig.NAME_KEY);
        comment = (String) map.get(AppConfig.COMMENT_KEY);
        date = (String) map.get(AppConfig.DATE_KEY);
        username = (String) map.get(AppConfig.USERNAME_KEY);
    }

    public HistoryItem(String id, String debt, String comment, String date, String username) {
        this.id = id;
        this.debt = debt;
        this.comment = comment;
        this.date = date;
        this.username = username;
    }

    public HistoryItem(String debt, String comment, String date, String username) {
        this.debt = debt;
        this.comment = comment;
        this.date = date;
        this.username = username;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
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

